// $Id$
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
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.babadshanjan
 * @created 24.03.2009
 * @version 1.0
 */
public class NameTypeDesignationStatusTest extends EntityTestBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NameTypeDesignationStatusTest.class);
	
	private static final UUID uuidAutomatic = UUID.fromString("e89d8b21-615a-4602-913f-1625bf39a69f");
	private static final UUID uuidFirstRevisor = UUID.fromString("a14ec046-c48f-4a73-939f-bd57880c7565");
	private static final UUID uuidMonotypy = UUID.fromString("3fc639b2-9a64-45f8-9a81-657a4043ad74");
	private static final UUID uuidNotApplicable = UUID.fromString("91a9d6a9-7754-41cd-9f7e-be136f599f7e");
	private static final UUID uuidOriginalDesignation = UUID.fromString("40032a44-973b-4a64-b25e-76f86c3a753c");
	private static final UUID uuidPresentDesignation = UUID.fromString("e5f38f5d-995d-4470-a036-1a9792a543fc");
	private static final UUID uuidSubsequentMonotypy = UUID.fromString("2b5806d8-31b0-406e-a32a-4adac0c89ae4");
	private static final UUID uuidSubsequentDesignation = UUID.fromString("3e449e7d-a03c-4431-a7d3-aa258406f6b2");
	private static final UUID uuidTautonymy = UUID.fromString("84521f09-3e10-43f5-aa6f-2173a55a6790");
	
	@BeforeClass
	public static void setUp() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
	}
	
	@Test
	public void testNameTypeDesignationStatusStringString() {
		NameTypeDesignationStatus term = new NameTypeDesignationStatus("term", "label", null);
		assertEquals("label", term.getLabel());
	}
	
	@Test
	public void testAUTOMATIC() {
		assertEquals(uuidAutomatic,  NameTypeDesignationStatus.AUTOMATIC().getUuid());	
	}

	@Test
	public void testMONOTYPY() {
		assertEquals(uuidMonotypy,  NameTypeDesignationStatus.MONOTYPY().getUuid());	
	}

	@Test
	public void testNOT_APPLICABLE() {
		assertEquals(uuidNotApplicable,  NameTypeDesignationStatus.NOT_APPLICABLE().getUuid());	
	}

	@Test
	public void testORIGINAL_DESIGNATION() {
		assertEquals(uuidOriginalDesignation,  NameTypeDesignationStatus.ORIGINAL_DESIGNATION().getUuid());	
	}

	@Test
	public void testPRESENT_DESIGNATION() {
		assertEquals(uuidPresentDesignation,  NameTypeDesignationStatus.PRESENT_DESIGNATION().getUuid());	
	}

	@Test
	public void testSUBSEQUENT_MONOTYPY() {
		assertEquals(uuidSubsequentMonotypy,  NameTypeDesignationStatus.SUBSEQUENT_MONOTYPY().getUuid());	
	}

	@Test
	public void testSUBSEQUENT_DESIGNATION() {
		assertEquals(uuidSubsequentDesignation,  NameTypeDesignationStatus.SUBSEQUENT_DESIGNATION().getUuid());	
	}

	@Test
	public void testTAUTONOMY() {
		assertEquals(uuidTautonymy,  NameTypeDesignationStatus.TAUTONYMY().getUuid());	
	}

}
