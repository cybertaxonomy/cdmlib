// $Id$
/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.occurrence.gbif;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;

/**
 * Utility class which provides the functionality to convert a JSON response
 * resulting from a GBIF query for occurrences to the corresponding CDM entities.
 * @author pplitzner
 * @date 22.05.2014
 *
 */
public class JsonGbifOccurrenceParser {

    /**
     * Parses the given {@link String} for occurrences.<br>
     * Note: The data structure of the GBIF response should not be changed.
     * @param jsonString JSON data as a String
     * @return the found occurrences as a collection of {@link DerivedUnitFacade}
     */
    public static Collection<DerivedUnitFacade> parseJsonRecords(String jsonString) {
        return parseJsonRecords(JSONObject.fromObject(jsonString));
    }

    /**
     * Parses the given {@link InputStream} for occurrences.
     * @param jsonString JSON data as an InputStream
     * @return the found occurrences as a collection of {@link DerivedUnitFacade}
     */
    public static Collection<DerivedUnitFacade> parseJsonRecords(InputStream inputStream) throws IOException{
        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(inputStream, stringWriter);
        return parseJsonRecords(stringWriter.toString());
    }

    /**
     * Parses the given {@link JSONObject} for occurrences.<br>
     * Note: The data structure of the GBIF response should not be changed.
     * @param jsonString JSON data as an JSONObject
     * @return the found occurrences as a collection of {@link DerivedUnitFacade}
     */
    public static Collection<DerivedUnitFacade> parseJsonRecords(JSONObject jsonObject){
        return parseJsonRecords(jsonObject.getJSONArray("results"));
    }

    /**
     * Parses the given {@link JSONArray} for occurrences.
     * @param jsonString JSON data as an {@link JSONArray}
     * @return the found occurrences as a collection of {@link DerivedUnitFacade}
     */
    private static Collection<DerivedUnitFacade> parseJsonRecords(JSONArray jsonArray) {
        Collection<DerivedUnitFacade> results = new ArrayList<DerivedUnitFacade>();
        for(Object o:jsonArray){
            //parse every record
            if(o instanceof JSONObject){
                DerivedUnitFacade derivedUnitFacade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
                JSONObject record = (JSONObject)o;

                if(record.has("locality")){
                    String locality = record.getString("locality");
                    derivedUnitFacade.setLocality(locality);
                }
                results.add(derivedUnitFacade);
            }
        }
        return results;
    }

}
