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
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
 * @created 29.01.2009
 */
public class AbcdGgbnImportTest extends CdmTransactionalIntegrationTest {

	@SpringBeanByName
	private CdmApplicationAwareDefaultImport<?> defaultImport;

	@SpringBeanByType
	private IOccurrenceService occurrenceService;

	@Test
    @DataSet( value="../../../BlankDataSet.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testImportGgbn() {
        String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/db6.xml";
        URL url = this.getClass().getResource(inputFile);
        assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);

        Abcd206ImportConfigurator importConfigurator = null;
        try {
            importConfigurator = Abcd206ImportConfigurator.NewInstance(url.toURI(), null,false);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Assert.fail();
        }
        assertNotNull("Configurator could not be created", importConfigurator);

        boolean result = defaultImport.invoke(importConfigurator);
        assertTrue("Return value for import.invoke should be true", result);
        assertEquals("Number of derived units is incorrect", 2, occurrenceService.count(DerivedUnit.class));
        assertEquals("Number of dna samples is incorrect", 1, occurrenceService.count(DnaSample.class));
        DnaSample dnaSample = occurrenceService.list(DnaSample.class, null, null, null, null).get(0);
        assertEquals("Wrong derivation type!", DerivationEventType.DNA_EXTRACTION(), dnaSample.getDerivedFrom().getType());

        assertEquals("Wrong number of originals", 1, dnaSample.getDerivedFrom().getOriginals().size());
	}

	@Test
	@DataSet( value="AbcdGgbnImportTest.testAttachDnaSampleToDerivedUnit.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
	public void testAttachDnaSampleToDerivedUnit(){
	    UUID derivedUnit1Uuid = UUID.fromString("eb40cb0f-efb2-4985-819e-a9168f6d61fe");

//        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//        derivedUnit.setAccessionNumber("B 10 0066577");
//        derivedUnit.setTitleCache("testUnit1", true);
//
//        derivedUnit.setUuid(derivedUnit1Uuid );
//
//        occurrenceService.save(derivedUnit);
//
//        commitAndStartNewTransaction(null);
//
//        setComplete();
//        endTransaction();
//
//
//        try {
//            writeDbUnitDataSetFile(new String[] {
//                    "SpecimenOrObservationBase",
//            }, "testAttachDnaSampleToDerivedUnit");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }


	    String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/db6.xml";
	    URL url = this.getClass().getResource(inputFile);
	    assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);

	    Abcd206ImportConfigurator importConfigurator = null;
	    try {
	        importConfigurator = Abcd206ImportConfigurator.NewInstance(url.toURI(), null,false);
	    } catch (URISyntaxException e) {
	        e.printStackTrace();
	        Assert.fail();
	    }
	    assertNotNull("Configurator could not be created", importConfigurator);

	    boolean result = defaultImport.invoke(importConfigurator);
	    assertTrue("Return value for import.invoke should be true", result);
	    assertEquals("Number of derived units is incorrect", 2, occurrenceService.count(DerivedUnit.class));
	    List<DerivedUnit> derivedUnits = occurrenceService.list(DerivedUnit.class, null, null, null, null);
	    assertEquals("Number of derived units is incorrect", 2, derivedUnits.size());

	    DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(derivedUnit1Uuid);
	    assertTrue(derivedUnits.contains(derivedUnit));

	    assertEquals("Number of dna samples is incorrect", 1, occurrenceService.count(DnaSample.class));
	    DnaSample dnaSample = occurrenceService.list(DnaSample.class, null, null, null, null).get(0);
	    assertEquals("Wrong derivation type!", DerivationEventType.DNA_EXTRACTION(), dnaSample.getDerivedFrom().getType());

	    assertEquals("Wrong number of originals", 1, dnaSample.getDerivedFrom().getOriginals().size());

	}

	@Test
	@DataSet( value="AbcdGgbnImportTest.testNoAttachDnaSampleToDerivedUnit.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
	public void testNoAttachDnaSampleToDerivedUnit(){
	    UUID derivedUnit1Uuid = UUID.fromString("eb40cb0f-efb2-4985-819e-a9168f6d61fe");

//        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//        derivedUnit.setAccessionNumber("B 10 0066577");
//        derivedUnit.setTitleCache("testUnit1", true);
//
//        derivedUnit.setUuid(derivedUnit1Uuid );
//
//        occurrenceService.save(derivedUnit);
//
//        commitAndStartNewTransaction(null);
//
//        setComplete();
//        endTransaction();
//
//
//        try {
//            writeDbUnitDataSetFile(new String[] {
//                    "SpecimenOrObservationBase",
//            }, "testAttachDnaSampleToDerivedUnit");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }


	    String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/db6.xml";
	    URL url = this.getClass().getResource(inputFile);
	    assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);

	    Abcd206ImportConfigurator importConfigurator = null;
	    try {
	        importConfigurator = Abcd206ImportConfigurator.NewInstance(url.toURI(), null,false);
	    } catch (URISyntaxException e) {
	        e.printStackTrace();
	        Assert.fail();
	    }
	    assertNotNull("Configurator could not be created", importConfigurator);

	    assertEquals("Number of derived units is incorrect", 1, occurrenceService.count(DerivedUnit.class));
	    boolean result = defaultImport.invoke(importConfigurator);
	    assertTrue("Return value for import.invoke should be true", result);
	    assertEquals("Number of derived units is incorrect", 3, occurrenceService.count(DerivedUnit.class));
	    List<DerivedUnit> derivedUnits = occurrenceService.list(DerivedUnit.class, null, null, null, null);
	    assertEquals("Number of derived units is incorrect", 3, derivedUnits.size());

	    DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(derivedUnit1Uuid);
	    assertTrue(derivedUnits.contains(derivedUnit));

	    assertEquals("Number of dna samples is incorrect", 1, occurrenceService.count(DnaSample.class));
	    DnaSample dnaSample = occurrenceService.list(DnaSample.class, null, null, null, null).get(0);
	    assertEquals("Wrong derivation type!", DerivationEventType.DNA_EXTRACTION(), dnaSample.getDerivedFrom().getType());

	    assertEquals("Wrong number of originals", 1, dnaSample.getDerivedFrom().getOriginals().size());

	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {
        UUID derivedUnit1Uuid = UUID.fromString("eb40cb0f-efb2-4985-819e-a9168f6d61fe");

        DerivedUnit derivedUnit = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
        derivedUnit.setAccessionNumber("B 10 0066577");
        derivedUnit.setTitleCache("testUnit1", true);

        derivedUnit.setUuid(derivedUnit1Uuid );

        occurrenceService.save(derivedUnit);

        commitAndStartNewTransaction(null);

        setComplete();
        endTransaction();


        try {
            writeDbUnitDataSetFile(new String[] {
                    "SpecimenOrObservationBase",
            }, "testAttachDnaSampleToDerivedUnit");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
