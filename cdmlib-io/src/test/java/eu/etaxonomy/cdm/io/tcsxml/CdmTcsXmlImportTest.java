/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.tcsxml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.tcsxml.in.CdmTcsXmlImport;
import eu.etaxonomy.cdm.io.tcsxml.in.TcsXmlImportConfigurator;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @since 29.01.2009
 */
public class CdmTcsXmlImportTest extends CdmTransactionalIntegrationTest {

	@SpringBeanByType
	private CdmTcsXmlImport cdmTcsXmlImport;

	@SpringBeanByType
	private INameService nameService;

	private IImportConfigurator configurator;

	@Before
	public void setUp() throws URISyntaxException {
		String inputFile = "/eu/etaxonomy/cdm/io/tcsxml/TcsXmlImportConfiguratorTest-input.xml";
		URL url = this.getClass().getResource(inputFile);
		assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
		configurator = TcsXmlImportConfigurator.NewInstance(URI.fromUrl(url), null);
		assertNotNull("Configurator could not be created", configurator);
	}

	@Test
	public void testInit() {
		assertNotNull("cdmTcsXmlImport should not be null", cdmTcsXmlImport);
		assertNotNull("nameService should not be null", nameService);
	}

	@Test
	public void testDoInvoke() {
		boolean result = cdmTcsXmlImport.invoke(configurator).isSuccess();
		assertTrue("Return value for import.invoke should be true", result);
		//assertEquals("Number of TaxonNames should be 16", 16, nameService.count(null));
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}