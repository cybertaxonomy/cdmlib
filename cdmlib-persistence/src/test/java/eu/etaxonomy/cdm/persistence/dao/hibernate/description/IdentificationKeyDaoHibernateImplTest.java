/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.persistence.dao.description.IIdentificationKeyDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet
public class IdentificationKeyDaoHibernateImplTest extends CdmIntegrationTest {
	
	@SpringBeanByType
	IIdentificationKeyDao identificationKeyDao;
	
	@Before
	public void setUp() {
		
	}
	
	@Test
	public void testCountByDistribution() {
		identificationKeyDao.list(null, null, null);
	}

}
