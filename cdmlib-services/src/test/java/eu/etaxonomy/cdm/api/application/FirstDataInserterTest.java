// $Id$
/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.application;

import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;

import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * test for the {@link FirstDataInserter}
 *
 * @author a.kohlbecker
 * @date Oct 12, 2012
 *
 */
@DataSet
public class FirstDataInserterTest extends CdmTransactionalIntegrationTest {

    private final String[] tableNames = new String[]{"USERACCOUNT", "USERACCOUNT_GRANTEDAUTHORITYIMPL", "GRANTEDAUTHORITYIMPL", "CDMMETADATA"};

    @Test
    @DataSet(value="FirstDataInserterTest.testBlankDB.xml")
    @ExpectedDataSet(value="FirstDataInserterTest.testBlankDB-result.xml")
    public void testBlankDB(){

        commitAndStartNewTransaction(null);
//        printDataSet(System.err, tableNames);
    }

}
