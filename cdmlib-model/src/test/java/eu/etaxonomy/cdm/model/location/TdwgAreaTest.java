/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.location;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;

/**
 * @author a.mueller
 * @created 23.10.2008
 * @version 1.0
 */
public class TdwgAreaTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TdwgAreaTest.class);

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.location.TdwgArea#getAreaByTdwgAbbreviation(java.lang.String)}.
	 */
	@Test
	public void testGetAreaByTdwgAbbreviation() {
//		Language.ENGLISH(); // to make sure Terms are already loaded
//		NamedArea area = TdwgArea.getAreaByTdwgAbbreviation("GER");
//		Assert.assertEquals("Germany", area.getLabel(Language.ENGLISH()));
//		Assert.assertNull(TdwgArea.getAreaByTdwgAbbreviation("A1R"));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.location.TdwgArea#getAreaByTdwgLabel(java.lang.String)}.
	 */
	@Test
	public void testGetAreaByTdwgLabel() {
//		Assert.assertEquals("Germany", TdwgArea.getAreaByTdwgLabel("Germany").getLabel(Language.ENGLISH()));
//		Assert.assertNull(TdwgArea.getAreaByTdwgLabel("A1R"));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.location.TdwgArea#isTdwgAreaLabel(java.lang.String)}.
	 */
	@Test
	public void testIsTdwgAreaLabel() {
//		Assert.assertTrue(TdwgArea.isTdwgAreaLabel("Germany"));
//		Assert.assertFalse(TdwgArea.isTdwgAreaLabel("sf2fe"));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.location.TdwgArea#addTdwgArea(eu.etaxonomy.cdm.model.location.NamedArea)}.
	 */
	@Test
	public void testAddTdwgArea() {
//		String testAreaLabel = "TestArea";
//		Assert.assertFalse(TdwgArea.isTdwgAreaLabel(testAreaLabel));
//		NamedArea area = NamedArea.NewInstance("", testAreaLabel, "");
//		TdwgArea.addTdwgArea(area);
//		Field labelMapField = null;
//		try {
//			labelMapField = TdwgArea.class.getDeclaredField("labelMap");
//			labelMapField.setAccessible(true);
//			Object obj = labelMapField.get(null);
//			Map<String, UUID> map = (Map<String, UUID>)obj;
//			Assert.assertEquals(area.getUuid(), map.get(testAreaLabel));
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	@Test
	public void testLoadingOfPartOfRelationships() {
//		NamedArea britain = TdwgArea.getAreaByTdwgAbbreviation("GRB");
//		NamedArea northernEurope = TdwgArea.getAreaByTdwgAbbreviation("10");
//		assert britain != null : "NamedArea must exist";
//		assert northernEurope != null : "NamedArea must exist";
//		
//		assertTrue("Northern Europe should include Britain",northernEurope.getIncludes().contains(britain));
//		assertEquals("Britain should be part of Northern Europe",britain.getPartOf(),northernEurope);
	}
	
	@Test
	public void testNamedAreaLevelAssignment() {
//		NamedArea britain = TdwgArea.getAreaByTdwgAbbreviation("GRB");
//		NamedArea northernEurope = TdwgArea.getAreaByTdwgAbbreviation("10");
//		assert britain != null : "NamedArea must exist";
//		assert northernEurope != null : "NamedArea must exist";
//		
//		assertEquals("Northern Europe should be TDWG Level 2",northernEurope.getLevel(),NamedAreaLevel.TDWG_LEVEL2());
//		assertEquals("Britain should be TDWG Level 3",britain.getLevel(),NamedAreaLevel.TDWG_LEVEL3());
	}
	
	@Test
	public void testUtf8(){
		DefaultTermInitializer initializer = new DefaultTermInitializer();
		initializer.initialize();
		NamedArea saoTome = TdwgArea.getTermByUuid(UUID.fromString("c64e07cc-0a58-44b3-ac91-c216d1b91c1f"));
		assertEquals("Utf8 error", "S\u00E3o Tom\u00E9", saoTome.getLabel());
		
	}	
}
