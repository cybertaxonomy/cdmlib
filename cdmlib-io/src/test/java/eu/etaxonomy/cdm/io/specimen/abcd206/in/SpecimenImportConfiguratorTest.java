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
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
 * @created 29.01.2009
 */
public class SpecimenImportConfiguratorTest extends CdmTransactionalIntegrationTest {

	@SpringBeanByName
	private CdmApplicationAwareDefaultImport<?> defaultImport;

	@SpringBeanByType
	private INameService nameService;

	@SpringBeanByType
	private IOccurrenceService occurrenceService;

	@SpringBeanByType
	private ITermService termService;

	@SpringBeanByType
	private ICommonService commonService;

	@SpringBeanByType
	private ITaxonNodeService taxonNodeService;

	@SpringBeanByType
	private IAgentService agentService;

	@SpringBeanByType
	private IReferenceService referenceService;



	private IImportConfigurator configurator;
	private IImportConfigurator configurator2;

	@Before
	public void setUp() {
		String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/ABCDImportTestCalvumPart1.xml";
        URL url = this.getClass().getResource(inputFile);
        assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
        try {
            configurator = Abcd206ImportConfigurator.NewInstance(url.toURI(), null,false);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Assert.fail();
        }
        assertNotNull("Configurator2 could not be created", configurator);

        //test2
        String inputFile2 = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/Campanula_ABCD_import_3_taxa_11_units.xml";
		URL url2 = this.getClass().getResource(inputFile2);
		assertNotNull("URL for the test file '" + inputFile2 + "' does not exist", url2);
		try {
			configurator2 = Abcd206ImportConfigurator.NewInstance(url2.toURI(), null,false);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			Assert.fail();
		}
		assertNotNull("Configurator could not be created", configurator2);
	}

	@Test
	public void testInit() {
	    System.out.println("TEST INIT");
		assertNotNull("import instance should not be null", defaultImport);
		assertNotNull("nameService should not be null", nameService);
		assertNotNull("occurence service should not be null", occurrenceService);
		assertNotNull("term service should not be null", termService);
		assertNotNull("common service should not be null", commonService);
		assertNotNull("taxon node service should not be null", taxonNodeService);
		assertNotNull("agent service should not be null", agentService);
		assertNotNull("reference service should not be null", referenceService);
	}

	@Test
	@Ignore
    @DataSet( value="../../../BlankDataSet.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testDoInvoke() {
        boolean result = defaultImport.invoke(configurator);
        assertTrue("Return value for import.invoke should be true", result);
        assertEquals("Number of TaxonNames is incorrect", 2, nameService.count(TaxonNameBase.class));
        /*
         * Classification
         * - Cichorium
         *   - Cichorium calvum
         */
        assertEquals("Number of TaxonNodes is incorrect", 3, taxonNodeService.count(TaxonNode.class));
        assertEquals("Number of specimen and observation is incorrect", 10, occurrenceService.count(DerivedUnit.class));
        //Asch. + Mitarbeiter der Floristischen Kartierung Deutschlands
        assertEquals("Number of persons is incorrect", 2, agentService.count(Person.class));
        //BfN
        assertEquals("Number of institutions is incorrect", 1, agentService.count(Institution.class));
    }


	@Test
	@Ignore
	@DataSet(value="SpecimenImportConfiguratorTest.doInvoke2.xml",  loadStrategy=CleanSweepInsertLoadStrategy.class)
	public void testDoInvoke2() {
		boolean result = defaultImport.invoke(configurator2);
		assertTrue("Return value for import.invoke should be true", result);
		assertEquals("Number of TaxonNames is incorrect", 4, nameService.count(TaxonNameBase.class));
		/*
		 * 5 taxon nodes:
		 *
         * Classification
         * - Campanula
         *   - Campanula patula
         *   - Campanula tridentata
         *   - Campanula lactiflora
         */
        assertEquals("Number of TaxonNodes is incorrect", 5, taxonNodeService.count(TaxonNode.class));
		assertEquals("Number of derived units is incorrect", 11, occurrenceService.count(DerivedUnit.class));
		assertEquals("Number of field units is incorrect", 11, occurrenceService.count(FieldUnit.class));
		assertEquals("Number of gathering agents is incorrect", 4, agentService.count(Person.class));
		//BGBM
		assertEquals("Number of institutions is incorrect", 1, agentService.count(Institution.class));
		assertEquals("Number of references is incorrect", 1, referenceService.count(Reference.class));
	}

	@Test
	@Ignore
    @DataSet( value="../../../BlankDataSet.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
    public void testImportSubspecies() {
        String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/camapanula_abietina_subspecies.xml";
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
        assertEquals("Number of TaxonNames is incorrect", 3, nameService.count(TaxonNameBase.class));
        /*
         * Classification
         * - Campanula
         *   - Campanula patula
         *      - Campanula patula subsp. abietina
         */
        assertEquals("Number of TaxonNodes is incorrect", 4, taxonNodeService.count(TaxonNode.class));
	}

	@Test
	@Ignore
	@DataSet( value="../../../BlankDataSet.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
	public void testImportVariety() {
	    String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/Campanula_variety.xml";
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

	    /*
	     * Classification
	     *  - Campanula
	     *   - Campanula versicolor var. tomentella Hal.
	     */
	    assertEquals(3, taxonNodeService.count(TaxonNode.class));
	    assertEquals(2, nameService.count(TaxonNameBase.class));
	    assertEquals(1, occurrenceService.count(DerivedUnit.class));
	    boolean varietyFound = false;
	    for(TaxonNameBase<?, ?> name:nameService.list(TaxonNameBase.class, null, null, null, null)){
	        if(name.getRank().equals(Rank.VARIETY())){
	            varietyFound = true;
	        }
	    }
	    assertTrue("Variety rank not set", varietyFound);
	}

	@Test
	@Ignore
	@DataSet( value="../../../BlankDataSet.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
	public void testMultipleIdentificationsPreferredFlag() {
	    String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/MultipleIdentificationsPreferredFlag.xml";
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

	    String nonPreferredNameCache = "Campanula flagellaris";
	    String preferredNameCache = "Campanula tymphaea";
	    //Campanula, "Campanula tymphaea Hausskn.", "Campanula flagellaris Halácsy"
	    assertEquals(3, nameService.count(TaxonNameBase.class));
	    /*
	     * Classification
	     *  - Campanula
	     *   - Campanula tymphaea Hausskn.
	     */
	    assertEquals(3, taxonNodeService.count(TaxonNode.class));
	    assertEquals(1, occurrenceService.count(DerivedUnit.class));
	    DerivedUnit derivedUnit = occurrenceService.list(DerivedUnit.class, null, null, null, null).get(0);
	    assertEquals(2, derivedUnit.getDeterminations());
	    for(DeterminationEvent determinationEvent:derivedUnit.getDeterminations()){
	        if(determinationEvent.getPreferredFlag()){
	            assertEquals(preferredNameCache,((NonViralName<?>) determinationEvent.getTaxonName()).getNameCache());
	        }
	        else{
	            assertEquals(nonPreferredNameCache,((NonViralName<?>) determinationEvent.getTaxonName()).getNameCache());
	        }
	    }

	}

	@Test
	@Ignore
	@DataSet( value="../../../BlankDataSet.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
	public void testSetUnitIDAsBarcode() {

	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }
}
