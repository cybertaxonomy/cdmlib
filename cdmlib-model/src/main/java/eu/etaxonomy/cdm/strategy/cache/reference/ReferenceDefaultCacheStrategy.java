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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
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
 * Generally the cache strategy allows to compute 3 formats:<BR>
 *
 *  1.) for bibliographic references (stored in {@link Reference#getTitleCache() titleCache}).<BR>
 *
 *  2.) for nomenclatural references (stored in {@link Reference#getAbbrevTitleCache() abbrevTitleCache}),
 *      but without micro reference (detail).<BR>
 *
 *  3.) for nomenclatural references with micro reference, but not stored anywhere as the micro reference
 *      is part of the name, not of the reference<BR>
 *
 *  4.) for short citation (e.g. Author 2009) as defined in {@link IReferenceCacheStrategy#getCitation(Reference, String)}
 *  and {@link IReferenceCacheStrategy#createShortCitation(Reference, String, Boolean)}
 *
 * @author a.mueller
 * @since 25.05.2016
 */
public class ReferenceDefaultCacheStrategy
        extends StrategyBase
        implements IReferenceCacheStrategy {

    private static final long serialVersionUID = 6773742298840407263L;
    private static final Logger logger = Logger.getLogger(ReferenceDefaultCacheStrategy.class);

    private final static UUID uuid = UUID.fromString("63e669ca-c6be-4a8a-b157-e391c22580f9");

    //article
    public static final String UNDEFINED_JOURNAL = "- undefined journal -";
    private static final String afterAuthor = ", ";

    //book

    //(book?) section
    private String afterSectionAuthor = " "+UTF8.EN_DASH+" ";

    //in reference
    private String biblioInSeparator = UTF8.EN_DASH + " In: "; //#9529
    private String biblioArticleInSeparator = UTF8.EN_DASH + " "; //#9529

    //common
    private static final String blank = " ";
    public static final String beforeYear = ". ";
    private static final String afterYear = "";

    private static final boolean trim = true;

// ************************ FACTORY ****************************/

    public static ReferenceDefaultCacheStrategy NewInstance(){
        return new ReferenceDefaultCacheStrategy();
    }

// ******************************* Main methods ******************************/

    @Override
    protected UUID getUuid() {
        return uuid;
    }

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
            result =  getTitleWithoutYearAndAuthor(reference, isNotAbbrev, false);
            result = addPages(result, reference);
            result = addYear(result, reference, false);
            TeamOrPersonBase<?> team = reference.getAuthorship();

            if (type == ReferenceType.Article){
                String artTitle = CdmUtils.addTrailingDotIfNotExists(reference.getTitle());
                result = CdmUtils.concat(" ", artTitle, result);
                if (team != null && isNotBlank(team.getTitleCache())){
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
        if (reference.getType() == ReferenceType.WebPage && reference.getUri() != null && !result.contains(reference.getUri().toString())){
            result = CdmUtils.concat(" "+UTF8.EN_DASH+" ", result, reference.getUri().toString());
        }
        if(reference.getAccessed() != null){
            //TODO still a bit preliminary, also brackets may change in future
            result = result + " [accessed " + getAccessedString(reference.getAccessed()) +"]";
        }
        return result;
    }

    private String getAccessedString(DateTime accessed) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
        String result = formatter.print(accessed);
        if (result.endsWith(" 00:00")){
            result = result.replace(" 00:00", "");
        }
        return result;
    }

    private String addPages(String result, Reference reference) {
        //pages
        if (isNotBlank(reference.getPages())){
            //Removing trailing added just in case, maybe not necessary
            result = removeTrailingDots(result).trim() + ": " + reference.getPages();
        }
        return result;
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
            result =  getTitleWithoutYearAndAuthor(reference, isAbbrev, false);
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
            result =  getTitleWithoutYearAndAuthor(reference, isAbbrev, false);
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

// ************************ TITLE CACHE SUBS ********************************************/

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
        result = biblioInSeparator +  result;

        //section title
        String title = CdmUtils.getPreferredNonEmptyString(
                reference.getTitle(), reference.getAbbrevTitle(), isAbbrev, trim);
        if (title.matches(".*[.!\\?]")){
            title = title.substring(0, title.length() - 1);
        }
        if (title.length() > 0){
            result = title.trim() + "." + blank + result;
        }

        //section author
        TeamOrPersonBase<?> thisRefTeam = reference.getAuthorship();
        String thisRefAuthor = "";
        if (thisRefTeam != null){
            thisRefAuthor = CdmUtils.getPreferredNonEmptyString(thisRefTeam.getTitleCache(),
                    thisRefTeam.getNomenclaturalTitle(), isAbbrev, trim);
        }
        String sep = result.startsWith(biblioInSeparator)? " ": afterSectionAuthor;
        result = CdmUtils.concat(sep, thisRefAuthor, result);

        //date
        if (reference.getDatePublished() != null && ! reference.getDatePublished().isEmpty()){
            String thisRefDate = reference.getDatePublished().toString();
            if (hasInRef && reference.getInBook().getDatePublished() != null){
                VerbatimTimePeriod inRefDate = reference.getInReference().getDatePublished();
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
            if (isNotBlank(author)){
                result = author + afterAuthor + result;
            }
        }
        return result;
    }

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

    /**
     * Adds the year or full date of a reference to a given string
     * @param currentStr the given string
     * @param reference the reference
     * @param useFullDatePublished wether to add the year only or the full date
     * @return the concatenated string
     */
    public static String addYear(String currentStr, Reference reference, boolean useFullDatePublished){
        String result;
        if (currentStr == null){
            return null;
        }
        String year = useFullDatePublished ? reference.getDatePublishedString() : reference.getYear();
        if (isBlank(year)){
            result = currentStr + afterYear;
        }else{
            String concat = isBlank(currentStr)  ? "" : currentStr.endsWith(".")  ? " " : beforeYear;
            result = currentStr + concat + year + afterYear;
        }
        return result;
    }

    private String getTitleWithoutYearAndAuthor(Reference ref, boolean isAbbrev, boolean isNomRef){
        return TitleWithoutYearAndAuthorHelper.getTitleWithoutYearAndAuthor(ref, isAbbrev, isNomRef);
    }

    private Object getUndefinedLabel(ReferenceType type) {
        if (type == ReferenceType.BookSection){
            return "book";
        }else if (type == ReferenceType.Generic){
            return "generic reference";
        }else if (type == ReferenceType.Section){
            return "in reference";
        } else {
            return type.getLabel();
        }
    }

    /**
     * Returns <code>true</code> if the type of the reference originally corresponded to a cache strategy
     * which inherited from {@link InRefDefaultCacheStrategyBase} and in case of type {@link ReferenceType#Generic}
     * if it really has an inreference (reference.getInreference() != null).
     * @param reference
     */
    public static boolean isRealInRef(Reference reference) {
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
     * @see ReferenceType#isNomRef()
     */
    public static boolean isNomRef(ReferenceType type){
        return type == null ? false : type.isNomRef();
    }

    /**
     * Returns year information as originally computed by {@link ReferenceDefaultCacheStrategy}.
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
            result = string.trim() + beforeYear + year + afterYear;
        }
        return result;
    }

// *************************** EXTERNAL USE *******************************************/

   public static String putAuthorToEndOfString(String referenceTitle, String authorTitle) {
       if(authorTitle != null){
           referenceTitle = referenceTitle.replace(authorTitle + ", ", "");
           referenceTitle += " - " + authorTitle;
       }
       return referenceTitle;
   }
}
