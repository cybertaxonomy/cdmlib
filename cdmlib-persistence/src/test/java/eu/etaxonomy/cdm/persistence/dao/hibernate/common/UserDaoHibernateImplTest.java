package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Array;

import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.dao.common.IUserDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet
public class UserDaoHibernateImplTest extends CdmIntegrationTest {
	
	@SpringBeanByType
	IUserDao userDao;
	
	@Test
	public void testFindUserByUsername() {
		User user = userDao.findUserByUsername("test");
		
		assertNotNull("findUserByUsername should return a user", user);
		assertEquals("the user should have had their authorities loaded",2,Array.getLength(user.getAuthorities()));
	}

}
