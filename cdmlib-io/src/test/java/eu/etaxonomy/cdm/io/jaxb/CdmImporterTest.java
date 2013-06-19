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
import java.util.ArrayList;
import java.util.List;

import org.dbunit.Assertion;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
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
//		IncludeTableFilter filter = new IncludeTableFilter();
//		filter.includeTable("T*");
//		ExcludeTableFilter filter2 = new ExcludeTableFilter();
//		filter2.excludeTable("RIGHTS");
//		printDataSet(System.out, filter2);
	}


	@Test
	@DataSet
	@ExpectedDataSet("CdmImporterTest.testImport-result.xml")
	@Ignore
	// 	     => create new dataset with void eu.etaxonomy.cdm.database.TestingTermInitializerTest.testPrintDataSet()
	//		 this method has some problem though
	/**
	 * this test imports an empty data set and tests that this empty import is changing nothing
	 */
	public void testImport() throws Exception {
		jaxbImport.doInvoke(new JaxbImportState(configurator));
		testExpectedDataSet(this.getClass().getResourceAsStream("/eu/etaxonomy/cdm/io/jaxb/CdmImporterTest.xml"));
	}

	protected void testExpectedDataSet(InputStream dataSet) {
		try {
			IDatabaseConnection databaseConnection = getConnection();
			
//			InputStreamReader dtdStream = CdmUtils.getUtf8ResourceReader("eu/etaxonomy/cdm/io/dataset.dtd");
			
			FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();

//			builder.setMetaDataSetFromDtd(dtdStream);  //needed?
			IDataSet expectedDataSet = builder.build(dataSet);
			expectedDataSet = removeRights(expectedDataSet);
			ReplacementDataSet replDataSet = new ReplacementDataSet( expectedDataSet); 
			replDataSet.addReplacementObject("[null]", null);
			expectedDataSet = replDataSet;
			
			IDataSet actualDataSet = databaseConnection.createDataSet();
			actualDataSet = removeRights(actualDataSet);
			actualDataSet = new FilteredDataSet(expectedDataSet.getTableNames(),actualDataSet);
			
			
			
			Assertion.assertEquals(expectedDataSet, actualDataSet);

		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception expected in database validation method");
		}
	}


	/**
	 * @param expectedDataSet
	 * @return
	 * @throws DataSetException
	 */
	private IDataSet removeRights(IDataSet dataset) throws DataSetException {
		List<String> filteredTableNames = new ArrayList<String>();
		filteredTableNames.remove("RIGHTS");
		for (String str : dataset.getTableNames()){
			if (! str.equalsIgnoreCase("RIGHTS")){
				filteredTableNames.add(str);
			}
		}
		IDataSet filteredDataSet = new FilteredDataSet(filteredTableNames.toArray(new String[]{}),dataset);

		return filteredDataSet;
	}
}
