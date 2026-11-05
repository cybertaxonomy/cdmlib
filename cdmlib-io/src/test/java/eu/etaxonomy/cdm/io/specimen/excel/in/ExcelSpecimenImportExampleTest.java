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

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmExpectedDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmIoTransactionalSpringIntegrationTest;

/**
 * @author a.mueller
 * @since 10.05.2011
 */
public class ExcelSpecimenImportExampleTest extends CdmIoTransactionalSpringIntegrationTest {

	@Autowired
    @Qualifier("defaultImport")
    private CdmApplicationAwareDefaultImport<?> defaultImport;

	@Autowired
	private INameService nameService;

	@Autowired
	private IOccurrenceService occurrenceService;

	private IImportConfigurator configurator;
	private IImportConfigurator configuratorXslx;

	@BeforeEach
	public void setUp() {
		//xsl
		try {
			String inputFile = "/eu/etaxonomy/cdm/io/specimen/excel/in/ExcelSpecimenImportExampleTest-input.xls";
			URL url = this.getClass().getResource(inputFile);
			assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
			configurator = SpecimenCdmExcelImportConfigurator.NewInstance(URI.fromUrl(url), null,false);
			configurator.setNomenclaturalCode(NomenclaturalCode.ICNAFP);
			assertNotNull("Configurator could not be created", configurator);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			Assert.fail("xsl configurator could not be created");
		}

		//xslx
		try {
			String inputFile = "/eu/etaxonomy/cdm/io/specimen/excel/in/ExcelSpecimenImportExampleTest-input.xlsx";
			URL url = this.getClass().getResource(inputFile);
			assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
			configuratorXslx = SpecimenCdmExcelImportConfigurator.NewInstance(URI.fromUrl(url), null,false);
			configuratorXslx.setNomenclaturalCode(NomenclaturalCode.ICNAFP);
			assertNotNull("Configurator could not be created", configurator);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			Assert.fail("Xslx configurator could not be created");
		}
	}

	@Test
	public void testInit() {
		assertNotNull("import instance should not be null", defaultImport);
		assertNotNull("nameService should not be null", nameService);
		assertNotNull("occurence service should not be null", occurrenceService);
	}

	 @Test
//	 @Disabled  //does not run together with testResultSet or others
	 @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
	 public void testDoInvoke() {
		 boolean result = defaultImport.invoke(configurator).isSuccess();
		 assertTrue("Return value for import.invoke should be true", result);
		 assertEquals("Number of specimen should be 3", 3,
		 occurrenceService.count(DerivedUnit.class));
		 assertEquals("Number of field units should be 3", 3,
		 occurrenceService.count(FieldUnit.class));

//		 printDataSet(System.out, new String[]{"SpecimenOrObservationBase","GatheringEvent","DerivationEvent"});
	 }

	 @Test
//	 @Disabled //does not run together with testResultSet or others
	 @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
	 public void testDoInvokeXslx() {
		 boolean result = defaultImport.invoke(configurator).isSuccess();
		 assertTrue("Return value for import.invoke should be true", result);
		 assertEquals("Number of specimen should be 3", 3,
		 occurrenceService.count(DerivedUnit.class));
		 assertEquals("Number of field units should be 3", 3,
		 occurrenceService.count(FieldUnit.class));
		 this.rollback();

//		 printDataSet(System.out, new String[]{"SpecimenOrObservationBase","GatheringEvent","DerivationEvent"});
	 }

	@Test
	@CdmDataSet
	@CdmExpectedDataSet
	@Disabled
	public void testResultSet() {
		boolean result = defaultImport.invoke(configurator).isSuccess();
		assertTrue("Return value for import.invoke should be true", result);
		assertEquals("Number of specimen should be 3", 3, occurrenceService.count(DerivedUnit.class));
		assertEquals("Number of field units should be 3", 3, occurrenceService.count(FieldUnit.class));

//		printDataSet(System.out, new String[]{"SpecimenOrObservationBase","DESCRIPTIONELEMENTBASE","DEFINEDTERMBASE"});
//

//		try {
//			String filePath = System.getProperty("java.io.tmpdir")
//					+ File.separator + "excelSpecimenOutput.xml";
//			File file = new File(filePath);
//			FileOutputStream myOut = new FileOutputStream(file);
//			System.out.println(file.getAbsolutePath());
//			printDataSet(myOut, new String[] { "AgentBase", "Collection",
//					"DerivationEvent", "DeterminationEvent",
//					"DescriptionElementBase", "DescriptionBase", "Extension",
//					"GatheringEvent", "GatheringEvent_DefinedTermBase",
//					"LanguageString", "OriginalSourceBase", "Reference",
//					"TaxonBase", "TaxonName", "TypeDesignationBase",
//					"TaxonName_TypeDesignationBase",
//					"SpecimenOrObservationBase", "DefinedTermBase",
//					"TermVocabulary", "Representation" });
//			// printDataSet(myOut);
//		} catch (FileNotFoundException e) {
//			Assert.fail(e.getLocalizedMessage());
//		}
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
