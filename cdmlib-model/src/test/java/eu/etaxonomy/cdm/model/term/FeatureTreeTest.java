package eu.etaxonomy.cdm.model.term;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.description.Feature;

public class FeatureTreeTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FeatureTreeTest.class);

	private TermTree<Feature> testTree;
	private TermNode<Feature> node1;
	private TermNode<Feature> node2;
	private TermNode<Feature> node3;
	private TermNode<Feature> node4;

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
		testTree = TermTree.NewFeatureInstance();

		node1 = testTree.getRoot().addChild(Feature.ANATOMY());
		node2 = node1.addChild(Feature.BIOLOGY_ECOLOGY());
		node3 = node2.addChild(Feature.DESCRIPTION());
		node4 = node3.addChild(Feature.DISCUSSION());
	}
//	@Test
//	public void testSetRoot(){
//		testTree.setRoot(node2);
//		assertNotNull(testTree.getRoot());
//		assertEquals(testTree.getRoot(), node2);
//	}
	@Test
	public void testAddChild(){
		TermNode<Feature> node21 = node1.addChild(Feature.ANATOMY(), 1);
		assertEquals(node1.getChildNodes().size(), 2);
		assertEquals(node1.getChildNodes().get(1), node21);
		assertEquals(node21.getParent(), node1);
	}
	@Test
	public void testClone(){
        TermNode<Feature> node21 = node1.addChild(Feature.ADDITIONAL_PUBLICATION(), 1);
		TermTree<Feature> clone = (TermTree<Feature>) testTree.clone();

		assertEquals (clone.getRoot().getTerm(), testTree.getRoot().getTerm());
		assertNotSame(clone.getRoot(), testTree.getRoot());
		List<TermNode<Feature>> children = clone.getRootChildren();

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
