/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.tcsxml;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.tcsxml.in.CdmTcsXmlImport;
import eu.etaxonomy.cdm.io.tcsxml.in.TcsXmlImportConfigurator;
import eu.etaxonomy.cdm.test.integration.spring.CdmIoTransactionalSpringIntegrationTest;

/**
 * @author a.mueller
 * @since 29.01.2009
 */
public class CdmTcsXmlImportTest extends CdmIoTransactionalSpringIntegrationTest {

	@Autowired
	private CdmTcsXmlImport cdmTcsXmlImport;

	@Autowired
	private INameService nameService;

	private IImportConfigurator configurator;

	@BeforeEach
	public void setUp() throws URISyntaxException {
		String inputFile = "/eu/etaxonomy/cdm/io/tcsxml/TcsXmlImportConfiguratorTest-input.xml";
		URL url = this.getClass().getResource(inputFile);
		assertNotNull(url, "URL for the test file '" + inputFile + "' does not exist");
		configurator = TcsXmlImportConfigurator.NewInstance(URI.fromUrl(url), null);
		assertNotNull(configurator, "Configurator could not be created");
	}

	@Test
	public void testInit() {
		assertNotNull(cdmTcsXmlImport, "cdmTcsXmlImport should not be null");
		assertNotNull(nameService, "nameService should not be null");
	}

	@Test
	public void testDoInvoke() {
		boolean result = cdmTcsXmlImport.invoke(configurator).isSuccess();
		assertTrue(result, "Return value for import.invoke should be true");
		//assertEquals(16, nameService.count(null), "Number of TaxonNames should be 16");
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}