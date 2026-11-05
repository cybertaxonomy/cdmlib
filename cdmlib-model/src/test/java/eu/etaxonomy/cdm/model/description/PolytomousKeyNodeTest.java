/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.description;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

public class PolytomousKeyNodeTest extends EntityTestBase {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private PolytomousKey key1;
	private Taxon taxon1;

	@BeforeEach
	public void setUp() throws Exception {
		key1 = PolytomousKey.NewInstance();
		key1.setTitleCache("My Test Key", true);
		PolytomousKeyNode root = key1.getRoot();
		root.setQuestion(KeyStatement.NewInstance("Is this Aus bus?"));

		// child1
		taxon1 = Taxon.NewInstance(null, null);
		taxon1.setTitleCache("Aus bus L.", true);
		PolytomousKeyNode child1 = PolytomousKeyNode.NewInstance("Yes", null, taxon1, null);
		Feature feature1 = Feature.NewInstance(null, "Leaf", null);
		child1.setFeature(feature1);
		root.addChild(child1);

		// child2
		Taxon taxon2 = Taxon.NewInstance(null, null);
		taxon2.setTitleCache("Cus dus Mill.", true);
		PolytomousKeyNode child2 = PolytomousKeyNode.NewInstance("No");
		child2.setTaxon(taxon2);
		root.addChild(child2);

		// child3
		Taxon taxon3 = Taxon.NewInstance(null, null);
		taxon3.setTitleCache("Cus dus subs. rus L.", true);
		PolytomousKeyNode child3 = PolytomousKeyNode.NewInstance("Long and wide");
		child3.setTaxon(taxon3);
		child1.addChild(child3);

		// child4
		Taxon taxon4 = Taxon.NewInstance(null, null);
		taxon4.setTitleCache("Cus dus subs. zus L.", true);
		PolytomousKeyNode child4 = PolytomousKeyNode.NewInstance("Small and narrow");
		child4.setTaxon(taxon4);
		child1.addChild(child4);

		PolytomousKey key2 = PolytomousKey.NewTitledInstance("Second Key");
		child3.setSubkey(key2);

		child4.setOtherNode(key2.getRoot());

		PolytomousKeyNode child5 = PolytomousKeyNode.NewInstance("Long and narrow");
		child3.addChild(child5);

	}

	// ********************* Tests ***************************************/

	@Test
	public void testNodeNumber() {
		PolytomousKeyNode root = key1.getRoot();
		assertEquals(Integer.valueOf(1), root.getNodeNumber(), "Root should have node number = 1");
		PolytomousKeyNode child1 = root.getChildAt(0);
		assertEquals(Integer.valueOf(2), child1.getNodeNumber(), "Child1 should have node number = 2");
		PolytomousKeyNode child2 = root.getChildAt(1);
		assertEquals(null, child2.getNodeNumber(), "Child2 should have node number = null");
		PolytomousKeyNode child3 = child1.getChildAt(0);
		assertEquals(Integer.valueOf(3), child3.getNodeNumber(), "Child3 should have node number = 3");
		PolytomousKeyNode child4 = child1.getChildAt(1);
		assertEquals(null, child4.getNodeNumber(), "Child4 should have node number  = null");
	}

	@Test
	public void testClone(){
		PolytomousKeyNode rootNode = key1.getRoot();
		PolytomousKeyNode clone = rootNode.clone();
		assertNotNull(clone);
		assertEquals(clone.getFeature(), rootNode.getFeature());
		assertEquals(clone.getKey(), rootNode.getKey());
		assertTrue(clone.getChildren().size() == 0);
		assertTrue(rootNode.getChildren().size()>0);
	}

	@Test
	public void testRemoveChild() {
		PolytomousKey key = PolytomousKey.NewInstance();
		PolytomousKeyNode parent = key.getRoot();
		PolytomousKeyNode child = PolytomousKeyNode.NewInstance();
		parent.addChild(child);

		assertEquals(1, parent.getChildren().size(), "Parent node should have one child");

		parent.removeChild(child);

		assertEquals(0, parent.getChildren().size(), "Parent node should have no children");
	}
}
