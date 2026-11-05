/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.occurrence.gbif;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.ext.occurrence.OccurenceQuery;

/**
 * @author pplitzner
 * @since 20.05.2014
 */
public class GbifQueryServiceWrapperTest {

    private static final String LOCALITY_STRING = "Saddington Reservoir, Saddington Reservoir";

    private final String dummyJsonRecords = "{" +
    		"\"results\":" +
    		    "[" +
        		    "{" +
            		"\"key\": 252408386," +
            		"\"datasetKey\": \"26a49731-9457-45b2-9105-1b96063deb26\"," +
            		"\"publishingOrgKey\": \"07f617d0-c688-11d8-bf62-b8a03c50a862\"," +
            		"\"publishingCountry\": \"GB\"," +
            		"\"protocol\": \"BIOCASE\"," +
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

    private final String dummyJsonDataset = "[" +
        "{"+
            "\"key\": 29596,"+
            "\"type\": \"BIOCASE\","+
            "\"url\": \"http://www.flora-mv.de/biocase/pywrapper.cgi?dsa=hoeherePflanzen\","+
            "\"createdBy\": \"crawler.gbif.org\","+
            "\"modifiedBy\": \"crawler.gbif.org\","+
            "\"created\": \"2014-01-13T10:32:05.170+0000\","+
            "\"modified\": \"2014-01-13T10:32:05.170+0000\","+
            "\"machineTags\": ["+
             "   {"+
             "       \"key\": 59486,"+
             "      \"namespace\": \"metasync.gbif.org\","+
             "       \"name\": \"conceptualSchema\","+
             "       \"value\": \"http://www.tdwg.org/schemas/abcd/2.06\","+
             "       \"createdBy\": \"crawler.gbif.org\","+
             "       \"created\": \"2014-01-13T10:32:05.172+0000\""+
             "   }"+
            "]"+
        "}"+
    "]";

    @Test
    public void testJsonToCdmObject() throws URISyntaxException{
        Collection<GbifResponse> records = GbifJsonOccurrenceParser.parseJsonRecords(dummyJsonRecords);
        assertEquals(1, records.size(), "number of records found is incorrect");
        GbifResponse gbifResponse = records.iterator().next();
        assertEquals(LOCALITY_STRING, gbifResponse.getDerivedUnitFacade().getLocalityText(), "Locality is incorrect");
        assertEquals(GbifDataSetProtocol.BIOCASE, gbifResponse.getDataSetProtocol(), "protocol is wrong");
        assertEquals(new URI("https://api.gbif.org/v1/dataset/26a49731-9457-45b2-9105-1b96063deb26/endpoint"), gbifResponse.getDataSetUri(), "protocol is wrong");
    }

    @Test
    public void testJsonOriginalDataSetUriParsing(){
        DataSetResponse response = GbifJsonOccurrenceParser.parseOriginalDataSetUri(dummyJsonDataset);
        assertEquals(GbifDataSetProtocol.BIOCASE, response.getProtocol(), "Response protocol is incorrect!");
        assertEquals(URI.create("http://www.flora-mv.de/biocase/pywrapper.cgi?dsa=hoeherePflanzen"), response.getEndpoint(), "Response endpoint is incorrect!");
    }

    @Test
    public void testQueryParameterConversion(){
        OccurenceQuery query = new OccurenceQuery("Campanula persicifolia", "T. Henning", "1234", "ACC-2", "BGBM", "DE", "pollen herbarium", new GregorianCalendar(2014, 05, 27), new GregorianCalendar(2014,05,28), false);
        List<NameValuePair> queryParams = new GbifQueryGenerator().generateQueryParams(query);
        NameValuePair pair = new BasicNameValuePair("scientificName", "Campanula persicifolia");
        assertTrue(queryParams.contains(pair), "query parameter is missing");
        //FIXME this will currently always fail because eventDate is still not supported by GBIF
        //"basisOfRecord" and "limit" is always set
        // + 8 from query (collectorsNumber will be represented in the two parameters "recordNumber" and "fieldNumber";
        // both dates are represented in one parameter "eventDate";
        // locality can still not be queried on GBIF web service)
//        assertEquals(10, queryParams.size(), "Number of generated URI parameters is incorrect");
    }

    @Test
    @Disabled //temporary ignored because service is unavailable
    public void testGbifWebService() {
        OccurenceQuery query = new OccurenceQuery("Campanula persicifolia", "E. J. Palmer", null, null, null, null, null, null, null, false);
        GbifQueryServiceWrapper service = new GbifQueryServiceWrapper();
        try {
            Collection<GbifResponse> gbifResponse = service.query(query);
            assertEquals(2, gbifResponse.size(), "Usually this query retrieves at least two units. " +
            		"Test failure may also be due to GBIF!" +
            		"Check http://api.gbif.org/v1/occurrence/search?basisOfRecord=PRESERVED_SPECIMEN&limit=100&recordedBy=E.+J.+Palmer&scientificName=Campanula+persicifolia");
        } catch (ClientProtocolException | URISyntaxException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGbifWebServiceNoParameters() {
    	OccurenceQuery query = new OccurenceQuery(null, null, null, null, null, null, null, null, null, false);
    	GbifQueryServiceWrapper service = new GbifQueryServiceWrapper();
    	try {
    		service.query(query);
    	} catch (IOException | URISyntaxException e) {
    		fail(e.getMessage());
    	}
    }
}