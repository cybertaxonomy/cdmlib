/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.filter.MatchMode;
import eu.etaxonomy.cdm.api.filter.Restriction;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmExpectedDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmTransactionalSpringIntegrationTest;

public class AgentDaoImplTest extends CdmTransactionalSpringIntegrationTest {

//    private static final String[] TABLE_NAMES = new String[] {"AGENTBASE", "AGENTBASE_AUD", "AGENTBASE_AGENTBASE", "AGENTBASE_AGENTBASE_AUD", "INSTITUTIONALMEMBERSHIP", "INSTITUTIONALMEMBERSHIP_AUD"};

    @Autowired
    private IAgentDao agentDao;

    private UUID uuid;
    private UUID personUuid;
    private AuditEvent previousAuditEvent;

    @BeforeEach
    public void setUp() {
        uuid = UUID.fromString("924fa059-1b83-45f8-bc3a-e754d2757364");
        personUuid = UUID.fromString("ed6ac546-8c6c-48c4-9b91-40b1157c05c6");
        previousAuditEvent = new AuditEvent();
        previousAuditEvent.setRevisionNumber(1000);
        previousAuditEvent.setUuid(UUID.fromString("a680fab4-365e-4765-b49e-768f2ee30cda"));
        AuditEventContextHolder.clearContext(); // By default we're in the current view (i.e. view == null)
    }

    @AfterEach
    public void tearDown() {
        AuditEventContextHolder.clearContext();
    }

    @Test
    @CdmDataSet
    public void testCountMembers() {
        Team team = (Team)agentDao.findByUuid(uuid);
        assert team != null : "team must exist";

        long numberOfMembers = agentDao.countMembers(team);
        assertEquals(5, numberOfMembers, "countMembers should return 5");
    }

    @Test
    @CdmDataSet
    public void testGetMembers() {
        Team team = (Team)agentDao.findByUuid(uuid);
        assert team != null : "team must exist";

        List<Person> members = agentDao.getMembers(team, null, null);
        assertNotNull(members, "getMembers should return a List");
        assertFalse(members.isEmpty(), "getMembers should not be empty");
        assertEquals(5, members.size(), "getMembers should return 5 person instances");
        //test order (3,4,1)
        Person firstMember = members.get(0);
        assertEquals(3, firstMember.getId(), "First member must be the one with agentbase id 3 (sortindex = 0)");
        Person lastMember = members.get(4);
        assertEquals(4, lastMember.getId(), "Last member must be the one with agentbase id 4 (sortindex = 4)");
        Person id1Member = members.get(2);
        assertEquals(1, id1Member.getId(), "The member with id = 1 must be the third (sortindex = 2)");
    }

    @Test
    @CdmDataSet("AgentDaoImplTest.testSave.xml")
    @CdmExpectedDataSet
    public void testSave() {
        Person person = Person.NewInstance();
        person.setGivenName("ben");
        agentDao.save(person);
        commit();
    }

    @Test
    @CdmDataSet
    @CdmExpectedDataSet
    public void testUpdate() {
        Person person = (Person)agentDao.findByUuid(personUuid);
        assert person != null : "person must exist";
        person.setGivenName("Benjamin");
        commit();
    }

    @Test
    @CdmDataSet("AgentDaoImplTest.testFind.xml")
    public void testFindInCurrentView() {
        Person person = (Person)agentDao.findByUuid(personUuid);
        assertEquals("Benjamin", person.getGivenName(), "The person's givenname should be \'Benjamin\' in the current view");
    }

    @Test
    @CdmDataSet("AgentDaoImplTest.testFind.xml")
    public void testFindInPreviousView() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        Person person = (Person)agentDao.findByUuid(personUuid);
        assertEquals(person.getGivenName(), "Ben", "The person's givenname should be \'Ben\' in the previous view");
    }

    @Test
    @CdmDataSet("AgentDaoImplTest.testFind.xml")
    @CdmExpectedDataSet
    public void testDelete() throws Exception {
        Person person = (Person)agentDao.findByUuid(personUuid);
        agentDao.delete(person);

        person =(Person)agentDao.findByUuid(personUuid);
        assertNull(person);
        commitAndStartNewTransaction(null);
    }

    @Test
    @CdmDataSet("AgentDaoImplTest.testExists.xml")
    public void testExists() {
        assertFalse(agentDao.exists(personUuid), "Person with the uuid " + personUuid.toString() +  "should not exist in the current view");
    }

    @Test
    @CdmDataSet("AgentDaoImplTest.testExists.xml")
    public void testExistsInPreviousView() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        assertTrue(agentDao.exists(uuid), "Person with the uuid " + personUuid.toString() +  "should exist in the previous view");
    }

    @Test
    @CdmDataSet("AgentDaoImplTest.testExists.xml")
    public void testCount() {
        assertEquals(8, agentDao.count(), "There should be eight agents in the current view");
    }

    @Test
    @CdmDataSet("AgentDaoImplTest.testExists.xml")
    public void testCountInPreviousView() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        assertEquals(9, agentDao.count(), "There should be nine agents in the previous view");
    }

    @Test
    @CdmDataSet("AgentDaoImplTest.testExists.xml")
    public void testCountAuditEvents() {
        assertEquals(11, agentDao.countAuditEvents(null, null, null, null), "There should be eleven AuditEvents");
    }

    @Test
    @CdmDataSet("AgentDaoImplTest.testExists.xml")
    public void testList() {
        List<AgentBase> result = agentDao.list(null,null);
        assertNotNull(result, "list() should return a list");
        assertEquals(result.size(), 8, "list() should return eight agents in the current view");
    }

    @Test
    @CdmDataSet("AgentDaoImplTest.testExists.xml")
    public void testListInPreviousView() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        List<AgentBase> result = agentDao.list(null, null);
        assertNotNull(result, "list() should return a list");
        assertEquals(result.size(), 9, "list() should return nine agents in the current view");
    }

    @Test
    @CdmDataSet("AgentDaoImplTest.testExists.xml")
    public void testSortingListInPreviousView() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        List<OrderHint> orderHints = new ArrayList<>();
        orderHints.add(new OrderHint("titleCache", SortOrder.ASCENDING));
        List<AgentBase> result = agentDao.list((Class)null, null, null, orderHints, null);
        assertNotNull(result, "list() should return a list");
        assertEquals(result.size(), 9, "list() should return nine agents in the current view");
        assertEquals(result.get(0).getTitleCache(), "B.R. Clark");
        assertEquals(result.get(8).getTitleCache(), "University of Oxford");
    }

    @Test
    @CdmDataSet("AgentDaoImplTest.testExists.xml")
    public void testCountPeople() {
        assertEquals(4, agentDao.count(Person.class), "There should be four agents in the current view");
    }

    @Test
    @CdmDataSet("AgentDaoImplTest.testExists.xml")
    public void testCountPeopleInPreviousView() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        assertEquals(5, agentDao.count(Person.class), "There should be five agents in the previous view");
    }

    @Test
    @CdmDataSet("AgentDaoImplTest.testExists.xml")
    public void testListPeople() {
        List<Person> result = agentDao.list(Person.class, null, null);
        assertNotNull(result, "list() should return a list");
        assertEquals(4, result.size(), "list() should return four agents in the current view");
    }

    @Test
    @CdmDataSet("AgentDaoImplTest.xml")
    public void testListPeopleFiltered() {

        List<Restriction<?>> restrictions = new ArrayList<>();
        List<AgentBase> result = agentDao.list(null, restrictions, (Integer)null, (Integer)null, null, null);
        assertNotNull(result, "list() should return a list");
        assertEquals(9, result.size(), "list() should return 9 AgentBase entities in the current view");

        List<Person>  personResults = agentDao.list(Person.class, restrictions, (Integer)null, (Integer)null, null, null);
        assertEquals(5, personResults.size(), "list() should return 5 Persons entities");

        Restriction<String> givenNameExact = new Restriction<>("givenName", MatchMode.EXACT);
        restrictions.add(givenNameExact);

        personResults = agentDao.list(Person.class, restrictions, (Integer)null, (Integer)null, null, null);
        assertEquals(5, personResults.size(), "list() empty value lists should be ignored");

        restrictions.clear();
        givenNameExact = new Restriction<>("givenName", MatchMode.EXACT, "Ben");
        restrictions.add(givenNameExact);
        personResults = agentDao.list(Person.class, restrictions, (Integer)null, (Integer)null, null, null);
        assertEquals(1, personResults.size(), "list() should return 1 AgentBase entity having the givenname 'Ben'");
    }

    @Test
    @CdmDataSet("AgentDaoImplTest.xml")
    public void testCountPeopleFiltered() {

        List<Restriction<?>> restrictions = new ArrayList<>();
        assertEquals(9, agentDao.count(null, restrictions), "count() should return 9 AgentBase entities");

        assertEquals(5, agentDao.count(Person.class, restrictions), "count() should return 5 Persons entities");

        Restriction<String> givenNameExact = new Restriction<>("givenName", MatchMode.EXACT);
        restrictions.add(givenNameExact);

        assertEquals(5, agentDao.count(Person.class, restrictions), "count() empty value lists should be ignored");

        restrictions.clear();
        givenNameExact = new Restriction<>("givenName", MatchMode.EXACT, "Ben");
        restrictions.add(givenNameExact);
        assertEquals(1, agentDao.count(Person.class, restrictions), "count() should return 1 Persons entity having the given name 'Ben'");
    }

    @Test
    @CdmDataSet("AgentDaoImplTest.testExists.xml")
    public void testListPeopleInPreviousView() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        List<Person> result = agentDao.list(Person.class, null, null);
        assertNotNull(result, "list() should return a list");
        assertEquals(result.size(), 5, "list() should return five agents in the current view");
    }

    @Test
    @CdmExpectedDataSet
    public void testPersonTitleCache(){
        Person person = Person.NewTitledInstance("");
        person.setNomenclaturalTitle("Abbrev. tit.");

        person.setUuid(UUID.fromString("ee7c3b99-bf23-43dc-a64b-d37d3c6ea414"));
        agentDao.save(person);
        commitAndStartNewTransaction(null);
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
