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

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.format.reference.NomenclaturalSourceFormatter;
import eu.etaxonomy.cdm.format.reference.OriginalSourceFormatter;
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
 * Generally the cache strategy allows to compute 2 formats:<BR><BR>
 *
 *  1.) for bibliographic references (stored in {@link Reference#getTitleCache() titleCache}).<BR>
 *
 *  2.) for nomenclatural references (stored in {@link Reference#getAbbrevTitleCache() abbrevTitleCache}),
 *      but without micro reference (detail).<BR>
 * <BR>
 * The formatting of nomenclatural references with micro references has been moved to {@link NomenclaturalSourceFormatter}
 * and the formatting of short citations has been moved to {@link OriginalSourceFormatter}.
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
    public static final String UNDEFINED_JOURNAL = "undefined journal " + UTF8.EN_DASH;
    private static final String afterAuthor = ": ";

    //book

    //(book?) section
    private static final String afterSectionAuthor = ": ";
    private static final String afterInRefAuthor = ", ";  //TODO needs discussion


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
        String authorAndYear = getAuthorAndYear(reference, isNotAbbrev, false);

        if (isRealInRef(reference)){
            //Section, Book-Section or Generic with inRef
            result = titleCacheRealInRef(reference, isNotAbbrev);
        }else if(isNomRef(type)){
            //all Non-InRef NomRefs
            //Article, CdDvd, Generic, Section, Thesis, WebPage (+Book, BookSection but not here)
            String title = getTitleWithoutYearAndAuthor(reference, isNotAbbrev, false);
            result = addPages(title, reference);

            if (type == ReferenceType.Article){
                String artTitle = CdmUtils.addTrailingDotIfNotExists(reference.getTitle());
                result = CdmUtils.concat(" ", artTitle, result);
                if (isNotBlank(authorAndYear)){
//                    String authorSeparator = isNotBlank(artTitle)? afterAuthor : " ";
                    String authorSeparator = afterAuthor;
                    authorAndYear += authorSeparator;
                }
                result = authorAndYear + result;
            }else{  //if Book, CdDvd, flat Generic, Thesis, WebPage
                if (isNotBlank(authorAndYear)){
                    String authorSeparator = isNotBlank(title)? afterAuthor : "";
                    authorAndYear += authorSeparator;
                }
                result = authorAndYear + result;
            }
            if (!type.isWebPage()){
                result = CdmUtils.addTrailingDotIfNotExists(result);
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
        return result == null ? null : result.trim();
    }

    private String getAuthorAndYear(Reference reference, boolean isAbbrev, boolean useFullDatePublished) {
        TeamOrPersonBase<?> author = reference.getAuthorship();
        String authorStr = (author == null)? "" : CdmUtils.getPreferredNonEmptyString(author.getTitleCache(),
                author.getNomenclaturalTitle(), isAbbrev, trim);
        String result = addAuthorYear(authorStr, reference, useFullDatePublished);
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
            result = getTitleWithoutYearAndAuthor(reference, isAbbrev, false);
            boolean useFullDatePublished = false;
            String articleTitle = CdmUtils.getPreferredNonEmptyString(reference.getTitle(),
                    reference.getAbbrevTitle(), isAbbrev, trim);
            result = CdmUtils.concat(" ", articleTitle, result);  //Article should maybe left out for nomenclatural references (?)
            String authorAndYear = getAuthorAndYear(reference, isAbbrev, useFullDatePublished);
            if (isNotBlank(authorAndYear)){
//                String authorSeparator = isNotBlank(reference.getTitle())? afterAuthor : " ";
                String authorSeparator = afterAuthor;
                authorAndYear += authorSeparator;
            }
            result = authorAndYear + result;
            result = CdmUtils.addTrailingDotIfNotExists(result);
        }else if (isRealInRef(reference)){
            result = titleCacheRealInRef(reference, isAbbrev);
        }else if (isNomRef(type)){
            String authorAndYear = getAuthorAndYear(reference, isAbbrev, false);
            String title = getTitleWithoutYearAndAuthor(reference, isAbbrev, false);
            result = addPages(title, reference);
            //if Book, CdDvd, flat Generic, Thesis, WebPage
            if (isNotBlank(authorAndYear)){
                String authorSeparator = isNotBlank(title)? afterAuthor : "";
                authorAndYear += authorSeparator;
            }
            result = authorAndYear + result;

            if (!type.isWebPage()){
                result = CdmUtils.addTrailingDotIfNotExists(result);
            }
        }else if(type == ReferenceType.Journal){
            result = titleCacheJournal(reference, isAbbrev);
        }else{
            result = titleCacheDefaultReference(reference, isAbbrev);
        }

        return result;
    }

// ************************ TITLE CACHE SUBS ********************************************/

    //section, book section or generic with inRef
    private String titleCacheRealInRef(Reference reference, boolean isAbbrev) {
        ReferenceType type = reference.getType();
        Reference inRef = reference.getInReference();
        boolean hasInRef = (inRef != null);

        String inRefAuthorAndTitle;
        //copy from InRefDefaultCacheStrategyBase
        if (inRef != null){
            String inRefTitle = TitleWithoutYearAndAuthorHelper.getTitleWithoutYearAndAuthor(inRef, isAbbrev, false);
            TeamOrPersonBase<?> inRefAuthor = inRef.getAuthorship();
            String authorStr = (inRefAuthor == null)? "" : CdmUtils.getPreferredNonEmptyString(inRefAuthor.getTitleCache(),
                    inRefAuthor.getNomenclaturalTitle(), isAbbrev, trim);
            inRefAuthorAndTitle = CdmUtils.concat(afterInRefAuthor, authorStr, inRefTitle);
        }else{
            inRefAuthorAndTitle = String.format("- undefined %s -", getUndefinedLabel(type));
        }
        inRefAuthorAndTitle = CdmUtils.addTrailingDotIfNotExists(inRefAuthorAndTitle);

        //in
        String result = biblioInSeparator + inRefAuthorAndTitle;

        //section title
        String title = CdmUtils.getPreferredNonEmptyString(
                reference.getTitle(), reference.getAbbrevTitle(), isAbbrev, trim);
        if (title.matches(".*[.!\\?]")){
            title = title.substring(0, title.length() - 1);
        }
        //pages
        String pages = getPages(reference);
        if (isNotBlank(pages)){
            title = CdmUtils.concat(", ", title, pages);
        }
        if (title.length() > 0){
            result = title.trim() + "." + blank + result;
        }

        //section author
        TeamOrPersonBase<?> author = reference.getAuthorship();
        String authorStr = (author == null)? "" : CdmUtils.getPreferredNonEmptyString(author.getTitleCache(),
                author.getNomenclaturalTitle(), isAbbrev, trim);

        //date
        String dateStr = null;
        VerbatimTimePeriod date = (reference.getDatePublished() != null && ! reference.getDatePublished().isEmpty())? reference.getDatePublished() : null;
        if (date == null && hasInRef && reference.getInReference().getDatePublished() != null && !reference.getInReference().getDatePublished().isEmpty()){
            date = reference.getInReference().getDatePublished();
        }
        if (date != null){
            dateStr = date.getYear();
        }

        String authorAndYear = CdmUtils.concat(" ", authorStr, dateStr);

        String sep = result.startsWith(biblioInSeparator)? " ": afterSectionAuthor;
        result = CdmUtils.concat(sep, authorAndYear, result);

        return result;
    }

    private static final String pageNoRe = "[0-9iIvVxXlLcCdDmM]+";
    private String getPages(Reference reference) {

        if (isBlank(reference.getPages())){
            return null;
        }else if (reference.getPages().matches(pageNoRe + "\\s*[-"+UTF8.EN_DASH+"]\\s*"+ pageNoRe)){
            return "pp. " + reference.getPages();
        }else if (reference.getPages().matches(pageNoRe)){
            return "p. " + reference.getPages();
        }else{
            return reference.getPages();
        }
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

    private String addAuthorYear(String authorStr, Reference reference, boolean useFullDatePublished){
        String year = useFullDatePublished ? reference.getDatePublishedString() : reference.getYear();
        if (isBlank(year)){
            return authorStr;
        }else{
            return CdmUtils.concat(" ", authorStr, year);
        }
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
