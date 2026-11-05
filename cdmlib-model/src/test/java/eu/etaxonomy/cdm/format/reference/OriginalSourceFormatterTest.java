/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.TimePeriodBase;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.reference.IDatabase;
import eu.etaxonomy.cdm.model.reference.ISection;
import eu.etaxonomy.cdm.model.reference.IWebPage;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.cache.reference.ReferenceDefaultCacheStrategyTest;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @since 03.05.2021
 */
public class OriginalSourceFormatterTest {

    //book // book section
    private static Reference book1;
    private static Team bookTeam1;

    private static OriginalSourceFormatter formatter = OriginalSourceFormatter.INSTANCE;
    private static OriginalSourceFormatter formatterWithBrackets = OriginalSourceFormatter.INSTANCE_WITH_YEAR_BRACKETS;
    private static OriginalSourceFormatter formatterLongCitation = OriginalSourceFormatter.INSTANCE_LONG_CITATION;
    private static OriginalSourceFormatter formatterWithHtml = OriginalSourceFormatter.INSTANCE_LONG_CITATION_WITH_HTML;

    @BeforeEach
    public void setUp() throws Exception {

        //book / section
        book1 = ReferenceFactory.newBook();
        bookTeam1 = Team.NewTitledInstance("Book Author", "TT.");
    }

    @Test
    public void testCreateShortCitation(){
        book1.setTitle("My book");
        book1.setAuthorship(bookTeam1);
        book1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1975));
        assertEquals("Book Author 1975: My book", book1.getTitleCache(), "Unexpected title cache.");

        book1.setTitleCache(null, false);
        book1.setEdition("ed. 3");

        assertEquals("Book Author 1975", formatter.format(book1, null), "Unexpected title cache.");
        assertEquals("Book Author 1975", formatter.format(book1, ""), "Unexpected title cache.");
        assertEquals("Book Author 1975: 55", formatter.format(book1, "55"), "Unexpected title cache.");
        assertEquals("Book Author (1975: 55)", formatterWithBrackets.format(book1, "55"), "Unexpected title cache.");
        assertEquals("Book Author (1975b)", formatterWithBrackets.format(book1, null, null, "b"), "Unexpected title cache.");
        assertEquals("Book Author (1975b: 66)", formatterWithBrackets.format(book1, "66", null, "b"), "Unexpected title cache.");

        //1 person
        Person person1 = Person.NewInstance("Pers.", "Person", "P.", "Percy");
        Team team = Team.NewInstance(person1);
        book1.setAuthorship(team);
        assertEquals("Person 1975: 55", formatter.format(book1, "55"), "Unexpected title cache.");
        team.setHasMoreMembers(true);
        assertEquals("Person & al. 1975: 55", formatter.format(book1, "55"), "Unexpected title cache.");
        team.setHasMoreMembers(false);

        //2 persons
        Person person2 = Person.NewInstance("Lers.", "Lerson", "L.", "Lercy");
        team.addTeamMember(person2);
        assertEquals("Person & Lerson 1975: 55", formatter.format(book1, "55"), "Unexpected title cache.");
        team.setHasMoreMembers(true);
        assertEquals("Person & al. 1975: 55", formatter.format(book1, "55"), "Unexpected title cache.");
        team.setHasMoreMembers(false);

        //3 persons
        Person person3 = Person.NewInstance("Gers.", "Gerson", "G.", "Gercy");
        team.addTeamMember(person3);
        assertEquals("Person & al. 1975: 55", formatter.format(book1, "55"), "Unexpected title cache.");
        team.setHasMoreMembers(true);
        assertEquals("Person & al. 1975: 55", formatter.format(book1, "55"), "Unexpected title cache.");
        team.setHasMoreMembers(false);  //in case we want to continue test
    }

    /**
     * @see {@link ReferenceDefaultCacheStrategyTest#testWebPageGetTitleCache()}
     */
    //#10057 see also ReferenceDefaultCacheStrategy
    @Test
    public void testWebPage(){

        //create webpage
        IWebPage webpage = ReferenceFactory.newWebPage();
        webpage.setAbbrevTitle("A beautiful taxon page");
        VerbatimTimePeriod datePublished = TimePeriodParser.parseStringVerbatim("2001");
        TimePeriodBase accessed = TimePeriodParser.parseStringVerbatim("2020");
        webpage.setDatePublished(datePublished);
        Person person = Person.NewInstance(null, "Miller", "A.", "Adam");
        webpage.setAuthorship(person);

        //assert with author
        assertEquals("Miller 2020", formatter.format((Reference)webpage, null, accessed), "Should be author and source.accessed");
        //not sure if details is needed but to be on the save side...
        assertEquals("Miller 2020: detail", formatter.format((Reference)webpage, "detail", accessed), "Should be author, source.accessed and detail");
        assertEquals("Miller 2001", formatter.format((Reference)webpage, null, null), "Should be author and webpage.datePublished year");
        assertEquals("Miller 2001c", formatter.format((Reference)webpage, null, null, "c"), "Should be author and webpage.datePublished unique year");

        //assert without author (the expected behavior is still undefined)
        webpage.setAuthorship(null);
        webpage.resetTitleCache();
        assertEquals("A beautiful taxon page 2020", formatter.format((Reference)webpage, null, accessed), "Formatting of webpages without author is still undefined");
        assertEquals("A beautiful taxon page 2020: detail", formatter.format((Reference)webpage, "detail", accessed), "Formatting of webpages without author is still undefined");
        assertEquals("A beautiful taxon page 2001", formatter.format((Reference)webpage, null, null), "Formatting of webpages without author is still undefined");
        webpage.resetTitleCache();
        assertEquals("A beautiful taxon page 2001", formatter.format((Reference)webpage, null, null), "Formatting of webpages without author is still undefined");
        assertEquals("A beautiful taxon page 2001: detail", formatter.format((Reference)webpage, "detail", null), "Formatting of webpages without author is still undefined");
        assertEquals("A beautiful taxon page 2001c: detail", formatter.format((Reference)webpage, "detail", null, "c"), "Formatting of webpages without author is still undefined");
    }

    @Test
    public void testDatabase(){

        //create database
        IDatabase database = ReferenceFactory.newDatabase();
        database.setAbbrevTitle("A beautiful taxon page");
        Person person = Person.NewInstance(null, "Miller", "A.", "Adam");
        database.setAuthorship(person);

        VerbatimTimePeriod datePublished = TimePeriodParser.parseStringVerbatim("2001");
        datePublished.setContinued(true);
        database.setDatePublished(datePublished);

        TimePeriod sourceAccessed = TimePeriodParser.parseString("2020");

        //assert with author
        assertEquals("Miller 2020", formatter.format((Reference)database, null, sourceAccessed), "Should be author and source.accessed");
        //not sure if details is needed but to be on the save side...
        assertEquals("Miller 2020: detail", formatter.format((Reference)database, "detail", sourceAccessed), "Should be author, source.accessed and detail");
        //TODO not clear yet how to handle the "+"
        assertEquals("Miller 2001+", formatter.format((Reference)database, null, null), "Should be author and webpage.datePublished year");
        assertEquals("Miller 2001+c", formatter.format((Reference)database, null, null, "c"), "Should be author and webpage.datePublished unique year");

        //long form
        assertEquals("Miller, A. 2001+: A beautiful taxon page [accessed: 2020]", formatterLongCitation.format((Reference)database, null, sourceAccessed), "Should be author and source.accessed");
        //not sure if details is needed but to be on the save side...
        assertEquals("Miller, A. 2001+: A beautiful taxon page. detail [accessed: 2020]", formatterLongCitation.format((Reference)database, "detail", sourceAccessed), "Should be author, source.accessed and detail");
        assertEquals("Miller, A. 2001+: A beautiful taxon page", formatterLongCitation.format((Reference)database, null, null), "Should be author and webpage.accessed year");
        assertEquals("Miller, A. 2001+: A beautiful taxon page", formatterLongCitation.format((Reference)database, null, null), "Should be author and webpage.datePublished year");
        assertEquals("Miller, A. 2001+: A beautiful taxon page", formatterLongCitation.format((Reference)database, null, null, "c"), "Should be author and webpage.datePublished unique year");


        //assert without author (the expected behavior is still undefined)
        database.setAuthorship(null);
        database.setUri(URI.create("https://abc.de/"));
        //database.setUri(URI.create("https://redlist.necca.gov.gr/"));
        //TODO do we really want to show the URI in the short form?
        database.resetTitleCache();
        assertEquals("A beautiful taxon page. Published at https://abc.de/ 2020", formatter.format((Reference)database, null, sourceAccessed), "Formatting of webpages without author is still undefined");
        assertEquals("A beautiful taxon page. Published at https://abc.de/ 2020: detail", formatter.format((Reference)database, "detail", sourceAccessed), "Formatting of webpages without author is still undefined");
        assertEquals("A beautiful taxon page. Published at https://abc.de/ 2001+", formatter.format((Reference)database, null, null), "Formatting of webpages without author is still undefined");
        database.resetTitleCache();
        //TODO is it really correct to have the date at the end here
        assertEquals("A beautiful taxon page. Published at https://abc.de/ 2001+", formatter.format((Reference)database, null, null), "Formatting of webpages without author is still undefined");
        assertEquals("A beautiful taxon page. Published at https://abc.de/ 2001+: detail", formatter.format((Reference)database, "detail", null), "Formatting of webpages without author is still undefined");
        assertEquals("A beautiful taxon page. Published at https://abc.de/ 2001+c: detail", formatter.format((Reference)database, "detail", null, "c"), "Formatting of webpages without author is still undefined");

        //long form
        assertEquals("2001+: A beautiful taxon page. Published at https://abc.de/ [accessed: 2020]", formatterLongCitation.format((Reference)database, null, sourceAccessed), "Should be author and source.accessed");

        //section
        database.setAuthorship(person);
        ISection databaseSection = ReferenceFactory.newSection();
        databaseSection.setTitle("My database section");
        assertEquals("Miller 2020: detail", formatter.format((Reference)database, "detail", sourceAccessed), "Formatting of webpages without author is still undefined");
        assertEquals("Miller, A. 2001+: A beautiful taxon page. Published at https://abc.de/. detail [accessed: 2020]", formatterLongCitation.format((Reference)database, "detail", sourceAccessed), "Formatting of webpages without author is still undefined");
        //... again with source
        DescriptionElementSource source = DescriptionElementSource.NewPrimarySourceInstance((Reference)database, "detail");
        source.setAccessed(sourceAccessed);
        assertEquals("Miller, A. 2001+: A beautiful taxon page. Published at https://abc.de/. detail [accessed: 2020]", formatterLongCitation.format(source), "Formatting of webpages without author is still undefined");

        assertEquals("Miller, A. 2001+: A beautiful taxon page. Published at <a href=\"https://abc.de/\">https://abc.de/</a>. detail [accessed: 2020]", formatterWithHtml.format(source), "Formatting of webpages with html around url");

    }


    @Test
    public void testProtectedTitleCacheCitation(){

        Reference ref = ReferenceFactory.newGeneric();
        ref.setTitleCache("Miller: Protected title cache reference", true);
        assertEquals("Miller: Protected title cache reference", formatter.format(ref, null, null), "");

        ref.setDatePublished(TimePeriodParser.parseStringVerbatim("1982"));
        assertEquals("Miller: Protected title cache reference 1982", formatter.format(ref, null, null), "");

        ref.setTitleCache("Miller 1982. Protected title cache reference", true);
        assertEquals("Miller 1982. Protected title cache reference", formatter.format(ref, null, null), "");

        ref.setTitleCache("Miller 1982: Protected title cache reference", true);
        assertEquals("Miller 1982: Protected title cache reference", formatter.format(ref, null, null), "");
    }

    @Test
    public void testCreateLongCitation(){
        //TODO this is still preliminary, formatting may change in future.

        book1.setTitle("My book");
        book1.setAuthorship(bookTeam1);
        book1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1975));
        assertEquals("Book Author 1975: My book", book1.getTitleCache(), "Unexpected title cache.");

        book1.setTitleCache(null, false);
        book1.setEdition("ed. 3");

        assertEquals("Book Author 1975: My book, ed. 3", formatterLongCitation.format(book1, null), "Unexpected title cache.");
        assertEquals("Book Author 1975: My book, ed. 3", formatterLongCitation.format(book1, ""), "Unexpected title cache.");
        assertEquals("Book Author 1975: My book, ed. 3. p 55", formatterLongCitation.format(book1, "55"), "Unexpected title cache.");

        //1 person
        Person person1 = Person.NewInstance("Pers.", "Person", "P.", "Percy");
        Team team = Team.NewInstance(person1);
        book1.setAuthorship(team);
        assertEquals("Person, P. 1975: My book, ed. 3. p 55", formatterLongCitation.format(book1, "55"), "Unexpected title cache.");
        team.setHasMoreMembers(true);
        assertEquals("Person, P. & al. 1975: My book, ed. 3. p 55", formatterLongCitation.format(book1, "55"), "Unexpected title cache.");
        team.setHasMoreMembers(false);

        //2 persons
        Person person2 = Person.NewInstance("Lers.", "Lerson", "L.", "Lercy");
        team.addTeamMember(person2);
        assertEquals("Person, P. & Lerson, L. 1975: My book, ed. 3. p 55", formatterLongCitation.format(book1, "55"), "Unexpected title cache.");
        team.setHasMoreMembers(true);
        assertEquals("Person, P., Lerson, L. & al. 1975: My book, ed. 3. p 55", formatterLongCitation.format(book1, "55"), "Unexpected title cache.");
        team.setHasMoreMembers(false);

        //3 persons
        Person person3 = Person.NewInstance("Gers.", "Gerson", "G.", "Gercy");
        team.addTeamMember(person3);
        assertEquals("Person, P., Lerson, L. & Gerson, G. 1975: My book, ed. 3. p 55", formatterLongCitation.format(book1, "55"), "Unexpected title cache.");
        team.setHasMoreMembers(true);
        assertEquals("Person, P., Lerson, L., Gerson, G. & al. 1975: My book, ed. 3. p 55", formatterLongCitation.format(book1, "55"), "Unexpected title cache.");
        team.setHasMoreMembers(false);  //in case we want to continue test
    }

    @Test
    public void testCreateCitationWithHtml(){
        //TODO this is still preliminary, formatting may change in future.

        book1.setTitle("My book");
        book1.setAuthorship(bookTeam1);
        book1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1975));
        assertEquals("Book Author 1975: My book", book1.getTitleCache(), "Unexpected title cache.");
    }

}