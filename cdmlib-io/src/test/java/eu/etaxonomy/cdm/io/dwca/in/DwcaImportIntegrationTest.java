/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.events.LoggingIoObserver;
import eu.etaxonomy.cdm.test.integration.spring.CdmIoTransactionalSpringIntegrationTest;

/**
 * @author a.mueller
 * @since 23.11.2011
 */
public class DwcaImportIntegrationTest extends CdmIoTransactionalSpringIntegrationTest{

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	@Autowired
    @Qualifier("defaultImport")
    private CdmApplicationAwareDefaultImport<?> defaultImport;

	private URI uri;
	private DwcaImportConfigurator configurator;

	@BeforeEach
	public void setUp() throws Exception {
		String inputFile = "/eu/etaxonomy/cdm/io/dwca/in/DwcaZipToStreamConverterTest-input.zip";
		URL url = this.getClass().getResource(inputFile);
		uri = URI.fromUrl(url);
		assertNotNull(uri, "URI for the test file '" + inputFile + "' does not exist");
		try {
			configurator = DwcaImportConfigurator.NewInstance(URI.fromUrl(url), null);
			configurator.addObserver(new LoggingIoObserver());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail();
		}
		assertNotNull(configurator, "Configurator could not be created");
	}

	@Test
	public void testInit() {
		assertNotNull(defaultImport, "import instance should not be null");
		assertNotNull(configurator, "configurator instance should not be null");
	}

	@Test
	public void testInvoke() {
		configurator.setDefaultPartitionSize(3);
		boolean result = defaultImport.invoke(configurator).isSuccess();
		assertTrue(result, "Invoke should return true");
		//to be continued
//		final String[]tableNames = {"TaxonBase","TaxonName","Classification",
//                "TaxonNode","HomotypicalGroup"};
//		commitAndStartNewTransaction(tableNames);
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}

}