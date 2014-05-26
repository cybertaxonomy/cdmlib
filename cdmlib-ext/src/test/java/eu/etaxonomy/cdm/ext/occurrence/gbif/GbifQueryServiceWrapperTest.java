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

import java.util.Collection;

import junit.framework.TestCase;

import org.junit.Test;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;

/**
 * @author pplitzner
 * @date 20.05.2014
 *
 */
public class GbifQueryServiceWrapperTest extends TestCase{

    private static final String LOCALITY_STRING = "Saddington Reservoir, Saddington Reservoir";

    private final String dummyJson = "{" +
    		"\"results\":" +
    		    "[" +
        		    "{" +
            		"\"key\": 252408386," +
            		"\"datasetKey\": \"26a49731-9457-45b2-9105-1b96063deb26\"," +
            		"\"publishingOrgKey\": \"07f617d0-c688-11d8-bf62-b8a03c50a862\"," +
            		"\"publishingCountry\": \"GB\"," +
            		"\"protocol\": \"DWC_ARCHIVE\"," +
            		"\"lastCrawled\": \"2013-09-07T07:08:17.000+0000\"," +
                    "\"identifiers\": [ ],"+
                    "\"media\": [ ],"+
                    "\"facts\": [ ],"+
                    "\"relations\": [ ],"+
                    "\"basisOfRecord\": \"OBSERVATION\","+
                    "\"decimalLongitude\": -1.0193,"+
                    "\"decimalLatitude\": 52.51021,"+
                    "\"continent\": \"EUROPE\","+
                    "\"year\": 2006,"+
                    "\"month\": 6,"+
                    "\"day\": 30,"+
                    "\"eventDate\": \"2006-06-29T22:00:00.000+0000\","+
                    "\"issues\": [ ],"+
                    "\"lastInterpreted\": \"2014-04-01T23:31:15.462+0000\","+
                    "\"geodeticDatum\": \"WGS84\","+
                    "\"countryCode\": \"GB\","+
                    "\"country\": \"United Kingdom\","+
                    "\"gbifID\": \"252408386\","+
                    "\"institutionCode\": \"Leicestershire and Rutland Environmental Records Centre\","+
                    "\"catalogNumber\": \"70875196\","+
                    "\"recordedBy\": \"DA Lott\","+
                    "\"locality\": \""+LOCALITY_STRING+"\","+
                    "\"collectionCode\": \"7472\","+
                    "\"identifiedBy\": \"DA Lott\"" +
                    "}" +
                "]" +
            "}";

    @Test
    public void testJsonToCdmObject(){
        Collection<DerivedUnitFacade> records = JsonGbifOccurrenceParser.parseJsonRecords(dummyJson);
        assertEquals("number of records found is incorrect", 1, records.size());
        DerivedUnitFacade facade = records.iterator().next();
        assertEquals("Locality is incorrect", LOCALITY_STRING, facade.getLocalityText());
    }

}
