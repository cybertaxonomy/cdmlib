/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.reference;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides the set of applicable  {@link Reference} fields per {@link ReferenceType}
 * together with the type specific name of the getter.
 * <p>
 * All this information can in principle be generically retrieved from the reference interfaces.
 * Creating and applying annotations
 * to refer to the actual field names could help in this case.
 * <p>
 *
 * @author a.kohlbecker
 * @since Jun 15, 2018
 *
 */
public class ReferencePropertyDefinitions {

    private static Map<String, String> iReference = new HashMap<>();
    private static Map<String, String> iPublicationBase = new HashMap<>();
    private static Map<String, String> iWithAuthorAndDate = new HashMap<>();
    private static Map<String, String> iWithDoi = new HashMap<>();
    private static Map<String, String> iAuthoredPublicationBase = new HashMap<>();
    private static Map<String, String> iNomenclaturalReference = new HashMap<>();
    private static Map<String, String> iVolumeReference = new HashMap<>();
    private static Map<String, String> iSection = new HashMap<>();
    private static Map<String, String> iPrintedUnitBase = new HashMap<>();
    private static Map<String, String> iArticle = new HashMap<>();
    private static Map<String, String> iBook = new HashMap<>();
    private static Map<String, String> iBookSection = new HashMap<>();
    private static Map<String, String> iInProceedings = new HashMap<>();
    private static Map<String, String> iProceedings = new HashMap<>();
    private static Map<String, String> iJournal = new HashMap<>();
    private static Map<String, String> iPrintSeries = new HashMap<>();
    private static Map<String, String> iThesis = new HashMap<>();
    private static Map<String, String> iReport = new HashMap<>();
    private static Map<String, String> iPersonalCommunication = new HashMap<>();
    private static Map<String, String> all = new HashMap<>();

    static {
        put(iReference, "uri");
        put(iReference, "title");
        put(iReference, "type");

        put(iWithAuthorAndDate, "authorship");
        put(iWithAuthorAndDate, "datePublished");

        put(iWithDoi, "doi");

        iPublicationBase = merge(iReference);
        put(iPublicationBase, "publisher");
        put(iPublicationBase, "placePublished");

        // put(iNomenclaturalReference, "year");
        // put(iNomenclaturalReference, "nomenclaturalCitation");

        iAuthoredPublicationBase = merge(iPublicationBase , iWithAuthorAndDate, iWithDoi);

        // ----------------------------------------------------------------------------
        // Field visibility different from ISection definition see discussion at
        //  - https://dev.e-taxonomy.eu/redmine/issues/9706#note-3
        // iSection = merge(iReference, iWithAuthorAndDate, iWithDoi, iNomenclaturalReference); // commented to fix visibility
        put(iSection, "authorship");
        put(iSection, "pages");
        put(iSection, "inReference");

        iVolumeReference = merge(iReference, iWithAuthorAndDate, iWithDoi);
        put(iVolumeReference, "volume");

        iPrintedUnitBase = merge(iPublicationBase, iSection, iVolumeReference);
        put(iPrintedUnitBase, "title");
        put(iPrintedUnitBase, "abbrevTitle");
        put(iPrintedUnitBase, "inReference", "inSeries");
        put(iPrintedUnitBase, "editor");

        iArticle = merge(iSection, iVolumeReference);
        put(iArticle, "inReference", "inJournal");

        iBook = merge(iPrintedUnitBase);
        put(iBook, "inReference", "inSeries");
        put(iBook, "edition");
        put(iBook, "isbn");

        iBookSection = merge(iSection);
        put(iBookSection, "inReference", "inBook");

        iProceedings = merge(iPrintedUnitBase);
        put(iProceedings, "organization");
        put(iProceedings, "isbn");

        iJournal = merge(iPublicationBase);
        put(iJournal, "issn");

        iInProceedings = merge(iSection);
        remove(iInProceedings, "series");
        put(iInProceedings, "inReference", "In proceedings");

        iPrintSeries = merge(iPublicationBase);
        put(iPrintSeries, "publisher");
        put(iPrintSeries, "placePublished");

        iThesis = merge(iPublicationBase);
        put(iThesis, "school");

        iReport = merge(iPublicationBase);
        put(iReport, "institution");

        iPersonalCommunication = merge(iReference, iWithAuthorAndDate);

        all = merge(iThesis, iPrintSeries, iInProceedings, iJournal, iArticle, iBook, iBookSection, iProceedings, iPrintedUnitBase, iVolumeReference, iReport);
    }

    /**
     *
     * @param type
     * @return a map (Reference.fieldName -> propertyName) with the Reference class field name as key and the property name as
     * defined in the most significant interface as value. The propertyName can be used as label in the UI
     *
     * @throws UnimplemetedCaseException
     */
    public static Map<String, String> fieldPropertyDefinition(ReferenceType type) throws UnimplemetedCaseException{

        if(type == null){
            return all;
        }
        switch (type){
        case Article:
            return iArticle;
        case Book:
            return iBook;
        case BookSection:
            return iBookSection;
        case CdDvd:
            return all;
        case Database:
            return all;
        case Generic:
            return all;
        case InProceedings:
            return iInProceedings;
        case Journal:
            return iJournal;
        case Map:
            return all;
        case Patent:
            return all;
        case PersonalCommunication:
            return iPersonalCommunication;
        case PrintSeries:
            return iPublicationBase;
        case Proceedings:
            return iProceedings;
        case Report:
            return all;
        case Section:
            return iSection;
        case Thesis:
            return iThesis;
        case WebPage:
            return all;
        default:
            return all;
        }

    }

    @SafeVarargs
    private static Map<String, String> merge(Map<String, String> ... maps) {

        Map<String, String> fieldPropertyMap = new HashMap<>();

        for(Map<String, String> m : maps){
            fieldPropertyMap.putAll(m);
        }

        return fieldPropertyMap;
    }


    private static void put(Map<String, String> fieldPropertyMap, String fieldName, String propertyName) {
        fieldPropertyMap.put(fieldName, propertyName);
    }

    private static void remove(Map<String, String> fieldPropertyMap, String fieldName) {
        fieldPropertyMap.remove(fieldName);
    }


   private static void put(Map<String, String> fieldPropertyMap, String fieldName) {
       put(fieldPropertyMap, fieldName, fieldName);
   }

   public static class UnimplemetedCaseException extends Exception{

        private static final long serialVersionUID = 1L;

        public UnimplemetedCaseException(ReferenceType type){
            super("No implementation for ReferenceType " + type.name());
        }
   }


}
