// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.excel.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.events.LoggingIoObserver;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @created 24.05.2013
 */
public class ExcelStreamImportTest extends CdmTransactionalIntegrationTest{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExcelStreamImportTest.class);

	@SpringBeanByName
	CdmApplicationAwareDefaultImport<?> defaultImport;

	@SpringBeanByType
	INameService nameService;

	@SpringBeanByType
	ITaxonService taxonService;

	@SpringBeanByType
	ITermService termService;

	@SpringBeanByType
	IClassificationService classificationService;

	@SpringBeanByType
	IDescriptionService descriptionService;

	private IImportConfigurator configurator;
	private IImportConfigurator uuidConfigurator;

	@Before
	public void setUp() throws URISyntaxException {
		//TODO create own test file
		String inputFile = "/eu/etaxonomy/cdm/io/excel/stream/ExcelStreamImport-TestInput.xls";
		URL url = this.getClass().getResource(inputFile);
	 	assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
		configurator = ExcelStreamImportConfigurator.NewInstance(url.toURI(), null, NomenclaturalCode.ICNAFP, null);
		assertNotNull("Configurator could not be created", configurator);
		configurator.addObserver(new LoggingIoObserver());

		inputFile = "/eu/etaxonomy/cdm/io/excel/taxa/NormalExplicitImportTest.testUuid-input.xls";
		url = this.getClass().getResource(inputFile);
	 	assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
		uuidConfigurator = ExcelStreamImportConfigurator.NewInstance(url.toURI(), null, NomenclaturalCode.ICNAFP, null);
		assertNotNull("Configurator could not be created", configurator);

	}

	@Test
	public void testInit() {
		assertNotNull("normalExplicitImport should not be null", defaultImport);
		assertNotNull("nameService should not be null", nameService);
	}

	@Test
	@DataSet
	public void testDoInvoke() {
//		printDataSet(System.out, new String[]{"ANNOTATION"});
		boolean result = defaultImport.invoke(configurator).isSuccess();
		assertTrue("Return value for import.invoke should be true", result);
		commitAndStartNewTransaction(new String[]{"TAXONNAMEBASE", "ANNOTATION"});
		assertEquals("Number of TaxonNames should be 10", 10 /*TODO */, nameService.count(null));
		List<Classification> treeList = classificationService.list(null, null,null,null,null);
		assertEquals("Number of classifications should be 1", 1, treeList.size());
		Classification tree = treeList.get(0);
		List<TaxonNode> rootNodes = tree.getChildNodes();
		assertEquals("Number of root nodes should be 1", 1, rootNodes.size());
		TaxonNode rootNode = rootNodes.iterator().next();

//		printDataSet(System.out, new String[]{"ANNOTATION"});
		Set<Annotation> annotationSet = rootNode.getTaxon().getAnnotations();
		assertTrue("Annotation is set for first Taxon", !annotationSet.isEmpty());

		assertEquals("Root taxon name should be Metazoa", "Metazoa", rootNode.getTaxon().getName().getTitleCache());
		TaxonNode secondTaxon = rootNode.getChildNodes().iterator().next();

		Set<Annotation> secondAnnotation = secondTaxon.getTaxon().getAnnotations();
		assertTrue("Annotation is empty for second Taxon", secondAnnotation.isEmpty());

		Set<TaxonDescription> taxonSet = secondTaxon.getTaxon().getDescriptions();
		assertTrue("Description is not Empty", !taxonSet.isEmpty());
		assertEquals("Number of Distributions should be 7", 7, descriptionService.listDescriptionElements(null, null, Distribution.class, null, null, null).size());
//		assertEquals("Number of Vernecular Names should be 7", 6, descriptionService.listDescriptionElements(null, null, CommonTaxonName.class, null, null, null).size());
		List<CommonTaxonName> list = descriptionService.listDescriptionElements(null, null, CommonTaxonName.class, null, null, null);
		for(DescriptionElementBase db : list){
			System.out.println(db.toString());
		}
		//TODO: write test for Refences Sources
		Set<IdentifiableSource> sourcesSet = secondTaxon.getTaxon().getSources();
		assertTrue("Sources are set for second Taxon", !sourcesSet.isEmpty());
		for(IdentifiableSource s : sourcesSet){
			System.out.println("ID: "+ s.getId());
			System.out.println("External ID: " +s.getIdInSource());
			System.out.println("Namespace: " + s.getIdNamespace());
			System.out.println("Reference: " + s.getCitation().toString());
		}

		//		assertEquals("Arthropoda node taxon name should be Arthropoda", "Arthropoda", arthropodaNode.getTaxon().getName().getTitleCache());
//		TaxonNode insectaNode = arthropodaNode.getChildNodes().iterator().next();
//		TaxonNode lepidopteraNode = insectaNode.getChildNodes().iterator().next();
//		TaxonNode noctuidaeNode = lepidopteraNode.getChildNodes().iterator().next();
//		TaxonNode noctuaNode = noctuidaeNode.getChildNodes().iterator().next();
//		assertEquals("Number of child nodes of noctuca should be 2", 2, noctuaNode.getChildNodes().size());
//
//		Iterator<TaxonNode> it = noctuaNode.getChildNodes().iterator();
//		TaxonNode childNode1 = it.next();
//		TaxonNode childNode2 = it.next();
//
//		TaxonNode noctuaPronubaNode;
//		if (childNode1.getTaxon().getName().getTitleCache().startsWith("Noctua pronuba")){
//			noctuaPronubaNode = childNode1;
//		}else{
//			noctuaPronubaNode = childNode2;
//		}
//
//		assertEquals("Noctua pronuba taxon name should be ", "Noctua pronuba", noctuaPronubaNode.getTaxon().getName().getTitleCache());
//		Taxon noctuaPronubaTaxon = noctuaPronubaNode.getTaxon();
//		Set<Synonym> synonyms = noctuaPronubaTaxon.getSynonyms();
//		assertEquals("Number of synonyms should be 1", 3 /*TODO*/, synonyms.size());
//		Synonym synonym = synonyms.iterator().next();
//		assertEquals("Synonym name should be ", "Noctua atlantica", (CdmBase.deproxy(synonym.getName(), NonViralName.class )).getNameCache());
//		Set<TaxonDescription> descriptions = noctuaPronubaTaxon.getDescriptions();
//		Assert.assertEquals("Number of descriptions should be 1", 1, descriptions.size());
//		TaxonDescription taxonDescription = descriptions.iterator().next();
//		Set<DescriptionElementBase> elements = taxonDescription.getElements();
//		List<CommonTaxonName> commonNames = new ArrayList<CommonTaxonName>();
//		for (DescriptionElementBase element : elements){
//			if (element.isInstanceOf(CommonTaxonName.class)){
//				commonNames.add((CommonTaxonName)element);
//			}
//		}
//		Assert.assertEquals("Number of common names should be 2", 2, commonNames.size());
//		Set<String> commonNameStrings = new HashSet<String>();
//		commonNameStrings.add(commonNames.get(0).getName());
//		commonNameStrings.add(commonNames.get(1).getName());
//		Assert.assertTrue("Common names must include Yellow Underwing", commonNameStrings.contains("Large Sunshine Underwing"));
//		Assert.assertTrue("Common names must include Yellow Underwing", commonNameStrings.contains("Yellow Underwing"));
	}

//	@Test
//	@DataSet(value="NormalExplicitImportTest.testUuid.xml")
//	public void testUUID() throws URISyntaxException{
//		UUID taxonUuid = UUID.fromString("aafce7fe-0c5f-42ed-814b-4c7c2c715660");
//		UUID synonymUuid = UUID.fromString("fc4a995b-37a9-4984-afe6-e352c6c04d92");
//
//
//		//test data set
//		assertEquals("Number of taxon bases should be 2", 2, taxonService.count(null));
//		Taxon taxon = (Taxon)taxonService.find(taxonUuid);
//		assertNotNull("Taxon with given uuid should exist", taxon);
//		assertEquals("Taxon should have no description", 0, taxon.getDescriptions().size());
//		Synonym synonym = (Synonym)taxonService.find(synonymUuid);
//		assertNotNull("Synonym with given uuid should exist", synonym);
//		assertEquals("Synonym should have 1 accepted taxon", 1, synonym.getAcceptedTaxa().size());
//
//		//import
//		boolean result = defaultImport.invoke(uuidConfigurator);
//		//test result
//		assertTrue("Return value for import.invoke should be true", result);
//		assertEquals("Number of taxon names should be 2", 2, nameService.count(null));
//		assertEquals("Number of taxa should be 2", 2, taxonService.count(null));
//		taxon = (Taxon)taxonService.find(taxonUuid);
//		assertEquals("Taxon should have 1 description", 1, taxon.getDescriptions().size());
//		TaxonDescription description = taxon.getDescriptions().iterator().next();
//		assertEquals("Number of description elements should be 2", 2, description.getElements().size());
//
//		String expectedText = "Description for the first taxon";
//		TextData textData = getTextElement(description, expectedText);
//		assertNotNull("The element should exists", textData);
//		Feature feature = textData.getFeature();
//		assertEquals("Unexpected feature", Feature.DESCRIPTION(), feature);
//		assertEquals("There should be exactly 1 language", 1,textData.getMultilanguageText().size());
//		Language language = textData.getMultilanguageText().keySet().iterator().next();
//		assertEquals("Language should be German", Language.GERMAN(), language);
//		String text = textData.getText(language);
//		assertEquals("Unexpected description text", expectedText, text);
//		assertEquals("Number of source elements should be 1", 1, textData.getSources().size());
//		DescriptionElementSource source = textData.getSources().iterator().next();
//		Reference ref = source.getCitation();
//		assertNotNull("Citation should not be null", ref);
//		assertNotNull("Authorship should not be null", ref.getAuthorship());
//		assertEquals("Source author should be 'Meyer et. al.'", "Meyer et. al.",ref.getAuthorship().getTitleCache());
//		assertEquals("Publication title should be 'My first book'", "My first book", ref.getTitle());
//		assertEquals("Publication year should be '1987'", "1987", ref.getYear());
//		TaxonNameBase nameUsedInSource = source.getNameUsedInSource();
//		assertNotNull("Name used in source should not be null", nameUsedInSource);
//		assertEquals("Name used in source title should be ", "Abies", nameUsedInSource.getTitleCache());
//
//
//		//synonym
//		expectedText = "A synonym description";
//		textData = getTextElement(description, expectedText);
//		assertNotNull("The element should exists", textData);
//		feature = textData.getFeature();
//		assertEquals("Unexpected feature", Feature.DESCRIPTION(), feature);
//		assertEquals("There should be exactly 1 language", 1,textData.getMultilanguageText().size());
//		language = textData.getMultilanguageText().keySet().iterator().next();
//		assertEquals("Language should be Spanish", Language.SPANISH_CASTILIAN(), language);
//		text = textData.getText(language);
//		assertEquals("Unexpected description text", expectedText, text);
//		assertEquals("Number of source elements should be 1", 1, textData.getSources().size());
//		source = textData.getSources().iterator().next();
//		ref = source.getCitation();
//		assertNotNull("Citation should not be null", ref);
//		assertNotNull("Authorship should not be null", ref.getAuthorship());
//		assertEquals("Source author should be 'Theys, A.'", "Theys, A.",ref.getAuthorship().getTitleCache());
//		assertEquals("Publication title should be 'The ultimate book'", "The ultimate book", ref.getTitle());
//		assertEquals("Publication year should be '2011'", "2011", ref.getYear());
//		nameUsedInSource = source.getNameUsedInSource();
//		assertNotNull("Name used in source should not be null", nameUsedInSource);
//		assertEquals("Name used in source title should be Pinus", "Pinus", nameUsedInSource.getTitleCache());
//
//	}

	/**
	 * Returns description element for record id 1
	 * @param description
	 * @return
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


    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
