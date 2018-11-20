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
 * All this information can in principle be generically retrieved from the reference interfaces. Creating and applying annotations
 * to refer to the actual field names could help in this case.
 * <p>
 *
 * @author a.kohlbecker
 * @since Jun 15, 2018
 *
 */
public class ReferencePropertyDefinitions {

    private static Map<String, String> iPublicationBase = new HashMap<>();
    private static Map<String, String> iReference = new HashMap<>();
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
    private static Map<String, String> all = new HashMap<>();

    static {

        Map<String, String> map;

        map = iReference;
        put(map, "uri");
        put(map, "datePublished");
        // put(map, "abbrevTitle"); // this is uses as nomenclatural title, so it makes no sense having this field in general, moved to iPrintedUnitBase
        put(map, "title");
        put(map, "authorship");
        put(map, "type");

        iPublicationBase = merge(iReference);
        map = iPublicationBase;
        put(map, "publisher");
        put(map, "placePublished");
        put(map, "doi");

        iSection = merge(iReference);
        map = iSection;
        put(map, "pages");
        put(map, "inReference");

        iVolumeReference = merge(iReference);
        map = iVolumeReference;
        put(map, "volume");

        iPrintedUnitBase = merge(iPublicationBase, iSection, iVolumeReference);
        map = iPrintedUnitBase;
        put(map, "title");
        put(map, "abbrevTitle");
        put(map, "inReference", "inSeries");
        put(map, "editor");
//        put(map, "seriesPart");
        put(map, "doi");

        iArticle = merge(iSection, iVolumeReference);
        map = iArticle;
//        put(map, "seriesPart");
        put(map, "inReference", "inJournal");
        put(map, "doi");

        iBook = merge(iPrintedUnitBase);
        map = iBook;
        put(map, "inReference", "inSeries");
        put(map, "edition");
        put(map, "isbn");

        iBookSection = merge(iSection);
        map = iBookSection;
        put(map, "inReference", "inBook");
        put(map, "doi");

        iProceedings = merge(iPrintedUnitBase);
        map = iProceedings;
        put(map, "organization");
        put(map, "doi");
        put(map, "isbn");

        iJournal = merge(iPublicationBase);
        map = iJournal;
        remove(map, "authorship");
        put(map, "issn");

        iInProceedings = merge(iSection);
        map = iInProceedings;
        remove(map, "series");
        put(map, "inReference", "inJournal");
        put(map, "doi");

        iPrintSeries = merge(iPublicationBase);
        map = iPrintSeries;
        remove(map, "authorship");
        remove(map, "doi");
        put(map, "publisher");
        put(map, "placePublished");

        iThesis = merge(iPublicationBase);
        map = iThesis;
        put(map, "school");

        iReport = merge(iPublicationBase);
        map = iReport;
        put(map, "institution");

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
            return all;
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
