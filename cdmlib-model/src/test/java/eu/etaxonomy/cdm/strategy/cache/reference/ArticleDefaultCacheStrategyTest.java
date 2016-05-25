// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.reference;


import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @date 16.06.2010
 *
 */
public class ArticleDefaultCacheStrategyTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ArticleDefaultCacheStrategyTest.class);

	private static IArticle article1;
	private static IJournal journal1;
	private static Team team1;
	private static Team team2;
	private static ArticleDefaultCacheStrategy defaultStrategy;
	private static final String detail1 = "55";

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		defaultStrategy = ArticleDefaultCacheStrategy.NewInstance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		article1 = ReferenceFactory.newArticle();
		article1.setCacheStrategy(defaultStrategy);
		journal1 = ReferenceFactory.newJournal();
		team1 = Team.NewInstance();
		team2 = Team.NewInstance();
		team1.setTitleCache("Team1", true);
		team1.setNomenclaturalTitle("T.", true);
		team2.setTitleCache("Team2", true);
		team2.setNomenclaturalTitle("TT.", true);
	}

//**************************** TESTS ***********************************


	@Test
	public void testGetTitleCache(){
		journal1.setTitle("My journal");
		journal1.setAuthorship(team2);
		article1.setTitle("My article");
		article1.setInJournal(journal1);
		article1.setAuthorship(team1);
		article1.setDatePublished(TimePeriod.NewInstance(1975));
		Assert.assertEquals("Team1, My article in My journal. 1975", article1.getTitleCache());

		article1.setInJournal(null);
		//TODO should not be needed here
		article1.setTitleCache(null, false);
		Assert.assertEquals("Team1, My article in " + ArticleDefaultCacheStrategy.UNDEFINED_JOURNAL + ". 1975", article1.getTitleCache());
	}

	@Ignore
	@Test
	//This test is just to show that there is still the title cache bug which is not
	//set to null by setInJournal(null)
	public void testGetTitleCache2(){
		journal1.setTitle("My journal");
		journal1.setAuthorship(team2);
		article1.setTitle("My article");
		article1.setInJournal(journal1);
		article1.setAuthorship(team1);
		article1.setDatePublished(TimePeriod.NewInstance(1975));
		Assert.assertEquals("Team1, My article in My journal. 1975", article1.getTitleCache());

		article1.setInJournal(null);
		Assert.assertEquals("Team1, My article in " + ArticleDefaultCacheStrategy.UNDEFINED_JOURNAL + ". 1975", article1.getTitleCache());
	}

	@Test
	public void testGetAbbrevTitleCache(){

		journal1.setTitle("My journal");
		journal1.setTitle("M. Journ.");
		journal1.setAuthorship(team2);
		article1.setTitle("My article");
		article1.setInJournal(journal1);
		article1.setAuthorship(team1);
		article1.setDatePublished(TimePeriod.NewInstance(1975));
		article1.setAbbrevTitle("M. Art.");
		Assert.assertEquals("T., M. Art. in M. Journ. 1975", article1.getAbbrevTitleCache());  //double dot may be removed in future #3645

		article1.setInJournal(null);
		//TODO should not be needed here
		article1.setTitleCache(null, false);
		Assert.assertEquals("Team1, My article in " + ArticleDefaultCacheStrategy.UNDEFINED_JOURNAL + ". 1975", article1.getTitleCache());
	}

	@Test
	public void testGetNomenclaturalCitation(){
		journal1.setTitle("My journal");
		journal1.setTitle("M. J.");
		journal1.setAuthorship(team2);
		article1.setTitle("My article");
		article1.setInJournal(journal1);
		article1.setAuthorship(team1);
		article1.setDatePublished(TimePeriod.NewInstance(1975));
		Assert.assertEquals("in M. J.: 55. 1975", article1.getNomenclaturalCitation(detail1));

		article1.setVolume("22");
		Assert.assertEquals("in M. J. 22: 55. 1975", article1.getNomenclaturalCitation(detail1));
		article1.setSeriesPart("ser. 11");
		Assert.assertEquals("in M. J., ser. 11, 22: 55. 1975", article1.getNomenclaturalCitation(detail1));
	}

	/**
	 * After ser. , sect. , abt. we want to have a comma, if there is not yet one following anyway
	 */
	@Test
	public void testGetNomenclaturalCitationSerSectAbt(){
		article1.setInJournal(journal1);
		article1.setVolume("22");
		journal1.setAbbrevTitle("J. Pl. Eur.");
		journal1.setAuthorship(team2);
		article1.setDatePublished(TimePeriod.NewInstance(1975));
		//no ser, sect, abt
		Assert.assertEquals("in J. Pl. Eur. 22: 55. 1975", article1.getNomenclaturalCitation(detail1));
		//ser
		journal1.setAbbrevTitle("J. Pl. Eur., ser. 3");
		Assert.assertEquals("in J. Pl. Eur., ser. 3, 22: 55. 1975", article1.getNomenclaturalCitation(detail1));
		journal1.setAbbrevTitle("J. Pl. Eur., Ser. 3");
		Assert.assertEquals("in J. Pl. Eur., Ser. 3, 22: 55. 1975", article1.getNomenclaturalCitation(detail1));
		journal1.setAbbrevTitle("J. Pl. Eur., ser. 3, s.n.");
		Assert.assertEquals("in J. Pl. Eur., ser. 3, s.n. 22: 55. 1975", article1.getNomenclaturalCitation(detail1));
		//sect
		journal1.setAbbrevTitle("J. Pl. Eur., sect. 3");
		Assert.assertEquals("in J. Pl. Eur., sect. 3, 22: 55. 1975", article1.getNomenclaturalCitation(detail1));
		journal1.setAbbrevTitle("J. Pl. Eur., Sect. 3");
		Assert.assertEquals("in J. Pl. Eur., Sect. 3, 22: 55. 1975", article1.getNomenclaturalCitation(detail1));
		journal1.setAbbrevTitle("J. Pl. Eur., Sect. 3, something");
		Assert.assertEquals("in J. Pl. Eur., Sect. 3, something 22: 55. 1975", article1.getNomenclaturalCitation(detail1));
		//abt
		journal1.setAbbrevTitle("J. Pl. Eur., abt. 3");
		Assert.assertEquals("in J. Pl. Eur., abt. 3, 22: 55. 1975", article1.getNomenclaturalCitation(detail1));
		journal1.setAbbrevTitle("J. Pl. Eur., Abt. 3");
		Assert.assertEquals("in J. Pl. Eur., Abt. 3, 22: 55. 1975", article1.getNomenclaturalCitation(detail1));
		journal1.setAbbrevTitle("J. Pl. Eur., abt. 3, no comma");
		Assert.assertEquals("in J. Pl. Eur., abt. 3, no comma 22: 55. 1975", article1.getNomenclaturalCitation(detail1));

		journal1.setAbbrevTitle("J. Pl. Eur., sect. 3");
		article1.setSeriesPart("1");
		Assert.assertEquals("in J. Pl. Eur., sect. 3, ser. 1, 22: 55. 1975", article1.getNomenclaturalCitation(detail1));
		article1.setSeriesPart("Series 2");
		Assert.assertEquals("in J. Pl. Eur., sect. 3, Series 2, 22: 55. 1975", article1.getNomenclaturalCitation(detail1));
	}



	@Test
	public void testGetTitleWithoutYearAndAuthor(){
		journal1.setTitle("My journal");
		journal1.setAuthorship(team2);
		article1.setTitle("My article");
		article1.setInJournal(journal1);
		article1.setAuthorship(team1);
		article1.setVolume("34");
		article1.setSeriesPart("ser. 2");
		article1.setDatePublished(TimePeriod.NewInstance(1975));
		Assert.assertEquals("in My journal, ser. 2, 34", defaultStrategy.getTitleWithoutYearAndAuthor((Reference)article1, false));
	}

	@Test
	public void testOldExistingBugs(){
		journal1.setTitle("Univ. Calif. Publ. Bot.");
		journal1.setAuthorship(null);

		Team articleAuthor = Team.NewTitledInstance("Babc. & Stebbins", "Babc. & Stebbins");
		article1.setTitle("");
		article1.setInJournal(journal1);
		article1.setAuthorship(articleAuthor);
		article1.setVolume("18");
		article1.setDatePublished(TimePeriod.NewInstance(1943));
		Assert.assertEquals("Babc. & Stebbins in Univ. Calif. Publ. Bot. 18. 1943", defaultStrategy.getTitleCache((Reference)article1));
	}

}
