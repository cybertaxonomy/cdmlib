/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.reference;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.format.CdmFormatterBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalSource;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.strategy.cache.agent.TeamDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.ReferenceDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.TitleWithoutYearAndAuthorHelper;

/**
 * @author a.mueller
 * @since 03.05.2021
 */
public class NomenclaturalSourceFormatter
        extends CdmFormatterBase<NomenclaturalSource>
        implements Serializable {

    private static final long serialVersionUID = -6263443499465634548L;

    private static final Logger logger = LogManager.getLogger();

    private static final String EMPTY_TITLE = "-";
    private static final String beforeMicroReference = ": ";
    private static final String afterInRefAuthor = ", ";

    private static NomenclaturalSourceFormatter instance;

    public static final NomenclaturalSourceFormatter INSTANCE() {
            if (instance == null){
                instance = new NomenclaturalSourceFormatter();
            }
            return instance;
    }

    @Override
    public String format(NomenclaturalSource source){
        if (source == null){
            return null;
        }
        Reference reference = source.getCitation();
        String microReference = source.getCitationMicroReference();
        if (reference == null && isBlank(microReference)){
            return null;
        }
        return format(reference, microReference);
    }

    /**
    * Returns a formatted string containing the entire citation used for
    * nomenclatural purposes based on the {@link Reference reference} supplied - including
    * (abbreviated) title  but not authors - and on the given details.<BR>
    *
    * @param  reference
    *                         the nomenclatural reference
    * @param  microReference  the string with the details (generally pages)
    *                         corresponding to the nomenclatural reference supplied
    *                         as the first argument
    * @return                 the formatted string representing the
    *                         nomenclatural citation
    * @see                    INomenclaturalReference#getNomenclaturalCitation(String)
    * @see                    TaxonName#getNomenclaturalReference()
    */
    public String format(Reference reference, String microReference){

        if (isNotBlank(microReference)){
            microReference = microReference.startsWith(":") ? microReference : getBeforeMicroReference() + microReference;
        }else{
            microReference = "";
        }

        if (reference == null){
            return EMPTY_TITLE + microReference;
        }

        if (reference.isProtectedAbbrevTitleCache()){
            String cache = reference.getAbbrevTitleCache();
            return handleDetailAndYearForProtected(reference, cache, microReference);
        }

        String result = getTitelDetailAndYear(reference, microReference);
        //if no data is available and only titleCache is protected take the protected title
        //this is to avoid empty cache if someone forgets to set also the abbrevTitleCache
        //we need to think about handling protected not separate for abbrevTitleCache and titleCache
        if (result.equals(getDetailOnly(microReference)) && reference.isProtectedTitleCache() ){
            String cache = reference.getTitleCache();
            return handleDetailAndYearForProtected(reference, cache, microReference);
        }

        if (isNotBlank(microReference)){
            microReference = getBeforeMicroReference() + microReference;
            if (microReference.endsWith(".")){
                microReference = microReference.substring(0, microReference.length() - 1);
            }
        }
        if (result.startsWith(". ")){  //only year available, remove '. '
            result = result.substring(2);
        }
        return result;
    }

    private String getBeforeMicroReference(){
        return beforeMicroReference;
    }

    /**
     * Returns the nomenclatural title with micro reference represented as token
     * which can later be replaced by the real data.
     * @param microReference
     */
    private String getTitelDetailAndYear(Reference ref, String microReference) {
        if (ReferenceDefaultCacheStrategy.isRealInRef(ref)){
            return getTokenizedNomenclaturalTitelInRef(ref, microReference);
        }else{
            String result = getTitleWithoutYearAndAuthor(ref, true, true);
            result = isBlank(result) ? getDetailOnly(microReference): isBlank(microReference)? result : isBlank(microReference)? result : result + microReference;
            result = addYear(result, ref, true);
            return result;
        }
    }

    private String getDetailOnly(String microReference) {
        if (isBlank(microReference)){
            return "";
        }else{
            return EMPTY_TITLE + microReference;
        }
    }

    private String handleDetailAndYearForProtected(Reference nomenclaturalReference, String cache, String microRef) {
        if (cache == null){
            logger.warn("Cache is null. This should never be the case.");
            cache = "";
        }
        String  result = cache + (cache.contains(microRef) ? "" : microRef);

        String date = nomenclaturalReference.getDatePublishedString();
        if (isNotBlank(date) && ! result.contains(date)){
            result = result + ReferenceDefaultCacheStrategy.beforeYear + date;
        }
        return result;
    }

    private String getTokenizedNomenclaturalTitelInRef(Reference thisRef, String microReference) {
        if (thisRef == null){
            return null;
        }

        Reference inRef = CdmBase.deproxy(thisRef.getInReference());
        if (inRef != null && inRef.getInReference() != null && thisRef.getType() == ReferenceType.Section){
            //this is a reference of type Section which has an in-in-Ref
            //TODO maybe we do not need to restrict to type=Section only
            return this.getTokenizedNomenclaturalTitelInInRef(thisRef, microReference);
        }

        String result;
        //use generics's publication date if it exists
        if (inRef == null || (thisRef.hasDatePublished() ) ){
            result =  inRef == null ? "" : getTitleWithoutYearAndAuthorGeneric(inRef, true);
            //added //TODO unify with non-inRef references formatting

            if (isNotBlank(thisRef.getVolume())){
                result = result + " " + thisRef.getVolume();
            }
            //TODO series / edition

            //end added
            result += microReference;
            result = addYear(result, thisRef, true);
        }else{
            //else use inRefs's publication date
            result = format(inRef, microReference); // inRef.getNomenclaturalCitation(INomenclaturalReference.MICRO_REFERENCE_TOKEN);
//            if (result != null){
//                result = result.replace(beforeMicroReference + INomenclaturalReference.MICRO_REFERENCE_TOKEN, INomenclaturalReference.MICRO_REFERENCE_TOKEN);
//            }
        }
        //FIXME: vol. etc., https://dev.e-taxonomy.eu/redmine/issues/2862

        result = getInRefAuthorPart(thisRef.getInReference(), afterInRefAuthor) + result;
        result = "in " +  result;
        return result;
    }


    /**
     * For handling in-in-Ref case.
     * Must only be called if a reference has inRef and inInRef
     * @param section
     * @return
     */
    private String getTokenizedNomenclaturalTitelInInRef(Reference ref, String microReference) {
        String result;

        Reference inRef = CdmBase.deproxy(ref.getInReference());
        Reference inInRef = CdmBase.deproxy(inRef.getInReference());

        if (! ReferenceDefaultCacheStrategy.isNomRef(inInRef.getType())){
            if (! ReferenceDefaultCacheStrategy.isNomRef(inRef.getType())){
                logger.warn("Neither inReference nor inInReference is a "
                        + " nomenclatural reference. This is not correct or not handled yet."
                        + " Generic titleWithoutYearAndAuthor used instead");
                result = getTitleWithoutYearAndAuthorGeneric(inInRef, true);
                //FIXME: vol. etc., https://dev.e-taxonomy.eu/redmine/issues/2862  (comment taken from super.getTokenizedNomenclaturalTitel())
            }else{
                result = getTitleWithoutYearAndAuthor(inRef, true, true);
            }
        }else{
            result = getTitleWithoutYearAndAuthor(inInRef, true, true);
        }
        result += microReference;

        Reference dataReference = (ref.hasDatePublished() ? ref : inRef.hasDatePublished() ? inRef : inInRef);

        result = addYear(result, dataReference, true);

        result = getInRefAuthorPart(inInRef, afterInRefAuthor) + result;
        if (! result.startsWith("in ")){
            result = "in " +  result;
        }
        return result;
    }

    /**
     * See https://dev.e-taxonomy.eu/redmine/issues/8881
     */
    private String getInRefAuthorPart(Reference book, String seperator){
        if (book == null){
            return "";
        }

        TeamOrPersonBase<?> author = book.getAuthorship();
        String result;
        if (author == null){
            result = "";
        }else if(author.isInstanceOf(Person.class)){
            Person person = CdmBase.deproxy(author, Person.class);
            result = getInRefPerson(person);
        }else{
            Team team = CdmBase.deproxy(author, Team.class);
            if (team.isProtectedNomenclaturalTitleCache()){
                //not yet finally discussed may change in future
                result = team.getNomenclaturalTitleCache();
            }else if (team.isProtectedTitleCache()){
                //not yet finally discussed may change in future
                result = team.getTitleCache();
            }else if (team.getTeamMembers().isEmpty()){
                //not yet finally discussed may change in future
                result = team.getTitleCache();
            }else{
                result = TeamDefaultCacheStrategy.INSTANCE_ET_AL_3().getFamilyTitle(team);
            }
        }

        result = Nz(result);
        if (! result.trim().equals("")){
            result = result + seperator;
        }
        return result;
    }

    private String getInRefPerson(Person person) {
        String result;
        if (isNotBlank(person.getFamilyName())){
            result = person.getFamilyName();
        }else if (isNotBlank(person.getNomenclaturalTitleCache())){
            result = person.getNomenclaturalTitleCache();  //TODO discuss if nomTitle is really better here then titleCache
        }else{
            result = person.getTitleCache();  //maybe remove everything behind a ","
        }
        return result;
    }

    /**
     * @see ReferenceDefaultCacheStrategy#addYear(String, Reference, boolean)
     */
    private String addYear(String yearStr, Reference ref, boolean useFullDatePublished) {
        return ReferenceDefaultCacheStrategy.addYear(yearStr, ref, useFullDatePublished);
    }
    private String getTitleWithoutYearAndAuthorGeneric(Reference ref, boolean isAbbrev){
        return TitleWithoutYearAndAuthorHelper.getTitleWithoutYearAndAuthorGeneric(ref, isAbbrev);
    }
    private String getTitleWithoutYearAndAuthor(Reference ref, boolean isAbbrev, boolean isNomRef){
        return TitleWithoutYearAndAuthorHelper.getTitleWithoutYearAndAuthor(ref, isAbbrev, isNomRef);
    }
}
