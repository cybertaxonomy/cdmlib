/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.excel.in;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.test.integration.spring.CdmIoTransactionalSpringIntegrationTest;

/**
 * @author a.mueller
 * @since 29.01.2009
 */
//@Disabled("Test class is just a copy of the ABCD import test. It still needs to be adapted")
public class ExcelImportConfiguratorTest extends CdmIoTransactionalSpringIntegrationTest {

	@Autowired
    @Qualifier("defaultImport")
    private CdmApplicationAwareDefaultImport<?> defaultImport;

	@Autowired
	private INameService nameService;

	@Autowired
	private IOccurrenceService occurrenceService;

	private IImportConfigurator configurator;

	@BeforeEach
	public void setUp() throws URISyntaxException {
		String inputFile = "/eu/etaxonomy/cdm/io/specimen/excel/in/ExcelImportConfiguratorTest-input.xls";
		URL url = this.getClass().getResource(inputFile);
		assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
		configurator = SpecimenSynthesysExcelImportConfigurator.NewInstance(URI.fromUrl(url), null, false);
		assertNotNull("Configurator could not be created", configurator);
	}

	@Test
	public void testInit() {
		assertNotNull("import instance should not be null", defaultImport);
		assertNotNull("nameService should not be null", nameService);
		assertNotNull("occurence service should not be null", occurrenceService);
	}
/*
	@Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
	public void testDoInvoke() {
		boolean result = defaultImport.invoke(configurator);
		assertTrue("Return value for import.invoke should be true", result);
		assertEquals("Number of TaxonNames should be 3", 3, nameService.count(null));
		assertEquals("Number of specimen should be 6", 6, occurrenceService.count(SpecimenOrObservationBase.class));
		assertEquals("Number of specimen should be 3", 3, occurrenceService.count(DerivedUnit.class));
		assertEquals("Number of specimen should be 3", 3, occurrenceService.count(FieldUnit.class));

	}
*/

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
