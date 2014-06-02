// $Id$
/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.occurrence.bioCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import org.junit.Test;

import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.ext.occurrence.OccurenceQuery;

/**
 * @author pplitzner
 * @date 16.09.2013
 *
 */
public class BioCaseQueryServiceWrapperTest extends TestCase{

    public static final Logger logger = Logger.getLogger(BioCaseQueryServiceWrapperTest.class);

    @Test
    public void testQuery() {

        if(UriUtils.isInternetAvailable(null)){
            BioCaseQueryServiceWrapper queryService = new BioCaseQueryServiceWrapper();
            try {
                OccurenceQuery query = new OccurenceQuery("Campanula*", null, null, null, null, null, null, null, null);
                InputStream response = queryService.query(query);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response));
                String line = null;
                do {
                    if(line!=null){
                        System.out.println(line);
                    }
                    line = reader.readLine();
                } while (line!=null);
            } catch (ClientProtocolException e) {
                fail(e.getMessage());
            } catch (IOException e) {
                fail(e.getMessage());
            } catch (URISyntaxException e) {
                fail(e.getMessage());
            }
        } else {
            logger.warn("SKIPPING TEST: no internet connectivity available");
        }
    }

    @Test
    public void testQueryForUnitId(){
        BioCaseQueryServiceWrapper service = new BioCaseQueryServiceWrapper();
        try {
            InputStream queryForSingleUnit = service.query(new OccurenceQuery("29596"), new URIBuilder("http://www.flora-mv.de/biocase/pywrapper.cgi?dsa=hoeherePflanzen").build());
        } catch (ClientProtocolException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        } catch (URISyntaxException e) {
            fail(e.getMessage());
        }
    }
}
