// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.ext;

import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.ext.geo.EditGeoServiceUtilities;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.AbsenceTerm;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.location.TdwgArea;

/**
 * @author a.mueller
 * @created 08.10.2008
 * @version 1.0
 */
public class EditGeoServiceTest  {
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
	public void testGetWebServiceUrl() {
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
				
		String result = EditGeoServiceUtilities.getEditGeoServiceUrlParameterString(distributions, presenceAbsenceColorMap, 600, 300, bbox,backLayer, languages );		
		//TODO Set semantics is not determined
		//String expected = "http://www.test.de/webservice?l=tdwg3&ad=tdwg3:a:GER|b:OKL|c:BGM|b:SPA|d:FRA&as=a:005500|b:00FF00|c:FFFFFF|d:001100&bbox=-20,40,40,40&ms=400x300";
		System.out.println(result);
		assertTrue(result.startsWith("l="));
		assertTrue(result.endsWith("&ms=600,300"));
		//assertTrue(result.matches("0000ff"));
		//TODO continue
	}
}