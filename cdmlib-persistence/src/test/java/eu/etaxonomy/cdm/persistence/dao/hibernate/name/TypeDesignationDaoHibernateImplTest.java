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
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.util.JDBCExceptionReporter;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITypeDesignationDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.babadshanjan
 * @author a.mueller
 * @created 25.05.2009
 * @version 1.1
 */
@DataSet
public class TypeDesignationDaoHibernateImplTest extends CdmIntegrationTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TypeDesignationDaoHibernateImplTest.class);

	@SpringBeanByType
	ITypeDesignationDao typeDesignationDao;
	
	@SpringBeanByType
	ITaxonNameDao nameDao;
	
	@SpringBeanByType
	IAgentDao agentDao;

	
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
		assertEquals(2, typeDesignations.size());
		SpecimenTypeDesignation specTypeDesig = null;
		for (TypeDesignationBase typeDesignation : typeDesignations) {
			if (typeDesignation.isInstanceOf(NameTypeDesignation.class)) {
				assertTrue(typeDesignation.getTypeStatus().isInstanceOf(NameTypeDesignationStatus.class));
			} else if (typeDesignation.isInstanceOf(SpecimenTypeDesignation.class)) {
				Assert.assertNull("There should be only 1 specimen type designation but this is already the second", specTypeDesig);
				TypeDesignationStatusBase typeDesignationStatus = typeDesignation.getTypeStatus();
				assertTrue(typeDesignationStatus.isInstanceOf(SpecimenTypeDesignationStatus.class));
				specTypeDesig = CdmBase.deproxy(typeDesignation,SpecimenTypeDesignation.class);
			}
		}
		Set<TaxonNameBase> names = specTypeDesig.getTypifiedNames();
		Assert.assertEquals("There should be exactly 1 typified name for the the specimen type designation", 1, names.size());
		TaxonNameBase singleName = names.iterator().next();
		Assert.assertEquals("", UUID.fromString("61b1dcae-8aa6-478a-bcd6-080cf0eb6ad7"), singleName.getUuid());
	}
	
//	/**
//	TODO currently throws "Could not execute JDBC batch update" exception when trying to save a new name
//	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.name.TypeDesignationHibernateImpl#saveOrUpdate(CdmBase)}.
//	 */
//	@Test
//	@ExpectedDataSet
//	public void testSaveTypeDesignations() {
////		List<TypeDesignationBase> typeDesignations = typeDesignationDao.getAllTypeDesignations(100, 0);
////		assertEquals(typeDesignations.size(), 2);
////		SpecimenTypeDesignation specTypeDesig = null;
////		for (TypeDesignationBase typeDesignation : typeDesignations) {
////			if (typeDesignation.isInstanceOf(SpecimenTypeDesignation.class)) {
////				specTypeDesig = CdmBase.deproxy(typeDesignation,SpecimenTypeDesignation.class);
////			}
////		}
//		
//		TaxonNameBase newName = BotanicalName.NewInstance(Rank.SPECIES());
////		newName.setTitleCache("Name used as typified name", true);
////		newName.addTypeDesignation(specTypeDesig, false);
////		Set<TaxonNameBase> typifiedNames = specTypeDesig.getTypifiedNames();
////		Assert.assertEquals("There should be 2 typified names for this type designation now", 2, typifiedNames.size());
//		
//		nameDao.save(newName);
////		typeDesignationDao.saveOrUpdate(specTypeDesig);
////		this.endTransaction();
////		printDataSet(System.out, new String[]{"TaxonNameBase","TypeDesignationBase",
////				"TypeDesignationBase_TaxonNameBase","TaxonNameBase_typeDesignationBase"});
//		
//		Person person = Person.NewTitledInstance("new Person");
//		agentDao.save(person);
//	}
	
//	@Test
//	public void printDataSet(){
//		printDataSet(System.out, new String[]{"TaxonNameBase","TypeDesignationBase",
//				"TypeDesignationBase_TaxonNameBase","TaxonNameBase_typeDesignationBase"});
//	}
	
}
