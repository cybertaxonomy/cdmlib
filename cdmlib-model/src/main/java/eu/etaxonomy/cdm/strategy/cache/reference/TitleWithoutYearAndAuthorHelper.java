/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.reference;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;

/**
 * @author a.mueller
 * @date 25.05.2016
 *
 */
public class TitleWithoutYearAndAuthorHelper {
    private static final Logger logger = Logger.getLogger(TitleWithoutYearAndAuthorHelper.class);

    //article
    private static final String prefixArticleReferenceJounal = "in";
    private static final String prefixSeriesArticle = "ser.";

    //book
    private static final String prefixBookEdition = "ed.";
    private static final String prefixBookSeries = ", ser.";

    //generic
    private static final String prefixEditionGeneric = "ed.";
    private static final String prefixSeriesGeneric = "ser.";

    //common
    private static final String blank = " ";
    private static final String comma = ",";


// *************************Main METHODS ***********************************/

    public static String getTitleWithoutYearAndAuthor(Reference ref, boolean isAbbrev){
        ReferenceType type = ref.getType();
        if (! DefaultReferenceCacheStrategy.isNomRef(type)){
            logger.warn("getTitleWithoutYearAndAuthor should not be required"
                    + " for reference type " + type.getMessage() +
                    " and does not exist. Use Generic getTitleWithoutYearAndAuthorGeneric instead");
            return getTitleWithoutYearAndAuthorGeneric(ref, isAbbrev);
        }else if (type == ReferenceType.Article){
            return getTitleWithoutYearAndAuthorArticle(ref, isAbbrev);
        }else if(type == ReferenceType.Book){
            return getTitleWithoutYearAndAuthorBook(ref, isAbbrev);
        }else if(type == ReferenceType.CdDvd){
            return getTitleWithoutYearAndAuthorCdDvd(ref, isAbbrev);
        }else if(type == ReferenceType.Generic){
            return getTitleWithoutYearAndAuthorGeneric(ref, isAbbrev);
        }else if (type == ReferenceType.WebPage || type == ReferenceType.Thesis) {
            return getTitleWithoutYearAndAuthorWebPageThesis(ref, isAbbrev);
        }else if (type == ReferenceType.Section || type == ReferenceType.BookSection){
            // not needed in Section
            logger.warn("Questionable procedure call. Procedure not implemented because not needed. ");
            return null;
        }else{
            //FIXME
            return null;
        }

    }



    private static String getTitleWithoutYearAndAuthorArticle(Reference article, boolean isAbbrev){
        if (article == null){
            return null;
        }
        IJournal journal = article.getInReference();

        String journalTitel;
        if (journal != null){
            journalTitel = CdmUtils.getPreferredNonEmptyString(journal.getTitle(), journal.getAbbrevTitle(), isAbbrev, true);
        }else{
            journalTitel = DefaultReferenceCacheStrategy.UNDEFINED_JOURNAL;
        }

        String series = Nz(article.getSeriesPart()).trim();
        String volume = Nz(article.getVolume()).trim();

        boolean needsComma = false;

        String nomRefCache = "";

        //inJournal
        nomRefCache = prefixArticleReferenceJounal + blank;

        //titelAbbrev
        if (isNotBlank(journalTitel)){
            nomRefCache = nomRefCache + journalTitel;
            needsComma = makeNeedsCommaArticle(needsComma, nomRefCache, volume, series);
            if (! needsComma){
                nomRefCache = nomRefCache + blank;
            }
        }

        //series and vol.
        nomRefCache = getSeriesAndVolPartArticle(series, volume, needsComma, nomRefCache);

        //delete "."
        while (nomRefCache.endsWith(".")){
            nomRefCache = nomRefCache.substring(0, nomRefCache.length()-1);
        }

        return nomRefCache.trim();
    }


    private static String getTitleWithoutYearAndAuthorBook(Reference ref, boolean isAbbrev){
        if (ref == null){
            return null;
        }
        //TODO
        String title = CdmUtils.getPreferredNonEmptyString(ref.getTitle(), ref.getAbbrevTitle(), isAbbrev, true);
        String edition = Nz(ref.getEdition()).trim();
        String series = ""; //TODO ref.getSeries();  //SeriesPart is handled later
        String refSeriesPart = Nz(ref.getSeriesPart());
        String volume = CdmUtils.Nz(ref.getVolume()).trim();
        String refYear = "";  //TODO nomenclaturalReference.getYear();


        String nomRefCache = "";
        Integer len;
        String lastChar = "";
        String character =".";
        len = title.length();
        if (len > 0){
            lastChar = title.substring(len-1, len);
        }
        //lastCharIsDouble = f_core_CompareStrings(RIGHT(@TitelAbbrev,1),character);
        boolean lastCharIsDouble = title.equals(character);

        if(lastCharIsDouble  && edition.length() == 0 && refSeriesPart.length() == 0 && volume.length() == 0 && refYear.length() > 0 ){
            title =  title.substring(1, len-1); //  SUBSTRING(@TitelAbbrev,1,@LEN-1)
        }


        boolean needsComma = false;
        //titelAbbrev
        if (!"".equals(title) ){
            String postfix = isNotBlank(edition + refSeriesPart) ? "" : blank;
            nomRefCache = title + postfix;
        }
        //edition
        String editionPart = "";
        if (StringUtils.isNotBlank(edition)){
            editionPart = edition;
            if (isNumeric(edition)){
                editionPart = prefixBookEdition + blank + editionPart;
            }
            needsComma = true;
        }
        nomRefCache = CdmUtils.concat(", ", nomRefCache, editionPart);

        //inSeries
        String seriesPart = "";
        if (StringUtils.isNotBlank(refSeriesPart)){
            seriesPart = refSeriesPart;
            if (isNumeric(refSeriesPart)){
                seriesPart = prefixBookSeries + blank + seriesPart;
            }
            if (needsComma){
                seriesPart = comma + seriesPart;
            }
            needsComma = true;
        }
        nomRefCache += seriesPart;


        //volume Part
        String volumePart = "";
        if (StringUtils.isNotBlank(volume)){
            volumePart = volume;
            if (needsComma){
                volumePart = comma + blank + volumePart;
            }else{
                volumePart = "" + volumePart;
            }
            //needsComma = false;
        }
        nomRefCache += volumePart;

        //delete .
        while (nomRefCache.endsWith(".")){
            nomRefCache = nomRefCache.substring(0, nomRefCache.length()-1);
        }

        return nomRefCache.trim();
    }


    public static  String getTitleWithoutYearAndAuthorGeneric(Reference genericReference, boolean isAbbrev){
        if (genericReference == null){
            return null;
        }
        //TODO
        String titel = CdmUtils.getPreferredNonEmptyString(genericReference.getTitle(), genericReference.getAbbrevTitle(), isAbbrev, true);
        String edition = CdmUtils.Nz(genericReference.getEdition());
        //TODO
        String series = CdmUtils.Nz(genericReference.getSeriesPart()).trim(); //nomenclaturalReference.getSeries();
        String volume = CdmUtils.Nz(genericReference.getVolume()).trim();

        String result = "";
        boolean lastCharIsDouble;
        Integer len;
        String lastChar ="";
        String character =".";
        len = titel.length();
        if (len > 0){lastChar = titel.substring(len-1, len);}
        //lastCharIsDouble = f_core_CompareStrings(RIGHT(@TitelAbbrev,1),character);
        lastCharIsDouble = titel.equals(character);

//      if(lastCharIsDouble  && edition.length() == 0 && series.length() == 0 && volume.length() == 0 && refYear.length() > 0 ){
//          titelAbbrev =  titelAbbrev.substring(1, len-1); //  SUBSTRING(@TitelAbbrev,1,@LEN-1)
//      }


        boolean needsComma = false;
        //titelAbbrev
        if (titel.length() > 0 ){
            String postfix = StringUtils.isNotBlank(edition) ? "" : blank;
            result = titel + postfix;
        }
        //edition
        String editionPart = "";
        if (StringUtils.isNotBlank(edition)){
            editionPart = edition;
            if (isNumeric(edition)){
                editionPart = prefixEditionGeneric + blank + editionPart;
            }
            needsComma = true;
        }
        result = CdmUtils.concat(", ", result, editionPart);

        //inSeries
        String seriesPart = "";
        if (isNotBlank(series)){
            seriesPart = series;
            if (isNumeric(series)){
                seriesPart = prefixSeriesGeneric + blank + seriesPart;
            }
            if (needsComma){
                seriesPart = comma + seriesPart;
            }
            needsComma = true;
        }
        result += seriesPart;


        //volume Part
        String volumePart = "";
        if (!"".equals(volume)){
            volumePart = volume;
            if (needsComma){
                volumePart = comma + blank + volumePart;
            }
            //needsComma = false;
        }
        result += volumePart;

        //delete .
        while (result.endsWith(".")){
            result = result.substring(0, result.length()-1);
        }

        return result.trim();
    }

    private static String getTitleWithoutYearAndAuthorCdDvd(Reference ref, boolean isAbbrev){
        if (ref == null){
            return null;
        }
        String nomRefCache = "";
        //TODO
        String titel = CdmUtils.getPreferredNonEmptyString(ref.getTitle(), ref.getAbbrevTitle(), isAbbrev, true);
//      String publisher = CdmUtils.Nz(nomenclaturalReference.getPublisher());

        boolean needsComma = false;
        //titelAbbrev
        if (titel.length() > 0){
            nomRefCache = titel + blank;
        }
//      //publisher
//      String publisherPart = "";
//      if (!"".equals(publisher)){
//          publisherPart = publisher;
//          needsComma = true;
//      }
//      nomRefCache += publisherPart;


        //delete .
        while (nomRefCache.endsWith(".")){
            nomRefCache = nomRefCache.substring(0, nomRefCache.length()-1);
        }
        return nomRefCache.trim();
    }

    /**
     * @param ref
     * @param isAbbrev
     * @return
     */
    private static String getTitleWithoutYearAndAuthorWebPageThesis(Reference ref, boolean isAbbrev) {
        //FIXME this is only a very fast copy and paste from "Generic". Must still be cleaned !

        if (ref == null){
            return null;
        }

        //titelAbbrev
        //TODO
        String titelAbbrev = CdmUtils.getPreferredNonEmptyString(ref.getTitle(), ref.getAbbrevTitle(), isAbbrev, true);

        //titelAbbrev
        String nomRefCache = titelAbbrev + blank;

        //delete .
        while (nomRefCache.endsWith(".")){
            nomRefCache = nomRefCache.substring(0, nomRefCache.length()-1);
        }

        return nomRefCache.trim();
    }

//**************************** HELPER ********************************/

    private static boolean makeNeedsCommaArticle(boolean needsComma, String nomRefCache, String volume, String series) {
        if (needsComma){
            return true;
        }else{
            nomRefCache = nomRefCache.toLowerCase();
            int serIndex = nomRefCache.indexOf(" ser. ");
            int sectIndex = nomRefCache.indexOf(" sect. ");
            int abtIndex = nomRefCache.indexOf(" abt. ");
            int index = Math.max(Math.max(serIndex, sectIndex), abtIndex);
            int commaIndex = nomRefCache.indexOf(",", index);
            if (index > -1 && commaIndex == -1 && isNotBlank(volume)){
                return true;
            }else if (isNotBlank(series)){
                return true;
            }else{
                return false;
            }
        }
    }

    private static String getSeriesAndVolPartArticle(String series, String volume,
            boolean needsComma, String nomRefCache) {
        //inSeries
        String seriesPart = "";
        if (isNotBlank(series)){
            seriesPart = series;
            if (CdmUtils.isNumeric(series)){
                seriesPart = prefixSeriesArticle + blank + seriesPart;
            }
//          if (needsComma){
                seriesPart = comma + blank + seriesPart;
//          }
            needsComma = true;
        }
        nomRefCache += seriesPart;


        //volume Part
        String volumePart = "";
        if (!"".equals(volume)){
            volumePart = volume;
            if (needsComma){
                volumePart = comma + blank + volumePart;
            }
            //needsComma = false;
        }
        nomRefCache += volumePart;
        return nomRefCache;
    }

// ****************** COMMON **********************************************/

    /**
     * Null safe string. Returns the given string if it is not <code>null</code>.
     * Empty string otherwise.
     * @see CdmUtils#Nz(String)
     * @return the null-safe string
     */
    private static String Nz(String str){
        return CdmUtils.Nz(str);
    }

    /**
     * Checks if a string is not blank.
     * @see StringUtils#isNotBlank(String)
     */
    private static boolean isNotBlank(String str){
        return StringUtils.isNotBlank(str);
    }

    /**
     * Checks if a string is blank.
     * @see StringUtils#isNotBlank(String)
     */
    private static boolean isBlank(String str){
        return StringUtils.isBlank(str);
    }

    private static boolean isNumeric(String string){
        if (string == null){
            return false;
        }
        try {
            Double.valueOf(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
