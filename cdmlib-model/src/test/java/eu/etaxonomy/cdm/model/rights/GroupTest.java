/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.rights;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import eu.etaxonomy.cdm.model.permission.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.permission.Group;
import eu.etaxonomy.cdm.model.permission.User;

/**
 * @author n.hoffmann
 * @since Mar 9, 2011
 */
public class GroupTest {

	private Group group;
	private String groupName = "";
	private GrantedAuthority grantedAuthority;
	private User user;

	@BeforeEach
	public void setUp() throws Exception {
		group = Group.NewInstance(groupName);
		grantedAuthority = GrantedAuthorityImpl.NewInstance(null);
		user = User.NewInstance(null, null);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.permission.Group#NewInstance(java.lang.String)}.
	 */
	@Test
	public final void testNewInstanceString() {
		assertNotNull(group, "A new group should have been created.");
		assertEquals(Group.class, group.getClass(), "Created object should be of type Group.");
		assertEquals(groupName, group.getName(), "Created group should have a name.");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.permission.Group#getGrantedAuthorities()}.
	 */
	@Test
	public final void testGetGrantedAuthorities() {
		// not yet implemented
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.permission.Group#addGrantedAuthority(org.springframework.security.core.GrantedAuthority)}.
	 */
	@Test
	public final void testAddGrantedAuthority() {
		group.addGrantedAuthority(grantedAuthority);

		assertEquals(1, group.getGrantedAuthorities().size(), "There should be exactly one GrantedAuthority.");
		assertEquals(grantedAuthority, group.getGrantedAuthorities().iterator().next(), "The containing GrantedAuthority should match the one we filled it with.");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.permission.Group#removeGrantedAuthority(org.springframework.security.core.GrantedAuthority)}.
	 */
	@Test
	public final void testRemoveGrantedAuthority() {
		group.addGrantedAuthority(grantedAuthority);

		assertEquals(1, group.getGrantedAuthorities().size(), "There should be exactly one GrantedAuthority.");
		assertEquals(grantedAuthority, group.getGrantedAuthorities().iterator().next(), "The containing GrantedAuthority should match the one we filled it with.");

		group.removeGrantedAuthority(grantedAuthority);

		assertEquals(0, group.getGrantedAuthorities().size(), "There should not contain GrantedAuthorities anymore.");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.permission.Group#setName(java.lang.String)}.
	 */
	@Test
	public final void testSetName() {
		String newName = "";
		group.setName(newName);
		assertEquals(newName, group.getName(), "Groups name should have changed.");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.permission.Group#getName()}.
	 */
	@Test
	public final void testGetName() {
		assertEquals(groupName, group.getName(), "Group should have a name");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.permission.Group#getMembers()}.
	 */
	@Test
	public final void testGetMembers() {
		// not implemented yet
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.permission.Group#addMember(eu.etaxonomy.cdm.model.permission.User)}.
	 */
	@Test
	public final void testAddMember() {
		group.addMember(user);

		assertEquals(1, group.getMembers().size(), "Group should have exactly one member.");
		assertEquals(user, group.getMembers().iterator().next(), "The member should match the one we filled it with.");

		assertEquals(group, user.getGroups().iterator().next(), "The user should be in the group");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.permission.Group#removeMember(eu.etaxonomy.cdm.model.permission.User)}.
	 */
	@Test
	public final void testRemoveMember() {
		group.addMember(user);

		assertEquals(1, group.getMembers().size(), "Group should have exactly one member.");
		assertEquals(user, group.getMembers().iterator().next(), "The member should match the one we filled it with.");

		group.removeMember(user);

		assertEquals(0, group.getMembers().size(), "Group should not have members anymore.");

		assertEquals(0, user.getGroups().size(), "The user should not be in any groups");
	}

}
