/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

public class OrderedTermBaseTest extends EntityTestBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(OrderedTermBaseTest.class);

	private OrderedTermBase otb1;
	private OrderedTermBase otb2;
	private OrderedTermBase otb3;
	private OrderedTermBase otb4;

	@BeforeClass
	public static void setUpBeforeClass() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
	}

	@Before
	public void setUp() throws Exception {
		otb1 = new DerivedOrderedTermBase();
		otb2 = new DerivedOrderedTermBase(TermType.Unknown, "term", "label", null);
		otb3 = new DerivedOrderedTermBase();
		otb4 = new DerivedOrderedTermBase();

		otb1.orderIndex = 1;
		otb2.orderIndex = 4;
		otb3.orderIndex = 4;
		otb4.orderIndex = 5;
	}

	private class DerivedOrderedTermBase extends OrderedTermBase<DerivedOrderedTermBase>{
		private DerivedOrderedTermBase(){
			super(TermType.Unknown);
		}
		private DerivedOrderedTermBase(TermType type, String term, String label, String labelAbbrev){
			super(type, term, label, labelAbbrev);
		}
		@Override
		protected void setDefaultTerms(TermVocabulary<DerivedOrderedTermBase> termVocabulary) {}
		@Override
		public void resetTerms() {};
	}

/************ TESTS *************************************/

	@Test
	public final void testOrderedTermBase() {
		assertNotNull(otb1);
	}

	@Test
	public final void testOrderedTermBaseStringString() {
		assertNotNull(otb2);
		assertEquals("label", otb2.getLabel());
		//TODO assertEquals("term", otb2.getD);
	}

	@Test
	public final void testCompareTo() {
		//since an exception is thrown when comparing OrderedTermBase that do not belong
		//to the same vocabulary this dummy vocabulary is added
		OrderedTermVocabulary<OrderedTermBase<?>> voc = new OrderedTermVocabulary();
		otb1.vocabulary = voc;
		otb2.vocabulary = voc;
		otb3.vocabulary = voc;

		int comp = otb1.compareTo(otb2);
		assertTrue("expected:  1 > 4", comp > 0  );
		assertTrue("expected:  1 = 1", otb1.compareTo(otb1) == 0  );
		assertTrue("expected:  4 = 4", otb2.compareTo(otb3) == 0  );
		assertTrue("expected:  5 < 1", otb3.compareTo(otb1) < 0  );

		Rank genus = Rank.GENUS();
		Rank species = Rank.SPECIES();
		Rank kingdom = Rank.KINGDOM();
		Rank family = Rank.FAMILY();

		comp = genus.compareTo(species);
		assertTrue( comp > 0  );
		assertTrue(genus.compareTo(genus) == 0  );
		assertTrue(family.compareTo(kingdom) < 0  );
	}


	@Test
	public final void testDecreaseVoc() {
		OrderedTermVocabulary<OrderedTermBase<?>> voc = new OrderedTermVocabulary();
		int before = otb1.orderIndex;
		otb1.decreaseIndex(voc);
		int after = otb1.orderIndex;
		assertEquals(before, after);
	}

	@Test
	public final void testIncrementVoc() {
		OrderedTermVocabulary<OrderedTermBase<?>> voc = new OrderedTermVocabulary();
		assertFalse(voc.indexChangeAllowed(otb1));
		int before = otb1.orderIndex;
		otb1.incrementIndex(voc);
		int after = otb1.orderIndex;
		assertEquals(before, after);
	}

}
