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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITaxonTreeService;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
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
	ITaxonTreeService taxonTreeService;

	private IImportConfigurator configurator;
	
	@Before
	public void setUp() {
		String inputFile = "/eu/etaxonomy/cdm/io/excel/taxa/NormalExplicitImportTest-input.xls";
		URL url = this.getClass().getResource(inputFile);
	 	assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
		configurator = NormalExplicitImportConfigurator.NewInstance(url.toString(), null, NomenclaturalCode.ICBN);
		assertNotNull("Configurator could not be created", configurator);
	}
	
	@Test
	public void testInit() {
		assertNotNull("normalExplicitImport should not be null", defaultImport);
		assertNotNull("nameService should not be null", nameService);
	}
	
	@Test
	@DataSet
	@Ignore //does run standalone, but not in suite (maven)
	public void testDoInvoke() {
		//printDataSet(System.out);
		boolean result = defaultImport.invoke(configurator);
		assertTrue("Return value for import.invoke should be true", result);
		assertEquals("Number of TaxonNames should be 9", 9, nameService.count(null));
		List<TaxonomicTree> treeList = taxonTreeService.list(null, null,null,null,null);
		assertEquals("Number of taxonomic trees should be 1", 1, treeList.size());
		TaxonomicTree tree = treeList.get(0);
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
}
