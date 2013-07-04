/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.redlist.bfnXml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * 
 * @author a.oppermann
 * @date 04.07.2013
 *
 */
public class BfnXmlImportTest extends CdmTransactionalIntegrationTest {
	
	Logger logger = Logger.getLogger(BfnXmlImportTest.class);
	@SpringBeanByName
	BfnXmlTaxonImport bfnXmlTaxonImport;

	@SpringBeanByType
	INameService nameService;

	private IImportConfigurator configurator;
	
	@Before
	public void setUp() throws URISyntaxException {
		String inputFile = "/home/alex/developement/cdmlib-app/app-import-bfn/src/test/resources/eu/etaxonomy/cdm/io/redlist/bfnXml/bfnXmlTest-input.xml";
		URL url = this.getClass().getResource(inputFile);
		assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
		configurator = BfnXmlImportConfigurator.NewInstance(url.toURI(), null);
		assertNotNull("Configurator could not be created", configurator);
	}
	
	@Test
	public void testInit() {
//		assertNotNull("cdmTcsXmlImport should not be null", bfnXmlTaxonImport);
//		assertNotNull("nameService should not be null", nameService);
	}
	
	@Test
	public void testDoInvoke() {
//		boolean result = bfnXmlTaxonImport.invoke(null);
//		assertTrue("Return value for import.invoke should be true", result);
//		assertEquals("Number of TaxonNames should be 16", 16, nameService.count(null));
	}

}
