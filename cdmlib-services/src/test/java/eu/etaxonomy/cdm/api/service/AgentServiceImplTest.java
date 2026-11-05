/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.strategy.cache.agent.TeamDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.merge.MergeException;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesTransactionalSpringIntegrationTest;

/**
 * @author a.mueller
 * @since 2015-04-01
 */
public class AgentServiceImplTest extends CdmServicesTransactionalSpringIntegrationTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private static final UUID UUID_EHRENBERG = UUID.fromString("6363ae88-ec57-4b23-8235-6c86fbe59446");

    @Autowired
    private IAgentService service;

    @Autowired
    private INameService nameSerivce;

    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
    public void testConvertPerson2Team() {

        //create data
        String fullAuthor = "Original author";
        String nomTitle = "Abrev. aut.";
        Person person = Person.NewTitledInstance(fullAuthor);
        person.setNomenclaturalTitle(nomTitle);
        Annotation annotation = Annotation.NewDefaultLanguageInstance("Meine annotation");
        TaxonName name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        person.addAnnotation(annotation);
        service.save(person);
        name.setCombinationAuthorship(person);

        nameSerivce.save(name);

        Team team = null;
        try {
            UpdateResult result = service.convertPerson2Team(person.getUuid());
            team = (Team) result.getCdmEntity();
        } catch (MergeException e) {
            fail("No Merge exception should be thrown");
        }
        assertNotNull(team);
        assertEquals(fullAuthor, team.getTitleCache(), "Title cache must be equal");
        assertEquals(nomTitle, team.getNomenclaturalTitleCache(), "Nom. title must be equal");
        assertTrue(team.getTeamMembers().isEmpty(), "Members must be empty");
        assertEquals(1, team.getAnnotations().size(), "Annotations should be moved");
        assertEquals(team, name.getCombinationAuthorship(), "Team must be combination author now");

        //test un-protected titleCache
        Person person2 = person.clone();
        person2.setProtectedTitleCache(false);
        assertEquals(nomTitle, person2.getTitleCache(), "Title cache must be equal");
        service.save(person2);

        try {
            UpdateResult result = service.convertPerson2Team(person2.getUuid());
            team = (Team) result.getCdmEntity();
        } catch (MergeException e) {
            fail("No Merge exception should be thrown, but was: " + e.getMessage());
        }
        assertEquals(person2.getTitleCache(), team.getTitleCache(), "Title cache must be equal");
        assertEquals(nomTitle, team.getNomenclaturalTitleCache(), "Nom. title must be equal");
        assertTrue(team.isProtectedNomenclaturalTitleCache(), "Nom. title must be protected");

        //test fully empty
        person2 = person2.clone();
        person2.setNomenclaturalTitle(null);  //now it is fully empty
        assertEquals("Person#0<" + person2.getUuid() + ">", person2.getTitleCache(), "Title cache must be equal");

        service.save(person2);
        try {
            UpdateResult result = service.convertPerson2Team(person2.getUuid());
            team = (Team) result.getCdmEntity();
        } catch (MergeException e) {
            fail("No Merge exception should be thrown, but was: " + e.getMessage());
        }
        assertEquals(TeamDefaultCacheStrategy.EMPTY_TEAM, team.getTitleCache(), "If person was completely empty we don't expect the title cache to be taken from person.nomenclaturalTitle");
        assertFalse(team.isProtectedTitleCache(), "If person was completely empty we don't expect the title cache to be protected");
        assertEquals(TeamDefaultCacheStrategy.EMPTY_TEAM, team.getNomenclaturalTitleCache(), "If person was completely empty we expect nom. title to be the empty team constant");
        assertEquals(TeamDefaultCacheStrategy.EMPTY_TEAM, team.getCollectorTitleCache(), "If person was completely empty we expect collector title to be the empty team constant");

        try {
            service.convertPerson2Team(person2.getUuid());
            fail("Non-existing person should throw an exception");
        } catch (MergeException e) {
            fail("No Merge exception should be thrown, but was: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            //nothing to do
        }
    }

    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
    public void testConvertTeam2Person() {

        String fullAuthor = "Original author";
        String nomTitle = "Abrev. aut.";
        String collectorTitle = "Coll. author";

        Team team = Team.NewTitledInstance(fullAuthor, nomTitle, collectorTitle);
        Annotation annotation = Annotation.NewDefaultLanguageInstance("Meine annotation");
        team.addAnnotation(annotation);
        TaxonName name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        name.setCombinationAuthorship(team);

        service.save(team);
        nameSerivce.save(name);

        UpdateResult result = null;
        Person person = null;
        try {
            result = service.convertTeam2Person(team);
            person = (Person) result.getCdmEntity();
        } catch (IllegalArgumentException e) {
            fail("No IllegalArgumentException should be thrown");
        } catch (MergeException e) {
            fail("No Merge exception should be thrown");
        }
        assertNotNull(person);
        assertEquals(fullAuthor, person.getTitleCache(), "Title cache must be equal");
        assertEquals(nomTitle, person.getNomenclaturalTitle(), "Nom. title must be equal to protected team nom. title cache");
        assertEquals(collectorTitle, person.getCollectorTitle(), "Collector title must be equal to protected team collector title cache");

        assertEquals(1, person.getAnnotations().size(), "Annotations should be moved");
        assertEquals(person, name.getCombinationAuthorship(), "person must be combination author now");

        //no protected nom. and collector cache
        team = Team.NewTitledInstance(fullAuthor, nomTitle, collectorTitle);
        team.setProtectedNomenclaturalTitleCache(false);
        team.setProtectedCollectorTitleCache(false);
        try {
            result = service.convertTeam2Person(team);
            person = (Person) result.getCdmEntity();
        } catch (Exception e) {
            fail("No Exception should be thrown");
        }
        assertNotNull(person);
        assertEquals(fullAuthor, person.getTitleCache(), "Title cache must be equal");
        assertNull(person.getNomenclaturalTitle(), "Nom. title must be empty as team nom. title cache is not protected");
        assertNull(person.getCollectorTitle(), "Collector title must be empty as team collector title cache is not protected");

    }

    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
    public void testConvertTeam2PersonWithMember() {

        String fullAuthor = "Original author";
        String nomTitle = "Abrev. aut.";
        Team team = Team.NewTitledInstance(fullAuthor, nomTitle);
        Annotation annotation = Annotation.NewDefaultLanguageInstance("Meine annotation");
        team.addAnnotation(annotation);
        Annotation annotation2 = Annotation.NewDefaultLanguageInstance("Meine annotation2");
        team.addAnnotation(annotation2);
        TaxonName name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        name.setCombinationAuthorship(team);
        Person member = Person.NewTitledInstance("Member person");
        member.setNomenclaturalTitle("Memb. pers.");
        Annotation annotation3 = Annotation.NewDefaultLanguageInstance("Meine annotation3");
        member.addAnnotation(annotation3);
        team.addTeamMember(member);
        service.save(team, member);

        nameSerivce.save(name);

        Person person = null;
        UpdateResult result = null;
        try {
            result = service.convertTeam2Person(team);
            person = (Person) result.getCdmEntity();
        } catch (IllegalArgumentException e) {
            fail("No IllegalArgumentException should be thrown");
        } catch (MergeException e) {
            fail("No Merge exception should be thrown");
        }
        assertNotNull(person);
        assertEquals(member, person, "Convert result and 'member' must be equal'");
        assertEquals("Member person", person.getTitleCache(), "Title cache must be equal");
        assertEquals("Memb. pers.", person.getNomenclaturalTitleCache(), "Nom. title must be equal");
        //FIXME should annotations be taken only from member ??
//      assertEquals(1, person.getAnnotations().size(), "Annotations should be moved");
        String annotationText = person.getAnnotations().iterator().next().getText();
//      assertEquals(annotation3.getText(), annotationText, "The only annotation should be annotation 3");
        //FIXME currently merge mode is still MERGE for user defined fields
//      assertNull(person.getContact(), "Contact must not be copied");
        assertEquals(person, name.getCombinationAuthorship(), "person must be combination author now");
    }

    @Test  //7874 //8030
    @CdmDataSet(cleanSweep = true, value = "AgentServiceImplTest.testUpdateTitleCache.xml")
    public final void testUpdateNomTitle() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        Field nomenclaturalTitleField = Person.class.getDeclaredField("nomenclaturalTitle");
        nomenclaturalTitleField.setAccessible(true);
        Field nomenclaturalTitleCacheField = TeamOrPersonBase.class.getDeclaredField("nomenclaturalTitleCache");
        nomenclaturalTitleCacheField.setAccessible(true);

        Person turland = (Person) service.load(UUID.fromString("a598ab3f-b33b-4b4b-b237-d616fcb6b5b1"));
        Person monro = (Person) service.load(UUID.fromString("e7206bc5-61ab-468e-a9f5-dec118b46b7f"));
        // TODO Add Assertion Person "Ehrenberg" must not be member of a team.
        Person ehrenberg = (Person) service.load(UUID_EHRENBERG);

        Team turland_monro_protected = CdmBase.deproxy(service.loadWithoutInitializing(3), Team.class);
        Team turland_monro = CdmBase.deproxy(service.loadWithoutInitializing(4), Team.class);
        Team turland_monro_null = CdmBase.deproxy(service.loadWithoutInitializing(5), Team.class);

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

        turland_monro_protected = CdmBase.deproxy(service.load(UUID.fromString("5bff55de-f7cc-44d9-baac-908f52ad0cb8")), Team.class);
        turland_monro = CdmBase.deproxy(service.load(UUID.fromString("30ca93d6-b543-4bb9-b6ff-e9ededa65af7")), Team.class);
        ehrenberg = (Person) service.load(UUID_EHRENBERG);

        assertNull(nomenclaturalTitleField.get(turland), "Expecting nomenclaturalTitle to be still NULL");
        assertEquals("Turland, N.J.", nomenclaturalTitleCacheField.get(turland), "Expecting nomenclaturalTitleCache to be set since it was NULL");
        assertNull(nomenclaturalTitleField.get(ehrenberg), "Expecting nomenclaturalTitle to be still NULL");
        assertEquals("Ehrenb.", nomenclaturalTitleCacheField.get(ehrenberg), "Expecting nomenclaturalTitleCache to be set since it was NULL");
        assertEquals("Ehrenb.", ehrenberg.getTitleCache(), "Expecting titleChache to be unchaged since it was protecetd");
        assertEquals("A.M. Monro", nomenclaturalTitleCacheField.get(monro).toString(), "Expecting nomenclaturalTitleCache to be unchanged");

        assertEquals("Turland, Monro", nomenclaturalTitleCacheField.get(turland_monro_protected));
        assertEquals("Turland, N.J. & A.M. Monro", nomenclaturalTitleCacheField.get(turland_monro).toString());
        assertEquals("Turland, N.J. & A.M. Monro", nomenclaturalTitleCacheField.get(turland_monro_null).toString(), "Expecting nomenclaturalTitle to be set since it was NULL");
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
