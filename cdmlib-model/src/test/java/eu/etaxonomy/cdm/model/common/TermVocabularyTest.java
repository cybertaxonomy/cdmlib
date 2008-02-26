package eu.etaxonomy.cdm.model.common;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;


public class TermVocabularyTest extends EntityTestBase {
	private static Logger logger = Logger.getLogger(TermVocabularyTest.class);

	private DefinedTermBase dtb1;
	private DefinedTermBase dtb2;
	private DefinedTermBase dtb3;
	private DefinedTermBase dtbFree;
	private TermVocabulary<DefinedTermBase> voc1;
	private TermVocabulary<DefinedTermBase> voc2;
	private TermVocabulary<DefinedTermBase> voc3;

	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		dtb1 = new DerivedDefinedTermBase("otb1", "high");
		dtb2 = new DerivedDefinedTermBase("term", "middel");
		dtb3 = new DerivedDefinedTermBase("otb3", "low");
		dtbFree = new DerivedDefinedTermBase();
		voc1 = new TermVocabulary<DefinedTermBase>();
		voc1.addTerm(dtb1);
		voc1.addTerm(dtb2);
		voc1.addTerm(dtb3);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	private class DerivedDefinedTermBase extends OrderedTermBase<DerivedDefinedTermBase>{
		private DerivedDefinedTermBase(){
			super();
		}
		private DerivedDefinedTermBase(String term, String label){
			super(term, label);
		}
	}
	
/****************** TESTS ****************************************/
	
	@Test
	public final void testSetUp() {
		assertEquals(3, voc1.size());
		assertEquals(3, voc1.getTerms().size());
	}
	
	@Test
	public final void testGetNewTermSet() {
		assertNotNull(voc1.getNewTermSet());
		assertTrue(Set.class.isAssignableFrom(voc1.getNewTermSet().getClass()));
	}

	@Test
	public final void testTermVocabulary() {
		assertNotNull(voc1);
	}

	@Test
	public final void testTermVocabularyStringStringString() {
		voc2 = new TermVocabulary<DefinedTermBase>("term", "label", "termSourceUri");
		assertEquals("label", voc2.getLabel());	
	}

	@Test
	
	public final void testGetTerms() {
		assertEquals(3, voc1.getTerms().size());
		//assertNotSame(voc1.terms, voc1.getTerms());
		assertTrue( voc1.terms.getClass().isAssignableFrom(voc1.getTerms().getClass()));
	}

	@Test
	public final void testSetTerms() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testAddTerm() {
		try {
			voc1.addTerm(dtbFree);
		} catch (WrongTermTypeException e) {
			fail();
		}
		assertEquals(4, voc1.size());
		
	}
	
	@Test
	public final void testAddTerm_2() {
		Rank rank = Rank.SPECIES();
		voc2 = new TermVocabulary<DefinedTermBase>();
		try {
			voc2.addTerm(rank);
		} catch (WrongTermTypeException e) {
			fail();
		}
		Language lang = Language.ENGLISH();
		try {
			voc2.addTerm(lang);
			fail("Exception should be thrown");
		} catch (WrongTermTypeException e) {
			assertTrue(true);
		}
	}
	
	@Test
	public final void testRemoveTerm() {
		assertEquals(3, voc1.size());
		assertTrue(voc1.getTerms().contains(dtb3));
		voc1.removeTerm(dtb3);
		assertFalse(voc1.getTerms().contains(dtb3));
		assertEquals(2, voc1.size());
		voc1.removeTerm(dtb3);
		assertEquals(2, voc1.size());
		assertTrue(voc1.getTerms().contains(dtb1));
		voc1.removeTerm(dtb1);
		assertFalse(voc1.getTerms().contains(dtb1));
		assertEquals(1, voc1.size());
		assertTrue(voc1.getTerms().contains(dtb2));
		voc1.removeTerm(dtb2);
		assertFalse(voc1.getTerms().contains(dtb2));
		assertEquals(0, voc1.size());
	}

	@Test
	public final void testGetTermSourceUri() {
		assertEquals(null, voc1.getTermSourceUri());
		voc2 = new TermVocabulary<DefinedTermBase>("term", "label", "uri");
		assertEquals("uri", voc2.getTermSourceUri());
	}

	@Test
	public final void testSetTermSourceUri() {
		voc1.setTermSourceUri("uri");
		assertEquals("uri", voc1.getTermSourceUri());
	}

	@Test
	public final void testGetTermClass() {
		assertEquals(dtb1.getClass(), voc1.getTermClass());
	}

	@Test
	public final void testIterator() {
		Iterator<DefinedTermBase> it = voc1.iterator();
		int i = 0;
		while (it.hasNext()){
			i++;
			assertTrue(voc1.getTerms().contains(it.next()));
		}
		assertEquals(3, i);
	}
}
