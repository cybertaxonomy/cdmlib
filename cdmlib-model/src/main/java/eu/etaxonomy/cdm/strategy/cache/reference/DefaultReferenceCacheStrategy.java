/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.reference;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.strategy.StrategyBase;

/**
 * #5833
 * The new single default cache strategy for {@link Reference references}.
 * As we do have only one {@link Reference} class left which implements multiple interfaces,
 * we may also only need 1 single cache strategy. However, care must be taken as the formatting
 * differs dependent on the type an the in-reference structure.
 *
 * Generally the cache strategy computes allows 3 formattings:<BR>
 *
 *  1.) for bibliographic references (stored in {@link Reference#getTitleCache() titleCache}).<BR>
 *
 *  2.) for nomenclatural references (stored in {@link Reference#getAbbrevTitleCache() abbrevTitleCache}),
 *      but without micro reference (detail).<BR>
 *
 *  3.) for nomenclatural references with micro reference, but not stored anywhere as the micro reference
 *      is part of the name, not of the reference<BR>
 *
 *  4.) for short citation (e.g. Author 2009) as defined in {@link IReferenceCacheStrategy#getCitation(Reference)}
 *
 * @author a.mueller
 * @date 25.05.2016
 *
 */
public class DefaultReferenceCacheStrategy extends StrategyBase implements INomenclaturalReferenceCacheStrategy{
    private static final long serialVersionUID = 6773742298840407263L;

    private static final Logger logger = Logger.getLogger(DefaultReferenceCacheStrategy.class);

    private final static UUID uuid = UUID.fromString("63e669ca-c6be-4a8a-b157-e391c22580f9");

    //article
    public static final String UNDEFINED_JOURNAL = "- undefined journal -";
    private static final String afterAuthor = ", ";

    //book


    //(book?) section
    private String afterSectionAuthor = " - ";

    //in reference
    private String inSeparator = "in ";
    private static final String afterInRefAuthor = ", ";

    //common
    private static final String blank = " ";
    private static final String beforeYear = ". ";
    private static final String beforeMicroReference = ": ";
    private static final String afterYear = "";


    private static final boolean trim = true;

// ************************ FACTORY ****************************/

    /**
     * Factory method
     * @return
     */
    public static DefaultReferenceCacheStrategy NewInstance(){
        return new DefaultReferenceCacheStrategy();
    }


    @Override
    protected UUID getUuid() {
        return uuid;
    }

// ******************************* Main methods ******************************/

    @Override
    public String getTitleCache(Reference reference) {
        if (reference == null){
            return null;
        }
        if (reference.isProtectedTitleCache()){
            return reference.getTitleCache();
        }
        boolean isNotAbbrev = false;

        String result;
        ReferenceType type = reference.getType();

        if (isRealInRef(reference)){
            result = titleCacheRealInRef(reference, isNotAbbrev);
        }else if(isNomRef(type)){
            //all Non-InRef NomRefs
            result =  getTitleWithoutYearAndAuthor(reference, isNotAbbrev);
            result = addPages(result, reference);
            result = addYear(result, reference, false);
            TeamOrPersonBase<?> team = reference.getAuthorship();

            if (type == ReferenceType.Article){
                result = CdmUtils.concat(" ", reference.getTitle(), result);
                if (team != null &&  isNotBlank(team.getTitleCache())){
                    String authorSeparator = isNotBlank(reference.getTitle())? afterAuthor : " ";
                    result = team.getTitleCache() + authorSeparator + result;
                }
            }else{  //if Book, CdDvd, flat Generic, Thesis, WebPage
                if (team != null){
                    String teamTitle = CdmUtils.getPreferredNonEmptyString(team.getTitleCache(),
                            team.getNomenclaturalTitle(), isNotAbbrev, trim);
                    if (teamTitle.length() > 0 ){
                        String concat = isNotBlank(result) ? afterAuthor : "";
                        result = teamTitle + concat + result;
                    }
                }
            }
        }else if (type == ReferenceType.Journal){
            result = titleCacheJournal(reference, isNotAbbrev);
        }else{
            result = titleCacheDefaultReference(reference, isNotAbbrev);
        }
        return result;
    }


    /**
     * @param result
     * @param reference
     * @return
     */
    private String addPages(String result, Reference reference) {
        //pages
        if (isNotBlank(reference.getPages())){
            //Removing trailing added just in case, maybe not necessary
            result = RemoveTrailingDot(Nz(result)).trim() + ": " + reference.getPages();
        }
        return result;
    }


    /**
     * @param nz
     * @return
     */
    private static String RemoveTrailingDot(String str) {
        if (str != null && str.endsWith(".")){
            str = str.substring(0, str.length()-1);
        }
        return str;
    }


    @Override
    public String getFullAbbrevTitleString(Reference reference) {
        if (reference == null){
            return null;
        }
        String result;
        ReferenceType type = reference.getType();
        boolean isAbbrev = true;

        if (reference.isProtectedAbbrevTitleCache()){
            return reference.getAbbrevTitleCache();
        }

        if (type == ReferenceType.Article){
            result =  getTitleWithoutYearAndAuthor(reference, isAbbrev);
            boolean useFullDatePublished = false;
            result = addYear(result, reference, useFullDatePublished);
            TeamOrPersonBase<?> team = reference.getAuthorship();
            String articleTitle = CdmUtils.getPreferredNonEmptyString(reference.getTitle(),
                    reference.getAbbrevTitle(), isAbbrev, trim);
            result = CdmUtils.concat(" ", articleTitle, result);  //Article should maybe left out for nomenclatural references (?)
            if (team != null &&  isNotBlank(team.getNomenclaturalTitle())){
                String authorSeparator = isNotBlank(articleTitle) ? afterAuthor : " ";
                result = team.getNomenclaturalTitle() + authorSeparator + result;
            }
        }else if (isRealInRef(reference)){
            result = titleCacheRealInRef(reference, isAbbrev);
        }else if (isNomRef(type)){
            //FIXME same as titleCache => try to merge, but note article case
            result =  getTitleWithoutYearAndAuthor(reference, isAbbrev);
            boolean useFullDatePublished = false;
            result = addYear(result, reference, useFullDatePublished);
            TeamOrPersonBase<?> team = reference.getAuthorship();

            if (team != null){
                String teamTitle = CdmUtils.getPreferredNonEmptyString(team.getTitleCache(),
                        team.getNomenclaturalTitle(), isAbbrev, trim);
                if (teamTitle.length() > 0 ){
                    String concat = isNotBlank(result) ? afterAuthor : "";
                    result = teamTitle + concat + result;
                }
            }
        }else if(type == ReferenceType.Journal){
            result = titleCacheJournal(reference, isAbbrev);
        }else{
            result = titleCacheDefaultReference(reference, isAbbrev);
        }

        return result;
    }

    @Override
    public String getCitation(Reference reference) {
        // mostly copied from nomRefCacheStrat, refCache, journalCache

        if (reference == null){
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        TeamOrPersonBase<?> team = reference.getAuthorship();

        String nextConcat = "";

        if (team != null &&  isNotBlank(team.getTitleCache())){
            stringBuilder.append(team.getTitleCache() );
            //here is the difference between nomRef and others
            if (isNomRef(reference.getType())) {
                nextConcat = afterAuthor;
            }else{
                //FIXME check if this really makes sense
                stringBuilder.append(afterAuthor);
                nextConcat = beforeYear;
            }
        }

        String year = reference.getYear();
        if (StringUtils.isNotBlank(year)){
            stringBuilder.append(nextConcat + year);
        }

        return stringBuilder.toString();
    }

    @Override
    public String getNomenclaturalCache(Reference reference) {
        return this.getNomenclaturalCitation(reference, null);
    }

// ************************ TITLE CACHE SUBS ********************************************/

    /**
     * @param reference
     * @param type
     * @param inRef
     * @param hasInRef
     * @return
     */
    private String titleCacheRealInRef(Reference reference, boolean isAbbrev) {
        ReferenceType type = reference.getType();
        Reference inRef = reference.getInReference();
        boolean hasInRef = (inRef != null);

        String result;
        //copy from InRefDefaultCacheStrategyBase
        if (inRef != null){
            result = CdmUtils.getPreferredNonEmptyString(inRef.getTitleCache(),
                    inRef.getAbbrevTitleCache(), isAbbrev, trim)  ;
        }else{
            result = String.format("- undefined %s -", getUndefinedLabel(type));
        }

        //in
        result = inSeparator +  result;

        //section title
        String title = CdmUtils.getPreferredNonEmptyString(
                reference.getTitle(), reference.getAbbrevTitle(), isAbbrev, trim);
        if (title.length() > 0){
            result = title + blank + result;
        }

        //section author
        TeamOrPersonBase<?> thisRefTeam = reference.getAuthorship();
        String thisRefAuthor = "";
        if (thisRefTeam != null){
            thisRefAuthor = CdmUtils.getPreferredNonEmptyString(thisRefTeam.getTitleCache(),
                    thisRefTeam.getNomenclaturalTitle(), isAbbrev, trim);
        }
        result = CdmUtils.concat(afterSectionAuthor, thisRefAuthor, result);

        //date
        if (reference.getDatePublished() != null && ! reference.getDatePublished().isEmpty()){
            String thisRefDate = reference.getDatePublished().toString();
            if (hasInRef && reference.getInBook().getDatePublished() != null){
                TimePeriod inRefDate = reference.getInReference().getDatePublished();
                String inRefDateString = inRefDate.getYear();
                if (isNotBlank(inRefDateString)){
                    int pos = StringUtils.lastIndexOf(result, inRefDateString);
                    if (pos > -1 ){
                        result = result.substring(0, pos) + thisRefDate + result.substring(pos + inRefDateString.length());
                    }else{
                        logger.warn("InRefDateString (" + inRefDateString + ") could not be found in result (" + result +")");
                    }
                }else{
                    //avoid duplicate dots ('..')
                    String bYearSeparator = result.substring(result.length() -1).equals(beforeYear.substring(0, 1)) ? beforeYear.substring(1) : beforeYear;
                    result = result + bYearSeparator + thisRefDate + afterYear;
                }
            }else{
                result = result + beforeYear + thisRefDate + afterYear;
            }
        }
        return result;
    }


    /**
     * @param reference
     * @return
     */
    private String titleCacheJournal(Reference reference, boolean isAbbrev) {
        String result;
        //copied from Journal

        //title
        result = CdmUtils.getPreferredNonEmptyString(reference.getTitle(),
                reference.getAbbrevTitle(), isAbbrev, trim);

//          //delete .
//          while (result.endsWith(".")){
//              result = result.substring(0, result.length()-1);
//          }
//          result = addYear(result, journal);

        TeamOrPersonBase<?> team = reference.getAuthorship();
        if (team != null){
            String author = CdmUtils.getPreferredNonEmptyString(team.getTitleCache(),
                    team.getNomenclaturalTitle(), isAbbrev, trim);
            if (StringUtils.isNotBlank(author)){
                result = author + afterAuthor + result;
            }
        }
        return result;
    }

    /**
     * @param reference
     * @param isNotAbbrev
     * @return
     */
    private String titleCacheDefaultReference(Reference reference, boolean isAbbrev) {
        String result;
        //copied from ReferenceDefaultCacheStrategy
        result = "";
        String titel = CdmUtils.getPreferredNonEmptyString(reference.getTitle(),
                reference.getAbbrevTitle(), isAbbrev, trim);
        if (isNotBlank(titel)){
            result = titel + blank;
        }
        //delete .
        while (result.endsWith(".")){
            result = result.substring(0, result.length()-1);
        }

        result = addYearReferenceDefault(result, reference);
        TeamOrPersonBase<?> team = reference.getAuthorship();
        if (team != null){
            String author = CdmUtils.getPreferredNonEmptyString(team.getTitleCache(),
                    team.getNomenclaturalTitle(), isAbbrev, trim);
            if (isNotBlank(author)){
                result = author + afterAuthor + result;
            }
        }
        return result;
    }

// ******************************* HELPER *****************************************/
    @Override
    public String getBeforeMicroReference(){
        return beforeMicroReference;
    }

    private String addYear(String string, Reference nomRef, boolean useFullDatePublished){
        String result;
        if (string == null){
            return null;
        }
        String year = useFullDatePublished ? nomRef.getDatePublishedString() : nomRef.getYear();
        if (isBlank(year)){
            result = string + afterYear;
        }else{
            String concat = isBlank(string)  ? "" : string.endsWith(".")  ? " " : beforeYear;
            result = string + concat + year + afterYear;
        }
        return result;
    }

    private String getTitleWithoutYearAndAuthor(Reference ref, boolean isAbbrev){
        return TitleWithoutYearAndAuthorHelper.getTitleWithoutYearAndAuthor(ref, isAbbrev);
    }
    private String getTitleWithoutYearAndAuthorGeneric(Reference ref, boolean isAbbrev){
        return TitleWithoutYearAndAuthorHelper.getTitleWithoutYearAndAuthorGeneric(ref, isAbbrev);
    }

    /**
     * @param type
     * @return
     */
    private Object getUndefinedLabel(ReferenceType type) {
        if (type == ReferenceType.BookSection){
            return "book";
        }else if (type == ReferenceType.Generic){
            return "generic reference";
        }else if (type == ReferenceType.Section){
            return "in reference";
        } else {
            return type.getMessage();
        }
    }


    /**
     * Returns <code>true</code> if the type of the reference originally corresponded to a cache strategy
     * which inherited from {@link InRefDefaultCacheStrategyBase} and in case of type {@link ReferenceType#Generic}
     * if it really has an inreference (reference.getInreference() != null).
     * @param reference
     */
    private boolean isRealInRef(Reference reference) {
        ReferenceType type = (reference.getType());
        if (type == null){
            return false;
        }else if (type == ReferenceType.BookSection || type == ReferenceType.Section){
            return true;
        }else if (type == ReferenceType.Generic){
            return reference.getInReference() != null;
        }else{
            return false;
        }
    }

    /**
     * Returns <code>true</code> if the type of the reference originally corresponded to a cache strategy
     * which inherited from {@link NomRefDefaultCacheStrategyBase}.
     * @param type
     */
    protected static boolean isNomRef(ReferenceType type){
        switch (type){
            case Article:
            case Book:
            case BookSection:
            case CdDvd:
            case Generic:
            case Section:
            case Thesis:
            case WebPage:
                return true;

            case Journal:
            default:
                return false;
        }
    }

    /**
     * Returns year information as originally computed by {@link ReferenceDefaultCacheStrategy}
     * @param string
     * @param ref
     * @return
     */
    private String addYearReferenceDefault(String string, Reference ref){
        String result;
        if (string == null){
            return null;
        }
        String year = CdmUtils.Nz(ref.getYear());
        if ("".equals(year)){
            result = string + afterYear;
        }else{
            result = string + beforeYear + year + afterYear;
        }
        return result;
    }


// ********************* Nomenclatural title ***************************************/

    @Override
    public String getNomenclaturalCitation(Reference reference, String microReference) {
        if (reference.isProtectedAbbrevTitleCache()){
            String cache = reference.getAbbrevTitleCache();
            return handleDetailAndYearForPreliminary(reference, cache, microReference);

        }
        String result = getTokenizedNomenclaturalTitel(reference);
        //if no data is available and only titleCache is protected take the protected title
        //this is to avoid empty cache if someone forgets to set also the abbrevTitleCache
        //we need to think about handling protected not separate for abbrevTitleCache  and titleCache
        if (result.equals(INomenclaturalReference.MICRO_REFERENCE_TOKEN) && reference.isProtectedTitleCache() ){
            String cache = reference.getTitleCache();
            return handleDetailAndYearForPreliminary(reference, cache, microReference);
        }

        microReference = Nz(microReference);
        if (StringUtils.isNotBlank(microReference)){
            microReference = getBeforeMicroReference() + microReference;
            if (microReference.endsWith(".")  && result.contains(INomenclaturalReference.MICRO_REFERENCE_TOKEN + ".") ){
                microReference = microReference.substring(0, microReference.length() - 1);
            }
        }
        result = replaceMicroRefToken(microReference, result);
        if (result.startsWith(". ")){  //only year available, remove '. '
            result = result.substring(2);
        }
        return result;
    }

    /**
     * @param nomenclaturalReference
     * @param microRef
     * @return
     */
    private String handleDetailAndYearForPreliminary(Reference nomenclaturalReference, String cache, String microReference) {
        String microRef = isNotBlank(microReference) ? getBeforeMicroReference() + microReference : "";
        if (cache == null){
            logger.warn("Cache is null. This should never be the case.");
            cache = "";
        }
        String  result = cache + (cache.contains(microRef) ? "" : microRef);

        String date = nomenclaturalReference.getDatePublishedString();
        if (isNotBlank(date) && ! result.contains(date)){
            result = result + beforeYear + date;
        }
        return result;
    }

    /**
     * Returns the nomenclatural title with micro reference represented as token
     * which can later be replaced by the real data.
     *
     * @see INomenclaturalReference#MICRO_REFERENCE_TOKEN
     *
     * @param ref The reference
     * @return
     */
    private String getTokenizedNomenclaturalTitel(Reference ref) {
        if (isRealInRef(ref)){
            return getTokenizedNomenclaturalTitelInRef(ref);
        }else{
            String result = getTitleWithoutYearAndAuthor(ref, true);
            result += INomenclaturalReference.MICRO_REFERENCE_TOKEN;
            result = addYear(result, ref, true);
            return result;
        }
    }

    private String getTokenizedNomenclaturalTitelInRef(Reference thisRef) {
        if (thisRef == null){
            return null;
        }

        Reference inRef = CdmBase.deproxy(thisRef.getInReference(), Reference.class);
        if (inRef != null && inRef.getInReference() != null && thisRef.getType() == ReferenceType.Section){
            //this is a reference of type Section which has a in-in-Ref
            //TODO maybe we do not need to restrict to type=Section only
            return this.getTokenizedNomenclaturalTitelInInRef(thisRef);
        }

        String result;
        //use generics's publication date if it exists
        if (inRef == null ||  (thisRef.hasDatePublished() ) ){
            getTitleWithoutYearAndAuthorGeneric(inRef, true);
            result =  inRef == null ? "" : getTitleWithoutYearAndAuthorGeneric(inRef, true);
            //added //TODO unify with non-inRef references formatting

            if (isNotBlank(thisRef.getVolume())){
                result = result + " " + thisRef.getVolume();
            }
            //TODO series / edition

            //end added
            result += INomenclaturalReference.MICRO_REFERENCE_TOKEN;
            result = addYear(result, thisRef, true);
        }else{
            //else use inRefs's publication date
            result = inRef.getNomenclaturalCitation(INomenclaturalReference.MICRO_REFERENCE_TOKEN);
            if (result != null){
                result = result.replace(beforeMicroReference +  INomenclaturalReference.MICRO_REFERENCE_TOKEN, INomenclaturalReference.MICRO_REFERENCE_TOKEN);
            }
        }
        //FIXME: vol. etc., http://dev.e-taxonomy.eu/trac/ticket/2862

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
    private String getTokenizedNomenclaturalTitelInInRef(Reference ref) {
        String result;

        Reference inRef = CdmBase.deproxy(ref.getInReference(), Reference.class);
        Reference inInRef = CdmBase.deproxy(inRef.getInReference(), Reference.class);

        if (! isNomRef(inInRef.getType())){
            if (! isNomRef(inRef.getType())){
                logger.warn("Neither inReference nor inInReference is a "
                        + " nomenclatural reference. This is not correct or not handled yet."
                        + " Generic titleWithoutYearAndAuthor used instead");
                result = getTitleWithoutYearAndAuthorGeneric(inInRef, true);
                //FIXME: vol. etc., http://dev.e-taxonomy.eu/trac/ticket/2862  (comment taken from super.getTokenizedNomenclaturalTitel())
            }else{
                result = getTitleWithoutYearAndAuthor(inRef, true);
            }
        }else{
            result = getTitleWithoutYearAndAuthor(inInRef, true);
        }
        result += INomenclaturalReference.MICRO_REFERENCE_TOKEN;

        Reference dataReference = (ref.hasDatePublished() ? ref : inRef.hasDatePublished() ? inRef : inInRef);

        result = addYear(result, dataReference, true);

        result = getInRefAuthorPart(inInRef, afterInRefAuthor) + result;
        if (! result.startsWith("in ")){
            result = "in " +  result;
        }
        return result;
    }

    private String getInRefAuthorPart(Reference book, String seperator){
        if (book == null){
            return "";
        }
        TeamOrPersonBase<?> team = book.getAuthorship();
        String result = Nz( team == null ? "" : team.getNomenclaturalTitle());
        if (! result.trim().equals("")){
            result = result + seperator;
        }
        return result;
    }


    /**
     * @param microReference
     * @param result
     * @return
     */
    private String replaceMicroRefToken(String microReference, String string) {
        int index = string.indexOf(INomenclaturalReference.MICRO_REFERENCE_TOKEN);

        if (index > -1){
            String before = string.substring(0, index);
            String after = string.substring(index + INomenclaturalReference.MICRO_REFERENCE_TOKEN.length() );
            String localMicroReference = microReference.trim();   //needed ?
            if (after.length() > 0){
                if (  ("".equals(localMicroReference) && before.endsWith(after.substring(0,1)) || localMicroReference.endsWith(after.substring(0,1)))){
                    after = after.substring(1);
                }
            }
            String result = before + localMicroReference + after;
            return result;
        }else{
            return string;
        }
    }

// *************************** EXTERNAL USE *******************************************/
   /**
    *
    * @param referenceTitleCache
    * @param authorTitleCache
    * @return
    */
   public static String putAuthorToEndOfString(String referenceTitleCache, String authorTitleCache) {
       if(authorTitleCache != null){
           referenceTitleCache = referenceTitleCache.replace(authorTitleCache + ", ", "");
           referenceTitleCache += " - " + authorTitleCache;
       }
       return referenceTitleCache;
   }
}
