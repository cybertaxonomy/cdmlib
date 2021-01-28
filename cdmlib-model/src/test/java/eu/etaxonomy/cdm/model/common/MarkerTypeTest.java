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
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Test;

import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 * @since 23.04.2008
 */
public class MarkerTypeTest extends EntityTestBase {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(MarkerTypeTest.class);

	private static final UUID uuidImported = UUID.fromString("96878790-4ceb-42a2-9738-a2242079b679");
	private static final UUID uuidToBeChecked = UUID.fromString("34204192-b41d-4857-a1d4-28992bef2a2a");
	private static final UUID uuidIsDoubtful = UUID.fromString("b51325c8-05fe-421a-832b-d86fc249ef6e");
	private static final UUID uuidComplete = UUID.fromString("b4b1b2ab-89a8-4ce6-8110-d60b8b1bc433");

/* ************ TESTS **********************************/

	@Test
	public void testNewInstanceStringString() {
		String term = "term";
		String label = "label";
		MarkerType markerType = MarkerType.NewInstance(term, label, null);
		assertNotNull(markerType);
		assertEquals(label, markerType.getLabel());
		assertEquals(term, markerType.getRepresentation(Language.DEFAULT()).getText());
	}

	@Test
	public void testIMPORTED() {
		assertNotNull(MarkerType.IMPORTED());
		assertEquals(uuidImported,  MarkerType.IMPORTED().getUuid());
	}

	@Test
	public void testTO_BE_CHECKED() {
		assertEquals(uuidToBeChecked,  MarkerType.TO_BE_CHECKED().getUuid());
	}

	@Test
	public void testIS_DOUBTFUL() {
		assertEquals(uuidIsDoubtful,  MarkerType.IS_DOUBTFUL().getUuid());
	}

	@Test
	public void testCOMPLETE() {
		assertEquals(uuidComplete,  MarkerType.COMPLETE().getUuid());
	}
}