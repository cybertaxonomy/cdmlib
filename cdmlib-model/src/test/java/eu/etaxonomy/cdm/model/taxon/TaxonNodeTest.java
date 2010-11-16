// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
//import eu.etaxonomy.cdm.model.reference.Book;
//import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 01.04.2009
 * @version 1.0
 */
public class TaxonNodeTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonNodeTest.class);
	private static String viewName1;
	private static TaxonomicTree taxonomicView1;
	private static TaxonomicTree taxonomicView2;
	private static Taxon taxon1;
	private static Taxon taxon2;
	private static Taxon taxon3;
	private static TaxonNameBase<?,?> taxonName1;
	private static TaxonNameBase<?,?> taxonName2;
	private static TaxonNameBase<?,?> taxonName3;
	private static Reference ref1;
	private static Reference ref2;
	private static Reference ref3;
	private static Synonym syn1;
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		viewName1 = "Greuther, 1993";
		ReferenceFactory refFactory = ReferenceFactory.newInstance();
		taxonomicView1 = TaxonomicTree.NewInstance(viewName1);
		taxonomicView2 = TaxonomicTree.NewInstance("Test View 2");
		taxonName1 = BotanicalName.NewInstance(Rank.SPECIES());
		taxonName1 = BotanicalName.NewInstance(Rank.SUBSPECIES());
		taxonName3 = BotanicalName.NewInstance(Rank.SPECIES());
		ref1 = refFactory.newJournal();
		ref2 = refFactory.newBook();
		ref3 = refFactory.newGeneric();
		taxon1 = Taxon.NewInstance(taxonName1, ref1);
		taxon2 = Taxon.NewInstance(taxonName2, ref1);
		taxon3 = Taxon.NewInstance(taxonName3, ref3);
		//taxonNode1 = new TaxonNode(taxon1, taxonomicView1);
		syn1 = Synonym.NewInstance(null, null);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

//****************************** TESTS *****************************************/


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonNode#NewInstance(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonomicTree)}.
	 */
	@Test
	public void testNewTaxonTaxonomicView() {
		TaxonNode testNode = new TaxonNode(taxon1);
		taxonomicView1.addChildNode(testNode, null, null, null);
		
		assertNotNull("test node should not be null", testNode);
		assertEquals(taxon1,testNode.getTaxon());
		assertEquals(taxonomicView1,testNode.getTaxonomicTree());
		assertTrue("taxon1 must become part of taxonomicView1", taxonomicView1.isTaxonInTree(taxon1));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonNode#addChild(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String, eu.etaxonomy.cdm.model.taxon.Synonym)}.
	 */
	@Test
	public void testAddChild() {
		TaxonNode root = taxonomicView1.addChildTaxon(taxon1, null, null, null);
		assertEquals("Number of all nodes in view should be 1", 1, taxonomicView1.getAllNodes().size());
		
		TaxonNode child = root.addChildTaxon(taxon2, ref2, "p33", syn1);
		//test child properties
		assertNotNull("Child should not be null", child);
		assertEquals("Child taxon should be taxon2", taxon2, child.getTaxon());
		assertEquals("Parent taxon should be taxon1", taxon1, (child.getParent()).getTaxon());
		assertEquals("Reference should be ref2", ref2, child.getReference());
		assertEquals("Microreference should be 'p33'", "p33", child.getMicroReference());
		assertEquals("Synonym should be syn1", syn1, child.getSynonymToBeUsed());
		
		//test parent properties
		Set<TaxonNode> childList = root.getChildNodes();
		assertFalse("parent child list must not be empty",childList.isEmpty());
		assertEquals("size of child list be 1", 1, childList.size());
		assertSame("taxa must be the same", taxon2, childList.iterator().next().getTaxon());
		
		//test view properties
		Set<TaxonNode> rootNodes = taxonomicView1.getChildNodes();
		assertEquals("Number of root nodes should be 1", 1, rootNodes.size());
		Set<TaxonNode> allNodes = taxonomicView1.getAllNodes();
		assertEquals("Number of all nodes should be 2", 2, allNodes.size());
		assertTrue("Taxonomic view should include child", allNodes.contains(child));
		
		
		//is part of taxon
		Set<TaxonNode> nodes2 = taxon2.getTaxonNodes();
		assertFalse("taxon2 must not be empty", nodes2.isEmpty());
		assertEquals("size of nodes of taxon2 must be 1", 1, nodes2.size());
		assertSame("taxa must be the same", taxon2, nodes2.iterator().next().getTaxon());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonNode#setTaxon(eu.etaxonomy.cdm.model.taxon.Taxon)}.
	 */
	@Test
	public void testSetTaxon() {
		TaxonNode node = new TaxonNode(taxon1);
		taxonomicView1.addChildNode(node, null, null, null);
		assertNotNull(taxon2);
		node.setTaxon(taxon2);
		assertSame("taxon must be the same", taxon2, node.getTaxon());
		assertTrue("taxon2 must contain node", taxon2.getTaxonNodes().contains(node));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonNode#setParent(eu.etaxonomy.cdm.model.taxon.TaxonNode)}.
	 */
	@Test
	public void testSetParent() {
		TaxonNode node = new TaxonNode(taxon1);
		assertNotNull(taxon2);
		TaxonNode parent = new TaxonNode(taxon2);
		assertSame("Taxon must be the same", taxon2, parent.getTaxon());
		taxonomicView1.addChildNode(parent, null, null, null);
		node.setParent(parent);
		assertSame("taxon2 must contain node", parent, node.getParent());
		assertTrue("setParent must not handle child list of parent", parent.getChildNodes().isEmpty());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonNode#getChildNodes()}.
	 */
	@Test
	public void testGetChildNodes() {
		TaxonNode root = taxonomicView1.addChildTaxon(taxon1, null, null, null);
		assertEquals("Number of all nodes in view should be 1", 1, taxonomicView1.getAllNodes().size());
		
		TaxonNode child = root.addChildTaxon(taxon2, ref2, "p33", syn1);
		
		Set<TaxonNode> childList = root.getChildNodes();
		assertFalse("parent child list must not be empty",childList.isEmpty());
		assertEquals("size of child list be 1", 1, childList.size());
		assertSame("child must be in child list", child, childList.iterator().next());
		assertSame("taxa must be the same", taxon2, childList.iterator().next().getTaxon());	
	}
	
	@Test
	public void testGetCountChildren(){
		TaxonNode root = taxonomicView1.addChildTaxon(taxon1, null, null, null);
		assertEquals("Count of children must be 0", 0, root.getCountChildren());
		TaxonNode child = root.addChildTaxon(taxon2, ref2, "p33", syn1);
		assertEquals("Count of children must be 1", 1, root.getCountChildren());
		Taxon taxon3 = Taxon.NewInstance(null, null);
		TaxonNode child2 = root.addChildTaxon(taxon3, null, null, null);
		assertEquals("Count of children must be 2", 2, root.getCountChildren());
		root.removeChildNode(child);
		assertEquals("Count of children must be 1", 1, root.getCountChildren());
		root.removeChildNode(child2);
		assertEquals("Count of children must be 0", 0, root.getCountChildren());
		
	}
	
	@Test
	public void testDelete(){
		TaxonNode root = taxonomicView1.addChildTaxon(taxon1, null, null, null);
		assertEquals("Number of all nodes in view should be 1", 1, taxonomicView1.getAllNodes().size());
		
		
		TaxonNode childNode = root.addChildTaxon(taxon2, null, null, null);
		assertEquals("Count of children must be 1", 1, root.getCountChildren());
		
		childNode.delete();
		assertEquals("Count of children must be 0", 0, root.getCountChildren());
		
		
		root.delete();
		assertEquals("Number of all nodes in view should be 0", 0, taxonomicView1.getAllNodes().size());
		
		
	}
	
	@Test
	public void testMoveTaxonNodeToOtherTree(){
		TaxonNode node = taxonomicView1.addChildTaxon(taxon1, null, null, null);
		assertEquals("The node should be in the classification we added it to", taxonomicView1, node.getTaxonomicTree());
		
		TaxonNode movedNode = taxonomicView2.addChildNode(node, null, null, null);
		assertEquals("The node should be in the classification we moved it to", taxonomicView2, movedNode.getTaxonomicTree());
		assertEquals("The old tree should be empty now", 0, taxonomicView1.getChildNodes().size());
	}
	
	@Test
	public void testMoveTaxonNodeToOtherTaxonNodeInDifferentTree(){
		TaxonNode node1 = taxonomicView1.addChildTaxon(taxon1, null, null, null);
		TaxonNode node2 = node1.addChildTaxon(taxon3, null, null, null);
		
		assertEquals("The node should have exactly one child", 1, node1.getChildNodes().size());
		assertEquals("The child is not in the correct tree", taxonomicView1, node2.getTaxonomicTree());
		assertEquals("The Classification should contain exactly two nodes", 2, taxonomicView1.getAllNodes().size());
		
		TaxonNode node3 = taxonomicView2.addChildTaxon(taxon3, null, null, null);
		
		// move node2 to node3 in other tree
		node3.addChildNode(node2, null, null, null);
		
		assertEquals("Old node should not have child nodes", 0, node1.getChildNodes().size());
		assertEquals("Old tree should contain only one node now", 1, taxonomicView1.getAllNodes().size());
		assertEquals("Moved node not in expected tree", taxonomicView2, node2.getTaxonomicTree());
		assertEquals("Count of nodes in new tree:", 2, taxonomicView2.getAllNodes().size());
		
	}
	
	/**
	 * Basically tests setTaxonomicTreeRecursively(TaxonomicTree) which is a private method
	 */
	@Test
	public void testMoveTaxonNodesRecursivelyToOtherTaxonNodeInDifferentTree(){
		TaxonNode node1 = taxonomicView1.addChildTaxon(taxon1, null, null, null);
		TaxonNode node2 = node1.addChildTaxon(taxon2, null, null, null);		
		TaxonNode node3 = node2.addChildTaxon(taxon3, null, null, null);
		
		//move the branch to a different tree		
		taxonomicView2.addChildNode(node1, null, null, null);
		
		assertEquals("Old tree should be empty:", 0, taxonomicView1.getAllNodes().size());
		assertEquals("Moved node not in expected tree:", taxonomicView2, node1.getTaxonomicTree());
		assertEquals("Recursively moved node not in expected tree:", taxonomicView2, node2.getTaxonomicTree());
		assertEquals("Recursively moved node not in expected tree:", taxonomicView2, node3.getTaxonomicTree());
		
		assertEquals("Count of nodes in new tree:", 3, taxonomicView2.getAllNodes().size());
		
	}
	
}
