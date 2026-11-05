/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.excel.in;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
		assertNotNull(url, "URL for the test file '" + inputFile + "' does not exist");
		configurator = SpecimenSynthesysExcelImportConfigurator.NewInstance(URI.fromUrl(url), null, false);
		assertNotNull(configurator, "Configurator could not be created");
	}

	@Test
	public void testInit() {
		assertNotNull(defaultImport, "import instance should not be null");
		assertNotNull(nameService, "nameService should not be null");
		assertNotNull(occurrenceService, "occurence service should not be null");
	}
/*
	@Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
	public void testDoInvoke() {
		boolean result = defaultImport.invoke(configurator);
		assertTrue(result, "Return value for import.invoke should be true");
		assertEquals(3, nameService.count(null), "Number of TaxonNames should be 3");
		assertEquals(6, occurrenceService.count(SpecimenOrObservationBase.class), "Number of specimen should be 6");
		assertEquals(3, occurrenceService.count(DerivedUnit.class), "Number of specimen should be 3");
		assertEquals(3, occurrenceService.count(FieldUnit.class), "Number of specimen should be 3");

	}
*/

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
