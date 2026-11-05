/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author a.mueller
 * @since 31.05.2010
 */
public class TeamTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private Team teamProtected;
	private Team teamWithMembers;
	private Person member1;
	private Person member2;
	private Person member3;
	private boolean eventWasFired = false;

	@BeforeEach
	public void setUp() throws Exception {
		teamProtected = Team.NewInstance();
		teamProtected.setTitleCache("Team1", true);
		teamProtected.setNomenclaturalTitleCache("NomTeam1", true);

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

	@Test
	public void testGetTitleCache() {
		assertEquals("Team1", teamProtected.getTitleCache(), "Title Cache of team 1 must be 'Team1'");
	}

	@Test
	public void testGetNomenclaturalTitleCache() {
		assertEquals("NomTeam1", teamProtected.getNomenclaturalTitleCache(), "Nom title Cache of team 1 must be 'NomTeam1'");
	}

	@Test
	public void testSetNomenclaturalTitleString() {
		eventWasFired = false;
		teamProtected.setNomenclaturalTitleCache("NomTeam1a", true);
		assertTrue(eventWasFired, "setNomenclaturalTitle(String, boolean) needs to fire property change event");
		assertEquals("NomTeam1a", teamProtected.getNomenclaturalTitleCache(), "Nom title Cache of team 1 must be 'NomTeam1a'");
	}

	@Test
	public void testSetNomenclaturalTitleStringBoolean() {
		eventWasFired = false;
		teamProtected.setNomenclaturalTitleCache("New Nom Title", false);
		assertTrue(eventWasFired, "setNomenclaturalTitle(String, boolean) needs to fire property change event");
		assertFalse(teamProtected.isProtectedNomenclaturalTitleCache(), "Protected title cache must be false for 'team protected'");
		//assertEquals("New Nom Title", teamProtected.getNomenclaturalTitle(), "Nom title Cache of team 1 must be 'New Nom Title'");
	}

	@Test
	public void testIsSetProtectedNomenclaturalTitleCache() {
		assertTrue(teamProtected.isProtectedNomenclaturalTitleCache(), "Protected title cache must be true for 'team protected'");
		teamProtected.setProtectedNomenclaturalTitleCache(false);
		assertFalse(teamProtected.isProtectedNomenclaturalTitleCache(), "Protected title cache must be false for 'team protected'");
	}

	@Test
	public void testAddGetRemoveTeamMemberPerson() {
		assertEquals(1, teamWithMembers.getTeamMembers().size(), "Number of team members should be 1");
		assertSame(member1, teamWithMembers.getTeamMembers().get(0), "Only team member should be 'Member1'");
		teamWithMembers.addTeamMember(member2);
		assertEquals(2, teamWithMembers.getTeamMembers().size(), "Number of team members should be 2");
		assertSame(member1, teamWithMembers.getTeamMembers().get(0), "First team member should be 'Member1'");
		assertSame(member2, teamWithMembers.getTeamMembers().get(1), "Second team member should be 'Member2'");
		teamWithMembers.addTeamMember(member3);
		assertEquals(3, teamWithMembers.getTeamMembers().size(), "Number of team members should be 3");
		assertSame(member1, teamWithMembers.getTeamMembers().get(0), "First team member should be 'Member1'");
		assertSame(member2, teamWithMembers.getTeamMembers().get(1), "Second team member should be 'Member2'");
		assertSame(member3, teamWithMembers.getTeamMembers().get(2), "Third team member should be 'Member3'");
		teamWithMembers.removeTeamMember(member2);
		assertEquals(2, teamWithMembers.getTeamMembers().size(), "Number of team members should be 2");
		assertSame(member1, teamWithMembers.getTeamMembers().get(0), "First team member should be 'Member1'");
		assertSame(member3, teamWithMembers.getTeamMembers().get(1), "Second team member should be 'Member3'");
		teamWithMembers.addTeamMember(member2, 1);
		assertEquals(3, teamWithMembers.getTeamMembers().size(), "Number of team members should be 3");
		assertSame(member1, teamWithMembers.getTeamMembers().get(0), "First team member should be 'Member1'");
		assertSame(member2, teamWithMembers.getTeamMembers().get(1), "Second team member should be 'Member2'");
		assertSame(member3, teamWithMembers.getTeamMembers().get(2), "Third team member should be 'Member3'");
		teamWithMembers.removeTeamMember(member2);
		teamWithMembers.addTeamMember(member2, 5);
		assertEquals(3, teamWithMembers.getTeamMembers().size(), "Number of team members should be 3");
		assertSame(member1, teamWithMembers.getTeamMembers().get(0), "First team member should be 'Member1'");
		assertSame(member3, teamWithMembers.getTeamMembers().get(1), "Second team member should be 'Member3'");
		assertSame(member2, teamWithMembers.getTeamMembers().get(2), "Third team member should be 'Member2'");
		teamWithMembers.addTeamMember(member2, 1);
		assertEquals(3, teamWithMembers.getTeamMembers().size(), "Number of team members should be 3");
		assertSame(member1, teamWithMembers.getTeamMembers().get(0), "First team member should be 'Member1'");
		assertSame(member2, teamWithMembers.getTeamMembers().get(1), "Second team member should be 'Member2'");
		assertSame(member3, teamWithMembers.getTeamMembers().get(2), "Third team member should be 'Member3'");
		teamProtected.addTeamMember(member1, 3);
		assertEquals(1, teamProtected.getTeamMembers().size(), "Number of team members should be 1");
	}

	@Test
	public void testAddRemoveTeamMemberPersonFireEvent() {
		eventWasFired = false;
		teamProtected.addTeamMember(member2);
		assertTrue(eventWasFired, "addTeamMember(Person) needs to fire property change event");
	}
}
