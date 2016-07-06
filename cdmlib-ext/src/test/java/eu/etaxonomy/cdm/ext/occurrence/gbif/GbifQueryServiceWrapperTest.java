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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.ext.occurrence.OccurenceQuery;

/**
 * @author pplitzner
 * @date 20.05.2014
 *
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
        Assert.assertEquals("number of records found is incorrect", 1, records.size());
        GbifResponse gbifResponse = records.iterator().next();
        Assert.assertEquals("Locality is incorrect", LOCALITY_STRING, gbifResponse.getDerivedUnitFacade().getLocalityText());
        Assert.assertEquals("protocol is wrong", GbifDataSetProtocol.BIOCASE, gbifResponse.getDataSetProtocol());
        Assert.assertEquals("protocol is wrong", new URI("http://api.gbif.org/v1/dataset/26a49731-9457-45b2-9105-1b96063deb26/endpoint"), gbifResponse.getDataSetUri());
    }

    @Test
    public void testJsonOriginalDataSetUriParsing(){
        DataSetResponse response = GbifJsonOccurrenceParser.parseOriginalDataSetUri(dummyJsonDataset);
        Assert.assertEquals("Response protocol is incorrect!", GbifDataSetProtocol.BIOCASE, response.getProtocol());
        Assert.assertEquals("Response endpoint is incorrect!", URI.create("http://www.flora-mv.de/biocase/pywrapper.cgi?dsa=hoeherePflanzen"), response.getEndpoint());
    }

    @Test
    public void testQueryParameterConversion(){
        OccurenceQuery query = new OccurenceQuery("Campanula persicifolia", "T. Henning", "1234", "ACC-2", "BGBM", "DE", "pollen herbarium", new GregorianCalendar(2014, 05, 27), new GregorianCalendar(2014,05,28));
        List<NameValuePair> queryParams = new GbifQueryGenerator().generateQueryParams(query);
        NameValuePair pair = new BasicNameValuePair("scientificName", "Campanula persicifolia");
        Assert.assertTrue("query parameter is missing", queryParams.contains(pair));
        //FIXME this will currently always fail because eventDate is still not supported by GBIF
        //"basisOfRecord" and "limit" is always set
        // + 8 from query (collectorsNumber will be represented in the two parameters "recordNumber" and "fieldNumber";
        // both dates are represented in one parameter "eventDate";
        // locality can still not be queried on GBIF web service)
//        assertEquals("Number of generated URI parameters is incorrect", 10, queryParams.size());
    }

    @Test
    public void testGbifWebService() {
        OccurenceQuery query = new OccurenceQuery("Campanula persicifolia", "E. J. Palmer", null, null, null, null, null, null, null);
        GbifQueryServiceWrapper service = new GbifQueryServiceWrapper();
        try {
            Collection<GbifResponse> gbifResponse = service.query(query);
            Assert.assertEquals("Usually this query retrieves at least two units. " +
            		"Test failure may also be due to GBIF!" +
            		"Check http://api.gbif.org/v1/occurrence/search?basisOfRecord=PRESERVED_SPECIMEN&limit=100&recordedBy=E.+J.+Palmer&scientificName=Campanula+persicifolia", 2, gbifResponse.size());
        } catch (ClientProtocolException e) {
            Assert.fail(e.getMessage());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } catch (URISyntaxException e) {
            Assert.fail(e.getMessage());
        }

    }

}
