package eu.etaxonomy.cdm.persistence.dao.hibernate.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet
public class AgentDaoImplTest extends CdmIntegrationTest {
	
	@SpringBeanByType
	private IAgentDao agentDao;
	
	private UUID uuid;
	private UUID personUuid;
	
	@Before
	public void setUp() {
		uuid = UUID.fromString("924fa059-1b83-45f8-bc3a-e754d2757364");
		personUuid = UUID.fromString("ed6ac546-8c6c-48c4-9b91-40b1157c05c6");
	}
	
	@Test
	public void testCountMembers() {
		Team team = (Team)agentDao.findByUuid(uuid);
		assert team != null : "team must exist";
		
		int numberOfMembers = agentDao.countMembers(team);
		assertEquals("countMembers should return 5",5,numberOfMembers);
	}
	
	@Test
	public void testGetMembers() {
		Team team = (Team)agentDao.findByUuid(uuid);
		assert team != null : "team must exist";
		
		List<Person> members = agentDao.getMembers(team, null, null);
		assertNotNull("getMembers should return a List", members);
		assertFalse("getMembers should not be empty",members.isEmpty());
		assertEquals("getMembers should return 5 person instances",5,members.size());
	}
	
	@Test
	public void testCountInstitutionalMemberships() {
		Person person = (Person)agentDao.findByUuid(personUuid);
		assert person != null : "person must exist";
		
		int numberOfInstitutionalMemberships = agentDao.countInstitutionalMemberships(person);
		assertEquals("countInstitutionalMemberships should return 3",3,numberOfInstitutionalMemberships);
	}
	
	@Test
	public void testGetInstitutionalMemberships() {
		Person person = (Person)agentDao.findByUuid(personUuid);
		assert person != null : "person must exist";
		
		List<InstitutionalMembership> memberships = agentDao.getInstitutionalMemberships(person, null, null);
		assertNotNull("getInstitutionalMemberships should return a List", memberships);
		assertFalse("getInstitutionalMemberships should not be empty",memberships.isEmpty());
		assertEquals("getInstitutionalMemberships should return 3 institutional membership instances",3,memberships.size());
	}
}
