/**
 * Copyright (C) 2010 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.ext.sru;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.ext.dc.DublinCoreSchemaAdapter;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @since 02.08.2010
 */
public class SruServiceWrapperTest {

    private static final Logger logger = LogManager.getLogger();

    private static final String baseUrl = "http://gso.gbv.de/sru/DB=1.83/";

    private SruServiceWrapper sruServiceWrapper;

    private static boolean internetIsAvailable = true;

    @BeforeAll
    public static void setUpClass() throws Exception {
        internetIsAvailable = true;
    }

    @BeforeEach
    public void setUp() throws Exception {
        sruServiceWrapper = new SruServiceWrapper();
        sruServiceWrapper.setBaseUrl(baseUrl);
        sruServiceWrapper.addSchemaAdapter(new DublinCoreSchemaAdapter());
    }

// ******************************* TESTS ******************************************************/

    @Test
    @Disabled // ignoring since Global References Index to Biodiversity has problems
    public void testDoSearchRetrieve(){

        List<Reference> refList_1 = sruServiceWrapper.doSearchRetrieve("pica.tit=\"Linnaei Species Plantarum\"", "dc");
        // -> http://gso.gbv.de/sru/DB=2.1/?version=1.1&operation=searchRetrieve&query=pica.tit%3D%22Species+Plantarum%22&recordSchema=dc

        if (testInternetConnectivity(refList_1)){

            assertEquals(5, refList_1.size(), "There should be exactly 5 results for 'Linnaei Species Plantarum'");
            Reference reference_1 = refList_1.get(0);
            logger.info(reference_1.toString());
            //title cache
            assertEquals("Caroli Linnaei species plantarum", reference_1.getTitleCache(), "Title of first entry should be 'Caroli Linnaei species plantarum'");

            //--------------------------

            List<Reference> refList_2 = sruServiceWrapper.doSearchRetrieve("pica.all = \"Species+plantarum\" and pica.dst = \"8305\"", "dc");
            // -> http://gso.gbv.de/sru/DB=2.1/?version=1.1&operation=searchRetrieve&query=pica.tit%3D%22Species+Plantarum%22&recordSchema=dc

            assertTrue(refList_2.size() > 0, "There should be at least 1 result for 'species+plantarum' and digitized");
            Reference reference_2 = refList_2.get(0);
            logger.info(reference_2.toString());
        }
    }

    private boolean testInternetConnectivity(List<?> list) {
        if (list == null || list.isEmpty()){
            boolean result = internetIsAvailable && UriUtils.isInternetAvailable(null);
            internetIsAvailable = result;
            return result;

        }
        return true;
    }


}
