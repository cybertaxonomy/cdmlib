/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonomicTreeDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class TaxonNodeDaoHibernateImplTest extends
		CdmTransactionalIntegrationTest {

	@SpringBeanByType	
	private ITaxonNodeDao taxonNodeDao;
	
	@SpringBeanByType
	private ITaxonomicTreeDao taxonomicTreeDao;
	
	private UUID uuid;
	
	@Before
	public void setUp(){
		uuid = UUID.fromString("0b5846e5-b8d2-4ca9-ac51-099286ea4adc");
		AuditEventContextHolder.clearContext(); 
	}
	
	@After
	public void tearDown(){
		AuditEventContextHolder.clearContext(); 
	}
	
	
	@Test
	@DataSet
	public void testInit() {
		assertNotNull("Instance of ITaxonDao expected",taxonNodeDao);
		assertNotNull("Instance of IReferenceDao expected",taxonomicTreeDao);
	}	
	
	@Test
	@DataSet
	public void testFindByUuid() {
		TaxonNode taxonNode = (TaxonNode) taxonNodeDao.findByUuid(uuid);
		TaxonomicTree.class.getDeclaredConstructors();
		assertNotNull("findByUuid should return a taxon node", taxonNode);
	}
	
	@Test
	@DataSet
	public void testTaxonomicTree() {
		TaxonomicTree taxonTree =  taxonomicTreeDao.findByUuid(UUID.fromString("aeee7448-5298-4991-b724-8d5b75a0a7a9"));
		
		assertNotNull("findByUuid should return a taxon tree", taxonTree);
		assertNotNull("taxonomic tree should have a name",taxonTree.getName());
		assertEquals("taxonomic tree should have a name which is 'Name'",taxonTree.getName().getText(),"Name");
	}
}
