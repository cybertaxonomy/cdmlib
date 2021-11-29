/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.reference.ris.in;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.DOI;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.reference.ris.in.RisRecordReader.RisValue;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @since 11.05.2017
 */
@Component
public class RisReferenceImport
        extends CdmImportBase<RisReferenceImportConfigurator, RisReferenceImportState>{

    private static final long serialVersionUID = 7022034669942979722L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(RisReferenceImport.class);

    @Override
    protected void doInvoke(RisReferenceImportState state) {
        RisReferenceImportConfigurator config = state.getConfig();
        try {
//            new FileReader(file)
            byte[] data = config.getStream();

            ByteArrayInputStream stream = new ByteArrayInputStream(data);
            InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
            RisRecordReader risReader = new RisRecordReader(state, reader);

            Set<Reference> referencesToSave = new HashSet<>();

            Map<RisReferenceTag, List<RisValue>> next = risReader.readRecord();
            while (next != RisRecordReader.EOF){
                Reference ref;
                String location = "";
                try {
                    location = recordLocation(state, next);
                    ref = handleSingleReference(state, next);
                    referencesToSave.add(ref);
                    if (ref.getInReference() != null){
                        referencesToSave.add(ref.getInReference());
                    }
                } catch (Exception e) {
                    String message = "Unexpected exception during RIS Reference Import";
                    state.getResult().addException(e, message, location);
                }

                next = risReader.readRecord();
            }

            getReferenceService().saveOrUpdate(referencesToSave);
            state.getResult().addNewRecords(Reference.class.getSimpleName(), referencesToSave.size());

        } catch (Exception e) {
            String message = "Unexpected exception during RIS Reference Import";
            state.getResult().addException(e, message);
        }

        //unhandled
        Map<RisReferenceTag, Integer> unhandled = state.getUnhandled();
        for (RisReferenceTag tag : unhandled.keySet()){
            String message = "RIS tag %s (%s) not yet handled. n = %d";
            message = String .format(message, tag.name(), tag.getDescription(), unhandled.get(tag));
            state.getResult().addWarning(message);
        }
    }

    private Reference handleSingleReference(RisReferenceImportState state,
            Map<RisReferenceTag, List<RisValue>> record) {

        //type
        ReferenceType type = makeReferenceType(state, record);
        Reference ref = ReferenceFactory.newReference(type);
        Reference inRef = null;
        if (hasInRef(ref)){
            ReferenceType inRefType =
                    type == ReferenceType.Article ? ReferenceType.Journal:
                    type == ReferenceType.BookSection ? ReferenceType.Book :
                        ReferenceType.Generic;
            inRef = ReferenceFactory.newReference(inRefType);
            ref.setInReference(inRef);
        }
        Reference higherRef = (inRef == null) ? ref : inRef;

        //titles
        handleTitle(state, record, ref, inRef, higherRef);

        //authors
        handleAuthors(state, record, ref, inRef);

        //Date
//        RisValue y1 = getSingleValue(state, record, RisReferenceTag.Y1); //Primary Date
        RisValue py = getSingleValue(state, record, RisReferenceTag.PY);
        RisValue da = getSingleValue(state, record, RisReferenceTag.DA);
        Integer year = makeYear(state, py);
        VerbatimTimePeriod date = makeDate(state, da);
        date = assertDateYear(state, year, date, py);
        ref.setDatePublished(date);
        //TODO y1 not yet handled

        //Note
        RisValue n1 = getSingleValue(state, record, RisReferenceTag.N1); //Note
        if (n1 != null){
            Annotation annotation = Annotation.NewInstance(n1.value, AnnotationType.EDITORIAL(), Language.DEFAULT());
            ref.addAnnotation(annotation);
        }

        //DOI
        handleDoi(state, record, ref);

        //UR
        RisValue ur = getSingleValue(state, record, RisReferenceTag.UR); //URL
        if (ur != null){
            URI uri;
            try {
                String urStr = ur.value;
                uri = URI.create(urStr);
                ref.setUri(uri);
            } catch (Exception e) {
                String message = "URL could not be recognized: " + ur.value;
                state.getResult().addWarning(message, null, ur.location);
            }
        }

        //Pages
        RisValue sp = getSingleValue(state, record, RisReferenceTag.SP);
        RisValue ep = getSingleValue(state, record, RisReferenceTag.EP);
        String pages = CdmUtils.concat("-", sp != null ? sp.value : null, ep != null ? ep.value : null);
        ref.setPages(pages);

        //Volume
        RisValue vl = getSingleValue(state, record, RisReferenceTag.VL);
        RisValue is = getSingleValue(state, record, RisReferenceTag.IS);
        String vol = (vl == null)? "": vl.value + (is != null ? "("+ is.value + ")": "");
        if (inRef != null && inRef.getType() == ReferenceType.Book){
            inRef.setVolume(vol);
        }else{
            ref.setVolume(vol);
        }

        //Publisher
        RisValue pb = getSingleValue(state, record, RisReferenceTag.PB);
        if (pb != null){
            higherRef.setPublisher(pb.value);
        }

        //CY - Place published
        RisValue cy = getSingleValue(state, record, RisReferenceTag.CY);
        if (cy != null){
            higherRef.setPlacePublished(cy.value);
        }

        //Abstract
        RisValue ab = getSingleValue(state, record, RisReferenceTag.AB);
        RisValue n2 = getSingleValue(state, record, RisReferenceTag.N2);
        RisValue abst = assertEqual(state, "Abstract", ab, n2);
        if (abst != null){
            ref.setReferenceAbstract(abst.value);
        }

        //ISSN/ISBN
        RisValue sn = getSingleValue(state, record, RisReferenceTag.SN);
        if (sn != null){
            if (higherRef.getType() == ReferenceType.Journal){
                higherRef.setIssn(sn.value);
            }else{
                higherRef.setIsbn(sn.value);
            }
        }

        //ID
        RisValue id = getSingleValue(state, record, RisReferenceTag.ID);
        String idStr = id != null? id.value: null;
        String recLoc = recordLocation(state, record);
        ref.addImportSource(idStr, null, state.getConfig().getSourceReference(), recLoc);
        if (inRef != null){
            inRef.addImportSource(idStr, null, state.getConfig().getSourceReference(), recLoc);
        }

        //remove
        record.remove(RisReferenceTag.ER);
        record.remove(RisReferenceTag.TY);

        for (RisReferenceTag tag : record.keySet()){
//            String message = "RIS Tag " + tag.name() +  " not yet handled";
//            state.getResult().addWarning(message, record.get(tag).get(0).location);
            state.addUnhandled(tag);

            //TODO add as annotation or extension
        }

        return ref;
    }

    private void handleDoi(RisReferenceImportState state, Map<RisReferenceTag, List<RisValue>> record, Reference ref) {
        RisValue doiVal = getSingleValue(state, record, RisReferenceTag.DO); //Doi
        if (doiVal != null){
            DOI doi;
            try {
                String doiStr = doiVal.value;
                if (doiStr.toLowerCase().startsWith("doi ")){
                    doiStr = doiStr.substring(4).trim();
                }
                doi = DOI.fromString(doiStr);
                ref.setDoi(doi);
            } catch (IllegalArgumentException e) {
                String message = "DOI could not be recognized: " + doiVal.value;
                state.getResult().addWarning(message, null, doiVal.location);
            }
        }
    }

    private void handleTitle(RisReferenceImportState state, Map<RisReferenceTag, List<RisValue>> record, Reference ref,
            Reference inRef, Reference higherRef) {
        //Title
        RisValue t1 = getSingleValue(state, record, RisReferenceTag.T1);
        RisValue ti = getSingleValue(state, record, RisReferenceTag.TI);
        RisValue title = assertEqual(state, "title", t1, ti);
        if (title != null){
            ref.setTitle(title.value);
        }

        //Journal title
        RisValue t2 = getSingleValue(state, record, RisReferenceTag.T2); //Secondary Title (journal title, if applicable)

        if (higherRef.getType() == ReferenceType.Journal){
            RisValue jf = getSingleValue(state, record, RisReferenceTag.JF); //Journal/Periodical name: full format. This is an alphanumeric field of up to 255 characters.
            RisValue jo = getSingleValue(state, record, RisReferenceTag.JO); //Journal/Periodical name: full format. This is an alphanumeric field of up to 255 characters.
            RisValue jf_jo = assertEqual(state, "Journal/Periodical name: full format", jf, jo);
            RisValue journalTitle = assertEqual(state, "Journal title", t2, jf_jo);
            if (journalTitle != null){
                higherRef.setTitle(journalTitle.value);
            }
        }else if (t2 != null && inRef != null){
            inRef.setTitle(t2.value);
        }else if (t2 != null){
            String message = "The tag %s ('%s') exists but the reference type usually has no in-reference."
                    + "This part of the title was neglected: %s";
            message = String.format(message, t2.tag.name(), t2.tag.getDescription(), t2.value);
            state.getResult().addWarning(message, null, t2.location);
        }else if (inRef != null){
            String message = "The reference type typically has an inreference but no secondary title (tag T2) was given.";
            state.getResult().addWarning(message, null, (title != null)? title.location : null);
        }

        //ST  (remove as same as TI or T1), not handled otherwise
        RisValue st = getSingleValue(state, record, RisReferenceTag.ST, false); //Short title
        if (st != null && st.value.equals(ref.getTitle())){
            record.remove(RisReferenceTag.ST);
        }
    }

    private void handleAuthors(RisReferenceImportState state, Map<RisReferenceTag, List<RisValue>> record,
            Reference ref, Reference inRef) {
        List<RisValue> authorList = getListValue(record, RisReferenceTag.AU);
        if (!authorList.isEmpty()){
            TeamOrPersonBase<?> author = makeAuthor(state, authorList);
            ref.setAuthorship(author);
        }
        List<RisValue> secondaryAuthorList = getListValue(record, RisReferenceTag.A2);
        if (!secondaryAuthorList.isEmpty()){
            if (inRef != null){
                if (inRef.getType() != ReferenceType.Journal){
                    TeamOrPersonBase<?> secAuthor = makeAuthor(state, secondaryAuthorList);
                    inRef.setAuthorship(secAuthor);
                }else{
                    String message = "The tag %s ('%s') exists but the in-reference type is 'journal' which typically has no author."
                            + "The secondary author(s) was/were neglected: %s";
                    message = String.format(message, RisReferenceTag.AU.name(), RisReferenceTag.AU.getDescription(), secondaryAuthorList.toString());
                    state.getResult().addWarning(message, null, secondaryAuthorList.get(0).location);
                }
            }else{
                String message = "The tag %s ('%s') exists but the reference type usually has no in-reference."
                        + "The secondary author(s) was/were neglected: %s";
                message = String.format(message, RisReferenceTag.AU.name(), RisReferenceTag.AU.getDescription(), secondaryAuthorList.toString());
                state.getResult().addWarning(message, null, secondaryAuthorList.get(0).location);
            }
        }
    }

    private boolean hasInRef(Reference ref) {
        return ref.getType() == ReferenceType.BookSection || ref.getType() == ReferenceType.Article ;
    }

    private String recordLocation(RisReferenceImportState state,
            Map<RisReferenceTag, List<RisValue>> record) {
        RisValue typeTag = this.getSingleValue(state, record, RisReferenceTag.TY, false);
        RisValue erTag = this.getSingleValue(state, record, RisReferenceTag.ER, false);

        String start = typeTag == null ? "??" : typeTag.location;
        String end = erTag == null ? "??" : erTag.location;

        String result = "line " + CdmUtils.concat(" - ", start, end);

        return result;
    }

    private VerbatimTimePeriod assertDateYear(RisReferenceImportState state, Integer year, VerbatimTimePeriod date, RisValue py) {
        if (year == null && date == null){
            return null;
        }else if (year == null){
            return date;
        }else if (date == null){
            return TimePeriodParser.parseStringVerbatim(String.valueOf(year));
        }else{
            if  (!year.equals(date.getStartYear())){
                if (date.getStartYear() == null){
                    date.setStartYear(year);
                }else if (isNotBlank(date.getFreeText())){
                    date.setStartYear(year);  //does this happen at all?
                    String message = "Year 'PY' and date 'DA' are not consistent. PY is neglected.";
                    state.getResult().addWarning(message, null, py.location);
                    return date;
                }else{
                    String message = "Year 'PY' and date 'DA' are not consistent. DA is used for freetext and PY is used for (start) year.";
                    state.getResult().addWarning(message, null, py.location);
                    return date;
                }
            }
            return date;
        }
    }

    /**
     * If val1 and val2 are both <code>not null</code> and not equal a warning is logged.
     * @return val1 if val1 is not null, val2 otherwise
     */
    private RisValue assertEqual(RisReferenceImportState state, String meaning, RisValue val1, RisValue val2) {
        if (val1 != null && val2 != null && !val1.value.equals(val2.value)){
            String message = "The tags '%s' and '%s' are not equal but have a similar meaning ('%s'). "
                    + "%s was used and %s neglected";
            message = String.format(message, val1.tag.name(), val2.tag.name(), meaning , val1.tag.name(), val2.tag.name());
            state.getResult().addWarning(message, null, val1.location);
        }
        return val1 != null ? val1 : val2;
    }

    private VerbatimTimePeriod makeDate(RisReferenceImportState state, RisValue da) {
        if (da == null){
            return null;
        }
        if (! da.value.matches("([0-9]{4})?(\\/([0-9]{2})?(\\/([0-9]{2})?(\\/.*)?)?)?")){
            String message = "Tag '%s' has incorrect format. Only exactly 'dddd/dd/dd/any text' is allowed (where d is a digit), but was '%s'";
            message = String.format(message, da.tag.name(), da.value);
            state.getResult().addWarning(message, null, da.location);
            return null;
        }
        String[] split = da.value.split("/");
        VerbatimTimePeriod tp = VerbatimTimePeriod.NewVerbatimInstance();
        if (split.length > 0 && isNotBlank(split[0])){
            tp.setStartYear(Integer.valueOf(split[0]));
        }
        if (split.length > 1 && isNotBlank(split[1])){
            tp.setStartMonth(Integer.valueOf(split[1]));
        }
        if (split.length > 2 && isNotBlank(split[2])){
            tp.setStartDay(Integer.valueOf(split[2]));
        }
        if (split.length > 3 && isNotBlank(split[3])){
            List<String> other = Arrays.asList(split).subList(3, split.length);
            String otherStr = CdmUtils.concat("/", other.toArray(new String[other.size()]));
            tp.setFreeText(tp.toString() + " " + otherStr);
        }
        return tp;
    }

    private Integer makeYear(RisReferenceImportState state, RisValue py) {
        if (py == null){
            return null;
        }
        if (py.value.matches("[0-9]{4}")){
            return Integer.valueOf(py.value);
        }else{
            String message = "Tag '%s' has incorrect format. Only exactly 4 digits are allowed, but was '%s'";
            message = String.format(message, py.tag.name(), py.value);
            state.getResult().addWarning(message, null, py.location);
            return null;
        }
    }

    private TeamOrPersonBase<?> makeAuthor(RisReferenceImportState state, List<RisValue> list) {
        if (list.size() == 1){
            return makePerson(state, list.get(0));
        }else{
            Team team = Team.NewInstance();
            for (RisValue value : list){
                team.addTeamMember(makePerson(state, value));
            }
            return team;
        }
    }

    private Person makePerson(RisReferenceImportState state, RisValue risValue) {
        Person person = Person.NewInstance();
        String[] split = risValue.value.split(",");
        if (split.length >= 1){
            person.setFamilyName(split[0].trim());
        }
        if (split.length >= 2){
            String givenNameOrInitial = split[1].trim();
            if (givenNameOrInitial.matches("[A-Za-z]\\.(\\s*[A-Za-z]\\.)*")){
                person.setInitials(givenNameOrInitial);
            }else{
                person.setGivenName(givenNameOrInitial);
            }
        }
        if (split.length >= 3){
            person.setSuffix(split[2].trim());
        }

        return person;
    }

    /**
     * Returns the single value for the given tag
     * and removes the tag from the record.
     * If more than 1 value exists this is logged
     * as a warning.
     */
    private RisValue getSingleValue(RisReferenceImportState state,
            Map<RisReferenceTag, List<RisValue>> record,
            RisReferenceTag tag) {
        return getSingleValue(state, record, tag, true);
    }

    /**
     * Returns the single value for the given tag
     * and removes the tag from the record.
     * If more than 1 value exists this is logged
     * as a warning.
     */
    private RisValue getSingleValue(RisReferenceImportState state,
            Map<RisReferenceTag, List<RisValue>> record,
            RisReferenceTag tag, boolean remove) {

        List<RisValue> list = record.get(tag);
        if (list == null){
            return null;
        }
        assertSingle(state, list, tag);
        if (remove){
            record.remove(tag);
        }
        return list.get(0);
    }

    private List<RisValue> getListValue(Map<RisReferenceTag, List<RisValue>> record,
            RisReferenceTag tag) {

        List<RisValue> list = record.get(tag);
        record.remove(tag);
        if (list == null){
            list = new ArrayList<>();
        }
        return list;
    }

    private void assertSingle(RisReferenceImportState state, List<RisValue> list, RisReferenceTag tag) {
        if (list.size() > 1){
            String message = "There is more than 1 tag '%s' but only 1 tag is supported by RIS format or"
                    + " by the current import implementation.";
            message = String.format(message, tag.name());
            state.getResult().addWarning(message, null, list.get(0).location + "ff");
        }else if (list.isEmpty()){
            state.getResult().addError("A tag list was empty. This should not happen and is a programming code error");
        }
    }

    private ReferenceType makeReferenceType(RisReferenceImportState state,
            Map<RisReferenceTag, List<RisValue>> record) {
        RisReferenceTag tyTag = RisReferenceTag.TY;
        RisValue value = this.getSingleValue(state, record, tyTag, false);
        String typeStr = value.value;
        RisRecordType type = RisRecordType.valueOf(typeStr);
        ReferenceType cdmType = type.getCdmReferenceType();
        return cdmType;
    }

    @Override
    protected boolean doCheck(RisReferenceImportState state) {
        return true;
    }

    @Override
    protected boolean isIgnore(RisReferenceImportState state) {
        return false;
    }
}
