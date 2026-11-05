/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.application;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmExpectedDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesTransactionalSpringIntegrationTest;

/**
 * test for the {@link FirstDataInserter}
 *
 * @author a.kohlbecker
 * @since Oct 12, 2012
 */
@CdmDataSet
public class FirstDataInserterTest extends CdmServicesTransactionalSpringIntegrationTest {

//    private final String[] tableNames = new String[]{"USERACCOUNT", "USERACCOUNT_GRANTEDAUTHORITYIMPL", "GRANTEDAUTHORITYIMPL", "CDMMETADATA", "PERMISSIONGROUP"};

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

    /**
     * Runs the FirstDataInserter on a blank database and
     * asserts that all groups and users have been created.
     */
    @Test
    @CdmDataSet("FirstDataInserterTest.testBlankDB.xml")
    @CdmExpectedDataSet(value="FirstDataInserterTest.testBlankDB-result.xml")
    public void testOnBlankDatabase(){

        commitAndStartNewTransaction(null);
//        printDataSet(System.err, tableNames);
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}