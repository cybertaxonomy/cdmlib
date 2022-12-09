/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.SortedSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

public class OrderedTermVocabularyTest extends EntityTestBase {

    @SuppressWarnings("unused")
    private static Logger logger = LogManager.getLogger();

	private OrderedTermBase<?> otb1;
	private OrderedTermBase<?> otb2;
	private OrderedTermBase<?> otb3;
	private OrderedTermBase<?> otbFree;
	private OrderedTermVocabulary<OrderedTermBase<?>> oVoc1;
	private OrderedTermVocabulary<OrderedTermBase<?>> oVoc2;

	@Before
	public void setUp() throws Exception {
		otb1 = new DerivedOrderedTermBase(TermType.Unknown,"otb1", "high", null, 1);
		otb2 = new DerivedOrderedTermBase(TermType.Unknown, "term", "middel", null, 2);
		otb3 = new DerivedOrderedTermBase(TermType.Unknown, "otb3", "low", null, 3);
		otbFree = new DerivedOrderedTermBase();
		oVoc1 = OrderedTermVocabulary.NewInstance(TermType.Unknown);
		oVoc1.addTerm(otb1);
		oVoc1.addTerm(otb2);
		oVoc1.addTerm(otb3);
	}

	private class DerivedOrderedTermBase extends OrderedTermBase<DerivedOrderedTermBase>{
        private static final long serialVersionUID = -6661559531712274867L;
        private DerivedOrderedTermBase(){
			super(TermType.Unknown);
		}
		private DerivedOrderedTermBase(TermType type, String term, String label, String labelAbbrev, int id){
			super(type, term, label, labelAbbrev);
			this.setId(id);
		}
		@Override
		protected void setDefaultTerms(TermVocabulary<DerivedOrderedTermBase> termVocabulary) {}
		@Override
		public void resetTerms() {}
	}

//*************************** TESTS *************************************/

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
		assertNotNull(oVoc1.newTermSet());
		assertTrue(SortedSet.class.isAssignableFrom(oVoc1.newTermSet().getClass()));
	}

	@Test
	public final void testGetTerms() {
		assertEquals(3, oVoc1.getTerms().size());
//		assertNotSame(oVoc1.terms, oVoc1.getTerms());
		assertTrue( oVoc1.terms.getClass().isAssignableFrom(oVoc1.getTerms().getClass()));
	}

	@Test
	public final void testAddTerm() {
		assertEquals(3, oVoc1.size());
		assertEquals(otb3, oVoc1.getLowestTerm());
		oVoc1.addTerm(otbFree);

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
		oVoc1.removeTerm(otb1);
		assertEquals(1, oVoc1.size());
		assertEquals(otb2, oVoc1.getLowestTerm());
		assertEquals(otb2, oVoc1.getHighestTerm());
		oVoc1.removeTerm(otb2);
		assertEquals(0, oVoc1.size());
		assertEquals(null, oVoc1.getHighestTerm());
	}

	@Test
	public final void testOrderedTermVocabulary() {
		assertNotNull(oVoc1);
	}

	@Test
	public final void testOrderedTermVocabularyStringStringString() {
		oVoc2 = new OrderedTermVocabulary<>(TermType.Unknown, "term", "label", null, URI.create("http://term.Source.Uri"), null);
		assertEquals("label", oVoc2.getLabel());
	}

	@Test
	public final void testGetLowerTerms() {
		assertEquals(0, oVoc1.getLowerTerms(otb3).size());
		assertEquals(1, oVoc1.getLowerTerms(otb2).size());
		assertEquals(2, oVoc1.getLowerTerms(otb1).size());
		assertEquals(otb2, oVoc1.getLowerTerms(otb1).last());
	}

	@Test
	public final void testGetHigherTerms() {
		assertEquals(2, oVoc1.getHigherTerms(otb3).size());
		assertEquals(1, oVoc1.getHigherTerms(otb2).size());
		assertEquals(0, oVoc1.getHigherTerms(otb1).size());
		assertEquals(otb2, oVoc1.getHigherTerms(otb3).first());
	}

	@Test
	public final void testGetNextHigherTerm() {
		assertEquals(otb2.getLabel(), oVoc1.getNextHigherTerm(otb3).getLabel());
		assertEquals(null, oVoc1.getNextHigherTerm(otb1));
	}

	@Test
	public final void testGetNextLowerTerm() {
		assertEquals(otb2.getLabel(), oVoc1.getNextLowerTerm(otb1).getLabel());
		assertEquals(null, oVoc1.getNextLowerTerm(otb3));
	}

	@Test
	public final void testAddTermAbove() {
		try {
			oVoc1.addTermAbove(otbFree, otb2);
		} catch (Exception e) {
			fail();
		}
		assertEquals(2, oVoc1.getLowerTerms(otbFree).size());
		assertEquals(otbFree.getLabel(), oVoc1.getNextLowerTerm(otb1).getLabel());
		assertEquals(otbFree.getLabel(), oVoc1.getNextHigherTerm(otb2).getLabel());
	}

	@Test
	public final void testAddTermBelow() {
		try {
			oVoc1.addTermBelow(otbFree, otb2);
		} catch (Exception e) {
			fail();
		}
		assertEquals(1, oVoc1.getLowerTerms(otbFree).size());
		assertEquals(otbFree.getLabel(), oVoc1.getNextLowerTerm(otb2).getLabel());
		assertEquals(otbFree.getLabel(), oVoc1.getNextHigherTerm(otb3).getLabel());
	}

	@Test
	public final void testIndexChangeAllowed() {
//		assertFalse(oVoc1.indexChangeAllowed(otb1));
	}

	@Test
	public final void testSize() {
		assertEquals(3, oVoc1.size());
		oVoc2 = new OrderedTermVocabulary<>();
		assertEquals(0, oVoc2.size());
	}
}
