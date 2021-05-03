/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.reference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.ICdDvd;
import eu.etaxonomy.cdm.model.reference.IDatabase;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.IWebPage;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.cache.agent.TeamDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @since 03.05.2021
 */
public class NomenclaturalSourceFormatterTest {

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

    //Generic
    private static Reference generic1;
    private static Team genericTeam1;

    //common
    private static NomenclaturalSourceFormatter formatter = NomenclaturalSourceFormatter.INSTANCE();
    private static final String detail1 = "55";

    @Before
    public void setUp() throws Exception {
        //article
        article1 = ReferenceFactory.newArticle();
        journal1 = ReferenceFactory.newJournal();
        articleTeam1 = Team.NewInstance();
        articleTeam2 = Team.NewInstance();
        articleTeam1.setTitleCache("Team1", true);
        articleTeam1.setNomenclaturalTitle("T.", true);
        articleTeam2.setTitleCache("Team2", true);
        articleTeam2.setNomenclaturalTitle("TT.", true);

        //book / section
        book1 = ReferenceFactory.newBook();
        bookTeam1 = Team.NewTitledInstance("Book Author", "TT.");
        bookSection1 = ReferenceFactory.newBookSection();
        sectionTeam1 = Team.NewTitledInstance("Section Author", "T.");

        //Generic
        generic1 = ReferenceFactory.newGeneric();
        genericTeam1 = Team.NewTitledInstance("Authorteam", "AT.");
    }


    @Test
    public void testArticleGetNomenclaturalCitation(){
        journal1.setTitle("My journal");
        journal1.setTitle("M. J.");
        ((Reference)journal1).setAuthorship(articleTeam2);
        article1.setTitle("My article");
        article1.setInJournal(journal1);
        article1.setAuthorship(articleTeam1);
        article1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1975));
        Assert.assertEquals("in M. J.: 55. 1975", formatter.format((Reference)article1, detail1));

        article1.setVolume("22");
        Assert.assertEquals("in M. J. 22: 55. 1975", formatter.format((Reference)article1, detail1));
        article1.setSeriesPart("ser. 11");
        Assert.assertEquals("in M. J., ser. 11, 22: 55. 1975", formatter.format((Reference)article1, detail1));

        article1.setPages("33"); //#6496 don't show pages in nomencl. citation
        Assert.assertEquals("in M. J., ser. 11, 22: 55. 1975", formatter.format((Reference)article1, detail1));
    }

    /**
     * After ser. , sect. , abt. we want to have a comma, if there is not yet one following anyway
     */
    @Test
    public void testArticleGetNomenclaturalCitationSerSectAbt(){
        article1.setInJournal(journal1);
        article1.setVolume("22");
        journal1.setAbbrevTitle("J. Pl. Eur.");
        ((Reference)journal1).setAuthorship(articleTeam2);
        article1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1975));
        //no ser, sect, abt
        Assert.assertEquals("in J. Pl. Eur. 22: 55. 1975", formatter.format((Reference)article1, detail1));
        //ser
        journal1.setAbbrevTitle("J. Pl. Eur., ser. 3");
        Assert.assertEquals("in J. Pl. Eur., ser. 3, 22: 55. 1975", formatter.format((Reference)article1, detail1));
        journal1.setAbbrevTitle("J. Pl. Eur., Ser. 3");
        Assert.assertEquals("in J. Pl. Eur., Ser. 3, 22: 55. 1975", formatter.format((Reference)article1, detail1));
        journal1.setAbbrevTitle("J. Pl. Eur., ser. 3, s.n.");
        Assert.assertEquals("in J. Pl. Eur., ser. 3, s.n. 22: 55. 1975", formatter.format((Reference)article1, detail1));
        //sect
        journal1.setAbbrevTitle("J. Pl. Eur., sect. 3");
        Assert.assertEquals("in J. Pl. Eur., sect. 3, 22: 55. 1975", formatter.format((Reference)article1, detail1));
        journal1.setAbbrevTitle("J. Pl. Eur., Sect. 3");
        Assert.assertEquals("in J. Pl. Eur., Sect. 3, 22: 55. 1975", formatter.format((Reference)article1, detail1));
        journal1.setAbbrevTitle("J. Pl. Eur., Sect. 3, something");
        Assert.assertEquals("in J. Pl. Eur., Sect. 3, something 22: 55. 1975", formatter.format((Reference)article1, detail1));
        //abt
        journal1.setAbbrevTitle("J. Pl. Eur., abt. 3");
        Assert.assertEquals("in J. Pl. Eur., abt. 3, 22: 55. 1975", formatter.format((Reference)article1, detail1));
        journal1.setAbbrevTitle("J. Pl. Eur., Abt. 3");
        Assert.assertEquals("in J. Pl. Eur., Abt. 3, 22: 55. 1975", formatter.format((Reference)article1, detail1));
        journal1.setAbbrevTitle("J. Pl. Eur., abt. 3, no comma");
        Assert.assertEquals("in J. Pl. Eur., abt. 3, no comma 22: 55. 1975", formatter.format((Reference)article1, detail1));

        journal1.setAbbrevTitle("J. Pl. Eur., sect. 3");
        article1.setSeriesPart("1");
        Assert.assertEquals("in J. Pl. Eur., sect. 3, ser. 1, 22: 55. 1975", formatter.format((Reference)article1, detail1));
        article1.setSeriesPart("Series 2");
        Assert.assertEquals("in J. Pl. Eur., sect. 3, Series 2, 22: 55. 1975", formatter.format((Reference)article1, detail1));
    }

    @Test
    public void testBookGetNomenclaturalCitation(){
        book1.setTitle("My book");
        book1.setAuthorship(bookTeam1);
        book1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1975));
        Assert.assertEquals("My book: 55. 1975", formatter.format((Reference)book1, detail1));
        book1.setAbbrevTitle("Analect. Bot.");
        Assert.assertEquals("Analect. Bot. 1975", formatter.format((Reference)book1, null));
    }


    //https://dev.e-taxonomy.eu/redmine/issues/8881
    @Test
    public void testInRefAuthor(){
        Person person1 = Person.NewInstance("Inauth.", "Inauthor", "A.B.", "Ala Bala");
        Person person2 = Person.NewInstance("Twoauth.", "Twoauthor", "C.", "Cla");
        Person person3 = Person.NewTitledInstance("Threeauth.");
        Team team = Team.NewInstance(person1, person2, person3);
        IBook book1 = ReferenceFactory.newBook();
        book1.setAbbrevTitle("Acta Inst. Bot. Acad. Sci. URSS");
        book1.setVolume("Fasc. 11");
        book1.setDatePublished(TimePeriodParser.parseStringVerbatim("1955"));
        bookSection1.setTitle("My chapter");
        bookSection1.setInBook(book1);
        book1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1956));

        book1.setAuthorship(person1);
        Assert.assertEquals("Unexpected nomencl. reference", "in Inauthor, Acta Inst. Bot. Acad. Sci. URSS Fasc. 11: 248. 1956", formatter.format((Reference)bookSection1, "248"));
        book1.setAuthorship(team);
        Assert.assertEquals("Unexpected nomencl. reference", "in Inauthor, Twoauthor & Threeauth., Acta Inst. Bot. Acad. Sci. URSS Fasc. 11: 248. 1956", formatter.format((Reference)bookSection1, "248"));
        team.setHasMoreMembers(true);
        Assert.assertEquals("Unexpected nomencl. reference", "in Inauthor, Twoauthor, Threeauth. & al., Acta Inst. Bot. Acad. Sci. URSS Fasc. 11: 248. 1956", formatter.format((Reference)bookSection1, "248"));
        book1.setAuthorship(Team.NewInstance(person1));
        Assert.assertEquals("Unexpected nomencl. reference", "in Inauthor, Acta Inst. Bot. Acad. Sci. URSS Fasc. 11: 248. 1956", formatter.format((Reference)bookSection1, "248"));

        //for the following the behavior is not yet finally discussed, may change in future
        book1.setAuthorship(team);
        team.setTitleCache("Teamcache", true);
        Assert.assertEquals("Unexpected nomencl. reference", "in Teamcache, Acta Inst. Bot. Acad. Sci. URSS Fasc. 11: 248. 1956", formatter.format((Reference)bookSection1, "248"));
        team.setTitleCache("Teamc.", true);
        Assert.assertEquals("Unexpected nomencl. reference", "in Teamc., Acta Inst. Bot. Acad. Sci. URSS Fasc. 11: 248. 1956", formatter.format((Reference)bookSection1, "248"));
        book1.setAuthorship(Team.NewInstance());
        Assert.assertEquals("Unexpected nomencl. reference", "in "+TeamDefaultCacheStrategy.EMPTY_TEAM+", Acta Inst. Bot. Acad. Sci. URSS Fasc. 11: 248. 1956", formatter.format((Reference)bookSection1, "248"));
    }

    //#3532
    @Test
    public void testUnexpectedNomenclaturalReferences(){
        Reference reference;

        //database
        IDatabase database1 = ReferenceFactory.newDatabase();
        reference = (Reference)database1;

        database1.setTitle("My database");
        //maybe we should have a trailing dot here
        Assert.assertEquals("My database: 55", formatter.format(reference, detail1));
        database1.setDatePublished(TimePeriodParser.parseStringVerbatim("1998"));
        Assert.assertEquals("My database: 55. 1998", formatter.format(reference, detail1));

        database1.setTitleCache("Your database", true);
        Assert.assertEquals("My database: 55. 1998", formatter.format(reference, detail1));

        //unclear if it is wanted that the year is shown, though the abbrev cache is protected, probably not
        reference.setAbbrevTitleCache("You. Db.", true);
        Assert.assertEquals("You. Db.: 55. 1998", formatter.format(reference, detail1));


        //CD/DVD
        ICdDvd cdDvd = ReferenceFactory.newCdDvd();
        reference= (Reference)cdDvd;
        cdDvd.setTitle("My Cd/Dvd");
        //maybe we should have a trailing dot here
        Assert.assertEquals("My Cd/Dvd: 55", formatter.format(reference, detail1));
        cdDvd.setDatePublished(TimePeriodParser.parseStringVerbatim("1998"));
        Assert.assertEquals("My Cd/Dvd: 55. 1998", formatter.format(reference, detail1));

        cdDvd.setTitleCache("Your Cd/Dvd", true);
        Assert.assertEquals("My Cd/Dvd: 55. 1998", formatter.format(reference, detail1));

        //unclear if it is wanted that the year is shown, though the abbrev cache is protected, probably not
        reference.setAbbrevTitleCache("You. Cd.", true);
        Assert.assertEquals("You. Cd.: 55. 1998", formatter.format(reference, detail1));


        //WebSite
        IWebPage webPage = ReferenceFactory.newWebPage();
        reference= (Reference)webPage;
        webPage.setTitle("My WebPage");
        //maybe we should have a trailing dot here
        Assert.assertEquals("My WebPage: 55", formatter.format(reference, detail1));
        webPage.setDatePublished(TimePeriodParser.parseStringVerbatim("1998"));
        Assert.assertEquals("My WebPage: 55. 1998", formatter.format(reference, detail1));

        webPage.setTitleCache("Your WebPage", true);
        Assert.assertEquals("My WebPage: 55. 1998", formatter.format(reference, detail1));

        //unclear if it is wanted that the year is shown, though the abbrev cache is protected, probably not
        reference.setAbbrevTitleCache("You. WP.", true);
        Assert.assertEquals("You. WP.: 55. 1998", formatter.format(reference, detail1));

        //uri
        webPage = ReferenceFactory.newWebPage();
        reference= (Reference)webPage;
        webPage.setUri(URI.create("http://www.abc.de"));
        Assert.assertEquals("http://www.abc.de: 55", formatter.format(reference, detail1));

        //TBC
    }

    @Test
    public void testBookSectionGetNomenclaturalCitation(){
        book1.setTitle("My book");
        book1.setAuthorship(bookTeam1);
        bookSection1.setTitle("My chapter");
        bookSection1.setInBook(book1);
        bookSection1.setAuthorship(sectionTeam1);
        book1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1975));
        //TODO still unclear which is correct
//      Assert.assertEquals("in Book Author, My book: 55. 1975", bookSection1.getNomenclaturalCitation(detail1));
        Assert.assertEquals("in TT., My book: 55. 1975", formatter.format((Reference)bookSection1, detail1));

        book1.setSeriesPart("2");
        Assert.assertEquals("in TT., My book, ser. 2: 55. 1975", formatter.format((Reference)bookSection1, detail1));
        //#6496 don't show pages in nom.ref. citations
        bookSection1.setPages("35-39");
        Assert.assertEquals("in TT., My book, ser. 2: 55. 1975", formatter.format((Reference)bookSection1, detail1));
    }

    @Test
    public void testGenericYearAndAuthorOnly(){
        generic1.setAuthorship(genericTeam1);
        generic1.setDatePublished(TimePeriodParser.parseStringVerbatim("1792"));
        generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
        Assert.assertEquals("Unexpected title cache.", "Authorteam, 1792", generic1.getTitleCache());
        Assert.assertEquals("1792", formatter.format(generic1, null));
    }


    @Test
    public void testGenericAuthorOnly(){
        generic1.setAuthorship(genericTeam1);
        generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
        Assert.assertEquals("Unexpected title cache.", "Authorteam", generic1.getTitleCache());
        Assert.assertEquals("", formatter.format(generic1, null));
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
        Assert.assertEquals("Unexpected title cache.", "in AT., My book title: 2", formatter.format(generic1, "2"));
    }

    @Test
    public void testGenericDoubleDotBeforeYear(){
        generic1.setAuthorship(genericTeam1);
        String detail = "sine no.";
        generic1.setAbbrevTitle("My title");
        generic1.setDatePublished(TimePeriodParser.parseStringVerbatim("1883-1884"));
        generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
        Assert.assertEquals("My title: sine no. 1883"+SEP+"1884", formatter.format(generic1, detail));
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
        //TODO author still unclear
//      Assert.assertEquals("Unexpected nom. ref.", "in Authorteam, My bk. tit.: pp. 44. 1987", generic1.getNomenclaturalCitation("pp. 44"));
        Assert.assertEquals("Unexpected nom. ref.", "in AT., My bk. tit.: pp. 44. 1987", formatter.format(generic1, "pp. 44"));
        generic1.setVolume("23");
        Assert.assertEquals("Unexpected nom. ref.", "in AT., My bk. tit. 23: pp. 44. 1987", formatter.format(generic1, "pp. 44"));
        generic1.setSeriesPart("ser. 11");
        //TODO
//      Assert.assertEquals("Unexpected nom. ref.", "in AT., My bk. tit., ser. 11, 23: pp. 44. 1987", generic1.getNomenclaturalCitation("pp. 44"));

        //protected
        generic1.setAbbrevTitleCache("My prot. abb. tit. in a bk.", true);  //TODO still needed?

        generic1.setDatePublished((VerbatimTimePeriod)null);
        Assert.assertEquals("Unexpected nom. ref.", "My prot. abb. tit. in a bk.", formatter.format(generic1, null));
        Assert.assertEquals("Unexpected nom. ref.", "My prot. abb. tit. in a bk.", formatter.format(generic1, ""));
        Assert.assertEquals("Unexpected nom. ref.", "My prot. abb. tit. in a bk.: pp. 44", formatter.format(generic1, "pp. 44"));

        generic1.setDatePublished(TimePeriodParser.parseStringVerbatim("1893"));
        Assert.assertEquals("Unexpected nom. ref.", "My prot. abb. tit. in a bk.: pp. 44. 1893", formatter.format(generic1, "pp. 44"));
    }

    @Test
    public void testGenericGetInRefWithoutInRef(){
        generic1.setTitle("My generic title");
        generic1.setAuthorship(genericTeam1);
        generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
        Assert.assertEquals("Unexpected title cache.", "My generic title: 2", formatter.format(generic1, "2"));
    }

    @Test
    public void testBookGetTitleCache2(){
        //series
        IBook book1 = ReferenceFactory.newBook();
        book1.setAbbrevTitle("Acta Inst. Bot. Acad. Sci. URSS");
        book1.setVolume("Fasc. 11");
        book1.setDatePublished(TimePeriodParser.parseStringVerbatim("1955"));
        Assert.assertEquals("Unexpected nomencl. reference", "Acta Inst. Bot. Acad. Sci. URSS Fasc. 11: 248. 1955", formatter.format((Reference)book1, "248"));
        book1.setSeriesPart("1");
        Assert.assertEquals("Unexpected nomencl. reference", "Acta Inst. Bot. Acad. Sci. URSS, ser. 1, Fasc. 11: 248. 1955", formatter.format((Reference)book1, "248"));
    }

    //#4338
    @Test
    public void testGenericMissingVolume(){
        generic1.setTitle("My generic");
        generic1.setAuthorship(genericTeam1);
        generic1.setDatePublished(TimePeriodParser.parseStringVerbatim("1883-1884"));
        generic1.setTitleCache(null, false);  //reset cache in case aspectJ is not enabled
        Assert.assertEquals("My generic: 55. 1883"+SEP+"1884", formatter.format(generic1, detail1));
        generic1.setVolume("7");
        Assert.assertEquals("My generic 7: 55. 1883"+SEP+"1884", formatter.format(generic1, detail1));

        //inRef
        Reference generic2 = ReferenceFactory.newGeneric();
        generic2.setTitle("My InRef");
        Person person2 = Person.NewTitledInstance("InRefAuthor");
        generic2.setAuthorship(person2);
        generic2.setDatePublished(TimePeriodParser.parseStringVerbatim("1885"));
        generic1.setInReference(generic2);

        //only reference has a volume
        Assert.assertEquals("in InRefAuthor, My InRef 7: 55. 1883"+SEP+"1884", formatter.format(generic1, detail1));

        //both have a volume
        generic2.setVolume("9");  //still unclear what should happen if you have such dirty data
//        Assert.assertEquals("in InRefAuthor, My InRef 7: 55. 1883-1884", formatter.format(generic1, detail1));

        //only inref has volume
        generic1.setVolume(null);
        Assert.assertEquals("in InRefAuthor, My InRef 9: 55. 1883"+SEP+"1884", formatter.format(generic1, detail1));
   }
}