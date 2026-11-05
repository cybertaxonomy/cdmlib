/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.permission.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.permission.Group;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesTransactionalSpringIntegrationTest;

/**
 * @author k.luther
 * @since 12.02.2014
 */
public class GroupServiceImplTest extends CdmServicesTransactionalSpringIntegrationTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private IGroupService groupService;

    @Autowired
    private IUserService userService;

    @Test
    public void testDeleteGroup(){
        GrantedAuthorityImpl testAuthority = GrantedAuthorityImpl.NewInstance("ADMIN_ALL_DELETE");
    	Group group = Group.NewInstance("TestGroup-delete");
    	group.addGrantedAuthority(testAuthority);
    	UUID groupUUID =  groupService.saveGroup(group);
    	User user1 = User.NewInstance("TestUser1-delete", "pwd");
    	UUID UserUUID =  userService.save(user1).getUuid();

		groupService.delete(group);

    	group = groupService.find(groupUUID);
    	assertNull(group);
    	user1 = userService.find(UserUUID);
    	assertNotNull(user1);

    }

    @Test
    public void testAddMemberToGroup(){
        GrantedAuthorityImpl testAuthority = GrantedAuthorityImpl.NewInstance("ADMIN_ALL_ADD_MEMBER");
        Group group = Group.NewInstance("TestGroup-addMember");
        group.addGrantedAuthority(testAuthority);
        UUID groupUUID =  groupService.saveGroup(group);
        User user1 = User.NewInstance("TestUser1-addMember", "pwd");
        UUID UserUUID =  userService.save(user1).getUuid();
        group.addMember(user1);
        List<Group> groups = new ArrayList<>();
        groups.add(group);
        groupService.merge(groups, true);
        commitAndStartNewTransaction();

        group = groupService.find(groupUUID);
        assertNotNull(group);
        assertNotNull(group.getMembers());
        user1 = userService.find(UserUUID);
        assertTrue(group.getMembers().iterator().next().equals(user1));

    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}

}
