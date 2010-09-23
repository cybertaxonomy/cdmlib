/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.reference;

import static org.junit.Assert.assertNotNull;

import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.reference.endnote.in.EndnoteImportConfigurator;

/**
 * @author andy
 *
 */
@Ignore //TODO
public class EndnoteRecordsImportTest /*extends CdmTransactionalIntegrationTest */{
	
//	@SpringBeanByName
//	CdmApplicationAwareDefaultImport<?> defaultImport;

	@SpringBeanByType
	INameService nameService;

	private IImportConfigurator configurator;
	
	@Before
	public void setUp() {
		String inputFile = "/eu/etaxonomy/cdm/io/reference/EndnoteRecordImportTest-input.xml";
		URL url = this.getClass().getResource(inputFile);
		assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
		try {
			configurator = EndnoteImportConfigurator.NewInstance(url.toURI(), null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			Assert.fail();
		}
		assertNotNull("Configurator could not be created", configurator);
	}
	
//***************************** TESTS *************************************//	
	
	@Test
	public void testInit() {
//		assertNotNull("XXX should not be null", defaultImport);
		assertNotNull("nameService should not be null", nameService);
	}
	
	@Test
	@Ignore
	public void testDoInvoke() {
		DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
		ICdmDataSource cdmDestination = CdmDataSource.NewH2EmbeddedInstance("endnoteTest", "sa", "", null);;
		String inputFile = "/eu/etaxonomy/cdm/io/reference/EndnoteRecordImportTest-input.xml";
		URL url = this.getClass().getResource(inputFile);
		assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
		
		EndnoteImportConfigurator config;
		try {
			config = EndnoteImportConfigurator.NewInstance(url.toURI(), cdmDestination);
			config.setDbSchemaValidation(hbm2dll);
			
			CdmDefaultImport<EndnoteImportConfigurator> defaultImport = new CdmDefaultImport<EndnoteImportConfigurator>();
			defaultImport.invoke(config);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			Assert.fail();
		}
		
		
		//IIboolean result = defaultImport.invoke(confi
	
//		Assert.assertTrue("Return value for import.invoke() should be true", result);
	//	assertEquals("Number of TaxonNames should be 5", 5, nameService.count());
	}
}
