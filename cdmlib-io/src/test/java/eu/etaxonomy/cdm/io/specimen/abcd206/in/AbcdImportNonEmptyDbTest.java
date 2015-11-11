/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * This test was formerly used to generally test if an AbcdImport can be run successful
 * and later it was also used to run import into a prefilled database.
 * We do now have more specific test classes {@link SpecimenImportConfiguratorTest}
 * and {@link AbcdGgbnImportTest} which do cover the specific single cases better.
 *
 * Also the input data for the test is incomplete so we could just throw it away.
 * However, as even incomplete and superficial tests sometimes find unexpected errors
 * we may keep it but not further develop it as long as it does not create larger
 * problems.
 *
 * @author a.mueller
 * @created 29.01.2009
 */
public class AbcdImportNonEmptyDbTest extends CdmTransactionalIntegrationTest {

	@SpringBeanByName
	CdmApplicationAwareDefaultImport<?> defaultImport;

	@SpringBeanByType
	INameService nameService;

	@SpringBeanByType
	IOccurrenceService occurrenceService;

	@SpringBeanByType
	ICommonService commonService;


	private IImportConfigurator configurator;

	@Before
	public void setUp() {
		String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/AbcdImportTestCalvumPart2.xml";
		URL url = this.getClass().getResource(inputFile);
        assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
        try {
            configurator = Abcd206ImportConfigurator.NewInstance(url.toURI(), null,false);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Assert.fail();
        }
        assertNotNull("Configurator3 could not be created", configurator);

	}

	@Test
	public void testInit() {
		assertNotNull("import instance should not be null", defaultImport);
		assertNotNull("nameService should not be null", nameService);
		assertNotNull("occurence service should not be null", occurrenceService);
		assertNotNull("common service should not be null", commonService);
	}

	@Test
    @DataSet( value="AbcdImportNonEmptyDbTest.xml")  //loadStrategy=CleanSweepInsertLoadStrategy.class
	public void testDoInvoke() {
        boolean result = defaultImport.invoke(configurator).isSuccess();
        assertTrue("Return value for import.invoke should be true", result);
        assertEquals("Number of TaxonNames is incorrect", 13, nameService.count(TaxonNameBase.class));
        assertEquals("Number of specimen is incorrect", 11, occurrenceService.count(DerivedUnit.class));
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
