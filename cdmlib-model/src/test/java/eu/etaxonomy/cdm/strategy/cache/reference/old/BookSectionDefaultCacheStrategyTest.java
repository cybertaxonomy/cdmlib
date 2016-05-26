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
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @date 16.06.2010
 *
 */
public class BookSectionDefaultCacheStrategyTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BookSectionDefaultCacheStrategyTest.class);

	private static IBookSection bookSection1;
	private static IBook book1;
	private static Team sectionTeam1;
	private static Team bookTeam1;
	private static BookSectionDefaultCacheStrategy defaultStrategy;
	private static final String detail1 = "55";

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		defaultStrategy = BookSectionDefaultCacheStrategy.NewInstance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		bookSection1 = ReferenceFactory.newBookSection();
		book1 = ReferenceFactory.newBook();
		sectionTeam1 = Team.NewTitledInstance("Section Author", "T.");
		bookTeam1 = Team.NewTitledInstance("Book Author", "TT.");
	}

//**************************** TESTS ***********************************


	@Test
	public void testGetTitleCache(){
		book1.setTitle("My book");
		book1.setAuthorship(bookTeam1);
		bookSection1.setTitle("My chapter");
		bookSection1.setInBook(book1);
		bookSection1.setAuthorship(sectionTeam1);
		book1.setDatePublished(TimePeriod.NewInstance(1975));
		Assert.assertEquals("Unexpected title cache.", "Section Author - My chapter in Book Author, My book. 1975", bookSection1.getTitleCache());
		book1.setDatePublished(null);
		bookSection1.setDatePublished(TimePeriod.NewInstance(1976));
		bookSection1.setTitleCache(null, false);
		book1.setTitleCache(null, false);
		Assert.assertEquals("Unexpected title cache.", "Section Author - My chapter in Book Author, My book. 1976", bookSection1.getTitleCache());
		book1.setDatePublished(TimePeriod.NewInstance(1977));
		bookSection1.setTitleCache(null, false);
		book1.setTitleCache(null, false);
		Assert.assertEquals("Unexpected title cache.", "Section Author - My chapter in Book Author, My book. 1976", bookSection1.getTitleCache());
		bookSection1.setTitleCache(null, false);
		book1.setTitleCache(null, false);
		book1.setSeriesPart("2");
		Assert.assertEquals("Unexpected title cache.", "Section Author - My chapter in Book Author, My book, ser. 2. 1976", bookSection1.getTitleCache());

		bookSection1.setInBook(null);
		bookSection1.setTitleCache(null, false);
		Assert.assertEquals("Unexpected title cache.", "Section Author - My chapter in - undefined book -. 1976", bookSection1.getTitleCache());

	}

	@Ignore
	@Test
	//This test is just to show that there is still the title cache bug which is not
	//set to null by setInBook(null) and others
	public void testGetTitleCache2(){
		book1.setTitle("My book");
		book1.setAuthorship(bookTeam1);
		bookSection1.setTitle("My chapter");
		bookSection1.setInBook(book1);
		bookSection1.setAuthorship(sectionTeam1);
		book1.setDatePublished(TimePeriod.NewInstance(1975));
		Assert.assertEquals("Unexpected title cache.", "Section Author - My chapter in Book Author, My book. 1975", bookSection1.getTitleCache());
		book1.setDatePublished(null);
		bookSection1.setDatePublished(TimePeriod.NewInstance(1976));
		Assert.assertEquals("Unexpected title cache.", "Section Author - My chapter in Book Author, My book. 1976", bookSection1.getTitleCache());
		book1.setDatePublished(TimePeriod.NewInstance(1977));
		Assert.assertEquals("Unexpected title cache.", "Section Author - My chapter in Book Author, My book. 1976", bookSection1.getTitleCache());


		bookSection1.setInBook(null);
		Assert.assertEquals("Unexpected title cache.", "Section Author - My chapter in - undefined book -. 1976", bookSection1.getTitleCache());

	}


	@Test
	public void testGetNomenclaturalCitation(){
		book1.setTitle("My book");
		book1.setAuthorship(bookTeam1);
		bookSection1.setTitle("My chapter");
		bookSection1.setInBook(book1);
		bookSection1.setAuthorship(sectionTeam1);
		book1.setDatePublished(TimePeriod.NewInstance(1975));
		//TODO still unclear which is correct
//		Assert.assertEquals("in Book Author, My book: 55. 1975", bookSection1.getNomenclaturalCitation(detail1));
		Assert.assertEquals("in TT., My book: 55. 1975", bookSection1.getNomenclaturalCitation(detail1));

		book1.setSeriesPart("2");
		Assert.assertEquals("in TT., My book, ser. 2: 55. 1975", bookSection1.getNomenclaturalCitation(detail1));
	}

	@Test
	public void testRealExample(){
		Team bookTeam = Team.NewTitledInstance("Chaudhary S. A.(ed.)", "Chaudhary S. A.(ed.)");
		IBook book = ReferenceFactory.newBook();
		book.setTitle("Flora of the Kingdom of Saudi Arabia");
		book.setAuthorship(bookTeam);
		book.setVolume("2(3)");
		book.setPlacePublished("Riyadh");
		book.setPublisher("National Herbarium");
		book.setDatePublished(TimePeriod.NewInstance(2000));

		Team sectionTeam = Team.NewTitledInstance("Chaudhary S. A.", "Chaudhary S. A.");
		IBookSection bookSection = ReferenceFactory.newBookSection();
		bookSection.setTitle("73. Hedypnois - 87. Crepis");
		bookSection.setInBook(book);
		bookSection.setAuthorship(sectionTeam);
		bookSection.setPages("222-251");
		Assert.assertEquals("Chaudhary S. A. - 73. Hedypnois - 87. Crepis in Chaudhary S. A.(ed.), Flora of the Kingdom of Saudi Arabia 2(3). 2000", bookSection.getTitleCache());

	}

}
