/**
 * Copyright (C) 2013 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.ext.occurrence.bioCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.ext.occurrence.OccurenceQuery;

/**
 * @author pplitzner
 * @since 16.09.2013
 */
public class BioCaseQueryServiceWrapperTest {

    public static final Logger logger = LogManager.getLogger();

    private static final int MAX_LINES_TO_READ = 1000;
    private static final int TIMEOUT = 60000;

    @Test(timeout=TIMEOUT)
    public void testQuery() {

        if(UriUtils.isInternetAvailable(null)){
            BioCaseQueryServiceWrapper queryService = new BioCaseQueryServiceWrapper();
            try {
                OccurenceQuery query = new OccurenceQuery("Campanula patula*", null, null, null, null, null, null, null, null, false);
                InputStream response = queryService.query(query, URI.create("http://ww3.bgbm.org/biocase/pywrapper.cgi?dsa=Herbar"));
                if(response==null){
                    logger.error("SKIPPING TEST: No response from BioCase provider");
                    return;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(response));
                String line = null;
                int count = 0;
                boolean isCorrectFormat = false;
                do {
                    if(count>MAX_LINES_TO_READ){
                        fail("Service response did not include parameter to test.");
                        break;
                    }
                    if(line!=null){
                        if(logger.isTraceEnabled()){
                            System.out.println(line);
                        }
                        //just check for recordCount attribute to see if a valid response was returned
                        String recordAttr = "recordCount=\"";
                        int index = line.indexOf(recordAttr);
                        if(index>-1){
                            isCorrectFormat = true;
                            break;
                        }
                    }
                    line = reader.readLine();
                    count++;
                } while (line!=null);
                assertTrue("BioCase response did not have the expected format", isCorrectFormat);
            } catch (NumberFormatException e) {
                fail(e.getMessage());
            } catch (ClientProtocolException e) {
                fail(e.getMessage());
            } catch (IOException e) {
                fail(e.getMessage());
            }
        } else {
            logger.error("SKIPPING TEST: no internet connectivity available");
            return;
        }
    }

    @Test//(timeout=TIMEOUT)
    public void testQueryForUnitId(){

        if(UriUtils.isInternetAvailable(null)){
            BioCaseQueryServiceWrapper service = new BioCaseQueryServiceWrapper();
            try {

                Set<String[]> unitIds = new HashSet<>();
                String[] unitIdArray ={"B 10 0463639"};

                unitIds.add(unitIdArray);
                InputStream queryForSingleUnit = service.query(new OccurenceQuery(unitIds), URI.create("https://ww3.bgbm.org/biocase/pywrapper.cgi?dsa=Herbar"));

                if(queryForSingleUnit==null){
                    logger.error("SKIPPING TEST: No response from BioCase provider");
                    return;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(queryForSingleUnit));
                String line = null;
                int count = 0;
                do {
                    if(count>MAX_LINES_TO_READ){
                        fail("Service response did not include parameter to test.");
                        break;
                    }
                    if(line!=null){
                        if(logger.isTraceEnabled()){
                            System.out.println(line);
                        }
                        String recordAttr = "recordCount=\"";
                        int index = line.indexOf(recordAttr);
                        if(index>-1){
                            String recordCount = line.substring(index+recordAttr.length(), index+recordAttr.length()+1);
                            assertEquals("Incorrect number of occurrences", 1, Integer.parseInt(recordCount.trim()));
                        }
                        String unitId = "<abcd:UnitID>";
                        int indexId = line.indexOf(unitId);
                        if(indexId>-1){
                            @SuppressWarnings("unused")
                            String id = line.substring(indexId+unitId.length(), indexId+unitId.length()+5);
                           // assertEquals("Incorrect UnitId", 29596, Integer.parseInt(id));
                           // break;
                        }
                    }
                    line = reader.readLine();
                    count++;
                } while (line!=null);
                unitIds = new HashSet<>();
                String[] unitIdsArray = {"B -W 16385 -01 0"};
                unitIds.add(unitIdsArray);
                String[] unitIdsArray2 ={"B 10 0641985"};
                unitIds.add(unitIdsArray2);
                queryForSingleUnit = service.query(new OccurenceQuery(unitIds), URI.create("http://ww3.bgbm.org/biocase/pywrapper.cgi?dsa=Herbar"));
                if(queryForSingleUnit==null){
                    logger.error("SKIPPING TEST: No response from BioCase provider");
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(queryForSingleUnit));
                line = null;
                count = 0;
                do {
                    if(count>MAX_LINES_TO_READ){
                        fail("Service response did not include parameter to test.");
                        break;
                    }
                    if(line!=null){
                        if(logger.isTraceEnabled()){
                            System.out.println(line);
                        }
                        String recordAttr = "recordCount=\"";
                        int index = line.indexOf(recordAttr);
                        if(index>-1){
                            String recordCount = line.substring(index+recordAttr.length(), index+recordAttr.length()+1);
                            assertEquals("Incorrect number of occurrences", 2, Integer.parseInt(recordCount));
                        }
                    }
                    line = reader.readLine();
                    count++;
                } while (line!=null);
            } catch (NumberFormatException | IOException e) {
                fail(e.getMessage());
            }
        } else {
            logger.error("SKIPPING TEST: no internet connectivity available");
            return;
        }
    }


}
