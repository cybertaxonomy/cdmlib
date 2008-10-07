/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.location.TdwgArea;

/**
 * @author a.mueller
 * @created 08.10.2008
 * @version 1.0
 */
public class DistributionTest {
	private static final Logger logger = Logger.getLogger(DistributionTest.class);

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
	
//******************************************** TESTS**************
	@Test
	public void testGetWebServiceUrl() {
		String webServiceUrl = "http://www.test.de/webservice";
		Set<Distribution> distributions = new HashSet<Distribution>();
		distributions.add(Distribution.NewInstance(TdwgArea.getAreaByTdwgLabel("SPA"), PresenceTerm.PRESENT()));
		distributions.add(Distribution.NewInstance(TdwgArea.getAreaByTdwgLabel("GER"), PresenceTerm.INTRODUCED()));
		distributions.add(Distribution.NewInstance(TdwgArea.getAreaByTdwgLabel("FRA"), PresenceTerm.CULTIVATED()));
		distributions.add(Distribution.NewInstance(TdwgArea.getAreaByTdwgLabel("BGM"), AbsenceTerm.ABSENT()));
		distributions.add(Distribution.NewInstance(TdwgArea.getAreaByTdwgLabel("OKL"), PresenceTerm.PRESENT()));
		String result = Distribution.getWebServiceUrl(distributions, webServiceUrl);
		//TODO Set semantics is not determined
		String expected = "http://www.test.de/webservice?l=tdwg3&ad=tdwg3:a:GER|b:OKL|c:BGM|b:SPA|d:FRA&as=a:005500|b:00FF00|c:FFFFFF|d:001100&bbox=-20,40,40,40&ms=400x300";
		System.out.println(result);
		assertTrue(result.startsWith("http://www.test.de/webservice?l=tdwg3&ad=tdwg3:"));
		assertTrue(result.endsWith("&bbox=-20,40,40,40&ms=400x300"));
		//TODO continue
	}
}
