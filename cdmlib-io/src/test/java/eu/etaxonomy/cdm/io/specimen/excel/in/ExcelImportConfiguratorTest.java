/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.excel.in;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @created 29.01.2009
 * @version 1.0
 */
//@Ignore("Test class is just a copy of the ABCD import test. It still needs to be adapted")  
public class ExcelImportConfiguratorTest extends CdmTransactionalIntegrationTest {
	
	@SpringBeanByName
	CdmApplicationAwareDefaultImport<?> defaultImport;

	@SpringBeanByType
	INameService nameService;

	@SpringBeanByType
	IOccurrenceService occurrenceService;

	
	private IImportConfigurator configurator;
	
	@Before
	public void setUp() throws URISyntaxException {
		String inputFile = "/eu/etaxonomy/cdm/io/specimen/excel/in/ExcelImportConfiguratorTest-input.xls";
		URL url = this.getClass().getResource(inputFile);
		assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
		configurator = SpecimenSynthesysExcelImportConfigurator.NewInstance(url.toURI(), null);
		assertNotNull("Configurator could not be created", configurator);
	}
	
	@Test
	public void testInit() {
		assertNotNull("import instance should not be null", defaultImport);
		assertNotNull("nameService should not be null", nameService);
		assertNotNull("occurence service should not be null", occurrenceService);
	}
	
	@Test
	@DataSet("../../../BlankDataSet.xml")
	public void testDoInvoke() {
		boolean result = defaultImport.invoke(configurator);
		assertTrue("Return value for import.invoke should be true", result);
		assertEquals("Number of TaxonNames should be 3", 3, nameService.count(null));
		assertEquals("Number of specimen should be 6", 6, occurrenceService.count(SpecimenOrObservationBase.class));
		assertEquals("Number of specimen should be 3", 3, occurrenceService.count(DerivedUnitBase.class));
		assertEquals("Number of specimen should be 3", 3, occurrenceService.count(FieldObservation.class));
		
	}

}
