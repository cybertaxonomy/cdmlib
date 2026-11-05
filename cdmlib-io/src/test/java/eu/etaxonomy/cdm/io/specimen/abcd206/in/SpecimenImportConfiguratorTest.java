/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import eu.etaxonomy.cdm.api.filter.MatchMode;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmIoTransactionalSpringIntegrationTest;

/**
 * @author a.mueller
 * @since 29.01.2009
 */
@Disabled  //as long as biocase service does not work
public class SpecimenImportConfiguratorTest extends CdmIoTransactionalSpringIntegrationTest {

	@Autowired
    @Qualifier("defaultImport")
    private CdmApplicationAwareDefaultImport<?> defaultImport;

	@Autowired
	private INameService nameService;

	@Autowired
	private ITaxonService taxonService;

	@Autowired
	private IOccurrenceService occurrenceService;

	@Autowired
	private ITermService termService;

	@Autowired
	private ICommonService commonService;

	@Autowired
	private ITaxonNodeService taxonNodeService;

	@Autowired
	private IAgentService agentService;

	@Autowired
	private IReferenceService referenceService;

	@Autowired
	private IClassificationService classificationService;

	private IImportConfigurator configurator;
	private IImportConfigurator configurator2;

	@BeforeEach
	public void setUp() {
		String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/AbcdImportTestCalvumPart1.xml";
        URL url = this.getClass().getResource(inputFile);
        assertNotNull(url, "URL for the test file '" + inputFile + "' does not exist");
        configurator = Abcd206ImportConfigurator.NewInstance(URI.create(url.toString()), null, false);
        assertNotNull(configurator, "Configurator could not be created");

        //test2
        String inputFile2 = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/Campanula_ABCD_import_3_taxa_11_units.xml";
		URL url2 = this.getClass().getResource(inputFile2);
		assertNotNull(url2, "URL for the test file '" + inputFile2 + "' does not exist");
		try {
			configurator2 = Abcd206ImportConfigurator.NewInstance(new URI(url2), null, false);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail();
		}
		assertNotNull(configurator2, "Configurator2 could not be created");
	}

	@Test
	public void testInit() {
	    System.out.println("TEST INIT");
		assertNotNull(defaultImport, "import instance should not be null");
		assertNotNull(nameService, "nameService should not be null");
		assertNotNull(occurrenceService, "occurence service should not be null");
		assertNotNull(termService, "term service should not be null");
		assertNotNull(commonService, "common service should not be null");
		assertNotNull(taxonNodeService, "taxon node service should not be null");
		assertNotNull(agentService, "agent service should not be null");
		assertNotNull(referenceService, "reference service should not be null");
	}

	@Test
	@CdmDataSet(cleanSweep = true, value = "/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
	public void testDoInvoke() {
        boolean result = defaultImport.invoke(configurator).isSuccess();
        assertTrue(result, "Return value for import.invoke should be true");
        assertEquals(2, nameService.count(TaxonName.class), "Number of TaxonNames is incorrect");
        /*
         * Classification
         * - Cichorium
         *   - Cichorium calvum
         */
        assertEquals(3, taxonNodeService.count(TaxonNode.class), "Number of TaxonNodes is incorrect");
        assertEquals(10, occurrenceService.count(DerivedUnit.class), "Number of specimen and observation is incorrect");
        //Asch. + Mitarbeiter der Floristischen Kartierung Deutschlands
        assertEquals(2, agentService.count(Person.class), "Number of persons is incorrect");
        //BfN
        assertEquals(1, agentService.count(Institution.class), "Number of institutions is incorrect");
    }

	@Test
	@CdmDataSet(cleanSweep = true, value = "SpecimenImportConfiguratorTest.doInvoke2.xml")
	public void testDoInvoke2() {
		boolean result = defaultImport.invoke(configurator2).isSuccess();
		assertTrue(result, "Return value for import.invoke should be true");

		assertEquals(4, nameService.count(TaxonName.class), "Number of TaxonNames is incorrect");
		List<TaxonName> listNames = nameService.list(TaxonName.class, 100, 0, null, null);

		for (TaxonName name: listNames){
		    System.out.println(name.getTitleCache());
		}
		/*
		 * 5 taxon nodes:
		 *
         * Classification
         * - Campanula
         *   - Campanula patula
         *   - Campanula tridentata
         *   - Campanula lactiflora
         */
        assertEquals(5, taxonNodeService.count(TaxonNode.class), "Number of TaxonNodes is incorrect");
		assertEquals(11, occurrenceService.count(DerivedUnit.class), "Number of derived units is incorrect");
		assertEquals(11, occurrenceService.count(FieldUnit.class), "Number of field units is incorrect");
		assertEquals(4, agentService.count(Person.class), "Number of gathering agents is incorrect");
		//BGBM
		assertEquals(1, agentService.count(Institution.class), "Number of institutions is incorrect");
		assertEquals(1, referenceService.count(Reference.class), "Number of references is incorrect");
	}

	@Test
    @CdmDataSet(cleanSweep = true, value = "/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public void testImportSubspecies() {

	    String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/camapanula_abietina_subspecies.xml";
        URL url = this.getClass().getResource(inputFile);
        assertNotNull(url, "URL for the test file '" + inputFile + "' does not exist");

        Abcd206ImportConfigurator importConfigurator = null;
        importConfigurator = Abcd206ImportConfigurator.NewInstance(URI.create(url.toString()), null, false);

        boolean result = defaultImport.invoke(importConfigurator).isSuccess();
        assertTrue(result, "Return value for import.invoke should be true");
        assertEquals(3, nameService.count(TaxonName.class), "Number of TaxonNames is incorrect");
        /*
         * Classification
         * - Campanula
         *   - Campanula patula
         *      - Campanula patula subsp. abietina
         */
        assertEquals(4, taxonNodeService.count(TaxonNode.class), "Number of TaxonNodes is incorrect");
	}

	@Test
	@CdmDataSet(cleanSweep = true, value = "/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
	public void testImportVariety() {
	    String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/Campanula_variety.xml";
	    URL url = this.getClass().getResource(inputFile);
	    assertNotNull(url, "URL for the test file '" + inputFile + "' does not exist");
	    Abcd206ImportConfigurator importConfigurator = Abcd206ImportConfigurator.NewInstance(URI.create(url.toString()), null, false);

	    boolean result = defaultImport.invoke(importConfigurator).isSuccess();
	    assertTrue(result, "Return value for import.invoke should be true");

	    /*
	     * Classification
	     *  - Campanula
	     *   - Campanula versicolor
	     *   - Campanula versicolor var. tomentella Hal.
	     */
	    assertEquals(4, taxonNodeService.count(TaxonNode.class));
	    assertEquals(3, nameService.count(TaxonName.class));
	    assertEquals(1, occurrenceService.count(DerivedUnit.class));
	    boolean varietyFound = false;
	    for(TaxonName name:nameService.list(TaxonName.class, null, null, null, null)){
	        if(name.getRank().equals(Rank.VARIETY())){
	            varietyFound = true;
	        }
	    }
	    assertTrue(varietyFound, "Variety rank not set");
	}

	@Test
	@CdmDataSet(cleanSweep = true, value = "/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
	public void testMultipleIdentificationsPreferredFlag() {
	    String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/MultipleIdentificationsPreferredFlag.xml";
	    URL url = this.getClass().getResource(inputFile);
	    assertNotNull(url, "URL for the test file '" + inputFile + "' does not exist");
	    Abcd206ImportConfigurator importConfigurator = Abcd206ImportConfigurator.NewInstance(URI.create(url.toString()), null, false);
	    importConfigurator.setAddDeterminations(true);

	    boolean result = defaultImport.invoke(importConfigurator).isSuccess();
	    assertTrue(result, "Return value for import.invoke should be true");

	    String nonPreferredNameCache = "Campanula flagellaris";
	    String preferredNameCache = "Campanula tymphaea";
	    //Campanula, "Campanula tymphaea Hausskn.", "Campanula flagellaris Halácsy"
	    assertEquals(3, nameService.count(TaxonName.class));
	    /*
	     * Classification
	     *  - Campanula
	     *   - Campanula tymphaea Hausskn.
	     */
	    assertEquals(3, taxonNodeService.count(TaxonNode.class));
	    assertEquals(1, occurrenceService.count(DerivedUnit.class));
	    DerivedUnit derivedUnit = occurrenceService.list(DerivedUnit.class, null, null, null, null).get(0);
	    assertEquals(2, derivedUnit.getDeterminations().size());
	    for(DeterminationEvent determinationEvent:derivedUnit.getDeterminations()){
	        if(determinationEvent.getPreferredFlag()){
	            assertEquals(preferredNameCache,determinationEvent.getTaxonName().getNameCache());
	        }
	        else{
	            assertEquals(nonPreferredNameCache,determinationEvent.getTaxonName().getNameCache());
	        }
	    }
	}

	@Test
    @CdmDataSet(cleanSweep = true, value = "/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public void testImportForm() {
        String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/C_drabifolia_major.xml";
        URL url = this.getClass().getResource(inputFile);
        assertNotNull(url, "URL for the test file '" + inputFile + "' does not exist");
        Abcd206ImportConfigurator importConfigurator = Abcd206ImportConfigurator.NewInstance(URI.create(url.toString()), null, false);

        boolean result = defaultImport.invoke(importConfigurator).isSuccess();
        assertTrue(result, "Return value for import.invoke should be true");
    }

	@Test
	@CdmDataSet(cleanSweep = true, value = "/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
	public void testMapUnitIDAsBarcode() {
	    String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/Campanula_ABCD_import_3_taxa_11_units.xml";
	    URL url = this.getClass().getResource(inputFile);
	    assertNotNull(url, "URL for the test file '" + inputFile + "' does not exist");

	    Abcd206ImportConfigurator importConfigurator = Abcd206ImportConfigurator.NewInstance(URI.create(url.toString()), null, false);

	    importConfigurator.setMapUnitIdToBarcode(true);
	    boolean result = defaultImport.invoke(importConfigurator).isSuccess();
	    assertTrue(result, "Return value for import.invoke should be true");
	    List<DerivedUnit> list = occurrenceService.list(DerivedUnit.class, null, null, null, null);
	    for (DerivedUnit derivedUnit : list) {
            assertTrue(derivedUnit.getBarcode()!=null);
        }
	}

	@Test
	@CdmDataSet(cleanSweep = true, value = "/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
	public void testMapUnitIDAsAccessionNumber() {
	    String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/Campanula_ABCD_import_3_taxa_11_units.xml";
	    URL url = this.getClass().getResource(inputFile);
	    assertNotNull(url, "URL for the test file '" + inputFile + "' does not exist");
	    Abcd206ImportConfigurator importConfigurator = Abcd206ImportConfigurator.NewInstance(URI.create(url.toString()), null, false);

	    importConfigurator.setMapUnitIdToAccessionNumber(true);
	    boolean result = defaultImport.invoke(importConfigurator).isSuccess();
	    assertTrue(result, "Return value for import.invoke should be true");
	    List<DerivedUnit> list = occurrenceService.list(DerivedUnit.class, null, null, null, null);
	    for (DerivedUnit derivedUnit : list) {
	        assertTrue(derivedUnit.getAccessionNumber()!=null);
	    }
	}

	@Test
	@CdmDataSet(cleanSweep = true, value = "/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
	public void testMapUnitIDAsCatalogNumber() {
	    String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/Campanula_ABCD_import_3_taxa_11_units.xml";
	    URL url = this.getClass().getResource(inputFile);
	    assertNotNull(url, "URL for the test file '" + inputFile + "' does not exist");
	    Abcd206ImportConfigurator importConfigurator = Abcd206ImportConfigurator.NewInstance(URI.create(url.toString()), null, false);
	    importConfigurator.setMapUnitIdToCatalogNumber(true);

	    boolean result = defaultImport.invoke(importConfigurator).isSuccess();
	    assertTrue(result, "Return value for import.invoke should be true");
	    List<DerivedUnit> list = occurrenceService.list(DerivedUnit.class, null, null, null, null);
	    for (DerivedUnit derivedUnit : list) {
	        assertTrue(derivedUnit.getCatalogNumber()!=null);
	    }
	}

	@Test
    @CdmDataSet(cleanSweep = true, value = "AbcdGgbnImportTest.testAttachDnaSampleToDerivedUnit.xml")
    public void testIgnoreExistingSpecimens(){
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

        String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/Campanula_B_10_0066577.xml";
        URL url = this.getClass().getResource(inputFile);
        assertNotNull(url, "URL for the test file '" + inputFile + "' does not exist");
        Abcd206ImportConfigurator importConfigurator = Abcd206ImportConfigurator.NewInstance(URI.create(url.toString()), null, false);
        importConfigurator.setDnaSoure(null);
        importConfigurator.setIgnoreImportOfExistingSpecimen(true);

        boolean result = defaultImport.invoke(importConfigurator).isSuccess();
        assertTrue(result, "Return value for import.invoke should be true");
        assertEquals(1, occurrenceService.count(DerivedUnit.class), "Number of derived units is incorrect");
        List<DerivedUnit> derivedUnits = occurrenceService.list(DerivedUnit.class, null, null, null, null);
        assertEquals(1, derivedUnits.size(), "Number of derived units is incorrect");

        DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(derivedUnit1Uuid);
        assertTrue(derivedUnits.contains(derivedUnit));
    }

	@Test
    @CdmDataSet(cleanSweep = true, value = "AbcdGgbnImportTest.testAttachDnaSampleToDerivedUnit.xml")
	public void testIgnoreExistingSpecimensWithDna(){
        UUID derivedUnit1Uuid = UUID.fromString("eb40cb0f-efb2-4985-819e-a9168f6d61fe");

        String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/Campanula_B_10_0066577.xml";
        URL url = this.getClass().getResource(inputFile);
        assertNotNull(url, "URL for the test file '" + inputFile + "' does not exist");
        Abcd206ImportConfigurator importConfigurator = Abcd206ImportConfigurator.NewInstance(URI.create(url.toString()), null, false);
        importConfigurator.setDnaSoure(URI.create("https://ww3.bgbm.org/biocase/pywrapper.cgi?dsa=DNA_Bank"));
        importConfigurator.setIgnoreImportOfExistingSpecimen(true);

        boolean result = defaultImport.invoke(importConfigurator).isSuccess();
        assertTrue(result, "Return value for import.invoke should be true");
        assertEquals(2, occurrenceService.count(DerivedUnit.class), "Number of derived units is incorrect");
        List<DerivedUnit> derivedUnits = occurrenceService.list(DerivedUnit.class, null, null, null, null);
        assertEquals(2, derivedUnits.size(), "Number of derived units is incorrect");

        DerivedUnit derivedUnit = (DerivedUnit) occurrenceService.load(derivedUnit1Uuid);
        assertTrue(derivedUnits.contains(derivedUnit));
    }

	/**
	 * Test imports one unit with an already existing taxon and one with a new taxon.
	 * The new taxon should be added to a newly created default classification.
	 */
	@Test
    @CdmDataSet(cleanSweep = true, value = "SpecimenImportConfiguratorTest.testIgnoreAuthorship.xml")
	public void testImportNewTaxaToDefaultClassification(){
	    UUID taxonUUID = UUID.fromString("26f98a58-09ab-49a0-ab9f-7490757c86d2");
	    UUID classificationUUID = UUID.fromString("eee32748-5b89-4266-a99a-1edb3781d0eb");

	    String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/Campanula_B_10_0066577_two_units_almost_same.xml";
	    URL url = this.getClass().getResource(inputFile);
	    assertNotNull(url, "URL for the test file '" + inputFile + "' does not exist");
	    Abcd206ImportConfigurator importConfigurator = Abcd206ImportConfigurator.NewInstance(URI.create(url.toString()), null, false);
	    importConfigurator.setDnaSoure(null);

	    //test initial state
	    Taxon taxon = (Taxon) taxonService.load(taxonUUID);
	    assertNotNull(taxon);
	    Classification classification = classificationService.load(classificationUUID);
	    assertNotNull(classification);
	    assertEquals(1, taxon.getTaxonNodes().size());
	    TaxonNode taxonNode = taxon.getTaxonNodes().iterator().next();
	    assertEquals(classification, taxonNode.getClassification());

	    assertEquals(1, classificationService.count(Classification.class));
	    assertEquals(1, taxonService.count(Taxon.class));

	    importConfigurator.setClassificationUuid(classificationUUID);
//	    importConfigurator.setIgnoreAuthorship(true);
	    boolean result = defaultImport.invoke(importConfigurator).isSuccess();
	    assertTrue(result, "Return value for import.invoke should be true");

	    //re-load classification to avoid session conflicts
	    classification = classificationService.load(classificationUUID);

	    assertEquals(2, occurrenceService.count(DerivedUnit.class));
//	    assertEquals(2, taxonService.count(Taxon.class));
	    assertEquals(2, classificationService.count(Classification.class));

	    //get default classification
	    List<Classification> list = classificationService.list(Classification.class, null, null, null, null);
	    Classification defaultClassification = null;
	    for (Classification c : list) {
            if(c.getUuid()!=classificationUUID){
                defaultClassification = c;
            }
        }
	    assertEquals(1, classification.getAllNodes().size());
	    assertEquals(2, defaultClassification.getAllNodes().size());
	}

    /**
     * Test should NOT create new taxa of the same name but have different authors.
     */
	@Test
    @CdmDataSet(cleanSweep = true, value = "SpecimenImportConfiguratorTest.testIgnoreAuthorship.xml")
	public void testIgnoreAuthorship(){
        UUID taxonUUID = UUID.fromString("26f98a58-09ab-49a0-ab9f-7490757c86d2");
        UUID classificationUUID = UUID.fromString("eee32748-5b89-4266-a99a-1edb3781d0eb");

//        Classification classification = Classification.NewInstance("Checklist");
//        classification.setUuid(classificationUUID);
//
//        Reference<?> secReference = ReferenceFactory.newGeneric();
//        Team team = Team.NewTitledInstance("different author", "different author");
//        secReference.setAuthorship(team);
//
//        INonViralName taxonName = TaxonNameFactory.NewNonViralInstance(Rank.VARIETY());
//        taxonName.setGenusOrUninomial("Campanula");
//        taxonName.setSpecificEpithet("versicolor");
//        taxonName.setInfraSpecificEpithet("tomentella");
//
//        Taxon taxon = Taxon.NewInstance(taxonName, secReference);
//        taxon.setUuid(taxonUUID);
//
//        classification.addChildTaxon(taxon, secReference, "");
//
//        taxonService.save(taxon);
//        nameService.save(taxonName);
//        referenceService.save(secReference);
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
//                    "SpecimenOrObservationBase_DerivationEvent",
//                    "DerivationEvent",
//                    "DescriptionElementBase",
//                    "DescriptionBase",
//                    "TaxonBase",
//                    "TypeDesignationBase",
//                    "TaxonName",
//                    "TaxonName_TypeDesignationBase",
//                    "HomotypicalGroup",
//                    "AgentBase",
//                    "AgentBase_AgentBase",
//                    "Reference",
//                    "TaxonNode",
//                    "Classification",
//                    "LanguageString"
//            }, "testIgnoreAuthorship");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/Campanula_B_10_0066577.xml";
	    URL url = this.getClass().getResource(inputFile);
	    assertNotNull(url, "URL for the test file '" + inputFile + "' does not exist");
	    Abcd206ImportConfigurator importConfigurator = Abcd206ImportConfigurator.NewInstance(URI.create(url.toString()), null, false);

	    //test initial state
	    Taxon taxon = (Taxon) taxonService.load(taxonUUID);
	    assertNotNull(taxon);
	    Classification classification = classificationService.load(classificationUUID);
	    assertNotNull(classification);
	    assertEquals(1, classificationService.count(Classification.class));
	    assertEquals(1, classification.getAllNodes().size());//taxon node
	    assertEquals(2, taxonNodeService.count(TaxonNode.class));//root node + Taxon node = 2 nodes

	    importConfigurator.setIgnoreAuthorship(true);
	    importConfigurator.setClassificationUuid(classificationUUID);
	    boolean result = defaultImport.invoke(importConfigurator).isSuccess();
	    assertTrue(result, "Return value for import.invoke should be true");

	    assertEquals(1, classificationService.count(Classification.class));
	    assertEquals(1, classification.getAllNodes().size());//taxon node
	    assertEquals(2, taxonNodeService.count(TaxonNode.class));//root node + Taxon node = 2 nodes
	}

    /**
     * Imports two DNA units belonging to the same taxon into an existing
     * classification. The taxon is not part of the classification so the
     * default classification will be created. The result should be:
     * existing classification (0 taxa), default classification (2 taxa [genus+species])
     */
    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml",
            "SpecimenImportConfiguratorTest.testImportTwoUnitsOfSameTaxonIntoExistingClassification.xml"
    })
	public void testImportTwoUnitsOfSameTaxonIntoExistingClassification(){

        UUID classificationUUID = UUID.fromString("18d22d00-5f70-4c8e-a1ed-dc45fae5b816");

        String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/Campanula_barbata.xml";
        URL url = this.getClass().getResource(inputFile);
        assertNotNull(url, "URL for the test file '" + inputFile + "' does not exist");
        Abcd206ImportConfigurator importConfigurator = Abcd206ImportConfigurator.NewInstance(URI.create(url.toString()), null, false);

        //test initial state
        Classification classification = classificationService.load(classificationUUID);
        assertNotNull(classification);
        assertEquals(0, classification.getAllNodes().size());
        assertEquals(1, classificationService.count(Classification.class));

        importConfigurator.setIgnoreAuthorship(true);
        importConfigurator.setClassificationUuid(classificationUUID);
        boolean result = defaultImport.invoke(importConfigurator).isSuccess();
        assertTrue(result, "Return value for import.invoke should be true");

        assertEquals(0, classification.getAllNodes().size());
        assertEquals(2, classificationService.count(Classification.class));

        Classification defaultClassification = null;
        for (Classification c : classificationService.list(Classification.class, null, null, null, null)) {
            if(!c.getUuid().equals(classificationUUID)){
                defaultClassification = c;
            }
        }
        assertNotNull(defaultClassification);
        Set<TaxonNode> allNodes = defaultClassification.getAllNodes();
        for (TaxonNode node:allNodes){
            System.out.println(node.getTaxon().getTitleCache());
        }

        assertEquals(3, defaultClassification.getAllNodes().size());
	}

    /**
     * Tests import of unit belonging to a taxon with a non-parsable name. Since
     * no rank can be deduced from the name there will be no taxon hierarchy
     * created but only the single taxon
     *
     * @throws ParseException
     */
    @Test
    @CdmDataSet(cleanSweep = true, value = "/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public void testImportNonParsableName() {
        String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/Campanula_americana.xml";
        URL url = this.getClass().getResource(inputFile);
        assertNotNull(url, "URL for the test file '" + inputFile + "' does not exist");
        Abcd206ImportConfigurator importConfigurator = Abcd206ImportConfigurator.NewInstance(URI.create(url.toString()), null, false);

        boolean result = defaultImport.invoke(importConfigurator).isSuccess();
        assertTrue(result, "Return value for import.invoke should be true");
        assertEquals(1, occurrenceService.count(DerivedUnit.class), "Number of derived units is incorrect");
        assertEquals(1, occurrenceService.count(DnaSample.class), "Number of dna samples is incorrect");
        assertEquals(1, occurrenceService.count(FieldUnit.class), "Number of field units is incorrect");
        List<TaxonNode> nodes = taxonNodeService.list(TaxonNode.class, null, null, null, null);
        occurrenceService.list(SpecimenOrObservationBase.class, null, null, null, null);
        /*
         * Default classification
         *   - Campanula ..g... americana --- hort. ttt ex Steud.
         */
        assertEquals(2, taxonNodeService.count(TaxonNode.class), "Number of taxon nodes is incorrect");
        assertEquals(1, taxonService.count(TaxonBase.class), "Number of taxa is incorrect");
        assertEquals(1, taxonService.findByTitleWithRestrictions(Taxon.class, "Campanula ..g... americana --- hort. ttt ex Steud.", MatchMode.ANYWHERE, null, null, null, null, null).getRecords().size());
    }

	@Test
	@Disabled
	@CdmDataSet(cleanSweep = true, value = "/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
	public void testSetUnitIDAsBarcode() {

	}

    @Override
//    @Test
    public void createTestDataSet() throws FileNotFoundException {
        UUID classificationUUID = UUID.fromString("18d22d00-5f70-4c8e-a1ed-dc45fae5b816");

        Classification classification = Classification.NewInstance("Checklist");
        classification.setUuid(classificationUUID);

        classificationService.save(classification);

        commitAndStartNewTransaction(null);

        setComplete();
        endTransaction();

        try {
            writeDbUnitDataSetFile(new String[] {
                    "SpecimenOrObservationBase",
                    "SpecimenOrObservationBase_DerivationEvent",
                    "DerivationEvent",
                    "DescriptionElementBase",
                    "DescriptionBase",
                    "TaxonBase",
                    "TypeDesignationBase",
                    "TaxonName",
                    "TaxonName_TypeDesignationBase",
                    "HomotypicalGroup",
                    "AgentBase",
                    "AgentBase_AgentBase",
                    "Reference",
                    "TaxonNode",
                    "Classification",
                    "LanguageString"
            }, "testImportTwoUnitsOfSameTaxonIntoExistingClassification", true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}