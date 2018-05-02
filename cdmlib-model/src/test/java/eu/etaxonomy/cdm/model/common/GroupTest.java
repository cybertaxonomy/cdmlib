/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author n.hoffmann
 * @since Mar 9, 2011
 * @version 1.0
 */
public class GroupTest {

	private Group group;
	private String groupName = "";
	private GrantedAuthority grantedAuthority;
	private User user;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		group = Group.NewInstance(groupName);
		grantedAuthority = GrantedAuthorityImpl.NewInstance(null);
		user = User.NewInstance(null, null);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Group#NewInstance(java.lang.String)}.
	 */
	@Test
	public final void testNewInstanceString() {
		assertNotNull("A new group should have been created.", group);
		assertEquals("Created object should be of type Group.", Group.class, group.getClass());
		assertEquals("Created group should have a name.", groupName, group.getName());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Group#getGrantedAuthorities()}.
	 */
	@Test
	public final void testGetGrantedAuthorities() {
		// not yet implemented
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Group#addGrantedAuthority(org.springframework.security.core.GrantedAuthority)}.
	 */
	@Test
	public final void testAddGrantedAuthority() {
		group.addGrantedAuthority(grantedAuthority);

		assertEquals("There should be exactly one GrantedAuthority.", 1, group.getGrantedAuthorities().size());
		assertEquals("The containing GrantedAuthority should match the one we filled it with.", grantedAuthority, group.getGrantedAuthorities().iterator().next());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Group#removeGrantedAuthority(org.springframework.security.core.GrantedAuthority)}.
	 */
	@Test
	public final void testRemoveGrantedAuthority() {
		group.addGrantedAuthority(grantedAuthority);

		assertEquals("There should be exactly one GrantedAuthority.", 1, group.getGrantedAuthorities().size());
		assertEquals("The containing GrantedAuthority should match the one we filled it with.", grantedAuthority, group.getGrantedAuthorities().iterator().next());

		group.removeGrantedAuthority(grantedAuthority);

		assertEquals("There should not contain GrantedAuthorities anymore.", 0, group.getGrantedAuthorities().size());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Group#setName(java.lang.String)}.
	 */
	@Test
	public final void testSetName() {
		String newName = "";
		group.setName(newName);
		assertEquals("Groups name should have changed.", newName, group.getName());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Group#getName()}.
	 */
	@Test
	public final void testGetName() {
		assertEquals("Group should have a name", groupName, group.getName());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Group#getMembers()}.
	 */
	@Test
	public final void testGetMembers() {
		// not implemented yet
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Group#addMember(eu.etaxonomy.cdm.model.common.User)}.
	 */
	@Test
	public final void testAddMember() {
		group.addMember(user);

		assertEquals("Group should have exactly one member.", 1, group.getMembers().size());
		assertEquals("The member should match the one we filled it with.", user, group.getMembers().iterator().next());

		assertEquals("The user should be in the group", group, user.getGroups().iterator().next());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Group#removeMember(eu.etaxonomy.cdm.model.common.User)}.
	 */
	@Test
	public final void testRemoveMember() {
		group.addMember(user);

		assertEquals("Group should have exactly one member.", 1, group.getMembers().size());
		assertEquals("The member should match the one we filled it with.", user, group.getMembers().iterator().next());

		group.removeMember(user);

		assertEquals("Group should not have members anymore.", 0, group.getMembers().size());

		assertEquals("The user should not be in any groups", 0, user.getGroups().size());
	}

}
