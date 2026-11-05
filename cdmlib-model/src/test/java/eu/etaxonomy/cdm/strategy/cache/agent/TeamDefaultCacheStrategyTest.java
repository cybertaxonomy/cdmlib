/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;

/**
 * @author a.mueller
 * @since 29.09.2009
 */
public class TeamDefaultCacheStrategyTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private static Team team1;
	private static Team team2;
	private static Team team3;

	private static Person person1;
	private static Person person2;
	private static Person person3;
	private static Person person4;

	@BeforeEach
	public void setUp() throws Exception {

		team1 = Team.NewInstance();
		team2 = Team.NewInstance();
		team3 = Team.NewInstance(); //empty team

		person1 = Person.NewInstance();

		person1.setGivenName("P1GN");
		person1.setFamilyName("P1FN");
		person1.setPrefix("Dr1.");
		person1.setSuffix("Suff1");
		person1.setCollectorTitle("P1CT");

		person2 = Person.NewInstance();
		person2.setNomenclaturalTitle("P2NomT");
		person2.setFamilyName("P2FN");
		person2.setGivenName("P2GN");
		person2.setSuffix("P2Suff");

		person3 = Person.NewInstance();
		person3.setNomenclaturalTitle("P3NomT");

	    person4 = Person.NewInstance();
	    person4.setNomenclaturalTitle("P4NomT");
	    person4.setFamilyName("P4FN");

		team1.addTeamMember(person1);

		team2.addTeamMember(person2);
		team2.addTeamMember(person1);
		team2.addTeamMember(person3);
		team2.addTeamMember(person4);
	}

//**************************************** TESTS **************************************

	@Test
	public final void testNewInstance() {
		TeamDefaultCacheStrategy cacheStrategy = TeamDefaultCacheStrategy.NewInstance();
		assertNotNull(cacheStrategy);
	}

	@Test
	public final void testGetNomenclaturalTitleCache(){

	    assertNotNull(team1.getNomenclaturalTitleCache(), "team1 nomenclatural title must not to be null");
		assertEquals("P1FN, P.", team1.getNomenclaturalTitleCache(), "team1 nomenclatural title should be created by elements");
		person1.setSuffix(null);
		assertEquals("P1FN, P.", team1.getNomenclaturalTitleCache(), "team1 nomenclatural title should be P1FN, P.");

		//team2
		assertEquals("P2NomT, P1FN, P., P3NomT & P4NomT", team2.getNomenclaturalTitleCache(), "team2 nomenclatural title should be 'P2NomT, P1FN, P., P3NomT & P4NomT'");
		//more
		team2.setHasMoreMembers(true);
        assertEquals("P2NomT, P1FN, P., P3NomT, P4NomT & al.", team2.getNomenclaturalTitleCache(), "team2 nomenclatural title should be 'P2NomT, P1FN, P., P3NomT, P4NomT & al.'");
        team2.setHasMoreMembers(false);
        //3 members
		team2.setCacheStrategy(TeamDefaultCacheStrategy.NewInstanceNomEtAl(3));
        team2.setTitleCache(null, false);
        assertEquals("P2NomT, P1FN, P. & al.", team2.getNomenclaturalTitleCache(), "team2 nomenclatural title should still be 'P2NomT, P1FN, P. & al.' now.");
        //4 members
        team2.setCacheStrategy(TeamDefaultCacheStrategy.NewInstanceNomEtAl(4));
        team2.setTitleCache(null, false);
        assertEquals("P2NomT, P1FN, P., P3NomT & P4NomT", team2.getNomenclaturalTitleCache(), "team2 nomenclatural title should still be 'P2NomT, P1FN, P., P3NomT & P4NomT' now.");


		//team3/empty team
		assertNotNull(team3.getNomenclaturalTitleCache(), "team3 nomenclatural title must not to be null");
		assertTrue(StringUtils.isNotBlank(team3.getNomenclaturalTitleCache()), "team3 nomenclatural title must not be empty");

		//don't take next test to serious, may be also something different, but not empty
		assertEquals(TeamDefaultCacheStrategy.EMPTY_TEAM, team3.getNomenclaturalTitleCache(), "team3 nomenclatural title should be empty team replacement string");
		team3.setCollectorTitleCache("CollTitleCache", true);
		team3.resetCaches();
        assertEquals("CollTitleCache", team3.getNomenclaturalTitleCache(), "unprotected team3 nomenclatural title should show collector title cache");
        assertEquals("CollTitleCache", team3.getTitleCache(), "unprotected team3 title cache should show collector title cache");

		team3.setNomenclaturalTitleCache("ProtectedNomCache", true);
		assertEquals("ProtectedNomCache", team3.getNomenclaturalTitleCache());
	    assertEquals("ProtectedNomCache", team3.cacheStrategy().getNomenclaturalTitleCache(team3));
	    team3.setCollectorTitleCache(null, false);
	    assertEquals("ProtectedNomCache", team3.getCollectorTitleCache(), "unprotected team3 collector title should show abbrev title cache");
	    assertEquals("ProtectedNomCache", team3.getTitleCache(), "unprotected team3 title cache should show abbrev title cache");



	}

	@Test
	public final void testGetTitleCache(){
		assertNotNull(team1.getTitleCache(), "team1 title cache must not to be null");
		assertEquals("P1FN, P.", team1.getTitleCache(), "team1 title cache should be created by members titleCache");
		person1.setSuffix(null);
		assertEquals("P1FN, P.", team1.getTitleCache(), "team1 title cache should be P1FN, P.");
		//peson2
		assertEquals("P2FN, P., P1FN, P., P3NomT & P4FN", team2.getTitleCache(), "team2 title cache should be 'P2FN, P., P1FN, P., P3NomT & P4FN'");
        team2.setHasMoreMembers(true);

        assertEquals("P2FN, P., P1FN, P., P3NomT, P4FN & al.", team2.getTitleCache(), "team2 title cache should be 'P2FN, P., P1FN, P., P3NomT, P4FN & al.'");
        team2.setHasMoreMembers(false);

		team2.setCacheStrategy(TeamDefaultCacheStrategy.NewInstanceTitleEtAl(3));
		team2.setTitleCache(null, false);
        assertEquals("P2FN, P., P1FN, P. & al.", team2.getTitleCache(), "team2 nomenclatural title should still be 'P2FN, P., P1FN, P. & al.' now.");

        team2.setCacheStrategy(TeamDefaultCacheStrategy.NewInstanceTitleEtAl(4));
        team2.setTitleCache(null, false);
        assertEquals("P2FN, P., P1FN, P., P3NomT & P4FN", team2.getTitleCache(), "team2 nomenclatural title should still be 'P2FN, P., P1FN, P., P3NomT & P4FN' now.");
        team2.setHasMoreMembers(true);
        team2.setTitleCache(null, false);
        assertEquals("P2FN, P., P1FN, P., P3NomT & al.", team2.getTitleCache(), "team2 nomenclatural title should still be 'P2FN, P., P1FN, P., P3NomT & al.' now.");


		//person3
		assertNotNull(team3.getTitleCache(), "team3 title cache must not to be null");
		assertTrue(StringUtils.isNotBlank(team3.getTitleCache()), "team3 title cache must not be empty");

		//don't take to exact, may be also something different, but not empty
		assertEquals(TeamDefaultCacheStrategy.EMPTY_TEAM, team3.getTitleCache(), "team3 title cache should be empty team replacement string");
	}

    @Test
    public final void testFullTitle(){

        assertNotNull(team1.getFullTitle(), "team1 full title must not to be null");
        assertEquals("Dr1. P1GN P1FN Suff1", team1.getFullTitle(), "team1 full title should be created by elements");
        person1.setSuffix(null);
        assertEquals("Dr1. P1GN P1FN", team1.getFullTitle(), "team1 full title should be Dr1. P1GN P1FN");
        //team2
        assertEquals("P2GN P2FN P2Suff, Dr1. P1GN P1FN, P3NomT & P4FN", team2.getFullTitle(), "team2 full title should be 'P2GN P2FN P2Suff, Dr1. P1GN P1FN, P3NomT & P4FN'");
        team2.setHasMoreMembers(true);
        assertEquals("P2GN P2FN P2Suff, Dr1. P1GN P1FN, P3NomT, P4FN & al.", team2.getFullTitle(), "team2 full title should be 'P2GN P2FN P2Suff, Dr1. P1GN P1FN, P3NomT, P4FN & al.'");

        //team3
        assertNotNull(team3.getFullTitle(), "team3 full title must not to be null");
        assertTrue(StringUtils.isNotBlank(team3.getFullTitle()), "team3 full title must not be empty");

        //don't take to serious, may be also something different, but not empty
        assertEquals(TeamDefaultCacheStrategy.EMPTY_TEAM, team3.getFullTitle(), "team3 full title should should be empty team replacement string");
    }

    @Test
    public final void testGetCollectorTitleCache(){

        assertNotNull(team1.getCollectorTitleCache(), "team1 collector title cache must not to be null");
        assertEquals("P1CT", team1.getCollectorTitleCache(), "team1 collector title cache title should be created by elements");
        person1.setSuffix(null);
        assertEquals("P1CT", team1.getCollectorTitleCache(), "team1 collector title cache should be P1FN, P.");

        //team2
        assertEquals("P. P2FN, P1CT, P3NomT & P4FN", team2.getCollectorTitleCache(), "team2 collector title cache should be 'P. P2FN, P1CT, P3NomT & P4FN'");
        //more
        team2.setHasMoreMembers(true);
        assertEquals("P. P2FN, P1CT, P3NomT, P4FN & al.", team2.getCollectorTitleCache(), "team2 collector title cache should be 'P. P2FN, P1CT, P3NomT, P4FN & al.'");
        team2.setHasMoreMembers(false);
        //3 members
        team2.setCacheStrategy(TeamDefaultCacheStrategy.NewInstance(3));
        team2.resetCaches();
        assertEquals("P. P2FN, P1CT & al.", team2.getCollectorTitleCache(), "team2 collector title cache should still be 'P. P2FN, P1CT & al.' now.");
        //4 members
        team2.setCacheStrategy(TeamDefaultCacheStrategy.NewInstance(4));
        team2.resetCaches();
        assertEquals("P. P2FN, P1CT, P3NomT & P4FN", team2.getCollectorTitleCache(), "team2 collector title cache should still be 'P. P2FN, P1CT, P3NomT & P4FN' now.");


        //team3/empty team
        assertNotNull(team3.getCollectorTitleCache(), "team3 collector title cache must not to be null");
        assertTrue(StringUtils.isNotBlank(team3.getCollectorTitleCache()), "team3 collector title cache must not be empty");

        //don't take next test to serious, may be also something different, but not empty
        assertEquals(TeamDefaultCacheStrategy.EMPTY_TEAM, team3.getCollectorTitleCache(), "team3 collector title cache should be empty team replacement string");
    }

	@Test
	public final void testListenersOnMembers(){
		assertNotNull(team1.getTitleCache(), "team1 title cache must not to be null");
		assertEquals("P1FN, P.", team1.getTitleCache(), "team1 title cache should be created by elements");
		person1.setGivenName("O.");
		assertEquals("P1FN, O.", team1.getTitleCache(), "team1 title cache should be P1FN, O.");
	}

	@Test
	public final void testRemoveWhitespaces() {
	    String author = null;
	    assertEquals(null, TeamDefaultCacheStrategy.removeWhitespaces(author));

	    author = "  ";
	    assertEquals("", TeamDefaultCacheStrategy.removeWhitespaces(author));

	    author = "Mill. ";
	    assertEquals("Mill.", TeamDefaultCacheStrategy.removeWhitespaces(author));

	    author = " Miller ";
	    assertEquals("Miller", TeamDefaultCacheStrategy.removeWhitespaces(author), "Result should always be trimed");

	    author = "A. Mill.";
        assertEquals("A.Mill.", TeamDefaultCacheStrategy.removeWhitespaces(author));

        author = "A. Mill.";
        assertEquals("A.Mill.", TeamDefaultCacheStrategy.removeWhitespaces(author));

        author = "A.   Mill.";
        assertEquals("A.Mill.", TeamDefaultCacheStrategy.removeWhitespaces(author));

        author = "A.   Mill. & B. Kohl.-Haber";
        assertEquals("A.Mill. & B.Kohl.-Haber", TeamDefaultCacheStrategy.removeWhitespaces(author));

        author = "A.   Mill. ,J. N. Bohl. f.& B. Kohl.-Haber";
        assertEquals("A.Mill.,J.N.Bohl.f. & B.Kohl.-Haber", TeamDefaultCacheStrategy.removeWhitespaces(author));

        author = " (Ab. ex CD. , All , Bet & J.Vall.) A.  Mill.ex Kohl.";
        assertEquals("(Ab. ex CD., All, Bet & J.Vall.) A.Mill. ex Kohl.", TeamDefaultCacheStrategy.removeWhitespaces(author));

	}
}
