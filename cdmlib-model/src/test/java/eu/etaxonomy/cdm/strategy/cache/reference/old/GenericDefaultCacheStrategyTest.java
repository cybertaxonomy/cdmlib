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


import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IGeneric;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @date 16.06.2010
 *
 */
public class GenericDefaultCacheStrategyTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GenericDefaultCacheStrategyTest.class);

	private static IGeneric generic1;
	private static Team team1;
	private static GenericDefaultCacheStrategy defaultStrategy;
	private static final String detail1 = "55";

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		defaultStrategy = GenericDefaultCacheStrategy.NewInstance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		generic1 = ReferenceFactory.newGeneric();
		generic1.setCacheStrategy(defaultStrategy);
		team1 = Team.NewTitledInstance("Authorteam", "AT.");
	}

//**************************** TESTS ***********************************


	@Test
	public void testGetTitleCache(){
		generic1.setTitle("auct.");
		Assert.assertEquals("Unexpected title cache.", "auct.", generic1.getTitleCache());
	}


	@Test
	public void testGetInRef(){
		generic1.setTitle("auct.");
		IBook book1 = ReferenceFactory.newBook();
		book1.setTitle("My book title");
		book1.setAuthorship(team1);
		Reference inRef = (Reference)book1;
		generic1.setInReference(inRef);
		generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
		//TODO author still unclear
//		Assert.assertEquals("Unexpected title cache.", "in Authorteam, My book title: 2", generic1.getNomenclaturalCitation("2"));
		Assert.assertEquals("Unexpected title cache.", "in AT., My book title: 2", generic1.getNomenclaturalCitation("2"));
	}

	@Test
	public void testGetInRefWithoutInRef(){
		generic1.setTitle("My generic title");
		generic1.setAuthorship(team1);
		generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
		Assert.assertEquals("Unexpected title cache.", "My generic title: 2", generic1.getNomenclaturalCitation("2"));
	}

	@Test
	public void testGetTitleCache2(){
		generic1.setTitle("Part Title");
		IBook book1 = ReferenceFactory.newBook();
		book1.setTitle("My book title");
		book1.setAuthorship(team1);
		Reference inRef = (Reference)book1;
		generic1.setInReference(inRef);
		generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
		Assert.assertEquals("Unexpected title cache.", "Part Title in Authorteam, My book title", generic1.getTitleCache());
	}


	@Test
	public void testGetAbbrevTitleCache(){
		generic1.setTitle("Part Title");
		generic1.setAbbrevTitle("Pt. Tit.");
		generic1.setDatePublished(TimePeriodParser.parseString("1987"));
		IBook book1 = ReferenceFactory.newBook();
		book1.setTitle("My book title");
		book1.setAbbrevTitle("My bk. tit.");
		book1.setAuthorship(team1);  //TODO handling not yet defined
		Reference inRef = (Reference)book1;
		generic1.setInReference(inRef);
		generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
		generic1.setAbbrevTitleCache(null, false);  //reset cache in case aspectJ is not enabled
		Assert.assertEquals("Unexpected abbrev title cache.", "Pt. Tit. in AT., My bk. tit. 1987", generic1.getAbbrevTitleCache());
		Assert.assertEquals("Title cache must still be the same", "Part Title in Authorteam, My book title. 1987", generic1.getTitleCache());
		//TODO author still unclear
//		Assert.assertEquals("Unexpected nom. ref.", "in Authorteam, My bk. tit.: pp. 44. 1987", generic1.getNomenclaturalCitation("pp. 44"));
		Assert.assertEquals("Unexpected nom. ref.", "in AT., My bk. tit.: pp. 44. 1987", generic1.getNomenclaturalCitation("pp. 44"));
		generic1.setVolume("23");
		Assert.assertEquals("Unexpected nom. ref.", "in AT., My bk. tit. 23: pp. 44. 1987", generic1.getNomenclaturalCitation("pp. 44"));
		generic1.setSeriesPart("ser. 11");
		//TODO
//		Assert.assertEquals("Unexpected nom. ref.", "in AT., My bk. tit., ser. 11, 23: pp. 44. 1987", generic1.getNomenclaturalCitation("pp. 44"));


		//protected
		generic1.setAbbrevTitleCache("My prot. abb. tit. in a bk.", true);
		Assert.assertEquals("Unexpected abbrev title cache.", "My prot. abb. tit. in a bk.", generic1.getAbbrevTitleCache());
		Assert.assertEquals("Unexpected title cache.", "Part Title in Authorteam, My book title. 1987", generic1.getTitleCache());

		generic1.setDatePublished(null);
		Assert.assertEquals("Unexpected nom. ref.", "My prot. abb. tit. in a bk.", generic1.getNomenclaturalCitation(null));
		Assert.assertEquals("Unexpected nom. ref.", "My prot. abb. tit. in a bk.", generic1.getNomenclaturalCitation(""));
		Assert.assertEquals("Unexpected nom. ref.", "My prot. abb. tit. in a bk.: pp. 44", generic1.getNomenclaturalCitation("pp. 44"));

		generic1.setDatePublished(TimePeriodParser.parseString("1893"));
		Assert.assertEquals("Unexpected nom. ref.", "My prot. abb. tit. in a bk.: pp. 44. 1893", generic1.getNomenclaturalCitation("pp. 44"));

	}

	@Test
	public void testGetTitleCacheWithoutInRef(){
		generic1.setTitle("My generic title");
		generic1.setAuthorship(team1);
		generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
		Assert.assertEquals("Unexpected title cache.", "Authorteam, My generic title", generic1.getTitleCache());
	}

	@Test
	public void testAuthorOnly(){
		generic1.setAuthorship(team1);
		generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
		Assert.assertEquals("Unexpected title cache.", "Authorteam", generic1.getTitleCache());
		Assert.assertEquals("", generic1.getNomenclaturalCitation(null));
	}

	@Test
	public void testYearAndAuthorOnly(){
		generic1.setAuthorship(team1);
		generic1.setDatePublished(TimePeriodParser.parseString("1792"));
		generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
		Assert.assertEquals("Unexpected title cache.", "Authorteam, 1792", generic1.getTitleCache());
		Assert.assertEquals("1792", generic1.getNomenclaturalCitation(null));
	}

	@Test
	public void testDoubleDotBeforeYear(){
		generic1.setAuthorship(team1);
		String detail = "sine no.";
		generic1.setAbbrevTitle("My title");
		generic1.setDatePublished(TimePeriodParser.parseString("1883-1884"));
		generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
		Assert.assertEquals("My title: sine no. 1883-1884", generic1.getNomenclaturalCitation(detail));
	}



}
