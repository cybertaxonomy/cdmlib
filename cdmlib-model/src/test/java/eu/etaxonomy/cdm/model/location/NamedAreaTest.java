/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.NamedArea.LevelNode;
import eu.etaxonomy.cdm.model.location.NamedArea.NamedAreaNode;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 * @since 26.05.2011
 */
public class NamedAreaTest extends EntityTestBase {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private NamedArea namedArea1;
	private NamedAreaLevel level1;

	@BeforeEach
	public void setUp() throws Exception {
		namedArea1 = NamedArea.NewInstance("Description for Named Area 1", "Named Area 1", "NA1");
		level1 = NamedAreaLevel.NewInstance("Description for level 1", "Level 1", "L1");
		namedArea1.setLevel(level1);
	}

	@Test
	public void NewInstanceStringStringStringTest(){
		assertEquals("Description for Named Area 1", namedArea1.getDescription());
		assertEquals("Named Area 1", namedArea1.getLabel());
		assertEquals("NA1", namedArea1.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel());
	}

	@Test
	public void labelWithLevelTest(){
		assertEquals("Named Area 1 - Level 1", NamedArea.labelWithLevel(namedArea1, Language.DEFAULT()));
		assertEquals("Germany - Country", NamedArea.labelWithLevel(Country.GERMANY(), Language.DEFAULT()));
		assertEquals("Germany - TDWG Level 3", NamedArea.labelWithLevel(getAreaByTdwgAbbreviation("GER"), Language.DEFAULT()));
		NamedArea namedArea2 = NamedArea.NewInstance("Description for Named Area 2", "", "NA2");
		assertEquals("NA2", NamedArea.labelWithLevel(namedArea2, Language.DEFAULT()));
		NamedArea namedArea3 = NamedArea.NewInstance("Description for Named Area 3", null, " ");
		assertEquals("Description for Named Area 3", NamedArea.labelWithLevel(namedArea3, Language.DEFAULT()));

		//TODO include Vocabulay information
	}

	@Test
	public void getHiearchieListTest(){

		//Create example data
		List<NamedArea> list = new ArrayList<>();
		NamedArea germanyL4 = getAreaByTdwgAbbreviation("GER-OO");
		assertNotNull(germanyL4, "Prerequisite: Germany should not be null");
		list.add(germanyL4);
		NamedArea franceL3 = getAreaByTdwgAbbreviation("FRA");
		assertNotNull(germanyL4, "Prerequisite: France should not be null");
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
		assertNull(root.area, "Root should not have an area");
		//level1
		assertEquals(3, root.levelList.size(), "level1 list should include 3 levels");
		LevelNode firstLevel1 = root.levelList.get(0);
		assertEquals(europe.getLevel(), firstLevel1.level);
		assertEquals(1, firstLevel1.areaList.size(), "There should be 1 level1 area");
		NamedAreaNode level1AreaNode = firstLevel1.areaList.get(0);
		assertEquals(europe, level1AreaNode.area, "Level 1 area should be Europe");
		LevelNode secondLevel1 = root.levelList.get(1);
		assertEquals(null, secondLevel1.level);

			//level 2
			assertEquals(1, level1AreaNode.levelList.size(), "level2 list should not be empty");
			LevelNode firstLevel2 = level1AreaNode.levelList.get(0);
			assertEquals(middleEurope.getLevel(), firstLevel2.level);
			assertEquals(2, firstLevel2.areaList.size(), "There should be 2 level2 area");
			NamedAreaNode middleEuropeAreaNode = firstLevel2.areaList.get(0);
			NamedAreaNode southWestAreaNode = firstLevel2.areaList.get(1);
			assertEquals(middleEurope, middleEuropeAreaNode.area, "First level2 area should be MiddleEurope (11)");
			assertEquals(southWestEurope, southWestAreaNode.area, "Second level2 area should be SouthWesternEurope (12)");
			//level 3
				//Middle Europe
				assertEquals(1, middleEuropeAreaNode.levelList.size(), "level3 list for Middle Europe should not be empty");
				LevelNode middleEuropeLevel3 = middleEuropeAreaNode.levelList.get(0);
				assertEquals(germanyL3.getLevel(), middleEuropeLevel3.level);
				assertEquals(1, middleEuropeLevel3.areaList.size(), "There should be 1 middle europe area");
				NamedAreaNode middleEuropeLevel3Area = middleEuropeLevel3.areaList.get(0);
				assertEquals(germanyL3, middleEuropeLevel3Area.area, "Middle Europe level 3 area should be GER");
					//level 4
					assertEquals(2, middleEuropeLevel3Area.levelList.size(), "sublevel list for GER should have 2 levels, TDWG level 4 and the new GER-L5");
					LevelNode germanyLevel3SubLevels = middleEuropeLevel3Area.levelList.get(0);
					assertEquals(germanyL4.getLevel(), germanyLevel3SubLevels.level, "Germany Level 3 sublevel should be level4");
					assertEquals(1, germanyLevel3SubLevels.areaList.size(), "There should be 1 GER subarea");
					NamedAreaNode germanyL3FirstSubArea = germanyLevel3SubLevels.areaList.get(0);
					assertEquals(germanyL4, germanyL3FirstSubArea.area, "Germany level 3 subarea should be GER-OO");
					assertEquals(0, germanyL3FirstSubArea.levelList.size(), "Germany level 4 area should not have sublevels");
					//level5
					LevelNode germanyLevel5Levels = middleEuropeLevel3Area.levelList.get(1);
					assertEquals(newGermanLevel5, germanyLevel5Levels.level, "Second Germany Level 3 sublevel should be GERL5");
					assertEquals(1, germanyLevel5Levels.areaList.size(), "There should be 1 GERL5 area");
					NamedAreaNode germanyL3SecondSubArea = germanyLevel5Levels.areaList.get(0);
					assertEquals(newGermanSubAreaAndLevel, germanyL3SecondSubArea.area, "Second Germany level 3 subarea should be GER-L5");
					assertEquals(0, germanyL3SecondSubArea.levelList.size(), "GER-L5 should not have sublevels");

				//SouthWest Europe
				assertEquals(1, southWestAreaNode.levelList.size(), "level3 list for Southwestern Europe should not be empty");
				LevelNode southWestLevel3 = southWestAreaNode.levelList.get(0);
				assertEquals(franceL3.getLevel(), southWestLevel3.level);
				assertEquals(1, southWestLevel3.areaList.size(), "There should be 1 south west european area");
				NamedAreaNode southWestLevel3Area = southWestLevel3.areaList.get(0);
				assertEquals(franceL3, southWestLevel3Area.area, "South Western Europe level 3 area should be FRA");
				assertEquals(0, southWestLevel3Area.levelList.size(), "France level 3 should not have sublevels (in this hierarchie as France level 4 areas were not added to the area list)");

//		System.out.println(root.toString(true, 0));
	}

	private NamedArea getAreaByTdwgAbbreviation(String tdwgAbbrev){
		return NamedArea.getAreaByTdwgAbbreviation(tdwgAbbrev);
	}
}
