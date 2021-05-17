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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.IGeneric;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.IWebPage;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * Copy of {@link ArticleDefaultCacheStrategyTest} to test the {@link ReferenceDefaultCacheStrategy}.
 *
 * @author a.mueller
 * @since 25.05.2016
 */
public class ReferenceDefaultCacheStrategyTest {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ReferenceDefaultCacheStrategyTest.class);

	private static final String SEP = TimePeriod.SEP;

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
	private static ReferenceDefaultCacheStrategy defaultStrategy;
	private static final String detail1 = "55";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		defaultStrategy = ReferenceDefaultCacheStrategy.NewInstance();
	}

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
        sectionTeam1 = Team.NewTitledInstance("Section Author", "T.");

        //CdDvd
        cdDvd = ReferenceFactory.newCdDvd();
        cdDvd.setCacheStrategy(defaultStrategy);
        cdDvdTitle = "A nice CD title";
        cdDvd.setTitle(cdDvdTitle);
        String publisher = "An ugly publisher";  //not yet implemented
        String place = "A beutiful place";  //not yet implemented
        VerbatimTimePeriod publicationDate = VerbatimTimePeriod.NewVerbatimInstance(1999, 2001);
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
		((Reference)journal1).setAuthorship(articleTeam2);  //incorrect use anyway
		article1.setTitle("My article");
		article1.setInJournal(journal1);
		article1.setAuthorship(articleTeam1);
		article1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1975));
		Assert.assertEquals("Team1, My article. "+UTF8.EN_DASH+" My journal. 1975", article1.getTitleCache());

		article1.setInJournal(null);
		//TODO should not be needed here
		article1.setTitleCache(null, false);
		Assert.assertEquals("Team1, My article. "+UTF8.EN_DASH+" " + ReferenceDefaultCacheStrategy.UNDEFINED_JOURNAL + ". 1975", article1.getTitleCache());
	}

	@Test
	//This test is just to show that setInJournal(null) now resets caches  #1815
	public void testSetInJournal(){
		journal1.setTitle("My journal");
		((Reference)journal1).setAuthorship(articleTeam2);
		article1.setTitle("My article");
		article1.setInJournal(journal1);
		article1.setAuthorship(articleTeam1);
		article1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1975));
		Assert.assertEquals("Team1, My article. "+UTF8.EN_DASH+" My journal. 1975", article1.getTitleCache());

		article1.setInJournal(null);
		Assert.assertEquals("Team1, My article. "+UTF8.EN_DASH+" " + ReferenceDefaultCacheStrategy.UNDEFINED_JOURNAL + ". 1975", article1.getTitleCache());
        article1.setDatePublished(null);
        Assert.assertEquals("Team1, My article. "+UTF8.EN_DASH+" " + ReferenceDefaultCacheStrategy.UNDEFINED_JOURNAL, article1.getTitleCache());
	}

	//#6496
    @Test
    public void testArticleGetTitleCacheWithPages(){
        journal1.setTitle("My journal");
        ((Reference)journal1).setAuthorship(articleTeam2);
        article1.setTitle("My article");
        article1.setInJournal(journal1);
        article1.setAuthorship(articleTeam1);
        article1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1975));
        Assert.assertEquals("Team1, My article. "+UTF8.EN_DASH+" My journal. 1975", article1.getTitleCache());
        article1.setPages("12-22");
        Assert.assertEquals("Team1, My article. "+UTF8.EN_DASH+" My journal: 12-22. 1975", article1.getTitleCache());

        article1.setVolume("7");
        Assert.assertEquals("Team1, My article. "+UTF8.EN_DASH+" My journal 7: 12-22. 1975", article1.getTitleCache());

        article1.setSeriesPart("II");
        //TODO unclear if punctuation is correct
        Assert.assertEquals("Team1, My article. "+UTF8.EN_DASH+" My journal, II, 7: 12-22. 1975", article1.getTitleCache());
   }

	@Test
	public void testArticleGetAbbrevTitleCache(){

		journal1.setTitle("My journal");
		journal1.setTitle("M. Journ.");
		((Reference)journal1).setAuthorship(articleTeam2);
		article1.setTitle("My article");
		article1.setInJournal(journal1);
		article1.setAuthorship(articleTeam1);
		article1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1975));
		article1.setAbbrevTitle("M. Art.");
		Assert.assertEquals("T., M. Art. "+UTF8.EN_DASH+" M. Journ. 1975", article1.getAbbrevTitleCache());  //double dot may be removed in future #3645

		article1.setInJournal(null);
		article1.setTitleCache(null, false);
		Assert.assertEquals("Team1, My article. "+UTF8.EN_DASH+" " + ReferenceDefaultCacheStrategy.UNDEFINED_JOURNAL + ". 1975", article1.getTitleCache());
	}

	@Test
	public void testArticleGetTitleWithoutYearAndAuthor() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		journal1.setTitle("My journal");
		journal1.setAbbrevTitle("My journ.");
		((Reference)journal1).setAuthorship(articleTeam2);
		article1.setTitle("My article");
		article1.setAbbrevTitle("My art.");
		article1.setInJournal(journal1);
		article1.setAuthorship(articleTeam1);
		article1.setVolume("34");
		article1.setSeriesPart("ser. 2");
		article1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1975));
		Method method = ReferenceDefaultCacheStrategy.class.getDeclaredMethod("getTitleWithoutYearAndAuthor", Reference.class, boolean.class, boolean.class);
		method.setAccessible(true);
		Assert.assertEquals(UTF8.EN_DASH + " My journal, ser. 2, 34", method.invoke(defaultStrategy, article1, false, false));
		Assert.assertEquals(UTF8.EN_DASH + " My journ., ser. 2, 34", method.invoke(defaultStrategy, article1, true, false));
		Assert.assertEquals("in My journal, ser. 2, 34", method.invoke(defaultStrategy, article1, false, true));
		Assert.assertEquals("in My journ., ser. 2, 34", method.invoke(defaultStrategy, article1, true, true));
	}

	@Test
	public void testArticleOldExistingBugs(){
		journal1.setTitle("Univ. Calif. Publ. Bot.");
		((Reference)journal1).setAuthorship(null);

		Team articleAuthor = Team.NewTitledInstance("Babc. & Stebbins", "Babc. & Stebbins");
		article1.setTitle("");
		article1.setInJournal(journal1);
		article1.setAuthorship(articleAuthor);
		article1.setVolume("18");
		article1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1943));
		//TODO what is the correct separator here? #9529
		Assert.assertEquals("Babc. & Stebbins "+UTF8.EN_DASH+" Univ. Calif. Publ. Bot. 18. 1943", defaultStrategy.getTitleCache((Reference)article1));
	}

// ******************* Book ****************************/

   @Test
    public void testBookGetTitleCache0(){
        book1.setTitle("My book");
        book1.setAuthorship(bookTeam1);
        book1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1975));
        Assert.assertEquals("Unexpected title cache.", "Book Author, My book. 1975", book1.getTitleCache());

        book1.setTitleCache(null, false);
        book1.setEdition("ed. 3");
        Assert.assertEquals("Unexpected title cache.", "Book Author, My book, ed. 3. 1975", book1.getTitleCache());

        VerbatimTimePeriod newDatePublished = TimePeriodParser.parseStringVerbatim("1975 (after Aug.)");
        book1.setDatePublished(newDatePublished);
        book1.setTitleCache(null, false);
        //TODO this behaviour needs to be discussed. Maybe better the complete date published string should be returned.
        Assert.assertEquals("Unexpected title cache.", "Book Author, My book, ed. 3", book1.getTitleCache());

        book1.setPages("1-405");
        Assert.assertEquals("Unexpected title cache.", "Book Author, My book, ed. 3: 1-405", book1.getTitleCache());

    }

    @Test
    public void testBookGetTitleCache1(){
        //series
        IBook book1 = ReferenceFactory.newBook();
        book1.setAbbrevTitle("Acta Inst. Bot. Acad. Sci. URSS");
        book1.setVolume("Fasc. 11");
        book1.setDatePublished(TimePeriodParser.parseStringVerbatim("1955"));
        Assert.assertEquals("Unexpected abbrev title cache", "Acta Inst. Bot. Acad. Sci. URSS Fasc. 11. 1955", book1.getTitleCache());
        book1.setSeriesPart("1");
        Assert.assertEquals("Unexpected abbrev title cache", "Acta Inst. Bot. Acad. Sci. URSS, ser. 1, Fasc. 11. 1955", book1.getTitleCache());
    }

// ***************************** Book Section ************************/

    //9529 and others
    @Test
    public void testBookSectionGetTitleCache(){
        String bookSetionTitle = "My chapter";
        book1.setTitle("My book");
        book1.setAuthorship(bookTeam1);
        bookSection1.setTitle(bookSetionTitle);
        bookSection1.setInBook(book1);
        bookSection1.setAuthorship(sectionTeam1);
        book1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1975));
        Assert.assertEquals("Unexpected title cache.", "Section Author: My chapter. "+UTF8.EN_DASH+" In: Book Author, My book. 1975", bookSection1.getTitleCache());

        book1.setDatePublished((VerbatimTimePeriod)null);
        bookSection1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1976));
        bookSection1.setTitleCache(null, false);
        book1.setTitleCache(null, false);
        Assert.assertEquals("Unexpected title cache.", "Section Author: My chapter. "+UTF8.EN_DASH+" In: Book Author, My book. 1976", bookSection1.getTitleCache());
        //with in-ref year (ignore if there is ref year)
        book1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1977));
        bookSection1.setTitleCache(null, false);
        book1.setTitleCache(null, false);
        Assert.assertEquals("Unexpected title cache.", "Section Author: My chapter. "+UTF8.EN_DASH+" In: Book Author, My book. 1976", bookSection1.getTitleCache());
        //with series part
        bookSection1.setTitleCache(null, false);
        book1.setTitleCache(null, false);
        book1.setSeriesPart("2");
        Assert.assertEquals("Unexpected title cache.", "Section Author: My chapter. "+UTF8.EN_DASH+" In: Book Author, My book, ser. 2. 1976", bookSection1.getTitleCache());
        //without section title
        bookSection1.setTitle(null);
        bookSection1.setTitleCache(null, false);
        book1.setTitleCache(null, false);
        Assert.assertEquals("Unexpected title cache.", "Section Author "+UTF8.EN_DASH+" In: Book Author, My book, ser. 2. 1976", bookSection1.getTitleCache());
        bookSection1.setTitle(bookSetionTitle);

        //#6496, 9529, 9530
        bookSection1.setPages("33-38");
        bookSection1.setTitleCache(null, false);
        Assert.assertEquals("Unexpected title cache.", "Section Author: My chapter, pp. 33-38. "+UTF8.EN_DASH+" In: Book Author, My book, ser. 2. 1976", bookSection1.getTitleCache());
        bookSection1.setPages("v");
        bookSection1.setTitleCache(null, false);
        Assert.assertEquals("Unexpected title cache.", "Section Author: My chapter, p. v. "+UTF8.EN_DASH+" In: Book Author, My book, ser. 2. 1976", bookSection1.getTitleCache());
        bookSection1.setPages(null);

        bookSection1.setInBook(null);
        bookSection1.setTitleCache(null, false);
        Assert.assertEquals("Unexpected title cache.", "Section Author: My chapter. "+UTF8.EN_DASH+" In: - undefined book -. 1976", bookSection1.getTitleCache());
    }

    @Test
    //This test is just to show that setInBook(null) now resets caches  #1815
    public void testBookSectionSetInBook(){
        book1.setTitle("My book");
        book1.setAuthorship(bookTeam1);
        bookSection1.setTitle("My chapter");
        bookSection1.setInBook(book1);
        bookSection1.setAuthorship(sectionTeam1);
        book1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1975));
        Assert.assertEquals("Unexpected title cache.", "Section Author: My chapter. "+UTF8.EN_DASH+" In: Book Author, My book. 1975", bookSection1.getTitleCache());
        book1.setDatePublished((VerbatimTimePeriod)null);
        bookSection1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1976));
        Assert.assertEquals("Unexpected title cache.", "Section Author: My chapter. "+UTF8.EN_DASH+" In: Book Author, My book. 1976", bookSection1.getTitleCache());
        book1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1977));
        Assert.assertEquals("Unexpected title cache.", "Section Author: My chapter. "+UTF8.EN_DASH+" In: Book Author, My book. 1976", bookSection1.getTitleCache());

        bookSection1.setInBook(null);
        Assert.assertEquals("Unexpected title cache.", "Section Author: My chapter. "+UTF8.EN_DASH+" In: - undefined book -. 1976", bookSection1.getTitleCache());
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
        book.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(2000));

        Team sectionTeam = Team.NewTitledInstance("Chaudhary S. A.", "Chaudhary S. A.");
        IBookSection bookSection = ReferenceFactory.newBookSection();
        bookSection.setTitle("73. Hedypnois - 87. Crepis");
        bookSection.setInBook(book);
        bookSection.setAuthorship(sectionTeam);
        bookSection.setPages("222-251");
        Assert.assertEquals("Chaudhary S. A.: 73. Hedypnois - 87. Crepis, pp. 222-251. "+UTF8.EN_DASH+" In: Chaudhary S. A.(ed.), Flora of the Kingdom of Saudi Arabia 2(3). 2000", bookSection.getTitleCache());

    }

    @Test
    public void testCdDvdGetTitleWithoutYearAndAuthor() {
        String result = TitleWithoutYearAndAuthorHelper.getTitleWithoutYearAndAuthor(cdDvd, false, false);
        assertEquals(cdDvdTitle, result);
    }

    //TODO missing publicationPlace and publisher has to be discussed
    @Test
    public void testCdDvdGetTitleCache() {
        String result = defaultStrategy.getTitleCache(cdDvd);
        assertEquals(cdDvdTitle + ". 1999"+SEP+"2001", result);
    }

// *************************** GENERIC *****************************************/

    @Test
    public void testGenericGetTitleCache(){
        generic1.setTitle("auct.");
        Assert.assertEquals("Unexpected title cache.", "auct.", generic1.getTitleCache());
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
        Assert.assertEquals("Unexpected title cache.", "Part Title. "+UTF8.EN_DASH+" In: Authorteam, My book title", generic1.getTitleCache());
    }

    @Test
    public void testGenericGetAbbrevTitleCache(){
        generic1.setTitle("Part Title");
        generic1.setAbbrevTitle("Pt. Tit.");
        generic1.setDatePublished(TimePeriodParser.parseStringVerbatim("1987"));
        IBook book1 = ReferenceFactory.newBook();
        book1.setTitle("My book title");
        book1.setAbbrevTitle("My bk. tit.");
        book1.setAuthorship(genericTeam1);  //TODO handling not yet defined
        Reference inRef = (Reference)book1;
        generic1.setInReference(inRef);
        generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
        generic1.setAbbrevTitleCache(null, false);  //reset cache in case aspectJ is not enabled
        Assert.assertEquals("Unexpected abbrev title cache.", "Pt. Tit. "+UTF8.EN_DASH+" In: AT., My bk. tit. 1987", generic1.getAbbrevTitleCache());
        Assert.assertEquals("Title cache must still be the same", "Part Title. "+UTF8.EN_DASH+" In: Authorteam, My book title. 1987", generic1.getTitleCache());

        //protected
        generic1.setAbbrevTitleCache("My prot. abb. tit. in a bk.", true);
        Assert.assertEquals("Unexpected abbrev title cache.", "My prot. abb. tit. in a bk.", generic1.getAbbrevTitleCache());
        Assert.assertEquals("Unexpected title cache.", "Part Title. "+UTF8.EN_DASH+" In: Authorteam, My book title. 1987", generic1.getTitleCache());
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
    }

    @Test
    public void testGenericYearAndAuthorOnly(){
        generic1.setAuthorship(genericTeam1);
        generic1.setDatePublished(TimePeriodParser.parseStringVerbatim("1792"));
        generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
        Assert.assertEquals("Unexpected title cache.", "Authorteam, 1792", generic1.getTitleCache());
    }

    //#4338
    @Test
    public void testGenericMissingVolume(){
        generic1.setTitle("My generic");
        generic1.setAuthorship(genericTeam1);
        generic1.setDatePublished(TimePeriodParser.parseStringVerbatim("1883-1884"));
        generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
        generic1.setVolume("7");
        Assert.assertEquals("Authorteam, My generic 7. 1883"+SEP+"1884", generic1.getTitleCache());
        Assert.assertEquals("AT., My generic 7. 1883"+SEP+"1884", generic1.getAbbrevTitleCache());

        //inRef
        Reference generic2 = ReferenceFactory.newGeneric();
        generic2.setTitle("My InRef");
        Person person2 = Person.NewTitledInstance("InRefAuthor");
        generic2.setAuthorship(person2);
        generic2.setDatePublished(TimePeriodParser.parseStringVerbatim("1885"));
        generic1.setInReference(generic2);

        //only reference has a volume
//        Assert.assertEquals("in InRefAuthor, My InRef 7: 55. 1883"+SEP+"1884", generic1.getNomenclaturalCitation(detail1));
//        Assert.assertEquals("Authorteam - My generic in InRefAuthor, My InRef 7. 1883-1884", generic1.getTitleCache());
//        Assert.assertEquals("AT. - My generic in InRefAuthor, My InRef 7. 1883-1884", generic1.getAbbrevTitleCache());

        //both have a volume
        generic2.setVolume("9");  //still unclear what should happen if you have such dirty data
//        Assert.assertEquals("Authorteam - My generic in InRefAuthor, My InRef 7. 1883-1884", generic1.getTitleCache());
//        Assert.assertEquals("AT. - My generic in InRefAuthor, My InRef 7. 1883-1884", generic1.getAbbrevTitleCache());

        //only inref has volume
        generic1.setVolume(null);
        Assert.assertEquals("Authorteam: My generic. "+UTF8.EN_DASH+" In: InRefAuthor, My InRef 9. 1883"+SEP+"1884", generic1.getTitleCache());
        Assert.assertEquals("AT.: My generic. "+UTF8.EN_DASH+" In: InRefAuthor, My InRef 9. 1883"+SEP+"1884", generic1.getAbbrevTitleCache());
   }

// ********************************** WEB PAGE ********************************************/

    @Test
    //still preliminary, may be modified in future
    public void testWebPageGetTitleCache(){
        webPage1.setUri(URI.create("http://flora.huji.ac.il"));
        Assert.assertEquals("Unexpected title cache.",
                "http://flora.huji.ac.il",
                webPage1.getTitleCache());

        webPage1.setTitle("Flora of Israel Online");
        webPage1.setAuthorship(webPageTeam1);
        webPage1.setAccessed(DateTime.parse("2001-01-05"));
        Assert.assertEquals("Unexpected title cache.",
                "Authorteam, D., Flora of Israel Online "+UTF8.EN_DASH+" http://flora.huji.ac.il [accessed 2001-01-05]",
                webPage1.getTitleCache());
    }

//  @Test
//  //WebPages should usually not be used as nomencl.reference, therefore this is less important
//  public void testWebPageGetAbbrevTitleCache(){
//      webPage1.setTitle("auct.");
//      Assert.assertEquals("Unexpected title cache.", "auct.", webPage1.getTitleCache());
//  }

}