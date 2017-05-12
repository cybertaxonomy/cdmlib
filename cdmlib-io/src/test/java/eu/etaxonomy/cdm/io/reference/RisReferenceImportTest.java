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

import java.io.FileNotFoundException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportResult;
import eu.etaxonomy.cdm.io.reference.ris.in.RisReferenceImportConfigurator;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author andy
 *
 */
public class RisReferenceImportTest extends CdmTransactionalIntegrationTest {

	@SpringBeanByName
	private CdmApplicationAwareDefaultImport<?> defaultImport;

	@SpringBeanByType
	private INameService nameService;

	private IImportConfigurator configurator;

	@Before
	public void setUp() {
		String inputFile = "/eu/etaxonomy/cdm/io/reference/RisReferenceImportTest-input.ris";
		URL url = this.getClass().getResource(inputFile);
		assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
		try {
			configurator = RisReferenceImportConfigurator.NewInstance(url, null);
		} catch (Exception e) {
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
		assertNotNull("configurator should not be null", configurator);
	}

	@Test
	public void testDoInvokeWithoutExceptions() {
		ImportResult result = defaultImport.invoke(configurator);
		String report = result.createReport().toString();
		System.out.println(report);
	}

	@Test
	@Ignore("Import does not fully work yet")
	public void testDoInvoke() {
		boolean result = defaultImport.invoke(configurator).isSuccess();
		//TODO result is still false
		logger.warn("No real testing for ris import yet");
		Assert.assertTrue("Return value for import.invoke() should be true", result);
//		assertEquals("Number of TaxonNames should be 5", 5, nameService.count());
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
