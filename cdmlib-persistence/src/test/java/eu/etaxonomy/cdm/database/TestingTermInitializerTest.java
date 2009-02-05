package eu.etaxonomy.cdm.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

public class TestingTermInitializerTest extends CdmIntegrationTest {
	
	@Test
	public void testInit() {		
		assertNotNull("TermInitializer should have initialized Language.DEFAULT",Language.DEFAULT());
		assertEquals("Language.DEFAULT should equal Language.ENGLISH",Language.DEFAULT(),Language.ENGLISH());
	}
	
	@Test
	public void testMarkerTypeIds() {
		assertEquals("We expect MarkerType.TO_BE_CHECKED to have an id of 893",893,MarkerType.TO_BE_CHECKED().getId());
	}
	
	@Test
	public void testFeatureIds() {
		assertEquals("We expect Feature.ECOLOGY to have an id of 922",922,Feature.ECOLOGY().getId());
	}
}
