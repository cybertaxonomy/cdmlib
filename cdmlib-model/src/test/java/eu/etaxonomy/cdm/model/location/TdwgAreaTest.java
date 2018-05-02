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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * @author a.mueller
 * @since 23.10.2008
 */
public class TdwgAreaTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TdwgAreaTest.class);

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		new DefaultTermInitializer().initialize();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

// ************** TESTS *************************************************

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.location.TdwgArea_Old#getAreaByTdwgAbbreviation(java.lang.String)}.
	 */
	@Test
	public void testGetAreaByTdwgAbbreviation() {
		Language.ENGLISH(); // to make sure Terms are already loaded
		NamedArea area = getAreaByTdwgAbbreviation("GER");
		Assert.assertEquals("Germany", area.getLabel(Language.ENGLISH()));
		Assert.assertNull(getAreaByTdwgAbbreviation("A1R"));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.location.TdwgArea_Old#getAreaByTdwgLabel(java.lang.String)}.
	 */
	@Test
	public void testGetAreaByTdwgLabel() {
		Assert.assertEquals("Germany", getAreaByTdwgLabel("Germany").getLabel(Language.ENGLISH()));
		Assert.assertNull(getAreaByTdwgLabel("A1R"));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.location.TdwgArea_Old#isTdwgAreaLabel(java.lang.String)}.
	 */
	@Test
	public void testIsTdwgAreaLabel() {
		Assert.assertTrue(isTdwgAreaLabel("Germany"));
		Assert.assertFalse(isTdwgAreaLabel("sf2fe"));
	}

//	/**
//	 * Test method for {@link eu.etaxonomy.cdm.model.location.TdwgArea#addTdwgArea(eu.etaxonomy.cdm.model.location.NamedArea)}.
//	 */
//	@Test
//	public void testAddTdwgArea() {
//		String testAreaLabel = "TestArea";
//		Assert.assertFalse(isTdwgAreaLabel(testAreaLabel));
//		NamedArea area = NamedArea.NewInstance("", testAreaLabel, "");
//		TdwgArea.addTdwgArea(area);
//		Field labelMapField = null;
//		try {
//			labelMapField = TdwgArea.class.getDeclaredField("labelMap");
//			labelMapField.setAccessible(true);
//			Object obj = labelMapField.get(null);
//			Map<String, UUID> map = (Map<String, UUID>)obj;
//			UUID uuid = map.get(testAreaLabel.toLowerCase());
//			Assert.assertEquals(area.getUuid(), uuid);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	@Test
	public void testLoadingOfPartOfRelationships() {
		NamedArea britain = getAreaByTdwgAbbreviation("GRB");
		NamedArea northernEurope = getAreaByTdwgAbbreviation("10");
		assert britain != null : "NamedArea must exist";
		assert northernEurope != null : "NamedArea must exist";

		Assert.assertTrue("Northern Europe should include Britain",northernEurope.getIncludes().contains(britain));
		assertEquals("Britain should be part of Northern Europe",britain.getPartOf(),northernEurope);
	}

	@Test
	public void testNamedAreaLevelAssignment() {
		NamedArea britain = getAreaByTdwgAbbreviation("GRB");
		NamedArea northernEurope = getAreaByTdwgAbbreviation("10");
		assert britain != null : "NamedArea must exist";
		assert northernEurope != null : "NamedArea must exist";

		assertEquals("Northern Europe should be TDWG Level 2",northernEurope.getLevel(),NamedAreaLevel.TDWG_LEVEL2());
		assertEquals("Britain should be TDWG Level 3",britain.getLevel(),NamedAreaLevel.TDWG_LEVEL3());
	}

	@Test
	public void testUtf8(){
		DefaultTermInitializer initializer = new DefaultTermInitializer();
		initializer.initialize();
		NamedArea saoTome = NamedArea.getTdwgTermByUuid(UUID.fromString("c64e07cc-0a58-44b3-ac91-c216d1b91c1f"));
		assertEquals("Utf8 error", "S\u00E3o Tom\u00E9", saoTome.getLabel());

	}


	@Test
	public void testCompare(){
		//test compare method for set functionality. TreeSet is used by OrderedTermVocabulary therefore
		//this needs to work correctly
		TermVocabulary voc = getAreaByTdwgAbbreviation("1").getVocabulary();

		Set<NamedArea> set = new TreeSet<NamedArea>();
		NamedArea area3 = NamedArea.NewInstance();
		area3.addRepresentation(Representation.NewInstance("Spain", "Spain", "SPA", Language.DEFAULT()));
		NamedArea area4 = NamedArea.NewInstance();
		area4.addRepresentation(Representation.NewInstance("Spain6", "Spain6", "SPA-SP", Language.DEFAULT()));
		voc.addTerm(area3);
		voc.addTerm(area4);

		set.add(area3);
		set.add(area4);
		Assert.assertEquals("There must be 2 areas in the set", 2, set.size());
	}



//	@Test
//	public void getHirarchichalAreasTest(){
//		//NamedArea area0 = getAreaByTdwgLabel("Spain");
//		//System.out.println(area0.getLabel().toString());
//		//NamedArea partof = area0.getPartOf();
//		//System.out.println(partof.getLevel().getLabel());
////		NamedAreaLevel level = area0.getLevel();
////		System.out.println(level.getLabel().toString());
////		System.out.println(getAreaLabelForTDWGLevel(1, area0));
////		System.out.println(getAreaLabelForTDWGLevel(2, area0));
////		System.out.println(getAreaLabelForTDWGLevel(3, area0));
//
//		//creating levels to omit
//		Set<NamedAreaLevel> omitLevels = new HashSet<NamedAreaLevel>();
//		NamedAreaLevel level = NamedAreaLevel.TDWG_LEVEL2();
//		omitLevels.add(level);
//
//		//creating and filling the list
//		List<NamedArea> areaList = new ArrayList<NamedArea>();
//		NamedArea area1 = getAreaByTdwgLabel("Spain");
//		areaList.add(area1);
//		NamedArea area2 = getAreaByTdwgLabel("Germany");
//		areaList.add(area2);
//		NamedArea area3 = getAreaByTdwgLabel("France");
//		areaList.add(area3);
//		NamedArea area4 = getAreaByTdwgLabel("Italy");
//		areaList.add(area4);
//		NamedArea area5 = getAreaByTdwgLabel("Croatia");
//		areaList.add(area5);
//		NamedArea area6 = getAreaByTdwgLabel("Portugal");
//		areaList.add(area6);
//
//		//System.out.println(areaListToString(areaList));
//
//		NamedAreaTree tree = new NamedAreaTree();
//		tree.merge(areaList, omitLevels);
///*
//		for (NamedArea area : areaList) {
//			List<NamedArea> levelList = area.getAllLevelList();
//			tree.merge(levelList);
//			//System.out.println(areaListToString(levelList));
//		}
//*/
//		System.out.println(tree.toString());
//		tree.sortChildren();
//		System.out.println(tree.toString());
//
//		List<NamedArea> areaList2 = new ArrayList<NamedArea>();
//		NamedArea area7 = getAreaByTdwgLabel("Chita");
//		areaList2.add(area7);
//		NamedArea area8 = getAreaByTdwgLabel("Buryatiya");
//		areaList2.add(area8);
//		NamedArea area9 = getAreaByTdwgLabel("Philippines");
//		areaList2.add(area9);
//
//		tree.merge(areaList2, omitLevels);
//		tree.sortChildren();
//		System.out.println(tree.toString());
//
//		List<Distribution> distList = new ArrayList<Distribution>();
//		Distribution dist1 = Distribution.NewInstance(area1, null);
//		distList.add(dist1);
//		Distribution dist2 = Distribution.NewInstance(area2, null);
//		distList.add(dist2);
//		Distribution dist3 = Distribution.NewInstance(area3, null);
//		distList.add(dist3);
//		Distribution dist4 = Distribution.NewInstance(area4, null);
//		distList.add(dist4);
//		Distribution dist5 = Distribution.NewInstance(area5, null);
//		distList.add(dist5);
//		Distribution dist6 = Distribution.NewInstance(area6, null);
//		distList.add(dist6);
//
//		DistributionTree distTree = new DistributionTree();
//		distTree.merge(distList, omitLevels);
//
//		List<Distribution> distList2 = new ArrayList<Distribution>();
//		Distribution dist7 = Distribution.NewInstance(area7, null);
//		distList2.add(dist7);
//		Distribution dist8 = Distribution.NewInstance(area8, null);
//		distList2.add(dist8);
//		Distribution dist9 = Distribution.NewInstance(area9, null);
//		distList2.add(dist9);
//		distTree.merge(distList2, omitLevels);
//		distTree.sortChildren();
//		System.out.println("## DISTRIBUTION TREE ##");
//		System.out.println(distTree.toString());
//
//	}
//


/*
	private void print(NamedAreaNode result) {
		System.out.print("{" + (result == null? "" :result.toString()));
		for (LevelNode levelNode :  result.levelList){
			System.out.print("[" + levelNode.toString()  );
			for (NamedAreaNode area : levelNode.areaList){
				print(area);
			}
			System.out.print("]" );
		}
		System.out.print("}");
	}
*/
	public String areaListToString (List<NamedArea> list){
		String result = "";
		for (NamedArea namedArea : list) {
			result = result + " [" + namedArea.getLabel().toString() + ", " + namedArea.getLevel().getLabel().toString() + "]";
		}
		return result;
	}

	//PRE: in parameter tdwg can not be smaller than the current tdwglevel of the second parameter
	public static NamedArea getAreaLabelForTDWGLevel(Integer tdwgLevel, NamedArea area){
		NamedArea aux = area;
		while (aux.getLevel().getLabel().toString().compareTo("TDWG Level " + tdwgLevel.toString()) != 0) {
			aux = aux.getPartOf();
		}
		return aux;
	}


	private NamedArea getAreaByTdwgAbbreviation(String tdwgAbbrev){
		return NamedArea.getAreaByTdwgAbbreviation(tdwgAbbrev);
	}

	private NamedArea getAreaByTdwgLabel(String tdwgLabel){
		return NamedArea.getAreaByTdwgLabel(tdwgLabel);
	}

	private boolean isTdwgAreaLabel(String tdwgLabel){
		return NamedArea.isTdwgAreaLabel(tdwgLabel);
	}

}
