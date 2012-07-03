/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.jaxb;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.dbunit.Assertion;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class CdmImporterTest extends CdmTransactionalIntegrationTest{

	@SpringBeanByType
	JaxbImport jaxbImport;

	private JaxbImportConfigurator configurator;

	@Before
	public void setUp() throws URISyntaxException {
		URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/jaxb/CdmImporterTest-input.xml");
		configurator = JaxbImportConfigurator.NewInstance(url.toURI(),null);
	}


	@Test
	public void testInit() {
		assertNotNull("jaxbImport should not be null",jaxbImport);
	}


	@Test
	@DataSet
	@ExpectedDataSet("CdmImporterTest.xml")
	@Ignore
	// FIXME Dataset file is corrupt since moving to unitils. Need to check what is  
	//		Ignoring this test since it has probelems with the dataset and unitils 3.x
	// 	     => create new dataset with void eu.etaxonomy.cdm.database.TestingTermInitializerTest.testPrintDataSet()
	//		 this method has some problem though
	/**
	 * this test imports an empty data set and tests that this empty import is changing nothing
	 */
	public void testImport() throws Exception {
		jaxbImport.doInvoke(new JaxbImportState(configurator));
//		testExpectedDataSet(this.getClass().getResourceAsStream("/eu/etaxonomy/cdm/io/jaxb/CdmImporterTest.testImport-result.xml"));
	}

//	protected void testExpectedDataSet(InputStream dataSet) {
//		try {
//			IDatabaseConnection databaseConnection = getConnection();
//
//			IDataSet expectedDataSet = new FlatXmlDataSet(dataSet, this.getClass().getResourceAsStream("/eu/etaxonomy/cdm/io/dataset.dtd"));
//			IDataSet actualDataSet = new FilteredDataSet(expectedDataSet.getTableNames(),databaseConnection.createDataSet());
//
//			Assertion.assertEquals(expectedDataSet,actualDataSet);
//
//		} catch (Exception e) {
//			//System.out.println(e);
//			//logger.error(e);
//			for(StackTraceElement ste : e.getStackTrace()) {
//				logger.error(ste);
//			}
//			fail("No exception expected in database validation method");
//		}
//	}
}
