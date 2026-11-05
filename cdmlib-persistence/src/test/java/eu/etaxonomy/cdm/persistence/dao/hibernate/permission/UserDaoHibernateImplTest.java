/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.filter.EntityFilter;
import eu.etaxonomy.cdm.api.filter.MatchMode;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.persistence.dao.permission.IUserDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmTransactionalSpringIntegrationTest;

@CdmDataSet
public class UserDaoHibernateImplTest extends CdmTransactionalSpringIntegrationTest {

	@Autowired
	private IUserDao userDao;

	@Test
	public void testFindUserByUsername() {
		User user = userDao.findUserByUsername("test");

		assertNotNull(user, "findUserByUsername should return a user");
		assertEquals(2, user.getAuthorities().size(), "the user should have had their authorities loaded");
	}

	@Test
	public void findByUsername(){
		String queryString = "test";
		MatchMode matchmode = MatchMode.ANYWHERE;
		List<EntityFilter<User>> filter = null;
		Integer pageSize = null;
		Integer pageNumber = null;
		List<OrderHint> orderHints = null;
		List<String> propertyPaths = null;
		List<User> list = userDao.findByUsername(queryString, matchmode, filter, pageSize, pageNumber, orderHints, propertyPaths);

		assertNotNull(list, "A list should be returned");
		assertEquals(3, list.size(), "3 users should be returned");

		pageSize = 2;
		list = userDao.findByUsername(queryString, matchmode, filter, pageSize, pageNumber, orderHints, propertyPaths);
		assertNotNull(list, "A list should be returned");
		assertEquals(2, list.size(), "2 users should be returned");
	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }
}
