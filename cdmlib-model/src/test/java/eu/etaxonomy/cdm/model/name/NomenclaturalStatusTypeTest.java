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
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.babadshanjan
 * @created 25.03.2009
 * @version 1.0
 */
public class NomenclaturalStatusTypeTest extends EntityTestBase {
	private static final Logger logger = Logger
			.getLogger(NomenclaturalStatusTypeTest.class);

	private static final UUID uuidDoubtful = UUID.fromString("0ffeb39e-872e-4c0f-85ba-a4150d9f9e7d");

	@BeforeClass
	public static void setUp() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
	}
	
	@Test
	public void testNomenclaturalStatusType() {
		NomenclaturalStatusType term = new NomenclaturalStatusType();
		assertNotNull(term);
	}
	
	@Test
	public void testNomenclaturalStatusTypeStringString() {
		NomenclaturalStatusType term = new NomenclaturalStatusType("term", "label", null);
		assertEquals("label", term.getLabel());
	}
	
	@Test
	public void testDOUBTFUL() {
		assertEquals(uuidDoubtful,  NomenclaturalStatusType.DOUBTFUL().getUuid());	
	}
}
