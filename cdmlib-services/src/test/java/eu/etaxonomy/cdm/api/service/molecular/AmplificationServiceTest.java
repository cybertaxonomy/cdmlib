/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.molecular;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.test.integration.spring.CdmServicesTransactionalSpringIntegrationTest;

/**
 * @author pplitzner
 * @since 31.03.2014
 */
public class AmplificationServiceTest extends CdmServicesTransactionalSpringIntegrationTest {

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

    @Test
    public void testNothingTest(){
    	//dummy as min 1 Test is required
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}