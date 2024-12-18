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
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.hibernate.proxy.HibernateProxy;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.compare.taxon.TaxonNodeSortMode;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextFormatter;
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

    private final UUID uuid1 = UUID.fromString("0b5846e5-b8d2-4ca9-ac51-099286ea4adc");
    private final UUID uuid2 = UUID.fromString("770239f6-4fa8-496b-8738-fe8f7b2ad519");
    private final UUID uuid3  = UUID.fromString("20c8f083-5870-4cbd-bf56-c5b2b98ab6a7");
    private final UUID classificationUuid = UUID.fromString("aeee7448-5298-4991-b724-8d5b75a0a7a9");

    boolean includeUnpublished;

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
        AuditEventContextHolder.clearContext();
        includeUnpublished = true;
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

        @SuppressWarnings("rawtypes")
        List<TaxonBase> taxa = taxonDao.list(10, 0);
        assertEquals("there should be 7 taxa", 7, taxa.size());
        taxNode3 = CdmBase.deproxy(taxNode3);
        taxNode = CdmBase.deproxy(taxNode);
        taxNode2 = CdmBase.deproxy(taxNode2);
        TaxonNode rootNode = CdmBase.deproxy(classification.getRootNode());
        TaxonNode newNode = rootNode.addChildTaxon(Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()), null), null, null);
        taxonNodeDao.saveOrUpdate(newNode);
        taxonNodeDao.delete(taxNode3, true);

        assertNull(taxonNodeDao.findByUuid(taxNode3.getUuid()));
        classification = classificationDao.findByUuid(ClassificationUuid);

        taxa = taxonDao.list(10, 0);
        // There should be 4 taxonBases: at the beginning 6 in the classification + 1 orphan taxon;
        //1 new created taxon -> 8: delete node3 deleted 4 taxa -> 4 taxa left.
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
        boolean includeUnpublished;
        Taxon t_acherontia = (Taxon) taxonDao.load(ACHERONTIA_UUID);
        TaxonNode subtree = null;
        includeUnpublished = true;

        Classification classification =  classificationDao.load(ClassificationUuid);
        List<TaxonNode> children = classificationDao.listChildrenOf(
                t_acherontia, classification, subtree, includeUnpublished, null, null, null);
        assertNotNull(children);
        assertEquals(2, children.size());

        includeUnpublished = false;
        children = classificationDao.listChildrenOf(
                t_acherontia, classification, subtree, includeUnpublished, null, null, null);
        assertNotNull(children);
        assertEquals(1, children.size()); //1 is unpublished
//        assertEquals(0, children.get(0).getChildNodes().size());


        includeUnpublished = true;
        TaxonNode t_acherontia_node = taxonNodeDao.load(NODE_ACHERONTIA_UUID);
        children =taxonNodeDao.listChildrenOf(t_acherontia_node, null, null, true, includeUnpublished, null, null);
        assertNotNull(children);
        assertEquals(3, children.size());
        //with comparator
        Comparator<TaxonNode> comparator = TaxonNodeSortMode.RankAndAlphabeticalOrder.comparator();
        children =taxonNodeDao.listChildrenOf(t_acherontia_node, null, null, true, includeUnpublished, null, comparator);
        assertEquals("Size should be same as without comparator", 3, children.size());
        //not recursive
        children =taxonNodeDao.listChildrenOf(t_acherontia_node, null, null, true, includeUnpublished, null, null);
        assertEquals("Size should be same as recursive", 3, children.size());

        includeUnpublished = false;
        children =taxonNodeDao.listChildrenOf(t_acherontia_node, null, null, true, includeUnpublished, null, null);
        assertNotNull(children);
        assertEquals(2, children.size()); //1 is unpublished
    }


    @Test
    @DataSet
    public void testListChildrenDTO(){
        boolean includeUnpublished;
        Taxon t_acherontia = (Taxon) taxonDao.load(ACHERONTIA_UUID);
        TaxonNode subtree = null;
        includeUnpublished = true;

        Classification classification =  classificationDao.load(ClassificationUuid);
        List<TaxonNodeDto> children = classificationDao.listChildrenOf(
                t_acherontia, classification, subtree, includeUnpublished, null, null);
        assertNotNull(children);
        assertEquals(2, children.size());
        TaxonNodeDto child = children.stream().filter(c->!c.isPublish()).findFirst().get();
        Assert.assertEquals(UUID.fromString("770239f6-4fa8-496b-8738-fe8f7b2ad519"), child.getUuid());
        Assert.assertEquals("Acherontia styx Westwood, 1847", child.getTitleCache());
        Assert.assertEquals("#t1#4#3#2#", child.getTreeIndex());
        Assert.assertEquals(Integer.valueOf(1), child.getTaxonomicChildrenCount());
        Assert.assertEquals(Integer.valueOf(1), child.getSortIndex());
        List<TaggedText> taggedTitle = child.getTaggedTitle();
        Assert.assertEquals("Acherontia styx Westwood, 1847", TaggedTextFormatter.createString(taggedTitle));
        Assert.assertEquals("Acherontia", taggedTitle.get(0).getText());

        includeUnpublished = false;
        children = classificationDao.listChildrenOf(
                t_acherontia, classification, subtree, includeUnpublished, null, null);
        assertNotNull(children);
        assertEquals(1, children.size()); //1 is unpublished
        child = children.get(0);
        Assert.assertEquals(uuid1, child.getUuid());
        Assert.assertEquals("Acherontia lachesis (Fabricius, 1798)", child.getTitleCache());
        Assert.assertEquals("#t1#4#3#1#", child.getTreeIndex());
        Assert.assertEquals(Integer.valueOf(0), child.getTaxonomicChildrenCount());
        Assert.assertEquals(Integer.valueOf(0), child.getSortIndex());


    }


    @Test
    @DataSet
    public void testListSiblings(){
        Taxon t_acherontia_lachesis = (Taxon) taxonDao.load(ACHERONTIA_LACHESIS);

        Classification classification =  classificationDao.load(ClassificationUuid);
        long count = classificationDao.countSiblingsOf(t_acherontia_lachesis, classification, includeUnpublished);
        assertEquals(2, count);
        List<TaxonNode> siblings = classificationDao.listSiblingsOf(
                t_acherontia_lachesis, classification, includeUnpublished, null, null, null);
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
        Taxon taxon = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()), null);
        Taxon taxon1 = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()), null);
        Taxon taxon2 = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()), null);
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
    @DataSet(value="TaxonNodeDaoHibernateImplTest.testSortindexForHibernateProxy.xml")
    @ExpectedDataSet("TaxonNodeDaoHibernateImplTest.testSortindexForHibernateProxy-result.xml")
    //test if TaxonNode.remove(index) works correctly with proxies
    public void testSortindexForHibernateProxy(){
    	Taxon taxonWithLazyLoadedParentNodeOnTopLevel = (Taxon)taxonDao.findByUuid(UUID.fromString("bc09aca6-06fd-4905-b1e7-cbf7cc65d783"));
    	TaxonNode parent = taxonWithLazyLoadedParentNodeOnTopLevel.getTaxonNodes().iterator().next().getParent();
    	Assert.assertTrue("Parent node must be proxy, otherwise test does not work", parent instanceof HibernateProxy);
    	Taxon firstTopLevelTaxon = (Taxon)taxonDao.findByUuid(UUID.fromString("7b8b5cb3-37ba-4dba-91ac-4c6ffd6ac331"));
    	Classification classification = classificationDao.findByUuid(ClassificationUuid);
    	TaxonNode childNode = classification.addParentChild(taxonWithLazyLoadedParentNodeOnTopLevel, firstTopLevelTaxon, null, null);
    	this.taxonNodeDao.saveOrUpdate(childNode);
    	commitAndStartNewTransaction( new String[]{"TaxonNode"});
    }

    @Test
    @DataSet(value="TaxonNodeDaoHibernateImplTest.testSortindexForHibernateProxy.xml")
    @ExpectedDataSet("TaxonNodeDaoHibernateImplTest.testSortindexForHibernateProxy2-result.xml")
    //test if TaxonNode.addNode(node) works correctly with proxies
    public void testSortindexForHibernateProxy2(){
    	Taxon taxonWithLazyLoadedParentNodeOnTopLevel = (Taxon)taxonDao.findByUuid(UUID.fromString("bc09aca6-06fd-4905-b1e7-cbf7cc65d783"));
    	TaxonNode parent = taxonWithLazyLoadedParentNodeOnTopLevel.getTaxonNodes().iterator().next().getParent();
    	Assert.assertTrue("Parent node must be proxy, otherwise test does not work", parent instanceof HibernateProxy);
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

    //see comment 7 in #6199
    @Test
    @DataSet
    public void testPersistExcludedInfos(){
        //test read
        TaxonNode excludedNode = taxonNodeDao.load(UUID.fromString("4f73adcc-a535-4fbe-a97a-c05ee8b12191"));
        Assert.assertTrue("Node should be excluded", excludedNode.isExcluded());
        TaxonNode unplacedNode = taxonNodeDao.load(UUID.fromString("20c8f083-5870-4cbd-bf56-c5b2b98ab6a7"));
        Assert.assertTrue("Node should be unplaced", unplacedNode.isUnplaced());
        TaxonNode notSpecialNode = taxonNodeDao.load(UUID.fromString("770239f6-4fa8-496b-8738-fe8f7b2ad519"));
        Assert.assertFalse("Node should be neither excluded nor unplaced", notSpecialNode.isUnplaced() || notSpecialNode.isExcluded());

        //read excluded node
        Map<Language, LanguageString> map = excludedNode.getPlacementNote();
        Assert.assertEquals(2, map.size());
        Set<Integer> langIds = new HashSet<>();
        for (Language lang : map.keySet()){
            langIds.add(lang.getId());
        }
        Assert.assertTrue("Excluded note must contain text for language id = 1", langIds.contains(1));
        Assert.assertTrue("", langIds.contains(2));
    }

    @Test
    @DataSet ("TaxonDaoHibernateImplTest.testGetTaxaByNameAndArea.xml")
    public final void testGetTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(){
        Classification classification = classificationDao.findByUuid(classificationUuid);
        List<UuidAndTitleCache<TaxonNode>> result = taxonNodeDao.getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(classification,  null, null, true);
        assertNotNull(result);
        assertEquals(7, result.size());

        //test exclude
        UUID excludeUUID = UUID.fromString("a9f42927-e507-4fda-9629-62073a908aae");
        List<UUID> excludeUUids = new ArrayList<>();
        excludeUUids.add(excludeUUID);
        result = taxonNodeDao.getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(classification,  null, null, false);
        assertEquals(6, result.size());

        //test limit
        int limit = 2;
        result = taxonNodeDao.getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(classification,  limit, null, false);
        assertEquals(2, result.size());

        //test pattern
        String pattern = "*Rothschi*";
        result = taxonNodeDao.getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(classification, 2, pattern, true);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(uuid1, result.get(0).getUuid());

        //test pattern
        pattern = "*TestBaum*";
        result = taxonNodeDao.getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(classification, null, pattern, true);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("6d6b43aa-3a77-4be5-91d0-00b702fc5d6e", result.get(0).getUuid().toString());
    }

    @Test
    @DataSet ("TaxonDaoHibernateImplTest.testGetTaxaByNameAndArea.xml")
    public final void testGetTaxonNodeUuidAndTitleCache(){
        String pattern = "";
        List<TaxonNodeDto> result = taxonNodeDao.getUuidAndTitleCache(100, pattern, classificationUuid, true);
        assertNotNull(result);
        assertEquals(6, result.size());

        //test limit
        int limit = 2;
        result = taxonNodeDao.getUuidAndTitleCache(limit, pattern, classificationUuid, true);
        assertEquals(2, result.size());

        //test pattern & classification
        pattern = "*Rothschi*";
        result = taxonNodeDao.getUuidAndTitleCache(100, pattern, classificationUuid, true);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(uuid1, result.get(0).getUuid());

        //test pattern without classification
        pattern = "*Rothschi*";
        result = taxonNodeDao.getUuidAndTitleCache(100, pattern, null, true);
        assertNotNull(result);
        assertEquals(2, result.size());

        //test doubtful & pattern
        pattern = "Aus*";
        result = taxonNodeDao.getUuidAndTitleCache(100, pattern, classificationUuid, true);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ebf8ea46-9f24-47be-8fb5-02bd67f90348", result.get(0).getUuid().toString());
    }

    @Test
    @DataSet ("TaxonNodeDaoHibernateImplTest.findWithoutRank.xml")
    public final void testGetTaxonNodeUuidAndTitleCacheOfacceptedTaxaByClassificationForNameWithoutRank(){
        //test name without rank
        Classification classification = classificationDao.findByUuid(ClassificationUuid);
        String pattern = "Acherontia kohlbeckeri*";
        List<UuidAndTitleCache<TaxonNode>> result = taxonNodeDao.getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(classification, null, pattern, true);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("4f73adcc-a535-4fbe-a97a-c05ee8b12191", result.get(0).getUuid().toString()); // titleCache:Acherontia kohlbeckeri rank: Unknown Rank
    }

    @Test
    @DataSet ("TaxonNodeDaoHibernateImplTest.findWithoutRank.xml")
    public final void testGetTaxonNodeDtoWithoutRank(){

        List<TaxonNodeDto> result = taxonNodeDao.getTaxonNodeDto(null, "", null); // cant use "*" here since this is not supported by the method under test
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("20c8f083-5870-4cbd-bf56-c5b2b98ab6a7", result.get(0).getUuid().toString()); // Acherontia(Fabricius, 1798) rank: Genus
        assertEquals(uuid1, result.get(1).getUuid()); // titleCache:Acherontia lachesis (Fabricius, 1798) rank: Species
        assertEquals("770239f6-4fa8-496b-8738-fe8f7b2ad519", result.get(2).getUuid().toString()); // titleCache:Acherontia styx Westwood, 1847 sec. cate-sphingidae.org rank: Species
        assertEquals("4f73adcc-a535-4fbe-a97a-c05ee8b12191", result.get(3).getUuid().toString()); // titleCache:Acherontia kohlbeckeri rank: Unknown Rank
    }

    @Test
    @DataSet ("TaxonNodeDaoHibernateImplTest.findWithoutRank.xml")
    public final void testGetTaxonNodeDtoCheckSortIndex(){

        List<TaxonNodeDto> result = taxonNodeDao.getTaxonNodeDto(null, "", null);
        assertEquals(5, result.size());

        assertTrue(0 == result.get(0).getSortIndex()); // Acherontia(Fabricius, 1798) rank: Genus
        assertTrue(0 == result.get(1).getSortIndex()); // titleCache:Acherontia lachesis (Fabricius, 1798) rank: Species
        assertTrue(1 == result.get(2).getSortIndex()); // titleCache:Acherontia styx Westwood, 1847 sec. cate-sphingidae.org rank: Species
        assertTrue(0 == result.get(3).getSortIndex()); // titleCache:Acherontia kohlbeckeri rank: Unknown Rank
    }

    @Test
    @DataSet ("TaxonNodeDaoHibernateImplTest.findWithoutRank.xml")
    public final void testGetTaxonNodeDtoCheckStatus(){

        List<TaxonNodeDto> result = taxonNodeDao.getTaxonNodeDto(null, "", null);
        assertEquals(5, result.size());

        assertEquals(TaxonNodeStatus.UNPLACED, result.get(0).getStatus()); // Acherontia(Fabricius, 1798) rank: Genus
        assertEquals(null, result.get(1).getStatus());  // titleCache:Acherontia lachesis (Fabricius, 1798) rank: Species
        assertEquals(null, result.get(2).getStatus());  // titleCache:Acherontia styx Westwood, 1847 sec. cate-sphingidae.org rank: Species
        assertEquals(TaxonNodeStatus.EXCLUDED, result.get(3).getStatus());  // titleCache:Acherontia kohlbeckeri rank: Unknown Rank


    }

    @Test
    @DataSet ("TaxonNodeDaoHibernateImplTest.findWithoutRank.xml")
    public final void testGetTaxonNodeDtoCheckNote(){

        List<TaxonNodeDto> result = taxonNodeDao.getTaxonNodeDto(null, "", null);
        assertEquals(5, result.size());

        assertNotNull(result.get(3).getPlacementNote()); // Acherontia(Fabricius, 1798) rank: Genus
        assertNotNull(result.get(4).getPlacementNote());  // titleCache:Acherontia lachesis (Fabricius, 1798) rank: Species
//        assertEquals(null, result.get(2).getStatus());  // titleCache:Acherontia styx Westwood, 1847 sec. cate-sphingidae.org rank: Species
//        assertEquals(TaxonNodeStatus.EXCLUDED, result.get(3).getStatus());  // titleCache:Acherontia kohlbeckeri rank: Unknown Rank
//

    }


    @Override
    public void createTestDataSet() throws FileNotFoundException {}

}
