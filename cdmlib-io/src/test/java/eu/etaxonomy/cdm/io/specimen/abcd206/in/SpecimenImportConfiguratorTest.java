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

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @created 29.01.2009
 * @version 1.0
 */
public class SpecimenImportConfiguratorTest extends CdmTransactionalIntegrationTest {

	@SpringBeanByName
	CdmApplicationAwareDefaultImport<?> defaultImport;

	@SpringBeanByType
	INameService nameService;

	@SpringBeanByType
	IOccurrenceService occurrenceService;

	@SpringBeanByType
	ITermService termService;


	private IImportConfigurator configurator;
	private IImportConfigurator configurator2;
	private IImportConfigurator configurator3;

	@Before
	public void setUp() {
		String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/SpecimenImportConfiguratorTest-input.xml";
		URL url = this.getClass().getResource(inputFile);
		assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
		try {
			configurator = Abcd206ImportConfigurator.NewInstance(url.toURI(), null,false);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			Assert.fail();
		}
		assertNotNull("Configurator could not be created", configurator);

		inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/ABCDImportTestCalvumPart1.xml";
        url = this.getClass().getResource(inputFile);
        assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
        try {
            configurator2 = Abcd206ImportConfigurator.NewInstance(url.toURI(), null,false);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Assert.fail();
        }
        assertNotNull("Configurator2 could not be created", configurator2);

//        inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/ABCDImportTestCalvumPart2.xml";
//        url = this.getClass().getResource(inputFile);
//        assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
//        try {
//            configurator3 = Abcd206ImportConfigurator.NewInstance(url.toURI(), null,false);
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//        assertNotNull("Configurator3 could not be created", configurator3);

	}

	@Test
	public void testInit() {
		assertNotNull("import instance should not be null", defaultImport);
		assertNotNull("nameService should not be null", nameService);
		assertNotNull("occurence service should not be null", occurrenceService);
		assertNotNull("term service should not be null", termService);
	}

//	@Test
//	@DataSet( value="../../../BlankDataSet.xml")  //loadStrategy=CleanSweepInsertLoadStrategy.class
//	public void testDoInvoke() {
//		boolean result = defaultImport.invoke(configurator);
//		assertTrue("Return value for import.invoke should be true", result);
//		assertEquals("Number of TaxonNames is incorrect", 2, nameService.count(TaxonNameBase.class));
//		assertEquals("Number of specimen is incorrect", 10, occurrenceService.count(DerivedUnitBase.class));
//	}

	@Test
    @DataSet( value="../../../BlankDataSet.xml")  //loadStrategy=CleanSweepInsertLoadStrategy.class
    public void testDoInvoke2() {
        boolean result = defaultImport.invoke(configurator2);
        assertTrue("Return value for import.invoke should be true", result);
        assertEquals("Number of TaxonNames is incorrect", 2, nameService.count(TaxonNameBase.class));
        assertEquals("Number of specimen and observation is incorrect", 10, occurrenceService.count(DerivedUnit.class));
        try {
            writeDbUnitDataSetFile(new String[] {
                    "TAXONBASE", "TAXONNAMEBASE",
                    "REFERENCE", "DESCRIPTIONELEMENTBASE", "DESCRIPTIONBASE",
                    "AGENTBASE", "CLASSIFICATION", "CLASSIFICATION_TAXONNODE", "TAXONNODE",
                    "HOMOTYPICALGROUP", "LANGUAGESTRING","COLLECTION","SPECIMENOROBSERVATIONBASE",
                    "ORIGINALSOURCEBASE", "GATHERINGEVENT", "DETERMINATIONEVENT",
                    "DERIVATIONEVENT", "SPECIMENOROBSERVATIONBASE_DERIVATIONEVENT",
                    "HIBERNATE_SEQUENCES",
             });
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//                File file = new File("./ABCDPart1Dataset.xml");
//        FileOutputStream fos;
//        try {
//            fos = new FileOutputStream(file);
//            printDataSet(fos);
//            fos.close();
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

		assertEquals("Number of TaxonNames is incorrect", 2, nameService.count(TaxonNameBase.class));
		assertEquals("Number of specimen is incorrect", 10, occurrenceService.count(DerivedUnit.class));

    }
}
