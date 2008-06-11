/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
 
package eu.etaxonomy.cdm.model.common;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

public class OrderedTermBaseTest extends EntityTestBase {
	private static final Logger logger = Logger.getLogger(OrderedTermBaseTest.class);

	private OrderedTermBase otb1;
	private OrderedTermBase otb2;
	private OrderedTermBase otb3;
	private OrderedTermBase otb4;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		otb1 = new DerivedOrderedTermBase();
		otb2 = new DerivedOrderedTermBase("term", "label", null);
		otb3 = new DerivedOrderedTermBase();
		otb4 = new DerivedOrderedTermBase();
		
		otb1.orderIndex = 1;
		otb2.orderIndex = 4;
		otb3.orderIndex = 4;
		otb4.orderIndex = 5;
	}

	@After
	public void tearDown() throws Exception {
	}
	
	private class DerivedOrderedTermBase extends OrderedTermBase<DerivedOrderedTermBase>{
		private DerivedOrderedTermBase(){
			super();
		}
		private DerivedOrderedTermBase(String term, String label, String labelAbbrev){
			super(term, label, labelAbbrev);
		}
		
		
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
		int comp = otb1.compareTo(otb2);
		assertTrue( comp > 0  );
		assertTrue(otb1.compareTo(otb1) == 0  );
		assertTrue(otb2.compareTo(otb3) == 0  );
		assertTrue(otb3.compareTo(otb1) < 0  );
	
		comp = Rank.GENUS().compareTo(Rank.SPECIES());
		assertTrue( comp > 0  );
		assertTrue(Rank.GENUS().compareTo(Rank.GENUS()) == 0  );
		assertTrue(Rank.FAMILY().compareTo(Rank.KINGDOM()) < 0  );
	}
	

	@Test
	public final void testDecreaseVoc() {
		OrderedTermVocabulary<OrderedTermBase> voc = new OrderedTermVocabulary();
		int before = otb1.orderIndex;
		otb1.decreaseIndex(voc);
		int after = otb1.orderIndex;
		assertEquals(before, after);
	}

	@Test
	public final void testIncrementVoc() {
		OrderedTermVocabulary<OrderedTermBase> voc = new OrderedTermVocabulary();
		assertFalse(voc.indexChangeAllowed(otb1));
		int before = otb1.orderIndex;
		otb1.incrementIndex(voc);
		int after = otb1.orderIndex;
		assertEquals(before, after);
	}

}
