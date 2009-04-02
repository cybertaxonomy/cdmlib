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

import static org.junit.Assert.*;

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
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.mueller
 * @created 01.04.2009
 * @version 1.0
 */
public class TaxonomicViewTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonomicViewTest.class);

	private static String viewName1;
	private static TaxonomicView taxonomicView1;
	private static TaxonNode taxonNode1;
	private static TaxonNode taxonNode2;
	private static TaxonNode taxonNode3;
	private static Taxon taxon1;
	private static Taxon taxon2;
	private static TaxonNameBase<?,?> taxonName1;
	private static ReferenceBase ref1;
	
	
	
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
		taxonomicView1 = TaxonomicView.NewInstance(viewName1);
		taxonName1 = BotanicalName.NewInstance(Rank.SPECIES());
		ref1 = Journal.NewInstance();
		taxon1 = Taxon.NewInstance(taxonName1, ref1);
		//taxonNode1 = new TaxonNode(taxon1, taxonomicView1);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

//****************************** TESTS *****************************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonomicView#addRoot(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.reference.ReferenceBase, java.lang.String, eu.etaxonomy.cdm.model.taxon.Synonym)}.
	 */
	@Test
	public void testAddRoot() {
		TaxonNameBase<?,?> synonymName = BotanicalName.NewInstance(Rank.SPECIES());
		Synonym synonym = Synonym.NewInstance(synonymName, ref1);
		TaxonNode taxonNode1 = taxonomicView1.addRoot(taxon1, synonym);
		
		//test root node
		Set<TaxonNode> rootNodes = taxonomicView1.getRootNodes();
		assertFalse("List of root nodes should not be empty", rootNodes.isEmpty());
		assertEquals("Number of root nodes should be 1", 1, rootNodes.size());
		TaxonNode root = rootNodes.iterator().next();
		assertEquals(taxon1, root.getTaxon());
		assertSame(taxonNode1, root);
		assertNull(root.getReferenceForParentChildRelation());
		assertNull(root.getMicroReferenceForParentChildRelation());
		assertEquals(synonym, root.getSynonymToBeUsed());
		
		//any node
		Set<TaxonNode> allNodes = taxonomicView1.getRootNodes();
		assertFalse("List of root nodes should not be empty", allNodes.isEmpty());
		assertEquals("Number of root nodes should be 1", 1, allNodes.size());
		TaxonNode anyNode = allNodes.iterator().next();
		assertSame("Taxon for TaxonNode should be the same added to the view", taxon1, anyNode.getTaxon());
		assertSame("TaxonNode should be the same added to the view", taxonNode1, anyNode);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonomicView#isTaxonInView(eu.etaxonomy.cdm.model.taxon.Taxon)}.
	 */
	@Test
	public void testIsTaxonInView() {
		taxonomicView1.addRoot(taxon1, null);
		
		assertTrue(taxonomicView1.isTaxonInView(taxon1));
		Taxon anyTaxon = Taxon.NewInstance(null, null);
		assertFalse(taxonomicView1.isTaxonInView(anyTaxon));
	}
	
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonomicView#makeRootChildOfOtherNode(eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.reference.ReferenceBase, java.util.String)}.
	 */
	@Test
	public void testMakeRootChildOfOtherNode() {
		TaxonNode root1 = taxonomicView1.addRoot(taxon1, null);
		TaxonNode root2 = taxonomicView1.addRoot(taxon2, null);
		Taxon taxon3 = Taxon.NewInstance(null, null);
		root2.addChild(taxon3);
		String microRef = "p55";
		
		assertFalse("Root1 must not yet be child of root 2", root2.getChildNodes().contains(root1));
		assertNotSame("Root2 must not yet be parent of root 1", root2, root1.getParent());
		assertEquals("view must contain 3 nodes", 3, taxonomicView1.getAllNodes().size());
		assertEquals("view must still contain 2 root", 2, taxonomicView1.getRootNodes().size());
		assertEquals("root2 must have 1 child", 1, root2.getChildNodes().size());
		
		taxonomicView1.makeRootChildOfOtherNode(root1, root2, ref1, microRef);
		assertTrue("Root1 must be child of root 2", root2.getChildNodes().contains(root1));
		assertSame("Root2 must be parent of root 1", root2, root1.getParent());
		assertEquals("view must contain 3 nodes", 3, taxonomicView1.getAllNodes().size());
		assertEquals("view must contain 1 root", 1, taxonomicView1.getRootNodes().size());
		assertEquals("new child node must have the expected reference for parent child relationship", ref1, root1.getReferenceForParentChildRelation());
		assertEquals("new child node must have the expected micro reference for parent child relationship", microRef, root1.getMicroReferenceForParentChildRelation());
		assertEquals("root2 must have 2 children", 2, root2.getChildNodes().size());
		
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonomicView#generateTitle()}.
	 */
	@Test
	public void testGenerateTitle() {
		TaxonomicView taxonomicViewLocal = TaxonomicView.NewInstance(viewName1);
		//Maybe changed if title cache is generated in a different way
		assertEquals(viewName1, taxonomicViewLocal.getTitleCache());
	}

}
