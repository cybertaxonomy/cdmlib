package eu.etaxonomy.cdm.model.common;

import static org.junit.Assert.*;

import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class OrderedTermVocabularyTest {
	private static Logger logger = Logger.getLogger(OrderedTermVocabularyTest.class);
	
	private OrderedTermBase otb1;
	private OrderedTermBase otb2;
	private OrderedTermBase otb3;
	private OrderedTermBase otbFree;
	private OrderedTermVocabulary<OrderedTermBase> oVoc1;
	private OrderedTermVocabulary<OrderedTermBase> oVoc2;
	private OrderedTermVocabulary<OrderedTermBase> oVoc3;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		otb1 = new DerivedOrderedTermBase("otb1", "otb1Label");
		otb2 = new DerivedOrderedTermBase("term", "label");
		otb3 = new DerivedOrderedTermBase("otb3", "otb3Label");
		otbFree = new DerivedOrderedTermBase();
		oVoc1 = new OrderedTermVocabulary<OrderedTermBase>();
		oVoc1.addTerm(otb1);
		oVoc1.addTerm(otb2);
		oVoc1.addTerm(otb3);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	private class DerivedOrderedTermBase extends OrderedTermBase<DerivedOrderedTermBase>{
		private DerivedOrderedTermBase(){
			super();
		}
		private DerivedOrderedTermBase(String term, String label){
			super(term, label);
		}
	}


/*************** TESTS *************************************/
	
	@Test
	public final void testSetUp() {
		assertEquals(3, oVoc1.size());
		assertEquals(otb3, oVoc1.getLowestTerm());
		assertEquals(otb1, oVoc1.getHighestTerm());
		assertEquals(0, oVoc1.getHigherTerms(otb1).size());
		assertEquals(0, oVoc1.getLowerTerms(otb3).size());
	}

	@Test
	public final void testGetNewTermSet() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testAddTerm() {
		assertEquals(3, oVoc1.size());
		assertEquals(otb3, oVoc1.getLowestTerm());
		try {
			oVoc1.addTerm(otbFree);
		} catch (WrongTermTypeException e) {
			fail();
		}
		assertEquals(4, oVoc1.size());
		assertEquals(otbFree, oVoc1.getLowestTerm());
	}

	@Test
	public final void testRemoveTerm() {
		assertEquals(3, oVoc1.size());
		assertEquals(otb3, oVoc1.getLowestTerm());
		oVoc1.removeTerm(otb3);
		assertEquals(2, oVoc1.size());
		assertEquals(otb2, oVoc1.getLowestTerm());
	}

	@Test
	public final void testOrderedTermVocabulary() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testOrderedTermVocabularyStringStringString() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetPrecedingTerms() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetSucceedingTerms() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetPreviousTerm() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetNextTerm() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testAddTermBefore() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testAddTermAfter() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testIndexChangeAllowed() {
		logger.warn("Not yet implemented"); // TODO
	}
	
	@Test
	public final void testSize() {
		logger.warn("Not yet implemented"); // TODO
	}
}
