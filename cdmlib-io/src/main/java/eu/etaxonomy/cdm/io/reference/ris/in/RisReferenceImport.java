/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.reference.ris.in;

import java.io.InputStreamReader;
import java.net.URI;
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
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.reference.ris.in.RisRecordReader.RisValue;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;

/**
 * @author a.mueller
 * @date 11.05.2017
 *
 */
@Component
public class RisReferenceImport
    extends CdmImportBase<RisReferenceImportConfigurator, RisReferenceImportState>{

    private static final long serialVersionUID = 7022034669942979722L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(RisReferenceImport.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInvoke(RisReferenceImportState state) {
        RisReferenceImportConfigurator config = state.getConfig();
        try {
//            new FileReader(file)
            InputStreamReader inputReader = config.getSource();
            RisRecordReader risReader = new RisRecordReader(state, inputReader);

            Set<Reference> referencesToSave = new HashSet<>();


            Map<RisReferenceTag, List<RisValue>> next = risReader.readRecord();
            while (next != RisRecordReader.EOF){
                Reference ref;
                String location = "";
                try {
                    location = recordLocation(state, next);
                    ref = makeReference(state, next);
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

    /**
     * @param state
     * @param next
     * @return
     */
    private Reference makeReference(RisReferenceImportState state,
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
        Reference higherRef = inRef == null ? ref : inRef;


        //Title
        RisValue t1 = getSingleValue(state, record, RisReferenceTag.T1);
        RisValue ti = getSingleValue(state, record, RisReferenceTag.TI);
        RisValue value = assertEqual(state, "title", t1, ti);
        if (value != null){
            ref.setTitle(value.value);
        }

        //Journal title
        RisValue t2 = getSingleValue(state, record, RisReferenceTag.T2); //Secondary Title (journal title, if applicable)

        if (higherRef.getType() == ReferenceType.Journal){
            RisValue jf = getSingleValue(state, record, RisReferenceTag.JF); //Journal/Periodical name: full format. This is an alphanumeric field of up to 255 characters.
            RisValue jo = getSingleValue(state, record, RisReferenceTag.JO); //Journal/Periodical name: full format. This is an alphanumeric field of up to 255 characters.
            RisValue x = assertEqual(state, "Journal/Periodical name: full format", jf, jo);
            x = assertEqual(state, "Journal title", t2, x);
            if (x != null){
                higherRef.setTitle(x.value);
            }
        }else{
            //TODO
        }

        //ST  (remove as same as TI or T1), not handled otherwise
        RisValue st = getSingleValue(state, record, RisReferenceTag.ST, false); //Short title
        if (st != null && st.value.equals(ref.getTitle())){
            record.remove(RisReferenceTag.ST);
        }

        //Author
        List<RisValue> list = getListValue(record, RisReferenceTag.AU);
        if (!list.isEmpty()){
            TeamOrPersonBase<?> author = makeAuthor(state, list);
            ref.setAuthorship(author);
        }

        //Date
//        RisValue y1 = getSingleValue(state, record, RisReferenceTag.Y1); //Primary Date
        RisValue py = getSingleValue(state, record, RisReferenceTag.PY);
        RisValue da = getSingleValue(state, record, RisReferenceTag.DA);
        Integer year = makeYear(state, py);
        TimePeriod date = makeDate(state, da);
        assertDateYear(state, year, date, py);
        ref.setDatePublished(date);
        //TODO y1 not yet handled

        //Note
        RisValue n1 = getSingleValue(state, record, RisReferenceTag.N1); //Note
        if (n1 != null){
            Annotation annotation = Annotation.NewInstance(n1.value, AnnotationType.EDITORIAL(), Language.DEFAULT());
            ref.addAnnotation(annotation);
        }

        //DOI
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
                state.getResult().addWarning(message, doiVal.location);
            }
        }

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
                state.getResult().addWarning(message, ur.location);
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
        String vol = vl == null? "": vl.value + (is != null ? "("+ is.value + ")": "");
        ref.setVolume(vol);

        //Publisher
        RisValue pb = getSingleValue(state, record, RisReferenceTag.PB);
        if (pb != null){
            higherRef.setPublisher(pb.value);
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
            ref.addImportSource(idStr, null, state.getConfig().getSourceReference(), recLoc);

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

    /**
     * @param ref
     * @return
     */
    private boolean hasInRef(Reference ref) {
        return ref.getType() == ReferenceType.BookSection || ref.getType() == ReferenceType.Article ;
    }


    /**
     * @param state
     * @param record
     * @return
     */
    private String recordLocation(RisReferenceImportState state,
            Map<RisReferenceTag, List<RisValue>> record) {
        RisValue typeTag = this.getSingleValue(state, record, RisReferenceTag.TY, false);
        RisValue erTag = this.getSingleValue(state, record, RisReferenceTag.ER, false);

        String start = typeTag == null ? "??" : typeTag.location;
        String end = erTag == null ? "??" : erTag.location;

        String result = "line " + CdmUtils.concat("-", start, end);

        return result;
    }

    /**
     * @param state
     * @param year
     * @param date
     */
    private void assertDateYear(RisReferenceImportState state, Integer year, TimePeriod date, RisValue py) {
        if (year != null && date != null && !year.equals(date.getStartYear())){
            String message = "Year 'PY' and date 'DA' are not consistent. PY is neglected.";
            state.getResult().addWarning(message, py.location);
        }
    }

    private RisValue assertEqual(RisReferenceImportState state, String meaning, RisValue val1, RisValue val2) {
        if (val1 != null && val2 != null && !val1.value.equals(val2.value)){
            String message = "The tags '%s' and '%s' are not equal but have a similar meaning ('%s'). "
                    + "%s was used and %s neglected";
            message = String.format(message, val1.tag.name(), val2.tag.name(), meaning , val1.tag.name(), val2.tag.name());
            state.getResult().addWarning(message, val1.location);
        }
        return val1 != null ? val1 : val2;
    }

    /**
     * @param state
     * @param da
     * @return
     */
    private TimePeriod makeDate(RisReferenceImportState state, RisValue da) {
        if (da == null){
            return null;
        }
        if (! da.value.matches("([0-9]{4})?(\\/([0-9]{2})?(\\/([0-9]{2})?(\\/.*)?)?)?")){
            String message = "Tag '%s' has incorrect format. Only exactly 'dddd/dd/dd/any text' is allowed (where d is a digit), but was '%s'";
            message = String.format(message, da.tag.name(), da.value);
            state.getResult().addWarning(message, da.location);
            return null;
        }
        String[] split = da.value.split("/");
        TimePeriod tp = TimePeriod.NewInstance();
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

    /**
     * @param state
     * @param py
     * @return
     */
    private Integer makeYear(RisReferenceImportState state, RisValue py) {
        if (py == null){
            return null;
        }
        if (py.value.matches("[0-9]{4}")){
            return Integer.valueOf(py.value);
        }else{
            String message = "Tag '%s' has incorrect format. Only exactly 4 digits are allowed, but was '%s'";
            message = String.format(message, py.tag.name(), py.value);
            state.getResult().addWarning(message, py.location);
            return null;
        }
    }

    /**
     * @param state
     * @param list
     * @return
     */
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

    /**
     * @param state
     * @param risValue
     * @return
     */
    private Person makePerson(RisReferenceImportState state, RisValue risValue) {
        Person person = Person.NewInstance();
        String[] split = risValue.value.split(",");
        if (split.length >= 1){
            person.setLastname(split[0].trim());
        }
        if (split.length >= 2){
            person.setFirstname(split[1].trim());
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

    /**
     * @param state
     * @param list
     * @param tag
     */
    private void assertSingle(RisReferenceImportState state, List<RisValue> list, RisReferenceTag tag) {
        if (list.size() > 1){
            String message = "There is more than 1 tag '%s' but only 1 tag is supported by RIS format or"
                    + " by the current import implementation.";
            message = String.format(message, tag.name());
            state.getResult().addWarning(message, list.get(0).location + "ff");
        }else if (list.isEmpty()){
            state.getResult().addError("A tag list was empty. This should not happen and is a programming code error");
        }
    }

    /**
     * @param state
     * @param next
     * @return
     */
    private ReferenceType makeReferenceType(RisReferenceImportState state,
            Map<RisReferenceTag, List<RisValue>> record) {
        RisReferenceTag tyTag = RisReferenceTag.TY;
        RisValue value = this.getSingleValue(state, record, tyTag, false);
        String typeStr = value.value;
        RisRecordType type = RisRecordType.valueOf(typeStr);
        ReferenceType cdmType = type.getCdmReferenceType();
        return cdmType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doCheck(RisReferenceImportState state) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isIgnore(RisReferenceImportState state) {
        return false;
    }
}
