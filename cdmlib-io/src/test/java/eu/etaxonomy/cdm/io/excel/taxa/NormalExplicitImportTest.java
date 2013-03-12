// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.excel.taxa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @created 26.08.2009
 * @version 1.0
 */
public class NormalExplicitImportTest extends CdmTransactionalIntegrationTest{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NormalExplicitImportTest.class);

	@SpringBeanByName
	CdmApplicationAwareDefaultImport defaultImport;

	@SpringBeanByType
	INameService nameService;
	
	@SpringBeanByType
	ITaxonService taxonService;
	
	@SpringBeanByType
	ITermService termService;
	
	@SpringBeanByType
	IClassificationService classificationService;

	private IImportConfigurator configurator;
	private IImportConfigurator uuidConfigurator;
	
	@Before
	public void setUp() throws URISyntaxException {
		String inputFile = "/eu/etaxonomy/cdm/io/excel/taxa/NormalExplicitImportTest-input.xls";
		URL url = this.getClass().getResource(inputFile);
	 	assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
		configurator = NormalExplicitImportConfigurator.NewInstance(url.toURI(), null, NomenclaturalCode.ICBN, null);
		assertNotNull("Configurator could not be created", configurator);
		
		inputFile = "/eu/etaxonomy/cdm/io/excel/taxa/NormalExplicitImportTest.testUuid-input.xls";
		url = this.getClass().getResource(inputFile);
	 	assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
		uuidConfigurator = NormalExplicitImportConfigurator.NewInstance(url.toURI(), null, NomenclaturalCode.ICBN, null);
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
		//printDataSet(System.out);
		boolean result = defaultImport.invoke(configurator);
		assertTrue("Return value for import.invoke should be true", result);
		assertEquals("Number of TaxonNames should be 9", 9, nameService.count(null));
		List<Classification> treeList = classificationService.list(null, null,null,null,null);
		assertEquals("Number of classifications should be 1", 1, treeList.size());
		Classification tree = treeList.get(0);
		Set<TaxonNode> rootNodes = tree.getChildNodes();
		assertEquals("Number of root nodes should be 1", 1, rootNodes.size());
		TaxonNode rootNode = rootNodes.iterator().next();
		assertEquals("Root taxon name should be Animalia", "Animalia", rootNode.getTaxon().getName().getTitleCache());
		TaxonNode arthropodaNode = rootNode.getChildNodes().iterator().next();
		assertEquals("Arthropoda node taxon name should be Arthropoda", "Arthropoda", arthropodaNode.getTaxon().getName().getTitleCache());
		TaxonNode insectaNode = arthropodaNode.getChildNodes().iterator().next();
		TaxonNode lepidopteraNode = insectaNode.getChildNodes().iterator().next();
		TaxonNode noctuidaeNode = lepidopteraNode.getChildNodes().iterator().next();
		TaxonNode noctuaNode = noctuidaeNode.getChildNodes().iterator().next();
		assertEquals("Number of child nodes of noctuca should be 2", 2, noctuaNode.getChildNodes().size());
		
		Iterator<TaxonNode> it = noctuaNode.getChildNodes().iterator();
		TaxonNode childNode1 = it.next();
		TaxonNode childNode2 = it.next();
		
		TaxonNode noctuaPronubaNode;
		if (childNode1.getTaxon().getName().getTitleCache().startsWith("Noctua pronuba")){
			noctuaPronubaNode = childNode1;
		}else{
			noctuaPronubaNode = childNode2;
		}
		
		assertEquals("Noctua pronuba taxon name should be ", "Noctua pronuba", noctuaPronubaNode.getTaxon().getName().getTitleCache());
		Taxon noctuaPronubaTaxon = noctuaPronubaNode.getTaxon();
		Set<Synonym> synonyms = noctuaPronubaTaxon.getSynonyms();
		assertEquals("Number of synonyms should be 1", 1, synonyms.size());
		Synonym synonym = synonyms.iterator().next();
		assertEquals("Synonym name should be ", "Noctua atlantica", ((NonViralName)synonym.getName()).getNameCache());
		Set<TaxonDescription> descriptions = noctuaPronubaTaxon.getDescriptions();
		Assert.assertEquals("Number of descriptions should be 1", 1, descriptions.size());
		TaxonDescription taxonDescription = descriptions.iterator().next();
		Set<DescriptionElementBase> elements = taxonDescription.getElements();
		List<CommonTaxonName> commonNames = new ArrayList<CommonTaxonName>();
		for (DescriptionElementBase element : elements){
			if (element.isInstanceOf(CommonTaxonName.class)){
				commonNames.add((CommonTaxonName)element);
			}
		}
		Assert.assertEquals("Number of common names should be 2", 2, commonNames.size());
		Set<String> commonNameStrings = new HashSet<String>();
		commonNameStrings.add(commonNames.get(0).getName());
		commonNameStrings.add(commonNames.get(1).getName());
		Assert.assertTrue("Common names must include Yellow Underwing", commonNameStrings.contains("Large Sunshine Underwing"));
		Assert.assertTrue("Common names must include Yellow Underwing", commonNameStrings.contains("Yellow Underwing"));
	}
	
	@Test
	@DataSet(value="NormalExplicitImportTest.testUuid.xml")
	public void testUUID() throws URISyntaxException{
		UUID taxonUuid = UUID.fromString("aafce7fe-0c5f-42ed-814b-4c7c2c715660");
		UUID synonymUuid = UUID.fromString("fc4a995b-37a9-4984-afe6-e352c6c04d92");
		
		
		//test data set
		assertEquals("Number of taxon bases should be 2", 2, taxonService.count(null));
		Taxon taxon = (Taxon)taxonService.find(taxonUuid);
		assertNotNull("Taxon with given uuid should exist", taxon);
		assertEquals("Taxon should have no description", 0, taxon.getDescriptions().size());
		Synonym synonym = (Synonym)taxonService.find(synonymUuid);
		assertNotNull("Synonym with given uuid should exist", synonym);
		assertEquals("Synonym should have 1 accepted taxon", 1, synonym.getAcceptedTaxa().size());
		
		//import
		boolean result = defaultImport.invoke(uuidConfigurator);
		//test result
		assertTrue("Return value for import.invoke should be true", result);
		assertEquals("Number of taxon names should be 2", 2, nameService.count(null));
		assertEquals("Number of taxa should be 2", 2, taxonService.count(null));
		taxon = (Taxon)taxonService.find(taxonUuid);
		assertEquals("Taxon should have 1 description", 1, taxon.getDescriptions().size());
		TaxonDescription description = taxon.getDescriptions().iterator().next();
		assertEquals("Number of description elements should be 2", 2, description.getElements().size());
		
		String expectedText = "Description for the first taxon";
		TextData textData = getTextElement(description, expectedText);
		assertNotNull("The element should exists", textData);
		Feature feature = textData.getFeature();
		assertEquals("Unexpected feature", Feature.DESCRIPTION(), feature);
		assertEquals("There should be exactly 1 language", 1,textData.getMultilanguageText().size());
		Language language = textData.getMultilanguageText().keySet().iterator().next();
		assertEquals("Language should be German", Language.GERMAN(), language);
		String text = textData.getText(language);
		assertEquals("Unexpected description text", expectedText, text);
		assertEquals("Number of source elements should be 1", 1, textData.getSources().size());
		DescriptionElementSource source = textData.getSources().iterator().next();
		Reference ref = source.getCitation();
		assertNotNull("Citation should not be null", ref);
		assertNotNull("AuthorTeam should not be null", ref.getAuthorTeam());
		assertEquals("Source author should be 'Meyer et. al.'", "Meyer et. al.",ref.getAuthorTeam().getTitleCache());
		assertEquals("Publication title should be 'My first book'", "My first book", ref.getTitle());
		assertEquals("Publication year should be '1987'", "1987", ref.getYear());
		TaxonNameBase nameUsedInSource = source.getNameUsedInSource();
		assertNotNull("Name used in source should not be null", nameUsedInSource);
		assertEquals("Name used in source title should be ", "Abies", nameUsedInSource.getTitleCache());
		
		
		//synonym
		expectedText = "A synonym description";
		textData = getTextElement(description, expectedText);
		assertNotNull("The element should exists", textData);
		feature = textData.getFeature();
		assertEquals("Unexpected feature", Feature.DESCRIPTION(), feature);
		assertEquals("There should be exactly 1 language", 1,textData.getMultilanguageText().size());
		language = textData.getMultilanguageText().keySet().iterator().next();
		assertEquals("Language should be Spanish", Language.SPANISH_CASTILIAN(), language);
		text = textData.getText(language);
		assertEquals("Unexpected description text", expectedText, text);
		assertEquals("Number of source elements should be 1", 1, textData.getSources().size());
		source = textData.getSources().iterator().next();
		ref = source.getCitation();
		assertNotNull("Citation should not be null", ref);
		assertNotNull("AuthorTeam should not be null", ref.getAuthorTeam());
		assertEquals("Source author should be 'Theys, A.'", "Theys, A.",ref.getAuthorTeam().getTitleCache());
		assertEquals("Publication title should be 'The ultimate book'", "The ultimate book", ref.getTitle());
		assertEquals("Publication year should be '2011'", "2011", ref.getYear());
		nameUsedInSource = source.getNameUsedInSource();
		assertNotNull("Name used in source should not be null", nameUsedInSource);
		assertEquals("Name used in source title should be Pinus", "Pinus", nameUsedInSource.getTitleCache());
		
	}

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

}