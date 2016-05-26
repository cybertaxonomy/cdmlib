// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.reference.old;


import java.net.URI;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.reference.IWebPage;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @date 16.06.2010
 * 
 * UNDER CONSTRUCTION
 *
 */
public class WebPageDefaultCacheStrategyTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(WebPageDefaultCacheStrategyTest.class);
	
	private static IWebPage webPage1;
	private static Team team1;
	private static WebPageDefaultCacheStrategy defaultStrategy;
	private static final String detail1 = "55";
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		defaultStrategy = WebPageDefaultCacheStrategy.NewInstance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		webPage1 = ReferenceFactory.newWebPage();
		webPage1.setCacheStrategy(defaultStrategy);
		team1 = Team.NewTitledInstance("Authorteam, D.", "AT.");
	}
	
//**************************** TESTS ***********************************

	
	@Test
	@Ignore //under development
	public void testGetTitleCache(){
		webPage1.setTitle("Flora of Israel Online");
		webPage1.setUri(URI.create("http://flora.huji.ac.il"));
		webPage1.setAuthorship(team1);
		webPage1.setDatePublished(TimePeriodParser.parseString("[accessed in 2011]"));
		//taken from Berlin Model, may be modified in future
		Assert.assertEquals("Unexpected title cache.", "Authorteam, D. - Flora of Israel Online - http://flora.huji.ac.il [accessed in 2011]", webPage1.getTitleCache());
	}
	
//	@Test
//	//WebPages should usually not be used as nomencl.reference, therefore this is less important
//	public void testGetAbbrevTitleCache(){
//		webPage1.setTitle("auct.");
//		Assert.assertEquals("Unexpected title cache.", "auct.", webPage1.getTitleCache());
//	}

}
