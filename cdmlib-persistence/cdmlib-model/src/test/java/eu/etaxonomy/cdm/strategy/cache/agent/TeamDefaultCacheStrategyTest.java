// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.agent;


import static org.junit.Assert.assertNotNull;
import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;

/**
 * @author a.mueller
 * @created 29.09.2009
 * @version 1.0
 */
public class TeamDefaultCacheStrategyTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TeamDefaultCacheStrategyTest.class);

	private static Team team1;
	private static Team team2;
	private static Team team3;
	
	private static Person person1;
	private static Person person2;
	private static Person person3;
	private static Person person4;
	
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
		team1 = Team.NewInstance();
		team2 = Team.NewInstance();
		team3 = Team.NewInstance(); //empty team
		
		person1 = Person.NewInstance();
		
		person1.setFirstname("P1FN");
		person1.setLastname("P1LN");
		person1.setPrefix("Dr1.");
		person1.setSuffix("Suff1");
		
		person2 = Person.NewInstance();
		person2.setNomenclaturalTitle("P2NomT");
		person2.setLastname("P2LN");
		person2.setFirstname("P2FN");
		person2.setSuffix("P2Suff");
		
		person3 = Person.NewInstance();
		person3.setNomenclaturalTitle("P3NomT");
		
		
		person4 = Person.NewInstance(); //empty person
		
		team1.addTeamMember(person1);
		team2.addTeamMember(person2);
		team2.addTeamMember(person1);
		team2.addTeamMember(person3);
		
		
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
//**************************************** TESTS **************************************
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.agent.PersonDefaultCacheStrategy#NewInstance()}.
	 */
	@Test
	public final void testNewInstance() {
		TeamDefaultCacheStrategy cacheStrategy = TeamDefaultCacheStrategy.NewInstance();
		assertNotNull(cacheStrategy);
	}

	@Test
	public final void testGetNomenclaturalTitleCache(){
		Assert.assertNotNull("team1 nomenclatural title must not to be null", team1.getNomenclaturalTitle());
		Assert.assertEquals("team1 nomenclatural title should be created by elements", "Dr1. P1FN P1LN Suff1", team1.getNomenclaturalTitle());
		person1.setSuffix(null);
		Assert.assertEquals("team1 nomenclatural title should be Dr1. P1FN P1LN", "Dr1. P1FN P1LN", team1.getNomenclaturalTitle());
		//peson2
		Assert.assertEquals("team2 nomenclatural title should be 'P2NomT & Dr1. P1FN P1LN & P3NomT'", "P2NomT & Dr1. P1FN P1LN & P3NomT", team2.getNomenclaturalTitle());
		//person3
		Assert.assertNotNull("team3 nomenclatural title must not to be null", team3.getNomenclaturalTitle());
		Assert.assertTrue("team3 nomenclatural title must not be empty", CdmUtils.isNotEmpty(team3.getNomenclaturalTitle()));
		
		
		//don't take next test to serious, may be also something different, but not empty
		Assert.assertEquals("team3 nomenclatural title should be empty team replacement string", TeamDefaultCacheStrategy.EMPTY_TEAM, team3.getNomenclaturalTitle());
		
	}
	

	@Test
	public final void testGetTitleCache(){
		Assert.assertNotNull("team1 title cache must not to be null", team1.getTitleCache());
		Assert.assertEquals("team1 title cache should be created by elements", "Dr1. P1FN P1LN Suff1", team1.getTitleCache());
		person1.setSuffix(null);
		Assert.assertEquals("team1 title cache should be Dr1. P1FN P1LN", "Dr1. P1FN P1LN", team1.getTitleCache());
		//peson2
		Assert.assertEquals("team2 title cache should be 'P2FN P2LN P2Suff & Dr1. P1FN P1LN & P3NomT'", "P2FN P2LN P2Suff & Dr1. P1FN P1LN & P3NomT", team2.getTitleCache());
		//person3
		Assert.assertNotNull("team3 title cache must not to be null", team3.getTitleCache());
		Assert.assertTrue("team3 title cache must not be empty", CdmUtils.isNotEmpty(team3.getTitleCache()));
		//don't take to serious, may be also something different, but not empty
		Assert.assertEquals("team3 title cache should should be empty team replacement string", TeamDefaultCacheStrategy.EMPTY_TEAM, team3.getTitleCache());
		
	}
	
	@Test
	public final void testListenersOnMembers(){
		Assert.assertNotNull("team1 title cache must not to be null", team1.getTitleCache());
		Assert.assertEquals("team1 title cache should be created by elements", "Dr1. P1FN P1LN Suff1", team1.getTitleCache());
		person1.setSuffix(null);
		Assert.assertEquals("team1 title cache should be Dr1. P1FN P1LN", "Dr1. P1FN P1LN", team1.getTitleCache());
	}
	
	
}
