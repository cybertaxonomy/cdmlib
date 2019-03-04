/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.match;

import java.net.URI;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.term.DefaultTermInitializer;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @since 03.08.2009
 */
public class MatchStrategyFactoryTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MatchStrategyFactoryTest.class);

	private static boolean FAIL_ALL = true;

	private IMatchStrategyEqual matchStrategy;
//	private IBook book1;
//	private String editionString1 ="Ed.1";
//	private String volumeString1 ="Vol.1";
//	private Team team1;
//	private IPrintSeries printSeries1;
//	private Annotation annotation1;
//	private String title1 = "Title1";
//	private VerbatimTimePeriod datePublished1 = VerbatimTimePeriod.NewVerbatimInstance(2000);
//	private int hasProblem1 = 1;
//	private LSID lsid1;
//
//	private IBook book2;
//	private String editionString2 ="Ed.2";
//	private String volumeString2 ="Vol.2";
//	private Team team2;
//	private IPrintSeries printSeries2;
//	private Annotation annotation2;
//	private String annotationString2;
//	private String title2 = "Title2";
//	private DateTime created2 = new DateTime(1999, 3, 1, 0, 0, 0, 0);
//	private VerbatimTimePeriod datePublished2 = VerbatimTimePeriod.NewVerbatimInstance(2002);
//	private int hasProblem2 = 1;
//	private LSID lsid2;
//
//	private IBook book3;

	private Institution institution1;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DefaultTermInitializer termInitializer = new DefaultTermInitializer();
		termInitializer.initialize();
	}


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
//		team1 = Team.NewInstance();
//		team1.setTitleCache("Team1",true);
//		team2 = Team.NewInstance();
//		team2.setTitleCache("Team2",true);
//		printSeries1 = ReferenceFactory.newPrintSeries("Series1");
//		printSeries1.setTitle("print series");
//		printSeries2 = ReferenceFactory.newPrintSeries("Series2");
//		annotation1 = Annotation.NewInstance("Annotation1", null);
//		annotationString2 = "Annotation2";
//		annotation2 = Annotation.NewInstance(annotationString2, null);
//
//		book1 = ReferenceFactory.newBook();
//		book1.setAuthorship(team1);
//		book1.setTitle(title1);
//		book1.setEdition(editionString1);
//		book1.setVolume(volumeString1);
//		book1.setInSeries(printSeries1);
//		((AnnotatableEntity) book1).addAnnotation(annotation1);
//		book1.setDatePublished(datePublished1);
//		book1.setParsingProblem(hasProblem1);
//		lsid1 = new LSID("authority1", "namespace1", "object1", "revision1");
//		book1.setLsid(lsid1);
//		((Reference) book1).setNomenclaturallyRelevant(false);
//
//		book2 = ReferenceFactory.newBook();
//		book2.setAuthorship(team2);
//		book2.setTitle(title2);
//		book2.setEdition(editionString2);
//		book2.setVolume(volumeString2);
//		book2.setInSeries(printSeries2);
//		( (AnnotatableEntity) book2).addAnnotation(annotation2);
//		book2.setCreated(created2);
//		book2.setDatePublished(datePublished2);
//		book2.setParsingProblem(hasProblem2);
//		lsid2 = new LSID("authority2", "namespace2", "object2", "revision2");
//		book2.setLsid(lsid2);
//		((Reference) book2).setNomenclaturallyRelevant(true);
//
		institution1 = Institution.NewNamedInstance("Institution1");

	}


//********************* TEST *********************************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.match.DefaultMatchStrategy#NewInstance(java.lang.Class)}.
	 */
	@Test
	public void testNewInstance() {
		matchStrategy = MatchStrategyFactory.NewDefaultInstance(Reference.class);
		Assert.assertNotNull(matchStrategy);
//		Assert.assertEquals(Reference.class, matchStrategy.getMatchClass());
	}

	   /**
     * Test method for {@link eu.etaxonomy.cdm.strategy.match.DefaultMatchStrategy#invoke(IMatchable, IMatchable), eu.etaxonomy.cdm.strategy.match.IMatchable)}.
     * @throws MatchException
     */
    @Test
    public void testParsedPerson() throws MatchException {
        IParsedMatchStrategy matchStrategy = MatchStrategyFactory.NewParsedPersonInstance();
        Assert.assertNotNull(matchStrategy);
        Person fullPerson;
        Person parsedPerson;
        MatchResult result;

        fullPerson = getDefaultFullPerson();

        //should match
        parsedPerson = getDefaultParsedPerson();
        result = matchStrategy.invoke(fullPerson, parsedPerson);
        System.out.println(result);
        Assert.assertTrue("Same nom.title. should match", result.isSuccessful());

        //differing nom. title.
        parsedPerson.setNomenclaturalTitle("Wrong");
        result = matchStrategy.invoke(fullPerson, parsedPerson, FAIL_ALL);
        System.out.println(result);
        Assert.assertFalse("Differing nom.title. should not match", matchStrategy.invoke(fullPerson, parsedPerson).isSuccessful());

        //differing family
        parsedPerson = getDefaultParsedPerson();
        parsedPerson.setFamilyName("Wrong");
        Assert.assertFalse("Differing family name should not match", matchStrategy.invoke(fullPerson, parsedPerson).isSuccessful());
        fullPerson.setFamilyName(null);
        Assert.assertFalse("Only parsed with family name should not match. Wrong direction.", matchStrategy.invoke(fullPerson, parsedPerson).isSuccessful());

        //
        parsedPerson = getDefaultParsedPerson();
        parsedPerson.setInitials("D.");
        Assert.assertFalse("Differing nom. title should not match", matchStrategy.invoke(fullPerson, parsedPerson).isSuccessful());

        //nom. title. (2)
        fullPerson = getDefaultFullPerson();
        parsedPerson = getDefaultParsedPerson();
        fullPerson.setNomenclaturalTitle("Wro.");
        Assert.assertFalse("Differing nom. title should not match", matchStrategy.invoke(fullPerson, parsedPerson).isSuccessful());

        //fullPerson protected
        fullPerson = getDefaultFullPerson();
        parsedPerson = getDefaultParsedPerson();
        fullPerson.setTitleCache(fullPerson.getTitleCache(), true);
        Assert.assertFalse("Differing protected title should not match", matchStrategy.invoke(fullPerson, parsedPerson).isSuccessful());

        //parsedPerson protected
        fullPerson = getDefaultFullPerson();
        parsedPerson = getDefaultParsedPerson();
        parsedPerson.setTitleCache(parsedPerson.getTitleCache(), true);
//        System.out.println(fullPerson.getTitleCache());
//        System.out.println(parsedPerson.getTitleCache());
        Assert.assertFalse("Differing nom. title should not match", matchStrategy.invoke(fullPerson, parsedPerson).isSuccessful());


    }


    /**
     * @return
     */
    protected Person getDefaultFullPerson() {
        Person fullPerson = Person.NewInstance();
        fullPerson.setInitials("F.G.");
        fullPerson.setFamilyName("Name");
        fullPerson.setNomenclaturalTitle("Nam.");
        fullPerson.setGivenName("Full Given");
        fullPerson.setPrefix("Dr.");
        fullPerson.setSuffix("jr.");
//        fullPerson.setCollectorTitle();
        fullPerson.setLifespan(TimePeriodParser.parseString("1972-2015"));
        fullPerson.addInstitutionalMembership(institution1, TimePeriodParser.parseString("2002-2004"), "Dept. X", "Developer");
        return fullPerson;
    }





    /**
     * @return
     */
    protected Person getDefaultParsedPerson() {
        Person parsedPerson = Person.NewInstance();
        parsedPerson.setNomenclaturalTitle("Nam.");
        return parsedPerson;
    }

    /**
      * Test method for {@link eu.etaxonomy.cdm.strategy.match.DefaultMatchStrategy#invoke(IMatchable, IMatchable), eu.etaxonomy.cdm.strategy.match.IMatchable)}.
      * @throws MatchException
      */
     @Test
     public void testParsedTeam() throws MatchException {
         IParsedMatchStrategy matchStrategy = MatchStrategyFactory.NewParsedTeamInstance();
         Assert.assertNotNull(matchStrategy);
         Team fullTeam;
         Team parsedTeam;
         MatchResult matchResult;

         fullTeam = getDefaultFullTeam();
         parsedTeam = getDefaultParsedTeam();

         //should match
         Assert.assertTrue("Same nom.title. should match", matchStrategy.invoke(fullTeam, parsedTeam).isSuccessful());

         //differing nom. title.
         parsedTeam.setNomenclaturalTitle("Wrong");
         Assert.assertFalse("Differing nom.title. should not match", matchStrategy.invoke(fullTeam, parsedTeam).isSuccessful());

         //differing family
         parsedTeam = getDefaultParsedTeam();
         parsedTeam.getTeamMembers().get(0).setFamilyName("Wrong");
         matchResult = matchStrategy.invoke(fullTeam, parsedTeam, true);
         Assert.assertFalse("Differing family name should not match", matchResult.isSuccessful());
         parsedTeam.getTeamMembers().get(0).setNomenclaturalTitle("Wrong.");
         matchResult = matchStrategy.invoke(fullTeam, parsedTeam, true);
         System.out.println(matchResult);
         Assert.assertFalse("Differing family name should not match", matchResult.isSuccessful());


         //
         parsedTeam = getDefaultParsedTeam();
         parsedTeam.getTeamMembers().get(0).setInitials("D.");
         Assert.assertFalse("Differing nom. title should not match", matchStrategy.invoke(fullTeam, parsedTeam).isSuccessful());

         //nom. title. (2)
         fullTeam = getDefaultFullTeam();
         parsedTeam = getDefaultParsedTeam();
         fullTeam.setNomenclaturalTitle("Wro.");
         Assert.assertFalse("Differing nom. title should not match", matchStrategy.invoke(fullTeam, parsedTeam).isSuccessful());

         //fullPerson protected
         fullTeam = getDefaultFullTeam();
         parsedTeam = getDefaultParsedTeam();
         fullTeam.setTitleCache(fullTeam.getTitleCache(), true);
         Assert.assertFalse("Differing protected title should not match", matchStrategy.invoke(fullTeam, parsedTeam).isSuccessful());

         //parsedPerson protected
         fullTeam = getDefaultFullTeam();
         parsedTeam = getDefaultParsedTeam();
         parsedTeam.setTitleCache(parsedTeam.getTitleCache(), true);
         Assert.assertFalse("Differing nom. title should not match", matchStrategy.invoke(fullTeam, parsedTeam).isSuccessful());

         fullTeam = getDefaultFullTeam();
         parsedTeam = getDefaultParsedTeam();
         fullTeam.setHasMoreMembers(true);
         Assert.assertFalse("Differing nom. title should not match", matchStrategy.invoke(fullTeam, parsedTeam).isSuccessful());


     }


    /**
     * @return
     */
    private Team getDefaultFullTeam() {
        Team team = Team.NewInstance();
        team.addTeamMember(getDefaultFullPerson());
        team.addTeamMember(getFullPerson2());
        team.getNomenclaturalTitle();
        return team;
    }


    protected Person getFullPerson2() {
        Person fullPerson = Person.NewInstance();
        fullPerson.setInitials("A.B.");
        fullPerson.setFamilyName("Nice");
        fullPerson.setNomenclaturalTitle("Nice");
        fullPerson.setGivenName("John");
        return fullPerson;
    }

    private Team getDefaultParsedTeam() {
        Team team = Team.NewInstance();
        team.addTeamMember(getDefaultParsedPerson());
        team.addTeamMember(getFullPerson2());
        //TODO should be done in cache strategy
        team.getNomenclaturalTitle();
        return team;
    }

    protected Person getParsedPerson2() {
        Person parsedPerson = Person.NewInstance();
        parsedPerson.setNomenclaturalTitle("Nice");
        return parsedPerson;
    }

    @Test
    public void testParsedBook() throws MatchException {
        IParsedMatchStrategy matchStrategy = MatchStrategyFactory.NewParsedBookInstance();
        Assert.assertNotNull(matchStrategy);
        IBook fullBook;
        IBook parsedBook;

        fullBook = getDefaultFullBook();
        parsedBook = getDefaultParsedBook();

        //should match
        parsedBook = getDefaultParsedBook();
        Assert.assertTrue("Same abbrev. title should match", matchStrategy.invoke(fullBook, parsedBook).isSuccessful());

        //differing nom. title.
        parsedBook.setAbbrevTitle("Wrong");
        Assert.assertFalse("Differing abbrev. title. should not match", matchStrategy.invoke(fullBook, parsedBook).isSuccessful());

        //differing family
        parsedBook = getDefaultParsedBook();
        parsedBook.setTitle("Wrong title");
        Assert.assertFalse("Differing title should not match", matchStrategy.invoke(fullBook, parsedBook).isSuccessful());
        fullBook.setTitle(null);
        Assert.assertFalse("Title only for parsed book should not match. Wrong direction.", matchStrategy.invoke(fullBook, parsedBook).isSuccessful());

        //change author
        fullBook = getDefaultFullBook();
        parsedBook = getDefaultParsedBook();
        ((Team)fullBook.getAuthorship()).getTeamMembers().get(0).setNomenclaturalTitle("Wrong");
        Assert.assertFalse("Differing author in nomencl. title should not match", matchStrategy.invoke(fullBook, parsedBook).isSuccessful());

        //change author
        fullBook = getDefaultFullBook();
        parsedBook = getDefaultParsedBook();
        ((Team)fullBook.getAuthorship()).getTeamMembers().get(0).setFamilyName("Changed");
        Assert.assertTrue("Full book family name author changed should still match", matchStrategy.invoke(fullBook, parsedBook).isSuccessful());


    }


    @Test
    public void testParsedBookSection() throws MatchException {
        IParsedMatchStrategy matchStrategy = MatchStrategyFactory.NewParsedBookSectionInstance();
        Assert.assertNotNull(matchStrategy);
        IBookSection fullBookSection;
        IBookSection parsedBookSection;

        fullBookSection = getMatchingFullBookSection();
        parsedBookSection = getDefaultParsedBookSection();
        Assert.assertTrue("Only author, book and date published should match", matchStrategy.invoke(fullBookSection, parsedBookSection).isSuccessful() );

        //should match
        fullBookSection = getDefaultFullBookSection();
        Assert.assertFalse("Abbrev. title must be equal or null", matchStrategy.invoke(fullBookSection, parsedBookSection).isSuccessful());
        parsedBookSection.setAbbrevTitle(fullBookSection.getAbbrevTitle());
        Assert.assertFalse("Still not match because pages are not equal (parsed is null)", matchStrategy.invoke(fullBookSection, parsedBookSection).isSuccessful());
        parsedBookSection.setPages(fullBookSection.getPages());
        Assert.assertFalse("Now they should match", matchStrategy.invoke(fullBookSection, parsedBookSection).isSuccessful());

        //differing nom. title.
        parsedBookSection.setAbbrevTitle("Wrong");
        Assert.assertFalse("Differing abbrev. title. should not match", matchStrategy.invoke(fullBookSection, parsedBookSection).isSuccessful());

        //differing family
        parsedBookSection = getDefaultParsedBookSection();
        parsedBookSection.setTitle("Wrong title");
        Assert.assertFalse("Differing title should not match", matchStrategy.invoke(fullBookSection, parsedBookSection).isSuccessful());
        fullBookSection.setTitle(null);
        Assert.assertFalse("Title only for parsed book should not match. Wrong direction.", matchStrategy.invoke(fullBookSection, parsedBookSection).isSuccessful());

        //change author
        fullBookSection = getMatchingFullBookSection();
        parsedBookSection = getDefaultParsedBookSection();
        fullBookSection.getAuthorship().setNomenclaturalTitle("Wrong");
        Assert.assertFalse("Differing author in nomencl. title should not match", matchStrategy.invoke(fullBookSection, parsedBookSection).isSuccessful());

        //change author
        fullBookSection = getMatchingFullBookSection();
        parsedBookSection = getDefaultParsedBookSection();
        ((Person)fullBookSection.getAuthorship()).setFamilyName("Changed");
        Assert.assertTrue("Full book family name author changed should still match", matchStrategy.invoke(fullBookSection, parsedBookSection).isSuccessful());
    }


    /**
     * @return
     */
    private IBook getDefaultFullBook() {
        IBook book = getDefaultParsedBook();
        book.setAuthorship(getDefaultFullTeam());
        book.setTitle("Flora Hellenica");
        book.setUri(URI.create("https://www.flora.hellenica.gr"));
        book.setIsbn("1234-222-222-333");  //format not yet correct
        book.setPublisher("Greek publisher", "Athens");
        book.getTitleCache();
        return book;
    }


    /**
     * @return
     */
    private IBook getDefaultParsedBook() {
        IBook book = ReferenceFactory.newBook();
        book.setAbbrevTitle("Fl. Hell.");
        book.setVolume("2");
        book.setEdition("ed. 3");
        book.setEditor("editor");
        book.setAuthorship(getDefaultParsedTeam());
        book.setDatePublished(TimePeriodParser.parseStringVerbatim("1982-10-06"));
        book.getAbbrevTitleCache();
        return book;
    }


    /**
     * @return
     */
    private IBookSection getDefaultFullBookSection() {
        IBookSection bookSection = getMatchingFullBookSection();
        bookSection.setTitle("Book section title");
        bookSection.setAbbrevTitle("Bk. sct. tit.");
        bookSection.setPages("22-33");
        return bookSection;
    }

    private IBookSection getMatchingFullBookSection() {
        IBookSection bookSection = ReferenceFactory.newBookSection();
        bookSection.setAuthorship(getDefaultFullPerson());
        bookSection.setInBook(getDefaultFullBook());
        bookSection.setDatePublished(TimePeriodParser.parseStringVerbatim("1892"));
        return bookSection;
    }

    /**
     * @return
     */
    private IBookSection getDefaultParsedBookSection() {
        IBookSection bookSection = ReferenceFactory.newBookSection();
        bookSection.setAuthorship(getDefaultParsedPerson());
        bookSection.setInBook(getDefaultParsedBook());
        bookSection.setDatePublished(TimePeriodParser.parseStringVerbatim("1892"));
        return bookSection;
    }


    @Test
    public void testParsedArticle() throws MatchException {
        IParsedMatchStrategy matchStrategy = MatchStrategyFactory.NewParsedArticleInstance();
        Assert.assertNotNull(matchStrategy);
        IArticle fullArticle;
        IArticle parsedArticle;

        fullArticle = getMatchingFullArticle();
        parsedArticle = getDefaultParsedArticle();
        Assert.assertTrue("Only author, book and date published should match", matchStrategy.invoke(fullArticle, parsedArticle).isSuccessful() );

        //should match
        fullArticle = getDefaultFullArticle();
        Assert.assertFalse("Abbrev. title must be equal or null", matchStrategy.invoke(fullArticle, parsedArticle).isSuccessful());
        parsedArticle.setAbbrevTitle(fullArticle.getAbbrevTitle());
        Assert.assertFalse("Still not match because pages are not equal (parsed is null)", matchStrategy.invoke(fullArticle, parsedArticle).isSuccessful());
        parsedArticle.setPages(fullArticle.getPages());
        Assert.assertFalse("Now they should match", matchStrategy.invoke(fullArticle, parsedArticle).isSuccessful());

        //differing nom. title.
        parsedArticle.setAbbrevTitle("Wrong");
        Assert.assertFalse("Differing abbrev. title. should not match", matchStrategy.invoke(fullArticle, parsedArticle).isSuccessful());

        //differing family
        parsedArticle = getDefaultParsedArticle();
        parsedArticle.setTitle("Wrong title");
        Assert.assertFalse("Differing title should not match", matchStrategy.invoke(fullArticle, parsedArticle).isSuccessful());
        fullArticle.setTitle(null);
        Assert.assertFalse("Title only for parsed book should not match. Wrong direction.", matchStrategy.invoke(fullArticle, parsedArticle).isSuccessful());

        //change author
        fullArticle = getMatchingFullArticle();
        parsedArticle = getDefaultParsedArticle();
        fullArticle.getAuthorship().setNomenclaturalTitle("Wrong");
        Assert.assertFalse("Differing author in nomencl. title should not match", matchStrategy.invoke(fullArticle, parsedArticle).isSuccessful());

        //change author
        fullArticle = getMatchingFullArticle();
        parsedArticle = getDefaultParsedArticle();
        ((Team)fullArticle.getAuthorship()).getTeamMembers().get(0).setFamilyName("Changed");
        Assert.assertTrue("Full book family name author changed should still match", matchStrategy.invoke(fullArticle, parsedArticle).isSuccessful());
    }

    private IArticle getDefaultFullArticle() {
        IArticle article = getMatchingFullArticle();
        article.setTitle("Article title");
        article.setAbbrevTitle("Art. tit.");
        article.setPages("22-33");
        return article;
    }

    private IArticle getMatchingFullArticle() {
        IArticle article = ReferenceFactory.newArticle();
        article.setAuthorship(getDefaultFullTeam());
        article.setInJournal(getDefaultFullJournal());
        article.setDatePublished(TimePeriodParser.parseStringVerbatim("1950"));
        return article;
    }

    /**
     * @return
     */
    private IArticle getDefaultParsedArticle() {
        IArticle article = ReferenceFactory.newArticle();
        article.setAuthorship(getDefaultParsedTeam());
        article.setInJournal(getDefaultParsedJournal());
        article.setDatePublished(TimePeriodParser.parseStringVerbatim("1950"));
        return article;
    }

    private IJournal getDefaultFullJournal() {
        IJournal journal = getDefaultParsedJournal();
//        journal.setAuthorship(getDefaultFullTeam()); //journals should not have authors
        journal.setTitle("J. Flow. Pl.");
        journal.setUri(URI.create("https://www.journal-flowering-plants.gr"));
        journal.setIssn("1234-222-222-333");  //format not yet correct
        journal.setPublisher("Botanical publisher", "Paris");
        journal.getTitleCache();
        return journal;
    }


    /**
     * @return
     */
    private IJournal getDefaultParsedJournal() {
        IJournal journal = ReferenceFactory.newJournal();
        journal.setAbbrevTitle("Fl. Hell.");
//        journal.setAuthorship(getDefaultParsedTeam());  //journals should not have authors
//        ((Reference)journal).setDatePublished(TimePeriodParser.parseStringVerbatim("1992-04-03")); //journals should not have date published
        journal.getTitleCache();  //not sure if enough
        return journal;
    }

    @Test
    public void testParsedReference() throws MatchException {
        IParsedMatchStrategy matchStrategy = MatchStrategyFactory.NewParsedReferenceInstance();
        Assert.assertNotNull(matchStrategy);
        IArticle fullArticle;
        IArticle parsedArticle;

        fullArticle = getMatchingFullArticle();
        parsedArticle = getDefaultParsedArticle();
        Assert.assertTrue("Only author, book and date published should match", matchStrategy.invoke(fullArticle, parsedArticle).isSuccessful() );

        //should match
        fullArticle = getDefaultFullArticle();
        Assert.assertFalse("Abbrev. title must be equal or null", matchStrategy.invoke(fullArticle, parsedArticle).isSuccessful());
        parsedArticle.setAbbrevTitle(fullArticle.getAbbrevTitle());
        Assert.assertFalse("Still not match because pages are not equal (parsed is null)", matchStrategy.invoke(fullArticle, parsedArticle).isSuccessful());
        parsedArticle.setPages(fullArticle.getPages());
        Assert.assertFalse("Now they should match", matchStrategy.invoke(fullArticle, parsedArticle).isSuccessful());

        //differing nom. title.
        parsedArticle.setAbbrevTitle("Wrong");
        Assert.assertFalse("Differing abbrev. title. should not match", matchStrategy.invoke(fullArticle, parsedArticle).isSuccessful());

        //differing family
        parsedArticle = getDefaultParsedArticle();
        parsedArticle.setTitle("Wrong title");
        Assert.assertFalse("Differing title should not match", matchStrategy.invoke(fullArticle, parsedArticle).isSuccessful());
        fullArticle.setTitle(null);
        Assert.assertFalse("Title only for parsed book should not match. Wrong direction.", matchStrategy.invoke(fullArticle, parsedArticle).isSuccessful());

        //change author
        fullArticle = getMatchingFullArticle();
        parsedArticle = getDefaultParsedArticle();
        fullArticle.getAuthorship().setNomenclaturalTitle("Wrong");
        Assert.assertFalse("Differing author in nomencl. title should not match", matchStrategy.invoke(fullArticle, parsedArticle).isSuccessful());

        //change author
        fullArticle = getMatchingFullArticle();
        parsedArticle = getDefaultParsedArticle();
        ((Team)fullArticle.getAuthorship()).getTeamMembers().get(0).setFamilyName("Changed");
        Assert.assertTrue("Full book family name author changed should still match", matchStrategy.invoke(fullArticle, parsedArticle).isSuccessful());
    }


}
