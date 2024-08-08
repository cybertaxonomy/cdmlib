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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

	@Before
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

	    Assert.assertNotNull("team1 nomenclatural title must not to be null",
		        team1.getNomenclaturalTitleCache());
		Assert.assertEquals("team1 nomenclatural title should be created by elements",
		        "P1FN, P.", team1.getNomenclaturalTitleCache());
		person1.setSuffix(null);
		Assert.assertEquals("team1 nomenclatural title should be P1FN, P.",
		        "P1FN, P.", team1.getNomenclaturalTitleCache());

		//team2
		Assert.assertEquals("team2 nomenclatural title should be 'P2NomT, P1FN, P., P3NomT & P4NomT'",
		        "P2NomT, P1FN, P., P3NomT & P4NomT", team2.getNomenclaturalTitleCache());
		//more
		team2.setHasMoreMembers(true);
        Assert.assertEquals("team2 nomenclatural title should be 'P2NomT, P1FN, P., P3NomT, P4NomT & al.'",
                "P2NomT, P1FN, P., P3NomT, P4NomT & al.", team2.getNomenclaturalTitleCache());
        team2.setHasMoreMembers(false);
        //3 members
		team2.setCacheStrategy(TeamDefaultCacheStrategy.NewInstanceNomEtAl(3));
        team2.setTitleCache(null, false);
        Assert.assertEquals("team2 nomenclatural title should still be 'P2NomT, P1FN, P. & al.' now.",
                "P2NomT, P1FN, P. & al.", team2.getNomenclaturalTitleCache());
        //4 members
        team2.setCacheStrategy(TeamDefaultCacheStrategy.NewInstanceNomEtAl(4));
        team2.setTitleCache(null, false);
        Assert.assertEquals("team2 nomenclatural title should still be 'P2NomT, P1FN, P., P3NomT & P4NomT' now.",
                "P2NomT, P1FN, P., P3NomT & P4NomT", team2.getNomenclaturalTitleCache());


		//team3/empty team
		Assert.assertNotNull("team3 nomenclatural title must not to be null",
		        team3.getNomenclaturalTitleCache());
		Assert.assertTrue("team3 nomenclatural title must not be empty",
		        StringUtils.isNotBlank(team3.getNomenclaturalTitleCache()));

		//don't take next test to serious, may be also something different, but not empty
		Assert.assertEquals("team3 nomenclatural title should be empty team replacement string",
		        TeamDefaultCacheStrategy.EMPTY_TEAM, team3.getNomenclaturalTitleCache());

		team3.setNomenclaturalTitleCache("ProtectedNomCache", true);
		Assert.assertEquals("ProtectedNomCache", team3.getNomenclaturalTitleCache());
	    Assert.assertEquals("ProtectedNomCache", team3.cacheStrategy().getNomenclaturalTitleCache(team3));
	}

	@Test
	public final void testGetTitleCache(){
		Assert.assertNotNull("team1 title cache must not to be null",
		        team1.getTitleCache());
		Assert.assertEquals("team1 title cache should be created by members titleCache",
		        "P1FN, P.", team1.getTitleCache());
		person1.setSuffix(null);
		Assert.assertEquals("team1 title cache should be P1FN, P.",
		        "P1FN, P.", team1.getTitleCache());
		//peson2
		Assert.assertEquals("team2 title cache should be 'P2FN, P., P1FN, P., P3NomT & P4FN'",
		        "P2FN, P., P1FN, P., P3NomT & P4FN", team2.getTitleCache());
        team2.setHasMoreMembers(true);

        Assert.assertEquals("team2 title cache should be 'P2FN, P., P1FN, P., P3NomT, P4FN & al.'",
                "P2FN, P., P1FN, P., P3NomT, P4FN & al.", team2.getTitleCache());
        team2.setHasMoreMembers(false);

		team2.setCacheStrategy(TeamDefaultCacheStrategy.NewInstanceTitleEtAl(3));
		team2.setTitleCache(null, false);
        Assert.assertEquals("team2 nomenclatural title should still be 'P2FN, P., P1FN, P. & al.' now.",
                "P2FN, P., P1FN, P. & al.", team2.getTitleCache());

        team2.setCacheStrategy(TeamDefaultCacheStrategy.NewInstanceTitleEtAl(4));
        team2.setTitleCache(null, false);
        Assert.assertEquals("team2 nomenclatural title should still be 'P2FN, P., P1FN, P., P3NomT & P4FN' now.",
                "P2FN, P., P1FN, P., P3NomT & P4FN", team2.getTitleCache());
        team2.setHasMoreMembers(true);
        team2.setTitleCache(null, false);
        Assert.assertEquals("team2 nomenclatural title should still be 'P2FN, P., P1FN, P., P3NomT & al.' now.",
                "P2FN, P., P1FN, P., P3NomT & al.", team2.getTitleCache());


		//person3
		Assert.assertNotNull("team3 title cache must not to be null",
		        team3.getTitleCache());
		Assert.assertTrue("team3 title cache must not be empty",
		        StringUtils.isNotBlank(team3.getTitleCache()));

		//don't take to exact, may be also something different, but not empty
		Assert.assertEquals("team3 title cache should be empty team replacement string",
		        TeamDefaultCacheStrategy.EMPTY_TEAM, team3.getTitleCache());
	}

    @Test
    public final void testFullTitle(){
        Assert.assertNotNull("team1 full title must not to be null", team1.getFullTitle());
        Assert.assertEquals("team1 full title should be created by elements",
                "Dr1. P1GN P1FN Suff1", team1.getFullTitle());
        person1.setSuffix(null);
        Assert.assertEquals("team1 full title should be Dr1. P1GN P1FN", "Dr1. P1GN P1FN",
                team1.getFullTitle());
        //team2
        Assert.assertEquals("team2 full title should be 'P2GN P2FN P2Suff, Dr1. P1GN P1FN, P3NomT & P4FN'",
                "P2GN P2FN P2Suff, Dr1. P1GN P1FN, P3NomT & P4FN", team2.getFullTitle());
        team2.setHasMoreMembers(true);
        Assert.assertEquals("team2 full title should be 'P2GN P2FN P2Suff, Dr1. P1GN P1FN, P3NomT, P4FN & al.'",
                "P2GN P2FN P2Suff, Dr1. P1GN P1FN, P3NomT, P4FN & al.", team2.getFullTitle());

        //team3
        Assert.assertNotNull("team3 full title must not to be null",
                team3.getFullTitle());
        Assert.assertTrue("team3 full title must not be empty",
                StringUtils.isNotBlank(team3.getFullTitle()));

        //don't take to serious, may be also something different, but not empty
        Assert.assertEquals("team3 full title should should be empty team replacement string",
                TeamDefaultCacheStrategy.EMPTY_TEAM, team3.getFullTitle());
    }

    @Test
    public final void testGetCollectorTitleCache(){
        Assert.assertNotNull("team1 collector title cache must not to be null",
                team1.getCollectorTitleCache());
        Assert.assertEquals("team1 collector title cache title should be created by elements",
                "P1CT", team1.getCollectorTitleCache());
        person1.setSuffix(null);
        Assert.assertEquals("team1 collector title cache should be P1FN, P.",
                "P1CT", team1.getCollectorTitleCache());

        //team2
        Assert.assertEquals("team2 collector title cache should be 'P. P2FN, P1CT, P3NomT & P4FN'",
                "P. P2FN, P1CT, P3NomT & P4FN", team2.getCollectorTitleCache());
        //more
        team2.setHasMoreMembers(true);
        Assert.assertEquals("team2 collector title cache should be 'P. P2FN, P1CT, P3NomT, P4FN & al.'",
                "P. P2FN, P1CT, P3NomT, P4FN & al.", team2.getCollectorTitleCache());
        team2.setHasMoreMembers(false);
        //3 members
        team2.setCacheStrategy(TeamDefaultCacheStrategy.NewInstance(3));
        team2.resetCaches();
        Assert.assertEquals("team2 collector title cache should still be 'P. P2FN, P1CT & al.' now.",
                "P. P2FN, P1CT & al.", team2.getCollectorTitleCache());
        //4 members
        team2.setCacheStrategy(TeamDefaultCacheStrategy.NewInstance(4));
        team2.resetCaches();
        Assert.assertEquals("team2 collector title cache should still be 'P. P2FN, P1CT, P3NomT & P4FN' now.",
                "P. P2FN, P1CT, P3NomT & P4FN", team2.getCollectorTitleCache());


        //team3/empty team
        Assert.assertNotNull("team3 collector title cache must not to be null",
                team3.getCollectorTitleCache());
        Assert.assertTrue("team3 collector title cache must not be empty",
                StringUtils.isNotBlank(team3.getCollectorTitleCache()));

        //don't take next test to serious, may be also something different, but not empty
        Assert.assertEquals("team3 collector title cache should be empty team replacement string", TeamDefaultCacheStrategy.EMPTY_TEAM, team3.getCollectorTitleCache());
    }

	@Test
	public final void testListenersOnMembers(){
		Assert.assertNotNull("team1 title cache must not to be null", team1.getTitleCache());
		Assert.assertEquals("team1 title cache should be created by elements",
		        "P1FN, P.", team1.getTitleCache());
		person1.setGivenName("O.");
		Assert.assertEquals("team1 title cache should be P1FN, O.", "P1FN, O.", team1.getTitleCache());
	}

	@Test
	public final void testRemoveWhitespaces() {
	    String author = null;
	    Assert.assertEquals(null, TeamDefaultCacheStrategy.removeWhitespaces(author));

	    author = "  ";
	    Assert.assertEquals("", TeamDefaultCacheStrategy.removeWhitespaces(author));

	    author = "Mill. ";
	    Assert.assertEquals("Mill.", TeamDefaultCacheStrategy.removeWhitespaces(author));

	    author = " Miller ";
	    Assert.assertEquals("Result should always be trimed", "Miller", TeamDefaultCacheStrategy.removeWhitespaces(author));

	    author = "A. Mill.";
        Assert.assertEquals("A.Mill.", TeamDefaultCacheStrategy.removeWhitespaces(author));

        author = "A. Mill.";
        Assert.assertEquals("A.Mill.", TeamDefaultCacheStrategy.removeWhitespaces(author));

        author = "A.   Mill.";
        Assert.assertEquals("A.Mill.", TeamDefaultCacheStrategy.removeWhitespaces(author));

        author = "A.   Mill. & B. Kohl.-Haber";
        Assert.assertEquals("A.Mill. & B.Kohl.-Haber", TeamDefaultCacheStrategy.removeWhitespaces(author));

        author = "A.   Mill. ,J. N. Bohl. f.& B. Kohl.-Haber";
        Assert.assertEquals("A.Mill.,J.N.Bohl.f. & B.Kohl.-Haber", TeamDefaultCacheStrategy.removeWhitespaces(author));

        author = " (Ab. ex CD. , All , Bet & J.Vall.) A.  Mill.ex Kohl.";
        Assert.assertEquals("(Ab. ex CD., All, Bet & J.Vall.) A.Mill. ex Kohl.", TeamDefaultCacheStrategy.removeWhitespaces(author));

	}
}
