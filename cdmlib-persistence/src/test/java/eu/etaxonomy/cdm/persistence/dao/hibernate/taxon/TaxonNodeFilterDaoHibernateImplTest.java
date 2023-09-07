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

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeFilterDao;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
 * @since 2014/06/13
 */
public class TaxonNodeFilterDaoHibernateImplTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    private ITaxonNodeDao taxonNodeDao;

    @SpringBeanByType
    private IClassificationDao classificationDao;

    @SpringBeanByType
    private ITaxonNodeFilterDao filterDao;

    @SpringBeanByType
    private IDefinedTermDao termDao;

    private final UUID europeUuid = UUID.fromString("e860871c-3a14-4ef2-9367-bbd92586c95b");
    private final UUID germanyUuid = UUID.fromString("7b7c2db5-aa44-4302-bdec-6556fd74b0b9");
    private final UUID denmarkUuid = UUID.fromString("f818c97e-fd61-42fe-9d75-d433f8cb349c");
    private final UUID franceUuid = UUID.fromString("41c5129a-3465-42cc-b016-59ab9ffad71a");

    private final UUID middleEuropeUuid = UUID.fromString("d292f237-da3d-408b-93a1-3257a8c80b97");
    private final UUID africaUuid = UUID.fromString("9444016a-b334-4772-8795-ed4019552087");

    private final UUID uuidNode1 = UUID.fromString("ec88fd49-59c8-4228-a826-77dff951d7f8");
    private final UUID uuidNode2 = UUID.fromString("4c0ecc22-e3c0-445d-912a-93ee49fb256a");
    private final UUID uuidNode3 = UUID.fromString("30ba17f1-2f37-4286-9665-27d5adbd230d");
    private final UUID uuidNode4 = UUID.fromString("2e6ca5d6-2fc8-4f0c-a3fe-03e596dd8afe");
    private final UUID uuidNode5 = UUID.fromString("e01f56c7-0315-450b-a05b-881d21adf02b");
    private final UUID uuidNodeUnpublished = UUID.fromString("96888466-f40e-43ed-a17d-cdc62bdf0ff0");

    private Classification classification1;
    private TaxonNode node1;
    private TaxonNode node2;
    private TaxonNode node3;
    private TaxonNode node4;
    private TaxonNode node5;
    private TaxonNode nodeUnpublished;
    private Taxon taxon1;
    private Taxon taxon2;
    private Taxon taxon3;
    private Taxon taxon4;
    private Taxon taxon5;
    private Taxon taxonUnpublished;

    @Before
    public void setUp() throws Exception {
        /*
         * classification 1
         *  - node1 (taxon1, Genus, Europe)
         *   - node3 (taxon3, Species, Germany)  //if subspecies exists in Denmark this is not fully correct !!
         *    - node4 (taxon4, Subspecies, Denmark)
         *    - node5 (taxon5, Subspecies)
         *  - node2 (taxon2, Family, France)
         */
        classification1 = Classification.NewInstance("TestClassification");
        Reference citation = null;
        String microCitation = null;
        taxon1 = Taxon.NewInstance(TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.GENUS(), null, null, null, null, null, null, null, null), null);
        taxon2 = Taxon.NewInstance(TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.FAMILY(), null, null, null, null, null, null, null, null), null);
        taxon3 = Taxon.NewInstance(TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.SPECIES(), null, null, null, null, null, null, null, null), null);
        taxon4 = Taxon.NewInstance(TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.SUBSPECIES(), null, null, null, null, null, null, null, null), null);
        taxon5 = Taxon.NewInstance(TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.SUBSPECIES(), null, null, null, null, null, null, null, null), null);
        taxonUnpublished = Taxon.NewInstance(TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.SUBSPECIES(), null, null, null, null, null, null, null, null), null);
        taxonUnpublished.setPublish(false);

        NamedArea europe = (NamedArea) termDao.load(europeUuid);
        NamedArea germany = (NamedArea) termDao.load(germanyUuid);
        NamedArea denmark = (NamedArea) termDao.load(denmarkUuid);
        NamedArea france = (NamedArea) termDao.load(franceUuid);
        TaxonDescription.NewInstance(taxon1).addElement(Distribution.NewInstance(europe, PresenceAbsenceTerm.NATIVE()));
        TaxonDescription.NewInstance(taxon2).addElement(Distribution.NewInstance(france, PresenceAbsenceTerm.NATIVE()));
        TaxonDescription.NewInstance(taxon3).addElement(Distribution.NewInstance(germany, PresenceAbsenceTerm.NATIVE()));
        TaxonDescription.NewInstance(taxon4).addElement(Distribution.NewInstance(denmark, PresenceAbsenceTerm.ABSENT()));

        node1 = classification1.addChildTaxon(taxon1, citation, microCitation);
        node1.setUuid(uuidNode1);
        node1= taxonNodeDao.save(node1);

        node2 = classification1.addChildTaxon(taxon2, citation, microCitation);
        node2.setUuid(uuidNode2);
        node2 = taxonNodeDao.save(node2);

        node3 = node1.addChildTaxon(taxon3, citation, microCitation);
        node3.setUuid(uuidNode3);
        taxonNodeDao.save(node3);

        node4 = node3.addChildTaxon(taxon4, citation, microCitation);
        node4.setUuid(uuidNode4);
        taxonNodeDao.save(node4);

        node5 = node3.addChildTaxon(taxon5, citation, microCitation);
        node5.setUuid(uuidNode5);
        node5 = taxonNodeDao.save(node5);

        nodeUnpublished = node3.addChildTaxon(taxonUnpublished, citation, microCitation);
        nodeUnpublished.setUuid(uuidNodeUnpublished);
        nodeUnpublished = taxonNodeDao.save(nodeUnpublished);

        //MergeResult result = taxonNodeDao.merge(node5, true);
        //node5 = (TaxonNode) result.getMergedEntity();

        //taxonNodeDao.save(node5);

        classificationDao.save(classification1);
    }

    @Test
    public void testListUuidsOrdered() {
        Classification classification = classificationDao.findByUuid(classification1.getUuid());

        TaxonNodeFilter filter;
        List<UUID> listUuid;

        //UUID
        filter = TaxonNodeFilter.NewClassificationInstance(classification);
        filter.setOrder(TaxonNodeFilter.ORDER.TREEINDEX);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("All 5 children but not root node should be returned", 5, listUuid.size());

        List<UUID> expectedList = Arrays.asList(new UUID[]{uuidNode1, uuidNode3, uuidNode4, uuidNode5, uuidNode2});
        //in theory node1 and 3 as well as node4 and 5 could be exchanged depending on the id they get. But we expect
        //in this test environment that node1.id < node2.id and node4.id < node5.id
        Assert.assertEquals(expectedList, listUuid);

        List<Integer> idList = filterDao.idList(filter);
        Assert.assertEquals((Integer)node1.getId(), idList.get(0));
        Assert.assertEquals((Integer)node3.getId(), idList.get(1));
        Assert.assertEquals((Integer)node4.getId(), idList.get(2));
        Assert.assertEquals((Integer)node5.getId(), idList.get(3));
        Assert.assertEquals((Integer)node2.getId(), idList.get(4));

        //ID
        filter.setOrder(TaxonNodeFilter.ORDER.ID);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("All 5 children but not root node should be returned", 5, listUuid.size());

        expectedList = Arrays.asList(new UUID[]{uuidNode1, uuidNode2, uuidNode3, uuidNode4, uuidNode5});
        //in theory the given id is not necessarily ascending per save, but usually it is, at least in test environment
        //and therefore we expect the given result
        Assert.assertEquals(expectedList, listUuid);

        filter.setOrder(TaxonNodeFilter.ORDER.TREEINDEX_DESC);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("All 5 children but not root node should be returned", 5, listUuid.size());

        expectedList = Arrays.asList(new UUID[]{uuidNode2, uuidNode5, uuidNode4, uuidNode3, uuidNode1});
        //in theory node1 and 3 as well as node4 and 5 could be exchanged depending on the id they get. But we expect
        //in this test environment that node1.id < node2.id and node4.id < node5.id
        Assert.assertEquals(expectedList, listUuid);

        idList = filterDao.idList(filter);
        Assert.assertEquals((Integer)node2.getId(), idList.get(0));
        Assert.assertEquals((Integer)node5.getId(), idList.get(1));
        Assert.assertEquals((Integer)node4.getId(), idList.get(2));
        Assert.assertEquals((Integer)node3.getId(), idList.get(3));
        Assert.assertEquals((Integer)node1.getId(), idList.get(4));
    }

    @Test
    public void testListUuidsByAreas() {
        String message = "wrong number of nodes filtered";
//        System.out.println("start:" + new DateTime().toString());

        NamedArea europe = HibernateProxyHelper.deproxy(termDao.load(europeUuid), NamedArea.class);
        NamedArea middleEurope = HibernateProxyHelper.deproxy(termDao.load(middleEuropeUuid), NamedArea.class);
        NamedArea africa = HibernateProxyHelper.deproxy(termDao.load(africaUuid), NamedArea.class);
        NamedArea germany = HibernateProxyHelper.deproxy(termDao.load(germanyUuid), NamedArea.class);

        TaxonNodeFilter filter = new TaxonNodeFilter(europe);
        List<UUID> listUuid = filterDao.listUuids(filter);

        assertEquals(message, 3, listUuid.size());
        Assert.assertTrue(listUuid.contains(node1.getUuid()));
        Assert.assertTrue(listUuid.contains(node2.getUuid()));
        Assert.assertTrue(listUuid.contains(node3.getUuid()));
        Assert.assertFalse(listUuid.contains(node4.getUuid())); //status is absent

        filter = new TaxonNodeFilter(germany);
        listUuid = filterDao.listUuids(filter);
        assertEquals(message, 1, listUuid.size());
        Assert.assertTrue(listUuid.contains(node3.getUuid()));

        filter = new TaxonNodeFilter(middleEurope);
        listUuid = filterDao.listUuids(filter);
        assertEquals(message, 1, listUuid.size());
        Assert.assertTrue(listUuid.contains(node3.getUuid()));

        filter = new TaxonNodeFilter(africa);
        listUuid = filterDao.listUuids(filter);
        assertEquals(message, 0, listUuid.size());
    }

//    @Test
//    public void testListUuidsByAreasWithAncestor() {
//        String message = "wrong number of nodes filtered";
////        System.out.println("start:" + new DateTime().toString());
//
//        NamedArea europe = HibernateProxyHelper.deproxy(termDao.load(europeUuid), NamedArea.class);
//        NamedArea middleEurope = HibernateProxyHelper.deproxy(termDao.load(middleEuropeUuid), NamedArea.class);
//        NamedArea africa = HibernateProxyHelper.deproxy(termDao.load(africaUuid), NamedArea.class);
//        NamedArea germany = HibernateProxyHelper.deproxy(termDao.load(germanyUuid), NamedArea.class);
//
//        TaxonNodeFilter filter = new TaxonNodeFilter(europe);
//        List<String> listTreeIndex = filterDao.listTreeIndex(filter);
//
//        assertEquals(message, 3, listTreeIndex.size());
//        Assert.assertTrue(listTreeIndex.contains(node1.getUuid()));
//        Assert.assertTrue(listTreeIndex.contains(node2.getUuid()));
//        Assert.assertTrue(listTreeIndex.contains(node3.getUuid()));
//        Assert.assertFalse(listTreeIndex.contains(node4.getUuid())); //status is absent
//
//        filter = new TaxonNodeFilter(germany);
//        List<UUID> listUuid = filterDao.listUuids(filter);
//        assertEquals(message, 1, listUuid.size());
//        Assert.assertTrue(listUuid.contains(node3.getUuid()));
//
//        filter = new TaxonNodeFilter(middleEurope);
//        listUuid = filterDao.listUuids(filter);
//        assertEquals(message, 1, listUuid.size());
//        Assert.assertTrue(listUuid.contains(node3.getUuid()));
//
//        filter = new TaxonNodeFilter(africa);
//        listUuid = filterDao.listUuids(filter);
//        assertEquals(message, 0, listUuid.size());
//
//    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    })
    public void testListUuidsByRank() {
        String message = "wrong number of nodes filtered";
        TaxonNodeFilter filter = TaxonNodeFilter.NewInstance();
        List<UUID> listUuid = filterDao.listUuids(filter);
        assertEquals(message, 5, listUuid.size());  //test start condition without rank filter

        filter = TaxonNodeFilter.NewRankInstance(Rank.SPECIES(), Rank.GENUS());
        listUuid = filterDao.listUuids(filter);
        assertEquals(message, 2, listUuid.size());
        Assert.assertTrue(listUuid.contains(node1.getUuid()));
        Assert.assertTrue(listUuid.contains(node3.getUuid()));

        filter = TaxonNodeFilter.NewRankInstance(Rank.SPECIES(), Rank.KINGDOM());
        listUuid = filterDao.listUuids(filter);
        assertEquals(message, 3, listUuid.size());
        Assert.assertTrue(listUuid.contains(node1.getUuid()));
        Assert.assertTrue(listUuid.contains(node2.getUuid()));
        Assert.assertTrue(listUuid.contains(node3.getUuid()));

        filter = TaxonNodeFilter.NewRankInstance(Rank.FAMILY(), Rank.FAMILY());
        listUuid = filterDao.listUuids(filter);
        assertEquals(message, 1, listUuid.size());
        Assert.assertTrue(listUuid.contains(node2.getUuid()));

        filter = TaxonNodeFilter.NewRankInstance(Rank.VARIETY(), Rank.SPECIES());
        listUuid = filterDao.listUuids(filter);
        assertEquals(message, 3, listUuid.size());
        Assert.assertTrue(listUuid.contains(node3.getUuid()));
        Assert.assertTrue(listUuid.contains(node4.getUuid()));
        Assert.assertTrue(listUuid.contains(node5.getUuid()));

        filter = TaxonNodeFilter.NewRankInstance(Rank.KINGDOM(), Rank.ORDER());
        listUuid = filterDao.listUuids(filter);
        assertEquals(message, 0, listUuid.size());

        //reset
        Rank nullRank = null;
        filter.setRankMax(nullRank).setRankMin(nullRank);
        listUuid = filterDao.listUuids(filter);
        assertEquals("Reseting the rank filters should work", 5, listUuid.size());

        filter = TaxonNodeFilter.NewRankInstance(Rank.KINGDOM(), Rank.ORDER());
        UUID nullUuid = null;
        filter.setRankMax(nullUuid).setRankMin(nullUuid);
        listUuid = filterDao.listUuids(filter);
        assertEquals("Reseting the rank filters should work", 5, listUuid.size());
    }

    @Test
    public void testListUuidsBySubtree() {
        Classification classification = classificationDao.findByUuid(classification1.getUuid());
        TaxonNodeFilter filter = TaxonNodeFilter.NewSubtreeInstance(node1);
        List<UUID> listUuid = filterDao.listUuids(filter);
//      List<TaxonNode> children = taxonNodeDao.listChildrenOf(node1, null, null, null, true);
        Assert.assertEquals("All 4 children should be returned", 4, listUuid.size());
        Assert.assertTrue(listUuid.contains(node4.getUuid()));
        Assert.assertFalse(listUuid.contains(node2.getUuid()));
        Assert.assertFalse(listUuid.contains(classification.getRootNode().getUuid()));

        filter = TaxonNodeFilter.NewSubtreeInstance(classification.getRootNode());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("All 5 children but not root node should be returned", 5, listUuid.size());

        filter.setIncludeRootNodes(true);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("All 6 children including root node should be returned", 6, listUuid.size());

        filter = TaxonNodeFilter.NewSubtreeInstance(node3);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("All 3 children should be returned", 3, listUuid.size());

        filter.orSubtree(node2);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("All 3 children and node 2 should be returned", 4, listUuid.size());
        Assert.assertTrue(listUuid.contains(node2.getUuid()));

        filter = TaxonNodeFilter.NewSubtreeInstance(node1).notSubtree(node4);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("Node and 2 children but not node4 should be returned", 3, listUuid.size());
        Assert.assertFalse(listUuid.contains(node4.getUuid()));

        //uuids
        filter = TaxonNodeFilter.NewSubtreeInstance(node3.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("All 3 children should be returned", 3, listUuid.size());

        filter = TaxonNodeFilter.NewSubtreeInstance(taxon1.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("A NON subtree uuid should not return a result", 0, listUuid.size());

    }

    @Test
    public void testIncludeUnpublished(){
        Classification classification = classificationDao.findByUuid(classification1.getUuid());
        TaxonNodeFilter filter = TaxonNodeFilter.NewSubtreeInstance(classification.getRootNode());
        List<UUID> listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("All 5 children but not root node should be returned", 5, listUuid.size());

        filter.setIncludeUnpublished(true);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("All 6 children including unpublished should be returned", 6, listUuid.size());

        filter.setIncludeRootNodes(true);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("All 7 children including root node should be returned", 7, listUuid.size());
    }

    @Test
    public void testListUuidsByClassification() {
        Classification classification = classificationDao.findByUuid(classification1.getUuid());

        TaxonNodeFilter filter;
        List<UUID> listUuid;

        filter = TaxonNodeFilter.NewClassificationInstance(classification);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("All 5 children but not root node should be returned", 5, listUuid.size());

        filter.setIncludeRootNodes(true);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("All 6 children including root node should be returned", 6, listUuid.size());

        filter = TaxonNodeFilter.NewClassificationInstance(classification.getUuid())
                .setIncludeRootNodes(true);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("All 6 children should be returned", 6, listUuid.size());

        filter = TaxonNodeFilter.NewClassificationInstance(taxon1.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("A NON classification uuid should not return a result", 0, listUuid.size());

    }

    @Test
    public void testListUuidsByTaxon() {

        TaxonNodeFilter filter;
        List<UUID> listUuid;

        filter = TaxonNodeFilter.NewTaxonInstance(taxon1);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("The 1 taxon should be returned", 1, listUuid.size());

        filter = TaxonNodeFilter.NewTaxonInstance(taxon1.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("The 1 taxon should be returned", 1, listUuid.size());

        filter = TaxonNodeFilter.NewTaxonInstance(taxon1.getUuid()).orTaxon(taxon2.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("The 2 taxa should be returned", 2, listUuid.size());

        filter = TaxonNodeFilter.NewTaxonInstance(node1.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("A NON taxon uuid should not return a result", 0, listUuid.size());
    }

    @Test
    public void testListUuidsByTaxonNode() {

        TaxonNodeFilter filter;
        List<UUID> listUuid;

        filter = TaxonNodeFilter.NewTaxonNodeInstance(node1);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("The 1 taxon should be returned", 1, listUuid.size());

        filter = TaxonNodeFilter.NewTaxonNodeInstance(node1.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("The 1 nodes should be returned", 1, listUuid.size());

        filter = TaxonNodeFilter.NewTaxonNodeInstance(node1.getUuid())
                .orTaxonNode(node2.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("The 2 nodes should be returned", 2, listUuid.size());

        filter = TaxonNodeFilter.NewTaxonNodeInstance(taxon1.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("A NON taxon node uuid should not return a result", 0, listUuid.size());

    }

    @Test
    public void testListUuidsCombined() {
        Classification classification = classificationDao.findByUuid(classification1.getUuid());
        TaxonNodeFilter filter = TaxonNodeFilter.NewSubtreeInstance(node1);
        List<UUID> listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("All 4 children should be returned", 4, listUuid.size());

        filter.orClassification(classification.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("Still 4 children should be returned", 4, listUuid.size());

        filter.orTaxon(taxon3).orTaxon(taxon4);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("The 2 children should be returned", 2, listUuid.size());

        filter.orTaxonNode(node3.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("1 node should remain", 1, listUuid.size());

        //New
        filter = TaxonNodeFilter.NewSubtreeInstance(node1);  //4 children, see above
        filter.orClassification(classification.getUuid());//4 children, see above

        filter.setRankMax(Rank.uuidSpecies);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("3 children should be returned", 3, listUuid.size());

        filter.setRankMin(Rank.uuidSpecies);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("Only species should be returned", 1, listUuid.size());

        Rank nullRank = null;
        filter.setRankMin(nullRank);
        filter.setIncludeUnpublished(true);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("4 children should be returned, including unpublished", 4, listUuid.size());

        NamedArea germany = HibernateProxyHelper.deproxy(termDao.load(germanyUuid), NamedArea.class);
        filter.orArea(germany);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("1 child should be returned", 1, listUuid.size());
    }

    @Test
    public void testCountBySubtree() {
        Classification classification = classificationDao.findByUuid(classification1.getUuid());
        TaxonNodeFilter filter = TaxonNodeFilter.NewSubtreeInstance(node1);
        long n = filterDao.count(filter);
        Assert.assertEquals("All 4 children should be returned", 4, n);

        filter = TaxonNodeFilter.NewSubtreeInstance(classification.getRootNode());
        n = filterDao.count(filter);
        Assert.assertEquals("All 5 children but not root node should be returned", 5, n);

        filter.setIncludeRootNodes(true);
        n = filterDao.count(filter);
        Assert.assertEquals("All 6 children including root node should be returned", 6, n);
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}

}
