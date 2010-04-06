// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.persistence.dao.name.ITypeDesignationDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.babadshanjan
 * @created 25.05.2009
 * @version 1.0
 */
@DataSet
public class TypeDesignationDaoHibernateImplTest extends CdmIntegrationTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(TypeDesignationDaoHibernateImplTest.class);

	@SpringBeanByType
	ITypeDesignationDao typeDesignationDao;
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.name.TypeDesignationHibernateImpl#TypeDesignationHibernateImpl()}.
	 */
	@Test
	public void testInit() {
		assertNotNull("Instance of ITypeDesignationDao expected", typeDesignationDao);
	}
	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.name.TypeDesignationHibernateImpl#getAllTypeDesignations(java.lang.Integer, java.lang.Integer)}.
	 */
	@Test
	public void testGetAllTypeDesignations() {
		List<TypeDesignationBase> typeDesignations = typeDesignationDao.getAllTypeDesignations(100, 0);
		assertEquals(typeDesignations.size(), 2);
		for (TypeDesignationBase typeDesignation : typeDesignations) {
			if (typeDesignation.isInstanceOf(NameTypeDesignation.class)) {
				assertTrue(typeDesignation.getTypeStatus().isInstanceOf(NameTypeDesignationStatus.class));
			} else if (typeDesignation.isInstanceOf(SpecimenTypeDesignation.class)) {
				TypeDesignationStatusBase typeDesignationStatus = typeDesignation.getTypeStatus();
				assertTrue(typeDesignationStatus.isInstanceOf(SpecimenTypeDesignationStatus.class));
			}
		}
	}
}
