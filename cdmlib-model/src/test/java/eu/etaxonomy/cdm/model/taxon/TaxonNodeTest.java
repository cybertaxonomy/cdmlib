/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.taxon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
//import eu.etaxonomy.cdm.model.reference.Book;
//import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @since 01.04.2009
 */
public class TaxonNodeTest {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private static String viewName1;
	private static Classification classification1;
	private static Classification classification2;
	private static Taxon taxon1;
	private static Taxon taxon2;
	private static Taxon taxon3;
	private static TaxonName taxonName1;
	private static TaxonName taxonName2;
	private static TaxonName taxonName3;
	private static Reference ref1;
	private static Reference ref2;
	private static Reference ref3;
	private static Synonym syn1;

	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	public static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	public void setUp() throws Exception {
		viewName1 = "Greuther, 1993";
		classification1 = Classification.NewInstance(viewName1);
		classification2 = Classification.NewInstance("Test View 2");
		taxonName1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		taxonName1 = TaxonNameFactory.NewBotanicalInstance(Rank.SUBSPECIES());
		taxonName3 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		ref1 = ReferenceFactory.newJournal();
		ref2 = ReferenceFactory.newBook();
		ref3 = ReferenceFactory.newGeneric();
		taxon1 = Taxon.NewInstance(taxonName1, ref1);
		taxon2 = Taxon.NewInstance(taxonName2, ref1);
		taxon3 = Taxon.NewInstance(taxonName3, ref3);
		//taxonNode1 = new TaxonNode(taxon1, taxonomicView1);
		syn1 = Synonym.NewInstance(null, null);
	}

//****************************** TESTS *****************************************/

	@Test
	public void testNewTaxonTaxonomicView() {
		TaxonNode testNode = new TaxonNode(taxon1);
		classification1.addChildNode(testNode, null, null);

		assertNotNull(testNode, "test node should not be null");
		assertEquals(taxon1,testNode.getTaxon());
		assertEquals(classification1,testNode.getClassification());
		assertTrue(classification1.isTaxonInTree(taxon1), "taxon1 must become part of taxonomicView1");
	}

	@Test
	public void testAddChild() {
		TaxonNode root = classification1.addChildTaxon(taxon1, null, null);
		assertEquals(1, classification1.getAllNodes().size(), "Number of all nodes in view should be 1");

		TaxonNode child = root.addChildTaxon(taxon2, ref2, "p33");
		child.setSynonymToBeUsed(syn1); //originally synToBeUsed was part of addChildTaxon
		//test child properties
		assertNotNull(child, "Child should not be null");
		assertEquals(taxon2, child.getTaxon(), "Child taxon should be taxon2");
		assertEquals(taxon1, (child.getParent()).getTaxon(), "Parent taxon should be taxon1");
		assertEquals(ref2, child.getReference(), "Reference should be ref2");
		assertEquals("p33", child.getMicroReference(), "Microreference should be 'p33'");
		assertEquals(syn1, child.getSynonymToBeUsed(), "Synonym should be syn1");

		//test parent properties
		List<TaxonNode> childList = root.getChildNodes();
		assertFalse(childList.isEmpty(), "parent child list must not be empty");
		assertEquals(1, childList.size(), "size of child list be 1");
		assertSame(taxon2, childList.iterator().next().getTaxon(), "taxa must be the same");

		//test view properties
		List<TaxonNode> rootNodes = classification1.getChildNodes();
		assertEquals(1, rootNodes.size(), "Number of root nodes should be 1");
		Set<TaxonNode> allNodes = classification1.getAllNodes();
		assertEquals(2, allNodes.size(), "Number of all nodes should be 2");
		assertTrue(allNodes.contains(child), "Taxonomic view should include child");


		//is part of taxon
		Set<TaxonNode> nodes2 = taxon2.getTaxonNodes();
		assertFalse(nodes2.isEmpty(), "taxon2 must not be empty");
		assertEquals(1, nodes2.size(), "size of nodes of taxon2 must be 1");
		assertSame(taxon2, nodes2.iterator().next().getTaxon(), "taxa must be the same");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonNode#setTaxon(eu.etaxonomy.cdm.model.taxon.Taxon)}.
	 */
	@Test
	public void testSetTaxon() {
		TaxonNode node = new TaxonNode(taxon1);
		classification1.addChildNode(node, null, null);
		assertNotNull(taxon2);
		node.setTaxon(taxon2);
		assertSame(taxon2, node.getTaxon(), "taxon must be the same");
		assertTrue(taxon2.getTaxonNodes().contains(node), "taxon2 must contain node");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonNode#setParent(eu.etaxonomy.cdm.model.taxon.TaxonNode)}.
	 */
	@Test
	public void testSetParent() {
		TaxonNode node = new TaxonNode(taxon1);
		assertNotNull(taxon2);
		TaxonNode parent = new TaxonNode(taxon2);
		assertSame(taxon2, parent.getTaxon(), "Taxon must be the same");
		classification1.addChildNode(parent, null, null);
		node.setParent(parent);
		assertSame(parent, node.getParent(), "taxon2 must contain node");
		assertTrue(parent.getChildNodes().isEmpty(), "setParent must not handle child list of parent");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonNode#getChildNodes()}.
	 */
	@Test
	public void testGetChildNodes() {
		TaxonNode root = classification1.addChildTaxon(taxon1, null, null);
		assertEquals(1, classification1.getAllNodes().size(), "Number of all nodes in view should be 1");

		TaxonNode child = root.addChildTaxon(taxon2, ref2, "p33");
		child.setSynonymToBeUsed(syn1);

		List<TaxonNode> childList = root.getChildNodes();
		assertFalse(childList.isEmpty(), "parent child list must not be empty");
		assertEquals(1, childList.size(), "size of child list be 1");
		assertSame(child, childList.iterator().next(), "child must be in child list");
		assertSame(taxon2, childList.iterator().next().getTaxon(), "taxa must be the same");
	}

	@Test
	public void testGetCountChildren(){
		TaxonNode root = classification1.addChildTaxon(taxon1, null, null);
		assertEquals(0, root.getCountChildren(), "Count of children must be 0");
		TaxonNode child = root.addChildTaxon(taxon2, ref2, "p33");
		child.setSynonymToBeUsed(syn1);
		assertEquals(1, root.getCountChildren(), "Count of children must be 1");
		Taxon taxon3 = Taxon.NewInstance(null, null);
		TaxonNode child2 = root.addChildTaxon(taxon3, null, null);
		assertEquals(2, root.getCountChildren(), "Count of children must be 2");
		root.removeChildNode(child);
		assertEquals(1, root.getCountChildren(), "Count of children must be 1");
		root.removeChildNode(child2);
		assertEquals(0, root.getCountChildren(), "Count of children must be 0");

	}

	@Test
	public void testDelete(){
		TaxonNode root = classification1.addChildTaxon(taxon1, null, null);
		assertEquals(1, classification1.getAllNodes().size(), "Number of all nodes in view should be 1");


		TaxonNode childNode = root.addChildTaxon(taxon2, null, null);
		assertEquals(1, root.getCountChildren(), "Count of children must be 1");

		childNode.delete();
		assertEquals(0, root.getCountChildren(), "Count of children must be 0");


		root.delete();
		assertEquals(0, classification1.getAllNodes().size(), "Number of all nodes in view should be 0");


	}

	@Test
	public void testMoveTaxonNodeToOtherTree(){
		TaxonNode node = classification1.addChildTaxon(taxon1, null, null);
		assertEquals(classification1, node.getClassification(), "The node should be in the classification we added it to");

		TaxonNode movedNode = classification2.addChildNode(node, null, null);
		assertEquals(classification2, movedNode.getClassification(), "The node should be in the classification we moved it to");
		assertEquals(0, classification1.getChildNodes().size(), "The old tree should be empty now");
	}

	@Test
	public void testMoveTaxonNodeToOtherTaxonNodeInDifferentTree(){
		TaxonNode node1 = classification1.addChildTaxon(taxon1, null, null);
		TaxonNode node2 = node1.addChildTaxon(taxon3, null, null);

		assertEquals(1, node1.getChildNodes().size(), "The node should have exactly one child");
		assertEquals(classification1, node2.getClassification(), "The child is not in the correct tree");
		assertEquals(2, classification1.getAllNodes().size(), "The Classification should contain exactly two nodes");

		TaxonNode node3 = classification2.addChildTaxon(taxon3, null, null);

		// move node2 to node3 in other tree
		node3.addChildNode(node2, null, null);

		assertEquals(0, node1.getChildNodes().size(), "Old node should not have child nodes");
		assertEquals(1, classification1.getAllNodes().size(), "Old tree should contain only one node now");
		assertEquals(classification2, node2.getClassification(), "Moved node not in expected tree");
		assertEquals(2, classification2.getAllNodes().size(), "Count of nodes in new tree:");

	}

	/**
	 * Basically tests #setClassificationRecursively(Classification) which is a private method
	 */
	@Test
	public void testMoveTaxonNodesRecursivelyToOtherTaxonNodeInDifferentTree(){
		TaxonNode node1 = classification1.addChildTaxon(taxon1, null, null);
		TaxonNode node2 = node1.addChildTaxon(taxon2, null, null);
		TaxonNode node3 = node2.addChildTaxon(taxon3, null, null);

		//move the branch to a different tree
		classification2.addChildNode(node1, null, null);

		assertEquals(0, classification1.getAllNodes().size(), "Old tree should be empty:");
		assertEquals(classification2, node1.getClassification(), "Moved node not in expected tree:");
		assertEquals(classification2, node2.getClassification(), "Recursively moved node not in expected tree:");
		assertEquals(classification2, node3.getClassification(), "Recursively moved node not in expected tree:");

		assertEquals(3, classification2.getAllNodes().size(), "Count of nodes in new tree:");

	}

	@Test
	public void testAddChildNode(){
		TaxonNode root = classification1.addChildTaxon(taxon1, null, null);
		assertEquals(1, classification1.getAllNodes().size(), "Number of all nodes in cla should be 1");

		TaxonNode child = root.addChildTaxon(taxon2, ref2, "p33");
		child.setSynonymToBeUsed(syn1); //originally synToBeUsed was part of addChildTaxon
		//test child properties
		assertNotNull(child, "Child should not be null");
		assertEquals(taxon2, child.getTaxon(), "Child taxon should be taxon2");
		assertEquals(taxon1, (child.getParent()).getTaxon(), "Parent taxon should be taxon1");
		assertEquals(ref2, child.getReference(), "Reference should be ref2");
		assertEquals("p33", child.getMicroReference(), "Microreference should be 'p33'");
		assertEquals(syn1, child.getSynonymToBeUsed(), "Synonym should be syn1");

		//test parent properties
		List<TaxonNode> childList = root.getChildNodes();
		assertFalse(childList.isEmpty(), "parent child list must not be empty");
		assertEquals(1, childList.size(), "size of child list be 1");
		assertSame(taxon2, childList.iterator().next().getTaxon(), "taxa must be the same");

		//test view properties
		List<TaxonNode> rootNodes = classification1.getChildNodes();
		assertEquals(1, rootNodes.size(), "Number of root nodes should be 1");
		Set<TaxonNode> allNodes = classification1.getAllNodes();
		assertEquals(2, allNodes.size(), "Number of all nodes should be 2");
		assertTrue(allNodes.contains(child), "Taxonomic view should include child");
	}



    @Test
    public void testGetAncestors(){
    	/*
    	 * Classification
    	 *  * Pinus
    	 *  `- Pinus pampa
    	 *   `- Pinus pampa subsp. persicifolia
    	 */
    	Classification classification = Classification.NewInstance("Classification");
    	IBotanicalName pinusName = TaxonNameFactory.NewBotanicalInstance(null);
    	pinusName.setGenusOrUninomial("Pinus");
    	Taxon pinus = Taxon.NewInstance(pinusName, null);
    	IBotanicalName pinusPampaName = TaxonNameFactory.NewBotanicalInstance(null);
    	pinusPampaName.setGenusOrUninomial("Pinus");
    	pinusPampaName.setSpecificEpithet("pampa");
    	Taxon pinusPampa = Taxon.NewInstance(pinusPampaName, null);
    	IBotanicalName pinusPampaSubName = TaxonNameFactory.NewBotanicalInstance(null);
    	pinusPampaSubName.setGenusOrUninomial("Pinus");
    	pinusPampaSubName.setSpecificEpithet("pampa");
    	pinusPampaSubName.setInfraSpecificEpithet("persicifolia");
    	Taxon pinusPampaSub = Taxon.NewInstance(pinusPampaSubName, null);

    	TaxonNode pinusNode = classification.addChildTaxon(pinus, null, null);
    	TaxonNode pinusPampaNode = classification.addParentChild(pinus, pinusPampa, null, null);
    	TaxonNode pinusPampaSubNode = classification.addParentChild(pinusPampa, pinusPampaSub, null, null);
    	TaxonNode rootNode = classification.getRootNode();

    	Set<TaxonNode> ancestors = pinusPampaSubNode.getAncestors();
    	assertEquals(3, ancestors.size());
    	assertTrue(ancestors.contains(pinusPampaNode));
    	assertTrue(ancestors.contains(pinusNode));
    	assertTrue(ancestors.contains(rootNode));

    	Set<TaxonNode> rootAncestors = rootNode.getAncestors();
    	assertTrue(rootAncestors.isEmpty());

    }

    @Test
    public void beanTests(){
//      #5307 Test that BeanUtils does not fail
        BeanUtils.getPropertyDescriptors(TaxonNode.class);
    }

}
