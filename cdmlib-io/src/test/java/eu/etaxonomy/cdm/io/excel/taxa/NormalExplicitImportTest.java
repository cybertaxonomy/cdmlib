/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.excel.taxa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmIoTransactionalSpringIntegrationTest;

/**
 * @author a.mueller
 * @since 26.08.2009
 */
public class NormalExplicitImportTest extends CdmIoTransactionalSpringIntegrationTest{

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	@Autowired
    @Qualifier("defaultImport")
    private CdmApplicationAwareDefaultImport<?> defaultImport;

	@Autowired
	private INameService nameService;

	@Autowired
	private ITaxonService taxonService;

	@Autowired
	private ITermService termService;

	@Autowired
	private IClassificationService classificationService;

	private IImportConfigurator configurator;
	private IImportConfigurator uuidConfigurator;
	private IImportConfigurator configuratorXslx;
	private IImportConfigurator configuratorTropicos;

	@BeforeEach
	public void setUp() throws URISyntaxException {

	    String inputFile = "/eu/etaxonomy/cdm/io/excel/taxa/NormalExplicitImportTest-input.xls";
		URL url = this.getClass().getResource(inputFile);
	 	assertNotNull(url, "URL for the test file '" + inputFile + "' does not exist");
		configurator = NormalExplicitImportConfigurator.NewInstance(URI.fromUrl(url), null, NomenclaturalCode.ICNAFP, null);
		assertNotNull(configurator, "Configurator could not be created");

		inputFile = "/eu/etaxonomy/cdm/io/excel/taxa/NormalExplicitImportTest.testUuid-input.xls";
		url = this.getClass().getResource(inputFile);
	 	assertNotNull(url, "URL for the test file '" + inputFile + "' does not exist");
		uuidConfigurator = NormalExplicitImportConfigurator.NewInstance(URI.fromUrl(url), null, NomenclaturalCode.ICNAFP, null);
		assertNotNull(uuidConfigurator, "Configurator could be created");

		String inputFileXslx = "/eu/etaxonomy/cdm/io/excel/taxa/NormalExplicitImportTest-input.xlsx";
		url = this.getClass().getResource(inputFileXslx);
	 	assertNotNull(url, "URL for the test file '" + inputFileXslx + "' does not exist");
		configuratorXslx = NormalExplicitImportConfigurator.NewInstance(URI.fromUrl(url), null, NomenclaturalCode.ICNAFP, null);
		assertNotNull(configuratorXslx, "Configurator could not be created");

		String inputFileTropicos = "/eu/etaxonomy/cdm/io/excel/taxa/ExcelTropicosImportExampleTest-input.xlsx";
        url = this.getClass().getResource(inputFileTropicos);
        assertNotNull(url, "URL for the test file '" + inputFileTropicos + "' does not exist");
        configuratorTropicos = NormalExplicitImportConfigurator.NewInstance(URI.fromUrl(url), null, NomenclaturalCode.ICNAFP, null);
        assertNotNull(configuratorTropicos, "Configurator could not be created");
	}

	@Test
	public void testInit() {
		assertNotNull(defaultImport, "normalExplicitImport should not be null");
		assertNotNull(nameService, "nameService should not be null");
	}

	@Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml",
            "NormalExplicitImportTest.xml"
    })
	public void testDoInvoke() {

		//printDataSet(System.out);
		boolean result = defaultImport.invoke(configurator).isSuccess();
		assertTrue(result, "Return value for import.invoke should be true");
		assertEquals(9, nameService.count(null), "Number of TaxonNames should be 9");
		List<Classification> treeList = classificationService.list(null, null,null,null,null);
		assertEquals(1, treeList.size(), "Number of classifications should be 1");
		Classification tree = treeList.get(0);
		List<TaxonNode> rootNodes = tree.getChildNodes();
		assertEquals(1, rootNodes.size(), "Number of root nodes should be 1");
		TaxonNode rootNode = rootNodes.iterator().next();
		assertEquals("Animalia", rootNode.getTaxon().getName().getTitleCache(), "Root taxon name should be Animalia");
		TaxonNode arthropodaNode = rootNode.getChildNodes().iterator().next();
		assertEquals("Arthropoda", arthropodaNode.getTaxon().getName().getTitleCache(), "Arthropoda node taxon name should be Arthropoda");
		TaxonNode insectaNode = arthropodaNode.getChildNodes().iterator().next();
		TaxonNode lepidopteraNode = insectaNode.getChildNodes().iterator().next();
		TaxonNode noctuidaeNode = lepidopteraNode.getChildNodes().iterator().next();
		TaxonNode noctuaNode = noctuidaeNode.getChildNodes().iterator().next();
		assertEquals(2, noctuaNode.getChildNodes().size(), "Number of child nodes of noctuca should be 2");

		Iterator<TaxonNode> it = noctuaNode.getChildNodes().iterator();
		TaxonNode childNode1 = it.next();
		TaxonNode childNode2 = it.next();

		TaxonNode noctuaPronubaNode;
		if (childNode1.getTaxon().getName().getTitleCache().startsWith("Noctua pronuba")){
			noctuaPronubaNode = childNode1;
		}else{
			noctuaPronubaNode = childNode2;
		}

		assertEquals("Noctua pronuba", noctuaPronubaNode.getTaxon().getName().getTitleCache(), "Noctua pronuba taxon name should be ");
		Taxon noctuaPronubaTaxon = noctuaPronubaNode.getTaxon();
		Set<Synonym> synonyms = noctuaPronubaTaxon.getSynonyms();
		assertEquals(1, synonyms.size(), "Number of synonyms should be 1");
		Synonym synonym = synonyms.iterator().next();
		assertEquals("Noctua atlantica", synonym.getName().getNameCache(), "Synonym name should be ");
		Set<TaxonDescription> descriptions = noctuaPronubaTaxon.getDescriptions();
		assertEquals(1, descriptions.size(), "Number of descriptions should be 1");
		TaxonDescription taxonDescription = descriptions.iterator().next();
		Set<DescriptionElementBase> elements = taxonDescription.getElements();
		List<CommonTaxonName> commonNames = new ArrayList<CommonTaxonName>();
		for (DescriptionElementBase element : elements){
			if (element.isInstanceOf(CommonTaxonName.class)){
				commonNames.add((CommonTaxonName)element);
			}
		}
		assertEquals(2, commonNames.size(), "Number of common names should be 2");
		Set<String> commonNameStrings = new HashSet<>();
		commonNameStrings.add(commonNames.get(0).getName());
		commonNameStrings.add(commonNames.get(1).getName());
		assertTrue(commonNameStrings.contains("Large Sunshine Underwing"), "Common names must include Yellow Underwing");
		assertTrue(commonNameStrings.contains("Yellow Underwing"), "Common names must include Yellow Underwing");
	}

	@Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml",
            "NormalExplicitImportTest.xml"
    })
	public void testDoInvokeXslx() {

		//printDataSet(System.out);
		boolean result = defaultImport.invoke(configuratorXslx).isSuccess();
		assertTrue(result, "Return value for import.invoke should be true");
		assertEquals(9, nameService.count(null), "Number of TaxonNames should be 9");
		List<Classification> treeList = classificationService.list(null, null,null,null,null);
		assertEquals(1, treeList.size(), "Number of classifications should be 1");
		Classification tree = treeList.get(0);
		List<TaxonNode> rootNodes = tree.getChildNodes();
		assertEquals(1, rootNodes.size(), "Number of root nodes should be 1");
		TaxonNode rootNode = rootNodes.iterator().next();
		assertEquals("Animalia", rootNode.getTaxon().getName().getTitleCache(), "Root taxon name should be Animalia");
		TaxonNode arthropodaNode = rootNode.getChildNodes().iterator().next();
		assertEquals("Arthropoda", arthropodaNode.getTaxon().getName().getTitleCache(), "Arthropoda node taxon name should be Arthropoda");
		TaxonNode insectaNode = arthropodaNode.getChildNodes().iterator().next();
		TaxonNode lepidopteraNode = insectaNode.getChildNodes().iterator().next();
		TaxonNode noctuidaeNode = lepidopteraNode.getChildNodes().iterator().next();
		TaxonNode noctuaNode = noctuidaeNode.getChildNodes().iterator().next();
		assertEquals(2, noctuaNode.getChildNodes().size(), "Number of child nodes of noctuca should be 2");

		Iterator<TaxonNode> it = noctuaNode.getChildNodes().iterator();
		TaxonNode childNode1 = it.next();
		TaxonNode childNode2 = it.next();

		TaxonNode noctuaPronubaNode;
		if (childNode1.getTaxon().getName().getTitleCache().startsWith("Noctua pronuba")){
			noctuaPronubaNode = childNode1;
		}else{
			noctuaPronubaNode = childNode2;
		}

		assertEquals("Noctua pronuba", noctuaPronubaNode.getTaxon().getName().getTitleCache(), "Noctua pronuba taxon name should be ");
		Taxon noctuaPronubaTaxon = noctuaPronubaNode.getTaxon();
		Set<Synonym> synonyms = noctuaPronubaTaxon.getSynonyms();
		assertEquals(1, synonyms.size(), "Number of synonyms should be 1");
		Synonym synonym = synonyms.iterator().next();
		assertEquals("Noctua atlantica", synonym.getName().getNameCache(), "Synonym name should be ");
		Set<TaxonDescription> descriptions = noctuaPronubaTaxon.getDescriptions();
		assertEquals(1, descriptions.size(), "Number of descriptions should be 1");
		TaxonDescription taxonDescription = descriptions.iterator().next();
		Set<DescriptionElementBase> elements = taxonDescription.getElements();
		List<CommonTaxonName> commonNames = new ArrayList<CommonTaxonName>();
		for (DescriptionElementBase element : elements){
			if (element.isInstanceOf(CommonTaxonName.class)){
				commonNames.add((CommonTaxonName)element);
			}
		}
		assertEquals(2, commonNames.size(), "Number of common names should be 2");
		Set<String> commonNameStrings = new HashSet<>();
		commonNameStrings.add(commonNames.get(0).getName());
		commonNameStrings.add(commonNames.get(1).getName());
		assertTrue(commonNameStrings.contains("Large Sunshine Underwing"), "Common names must include Yellow Underwing");
		assertTrue(commonNameStrings.contains("Yellow Underwing"), "Common names must include Yellow Underwing");
	}

	@Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml",
            "NormalExplicitImportTest.testUuid.xml"
    })
	@Disabled //temporarily ignore as it fails in jenkins (but not local) #10524, #10680
	public void testUUID(){
		UUID taxonUuid = UUID.fromString("aafce7fe-0c5f-42ed-814b-4c7c2c715660");
		UUID synonymUuid = UUID.fromString("fc4a995b-37a9-4984-afe6-e352c6c04d92");

		//test data set
		assertEquals(2, taxonService.count(null), "Number of taxon bases should be 2");
		Taxon taxon = (Taxon)taxonService.find(taxonUuid);
		assertNotNull(taxon, "Taxon with given uuid should exist");
		assertEquals(0, taxon.getDescriptions().size(), "Taxon should have no description");
		Synonym synonym = (Synonym)taxonService.find(synonymUuid);
		assertNotNull(synonym, "Synonym with given uuid should exist");
		assertNotNull(synonym.getAcceptedTaxon(), "Synonym should have 1 accepted taxon");

		//import
		boolean result = defaultImport.invoke(uuidConfigurator).isSuccess();
		//test result
		assertTrue(result, "Return value for import.invoke should be true");
		assertEquals(2, nameService.count(null), "Number of taxon names should be 2");
		assertEquals(2, taxonService.count(null), "Number of taxa should be 2");
		taxon = (Taxon)taxonService.find(taxonUuid);
		assertEquals(1, taxon.getDescriptions().size(), "Taxon should have 1 description");
		TaxonDescription description = taxon.getDescriptions().iterator().next();
		assertEquals(2, description.getElements().size(), "Number of description elements should be 2");

		String expectedText = "Description for the first taxon";
		TextData textData = getTextElement(description, expectedText);
		assertNotNull(textData, "The element should exists");
		Feature feature = textData.getFeature();
		assertEquals(Feature.DESCRIPTION(), feature, "Unexpected feature");
		assertEquals(1, textData.getMultilanguageText().size(), "There should be exactly 1 language");
		Language language = textData.getMultilanguageText().keySet().iterator().next();
		assertEquals(Language.GERMAN(), language, "Language should be German");
		String text = textData.getText(language);
		assertEquals(expectedText, text, "Unexpected description text");
		assertEquals(1, textData.getSources().size(), "Number of source elements should be 1");
		DescriptionElementSource source = textData.getSources().iterator().next();
		Reference ref = source.getCitation();
		assertNotNull(ref, "Citation should not be null");
		assertNotNull(ref.getAuthorship(), "Authorship should not be null");
		assertEquals("Meyer et. al.", ref.getAuthorship().getTitleCache(), "Source author should be 'Meyer et. al.'");
		assertEquals("My first book", ref.getTitle(), "Publication title should be 'My first book'");
		assertEquals("1987", ref.getYear(), "Publication year should be '1987'");
		TaxonName nameUsedInSource = source.getNameUsedInSource();
		assertNotNull(nameUsedInSource, "Name used in source should not be null");
		assertEquals("Abies", nameUsedInSource.getTitleCache(), "Name used in source title should be ");

		//synonym
		expectedText = "A synonym description";
		textData = getTextElement(description, expectedText);
		assertNotNull(textData, "The element should exists");
		feature = textData.getFeature();
		assertEquals(Feature.DESCRIPTION(), feature, "Unexpected feature");
		assertEquals(1, textData.getMultilanguageText().size(), "There should be exactly 1 language");
		language = textData.getMultilanguageText().keySet().iterator().next();
		assertEquals(Language.SPANISH_CASTILIAN(), language, "Language should be Spanish");
		text = textData.getText(language);
		assertEquals(expectedText, text, "Unexpected description text");
		assertEquals(1, textData.getSources().size(), "Number of source elements should be 1");
		source = textData.getSources().iterator().next();
		ref = source.getCitation();
		assertNotNull(ref, "Citation should not be null");
		assertNotNull(ref.getAuthorship(), "Authorship should not be null");
		assertEquals("Theys, A.", ref.getAuthorship().getTitleCache(), "Source author should be 'Theys, A.'");
		assertEquals("The ultimate book", ref.getTitle(), "Publication title should be 'The ultimate book'");
		assertEquals("2011", ref.getYear(), "Publication year should be '2011'");
		nameUsedInSource = source.getNameUsedInSource();
		assertNotNull(nameUsedInSource, "Name used in source should not be null");
		assertEquals("Pinus", nameUsedInSource.getTitleCache(), "Name used in source title should be Pinus");

	}

	/**
	 * Returns description element for record id 1
	 */
	private TextData getTextElement(TaxonDescription description, String descriptionText) {
		for (DescriptionElementBase element : description.getElements()){
			if (element.isInstanceOf(TextData.class)){
				TextData textData = CdmBase.deproxy(element, TextData.class);
				for (LanguageString ls :textData.getMultilanguageText().values()){
					if (ls.getText().equals(descriptionText)){
						return textData;
					}
				}
			}
		}
		return null;
	}

    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml",
            "NormalExplicitImportTest.xml"
    })
    public void testDoInvokeTropicos() {
        //printDataSet(System.out);
        boolean result = defaultImport.invoke(configuratorTropicos).isSuccess();
        assertTrue(result, "Return value for import.invoke should be true");
        int count = nameService.count(null);

        assertEquals(19, count, "Number of TaxonNames should be 19");
        int countTaxa = taxonService.count(Taxon.class);
        assertEquals(18, countTaxa, "Number of Taxa should be 18");
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}

}