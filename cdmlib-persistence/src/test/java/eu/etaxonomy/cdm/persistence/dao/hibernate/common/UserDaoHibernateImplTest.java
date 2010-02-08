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

import java.lang.reflect.Array;

import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
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
		assertEquals("the user should have had their authorities loaded",2,user.getAuthorities().size());
	}

}
