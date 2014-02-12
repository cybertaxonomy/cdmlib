package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.exception.ReferencedObjectUndeletableException;
import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

public class GroupServiceImplTest extends CdmIntegrationTest {
	private static final Logger logger = Logger.getLogger(GroupServiceImplTest.class);

    @SpringBeanByType
    IGroupService groupService;
    
    @SpringBeanByType
    IUserService userService;
    
    
    @Test
    public void testDeleteGroup(){
    	GrantedAuthorityImpl testAuthority = GrantedAuthorityImpl.NewInstance();
    	testAuthority.setAuthority("ADMIN_ALL");
    	Group group = Group.NewInstance("TestGroup");
    	group.addGrantedAuthority(testAuthority);
    	UUID groupUUID =  groupService.saveGroup(group);
    	User user1 = User.NewInstance("TestUser1", "pwd");
    	UUID UserUUID =  userService.save(user1);
    	
    	try {
			groupService.delete(group);
		} catch (ReferencedObjectUndeletableException e) {
			Assert.fail();
			
		}
    	group = groupService.find(groupUUID);
    	assertNull(group);
    	user1 = userService.find(UserUUID);
    	assertNotNull(user1);
    	
    }

}
