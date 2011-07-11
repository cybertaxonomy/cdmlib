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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @created 10.05.2011
 * @version 1.0
 */
@Ignore //currently jenkins throws an exception
public class ExcelSpecimenImportExampleTest extends CdmTransactionalIntegrationTest {
	
	@SpringBeanByName
	CdmApplicationAwareDefaultImport<?> defaultImport;

	@SpringBeanByType
	INameService nameService;

	@SpringBeanByType
	IOccurrenceService occurrenceService;

	
	private IImportConfigurator configurator;
	
	@Before
	public void setUp() {
		String inputFile = "/eu/etaxonomy/cdm/io/specimen/excel/in/ExcelSpecimenImportExampleTest-input.xls";
		URL url = this.getClass().getResource(inputFile);
		assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
		try {
			configurator = SpecimenCdmExcelImportConfigurator.NewInstance(url.toURI(), null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			Assert.fail();
		}
		assertNotNull("Configurator could not be created", configurator);
	}
	
	@Test
	public void testInit() {
		assertNotNull("import instance should not be null", defaultImport);
		assertNotNull("nameService should not be null", nameService);
		assertNotNull("occurence service should not be null", occurrenceService);
	}
//	
//	@Test
//	public void testDoInvoke() {
//		boolean result = defaultImport.invoke(configurator);
//		assertTrue("Return value for import.invoke should be true", result);
//		assertEquals("Number of specimen should be 3", 3, occurrenceService.count(DerivedUnitBase.class));
//		assertEquals("Number of field observations should be 3", 3, occurrenceService.count(FieldObservation.class));
//			
////		printDataSet(System.out, new String[]{"SpecimenOrObservationBase","GatheringEvent","DerivationEvent"});
//		
//	}
	
	@Test
	@DataSet
	@ExpectedDataSet
	public void testResultSet() {
		boolean result = defaultImport.invoke(configurator);
		assertTrue("Return value for import.invoke should be true", result);
		assertEquals("Number of specimen should be 3", 3, occurrenceService.count(DerivedUnitBase.class));
		assertEquals("Number of field observations should be 3", 3, occurrenceService.count(FieldObservation.class));
		
		try {
			String filePath = System.getProperty("java.io.tmpdir")+File.separator+"excelSpecimenOutput.xml";
			File file = new File(filePath);
			FileOutputStream myOut = new FileOutputStream(file);
			System.out.println(file.getAbsolutePath());
			printDataSet(myOut, new String[]{"AgentBase","Collection","DerivationEvent","DeterminationEvent","DescriptionElementBase",
					"DescriptionBase","Extension","GatheringEvent","GatheringEvent_DefinedTermBase","LanguageString","OriginalSourceBase",
					"Reference","TaxonBase","TaxonNameBase","TypeDesignationBase",
					"TypeDesignationBase_taxonnamebase","SpecimenOrObservationBase","DefinedTermBase","TermVocabulary","Representation"});
//			printDataSet(myOut);
		} catch (FileNotFoundException e) {
			Assert.fail(e.getLocalizedMessage());
		}
		
	}

}
