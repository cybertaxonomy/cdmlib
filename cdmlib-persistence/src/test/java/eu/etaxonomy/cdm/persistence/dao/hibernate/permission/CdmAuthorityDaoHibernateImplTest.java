/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.permission;

import java.io.FileNotFoundException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.permission.CRUD;
import eu.etaxonomy.cdm.model.permission.CdmAuthority;
import eu.etaxonomy.cdm.model.permission.Operation;
import eu.etaxonomy.cdm.model.permission.PermissionClass;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.persistence.dao.permission.ICdmAuthorityDao;
import eu.etaxonomy.cdm.persistence.dao.permission.IUserDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class CdmAuthorityDaoHibernateImplTest extends CdmTransactionalIntegrationTest {

	@SpringBeanByType
	private ICdmAuthorityDao authorityDao;
    @SpringBeanByType
    private IUserDao userDao;

	private UUID uuidAuthority1 = UUID.fromString("596b1325-be50-4b0a-9aa2-3ecd610215f2");
	private UUID uuidTarget = UUID.fromString("037a06cd-a17c-43f8-8bfb-b7ed87e266dd");



	@Before
	public void setUp() {

	}

	@Test
	public void testSave() {
	    CdmAuthority authority = CdmAuthority.NewInstance(PermissionClass.AGENTBASE,
	            "property", Operation.CREATE, uuidAuthority1);
		authorityDao.save(authority);
	}

    @Test
    @DataSet
    public void testLoad() {
        CdmAuthority authority = authorityDao.load(uuidAuthority1);
        Assert.assertNotNull(authority);
        Assert.assertTrue(authority.getOperations().contains(CRUD.CREATE));
        User user = userDao.load(1, null);
        Assert.assertNotNull(user);
        Assert.assertEquals(authority, user.getAuthoritiesB().iterator().next());
    }

    @Override
//    @Test
    public void createTestDataSet() throws FileNotFoundException {
//        CdmAuthority authority = CdmAuthority.NewInstance(PermissionClass.AGENTBASE,
//                "property", Operation.CREATE, uuidTarget);
//        authority.setUuid(uuidAuthority1);
//        User user = User.NewInstance("username", "pwd");
//        user.addAuthority(authority);
//        userDao.save(user);
//
//
//        // 2. end the transaction so that all data is actually written to the db
//        setComplete();
//        endTransaction();
//        String fileNameAppendix = null;
//
//        // 3.
//        writeDbUnitDataSetFile(new String[] {
//            "Authority","PermissionGroup_Authority","UserAccount_Authority","UserAccount" // IMPORTANT!!!
//            },
//            fileNameAppendix, true );
    }


}
