/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;

/**
 * @author a.babadshanjan
 * @since 19.05.2009
 * @version 1.0
 */
public class NameTypeDesignationTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(NameTypeDesignationTest.class);

	private NameTypeDesignation term1 = null;
	private NameTypeDesignation term2 = null;

	@BeforeClass
	public static void setUpBeforeClass() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
	}

	@Before
	public void setUp() {
		term1 = new NameTypeDesignation();
		term2 = new NameTypeDesignation(TaxonNameFactory.NewZoologicalInstance(null), null, null, null, null, true, false, false);
	}

	@Test
	public void testNameTypeDesignation() {
		assertNotNull(term1);
		assertNotNull(term2);
	}

	@Test
	public void testGetTypeStatus() {
		term1.setTypeStatus(NameTypeDesignationStatus.TAUTONYMY());
		assertEquals(term1.getTypeStatus(), NameTypeDesignationStatus.TAUTONYMY());
		assertTrue(term1.getTypeStatus().isInstanceOf(NameTypeDesignationStatus.class));
	}
}
