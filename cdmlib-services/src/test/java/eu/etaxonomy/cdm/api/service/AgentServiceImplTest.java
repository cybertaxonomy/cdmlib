// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.io.FileNotFoundException;
import java.net.URI;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.strategy.merge.MergeException;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @created 2015-04-01
 */
public class AgentServiceImplTest extends CdmTransactionalIntegrationTest{

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AgentServiceImplTest.class);

    @SpringBeanByType
    private IAgentService service;

    @SpringBeanByType
    private INameService nameSerivce;


    @Test
    public void testConvertPerson2Team(){
    	String fullAuthor = "Original author";
    	String nomTitle = "Abrev. aut.";
    	Person person = Person.NewTitledInstance(fullAuthor);
    	person.setNomenclaturalTitle(nomTitle);
    	Annotation annotation = Annotation.NewDefaultLanguageInstance("Meine annotation");
    	person.setContact(getContact());
    	BotanicalName name = BotanicalName.NewInstance(Rank.SPECIES());
    	name.setCombinationAuthorship(person);
    	person.addAnnotation(annotation);

    	service.save(person);
    	nameSerivce.save(name);

    	Team team = null;
		try {
			team = service.convertPerson2Team(person);
		} catch (MergeException e) {
			Assert.fail("No Merge exception should be thrown");
		}
    	Assert.assertNotNull(team);
    	//Assert.assertEquals("Title cache must be equal", fullAuthor, team.getTitleCache());
    	//Assert.assertEquals("Nom. title must be equal", nomTitle, team.getNomenclaturalTitle());
    	Assert.assertEquals("Annotations should be moved", 1, team.getAnnotations().size());
       	Assert.assertNotNull("Contact must be copied too", team.getContact());
    	Assert.assertEquals("Team must be combination author now", team, name.getCombinationAuthorship());

    }

    private Contact getContact(){
    	URI uri = URI.create("a");
    	Contact contact = Contact.NewInstance("My street", "12345", null, null, null, "region", "a@bc.de", "030-445566", "030-12345", uri, Point.NewInstance(2d, 5d, null, null));
    	return contact;
    }


    @Test
    public void testConvertTeam2Person(){
    	String fullAuthor = "Original author";
    	String nomTitle = "Abrev. aut.";
    	Team team = Team.NewTitledInstance(fullAuthor, nomTitle);
    	Annotation annotation = Annotation.NewDefaultLanguageInstance("Meine annotation");
    	team.addAnnotation(annotation);
    	team.setContact(getContact());
    	BotanicalName name = BotanicalName.NewInstance(Rank.SPECIES());
    	name.setCombinationAuthorship(team);

    	service.save(team);
    	nameSerivce.save(name);

    	Person person = null;
		try {
			person = service.convertTeam2Person(team);
		} catch (IllegalArgumentException e) {
			Assert.fail("No IllegalArgumentException should be thrown");
		} catch (MergeException e) {
			Assert.fail("No Merge exception should be thrown");
		}
    	Assert.assertNotNull(person);
    	Assert.assertEquals("Title cache must be equal", fullAuthor, person.getTitleCache());
    	Assert.assertEquals("Nom. title must be equal", nomTitle, person.getNomenclaturalTitle());
    	Assert.assertEquals("Annotations should be moved", 1, person.getAnnotations().size());
    	Assert.assertNotNull("Contact must be copied too", person.getContact());
    	Assert.assertEquals("person must be combination author now", person, name.getCombinationAuthorship());
    }


    @Test
    public void testConvertTeam2PersonWithMember(){
    	String fullAuthor = "Original author";
    	String nomTitle = "Abrev. aut.";
    	Team team = Team.NewTitledInstance(fullAuthor, nomTitle);
    	Annotation annotation = Annotation.NewDefaultLanguageInstance("Meine annotation");
    	team.addAnnotation(annotation);
    	Annotation annotation2 = Annotation.NewDefaultLanguageInstance("Meine annotation2");
    	team.addAnnotation(annotation2);
    	team.setContact(getContact());
    	BotanicalName name = BotanicalName.NewInstance(Rank.SPECIES());
    	name.setCombinationAuthorship(team);
    	Person member = Person.NewTitledInstance("Member person");
    	member.setNomenclaturalTitle("Memb. pers.");
    	Annotation annotation3 = Annotation.NewDefaultLanguageInstance("Meine annotation3");
    	member.addAnnotation(annotation3);
    	team.addTeamMember(member);

    	service.save(team);

    	nameSerivce.save(name);

    	Person person = null;
		try {
			person = service.convertTeam2Person(team);
		} catch (IllegalArgumentException e) {
			Assert.fail("No IllegalArgumentException should be thrown");
		} catch (MergeException e) {
			Assert.fail("No Merge exception should be thrown");
		}
    	Assert.assertNotNull(person);
    	Assert.assertEquals("Convert result and 'member' must be equal'", member, person);
    	Assert.assertEquals("Title cache must be equal", "Member person", person.getTitleCache());
    	Assert.assertEquals("Nom. title must be equal", "Memb. pers.", person.getNomenclaturalTitle());
    	//FIXME should annotations be taken only from member ??
//    	Assert.assertEquals("Annotations should be moved", 1, person.getAnnotations().size());
    	String annotationText = person.getAnnotations().iterator().next().getText();
//    	Assert.assertEquals("The only annotation should be annotation 3", annotation3.getText(), annotationText);
    	//FIXME currently merge mode is still MERGE for user defined fields
//    	Assert.assertNull("Contact must not be copied", person.getContact());
    	Assert.assertEquals("person must be combination author now", person, name.getCombinationAuthorship());
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
