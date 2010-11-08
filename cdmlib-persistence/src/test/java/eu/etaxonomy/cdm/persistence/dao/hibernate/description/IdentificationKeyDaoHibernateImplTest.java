/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.persistence.dao.description.IIdentificationKeyDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet
public class IdentificationKeyDaoHibernateImplTest extends CdmIntegrationTest {
	
	@SpringBeanByType
	IIdentificationKeyDao identificationKeyDao;

	@SpringBeanByType
	PolytomousKeyDaoImpl polytomousKeyDao;

	
	@Before
	public void setUp() {
		
	}
	
	
	@Test
	public void testCountByDistribution() {
		identificationKeyDao.list(null, null, null);
	}
	
	@Test
	public void testSavePolytomousKey() {
		PolytomousKey existingKey = polytomousKeyDao.findByUuid(UUID.fromString("bab66772-2c83-428a-bb6d-655d12ac6097"));
		Assert.assertNotNull("",existingKey);
		PolytomousKeyNode root = existingKey.getRoot();
		Assert.assertNotNull("",root);
		Assert.assertEquals(2, root.childCount());
		
		//new key
		PolytomousKey newKey = PolytomousKey.NewInstance();
		PolytomousKeyNode newRoot = PolytomousKeyNode.NewInstance();
		newKey.setRoot(newRoot);
		polytomousKeyDao.save(newKey);
	}
}
