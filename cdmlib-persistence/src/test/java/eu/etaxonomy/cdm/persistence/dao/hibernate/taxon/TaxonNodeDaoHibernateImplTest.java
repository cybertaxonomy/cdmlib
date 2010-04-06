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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonomicTreeDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class TaxonNodeDaoHibernateImplTest extends
		CdmTransactionalIntegrationTest {

	@SpringBeanByType	
	private ITaxonNodeDao taxonNodeDao;
	
	@SpringBeanByType
	private ITaxonomicTreeDao taxonomicTreeDao;
	
	@SpringBeanByType
	private ITaxonDao taxonDao;
	
	private UUID uuid1;
	private UUID uuid2;
	
	@Before
	public void setUp(){
		uuid1 = UUID.fromString("0b5846e5-b8d2-4ca9-ac51-099286ea4adc");
		uuid2 = UUID.fromString("770239f6-4fa8-496b-8738-fe8f7b2ad519");
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
		TaxonNode taxonNode = (TaxonNode) taxonNodeDao.findByUuid(uuid1);
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
		TaxonNode taxNode = (TaxonNode) taxonNodeDao.findByUuid(uuid1);
		TaxonNode taxNode2 = (TaxonNode) taxonNodeDao.findByUuid(uuid2);
		Set<TaxonNode> rootNodes = new HashSet<TaxonNode>();
		
		rootNodes.add(taxNode2);
		for (TaxonNode rootNode : rootNodes){
			taxonTree.addChildNode(rootNode, rootNode.getReference(), rootNode.getMicroReference(), rootNode.getSynonymToBeUsed());
		}
		//old: taxonTree.setRootNodes(rootNodes);
		taxNode.addChildNode(taxNode2, null, null,null);
		
		Taxon taxon2 = taxNode2.getTaxon();
		Taxon taxon = taxNode.getTaxon();
		UUID uuidTaxon = taxon.getUuid();
		UUID uuidTaxon2 = taxon2.getUuid();
	
		List<TaxonBase> taxa = taxonDao.getAllTaxonBases(10, 0);
		assertEquals("there should be only two taxa", 5, taxa.size());
		
		taxonNodeDao.delete(taxNode2);
				
		taxa = taxonDao.getAllTaxonBases(10, 0);
		assertEquals("there should be only one taxon left", 4, taxa.size());
		
	}
}
