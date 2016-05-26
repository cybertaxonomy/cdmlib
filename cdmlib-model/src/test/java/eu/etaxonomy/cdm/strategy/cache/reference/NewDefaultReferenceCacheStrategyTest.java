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


import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.IGeneric;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.IWebPage;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.cache.reference.old.ArticleDefaultCacheStrategyTest;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * Copy of {@link ArticleDefaultCacheStrategyTest} to test the {@link NewDefaultReferenceCacheStrategy}.
 *
 * @author a.mueller
 * @date 25.05.2016
 *
 */
public class NewDefaultReferenceCacheStrategyTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NewDefaultReferenceCacheStrategyTest.class);

	//article
	private static IArticle article1;
	private static IJournal journal1;
	private static Team articleTeam1;
	private static Team articleTeam2;

	//book // book section
	private static IBook book1;
    private static Team bookTeam1;

    //book section
    private static IBookSection bookSection1;
    private static Team sectionTeam1;

    //CdDvd
    private static Reference cdDvd;
    private static String cdDvdTitle;

    //Generic
    private static IGeneric generic1;
    private static Team genericTeam1;

    //WebPage
    private static IWebPage webPage1;
    private static Team webPageTeam1;

	//common
	private static NewDefaultReferenceCacheStrategy defaultStrategy;
	private static final String detail1 = "55";

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		defaultStrategy = NewDefaultReferenceCacheStrategy.NewInstance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	    //article
		article1 = ReferenceFactory.newArticle();
		article1.setCacheStrategy(defaultStrategy);
		journal1 = ReferenceFactory.newJournal();
		journal1.setCacheStrategy(defaultStrategy);
		articleTeam1 = Team.NewInstance();
		articleTeam2 = Team.NewInstance();
		articleTeam1.setTitleCache("Team1", true);
		articleTeam1.setNomenclaturalTitle("T.", true);
		articleTeam2.setTitleCache("Team2", true);
		articleTeam2.setNomenclaturalTitle("TT.", true);

		//book / section
		book1 = ReferenceFactory.newBook();
		book1.setCacheStrategy(defaultStrategy);
        bookTeam1 = Team.NewTitledInstance("Book Author", "TT.");
        bookSection1 = ReferenceFactory.newBookSection();
        bookSection1.setCacheStrategy(defaultStrategy);
        sectionTeam1 = Team.NewTitledInstance("Section Author", "T.");

        //CdDvd
        cdDvd = ReferenceFactory.newCdDvd();
        cdDvdTitle = "A nice CD title";
        cdDvd.setTitle(cdDvdTitle);
        String publisher = "An ugly publisher";  //not yet implemented
        String place = "A beutiful place";  //not yet implemented
        TimePeriod publicationDate = TimePeriod.NewInstance(1999, 2001);
        cdDvd.setDatePublished(publicationDate);

        //Generic
        generic1 = ReferenceFactory.newGeneric();
        generic1.setCacheStrategy(defaultStrategy);
        genericTeam1 = Team.NewTitledInstance("Authorteam", "AT.");

        //WebPage
        webPage1 = ReferenceFactory.newWebPage();
        webPage1.setCacheStrategy(defaultStrategy);
        webPageTeam1 = Team.NewTitledInstance("Authorteam, D.", "AT.");
	}

//**************************** TESTS ***********************************


	@Test
	public void testArticleGetTitleCache(){
		journal1.setTitle("My journal");
		journal1.setAuthorship(articleTeam2);
		article1.setTitle("My article");
		article1.setInJournal(journal1);
		article1.setAuthorship(articleTeam1);
		article1.setDatePublished(TimePeriod.NewInstance(1975));
		Assert.assertEquals("Team1, My article in My journal. 1975", article1.getTitleCache());

		article1.setInJournal(null);
		//TODO should not be needed here
		article1.setTitleCache(null, false);
		Assert.assertEquals("Team1, My article in " + NewDefaultReferenceCacheStrategy.UNDEFINED_JOURNAL + ". 1975", article1.getTitleCache());
	}

	@Ignore
	@Test
	//This test is just to show that there is still the title cache bug which is not
	//set to null by setInJournal(null)
	public void testArticleGetTitleCache2(){
		journal1.setTitle("My journal");
		journal1.setAuthorship(articleTeam2);
		article1.setTitle("My article");
		article1.setInJournal(journal1);
		article1.setAuthorship(articleTeam1);
		article1.setDatePublished(TimePeriod.NewInstance(1975));
		Assert.assertEquals("Team1, My article in My journal. 1975", article1.getTitleCache());

		article1.setInJournal(null);
		Assert.assertEquals("Team1, My article in " + NewDefaultReferenceCacheStrategy.UNDEFINED_JOURNAL + ". 1975", article1.getTitleCache());
	}

	@Test
	public void testArticleGetAbbrevTitleCache(){

		journal1.setTitle("My journal");
		journal1.setTitle("M. Journ.");
		journal1.setAuthorship(articleTeam2);
		article1.setTitle("My article");
		article1.setInJournal(journal1);
		article1.setAuthorship(articleTeam1);
		article1.setDatePublished(TimePeriod.NewInstance(1975));
		article1.setAbbrevTitle("M. Art.");
		Assert.assertEquals("T., M. Art. in M. Journ. 1975", article1.getAbbrevTitleCache());  //double dot may be removed in future #3645

		article1.setInJournal(null);
		//TODO should not be needed here
		article1.setTitleCache(null, false);
		Assert.assertEquals("Team1, My article in " + NewDefaultReferenceCacheStrategy.UNDEFINED_JOURNAL + ". 1975", article1.getTitleCache());
	}

	@Test
	public void testArticleGetNomenclaturalCitation(){
		journal1.setTitle("My journal");
		journal1.setTitle("M. J.");
		journal1.setAuthorship(articleTeam2);
		article1.setTitle("My article");
		article1.setInJournal(journal1);
		article1.setAuthorship(articleTeam1);
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
	public void testArticleGetNomenclaturalCitationSerSectAbt(){
		article1.setInJournal(journal1);
		article1.setVolume("22");
		journal1.setAbbrevTitle("J. Pl. Eur.");
		journal1.setAuthorship(articleTeam2);
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
	public void testArticleGetTitleWithoutYearAndAuthor(){
		journal1.setTitle("My journal");
		journal1.setAuthorship(articleTeam2);
		article1.setTitle("My article");
		article1.setInJournal(journal1);
		article1.setAuthorship(articleTeam1);
		article1.setVolume("34");
		article1.setSeriesPart("ser. 2");
		article1.setDatePublished(TimePeriod.NewInstance(1975));
		//FIXME removed for new formatter
//		Assert.assertEquals("in My journal, ser. 2, 34", defaultStrategy.getTitleWithoutYearAndAuthor((Reference)article1, false));
	}

	@Test
	public void testArticleOldExistingBugs(){
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

// ******************* Book ****************************/

   @Test
    public void testBookGetTitleCache0(){
        book1.setTitle("My book");
        book1.setAuthorship(bookTeam1);
        book1.setDatePublished(TimePeriod.NewInstance(1975));
        Assert.assertEquals("Unexpected title cache.", "Book Author, My book. 1975", book1.getTitleCache());

        book1.setTitleCache(null, false);
        book1.setEdition("ed. 3");
        Assert.assertEquals("Unexpected title cache.", "Book Author, My book, ed. 3. 1975", book1.getTitleCache());

        TimePeriod newDatePublished = TimePeriodParser.parseString("1975 (after Aug.)");
        book1.setDatePublished(newDatePublished);
        book1.setTitleCache(null, false);
        //TODO this behaviour needs to be discussed. Maybe better the complete date published string should be returned.
        Assert.assertEquals("Unexpected title cache.", "Book Author, My book, ed. 3", book1.getTitleCache());

    }


    @Test
    public void testBookGetTitleCache1(){
        //series
        IBook book1 = ReferenceFactory.newBook();
        book1.setAbbrevTitle("Acta Inst. Bot. Acad. Sci. URSS");
        book1.setSeriesPart("1");
        book1.setVolume("Fasc. 11");
        book1.setDatePublished(TimePeriodParser.parseString("1955"));
        Assert.assertEquals("Unexpected abbrev title cache", "Acta Inst. Bot. Acad. Sci. URSS, ser. 1, Fasc. 11. 1955", book1.getTitleCache());
        Assert.assertEquals("Unexpected nomencl. reference", "Acta Inst. Bot. Acad. Sci. URSS, ser. 1, Fasc. 11: 248. 1955", book1.getNomenclaturalCitation("248"));
    }


    @Test
    public void testBookGetTitleCache2(){
        //series
        IBook book1 = ReferenceFactory.newBook();
        book1.setAbbrevTitle("Acta Inst. Bot. Acad. Sci. URSS");
        book1.setVolume("Fasc. 11");
        book1.setDatePublished(TimePeriodParser.parseString("1955"));
        Assert.assertEquals("Unexpected abbrev title cache", "Acta Inst. Bot. Acad. Sci. URSS Fasc. 11. 1955", book1.getTitleCache());
        Assert.assertEquals("Unexpected nomencl. reference", "Acta Inst. Bot. Acad. Sci. URSS Fasc. 11: 248. 1955", book1.getNomenclaturalCitation("248"));
        book1.setSeriesPart("1");
        Assert.assertEquals("Unexpected nomencl. reference", "Acta Inst. Bot. Acad. Sci. URSS, ser. 1, Fasc. 11: 248. 1955", book1.getNomenclaturalCitation("248"));
    }

    @Test
    public void testBookGetNomenclaturalCitation(){
        book1.setTitle("My book");
        book1.setAuthorship(bookTeam1);
        book1.setDatePublished(TimePeriod.NewInstance(1975));
        Assert.assertEquals("My book: 55. 1975", book1.getNomenclaturalCitation(detail1));
        book1.setAbbrevTitle("Analect. Bot.");
        Assert.assertEquals("Analect. Bot. 1975", book1.getNomenclaturalCitation(null));
    }

// ***************************** Book Section ************************/

    @Test
    public void testBookSectionGetTitleCache(){
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
    public void testBookSectionGetTitleCache2(){
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
    public void testBookSectionGetNomenclaturalCitation(){
        book1.setTitle("My book");
        book1.setAuthorship(bookTeam1);
        bookSection1.setTitle("My chapter");
        bookSection1.setInBook(book1);
        bookSection1.setAuthorship(sectionTeam1);
        book1.setDatePublished(TimePeriod.NewInstance(1975));
        //TODO still unclear which is correct
//      Assert.assertEquals("in Book Author, My book: 55. 1975", bookSection1.getNomenclaturalCitation(detail1));
        Assert.assertEquals("in TT., My book: 55. 1975", bookSection1.getNomenclaturalCitation(detail1));

        book1.setSeriesPart("2");
        Assert.assertEquals("in TT., My book, ser. 2: 55. 1975", bookSection1.getNomenclaturalCitation(detail1));
    }

    @Test
    public void testBookSectionRealExample(){
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

    @Test
    public void testCdDvdGetTitleWithoutYearAndAuthor() {
        String result = TitleWithoutYearAndAuthorHelper.getTitleWithoutYearAndAuthor(cdDvd, false);
        assertEquals(cdDvdTitle, result);
    }

    //TODO missing publicationPlace and publisher has to be discussed
    @Test
    public void testCdDvdGetTitleCache() {
        String result = defaultStrategy.getTitleCache(cdDvd);
        assertEquals(cdDvdTitle + ". 1999-2001", result);
    }

// *************************** GENERIC *****************************************/

    @Test
    public void testGenericGetTitleCache(){
        generic1.setTitle("auct.");
        Assert.assertEquals("Unexpected title cache.", "auct.", generic1.getTitleCache());
    }


    @Test
    public void testGenericGetInRef(){
        generic1.setTitle("auct.");
        IBook book1 = ReferenceFactory.newBook();
        book1.setTitle("My book title");
        book1.setAuthorship(genericTeam1);
        Reference inRef = (Reference)book1;
        generic1.setInReference(inRef);
        generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
        //TODO author still unclear
//      Assert.assertEquals("Unexpected title cache.", "in Authorteam, My book title: 2", generic1.getNomenclaturalCitation("2"));
        Assert.assertEquals("Unexpected title cache.", "in AT., My book title: 2", generic1.getNomenclaturalCitation("2"));
    }

    @Test
    public void testGenericGetInRefWithoutInRef(){
        generic1.setTitle("My generic title");
        generic1.setAuthorship(genericTeam1);
        generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
        Assert.assertEquals("Unexpected title cache.", "My generic title: 2", generic1.getNomenclaturalCitation("2"));
    }

    @Test
    public void testGenericGetTitleCache2(){
        generic1.setTitle("Part Title");
        IBook book1 = ReferenceFactory.newBook();
        book1.setTitle("My book title");
        book1.setAuthorship(genericTeam1);
        Reference inRef = (Reference)book1;
        generic1.setInReference(inRef);
        generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
        Assert.assertEquals("Unexpected title cache.", "Part Title in Authorteam, My book title", generic1.getTitleCache());
    }


    @Test
    public void testGenericGetAbbrevTitleCache(){
        generic1.setTitle("Part Title");
        generic1.setAbbrevTitle("Pt. Tit.");
        generic1.setDatePublished(TimePeriodParser.parseString("1987"));
        IBook book1 = ReferenceFactory.newBook();
        book1.setTitle("My book title");
        book1.setAbbrevTitle("My bk. tit.");
        book1.setAuthorship(genericTeam1);  //TODO handling not yet defined
        Reference inRef = (Reference)book1;
        generic1.setInReference(inRef);
        generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
        generic1.setAbbrevTitleCache(null, false);  //reset cache in case aspectJ is not enabled
        Assert.assertEquals("Unexpected abbrev title cache.", "Pt. Tit. in AT., My bk. tit. 1987", generic1.getAbbrevTitleCache());
        Assert.assertEquals("Title cache must still be the same", "Part Title in Authorteam, My book title. 1987", generic1.getTitleCache());
        //TODO author still unclear
//      Assert.assertEquals("Unexpected nom. ref.", "in Authorteam, My bk. tit.: pp. 44. 1987", generic1.getNomenclaturalCitation("pp. 44"));
        Assert.assertEquals("Unexpected nom. ref.", "in AT., My bk. tit.: pp. 44. 1987", generic1.getNomenclaturalCitation("pp. 44"));
        generic1.setVolume("23");
        Assert.assertEquals("Unexpected nom. ref.", "in AT., My bk. tit. 23: pp. 44. 1987", generic1.getNomenclaturalCitation("pp. 44"));
        generic1.setSeriesPart("ser. 11");
        //TODO
//      Assert.assertEquals("Unexpected nom. ref.", "in AT., My bk. tit., ser. 11, 23: pp. 44. 1987", generic1.getNomenclaturalCitation("pp. 44"));


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
    public void testGenericGetTitleCacheWithoutInRef(){
        generic1.setTitle("My generic title");
        generic1.setAuthorship(genericTeam1);
        generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
        Assert.assertEquals("Unexpected title cache.", "Authorteam, My generic title", generic1.getTitleCache());
    }

    @Test
    public void testGenericAuthorOnly(){
        generic1.setAuthorship(genericTeam1);
        generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
        Assert.assertEquals("Unexpected title cache.", "Authorteam", generic1.getTitleCache());
        Assert.assertEquals("", generic1.getNomenclaturalCitation(null));
    }

    @Test
    public void testGenericYearAndAuthorOnly(){
        generic1.setAuthorship(genericTeam1);
        generic1.setDatePublished(TimePeriodParser.parseString("1792"));
        generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
        Assert.assertEquals("Unexpected title cache.", "Authorteam, 1792", generic1.getTitleCache());
        Assert.assertEquals("1792", generic1.getNomenclaturalCitation(null));
    }

    @Test
    public void testGenericDoubleDotBeforeYear(){
        generic1.setAuthorship(genericTeam1);
        String detail = "sine no.";
        generic1.setAbbrevTitle("My title");
        generic1.setDatePublished(TimePeriodParser.parseString("1883-1884"));
        generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
        Assert.assertEquals("My title: sine no. 1883-1884", generic1.getNomenclaturalCitation(detail));
    }

// ********************************** WEB PAGE ********************************************/

    @Test
//    @Ignore //under development
    public void testWebPageGetTitleCache(){
        webPage1.setTitle("Flora of Israel Online");
        webPage1.setUri(URI.create("http://flora.huji.ac.il"));
        webPage1.setAuthorship(webPageTeam1);
        webPage1.setDatePublished(TimePeriodParser.parseString("[accessed in 2011]"));
        //taken from Berlin Model, may be modified in future
        Assert.assertEquals("Unexpected title cache.", "Authorteam, D. - Flora of Israel Online - http://flora.huji.ac.il [accessed in 2011]", webPage1.getTitleCache());
    }

//  @Test
//  //WebPages should usually not be used as nomencl.reference, therefore this is less important
//  public void testWebPageGetAbbrevTitleCache(){
//      webPage1.setTitle("auct.");
//      Assert.assertEquals("Unexpected title cache.", "auct.", webPage1.getTitleCache());
//  }


}
