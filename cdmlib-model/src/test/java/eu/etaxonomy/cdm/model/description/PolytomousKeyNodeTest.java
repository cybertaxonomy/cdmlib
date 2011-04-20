package eu.etaxonomy.cdm.model.description;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class PolytomousKeyNodeTest {
	private static Logger logger = Logger
			.getLogger(PolytomousKeyNodeTest.class);

	private PolytomousKey key1;
	private Taxon taxon1;

	@BeforeClass
	public static void setUpBeforeClass() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
	}

	@Before
	public void setUp() throws Exception {
		key1 = PolytomousKey.NewInstance();
		key1.setTitleCache("My Test Key", true);
		PolytomousKeyNode root = key1.getRoot();
		root.setQuestion(KeyStatement.NewInstance("Is this Aus bus?"));
		// child1
		taxon1 = Taxon.NewInstance(null, null);
		taxon1.setTitleCache("Aus bus L.", true);
		PolytomousKeyNode child1 = PolytomousKeyNode.NewInstance("Yes", null,
				taxon1, null);
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
		PolytomousKeyNode child3 = PolytomousKeyNode
				.NewInstance("Long and wide");
		child3.setTaxon(taxon3);
		child1.addChild(child3);
		// child4
		Taxon taxon4 = Taxon.NewInstance(null, null);
		taxon4.setTitleCache("Cus dus subs. zus L.", true);
		PolytomousKeyNode child4 = PolytomousKeyNode
				.NewInstance("Small and narrow");
		child4.setTaxon(taxon4);
		child1.addChild(child4);

		PolytomousKey key2 = PolytomousKey.NewTitledInstance("Second Key");
		child3.setSubkey(key2);

		child4.setOtherNode(key2.getRoot());

	}

	// ********************* Tests
	// *******************************************************/

	@Test
	public void testNodeNumber() {
		PolytomousKeyNode root = key1.getRoot();
		Assert.assertEquals("Root should have node number 0",
				Integer.valueOf(0), root.getNodeNumber());
		PolytomousKeyNode child1 = root.getChildAt(0);
		Assert.assertEquals("Child1 should have node number 1",
				Integer.valueOf(1), child1.getNodeNumber());
		PolytomousKeyNode child2 = root.getChildAt(1);
		Assert.assertEquals("Child2 should have node number <null>", null,
				child2.getNodeNumber());
		PolytomousKeyNode child3 = child1.getChildAt(0);
		Assert.assertEquals("Child3 should have node number <null>", null,
				child3.getNodeNumber());
		PolytomousKeyNode child4 = child1.getChildAt(0);
		Assert.assertEquals("Child4 should have node number <null>", null,
				child4.getNodeNumber());

	}

	@Test
	public void testRemoveChild() {
		PolytomousKey key = PolytomousKey.NewInstance();
		PolytomousKeyNode parent = key.getRoot();
		PolytomousKeyNode child = PolytomousKeyNode.NewInstance();
		parent.addChild(child);

		Assert.assertEquals("Parent node should have one child", 1, parent
				.getChildren().size());

		parent.removeChild(child);

		Assert.assertEquals("Parent node should have no children", 0, parent
				.getChildren().size());
	}

}
