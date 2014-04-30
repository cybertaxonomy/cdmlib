/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
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

    private static final UUID ACHERONTIA_UUID = UUID.fromString("3b2b3e17-5c4a-4d1b-aa39-349f63100d6b");
    private static final UUID NODE_ACHERONTIA_UUID = UUID.fromString("20c8f083-5870-4cbd-bf56-c5b2b98ab6a7");

    private static final List<String> CLASSIFICATION_INIT_STRATEGY = Arrays.asList(new String[]{
            "rootNode"
    });
    private static final List<String> TAXONNODE_INIT_STRATEGY = Arrays.asList(new String[]{
            "taxon",
            "childNodes"
    });

    @Before
    public void setUp(){
        uuid1 = UUID.fromString("0b5846e5-b8d2-4ca9-ac51-099286ea4adc");
        uuid3 = UUID.fromString("20c8f083-5870-4cbd-bf56-c5b2b98ab6a7");
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
        TaxonNode taxonNode = taxonNodeDao.findByUuid(uuid1);
        Classification.class.getDeclaredConstructors();
        assertNotNull("findByUuid should return a taxon node", taxonNode);
    }

    @Test
    @DataSet
    public void testClassification() {

        Classification classification =  classificationDao.load(UUID.fromString("aeee7448-5298-4991-b724-8d5b75a0a7a9"), CLASSIFICATION_INIT_STRATEGY);

        assertNotNull("findByUuid should return a taxon tree", classification);
        assertNotNull("classification should have a name",classification.getName());
        assertEquals("classification should have a name which is 'Name'",classification.getName().getText(),"Name");
        TaxonNode taxNode = taxonNodeDao.load(uuid1,TAXONNODE_INIT_STRATEGY);
        TaxonNode taxNode2 = taxonNodeDao.load(uuid2,TAXONNODE_INIT_STRATEGY);

        TaxonNode taxNode3 = taxonNodeDao.load(uuid3, TAXONNODE_INIT_STRATEGY);



        List<TaxonBase> taxa = taxonDao.getAllTaxonBases(10, 0);
        assertEquals("there should be 7 taxa", 7, taxa.size());
        taxNode3 = HibernateProxyHelper.deproxy(taxNode3, TaxonNode.class);
        taxNode = HibernateProxyHelper.deproxy(taxNode, TaxonNode.class);
        taxNode2 = HibernateProxyHelper.deproxy(taxNode2, TaxonNode.class);
        TaxonNode rootNode = HibernateProxyHelper.deproxy(classification.getRootNode(), TaxonNode.class);
        rootNode.addChildTaxon(Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()), null), null, null);
        taxonNodeDao.delete(taxNode3, true);
        classification = classificationDao.findByUuid(UUID.fromString("aeee7448-5298-4991-b724-8d5b75a0a7a9"));

        taxa = taxonDao.getAllTaxonBases(10, 0);
        assertEquals("there should be 7 taxa left", 7, taxa.size());
        taxonNodeDao.flush();
        classificationDao.delete(classification);
        classification = null;

        classificationDao.flush();
        classification = classificationDao.findByUuid(UUID.fromString("aeee7448-5298-4991-b724-8d5b75a0a7a9"));
        assertEquals("The tree should be null", null, classification);

    }

    @Test
    @DataSet
    public void testlistChildren(){
        Taxon t_acherontia = (Taxon) taxonDao.load(ACHERONTIA_UUID);

        Classification classification =  classificationDao.load(UUID.fromString("aeee7448-5298-4991-b724-8d5b75a0a7a9"));
        List<TaxonNode> children = classificationDao.listChildrenOf(t_acherontia, classification, null, null, null);
        assertNotNull(children);
        assertEquals(2, children.size());

    }

    @Test
    @DataSet
    public void testGetAllTaxaByClassification(){
        Classification classification =  classificationDao.load(UUID.fromString("aeee7448-5298-4991-b724-8d5b75a0a7a9"), CLASSIFICATION_INIT_STRATEGY);

        assertNotNull("findByUuid should return a taxon tree", classification);
        assertNotNull("classification should have a name",classification.getName());
        assertEquals("classification should have a name which is 'Name'",classification.getName().getText(),"Name");
        TaxonNode taxNode = taxonNodeDao.load(uuid1,TAXONNODE_INIT_STRATEGY);
        TaxonNode taxNode2 = taxonNodeDao.load(uuid2,TAXONNODE_INIT_STRATEGY);

        TaxonNode taxNode3 = taxonNodeDao.load(uuid3, TAXONNODE_INIT_STRATEGY);
        Taxon taxon = Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()), null);
        Taxon taxon1 = Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()), null);
        Taxon taxon2 = Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()), null);
        taxNode.addChildTaxon(taxon, null, null);
        taxNode2.addChildTaxon(taxon1, null, null);
        taxNode3.addChildTaxon(taxon2, null, null);

        List<TaxonNode> taxas = taxonNodeDao.getTaxonOfAcceptedTaxaByClassification(classification, null, null);
        assertEquals("there should be 6 taxa left", 6, taxas.size());


        taxas = taxonNodeDao.getTaxonOfAcceptedTaxaByClassification(classification, 0, 10);
        logger.info(taxas.size());
        assertEquals("there should be 6 taxa left", 6, taxas.size());

        int countTaxa = taxonNodeDao.countTaxonOfAcceptedTaxaByClassification(classification);
        logger.info(countTaxa);
        assertEquals("there should be 6 taxa left", 6, countTaxa);


    }
}
