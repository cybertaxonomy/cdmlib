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


import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
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
		journal1.setAuthorTeam(team2);
		article1.setTitle("My article");
		article1.setInJournal(journal1);
		article1.setAuthorTeam(team1);
		article1.setDatePublished(TimePeriod.NewInstance(1975));
		Assert.assertEquals("Team1, My article in My journal. 1975", article1.getTitleCache());

		article1.setInJournal(null);
		//TODO should not be needed here
		article1.setTitleCache(null);
		Assert.assertEquals("Team1, My article in " + ArticleDefaultCacheStrategy.UNDEFINED_JOURNAL + ". 1975", article1.getTitleCache());	
	}
	
	@Ignore
	@Test
	//This test is just to show that there is still the title cache bug which is not
	//set to null by setInJournal(null)
	public void testGetTitleCache2(){
		journal1.setTitle("My journal");
		journal1.setAuthorTeam(team2);
		article1.setTitle("My article");
		article1.setInJournal(journal1);
		article1.setAuthorTeam(team1);
		article1.setDatePublished(TimePeriod.NewInstance(1975));
		Assert.assertEquals("Team1, My article in My journal. 1975", article1.getTitleCache());

		article1.setInJournal(null);
		Assert.assertEquals("Team1, My article in " + ArticleDefaultCacheStrategy.UNDEFINED_JOURNAL + ". 1975", article1.getTitleCache());	
	}

	@Test
	public void testGetNomenclaturalCitation(){
		journal1.setTitle("My journal");
		journal1.setAuthorTeam(team2);
		article1.setTitle("My article");
		article1.setInJournal(journal1);
		article1.setAuthorTeam(team1);
		article1.setDatePublished(TimePeriod.NewInstance(1975));
		Assert.assertEquals("in My journal: 55. 1975", article1.getNomenclaturalCitation(detail1));
	}

	
	@Test 
	public void testGetNomRefTitleWithoutYearAndAuthor(){
		journal1.setTitle("My journal");
		journal1.setAuthorTeam(team2);
		article1.setTitle("My article");
		article1.setInJournal(journal1);
		article1.setAuthorTeam(team1);
		article1.setVolume("34");
		article1.setSeries("ser. 2");
		article1.setDatePublished(TimePeriod.NewInstance(1975));
		Assert.assertEquals("in My journal ser. 2, 34", defaultStrategy.getNomRefTitleWithoutYearAndAuthor((ReferenceBase)article1));
	}
	
}
