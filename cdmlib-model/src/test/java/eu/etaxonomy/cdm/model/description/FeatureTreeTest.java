package eu.etaxonomy.cdm.model.description;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.term.DefaultTermInitializer;

public class FeatureTreeTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FeatureTreeTest.class);

	private FeatureTree testTree;
	private FeatureNode node1;
	private FeatureNode node2;
	private FeatureNode node3;
	private FeatureNode node4;

	@BeforeClass
	public static void setUpBeforeClass() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testTree = FeatureTree.NewInstance();

		node1 = FeatureNode.NewInstance(Feature.ANATOMY());
		node2 = FeatureNode.NewInstance(Feature.BIOLOGY_ECOLOGY());
		node3 = FeatureNode.NewInstance(Feature.DESCRIPTION());
		node4 = FeatureNode.NewInstance(Feature.DISCUSSION());

		testTree.getRoot().addChild(node1);
		node1.addChild(node2);
		node2.addChild(node3);
		node3.addChild(node4);



	}
//	@Test
//	public void testSetRoot(){
//		testTree.setRoot(node2);
//		assertNotNull(testTree.getRoot());
//		assertEquals(testTree.getRoot(), node2);
//	}
	@Test
	public void testAddChild(){
		FeatureNode node21 = FeatureNode.NewInstance();
		node21.setTerm(Feature.ANATOMY());
		node1.addChild(node21, 1);

		assertEquals(node1.getChildNodes().size(), 2);
		assertEquals(node1.getChildNodes().get(1), node21);


		assertEquals(node21.getParent(), node1);


	}
	@Test
	public void testClone(){
		FeatureNode node21 = FeatureNode.NewInstance();
		node21.setTerm(Feature.ADDITIONAL_PUBLICATION());
		node1.addChild(node21, 1);
		FeatureTree clone = (FeatureTree) testTree.clone();
		assertEquals (clone.getRoot().getTerm(), testTree.getRoot().getTerm());
		assertNotSame(clone.getRoot(), testTree.getRoot());
		List<FeatureNode> children = clone.getRootChildren();


		assertEquals(children.get(0).getTerm(), node1.getTerm());
	    assertNotSame(children.get(0), node1);
	    children = children.get(0).getChildNodes();

		assertEquals(children.get(0).getTerm(), node2.getTerm());
		assertNotSame(children.get(0), node2);
		assertEquals(children.get(1).getTerm(), node21.getTerm());
		assertNotSame(children.get(1), node21);
		assertEquals(children.get(0).getChildAt(0).getTerm(), node3.getTerm());
		assertNotSame(children.get(0).getChildAt(0), node3);
	}


}
