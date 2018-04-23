/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.agent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author a.mueller
 * @since 31.05.2010
 *
 */
public class TeamTest {
	public static final Logger logger = Logger.getLogger(TeamTest.class);

	private Team teamProtected;
	private Team teamWithMembers;
	private Person member1;
	private Person member2;
	private Person member3;
	private boolean eventWasFired = false;


	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		teamProtected = Team.NewInstance();
		teamProtected.setTitleCache("Team1", true);
		teamProtected.setNomenclaturalTitle("NomTeam1");

		PropertyChangeListener listener = new PropertyChangeListener(){
        	@Override
            public void propertyChange(PropertyChangeEvent e) {
        		eventWasFired = true;
        	}
    	};
    	teamProtected.addPropertyChangeListener(listener);

		teamWithMembers = Team.NewInstance();
		member1 = Person.NewTitledInstance("Member1");
		member2 = Person.NewTitledInstance("Member2");
		member3 = Person.NewTitledInstance("Member3");
		teamWithMembers.addTeamMember(member1);

	}

//********************* METHODS **********************

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.agent.Team#getTitleCache()}.
	 */
	@Test
	public void testGetTitleCache() {
		Assert.assertEquals("Title Cache of team 1 must be 'Team1'", "Team1", teamProtected.getTitleCache());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.agent.Team#getNomenclaturalTitle()}.
	 */
	@Test
	public void testGetNomenclaturalTitle() {
		Assert.assertEquals("Nom title Cache of team 1 must be 'NomTeam1'", "NomTeam1", teamProtected.getNomenclaturalTitle());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.agent.Team#setNomenclaturalTitle(java.lang.String)}.
	 */
	@Test
	public void testSetNomenclaturalTitleString() {
		eventWasFired = false;
		teamProtected.setNomenclaturalTitle("NomTeam1a");
		Assert.assertTrue("setNomenclaturalTitle(String, boolean) needs to fire property change event", eventWasFired);
		Assert.assertEquals("Nom title Cache of team 1 must be 'NomTeam1a'", "NomTeam1a", teamProtected.getNomenclaturalTitle());
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.agent.Team#setNomenclaturalTitle(java.lang.String, boolean)}.
	 */
	@Test
	public void testSetNomenclaturalTitleStringBoolean() {
		eventWasFired = false;
		teamProtected.setNomenclaturalTitle("New Nom Title", false);
		Assert.assertTrue("setNomenclaturalTitle(String, boolean) needs to fire property change event", eventWasFired);
		Assert.assertFalse("Protected title cache must be false for 'team protected'", teamProtected.isProtectedNomenclaturalTitleCache() );
		//Assert.assertEquals("Nom title Cache of team 1 must be 'New Nom Title'", "New Nom Title", teamProtected.getNomenclaturalTitle());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.agent.Team#isProtectedNomenclaturalTitleCache()}.
	 */
	@Test
	public void testIsSetProtectedNomenclaturalTitleCache() {
		Assert.assertTrue("Protected title cache must be true for 'team protected'", teamProtected.isProtectedNomenclaturalTitleCache() );
		teamProtected.setProtectedNomenclaturalTitleCache(false);
		Assert.assertFalse("Protected title cache must be false for 'team protected'", teamProtected.isProtectedNomenclaturalTitleCache() );
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.agent.Team#addTeamMember(eu.etaxonomy.cdm.model.agent.Person)}.
	 */
	@Test
	public void testAddGetRemoveTeamMemberPerson() {
		Assert.assertEquals("Number of team members should be 1", 1,teamWithMembers.getTeamMembers().size());
		Assert.assertSame("Only team member should be 'Member1'", member1, teamWithMembers.getTeamMembers().get(0));
		teamWithMembers.addTeamMember(member2);
		Assert.assertEquals("Number of team members should be 2", 2,teamWithMembers.getTeamMembers().size());
		Assert.assertSame("First team member should be 'Member1'", member1, teamWithMembers.getTeamMembers().get(0));
		Assert.assertSame("Second team member should be 'Member2'", member2, teamWithMembers.getTeamMembers().get(1));
		teamWithMembers.addTeamMember(member3);
		Assert.assertEquals("Number of team members should be 3", 3,teamWithMembers.getTeamMembers().size());
		Assert.assertSame("First team member should be 'Member1'", member1, teamWithMembers.getTeamMembers().get(0));
		Assert.assertSame("Second team member should be 'Member2'", member2, teamWithMembers.getTeamMembers().get(1));
		Assert.assertSame("Third team member should be 'Member3'", member3, teamWithMembers.getTeamMembers().get(2));
		teamWithMembers.removeTeamMember(member2);
		Assert.assertEquals("Number of team members should be 2", 2,teamWithMembers.getTeamMembers().size());
		Assert.assertSame("First team member should be 'Member1'", member1, teamWithMembers.getTeamMembers().get(0));
		Assert.assertSame("Second team member should be 'Member3'", member3, teamWithMembers.getTeamMembers().get(1));
		teamWithMembers.addTeamMember(member2, 1);
		Assert.assertEquals("Number of team members should be 3", 3,teamWithMembers.getTeamMembers().size());
		Assert.assertSame("First team member should be 'Member1'", member1, teamWithMembers.getTeamMembers().get(0));
		Assert.assertSame("Second team member should be 'Member2'", member2, teamWithMembers.getTeamMembers().get(1));
		Assert.assertSame("Third team member should be 'Member3'", member3, teamWithMembers.getTeamMembers().get(2));
		teamWithMembers.removeTeamMember(member2);
		teamWithMembers.addTeamMember(member2, 5);
		Assert.assertEquals("Number of team members should be 3", 3,teamWithMembers.getTeamMembers().size());
		Assert.assertSame("First team member should be 'Member1'", member1, teamWithMembers.getTeamMembers().get(0));
		Assert.assertSame("Second team member should be 'Member3'", member3, teamWithMembers.getTeamMembers().get(1));
		Assert.assertSame("Third team member should be 'Member2'", member2, teamWithMembers.getTeamMembers().get(2));
		teamWithMembers.addTeamMember(member2, 1);
		Assert.assertEquals("Number of team members should be 3", 3,teamWithMembers.getTeamMembers().size());
		Assert.assertSame("First team member should be 'Member1'", member1, teamWithMembers.getTeamMembers().get(0));
		Assert.assertSame("Second team member should be 'Member2'", member2, teamWithMembers.getTeamMembers().get(1));
		Assert.assertSame("Third team member should be 'Member3'", member3, teamWithMembers.getTeamMembers().get(2));
		teamProtected.addTeamMember(member1, 3);
		Assert.assertEquals("Number of team members should be 1", 1,teamProtected.getTeamMembers().size());
	}

	@Test
	public void testAddRemoveTeamMemberPersonFireEvent() {
		eventWasFired = false;
		teamProtected.addTeamMember(member2);
		Assert.assertTrue("addTeamMember(Person) needs to fire property change event", eventWasFired);
	}


}
