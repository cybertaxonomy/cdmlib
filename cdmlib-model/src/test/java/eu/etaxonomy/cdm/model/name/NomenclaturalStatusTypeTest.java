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
 * @since 25.03.2009
 */
public class NomenclaturalStatusTypeTest extends EntityTestBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NomenclaturalStatusTypeTest.class);

	private static final UUID uuidDoubtful = UUID.fromString("0ffeb39e-872e-4c0f-85ba-a4150d9f9e7d");
	private static final UUID uuidCombNov = UUID.fromString("ed508710-deef-44b1-96f6-1ce6d2c9c884");
	private static final UUID uuidNotAvailable = UUID.fromString("6d9ed462-b761-4da3-9304-4749e883d4eb");
	

	@BeforeClass
	public static void setUp() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
	}
	
	@Test
	public void testNomenclaturalStatusTypeStringString() {
		NomenclaturalStatusType term = NomenclaturalStatusType.NewInstance("term", "label", null);
		assertEquals("label", term.getLabel());
	}
	
	@Test
	public void testDoubtful() {
		assertEquals(uuidDoubtful,  NomenclaturalStatusType.DOUBTFUL().getUuid());	
	}
	
	
	@Test
	public void testCombNov() {
		assertEquals(uuidCombNov,  NomenclaturalStatusType.COMB_NOV().getUuid());	
	}
	
	@Test
	public void testNotAvailable() {
		assertEquals(uuidNotAvailable,  NomenclaturalStatusType.ZOO_NOT_AVAILABLE().getUuid());	
	}
}
