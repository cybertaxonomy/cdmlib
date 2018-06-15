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
 * This class contains provides the set of applicable  {@link Reference} fields per {@link ReferenceType}
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

    static {

        Map<String, String> map;

        map = iReference;
        put(map, "uri");
        put(map, "datePublished");
        put(map, "abbrevTitle");
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
        put(map, "inReference", "inSeries");
        put(map, "editor");
        put(map, "seriesPart");

        iArticle = merge(iSection, iVolumeReference);
        map = iArticle;
        put(map, "seriesPart");
        put(map, "inReference", "inJournal");

        iBook = merge(iPrintedUnitBase);
        map = iBook;
        put(map, "edition");
        put(map, "isbn");

        iBookSection = merge(iSection);
        map = iBookSection;
        put(map, "inReference", "inBook");

        iProceedings = merge(iPrintedUnitBase);
        map = iProceedings;
        put(map, "organization");

        iJournal = merge(iPublicationBase);
        map = iJournal;
        put(map, "issn");

        iInProceedings = merge(iSection);
        map = iInProceedings;
        put(map, "seriesPart");
        put(map, "inReference", "inJournal");
        put(map, "doi");

        iPrintSeries = merge(iPublicationBase);
        map = iPrintSeries;
        put(map, "publisher");
        put(map, "placePublished");
        put(map, "doi");

        iThesis = merge(iPublicationBase);
        map = iThesis;
        put(map, "school");


    }

    /**
     *
     * @param type
     * @return a map (Reference.fieldName -> propertyName) with the Reference class field name as key and the property name as
     * defined in the most significant interface as value.
     *
     * @throws UnimplemetedCaseException
     */
    public static Map<String, String> fieldPropertyDefinition(ReferenceType type) throws UnimplemetedCaseException{


        switch (type){
        case Article:
            return iArticle;
        case Book:
            return iBook;
        case BookSection:
            return iBookSection;
        case CdDvd:
            throw new UnimplemetedCaseException(type);
        case Database:
            throw new UnimplemetedCaseException(type);
        case Generic:
            throw new UnimplemetedCaseException(type);
        case InProceedings:
            return iInProceedings;
        case Journal:
            return iJournal;
        case Map:
            throw new UnimplemetedCaseException(type);
        case Patent:
            throw new UnimplemetedCaseException(type);
        case PersonalCommunication:
            throw new UnimplemetedCaseException(type);
        case PrintSeries:
            return iPublicationBase;
        case Proceedings:
            return iProceedings;
        case Report:
            throw new UnimplemetedCaseException(type);
        case Section:
            return iSection;
        case Thesis:
            return iThesis;
        case WebPage:
            throw new UnimplemetedCaseException(type);
        default:
            throw new UnimplemetedCaseException(type);
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
