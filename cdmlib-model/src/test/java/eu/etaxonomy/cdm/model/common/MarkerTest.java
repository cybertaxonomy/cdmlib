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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 * @since 22.04.2008
 */
public class MarkerTest extends EntityTestBase {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private static boolean flag1;
	private static Marker marker1;
	private static MarkerType markerType1;
	private static AnnotatableEntity annotatedObject1;

	@Before
	public void setUp() throws Exception {
		flag1 = true;
		markerType1 = MarkerType.TO_BE_CHECKED();
		marker1 = Marker.NewInstance(markerType1 ,  flag1);
		annotatedObject1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		annotatedObject1.addMarker(marker1);
	}

/* *************** TESTS ********************************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Marker#NewInstance(eu.etaxonomy.cdm.model.common.MarkerType, boolean)}.
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Marker#Marker(eu.etaxonomy.cdm.model.common.MarkerType, boolean)}.
	 */
	@Test
	public void testNewInstance() {
		assertNotNull(marker1);
		assertEquals(flag1, marker1.getFlag());
		assertSame(markerType1, marker1.getMarkerType());
		assertSame(1, annotatedObject1.getMarkers().size());
		assertSame(annotatedObject1.getMarkers().toArray()[0], marker1);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Marker#getType()}.
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Marker#setType(eu.etaxonomy.cdm.model.common.MarkerType)}.
	 */
	@Test
	public void testGetSetType() {
		marker1.setMarkerType(MarkerType.IS_DOUBTFUL());
		assertSame(MarkerType.IS_DOUBTFUL(), marker1.getMarkerType());
		marker1.setMarkerType(null);
		assertNull(marker1.getMarkerType());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Marker#getFlag()}.
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Marker#setFlag(boolean)}.
	 */
	@Test
	public void testGetSetFlag() {
		marker1.setFlag(true);
		assertTrue(marker1.getFlag());
		marker1.setFlag(false);
		assertFalse(marker1.getFlag());
	}

}
