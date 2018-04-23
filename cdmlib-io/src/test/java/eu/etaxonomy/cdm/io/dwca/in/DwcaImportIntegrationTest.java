/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.spring.annotation.SpringBeanByName;

import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.events.LoggingIoObserver;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 \* @since 23.11.2011
 */
@Transactional(TransactionMode.ROLLBACK)
public class DwcaImportIntegrationTest  extends CdmTransactionalIntegrationTest{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaImportIntegrationTest.class);

	@SpringBeanByName
	private CdmApplicationAwareDefaultImport<?> defaultImport;

	private URI uri;
	private DwcaImportConfigurator configurator;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		String inputFile = "/eu/etaxonomy/cdm/io/dwca/in/DwcaZipToStreamConverterTest-input.zip";
		URL url = this.getClass().getResource(inputFile);
		uri = url.toURI();
		assertNotNull("URI for the test file '" + inputFile + "' does not exist", uri);
		try {
			configurator = DwcaImportConfigurator.NewInstance(url.toURI(), null);
			configurator.addObserver(new LoggingIoObserver());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			Assert.fail();
		}
		assertNotNull("Configurator could not be created", configurator);
	}

	@Test
	public void testInit() {
		assertNotNull("import instance should not be null", defaultImport);
		assertNotNull("configurator instance should not be null", configurator);
	}

	@Test
	public void testInvoke() {
		configurator.setDefaultPartitionSize(3);
		boolean result = defaultImport.invoke(configurator).isSuccess();
		Assert.assertTrue("Invoke should return true", result);
		//to be continued
//		final String[]tableNames = {"TaxonBase","TaxonName","Classification",
//                "TaxonNode","HomotypicalGroup"};
//		commitAndStartNewTransaction(tableNames);
	}


    @Override
    public void createTestDataSet() throws FileNotFoundException {}

}
