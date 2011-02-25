// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.ext.geo;

import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.ext.geo.EditGeoServiceUtilities;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.AbsenceTerm;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.TdwgArea;

/**
 * @author a.mueller
 * @created 08.10.2008
 * @version 1.0
 */
public class EditGeoServiceTest  {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(EditGeoServiceTest.class);

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
	
//******************************************** TESTS**************
	@Test
	public void testGetWebServiceUrlTdwg() {
		//String webServiceUrl = "http://www.test.de/webservice";
		Set<Distribution> distributions = new HashSet<Distribution>();
		distributions.add(Distribution.NewInstance(TdwgArea.getAreaByTdwgAbbreviation("SPA"), PresenceTerm.PRESENT()));
		distributions.add(Distribution.NewInstance(TdwgArea.getAreaByTdwgAbbreviation("GER"), PresenceTerm.INTRODUCED()));
		distributions.add(Distribution.NewInstance(TdwgArea.getAreaByTdwgAbbreviation("14"), PresenceTerm.CULTIVATED()));
		distributions.add(Distribution.NewInstance(TdwgArea.getAreaByTdwgAbbreviation("BGM"), AbsenceTerm.ABSENT()));
		distributions.add(Distribution.NewInstance(TdwgArea.getAreaByTdwgAbbreviation("FRA"), AbsenceTerm.ABSENT()));
		distributions.add(Distribution.NewInstance(TdwgArea.getAreaByTdwgAbbreviation("IND-AP"), PresenceTerm.PRESENT()));
		
		Map<PresenceAbsenceTermBase<?>, Color> presenceAbsenceColorMap = new HashMap<PresenceAbsenceTermBase<?>, Color>();
		presenceAbsenceColorMap.put(PresenceTerm.PRESENT(), Color.BLUE);
		presenceAbsenceColorMap.put(PresenceTerm.INTRODUCED(), Color.BLACK);
		presenceAbsenceColorMap.put(PresenceTerm.CULTIVATED(), Color.YELLOW);
		presenceAbsenceColorMap.put(AbsenceTerm.ABSENT(), Color.DARK_GRAY);
		String backLayer ="";
		presenceAbsenceColorMap = null;
		String bbox="-20,0,120,70";
		List<Language> languages = new ArrayList<Language>();
				
		String result = EditGeoServiceUtilities.getDistributionServiceRequestParameterString(distributions, presenceAbsenceColorMap, 600, 300, bbox,backLayer, languages );		
		//TODO Set semantics is not determined
		//String expected = "http://www.test.de/webservice?l=tdwg3&ad=tdwg3:a:GER|b:OKL|c:BGM|b:SPA|d:FRA&as=a:005500|b:00FF00|c:FFFFFF|d:001100&bbox=-20,40,40,40&ms=400x300";
		System.out.println(result);
		assertTrue(result.startsWith("l="));
		assertTrue(result.endsWith("&ms=600,300"));
		assertTrue(result.matches(".*ad=tdwg[1-4].*"));
		assertTrue(result.matches(".*tdwg2:[a-d]:14[\\|&].*") );
		assertTrue(result.matches(".*[a-d]:FRA,BGM[\\|&].*") || result.matches(".*[a-d]:BGM,FRA[\\|&].*") );
		assertTrue(result.matches(".*[a-d]:GER[\\|&].*") );
		assertTrue(result.matches(".*[a-d]:SPA[\\|&].*") );
		assertTrue(result.matches(".*tdwg4:[a-d]:INDAP[\\|&].*") );
		//assertTrue(result.matches("0000ff"));
		//TODO continue
	}
	
	@Test
	public void testGetWebServiceUrlCyprus() {
		makeCyprusAreas();
		Set<Distribution> distributions = new HashSet<Distribution>();
		distributions.add(Distribution.NewInstance(divisions.get("1"), PresenceTerm.PRESENT()));
		distributions.add(Distribution.NewInstance(divisions.get("2"), PresenceTerm.INTRODUCED()));
		distributions.add(Distribution.NewInstance(divisions.get("3"), PresenceTerm.CULTIVATED()));
		distributions.add(Distribution.NewInstance(divisions.get("4"), AbsenceTerm.ABSENT()));
		distributions.add(Distribution.NewInstance(divisions.get("5"), AbsenceTerm.ABSENT()));
		distributions.add(Distribution.NewInstance(divisions.get("6"), PresenceTerm.PRESENT()));
		
		Map<PresenceAbsenceTermBase<?>, Color> presenceAbsenceColorMap = new HashMap<PresenceAbsenceTermBase<?>, Color>();
		presenceAbsenceColorMap.put(PresenceTerm.PRESENT(), Color.BLUE);
		presenceAbsenceColorMap.put(PresenceTerm.INTRODUCED(), Color.BLACK);
		presenceAbsenceColorMap.put(PresenceTerm.CULTIVATED(), Color.YELLOW);
		presenceAbsenceColorMap.put(AbsenceTerm.ABSENT(), Color.DARK_GRAY);
		String backLayer ="";
		presenceAbsenceColorMap = null;
		String bbox="-20,0,120,70";
		List<Language> languages = new ArrayList<Language>();
				
		String result = EditGeoServiceUtilities.getDistributionServiceRequestParameterString(distributions, presenceAbsenceColorMap, 600, 300, bbox,backLayer, languages );		
		//TODO Set semantics is not determined
		//String expected = "http://www.test.de/webservice?l=tdwg3&ad=tdwg3:a:GER|b:OKL|c:BGM|b:SPA|d:FRA&as=a:005500|b:00FF00|c:FFFFFF|d:001100&bbox=-20,40,40,40&ms=400x300";
		System.out.println(result);
		assertTrue(result.startsWith("l="));
		assertTrue(result.endsWith("&ms=600,300"));
		assertTrue(result.matches(".*ad=cyprusdivs:bdcode:.*"));
		assertTrue(result.matches(".*[a-d]:5,4[\\|&].*") || result.matches(".*[a-d]:4,5[\\|&].*") );
		assertTrue(result.matches(".*[a-d]:1,6[\\|&].*") || result.matches(".*[a-d]:6,1[\\|&].*") );
		assertTrue(result.matches(".*[a-d]:2[\\|&].*") );
		assertTrue(result.matches(".*[a-d]:3[\\|&].*") );
	}
	
	public static final UUID uuidCyprusDivisionsVocabulary = UUID.fromString("2119f610-1f93-4d87-af28-40aeefaca100");
	private Map<String, NamedArea> divisions = new HashMap<String, NamedArea>();
	
	private boolean makeCyprusAreas() {
		//divisions
		
		
		NamedAreaType areaType = NamedAreaType.NATURAL_AREA();
		NamedAreaLevel areaLevel = NamedAreaLevel.NewInstance("Cyprus Division", "Cyprus Division", null);
		
		TermVocabulary areaVocabulary = TermVocabulary.NewInstance("Cyprus devisions", "Cyprus divisions", null, null);
		areaVocabulary.setUuid(uuidCyprusDivisionsVocabulary);
		
		for(int i = 1; i <= 8; i++){
			UUID divisionUuid = getNamedAreaUuid(String.valueOf(i));
			NamedArea division = this.getNamedArea(divisionUuid, "Division " + i, "Cyprus: Division " + i, String.valueOf(i), areaType, areaLevel, areaVocabulary);
			divisions.put(String.valueOf(i), division);
		}
		
//		indigenousStatus = (PresenceTerm)getTermService().find(CyprusTransformer.indigenousUuid);
//		casualStatus = (PresenceTerm)getTermService().find(CyprusTransformer.casualUuid);
//		nonInvasiveStatus = (PresenceTerm)getTermService().find(CyprusTransformer.nonInvasiveUuid);
//		invasiveStatus = (PresenceTerm)getTermService().find(CyprusTransformer.invasiveUuid);
//		questionableStatus = (PresenceTerm)getTermService().find(CyprusTransformer.questionableUuid);
		
		return true;
			
		
	}
		
	public static final UUID uuidDivision1 = UUID.fromString("ab17eee9-1abb-4ce9-a9a2-563f840cdbfc");
	public static final UUID uuidDivision2 = UUID.fromString("c3606165-efb7-4224-a168-63e009eb4aa5");
	public static final UUID uuidDivision3 = UUID.fromString("750d4e07-e34b-491f-a7b7-09723afdc960");
	public static final UUID uuidDivision4 = UUID.fromString("8a858922-e8e5-4791-ad53-906e50633ec7");
	public static final UUID uuidDivision5 = UUID.fromString("16057133-d541-4ebd-81d4-cb92265ec54c");
	public static final UUID uuidDivision6 = UUID.fromString("fbf21230-4a42-4f4c-9af8-5da52123c264");
	public static final UUID uuidDivision7 = UUID.fromString("d31dd96a-36ea-4428-871c-d8552a9565ca");
	public static final UUID uuidDivision8 = UUID.fromString("236ea447-c3ab-486d-9e06-cc5907861acc");

	
	public UUID getNamedAreaUuid(String key) {
		if (CdmUtils.isEmpty(key)){return null;
		}else if (key.equalsIgnoreCase("1")){return uuidDivision1;
		}else if (key.equalsIgnoreCase("2")){return uuidDivision2;
		}else if (key.equalsIgnoreCase("3")){return uuidDivision3;
		}else if (key.equalsIgnoreCase("4")){return uuidDivision4;
		}else if (key.equalsIgnoreCase("5")){return uuidDivision5;
		}else if (key.equalsIgnoreCase("6")){return uuidDivision6;
		}else if (key.equalsIgnoreCase("7")){return uuidDivision7;
		}else if (key.equalsIgnoreCase("8")){return uuidDivision8;
		}else{
			return null;
		}	
	}
	
	protected NamedArea getNamedArea(UUID uuid, String label, String text, String labelAbbrev, NamedAreaType areaType, NamedAreaLevel level, TermVocabulary voc){
		NamedArea namedArea = NamedArea.NewInstance(text, label, labelAbbrev);
		voc.addTerm(namedArea);
		namedArea.setType(areaType);
		namedArea.setLevel(level);
		namedArea.setUuid(uuid);
		return namedArea;
	}
}