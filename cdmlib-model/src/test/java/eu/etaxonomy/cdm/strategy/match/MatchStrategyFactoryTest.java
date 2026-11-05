/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.match;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.DOI;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * Test class for {@link MatchStrategyFactory}
 *
 * @author a.mueller
 * @since 03.08.2009
 */
public class MatchStrategyFactoryTest extends TermTestBase {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private static boolean FAIL_ALL = true;

	private IMatchStrategyEqual matchStrategy;

	private Institution institution1;

	@BeforeEach
	public void setUp() throws Exception {
		institution1 = Institution.NewNamedInstance("Institution1");
	}

//********************* TEST *********************************************/

	@Test
	public void testNewInstance() {
		matchStrategy = MatchStrategyFactory.NewDefaultInstance(Reference.class);
		assertNotNull(matchStrategy);
//		assertEquals(Reference.class, matchStrategy.getMatchClass());
	}

    @Test
    public void testParsedPerson() throws MatchException {

        IParsedMatchStrategy matchStrategy = MatchStrategyFactory.NewParsedPersonInstance();
        assertNotNull(matchStrategy);
        Person fullPerson;
        Person parsedPerson;
        MatchResult result;

        fullPerson = getDefaultFullPerson();
        //should match
        parsedPerson = getDefaultParsedPerson();

        result = matchStrategy.invoke(parsedPerson, fullPerson);
//        System.out.println(result);
        assertTrue(result.isSuccessful(), "Same nom.title. should match");

        //differing nom. title.
        parsedPerson.setNomenclaturalTitle("Wrong");
        result = matchStrategy.invoke(fullPerson, parsedPerson, FAIL_ALL);
        assertFalse(matchStrategy.invoke(parsedPerson, fullPerson).isSuccessful(), "Differing nom.title should not match");

        //differing family
        parsedPerson = getDefaultParsedPerson();
        parsedPerson.setFamilyName("Wrong");
        assertFalse(matchStrategy.invoke(parsedPerson, fullPerson).isSuccessful(), "Differing family name should not match");
        fullPerson.setFamilyName(null);
        assertFalse(matchStrategy.invoke(parsedPerson, fullPerson).isSuccessful(), "Only parsed with family name should not match. Wrong direction.");

        //
        parsedPerson = getDefaultParsedPerson();
        parsedPerson.setInitials("D.");
        assertFalse(matchStrategy.invoke(fullPerson, parsedPerson).isSuccessful(), "Differing nom. title should not match");

        //nom. title. (2)
        fullPerson = getDefaultFullPerson();
        parsedPerson = getDefaultParsedPerson();
        fullPerson.setNomenclaturalTitle("Wro.");
        assertFalse(matchStrategy.invoke(fullPerson, parsedPerson).isSuccessful(), "Differing nom. title should not match");

        //collector Title
        fullPerson = getDefaultFullPerson();
        parsedPerson = getDefaultParsedPerson();
        parsedPerson.setCollectorTitle("Collector");
        result = matchStrategy.invoke(fullPerson, parsedPerson, FAIL_ALL);
        assertFalse(matchStrategy.invoke(parsedPerson, fullPerson).isSuccessful(), "Differing collectorTitle should not match");


        //fullPerson protected
        fullPerson = getDefaultFullPerson();
        parsedPerson = getDefaultParsedPerson();
        fullPerson.setTitleCache(fullPerson.getTitleCache(), true);
        assertFalse(matchStrategy.invoke(fullPerson, parsedPerson).isSuccessful(), "Differing protected title should not match");

        //parsedPerson protected
        fullPerson = getDefaultFullPerson();
        parsedPerson = getDefaultParsedPerson();
        parsedPerson.setTitleCache(parsedPerson.getTitleCache(), true);
        assertFalse(matchStrategy.invoke(fullPerson, parsedPerson).isSuccessful(), "Differing nom. title should not match");

        //test (protected) titleCaches
        fullPerson = getDefaultFullPerson();
        parsedPerson = getDefaultParsedPerson();
        assertTrue(matchStrategy.invoke(parsedPerson, fullPerson).isSuccessful(), "show again that default persons match, for next test");
        assertFalse(fullPerson.getTitleCache().equals(parsedPerson.getTitleCache()), "Show that even if both match the titleCaches must not necessarily be equal");

        parsedPerson.setTitleCache(fullPerson.getTitleCache(), true);
        assertFalse(matchStrategy.invoke(parsedPerson, fullPerson).isSuccessful(), "One protected and one unprotected titleCache should not match even "
                + "if the titleCache is equal");

        parsedPerson.setTitleCache("xxx", true);
        assertFalse(matchStrategy.invoke(fullPerson, parsedPerson).isSuccessful(), "");

        fullPerson.setTitleCache("xxx", true);
        assertTrue(matchStrategy.invoke(parsedPerson, fullPerson).isSuccessful(), "Protected but equal titleCaches should match");

        parsedPerson.setTitleCache("yyy", true);
        assertFalse(matchStrategy.invoke(parsedPerson, fullPerson).isSuccessful(), "Differing protected titleCaches should not match");
    }

    protected Person getDefaultFullPerson() {
        Person fullPerson = Person.NewInstance();
        fullPerson.setInitials("F.G.");
        fullPerson.setFamilyName("Name");
        fullPerson.setNomenclaturalTitle("Nam.");
        fullPerson.setGivenName("Full Given");
        fullPerson.setPrefix("Dr.");
        fullPerson.setSuffix("jr.");
        fullPerson.setCollectorTitle("Coll.");
        fullPerson.setLifespan(TimePeriodParser.parseString("1972-2015"));
        fullPerson.addInstitutionalMembership(institution1, TimePeriodParser.parseString("2002-2004"), "Dept. X", "Developer");
        fullPerson.updateCaches();
        return fullPerson;
    }

    protected Person getDefaultParsedPerson() {
        Person parsedPerson = Person.NewInstance();
        parsedPerson.setNomenclaturalTitle("Nam.");
        parsedPerson.updateCaches();
        return parsedPerson;
    }
    protected Person getDefaultParsedCollector() {
        Person parsedPerson = Person.NewInstance();
        parsedPerson.setCollectorTitle("Coll.");
        parsedPerson.updateCaches();
        return parsedPerson;
    }

    protected Person getParsedCollector2() {
        Person parsedPerson = Person.NewInstance();
        parsedPerson.setCollectorTitle("Nice,J.");
        parsedPerson.updateCaches();
        return parsedPerson;
    }

    @Test
    public void testParsedTeam() throws MatchException {

        IParsedMatchStrategy matchStrategy = MatchStrategyFactory.NewParsedTeamInstance();
        assertNotNull(matchStrategy);
        Team fullTeam;
        Team parsedTeam;
        MatchResult matchResult;

        fullTeam = getDefaultFullTeam();
        parsedTeam = getDefaultParsedTeam();

        //should match
        assertTrue(matchStrategy.invoke(parsedTeam, fullTeam).isSuccessful(), "Same nom.title. should match");

        //differing nom. title.
        parsedTeam.setNomenclaturalTitleCache("Wrong", true);
        assertFalse(matchStrategy.invoke(parsedTeam, fullTeam).isSuccessful(), "Differing nom.title. should not match");

        //differing family
        parsedTeam = getDefaultParsedTeam();
        parsedTeam.getTeamMembers().get(0).setFamilyName("Wrong");
        matchResult = matchStrategy.invoke(parsedTeam, fullTeam, true);
        assertFalse(matchResult.isSuccessful(), "Differing family name should not match");
        parsedTeam.getTeamMembers().get(0).setNomenclaturalTitle("Wrong.");
        matchResult = matchStrategy.invoke(parsedTeam, fullTeam, true);
        System.out.println(matchResult);
        assertFalse(matchResult.isSuccessful(), "Differing family name should not match");

        //
        parsedTeam = getDefaultParsedTeam();
        parsedTeam.getTeamMembers().get(0).setInitials("D.");
        assertFalse(matchStrategy.invoke(parsedTeam, fullTeam).isSuccessful(), "Differing nom. title should not match");

        //nom. title. (2)
        fullTeam = getDefaultFullTeam();
        parsedTeam = getDefaultParsedTeam();
        fullTeam.setNomenclaturalTitleCache("Wro.", true);
        assertFalse(matchStrategy.invoke(parsedTeam, fullTeam).isSuccessful(), "Differing nom. title should not match");

        //fullPerson protected
        fullTeam = getDefaultFullTeam();
        parsedTeam = getDefaultParsedTeam();
        fullTeam.setTitleCache(fullTeam.getTitleCache(), true);
        assertFalse(matchStrategy.invoke(parsedTeam, fullTeam).isSuccessful(), "Differing protected title should not match");

        //parsedPerson protected
        fullTeam = getDefaultFullTeam();
        parsedTeam = getDefaultParsedTeam();
        parsedTeam.setTitleCache(parsedTeam.getTitleCache(), true);
        assertFalse(matchStrategy.invoke(parsedTeam, fullTeam).isSuccessful(), "Differing nom. title should not match");

        fullTeam = getDefaultFullTeam();
        parsedTeam = getDefaultParsedTeam();
        fullTeam.setHasMoreMembers(true);
        assertFalse(matchStrategy.invoke(parsedTeam, fullTeam).isSuccessful(), "Differing nom. title should not match");
    }

    private Team getDefaultFullTeam() {
        Team team = Team.NewInstance();
        team.addTeamMember(getDefaultFullPerson());
        team.addTeamMember(getFullPerson2());
        team.updateCaches();
        return team;
    }


    protected Person getFullPerson2() {
        Person fullPerson = Person.NewInstance();
        fullPerson.setInitials("A.B.");
        fullPerson.setFamilyName("Nice");
        fullPerson.setNomenclaturalTitle("Nice");
        fullPerson.setGivenName("John");
        fullPerson.setCollectorTitle("Nice,J.");
        fullPerson.updateCaches();
        return fullPerson;
    }

    private Team getDefaultParsedTeam() {
        Team team = Team.NewInstance();
        team.addTeamMember(getDefaultParsedPerson());
        team.addTeamMember(getFullPerson2());
        team.updateCaches();
        return team;
    }

    private Team getDefaultParsedCollectorTeam() {
        Team team = Team.NewInstance();
        team.addTeamMember(getDefaultParsedCollector());
        team.addTeamMember(getParsedCollector2());
        team.updateCaches();
        return team;
    }


    protected Person getParsedPerson2() {
        Person parsedPerson = Person.NewInstance();
        parsedPerson.setNomenclaturalTitle("Nice");
        parsedPerson.updateCaches();
        return parsedPerson;
    }

    @Test
    public void testParsedCollectorPerson() throws MatchException{

        Person fullPerson = getDefaultFullPerson();
        Person parsedPerson = getDefaultParsedPerson();
        parsedPerson.setCollectorTitle("Collector");

        IParsedMatchStrategy collector_matchStrategy = MatchStrategyFactory.NewParsedCollectorPersonInstance();

        assertFalse(collector_matchStrategy.invoke(parsedPerson, fullPerson).isSuccessful(), "Differing collectorTitle should not match");

        parsedPerson = getDefaultParsedCollector();

        assertTrue(collector_matchStrategy.invoke(parsedPerson, fullPerson).isSuccessful(), "Equal collectorTitle should match even if full person has further values");

        //test (protected) titleCaches
        assertFalse(fullPerson.getTitleCache().equals(parsedPerson.getTitleCache()), "Show that even if both match the titleCaches must not necessarily be equal");

        parsedPerson.setTitleCache(fullPerson.getTitleCache(), true);
        assertFalse(collector_matchStrategy.invoke(parsedPerson, fullPerson).isSuccessful(), "One protected and one unprotected titleCache should not match even "
                + "if the titleCache is equal");

        fullPerson.setTitleCache("xxx", true);
        parsedPerson.setTitleCache("xxx", true);
        assertTrue(collector_matchStrategy.invoke(parsedPerson, fullPerson).isSuccessful(), "Protected but equal titleCaches should match");

        parsedPerson.setTitleCache("yyy", true);
        assertFalse(collector_matchStrategy.invoke(parsedPerson, fullPerson).isSuccessful(), "Differing protected titleCaches should not match");

    }

    @Test
    public void testParsedCollectorTeam() throws MatchException{
        Team parsedTeam = getDefaultParsedCollectorTeam();
        Team fullTeam = getDefaultFullTeam();

        IParsedMatchStrategy collector_matchStrategy = MatchStrategyFactory.NewParsedCollectorTeamInstance();

        assertTrue(collector_matchStrategy.invoke(parsedTeam, fullTeam).isSuccessful(), "Differing collectorTitle should not match");

        parsedTeam.getTeamMembers().get(0).setCollectorTitle("Collector");
        parsedTeam.updateCaches();
        assertFalse(collector_matchStrategy.invoke(parsedTeam, fullTeam).isSuccessful(), "Differing collectorTitle should not match");
        //reset
        parsedTeam = getDefaultParsedCollectorTeam();

        //test protected titleCache true
        parsedTeam.setProtectedNomenclaturalTitleCache(true);
        assertFalse(collector_matchStrategy.invoke(parsedTeam, fullTeam).isSuccessful(), "Differing protected titleCaches should not match");
    }

    @Test
    public void testParsedBook() throws MatchException {
        IParsedMatchStrategy matchStrategy = MatchStrategyFactory.NewParsedBookInstance();
        assertNotNull(matchStrategy);
        IBook fullBook;
        IBook parsedBook;

        fullBook = getDefaultFullBook();
        parsedBook = getDefaultParsedBook();

        //should match
        parsedBook = getDefaultParsedBook();
        assertTrue(matchStrategy.invoke(parsedBook, fullBook).isSuccessful(), "Same abbrev. title should match");
        fullBook.setDoi(DOI.fromString("10.1234/abc"));
        assertTrue(matchStrategy.invoke(parsedBook, fullBook).isSuccessful(), "DOI only with full book should match as abbrev title identifies the book");

        //differing nom. title.
        parsedBook.setAbbrevTitle("Wrong");
        assertFalse(matchStrategy.invoke(parsedBook, fullBook).isSuccessful(), "Differing abbrev. title. should not match");

        //differing family
        parsedBook = getDefaultParsedBook();
        parsedBook.setTitle("Wrong title");
        assertFalse(matchStrategy.invoke(parsedBook, fullBook).isSuccessful(), "Differing title should not match");
        fullBook.setTitle(null);
        assertFalse(matchStrategy.invoke(parsedBook, fullBook).isSuccessful(), "Title only for parsed book should not match. Wrong direction.");

        //change author
        fullBook = getDefaultFullBook();
        parsedBook = getDefaultParsedBook();
        ((Team)fullBook.getAuthorship()).getTeamMembers().get(0).setNomenclaturalTitle("Wrong");
        assertFalse(matchStrategy.invoke(parsedBook, fullBook).isSuccessful(), "Differing author in nomencl. title should not match");

        //change author
        fullBook = getDefaultFullBook();
        parsedBook = getDefaultParsedBook();
        ((Team)fullBook.getAuthorship()).getTeamMembers().get(0).setFamilyName("Changed");
        assertTrue(matchStrategy.invoke(parsedBook, fullBook).isSuccessful(), "Full book family name author changed should still match");
    }

    @Test
    public void testParsedBookSection() throws MatchException {
        IParsedMatchStrategy matchStrategy = MatchStrategyFactory.NewParsedBookSectionInstance();
        assertNotNull(matchStrategy);
        IBookSection fullBookSection;
        IBookSection parsedBookSection;

        fullBookSection = getMatchingFullBookSection();
        parsedBookSection = getDefaultParsedBookSection();
        assertTrue(matchStrategy.invoke(parsedBookSection, fullBookSection).isSuccessful(), "Only author, book and date published should match");
        fullBookSection.setDoi(DOI.fromString("10.1234/abc"));
        assertFalse(matchStrategy.invoke(parsedBookSection, fullBookSection).isSuccessful(), "Full book section having additional parameters should not match if parsed article has no identifying parameter like (abbrev)title or page");
        fullBookSection.setDoi(null);
        fullBookSection.setReferenceAbstract("My abstract");
        assertFalse(matchStrategy.invoke(parsedBookSection, fullBookSection).isSuccessful(), "Full book section having additional parameters should not match if parsed article has no identifying parameter like (abbrev)title or page");

        //should match
        fullBookSection = getDefaultFullBookSection();
        assertFalse(matchStrategy.invoke(fullBookSection, parsedBookSection).isSuccessful(), "Abbrev. title must be equal or null");
        parsedBookSection.setAbbrevTitle(fullBookSection.getAbbrevTitle());
        assertFalse(matchStrategy.invoke(parsedBookSection, fullBookSection).isSuccessful(), "Still not match because pages are not equal (parsed is null)");
        parsedBookSection.setPages(fullBookSection.getPages());
        assertFalse(matchStrategy.invoke(parsedBookSection, fullBookSection).isSuccessful(), "Now they should match");

        //differing nom. title.
        parsedBookSection.setAbbrevTitle("Wrong");
        assertFalse(matchStrategy.invoke(parsedBookSection, fullBookSection).isSuccessful(), "Differing abbrev. title. should not match");

        //differing family
        parsedBookSection = getDefaultParsedBookSection();
        parsedBookSection.setTitle("Wrong title");
        assertFalse(matchStrategy.invoke(parsedBookSection, fullBookSection).isSuccessful(), "Differing title should not match");
        fullBookSection.setTitle(null);
        assertFalse(matchStrategy.invoke(parsedBookSection, fullBookSection).isSuccessful(), "Title only for parsed book should not match. Wrong direction.");

        //change author
        fullBookSection = getMatchingFullBookSection();
        parsedBookSection = getDefaultParsedBookSection();
        fullBookSection.getAuthorship().setNomenclaturalTitleCache("Wrong", true);
        assertFalse(matchStrategy.invoke(parsedBookSection, fullBookSection).isSuccessful(), "Differing author in nomencl. title should not match");

        //change author
        fullBookSection = getMatchingFullBookSection();
        parsedBookSection = getDefaultParsedBookSection();
        ((Person)fullBookSection.getAuthorship()).setFamilyName("Changed");
        assertTrue(matchStrategy.invoke(parsedBookSection, fullBookSection).isSuccessful(), "Full book family name author changed should still match");
    }

    private IBook getDefaultFullBook() {
        IBook book = getDefaultParsedBook();
        book.setAuthorship(getDefaultFullTeam());
        book.setTitle("Flora Hellenica");
        book.setUri(URI.create("https://www.flora.hellenica.gr"));
        book.setIsbn("1234-222-222-333");  //format not yet correct
        book.setPublisher("Greek publisher", "Athens");
        ((Reference)book).updateCaches();
        return book;
    }

    private IBook getDefaultParsedBook() {
        IBook book = ReferenceFactory.newBook();
        book.setAbbrevTitle("Fl. Hell.");
        book.setVolume("2");
        book.setEdition("ed. 3");
        book.setAuthorship(getDefaultParsedTeam());
        book.setDatePublished(TimePeriodParser.parseStringVerbatim("1982-10-06"));
        ((Reference)book).updateCaches();
        return book;
    }

    private IBookSection getDefaultFullBookSection() {
        IBookSection bookSection = getMatchingFullBookSection();
        bookSection.setTitle("Book section title");
        bookSection.setAbbrevTitle("Bk. sct. tit.");
        bookSection.setPages("22-33");
        ((Reference)bookSection).updateCaches();
        return bookSection;
    }

    private IBookSection getMatchingFullBookSection() {
        IBookSection bookSection = ReferenceFactory.newBookSection();
        bookSection.setAuthorship(getDefaultFullPerson());
        bookSection.setInBook(getDefaultFullBook());
        bookSection.setDatePublished(TimePeriodParser.parseStringVerbatim("1892"));
        ((Reference)bookSection).updateCaches();
        return bookSection;
    }

    private IBookSection getDefaultParsedBookSection() {
        IBookSection bookSection = ReferenceFactory.newBookSection();
        bookSection.setAuthorship(getDefaultParsedPerson());
        bookSection.setInBook(getDefaultParsedBook());
        bookSection.setDatePublished(TimePeriodParser.parseStringVerbatim("1892"));
        ((Reference)bookSection).updateCaches();
        return bookSection;
    }

    @Test
    public void testParsedArticle() throws MatchException {

        IParsedMatchStrategy matchStrategy = MatchStrategyFactory.NewParsedArticleInstance();
        assertNotNull(matchStrategy);
        IArticle fullArticle;
        IArticle parsedArticle;

        fullArticle = getMatchingFullArticle();
        parsedArticle = getDefaultParsedArticle();
        assertTrue(matchStrategy.invoke(parsedArticle, fullArticle).isSuccessful(), "Having only paramters both have in common like author, book and date published "
                + "should match");
        fullArticle.setDoi(DOI.fromString("10.1234/abc"));
        assertFalse(matchStrategy.invoke(parsedArticle, fullArticle).isSuccessful(), "Full article having additional parameters should not match if parsed article has no identifying parameter like (abbrev)title or page");

        //no match due to missing abbrev title match
        fullArticle = getDefaultFullArticle();
        assertFalse(matchStrategy.invoke(parsedArticle,
                fullArticle).isSuccessful(), "Abbrev. title must be equal or null");
        parsedArticle.setAbbrevTitle(fullArticle.getAbbrevTitle());
        assertFalse(matchStrategy.invoke(parsedArticle, fullArticle).isSuccessful(), "Still not match because pages are not equal (parsed is null)");
        //FIXME in future this should not fail, but parsed articles never have really pages, they only have page or a page span in the parsed detail which is not the same as the pages of the article
        parsedArticle.setPages(fullArticle.getPages());
        assertFalse(matchStrategy.invoke(parsedArticle, fullArticle).isSuccessful(), "Now they should match");

        //differing nom. title.
        parsedArticle.setAbbrevTitle("Wrong");
        assertFalse(matchStrategy.invoke(parsedArticle, fullArticle).isSuccessful(), "Differing abbrev. title. should not match");

        //differing family
        parsedArticle = getDefaultParsedArticle();
        parsedArticle.setTitle("Wrong title");
        assertFalse(matchStrategy.invoke(parsedArticle, fullArticle).isSuccessful(), "Differing title should not match");
        fullArticle.setTitle(null);
        assertFalse(matchStrategy.invoke(parsedArticle, fullArticle).isSuccessful(), "Title only for parsed book should not match. Wrong direction.");

        //change author
        fullArticle = getMatchingFullArticle();
        parsedArticle = getDefaultParsedArticle();
        fullArticle.getAuthorship().setNomenclaturalTitleCache("Wrong", true);
        assertFalse(matchStrategy.invoke(parsedArticle, fullArticle).isSuccessful(), "Differing author in nomencl. title should not match");

        //change author
        fullArticle = getMatchingFullArticle();
        parsedArticle = getDefaultParsedArticle();
        ((Team)fullArticle.getAuthorship()).getTeamMembers().get(0).setFamilyName("Changed");
        assertTrue(matchStrategy.invoke(parsedArticle, fullArticle).isSuccessful(), "Full book family name author changed should still match");
    }

    /**
     * Like {@link #getMatchingFullArticle() matching full article} with additional
     * title, abbrevTitle and pages
     */
    private IArticle getDefaultFullArticle() {
        IArticle article = getMatchingFullArticle();
        article.setTitle("Article title");
        article.setAbbrevTitle("Art. tit.");
        article.setPages("22-33");
        ((Reference)article).updateCaches();
        return article;
    }

    /**
     * Article with {@link #getDefaultFullTeam() full team},
     * {@link #getDefaultFullJournal() full journal} and date published
     */
    private IArticle getMatchingFullArticle() {
        IArticle article = ReferenceFactory.newArticle();
        article.setAuthorship(getDefaultFullTeam());
        article.setInJournal(getDefaultFullJournal());
        article.setDatePublished(TimePeriodParser.parseStringVerbatim("1950"));
        ((Reference)article).updateCaches();
        return article;
    }

    /**
     * Article with {@link #getDefaultParsedTeam() parsed authorship},
     * {@link #getDefaultParsedJournal() parsed journal} and date published.
     */
    private IArticle getDefaultParsedArticle() {
        IArticle article = ReferenceFactory.newArticle();
        article.setAuthorship(getDefaultParsedTeam());
        article.setInJournal(getDefaultParsedJournal());
        article.setDatePublished(TimePeriodParser.parseStringVerbatim("1950"));
        ((Reference)article).updateCaches();
        return article;
    }

    private IJournal getDefaultFullJournal() {
        IJournal journal = getDefaultParsedJournal();
//        journal.setAuthorship(getDefaultFullTeam()); //journals should not have authors
        journal.setTitle("J. Flow. Pl.");
        journal.setUri(URI.create("https://www.journal-flowering-plants.gr"));
        journal.setIssn("1234-222-222-333");  //format not yet correct
        journal.setPublisher("Botanical publisher", "Paris");
        ((Reference)journal).updateCaches();
        return journal;
    }

    private IJournal getDefaultParsedJournal() {
        IJournal journal = ReferenceFactory.newJournal();
        journal.setAbbrevTitle("Fl. Hell.");
//        journal.setAuthorship(getDefaultParsedTeam());  //journals should not have authors
//        ((Reference)journal).setDatePublished(TimePeriodParser.parseStringVerbatim("1992-04-03")); //journals should not have date published
        ((Reference)journal).updateCaches();
        return journal;
    }

    @Test
    public void testParsedReference() throws MatchException {
        IParsedMatchStrategy matchStrategy = MatchStrategyFactory.NewParsedReferenceInstance();
        assertNotNull(matchStrategy);
        IArticle fullArticle;
        IArticle parsedArticle;

        fullArticle = getMatchingFullArticle();
        parsedArticle = getDefaultParsedArticle();
        assertTrue(matchStrategy.invoke(parsedArticle, fullArticle).isSuccessful(), "Only author, book and date published should match");

        //should match
        fullArticle = getDefaultFullArticle();
        assertFalse(matchStrategy.invoke(parsedArticle, fullArticle).isSuccessful(), "Abbrev. title must be equal or null");
        parsedArticle.setAbbrevTitle(fullArticle.getAbbrevTitle());
        assertFalse(matchStrategy.invoke(parsedArticle, fullArticle).isSuccessful(), "Still not match because pages are not equal (parsed is null)");
        parsedArticle.setPages(fullArticle.getPages());
        assertFalse(matchStrategy.invoke(parsedArticle, fullArticle).isSuccessful(), "Now they should match");

        //differing nom. title.
        parsedArticle.setAbbrevTitle("Wrong");
        assertFalse(matchStrategy.invoke(parsedArticle, fullArticle).isSuccessful(), "Differing abbrev. title. should not match");

        //differing family
        parsedArticle = getDefaultParsedArticle();
        parsedArticle.setTitle("Wrong title");
        assertFalse(matchStrategy.invoke(parsedArticle, fullArticle).isSuccessful(), "Differing title should not match");
        fullArticle.setTitle(null);
        assertFalse(matchStrategy.invoke(parsedArticle, fullArticle).isSuccessful(), "Title only for parsed book should not match. Wrong direction.");

        //change author
        fullArticle = getMatchingFullArticle();
        parsedArticle = getDefaultParsedArticle();
        fullArticle.getAuthorship().setNomenclaturalTitleCache("Wrong", true);
        assertFalse(matchStrategy.invoke(parsedArticle, fullArticle).isSuccessful(), "Differing author in nomencl. title should not match");

        //change author
        fullArticle = getMatchingFullArticle();
        parsedArticle = getDefaultParsedArticle();
        ((Team)fullArticle.getAuthorship()).getTeamMembers().get(0).setFamilyName("Changed");
        assertTrue(matchStrategy.invoke(parsedArticle, fullArticle).isSuccessful(), "Full book family name author changed should still match");
    }

    /**
     * Name like {@link #getDefaultParsedName()} with
     * nom. ref. and additional
     * name description (etymology).
     */
    private TaxonName getDefaultFullName() {
        TaxonName name = getDefaultParsedName();
        name.setNameApprobation("approbation");
        TaxonNameDescription description = TaxonNameDescription.NewInstance(name);
        TextData etymology = TextData.NewInstance(Feature.ETYMOLOGY());
        etymology.putText(Language.ENGLISH(), "My etymology");
        description.addElement(etymology);

        name.setNomenclaturalReference(getDefaultFullArticle());
        name.setNomenclaturalMicroReference("p. 123");

        //TODO continue
        name.updateCaches();
        return name;
    }

    /**
     * Name "Abies alba Nam. & Nice", with team members having more data
     * @see #getDefaultFullTeam()
     */
    private TaxonName getDefaultParsedName() {
        TaxonName name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        name.setCombinationAuthorship(getDefaultFullTeam());
        name.setGenusOrUninomial("Abies");
        name.setSpecificEpithet("alba");
        name.updateCaches();
        return name;
    }

    @Test
    public void testParsedOriginalSpelling() throws MatchException {
        IParsedMatchStrategy matchStrategy = MatchStrategyFactory.NewParsedOriginalSpellingInstance();
        assertNotNull(matchStrategy);

        TaxonName fullName = getDefaultFullName();
        TaxonName parsedName = getDefaultParsedName();
        assertTrue(matchStrategy.invoke(parsedName, fullName).isSuccessful(), "Only ... should match");

        //TODO
    }

    @Test
    //TODO discuss if parsedOriginalSpelling and parsedHybridParent can be merged,
    //     they probably follow the same rules
    public void testParsedHybridParent() throws MatchException {
        IParsedMatchStrategy matchStrategy = MatchStrategyFactory.NewParsedHybridParentInstance();
        assertNotNull(matchStrategy);

        TaxonName fullName = getDefaultFullName();
        TaxonName parsedName = getDefaultParsedName();
        assertTrue(matchStrategy.invoke(parsedName, fullName).isSuccessful(), "Only ... should match");

        parsedName.setCombinationAuthorship(null);
        assertFalse(matchStrategy.invoke(parsedName, fullName).isSuccessful(), "Authors must match exactly");

        parsedName.setCombinationAuthorship(getDefaultParsedPerson());
        assertFalse(matchStrategy.invoke(parsedName, fullName).isSuccessful(), "Authors must not differ");

        //TODO testParsedHybridParent tbc, test not yet complete
    }
}
