/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.reference;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.TimePeriod;
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

    @Before
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
        Assert.assertEquals("Unexpected title cache.", "Book Author 1975: My book", book1.getTitleCache());

        book1.setTitleCache(null, false);
        book1.setEdition("ed. 3");

        Assert.assertEquals("Unexpected title cache.", "Book Author 1975", formatter.format(book1, null));
        Assert.assertEquals("Unexpected title cache.", "Book Author 1975", formatter.format(book1, ""));
        Assert.assertEquals("Unexpected title cache.", "Book Author 1975: 55", formatter.format(book1, "55"));
        Assert.assertEquals("Unexpected title cache.", "Book Author (1975: 55)",
                formatterWithBrackets.format(book1, "55"));
        Assert.assertEquals("Unexpected title cache.", "Book Author (1975b)",
                formatterWithBrackets.format(book1, null, null, "b"));
        Assert.assertEquals("Unexpected title cache.", "Book Author (1975b: 66)",
                formatterWithBrackets.format(book1, "66", null, "b"));

        //1 person
        Person person1 = Person.NewInstance("Pers.", "Person", "P.", "Percy");
        Team team = Team.NewInstance(person1);
        book1.setAuthorship(team);
        Assert.assertEquals("Unexpected title cache.", "Person 1975: 55", formatter.format(book1, "55"));
        team.setHasMoreMembers(true);
        Assert.assertEquals("Unexpected title cache.", "Person & al. 1975: 55", formatter.format(book1, "55"));
        team.setHasMoreMembers(false);

        //2 persons
        Person person2 = Person.NewInstance("Lers.", "Lerson", "L.", "Lercy");
        team.addTeamMember(person2);
        Assert.assertEquals("Unexpected title cache.", "Person & Lerson 1975: 55", formatter.format(book1, "55"));
        team.setHasMoreMembers(true);
        Assert.assertEquals("Unexpected title cache.", "Person & al. 1975: 55", formatter.format(book1, "55"));
        team.setHasMoreMembers(false);

        //3 persons
        Person person3 = Person.NewInstance("Gers.", "Gerson", "G.", "Gercy");
        team.addTeamMember(person3);
        Assert.assertEquals("Unexpected title cache.", "Person & al. 1975: 55", formatter.format(book1, "55"));
        team.setHasMoreMembers(true);
        Assert.assertEquals("Unexpected title cache.", "Person & al. 1975: 55", formatter.format(book1, "55"));
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
        TimePeriod accessed = TimePeriodParser.parseStringVerbatim("2020");
        DateTime webpageAccessed = DateTime.parse("2010-06-30T01:20+02:00");
        webpage.setAccessed(webpageAccessed);
        webpage.setDatePublished(datePublished);
        Person person = Person.NewInstance(null, "Miller", "A.", "Adam");
        webpage.setAuthorship(person);

        //assert with author
        Assert.assertEquals("Should be author and source.accessed",
                "Miller 2020", formatter.format((Reference)webpage, null, accessed));
        //not sure if details is needed but to be on the save side...
        Assert.assertEquals("Should be author, source.accessed and detail",
                "Miller 2020: detail", formatter.format((Reference)webpage, "detail", accessed));
        Assert.assertEquals("Should be author and webpage.accessed year",
                "Miller 2010", formatter.format((Reference)webpage, null, null));
        webpage.setAccessed(null);
        Assert.assertEquals("Should be author and webpage.datePublished year", "Miller 2001", formatter.format((Reference)webpage, null, null));
        Assert.assertEquals("Should be author and webpage.datePublished unique year", "Miller 2001c", formatter.format((Reference)webpage, null, null, "c"));

        //assert without author (the expected behavior is still undefined)
        webpage.setAuthorship(null);
        webpage.setAccessed(webpageAccessed);
        webpage.resetTitleCache();
        Assert.assertEquals("Formatting of webpages without author is still undefined",
                "A beautiful taxon page 2020", formatter.format((Reference)webpage, null, accessed));
        Assert.assertEquals("Formatting of webpages without author is still undefined",
                "A beautiful taxon page 2020: detail", formatter.format((Reference)webpage, "detail", accessed));
        Assert.assertEquals("Formatting of webpages without author is still undefined",
                "A beautiful taxon page 2010", formatter.format((Reference)webpage, null, null));
        webpage.setAccessed(null);
        webpage.resetTitleCache();
        Assert.assertEquals("Formatting of webpages without author is still undefined",
                "A beautiful taxon page 2001", formatter.format((Reference)webpage, null, null));
        Assert.assertEquals("Formatting of webpages without author is still undefined",
                "A beautiful taxon page 2001: detail", formatter.format((Reference)webpage, "detail", null));
        Assert.assertEquals("Formatting of webpages without author is still undefined",
                "A beautiful taxon page 2001c: detail", formatter.format((Reference)webpage, "detail", null, "c"));
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

        DateTime databaseAccessed = DateTime.parse("2010-06-30T01:20+02:00");
        database.setAccessed(databaseAccessed);

        TimePeriod sourceAccessed = TimePeriodParser.parseStringVerbatim("2020");

        //assert with author
        Assert.assertEquals("Should be author and source.accessed", "Miller 2020", formatter.format((Reference)database, null, sourceAccessed));
        //not sure if details is needed but to be on the save side...
        Assert.assertEquals("Should be author, source.accessed and detail", "Miller 2020: detail", formatter.format((Reference)database, "detail", sourceAccessed));
        Assert.assertEquals("Should be author and webpage.accessed year", "Miller 2010", formatter.format((Reference)database, null, null));
        database.setAccessed(null);
        //TODO not clear yet how to handle the "+"
        Assert.assertEquals("Should be author and webpage.datePublished year", "Miller 2001+", formatter.format((Reference)database, null, null));
        Assert.assertEquals("Should be author and webpage.datePublished unique year", "Miller 2001+c", formatter.format((Reference)database, null, null, "c"));

        //long form
        Assert.assertEquals("Should be author and source.accessed", "Miller, A. 2001+: A beautiful taxon page [accessed: 2020]", formatterLongCitation.format((Reference)database, null, sourceAccessed));
        //not sure if details is needed but to be on the save side...
        Assert.assertEquals("Should be author, source.accessed and detail", "Miller, A. 2001+: A beautiful taxon page. detail [accessed: 2020]", formatterLongCitation.format((Reference)database, "detail", sourceAccessed));
        Assert.assertEquals("Should be author and webpage.accessed year", "Miller, A. 2001+: A beautiful taxon page", formatterLongCitation.format((Reference)database, null, null));
        database.setAccessed(null);
        Assert.assertEquals("Should be author and webpage.datePublished year", "Miller, A. 2001+: A beautiful taxon page", formatterLongCitation.format((Reference)database, null, null));
        Assert.assertEquals("Should be author and webpage.datePublished unique year", "Miller, A. 2001+: A beautiful taxon page", formatterLongCitation.format((Reference)database, null, null, "c"));


        //assert without author (the expected behavior is still undefined)
        database.setAuthorship(null);
        database.setAccessed(databaseAccessed);
        database.setUri(URI.create("https://abc.de/"));
        //TODO do we really want to show the URI in the short form?
        database.resetTitleCache();
        Assert.assertEquals("Formatting of webpages without author is still undefined",
                "A beautiful taxon page. Published at https://abc.de/ 2020", formatter.format((Reference)database, null, sourceAccessed));
        Assert.assertEquals("Formatting of webpages without author is still undefined",
                "A beautiful taxon page. Published at https://abc.de/ 2020: detail", formatter.format((Reference)database, "detail", sourceAccessed));
        Assert.assertEquals("Formatting of webpages without author is still undefined",
                "A beautiful taxon page. Published at https://abc.de/ 2010", formatter.format((Reference)database, null, null));
        database.setAccessed(null);
        database.resetTitleCache();
        //TODO is it really correct to have the date at the end here
        Assert.assertEquals("Formatting of webpages without author is still undefined",
                "A beautiful taxon page. Published at https://abc.de/ 2001+", formatter.format((Reference)database, null, null));
        Assert.assertEquals("Formatting of webpages without author is still undefined",
                "A beautiful taxon page. Published at https://abc.de/ 2001+: detail", formatter.format((Reference)database, "detail", null));
        Assert.assertEquals("Formatting of webpages without author is still undefined",
                "A beautiful taxon page. Published at https://abc.de/ 2001+c: detail", formatter.format((Reference)database, "detail", null, "c"));

        //long form
        Assert.assertEquals("Should be author and source.accessed", "2001+: A beautiful taxon page. Published at https://abc.de/ [accessed: 2020]", formatterLongCitation.format((Reference)database, null, sourceAccessed));

        //section
        database.setAuthorship(person);
        ISection databaseSection = ReferenceFactory.newSection();
        databaseSection.setTitle("My database section");
        DateTime sectionAccessed = DateTime.parse("2010-06-30T01:20+02:00");
        ((Reference)databaseSection).setAccessed(sectionAccessed);
        Assert.assertEquals("Formatting of webpages without author is still undefined",
                "Miller 2020: detail", formatter.format((Reference)database, "detail", sourceAccessed));
        Assert.assertEquals("Formatting of webpages without author is still undefined",
                "Miller, A. 2001+: A beautiful taxon page. Published at https://abc.de/. detail [accessed: 2020]",
                formatterLongCitation.format((Reference)database, "detail", sourceAccessed));
        //... again with source
        DescriptionElementSource source = DescriptionElementSource.NewPrimarySourceInstance((Reference)database, "detail");
        source.setAccessed(sourceAccessed);
        Assert.assertEquals("Formatting of webpages without author is still undefined",
                "Miller, A. 2001+: A beautiful taxon page. Published at https://abc.de/. detail [accessed: 2020]",
                formatterLongCitation.format(source));
    }


    @Test
    public void testProtectedTitleCacheCitation(){

        Reference ref = ReferenceFactory.newGeneric();
        ref.setTitleCache("Miller: Protected title cache reference", true);
        Assert.assertEquals("", "Miller: Protected title cache reference", formatter.format(ref, null, null));

        ref.setDatePublished(TimePeriodParser.parseStringVerbatim("1982"));
        Assert.assertEquals("", "Miller: Protected title cache reference 1982", formatter.format(ref, null, null));

        ref.setTitleCache("Miller 1982. Protected title cache reference", true);
        Assert.assertEquals("", "Miller 1982. Protected title cache reference", formatter.format(ref, null, null));

        ref.setTitleCache("Miller 1982: Protected title cache reference", true);
        Assert.assertEquals("", "Miller 1982: Protected title cache reference", formatter.format(ref, null, null));
    }

    @Test
    public void testCreateLongCitation(){
        //TODO this is still preliminary, formatting may change in future.

        book1.setTitle("My book");
        book1.setAuthorship(bookTeam1);
        book1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1975));
        Assert.assertEquals("Unexpected title cache.", "Book Author 1975: My book", book1.getTitleCache());

        book1.setTitleCache(null, false);
        book1.setEdition("ed. 3");

        Assert.assertEquals("Unexpected title cache.", "Book Author 1975: My book, ed. 3", formatterLongCitation.format(book1, null));
        Assert.assertEquals("Unexpected title cache.", "Book Author 1975: My book, ed. 3", formatterLongCitation.format(book1, ""));
        Assert.assertEquals("Unexpected title cache.", "Book Author 1975: My book, ed. 3. p 55", formatterLongCitation.format(book1, "55"));

        //1 person
        Person person1 = Person.NewInstance("Pers.", "Person", "P.", "Percy");
        Team team = Team.NewInstance(person1);
        book1.setAuthorship(team);
        Assert.assertEquals("Unexpected title cache.", "Person, P. 1975: My book, ed. 3. p 55", formatterLongCitation.format(book1, "55"));
        team.setHasMoreMembers(true);
        Assert.assertEquals("Unexpected title cache.", "Person, P. & al. 1975: My book, ed. 3. p 55", formatterLongCitation.format(book1, "55"));
        team.setHasMoreMembers(false);

        //2 persons
        Person person2 = Person.NewInstance("Lers.", "Lerson", "L.", "Lercy");
        team.addTeamMember(person2);
        Assert.assertEquals("Unexpected title cache.", "Person, P. & Lerson, L. 1975: My book, ed. 3. p 55", formatterLongCitation.format(book1, "55"));
        team.setHasMoreMembers(true);
        Assert.assertEquals("Unexpected title cache.", "Person, P., Lerson, L. & al. 1975: My book, ed. 3. p 55", formatterLongCitation.format(book1, "55"));
        team.setHasMoreMembers(false);

        //3 persons
        Person person3 = Person.NewInstance("Gers.", "Gerson", "G.", "Gercy");
        team.addTeamMember(person3);
        Assert.assertEquals("Unexpected title cache.", "Person, P., Lerson, L. & Gerson, G. 1975: My book, ed. 3. p 55", formatterLongCitation.format(book1, "55"));
        team.setHasMoreMembers(true);
        Assert.assertEquals("Unexpected title cache.", "Person, P., Lerson, L., Gerson, G. & al. 1975: My book, ed. 3. p 55", formatterLongCitation.format(book1, "55"));
        team.setHasMoreMembers(false);  //in case we want to continue test
    }
}