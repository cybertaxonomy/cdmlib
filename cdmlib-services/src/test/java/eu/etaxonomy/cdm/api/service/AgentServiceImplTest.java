/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.strategy.cache.agent.TeamDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.merge.MergeException;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
 * @since 2015-04-01
 */
public class AgentServiceImplTest extends CdmTransactionalIntegrationTest{

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AgentServiceImplTest.class);

    private static final UUID UUID_EHRENBERG = UUID.fromString("6363ae88-ec57-4b23-8235-6c86fbe59446");

    @SpringBeanByType
    private IAgentService service;

    @SpringBeanByType
    private INameService nameSerivce;

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testConvertPerson2Team(){

        //create data
        String fullAuthor = "Original author";
    	String nomTitle = "Abrev. aut.";
    	Person person = Person.NewTitledInstance(fullAuthor);
    	person.setNomenclaturalTitle(nomTitle);
    	Annotation annotation = Annotation.NewDefaultLanguageInstance("Meine annotation");
    	person.setContact(getContact());
    	TaxonName name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
    	name.setCombinationAuthorship(person);
    	person.addAnnotation(annotation);

    	nameSerivce.save(name);

    	Team team = null;
	    try {
	        UpdateResult result = service.convertPerson2Team(person);
	        team = (Team) result.getCdmEntity();
	    } catch (MergeException e) {
	        Assert.fail("No Merge exception should be thrown");
	    }
	    Assert.assertNotNull(team);
	    Assert.assertEquals("Title cache must be equal", fullAuthor, team.getTitleCache());
	    Assert.assertEquals("Nom. title must be equal", nomTitle, team.getNomenclaturalTitleCache());
	    Assert.assertTrue("Members must be empty", team.getTeamMembers().isEmpty());
	    Assert.assertEquals("Annotations should be moved", 1, team.getAnnotations().size());
	    Assert.assertNotNull("Contact must be copied too", team.getContact());
	    Assert.assertEquals("Team must be combination author now", team, name.getCombinationAuthorship());

	    //test un-protected titleCache
	    Person person2 = person.clone();
	    person2.setProtectedTitleCache(false);
	    Assert.assertEquals("Title cache must be equal", nomTitle, person2.getTitleCache());
	    service.save(person2);

	    try{
	        UpdateResult result = service.convertPerson2Team(person2);
	        team = (Team) result.getCdmEntity();
        } catch (MergeException e) {
            Assert.fail("No Merge exception should be thrown, but was: " + e.getMessage());
        }
	    Assert.assertEquals("Title cache must be equal", person2.getTitleCache(), team.getTitleCache());
        Assert.assertEquals("Nom. title must be equal", nomTitle, team.getNomenclaturalTitleCache());
        Assert.assertTrue("Nom. title must be protected", team.isProtectedNomenclaturalTitleCache());

        //test fully empty
        person2 = person2.clone();
        person2.setNomenclaturalTitle(null);  //now it is fully empty
        Assert.assertEquals("Title cache must be equal", "Person#0<"+person2.getUuid()+">", person2.getTitleCache());  //expected value may change when toString() implementation changes for Person class

        service.save(person2);
        try{
            UpdateResult result = service.convertPerson2Team(person2.getUuid());
            team = (Team) result.getCdmEntity();
        } catch (MergeException e) {
            Assert.fail("No Merge exception should be thrown, but was: " + e.getMessage());
        }
        Assert.assertEquals("If person was completely empty we don't expect the title cache to be taken from person.nomenclaturalTitle", TeamDefaultCacheStrategy.EMPTY_TEAM, team.getTitleCache());
        Assert.assertFalse("If person was completely empty we don't expect the title cache to be protected", team.isProtectedTitleCache());
        Assert.assertEquals("If person was completely empty we expect nom. title to be the empty team constant", TeamDefaultCacheStrategy.EMPTY_TEAM, team.getNomenclaturalTitleCache());
        Assert.assertEquals("If person was completely empty we expect collector title to be the empty team constant", TeamDefaultCacheStrategy.EMPTY_TEAM, team.getCollectorTitleCache());

        try{
            service.convertPerson2Team(person2.getUuid());
            Assert.fail("Non-existing person should throw an exception");
        } catch (MergeException e) {
            Assert.fail("No Merge exception should be thrown, but was: " + e.getMessage());
        }catch (IllegalArgumentException e) {
            //nothing to do
        }
    }

    private Contact getContact(){
    	URI uri = URI.create("a");
    	Contact contact = Contact.NewInstance("My street", "12345", null, null, null, "region", "a@bc.de", "030-445566", "030-12345", uri, Point.NewInstance(2d, 5d, null, null));
    	return contact;
    }


    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testConvertTeam2Person(){
    	String fullAuthor = "Original author";
    	String nomTitle = "Abrev. aut.";
    	Team team = Team.NewTitledInstance(fullAuthor, nomTitle);
    	Annotation annotation = Annotation.NewDefaultLanguageInstance("Meine annotation");
    	team.addAnnotation(annotation);
    	team.setContact(getContact());
    	TaxonName name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
    	name.setCombinationAuthorship(team);

    	service.save(team);
    	nameSerivce.save(name);

    	UpdateResult result = null;
    	Person person = null;
		try {
			result = service.convertTeam2Person(team);
			person = (Person)result.getCdmEntity();
		} catch (IllegalArgumentException e) {
			Assert.fail("No IllegalArgumentException should be thrown");
		} catch (MergeException e) {
			Assert.fail("No Merge exception should be thrown");
		}
    	Assert.assertNotNull(person);
    	Assert.assertEquals("Title cache must be equal", fullAuthor, person.getTitleCache());
    	Assert.assertEquals("Nom. title must be equal", nomTitle, person.getNomenclaturalTitleCache());
    	Assert.assertEquals("Annotations should be moved", 1, person.getAnnotations().size());
    	Assert.assertNotNull("Contact must be copied too", person.getContact());
    	Assert.assertEquals("person must be combination author now", person, name.getCombinationAuthorship());
    }


    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testConvertTeam2PersonWithMember(){
    	String fullAuthor = "Original author";
    	String nomTitle = "Abrev. aut.";
    	Team team = Team.NewTitledInstance(fullAuthor, nomTitle);
    	Annotation annotation = Annotation.NewDefaultLanguageInstance("Meine annotation");
    	team.addAnnotation(annotation);
    	Annotation annotation2 = Annotation.NewDefaultLanguageInstance("Meine annotation2");
    	team.addAnnotation(annotation2);
    	team.setContact(getContact());
    	TaxonName name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
    	name.setCombinationAuthorship(team);
    	Person member = Person.NewTitledInstance("Member person");
    	member.setNomenclaturalTitle("Memb. pers.");
    	Annotation annotation3 = Annotation.NewDefaultLanguageInstance("Meine annotation3");
    	member.addAnnotation(annotation3);
    	team.addTeamMember(member);

    	service.save(team);

    	nameSerivce.save(name);

    	Person person = null;
    	UpdateResult result = null;
		try {
		    result = service.convertTeam2Person(team);
		    person = (Person) result.getCdmEntity();
		} catch (IllegalArgumentException e) {
			Assert.fail("No IllegalArgumentException should be thrown");
		} catch (MergeException e) {
			Assert.fail("No Merge exception should be thrown");
		}
    	Assert.assertNotNull(person);
    	Assert.assertEquals("Convert result and 'member' must be equal'", member, person);
    	Assert.assertEquals("Title cache must be equal", "Member person", person.getTitleCache());
    	Assert.assertEquals("Nom. title must be equal", "Memb. pers.", person.getNomenclaturalTitleCache());
    	//FIXME should annotations be taken only from member ??
//    	Assert.assertEquals("Annotations should be moved", 1, person.getAnnotations().size());
    	String annotationText = person.getAnnotations().iterator().next().getText();
//    	Assert.assertEquals("The only annotation should be annotation 3", annotation3.getText(), annotationText);
    	//FIXME currently merge mode is still MERGE for user defined fields
//    	Assert.assertNull("Contact must not be copied", person.getContact());
    	Assert.assertEquals("person must be combination author now", person, name.getCombinationAuthorship());
    }


    @Test  //7874 //8030
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="AgentServiceImplTest.testUpdateTitleCache.xml")
    public final void testUpdateNomTitle() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        Field nomenclaturalTitleField = Person.class.getDeclaredField("nomenclaturalTitle");
        nomenclaturalTitleField.setAccessible(true);
        Field nomenclaturalTitleCacheField = TeamOrPersonBase.class.getDeclaredField("nomenclaturalTitleCache");
        nomenclaturalTitleCacheField.setAccessible(true);

        Person turland = (Person) service.load(UUID.fromString("a598ab3f-b33b-4b4b-b237-d616fcb6b5b1"));
        Person monro = (Person) service.load(UUID.fromString("e7206bc5-61ab-468e-a9f5-dec118b46b7f"));
        // TODO Add Assertion Person "Ehrenberg" must not be member of a team.
        Person ehrenberg = (Person) service.load(UUID_EHRENBERG);

        Team turland_monro_protected = (Team) service.load(UUID.fromString("5bff55de-f7cc-44d9-baac-908f52ad0cb8"));
        Team turland_monro = (Team) service.load(UUID.fromString("30ca93d6-b543-4bb9-b6ff-e9ededa65af7"));
        Team turland_monro_null = (Team) service.load(UUID.fromString("a4ca0d37-d78b-4bcc-875e-d4ea5a031089"));

        // Person has no flag for protecting the nomenclaturalTitle
        assertNull(nomenclaturalTitleField.get(turland));
        assertNull(nomenclaturalTitleField.get(ehrenberg));
        assertTrue(ehrenberg.isProtectedTitleCache());
        assertEquals("A.M. Monro", nomenclaturalTitleField.get(monro).toString());

        // Team has a flag for protectedNomenclaturalTitle flag
        assertEquals("Turland, Monro", nomenclaturalTitleCacheField.get(turland_monro_protected));
        assertTrue(turland_monro_protected.isProtectedNomenclaturalTitleCache());
        assertEquals("--to be updated--", nomenclaturalTitleCacheField.get(turland_monro).toString());
        assertFalse(turland_monro.isProtectedNomenclaturalTitleCache());
        assertNull(nomenclaturalTitleCacheField.get(turland_monro_null));
        assertFalse(turland_monro_null.isProtectedNomenclaturalTitleCache());

        service.updateCaches();

        turland_monro_protected = (Team) service.load(UUID.fromString("5bff55de-f7cc-44d9-baac-908f52ad0cb8"));
        turland_monro = (Team) service.load(UUID.fromString("30ca93d6-b543-4bb9-b6ff-e9ededa65af7"));
        ehrenberg = (Person)service.load(UUID_EHRENBERG);

        assertNull("Expecting nomenclaturalTitle to be still NULL", nomenclaturalTitleField.get(turland));
        assertEquals("Expecting nomenclaturalTitleCache to be set since it was NULL", "Turland, N.J.", nomenclaturalTitleCacheField.get(turland));
        assertNull("Expecting nomenclaturalTitle to be still NULL", nomenclaturalTitleField.get(ehrenberg));
        assertEquals("Expecting nomenclaturalTitleCache to be set since it was NULL", "Ehrenb.", nomenclaturalTitleCacheField.get(ehrenberg));
        assertEquals("Expecting titleChache to be unchaged since it was protecetd", "Ehrenb.", ehrenberg.getTitleCache());
        assertEquals("Expecting nomenclaturalTitleCache to be unchanged", "A.M. Monro", nomenclaturalTitleCacheField.get(monro).toString());

        assertEquals("Turland, Monro", nomenclaturalTitleCacheField.get(turland_monro_protected));
        assertEquals("Turland, N.J. & A.M. Monro", nomenclaturalTitleCacheField.get(turland_monro).toString());
        assertEquals("Expecting nomenclaturalTitle to be set since it was NULL", "Turland, N.J. & A.M. Monro", nomenclaturalTitleCacheField.get(turland_monro_null).toString());
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
