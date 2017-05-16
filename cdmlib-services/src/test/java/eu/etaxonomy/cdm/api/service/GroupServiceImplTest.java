package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileNotFoundException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

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


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }

}
