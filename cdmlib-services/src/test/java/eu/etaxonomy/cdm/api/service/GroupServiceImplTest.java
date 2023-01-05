package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.permission.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.permission.Group;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class GroupServiceImplTest extends CdmTransactionalIntegrationTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @SpringBeanByType
    private IGroupService groupService;

    @SpringBeanByType
    private IUserService userService;

    @Test
    public void testDeleteGroup(){
        String admin_all = "ADMIN_ALL";
    	GrantedAuthorityImpl testAuthority = GrantedAuthorityImpl.NewInstance(admin_all);
    	Group group = Group.NewInstance("TestGroup");
    	group.addGrantedAuthority(testAuthority);
    	UUID groupUUID =  groupService.saveGroup(group);
    	User user1 = User.NewInstance("TestUser1", "pwd");
    	UUID UserUUID =  userService.save(user1).getUuid();

		groupService.delete(group);

    	group = groupService.find(groupUUID);
    	assertNull(group);
    	user1 = userService.find(UserUUID);
    	assertNotNull(user1);

    }



    @Test
    public void testAddMemberToGroup(){
        String admin_all = "ADMIN_ALL";
        GrantedAuthorityImpl testAuthority = GrantedAuthorityImpl.NewInstance(admin_all);
        Group group = Group.NewInstance("TestGroup");
        group.addGrantedAuthority(testAuthority);
        UUID groupUUID =  groupService.saveGroup(group);
        User user1 = User.NewInstance("TestUser1", "pwd");
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
