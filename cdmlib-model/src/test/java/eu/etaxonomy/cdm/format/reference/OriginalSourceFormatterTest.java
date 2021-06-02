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

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

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
}