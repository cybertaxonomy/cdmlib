/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;


public class AgentDaoImplTest extends CdmTransactionalIntegrationTest {

//    private static final String[] TABLE_NAMES = new String[] {"AGENTBASE", "AGENTBASE_AUD", "AGENTBASE_AGENTBASE", "AGENTBASE_AGENTBASE_AUD", "INSTITUTIONALMEMBERSHIP", "INSTITUTIONALMEMBERSHIP_AUD"};


    @SpringBeanByType
    private IAgentDao agentDao;

    @SpringBeanByType
	IReferenceDao referenceDao;

    private UUID uuid;
    private UUID personUuid;
    private AuditEvent previousAuditEvent;

    @Before
    public void setUp() {
        uuid = UUID.fromString("924fa059-1b83-45f8-bc3a-e754d2757364");
        personUuid = UUID.fromString("ed6ac546-8c6c-48c4-9b91-40b1157c05c6");
        previousAuditEvent = new AuditEvent();
        previousAuditEvent.setRevisionNumber(1000);
        previousAuditEvent.setUuid(UUID.fromString("a680fab4-365e-4765-b49e-768f2ee30cda"));
        AuditEventContextHolder.clearContext(); // By default we're in the current view (i.e. view == null)
    }

    @After
    public void tearDown() {
        AuditEventContextHolder.clearContext();
    }

    @Test
    @DataSet
    public void testCountMembers() {
        Team team = (Team)agentDao.findByUuid(uuid);
        assert team != null : "team must exist";

        int numberOfMembers = agentDao.countMembers(team);
        assertEquals("countMembers should return 5",5,numberOfMembers);
    }

    @Test
    @DataSet
    public void testGetMembers() {
        Team team = (Team)agentDao.findByUuid(uuid);
        assert team != null : "team must exist";

        List<Person> members = agentDao.getMembers(team, null, null);
        assertNotNull("getMembers should return a List", members);
        assertFalse("getMembers should not be empty",members.isEmpty());
        assertEquals("getMembers should return 5 person instances",5,members.size());
        //test order (3,4,1)
        Person firstMember = members.get(0);
        assertEquals("First member must be the one with agentbase id 3 (sortindex = 0)", 3, firstMember.getId());
        Person lastMember = members.get(4);
        assertEquals("Last member must be the one with agentbase id 4 (sortindex = 4)", 4, lastMember.getId());
        Person id1Member = members.get(2);
        assertEquals("The member with id = 1 must be the third (sortindex = 2)", 1, id1Member.getId());
    }

    @Test
    @DataSet
    public void testCountInstitutionalMemberships() {
        Person person = (Person)agentDao.findByUuid(personUuid);
        assert person != null : "person must exist";

        int numberOfInstitutionalMemberships = agentDao.countInstitutionalMemberships(person);
        assertEquals("countInstitutionalMemberships should return 3",3,numberOfInstitutionalMemberships);
    }

    @Test
    @DataSet
    public void testGetInstitutionalMemberships() {
        Person person = (Person)agentDao.findByUuid(personUuid);
        assert person != null : "person must exist";

        List<InstitutionalMembership> memberships = agentDao.getInstitutionalMemberships(person, null, null);
        assertNotNull("getInstitutionalMemberships should return a List", memberships);
        assertFalse("getInstitutionalMemberships should not be empty",memberships.isEmpty());
        assertEquals("getInstitutionalMemberships should return 3 institutional membership instances",3,memberships.size());
    }

    @Test
    @DataSet("AgentDaoImplTest.testSave.xml")
    @ExpectedDataSet
    public void testSave() {
        Person person = Person.NewInstance();
        person.setFirstname("ben");
        agentDao.save(person);
        commit();
    }

    @Test
    @DataSet
    @ExpectedDataSet
    public void testUpdate() {
        Person person = (Person)agentDao.findByUuid(personUuid);
        assert person != null : "person must exist";
        person.setFirstname("Benjamin");
        agentDao.update(person);
//        commitAndStartNewTransaction(new String[]{"AGENTBASE_AUD","AGENTBASE"});
        commit();
    }

    @Test
    @DataSet("AgentDaoImplTest.testFind.xml")
    public void testFindInCurrentView() {
        Person person = (Person)agentDao.findByUuid(personUuid);
        Assert.assertEquals("The person's firstname should be \'Benjamin\' in the current view", "Benjamin", person.getFirstname());
    }

    @Test
    @DataSet("AgentDaoImplTest.testFind.xml")
    public void testFindInPreviousView() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        Person person = (Person)agentDao.findByUuid(personUuid);
        Assert.assertEquals("The person's firstname should be \'Ben\' in the previous view",person.getFirstname(),"Ben");
    }

    @Test
    @DataSet("AgentDaoImplTest.testFind.xml")
    @ExpectedDataSet
    public void testDelete() throws Exception {
        Person person = (Person)agentDao.findByUuid(personUuid);
        agentDao.delete(person);


        person =(Person)agentDao.findByUuid(personUuid);
        Assert.assertNull(person);
        commitAndStartNewTransaction(null);
    }

    @Test
    @DataSet("AgentDaoImplTest.testExists.xml")
    public void testExists() {
        Assert.assertFalse("Person with the uuid " + personUuid.toString() +  "should not exist in the current view",agentDao.exists(personUuid));
    }

    @Test
    @DataSet("AgentDaoImplTest.testExists.xml")
    public void testExistsInPreviousView() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        Assert.assertTrue("Person with the uuid " + personUuid.toString() +  "should exist in the previous view",agentDao.exists(uuid));
    }

    @Test
    @DataSet("AgentDaoImplTest.testExists.xml")
    public void testCount() {
        Assert.assertEquals("There should be eight agents in the current view",8, agentDao.count());
    }

    @Test
    @DataSet("AgentDaoImplTest.testExists.xml")
    public void testCountInPreviousView() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        Assert.assertEquals("There should be nine agents in the previous view",9, agentDao.count());
    }

    @Test
    @DataSet("AgentDaoImplTest.testExists.xml")
    public void testCountAuditEvents() {
        Assert.assertEquals("There should be eleven AuditEvents",11, agentDao.countAuditEvents(null, null, null, null));
    }

    @Test
    @DataSet("AgentDaoImplTest.testExists.xml")
    public void testList() {
        List<AgentBase> result = agentDao.list(null,null);
        Assert.assertNotNull("list() should return a list",result);
        Assert.assertEquals("list() should return eight agents in the current view", result.size(),8);
    }

    @Test
    @DataSet("AgentDaoImplTest.testExists.xml")
    public void testListInPreviousView() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        List<AgentBase> result = agentDao.list(null, null);
        Assert.assertNotNull("list() should return a list",result);
        Assert.assertEquals("list() should return nine agents in the current view",result.size(),9);
    }

    @Test
    @DataSet("AgentDaoImplTest.testExists.xml")
    public void testSortingListInPreviousView() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        List<OrderHint> orderHints = new ArrayList<OrderHint>();
        orderHints.add(new OrderHint("titleCache", SortOrder.ASCENDING));
        List<AgentBase> result = agentDao.list((Class)null, null, null, orderHints, null);
        Assert.assertNotNull("list() should return a list",result);
        Assert.assertEquals("list() should return nine agents in the current view",result.size(),9);
        Assert.assertEquals(result.get(0).getTitleCache(), "B.R. Clark");
        Assert.assertEquals(result.get(8).getTitleCache(), "University of Oxford");
    }

    @Test
    @DataSet("AgentDaoImplTest.testExists.xml")
    public void testCountPeople() {
        Assert.assertEquals("There should be four agents in the current view",4, agentDao.count(Person.class));
    }

    @Test
    @DataSet("AgentDaoImplTest.testExists.xml")
    public void testCountPeopleInPreviousView() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        Assert.assertEquals("There should be five agents in the previous view",5, agentDao.count(Person.class));
    }

    @Test
    @DataSet("AgentDaoImplTest.testExists.xml")
    public void testListPeople() {
        List<Person> result = agentDao.list(Person.class,null,null);
        Assert.assertNotNull("list() should return a list",result);
        Assert.assertEquals("list() should return four agents in the current view", result.size(),4);
    }

    @Test
    @DataSet("AgentDaoImplTest.testExists.xml")
    public void testListPeopleInPreviousView() {
        AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
        List<Person> result = agentDao.list(Person.class,null, null);
        Assert.assertNotNull("list() should return a list",result);
        Assert.assertEquals("list() should return five agents in the current view",result.size(),5);
    }

    @Test
    @ExpectedDataSet
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
