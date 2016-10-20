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
import static org.junit.Assert.assertNull;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javassist.util.proxy.Proxy;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class TaxonNodeDaoHibernateImplTest extends CdmTransactionalIntegrationTest {

    private static final UUID ClassificationUuid = UUID.fromString("aeee7448-5298-4991-b724-8d5b75a0a7a9");

	@SpringBeanByType
    private ITaxonNodeDao taxonNodeDao;

    @SpringBeanByType
    private IClassificationDao classificationDao;

    @SpringBeanByType
    private ITaxonDao taxonDao;

    @SpringBeanByType
    private IDefinedTermDao termDao;

    private UUID uuid1;
    private UUID uuid2;
    private UUID uuid3;

    private static final UUID ACHERONTIA_UUID = UUID.fromString("3b2b3e17-5c4a-4d1b-aa39-349f63100d6b");
    private static final UUID ACHERONTIA_LACHESIS = UUID.fromString("bc09aca6-06fd-4905-b1e7-cbf7cc65d783");
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

        Classification classification =  classificationDao.load(ClassificationUuid, CLASSIFICATION_INIT_STRATEGY);

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
        TaxonNode newNode = rootNode.addChildTaxon(Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()), null), null, null);
        taxonNodeDao.saveOrUpdate(newNode);
        taxonNodeDao.delete(taxNode3, true);

        assertNull(taxonNodeDao.findByUuid(taxNode3.getUuid()));
        classification = classificationDao.findByUuid(ClassificationUuid);

        taxa = taxonDao.getAllTaxonBases(10, 0);
        // There should be 4 taxonBases: at the beginning 6 in the classification + 1 orphan taxon; 1 new created taxon -> 8: delete node3 deleted 4 taxa -> 4 taxa left.
        assertEquals("there should be 4 taxa left", 4, taxa.size());

        classificationDao.delete(classification);
        classification = null;

       // classificationDao.flush();
        classification = classificationDao.findByUuid(ClassificationUuid);
        assertEquals("The tree should be null", null, classification);

    }

    @Test
    @DataSet
    public void testListChildren(){
        Taxon t_acherontia = (Taxon) taxonDao.load(ACHERONTIA_UUID);

        Classification classification =  classificationDao.load(ClassificationUuid);
        List<TaxonNode> children = classificationDao.listChildrenOf(t_acherontia, classification, null, null, null);
        assertNotNull(children);
        assertEquals(2, children.size());
        TaxonNode t_acherontia_node = taxonNodeDao.load(NODE_ACHERONTIA_UUID);
        children =taxonNodeDao.listChildrenOf(t_acherontia_node, null, null, null, true);
        assertNotNull(children);
        assertEquals(3, children.size());
    }

    @Test
    @DataSet
    public void testListSiblings(){
        Taxon t_acherontia_lachesis = (Taxon) taxonDao.load(ACHERONTIA_LACHESIS);

        Classification classification =  classificationDao.load(ClassificationUuid);
        long count = classificationDao.countSiblingsOf(t_acherontia_lachesis, classification);
        assertEquals(2, count);
        List<TaxonNode> siblings = classificationDao.listSiblingsOf(t_acherontia_lachesis, classification, null, null, null);
        assertNotNull(siblings);
        assertEquals(2, siblings.size());
    }

    @Test
    @DataSet
    public void testGetAllTaxaByClassification(){
        Classification classification =  classificationDao.load(ClassificationUuid, CLASSIFICATION_INIT_STRATEGY);

        assertNotNull("findByUuid should return a taxon tree", classification);
        assertNotNull("classification should have a name",classification.getName());
        assertEquals("classification should have a name which is 'Name'",classification.getName().getText(),"Name");
        TaxonNode taxNode = taxonNodeDao.load(uuid1,TAXONNODE_INIT_STRATEGY);
        TaxonNode taxNode2 = taxonNodeDao.load(uuid2,TAXONNODE_INIT_STRATEGY);

        TaxonNode taxNode3 = taxonNodeDao.load(uuid3, TAXONNODE_INIT_STRATEGY);
        Taxon taxon = Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()), null);
        Taxon taxon1 = Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()), null);
        Taxon taxon2 = Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()), null);
        TaxonNode child = taxNode.addChildTaxon(taxon, null, null);
        UUID childUuid = taxonNodeDao.saveOrUpdate(child);
        child = taxonNodeDao.load(childUuid);
        assertNotNull(child);
        child = taxNode2.addChildTaxon(taxon1, null, null);
        taxonNodeDao.saveOrUpdate(child);
        child = taxNode3.addChildTaxon(taxon2, null, null);
        taxonNodeDao.saveOrUpdate(child);

        List<TaxonNode> taxas = taxonNodeDao.getTaxonOfAcceptedTaxaByClassification(classification, null, null);
        assertEquals("there should be 7 taxa left", 7, taxas.size());
        commitAndStartNewTransaction(null);

        taxas = taxonNodeDao.getTaxonOfAcceptedTaxaByClassification(classification, 0, 10);
        logger.info(taxas.size());
        assertEquals("there should be 7 taxa left", 7, taxas.size());

        int countTaxa = taxonNodeDao.countTaxonOfAcceptedTaxaByClassification(classification);
        logger.info(countTaxa);
        assertEquals("there should be 7 taxa left", 7, countTaxa);
    }

    @Test
    @DataSet(value="TaxonNodeDaoHibernateImplTest.testSortindexForJavassist.xml")
    @ExpectedDataSet("TaxonNodeDaoHibernateImplTest.testSortindexForJavassist-result.xml")

    //test if TaxonNode.remove(index) works correctly with proxies
    public void testSortindexForJavassist(){
    	Taxon taxonWithLazyLoadedParentNodeOnTopLevel = (Taxon)taxonDao.findByUuid(UUID.fromString("bc09aca6-06fd-4905-b1e7-cbf7cc65d783"));
    	TaxonNode parent = taxonWithLazyLoadedParentNodeOnTopLevel.getTaxonNodes().iterator().next().getParent();
    	Assert.assertTrue("Parent node must be proxy, otherwise test does not work", parent instanceof Proxy);
    	Taxon firstTopLevelTaxon = (Taxon)taxonDao.findByUuid(UUID.fromString("7b8b5cb3-37ba-4dba-91ac-4c6ffd6ac331"));
    	Classification classification = classificationDao.findByUuid(ClassificationUuid);
    	TaxonNode childNode = classification.addParentChild(taxonWithLazyLoadedParentNodeOnTopLevel, firstTopLevelTaxon, null, null);
    	this.taxonNodeDao.saveOrUpdate(childNode);
    	commitAndStartNewTransaction( new String[]{"TaxonNode"});
    }

    @Test
    @DataSet(value="TaxonNodeDaoHibernateImplTest.testSortindexForJavassist.xml")
    @ExpectedDataSet("TaxonNodeDaoHibernateImplTest.testSortindexForJavassist2-result.xml")
    //test if TaxonNode.addNode(node) works correctly with proxies
    public void testSortindexForJavassist2(){
    	Taxon taxonWithLazyLoadedParentNodeOnTopLevel = (Taxon)taxonDao.findByUuid(UUID.fromString("bc09aca6-06fd-4905-b1e7-cbf7cc65d783"));
    	TaxonNode parent = taxonWithLazyLoadedParentNodeOnTopLevel.getTaxonNodes().iterator().next().getParent();
    	Assert.assertTrue("Parent node must be proxy, otherwise test does not work", parent instanceof Proxy);
    	Taxon newTaxon = Taxon.NewInstance(null, null);
    	Classification classification = classificationDao.findByUuid(ClassificationUuid);
    	TaxonNode newNode = classification.addChildTaxon(newTaxon, 0, null, null);
    	newNode.setUuid(UUID.fromString("58728644-1155-4520-98f7-309fdb62abd7"));
    	this.taxonNodeDao.saveOrUpdate(newNode);
    	commitAndStartNewTransaction( new String[]{"TaxonNode"});
    }

    @Test
    public void testSaveAndLoadTaxonNodeAgentRelation(){
        Classification classification = Classification.NewInstance("Me");
        Taxon taxon = Taxon.NewInstance(null, null);
        Person person = Person.NewInstance();
        TaxonNode node = classification.addChildTaxon(taxon, null, null);
        DefinedTerm lastScrutiny = (DefinedTerm)termDao.findByUuid(DefinedTerm.uuidLastScrutiny);
        TaxonNodeAgentRelation rel = node.addAgentRelation(lastScrutiny, person);
        taxonNodeDao.save(node);
        commitAndStartNewTransaction(null);

        TaxonNode newNode = taxonNodeDao.load(node.getUuid());
        Assert.assertNotSame(node, newNode);
        Assert.assertEquals("Node should have agent relation", 1, newNode.getAgentRelations().size());
        TaxonNodeAgentRelation newRel = newNode.getAgentRelations().iterator().next();
        Assert.assertEquals(rel, newRel);
        Assert.assertEquals(rel.getId(), newRel.getId());
        Assert.assertNotSame(rel, newRel);
    }


    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub
    }

}
