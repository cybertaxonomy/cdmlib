/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.location;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.NamedArea.LevelNode;
import eu.etaxonomy.cdm.model.location.NamedArea.NamedAreaNode;

/**
 * @author a.mueller
 \* @since 26.05.2011
 *
 */
public class NamedAreaTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NamedAreaTest.class);
	
	private NamedArea namedArea1;
	private NamedAreaLevel level1;
	private NamedAreaType areaType1;
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DefaultTermInitializer initializer = new DefaultTermInitializer();
		initializer.initialize();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		namedArea1 = NamedArea.NewInstance("Description for Named Area 1", "Named Area 1", "NA1");
		level1 = NamedAreaLevel.NewInstance("Description for level 1", "Level 1", "L1");
		namedArea1.setLevel(level1);
	}
	
	@Test
	public void NewInstanceStringStringStringTest(){
		Assert.assertEquals("Description for Named Area 1", namedArea1.getDescription());
		Assert.assertEquals("Named Area 1", namedArea1.getLabel());
		Assert.assertEquals("NA1", namedArea1.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel());
	}

	
	@Test
	public void labelWithLevelTest(){
		Assert.assertEquals("Named Area 1 - Level 1", NamedArea.labelWithLevel(namedArea1, Language.DEFAULT()));
		Assert.assertEquals("Germany - Country", NamedArea.labelWithLevel(Country.GERMANY(), Language.DEFAULT()));
		Assert.assertEquals("Germany - TDWG Level 3", NamedArea.labelWithLevel(getAreaByTdwgAbbreviation("GER"), Language.DEFAULT()));
		NamedArea namedArea2 = NamedArea.NewInstance("Description for Named Area 2", "", "NA2");
		Assert.assertEquals("NA2 - NamedArea", NamedArea.labelWithLevel(namedArea2, Language.DEFAULT()));
		NamedArea namedArea3 = NamedArea.NewInstance("Description for Named Area 3", null, " ");
		Assert.assertEquals("Description for Named Area 3 - NamedArea", NamedArea.labelWithLevel(namedArea3, Language.DEFAULT()));
		
		//TODO include Vocabulay information
	}
	
	@Test
	public void getHiearchieListTest(){
		//Create example data
		List<NamedArea> list = new ArrayList<NamedArea>(); 
		NamedArea germanyL4 = getAreaByTdwgAbbreviation("GER-OO");
		Assert.assertNotNull("Prerequisite: Germany should not be null", germanyL4);
		list.add(germanyL4);
		NamedArea franceL3 = getAreaByTdwgAbbreviation("FRA");
		Assert.assertNotNull("Prerequisite: France should not be null", germanyL4);
		list.add(franceL3);
		NamedArea europe = getAreaByTdwgAbbreviation("1");
		NamedArea middleEurope = getAreaByTdwgAbbreviation("11");
//		System.out.println(middleEurope.getLabel());
		NamedArea southWestEurope = getAreaByTdwgAbbreviation("12");
//		System.out.println(southWestEurope.getLabel());
		NamedArea germanyL3 = getAreaByTdwgAbbreviation("GER");
		
		NamedArea newArea1 = NamedArea.NewInstance("New Area1 Description", "New Area1", "NA1");
		list.add(newArea1);
		NamedArea newArea2 = NamedArea.NewInstance("New Area2 Description", "New Area2", "NA2");
		NamedAreaLevel newLevel = NamedAreaLevel.NewInstance("New Level Description", "New level", "NL");
		newArea2.setLevel(newLevel);
		list.add(newArea2);
		
		NamedArea newGermanSubAreaAndLevel = NamedArea.NewInstance("New German Level 3 subarea", "New GER subarea", "GER-L5");
		NamedAreaLevel newGermanLevel5 = NamedAreaLevel.NewInstance("New German Level 5 Description", "GER Level 5", "GERL5");
		newGermanSubAreaAndLevel.setLevel(newGermanLevel5);
		germanyL3.addIncludes(newGermanSubAreaAndLevel);
//		germanyL3.getLevel().addIncludes(newGermanLevel5);
		list.add(newGermanSubAreaAndLevel);
		
		
		
		
		NamedAreaNode root = NamedArea.getHiearchieList(list);
		
		//level 0
		Assert.assertNull("Root should not have an area", root.area);
		//level1
		Assert.assertEquals("level1 list should include 3 levels", 3, root.levelList.size());
		LevelNode firstLevel1 = root.levelList.get(0);
		Assert.assertEquals(europe.getLevel(), firstLevel1.level);
		Assert.assertEquals("There should be 1 level1 area", 1, firstLevel1.areaList.size());
		NamedAreaNode level1AreaNode = firstLevel1.areaList.get(0);
		Assert.assertEquals("Level 1 area should be Europe", europe, level1AreaNode.area);
		LevelNode secondLevel1 = root.levelList.get(1);
		Assert.assertEquals(null, secondLevel1.level);
		
			//level 2
			Assert.assertEquals("level2 list should not be empty", 1, level1AreaNode.levelList.size());
			LevelNode firstLevel2 = level1AreaNode.levelList.get(0);
			Assert.assertEquals(middleEurope.getLevel(), firstLevel2.level);
			Assert.assertEquals("There should be 2 level2 area", 2, firstLevel2.areaList.size());
			NamedAreaNode middleEuropeAreaNode = firstLevel2.areaList.get(0);
			NamedAreaNode southWestAreaNode = firstLevel2.areaList.get(1);
			Assert.assertEquals("First level2 area should be MiddleEurope (11)", middleEurope, middleEuropeAreaNode.area);
			Assert.assertEquals("Second level2 area should be SouthWesternEurope (12)", southWestEurope, southWestAreaNode.area);
			//level 3
				//Middle Europe
				Assert.assertEquals("level3 list for Middle Europe should not be empty", 1, middleEuropeAreaNode.levelList.size());
				LevelNode middleEuropeLevel3 = middleEuropeAreaNode.levelList.get(0);
				Assert.assertEquals(germanyL3.getLevel(), middleEuropeLevel3.level);
				Assert.assertEquals("There should be 1 middle europe area", 1, middleEuropeLevel3.areaList.size());
				NamedAreaNode middleEuropeLevel3Area = middleEuropeLevel3.areaList.get(0);
				Assert.assertEquals("Middle Europe level 3 area should be GER", germanyL3, middleEuropeLevel3Area.area);
					//level 4
					Assert.assertEquals("sublevel list for GER should have 2 levels, TDWG level 4 and the new GER-L5", 2, middleEuropeLevel3Area.levelList.size());
					LevelNode germanyLevel3SubLevels = middleEuropeLevel3Area.levelList.get(0);
					Assert.assertEquals("Germany Level 3 sublevel should be level4", germanyL4.getLevel(), germanyLevel3SubLevels.level);
					Assert.assertEquals("There should be 1 GER subarea", 1, germanyLevel3SubLevels.areaList.size());
					NamedAreaNode germanyL3FirstSubArea = germanyLevel3SubLevels.areaList.get(0);
					Assert.assertEquals("Germany level 3 subarea should be GER-OO", germanyL4, germanyL3FirstSubArea.area);
					Assert.assertEquals("Germany level 4 area should not have sublevels", 0, germanyL3FirstSubArea.levelList.size());
					//level5
					LevelNode germanyLevel5Levels = middleEuropeLevel3Area.levelList.get(1);
					Assert.assertEquals("Second Germany Level 3 sublevel should be GERL5", newGermanLevel5, germanyLevel5Levels.level);
					Assert.assertEquals("There should be 1 GERL5 area", 1, germanyLevel5Levels.areaList.size());
					NamedAreaNode germanyL3SecondSubArea = germanyLevel5Levels.areaList.get(0);
					Assert.assertEquals("Second Germany level 3 subarea should be GER-L5", newGermanSubAreaAndLevel, germanyL3SecondSubArea.area);
					Assert.assertEquals("GER-L5 should not have sublevels", 0, germanyL3SecondSubArea.levelList.size());
					
				
				//SouthWest Europe
				Assert.assertEquals("level3 list for Southwestern Europe should not be empty", 1, southWestAreaNode.levelList.size());
				LevelNode southWestLevel3 = southWestAreaNode.levelList.get(0);
				Assert.assertEquals(franceL3.getLevel(), southWestLevel3.level);
				Assert.assertEquals("There should be 1 south west european area", 1, southWestLevel3.areaList.size());
				NamedAreaNode southWestLevel3Area = southWestLevel3.areaList.get(0);
				Assert.assertEquals("South Western Europe level 3 area should be FRA", franceL3, southWestLevel3Area.area);
				Assert.assertEquals("France level 3 should not have sublevels (in this hierarchie as France level 4 areas were not added to the area list)", 0, southWestLevel3Area.levelList.size());
				
			
		
		System.out.println(root.toString(true, 0));	
	}
	
	private NamedArea getAreaByTdwgAbbreviation(String tdwgAbbrev){
		return NamedArea.getAreaByTdwgAbbreviation(tdwgAbbrev);
	}

}
