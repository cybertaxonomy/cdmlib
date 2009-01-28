package eu.etaxonomy.cdm.io.jaxb;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.net.URL;

import org.dbunit.Assertion;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class CdmImporterTest extends CdmTransactionalIntegrationTest{
	
	@SpringBeanByType
	CdmImporter cdmImporter;
	
	private IImportConfigurator configurator;
	
	@Before
	public void setUp() {
		URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/jaxb/CdmImporterTest-input.xml");
		configurator = JaxbImportConfigurator.NewInstance(url.toString(),null);
	}
	
	
	@Test
	public void testInit() {
		assertNotNull("cdmImporter should not be null",cdmImporter);
	}
	
	@Test
	@DataSet
	public void testImport() throws Exception {
		cdmImporter.doInvoke(configurator, null);
		testExpectedDataSet(this.getClass().getResourceAsStream("/eu/etaxonomy/cdm/io/jaxb/CdmImporterTest.testImport-result.xml"));
	}

	protected void testExpectedDataSet(InputStream dataSet) {
		try {
			IDatabaseConnection databaseConnection = getConnection();
			
			IDataSet actualDataSet = databaseConnection.createDataSet();
			IDataSet expectedDataSet = new FlatXmlDataSet(dataSet, this.getClass().getResourceAsStream("/eu/etaxonomy/cdm/io/dataset.dtd"));

			Assertion.assertEquals(expectedDataSet,actualDataSet);
			
		} catch (Exception e) {
			System.out.println(e);
			logger.error(e);
			for(StackTraceElement ste : e.getStackTrace()) {
				logger.error(ste);
			}
			fail("No exception expected in database validation method");
		} 
	}
}
