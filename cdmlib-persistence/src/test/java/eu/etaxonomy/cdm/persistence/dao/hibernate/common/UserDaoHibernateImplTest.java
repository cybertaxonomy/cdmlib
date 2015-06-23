/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.dao.common.IUserDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet
public class UserDaoHibernateImplTest extends CdmIntegrationTest {

	@SpringBeanByType
	IUserDao userDao;


	@Test
	public void testFindUserByUsername() {
		User user = userDao.findUserByUsername("test");

		assertNotNull("findUserByUsername should return a user", user);
		assertEquals("the user should have had their authorities loaded",2,user.getAuthorities().size());
	}

	@Test
	public void findByUsername(){
		String queryString = "test";
		MatchMode matchmode = MatchMode.ANYWHERE;
		List<Criterion> criteria = null;
		Integer pageSize = null;
		Integer pageNumber = null;
		List<OrderHint> orderHints = null;
		List<String> propertyPaths = null;
		List<User> list = userDao.findByUsername(queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths);

		assertNotNull("A list should be returned", list);
		assertEquals("3 users should be returned", 3, list.size());

		pageSize = 2;
		list = userDao.findByUsername(queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths);
		assertNotNull("A list should be returned", list);
		assertEquals("2 users should be returned", 2, list.size());
	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }
}
