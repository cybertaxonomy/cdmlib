/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import static org.junit.Assert.assertNotNull;
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
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class TaxonNodeDaoHibernateImplTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    private ITaxonNodeDao taxonNodeDao;

    @SpringBeanByType
    private IClassificationDao classificationDao;

    @SpringBeanByType
    private ITaxonDao taxonDao;

    private UUID uuid1;
    private UUID uuid2;
    private UUID uuid3;

    @Before
    public void setUp(){
        uuid1 = UUID.fromString("20c8f083-5870-4cbd-bf56-c5b2b98ab6a7");
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
        assertNotNull("Instance of IReferenceDao expected",classificationDao);
    }

    @Test
    @DataSet
    public void testFindByUuid() {
        TaxonNode taxonNode = (TaxonNode) taxonNodeDao.findByUuid(uuid1);
        Classification.class.getDeclaredConstructors();
        assertNotNull("findByUuid should return a taxon node", taxonNode);
    }

    @Test
    @DataSet
    public void testClassification() {
        Classification classification =  classificationDao.findByUuid(UUID.fromString("aeee7448-5298-4991-b724-8d5b75a0a7a9"));

        assertNotNull("findByUuid should return a taxon tree", classification);
        assertNotNull("classification should have a name",classification.getName());
        assertEquals("classification should have a name which is 'Name'",classification.getName().getText(),"Name");
        TaxonNode taxNode = (TaxonNode) taxonNodeDao.findByUuid(uuid1);
        TaxonNode taxNode2 = (TaxonNode) taxonNodeDao.findByUuid(uuid2);
        Set<TaxonNode> rootNodes = new HashSet<TaxonNode>();

        rootNodes.add(taxNode);


        for (TaxonNode rootNode : rootNodes){
            classification.addChildNode(rootNode, rootNode.getReference(), rootNode.getMicroReference(), rootNode.getSynonymToBeUsed());
        }

        taxNode.addChildNode(taxNode2, null, null,null);
        
        Taxon taxon2 = taxNode2.getTaxon();
        Taxon taxon = taxNode.getTaxon();
        UUID uuidTaxon = taxon.getUuid();
        UUID uuidTaxon2 = taxon2.getUuid();

        List<TaxonBase> taxa = taxonDao.getAllTaxonBases(10, 0);
        assertEquals("there should be six taxa", 6, taxa.size());

        taxonNodeDao.delete(taxNode2);

        taxa = taxonDao.getAllTaxonBases(10, 0);
        assertEquals("there should be five taxa left", 5, taxa.size());

        classificationDao.delete(classification);
        classification = classificationDao.findByUuid(UUID.fromString("aeee7448-5298-4991-b724-8d5b75a0a7a9"));
        assertEquals("The tree should be null", null, classification);


    }
}
