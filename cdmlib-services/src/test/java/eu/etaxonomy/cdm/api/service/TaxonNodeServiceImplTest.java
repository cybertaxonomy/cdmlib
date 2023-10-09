/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.config.PublishForSubtreeConfigurator;
import eu.etaxonomy.cdm.api.service.config.SecundumForSubtreeConfigurator;
import eu.etaxonomy.cdm.api.service.config.SubtreeCloneConfigurator;
import eu.etaxonomy.cdm.api.service.dto.TaxonDistributionDTO;
import eu.etaxonomy.cdm.compare.taxon.TaxonNodeNaturalComparator;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.metadata.SecReferenceHandlingEnum;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author n.hoffmann
 * @since Dec 16, 2010
 */
public class TaxonNodeServiceImplTest extends CdmTransactionalIntegrationTest{

    private static String[] tableNames = new String[]{"CLASSIFICATION","TAXONNODE"};

    @SpringBeanByType
	private ITaxonNodeService taxonNodeService;

	@SpringBeanByType
	private IClassificationService classificationService;

	@SpringBeanByType
	private IReferenceService referenceService;

	@SpringBeanByType
	private IAgentService agentService;

	@SpringBeanByType
	private ITermService termService;

	@SpringBeanByType
    private INameService nameService;

	@SpringBeanByType
	private ITaxonService taxonService;

    @SpringBeanByType
    private IClassificationDao classificationDao;

    @SpringBeanByType
    private IReferenceDao referenceDao;

	@SpringBeanByType
	private IPolytomousKeyService polKeyService;

	@SpringBeanByType
	private IPolytomousKeyNodeService polKeyNodeService;

    private static final UUID uuidRefNewSec = UUID.fromString("1d3fb074-d7ba-47e4-be94-b4cb1a99afa7");

	private static final UUID t1Uuid = UUID.fromString("55c3e41a-c629-40e6-aa6a-ff274ac6ddb1");
	private static final UUID t2Uuid = UUID.fromString("2659a7e0-ff35-4ee4-8493-b453756ab955");
	private static final UUID classificationUuid = UUID.fromString("6c2bc8d9-ee62-4222-be89-4a8e31770878");
	private static final UUID classification2Uuid = UUID.fromString("43d67247-936f-42a3-a739-bbcde372e334");
	private static final UUID referenceUuid = UUID.fromString("de7d1205-291f-45d9-9059-ca83fc7ade14");
	private static final UUID node1Uuid= UUID.fromString("484a1a77-689c-44be-8e65-347d835f47e8");
	private static final UUID node2Uuid = UUID.fromString("2d41f0c2-b785-4f73-a436-cc2d5e93cc5b");

	private static final UUID node4Uuid = UUID.fromString("2fbf7bf5-22dd-4c1a-84e4-c8c93d1f0342");
	private static final UUID node5Uuid = UUID.fromString("c4d5170a-7967-4dac-ab76-ae2019eefde5");
	private static final UUID node6Uuid = UUID.fromString("b419ba5e-9c8b-449c-ad86-7abfca9a7340");
	private static final UUID rootNodeUuid = UUID.fromString("324a1a77-689c-44be-8e65-347d835f4111");
	private static final UUID person1uuid = UUID.fromString("fe660517-8d8e-4dac-8bbb-4fb8f4f4a72e");

	private Taxon t1;
	private Taxon t2;
	private Taxon t4;
	private SynonymType synonymType;
	private Reference reference;
	private String referenceDetail;
	private Classification classification;
	private TaxonNode node1;
	private TaxonNode node2;

    private TaxonNode node4;

	@Test
	@DataSet
	public final void testMakeTaxonNodeASynonymOfAnotherTaxonNode() {
		classification = classificationService.load(classificationUuid);
		node1 = taxonNodeService.load(node1Uuid);
		node2 = taxonNodeService.load(node2Uuid);
		node4 = taxonNodeService.load(node4Uuid);
		reference = referenceService.load(referenceUuid);
		synonymType = SynonymType.HOMOTYPIC_SYNONYM_OF;
		referenceDetail = "test";

		//TODO

		// descriptions
		t1 = node1.getTaxon();
		PolytomousKey polKey = PolytomousKey.NewInstance();
		PolytomousKeyNode keyNode = PolytomousKeyNode.NewInstance("", "", t1, null);
		keyNode.setKey(polKey);
		polKeyNodeService.save(keyNode);
		polKeyService.save(polKey);

		//nameRelations

		TaxonName relatedName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		t1.getName().addRelationshipFromName(relatedName, NameRelationshipType.ALTERNATIVE_NAME(), null, null );

		//taxonRelations
		Taxon relatedTaxon = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES()), null);
		t1.addTaxonRelation(relatedTaxon, TaxonRelationshipType.CONGRUENT_OR_EXCLUDES(), null, null);
		Synonym synonym = Synonym.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES()), null);
		taxonService.save(t1);
		taxonService.save(relatedTaxon);
		nameService.save(relatedName);

		t1.addHomotypicSynonym(synonym);
		taxonService.saveOrUpdate(t1);
		t1 =(Taxon) taxonService.load(t1.getUuid());
		t1 = HibernateProxyHelper.deproxy(t1);
		TaxonName nameT1 = t1.getName();
		t2 = node2.getTaxon();
		assertEquals(2, t1.getDescriptions().size());
		Assert.assertTrue(t2.getSynonyms().isEmpty());
		Assert.assertTrue(t2.getDescriptions().size() == 0);
		assertEquals(2,t1.getSynonyms().size());
		DeleteResult result;

		t4 = node4.getTaxon();
        UUID uuidT4 = t4.getUuid();
        t4 = (Taxon) taxonService.find(uuidT4);
        TaxonName name4 = nameService.find(t4.getName().getUuid());
        result = taxonNodeService.makeTaxonNodeASynonymOfAnotherTaxonNode(node4, node2, synonymType, reference, referenceDetail, null, true);
        if (result.isError() || result.isAbort()){
            Assert.fail();
        }
        t4 = (Taxon)taxonService.find(uuidT4);
        assertNull(t4);

		//Taxon can't be deleted because of the polytomous key node
		result = taxonNodeService.makeTaxonNodeASynonymOfAnotherTaxonNode(node1, node2, synonymType, reference, referenceDetail,null, true);
		if (result.isError() || result.isAbort()){
			Assert.fail();
		}
		commitAndStartNewTransaction(new String[]{/*"TaxonNode"*/});
		t1 = (Taxon)taxonService.find(t1Uuid);
		assertNotNull(t1);//because of the polytomous key node
		node1 = taxonNodeService.load(node1Uuid);
		assertNull(node1);

		Set<CdmBase> updatedObjects = result.getUpdatedObjects();
		Iterator<CdmBase> it = updatedObjects.iterator();
		Taxon taxon;
		while (it.hasNext()) {
			CdmBase updatedObject = it.next();
			if(updatedObject.isInstanceOf(Taxon.class)){
				taxon = HibernateProxyHelper.deproxy(updatedObject, Taxon.class);
				Set<Synonym> syns =  taxon.getSynonyms();
				assertNotNull(syns);
				if (taxon.equals(t2)){
				    assertEquals(4,syns.size());
				    Set<TaxonName> typifiedNames =taxon.getHomotypicGroup().getTypifiedNames();
	                assertEquals(typifiedNames.size(),4);
	                assertTrue(taxon.getHomotypicGroup().equals( nameT1.getHomotypicalGroup()));

	                assertEquals(taxon, t2);
				}

			}

		}
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonNodeServiceImpl#makeTaxonNodeASynonymOfAnotherTaxonNode(eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.taxon.SynonymType, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String)}.
	 */
	@Test
	@DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
	public final void testMakeTaxonNodeAHeterotypicSynonymOfAnotherTaxonNode() {

	    //create data
	    classification = classificationService.load(classificationUuid);
		node1 = taxonNodeService.load(node1Uuid);
		node2 = taxonNodeService.load(node2Uuid);
		reference = referenceService.load(referenceUuid);
		synonymType = SynonymType.HETEROTYPIC_SYNONYM_OF;
		referenceDetail = "test";

		// descriptions
		t1 = node1.getTaxon();
		PolytomousKey polKey = PolytomousKey.NewInstance();
		PolytomousKeyNode keyNode = PolytomousKeyNode.NewInstance("", "", t1, null);
		keyNode.setKey(polKey);
		polKeyNodeService.save(keyNode);
		polKeyService.save(polKey);

		//nameRelations
		TaxonName relatedName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		t1.getName().addRelationshipFromName(relatedName, NameRelationshipType.ALTERNATIVE_NAME(), null, null );
		TaxonName name1 = t1.getName();
		UUID name1UUID = name1.getUuid();
		//taxonRelations
		Taxon relatedTaxon = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES()), null);
		t1.addTaxonRelation(relatedTaxon, TaxonRelationshipType.CONGRUENT_OR_EXCLUDES(), null, null);
		Synonym t1HomotypSynonym = Synonym.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES()), null);

		t1.addHomotypicSynonym(t1HomotypSynonym);
		TaxonName nameT1 = t1.getName();
		t2 = node2.getTaxon();
		assertEquals("taxon 1 must have 2 descriptions", 2, t1.getDescriptions().size());
		assertEquals("taxon 1 must have 2 synonyms", 2, t1.getSynonyms().size());
		Assert.assertTrue("taxon 2 must have no synonyms", t2.getSynonyms().isEmpty());
		Assert.assertTrue("taxon 2 must have no descriptions", t2.getDescriptions().size() == 0);

		//save
		taxonService.save(t1HomotypSynonym);
		taxonService.save(relatedTaxon);
		nameService.save(relatedName);

		//do it
		DeleteResult result = taxonNodeService.makeTaxonNodeASynonymOfAnotherTaxonNode
		        (node1, node2, synonymType, reference, referenceDetail, null, true);

		//post conditions
		if (!result.getUpdatedObjects().iterator().hasNext()){
			Assert.fail("Some updates must have taken place");
		}
		assertEquals(3, result.getUpdatedObjects().size());
		assertNotNull("Old taxon should not have been deleted as it is referenced by key node", taxonService.find(t1Uuid));
		assertNull("Old taxon node should not exist anymore", taxonNodeService.find(node1Uuid));

		t1HomotypSynonym = (Synonym)taxonService.find(t1HomotypSynonym.getUuid());
		assertNotNull(t1HomotypSynonym);

		keyNode.setTaxon(null);
		polKeyNodeService.saveOrUpdate(keyNode);
		t2 =HibernateProxyHelper.deproxy(t2);
		HibernateProxyHelper.deproxy(t2.getHomotypicGroup());
		t2.setName(HibernateProxyHelper.deproxy(t2.getName()));

		assertFalse("taxon 2 must have a synonym now", t2.getSynonyms().isEmpty());
		assertEquals("taxon 2 must have 3 synonyms now, the old taxon 1 and it's 2 synonyms", 3, t2.getSynonyms().size());
		assertEquals("taxon 2 must have 2 descriptions now, taken form taxon 1", 2, t2.getDescriptions().size());

		result = taxonService.deleteTaxon(t1.getUuid(), null, null);
		if (result.isAbort() || result.isError()){
			Assert.fail();
		}
		assertNull(taxonService.find(t1Uuid));
		assertNull(taxonNodeService.find(node1Uuid));
		name1 = nameService.find(name1UUID);
		assertNotNull("taxon name 1 should still exist", name1);
		assertEquals("... but being used for the new synonym only as taxon 1 is deleted", 1, name1.getTaxonBases().size());
		t1HomotypSynonym = (Synonym)taxonService.find(t1HomotypSynonym.getUuid());
		assertNotNull(t1HomotypSynonym);

		Synonym newSynonym =(Synonym) name1.getTaxonBases().iterator().next();

		Taxon newAcceptedTaxon = CdmBase.deproxy(taxonService.find(t2.getUuid()), Taxon.class);
		assertEquals("The new synonym (old accepted taxon) and it's homotypic synonym should still be homotypic", newSynonym.getHomotypicGroup(), t1HomotypSynonym.getName().getHomotypicalGroup());
		assertFalse("The new accepted taxon must not be homotypic to ", newAcceptedTaxon.getHomotypicGroup().equals(newSynonym.getName().getHomotypicalGroup()));

		assertEquals("The new accepted taxon is taxon 2", newAcceptedTaxon, t2);
		assertEquals("The new synonyms name must be the same as the old accepted taxon's name", newSynonym.getName(), nameT1);
	}


	@Test
	@DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonNodeServiceImplTest-indexing.xml")
	public final void testIndexCreateNode() {
		Taxon taxon = Taxon.NewInstance(null, null);
		classification = classificationService.load(classificationUuid);
		node2 = taxonNodeService.load(node2Uuid);
		String oldTreeIndex = node2.treeIndex();

		TaxonNode newNode = node2.addChildTaxon(taxon, null, null);
		taxonNodeService.saveOrUpdate(newNode);
		commitAndStartNewTransaction(new String[]{"TaxonNode"});
		newNode = taxonNodeService.load(newNode.getUuid());
		Assert.assertEquals("", oldTreeIndex + newNode.getId() + "#", newNode.treeIndex());
	}


	@Test
	@DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonNodeServiceImplTest-indexing.xml")
	public final void testIndexMoveNode() {
		//in classification
		classification = classificationService.load(classificationUuid);
		node1 = taxonNodeService.load(node1Uuid);
		node2 = taxonNodeService.load(node2Uuid);
		node2.addChildNode(node1, null, null);
		taxonNodeService.saveOrUpdate(node1);
		commitAndStartNewTransaction(new String[]{"TaxonNode"});
		TaxonNode node6 = taxonNodeService.load(node6Uuid);
		Assert.assertEquals("Node6 treeindex is not correct", node2.treeIndex() + "2#4#6#", node6.treeIndex());

		//root of new classification
		Classification classification2 = classificationService.load(classification2Uuid);
		node1 = taxonNodeService.load(node1Uuid);
		classification2.setRootNode(HibernateProxyHelper.deproxy(classification2.getRootNode(),TaxonNode.class));
		classification2.addChildNode(node1, null, null);
		taxonNodeService.saveOrUpdate(node1);
		commitAndStartNewTransaction(new String[]{"TaxonNode"});
		node1 = taxonNodeService.load(node1Uuid);
		Assert.assertEquals("Node1 treeindex is not correct", "#t2#8#2#", node1.treeIndex());
		node6 = taxonNodeService.load(node6Uuid);
		Assert.assertEquals("Node6 treeindex is not correct", "#t2#8#2#4#6#", node6.treeIndex());

		//into new classification
		node2 = taxonNodeService.load(node2Uuid);
		TaxonNode node5 = taxonNodeService.load(node5Uuid);
		node2 =node5.addChildNode(node2, null, null);
		taxonNodeService.saveOrUpdate(node2);
		commitAndStartNewTransaction(new String[]{"TaxonNode"});
		node2 = taxonNodeService.load(node2Uuid);
		Assert.assertEquals("Node3 treeindex is not correct", "#t2#8#2#5#3#", node2.treeIndex());

}

	@Test
	@DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonNodeServiceImplTest-indexing.xml")
	public final void testIndexDeleteNode() {
		commitAndStartNewTransaction(new String[]{"TaxonNode"});
		node1 = taxonNodeService.load(node1Uuid);
		TaxonNode node4 = taxonNodeService.load(node4Uuid);
		String treeIndex = node1.treeIndex();
		TaxonNode node6 = taxonNodeService.load(node6Uuid);
		treeIndex= node6.treeIndex();

		HibernateProxyHelper.deproxy(node1, TaxonNode.class);
		node1.deleteChildNode(node4, false);
		TaxonNode node5 = taxonNodeService.load(node5Uuid);
		treeIndex = node5.treeIndex();

		node6 = taxonNodeService.load(node6Uuid);

		treeIndex = node6.treeIndex();
		Taxon newTaxon= Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES()), null);
		UUID taxonNewUuid = taxonService.save(newTaxon).getUuid();

		node5.addChildTaxon(newTaxon, null, null);
		String node5TreeIndex =node5.treeIndex();
		taxonNodeService.saveOrUpdate(node5);

		commitAndStartNewTransaction(new String[]{"TaxonNode"});
		node5 = taxonNodeService.load(node5Uuid);
		List<TaxonNode> children =  node5.getChildNodes();
		TaxonNode node = children.get(0);
		int id = node.getId();
		Assert.assertEquals("Node6 treeindex is not correct", "#t1#1#2#6#", treeIndex);
		Assert.assertEquals("new node treeindex is not correct", node5TreeIndex + id +"#", node.treeIndex());
	}


	@Test
	@DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
	public final void testDeleteNode(){
		classification = classificationService.load(classificationUuid);
		node1 = taxonNodeService.load(node1Uuid);
		node2 = taxonNodeService.load(rootNodeUuid);
		node1 = HibernateProxyHelper.deproxy(node1);

		TaxonNode newNode = node1.addChildTaxon(Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES()), null), null, null);
		UUID uuidNewNode = taxonNodeService.save(newNode).getUuid();
		newNode = taxonNodeService.load(uuidNewNode);
		UUID taxUUID = newNode.getTaxon().getUuid();
		UUID nameUUID = newNode.getTaxon().getName().getUuid();

		DeleteResult result = taxonNodeService.deleteTaxonNode(node1, null);
		if (!result.isOk()){
			Assert.fail();
		}
		newNode = taxonNodeService.load(uuidNewNode);
		node1 = taxonNodeService.load(node1Uuid);
		assertNull(newNode);
		assertNull(node1);

		t1 = (Taxon) taxonService.load(t1Uuid);
		assertNull(t1);
		Taxon newTaxon = (Taxon)taxonService.load(taxUUID);
		assertNull(newTaxon);
		IBotanicalName name = nameService.load(nameUUID);
		assertNull(name);


	}

	@Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)

    public final void testDeleteNodeWithReusedTaxon(){
        classification = classificationService.load(classificationUuid);
        node1 = taxonNodeService.load(node1Uuid);
        node2 = taxonNodeService.load(rootNodeUuid);
        node1 = HibernateProxyHelper.deproxy(node1);


        Classification classification2 = Classification.NewInstance("Classification2");
        TaxonNode nodeClassification2 =classification2.addChildTaxon(node1.getTaxon(), null, null);
        assertEquals(node1.getTaxon().getUuid(), t1Uuid);
        classificationService.save(classification2);
        List<TaxonNode> nodesOfClassification2 = taxonNodeService.listAllNodesForClassification(classification2, null, null);
        UUID nodeUUID = nodesOfClassification2.get(0).getUuid();
        assertEquals(nodeUUID, nodeClassification2.getUuid());
        Taxon newTaxon = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES()),  null);
        taxonService.save(newTaxon);
        TaxonNode newNode = node1.addChildTaxon(newTaxon,null, null);
        UUID uuidNewNode = taxonNodeService.save(newNode).getUuid();
        newNode = taxonNodeService.load(uuidNewNode);
        UUID taxUUID = newNode.getTaxon().getUuid();
        UUID nameUUID = newNode.getTaxon().getName().getUuid();

        DeleteResult result = taxonNodeService.deleteTaxonNode(node1, null);
        if (!result.isOk()){
            Assert.fail();
        }
        //taxonService.getSession().flush();
        newNode = taxonNodeService.load(uuidNewNode);
        node1 = taxonNodeService.load(node1Uuid);
        assertNull(newNode);
        assertNull(node1);
        List<String> propertyPath = new ArrayList<>();
        propertyPath.add("taxon.name.*");
        nodeClassification2 =taxonNodeService.load(nodeUUID, propertyPath);
        assertNotNull(nodeClassification2);
        assertNotNull(nodeClassification2.getTaxon());
        assertNotNull(nodeClassification2.getTaxon().getName());

        t1 = (Taxon) taxonService.load(t1Uuid);
        assertNotNull(t1);
        newTaxon = (Taxon)taxonService.load(taxUUID);
        assertNull(newTaxon);
        IBotanicalName name = nameService.load(nameUUID);
        assertNull(name);
    }

	@Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
	public final void testDeleteNodes(){
		classification = classificationService.load(classificationUuid);
		node1 = taxonNodeService.load(node1Uuid);
		node2 = taxonNodeService.load(rootNodeUuid);
		node1 = HibernateProxyHelper.deproxy(node1);
		node2 = HibernateProxyHelper.deproxy(node2);
		TaxonNode newNode = node1.addChildTaxon(Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES()), null), null, null);
		UUID uuidNewNode = taxonNodeService.save(newNode).getUuid();
		List<TaxonNode> treeNodes = new ArrayList<>();
		treeNodes.add(node1);
		treeNodes.add(node2);

		taxonNodeService.deleteTaxonNodes(treeNodes, null);

		newNode = taxonNodeService.load(uuidNewNode);
		node1 = taxonNodeService.load(node1Uuid);
		assertNull(newNode);
		assertNull(node1);
		//taxonService.getSession().flush();
		t1 = (Taxon) taxonService.load(t1Uuid);
		assertNull(t1);
		t2 = (Taxon) taxonService.load(t2Uuid);
		assertNull(t2);
	}

	@Test
	@DataSet
	public void testMoveTaxonNode(){
	    classification = classificationService.load(classificationUuid);
	  //  Set<TaxonNode>  nodes = classification.getAllNodes();
	    List<TaxonNode>  nodes = classification.getChildNodes();
	    System.out.println(nodes.size());
	    classification.addChildTaxon(Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES()), null), nodes.size(), null, null);
	    nodes =  classification.getChildNodes();
	    System.out.println(nodes.size());
	}

    @Test
    public void testCompareNaturalOrder() {
    	/*
    	 * Classification
    	 *  * Abies
    	 *  `- Abies alba
    	 *   - Abies balsamea
    	 *  * Pinus
    	 *  `- Pinus pampa
    	 */
    	Classification classification = Classification.NewInstance("Classification");
    	IBotanicalName abiesName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
    	abiesName.setGenusOrUninomial("Abies");
    	Taxon abies = Taxon.NewInstance(abiesName, null);
    	IBotanicalName abiesAlbaName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
    	abiesAlbaName.setGenusOrUninomial("Abies");
    	abiesAlbaName.setSpecificEpithet("alba");
    	Taxon abiesAlba = Taxon.NewInstance(abiesAlbaName, null);
    	IBotanicalName pinusName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
    	pinusName.setGenusOrUninomial("Pinus");
    	Taxon pinus = Taxon.NewInstance(pinusName, null);
    	IBotanicalName pinusPampaName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
    	pinusPampaName.setGenusOrUninomial("Pinus");
    	pinusPampaName.setSpecificEpithet("pampa");
    	Taxon pinusPampa = Taxon.NewInstance(pinusPampaName, null);

        IBotanicalName abiesBalsameaName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        abiesBalsameaName.setGenusOrUninomial("Abies");
        abiesBalsameaName.setSpecificEpithet("balsamea");
        Taxon abiesBalsamea = Taxon.NewInstance(abiesBalsameaName, null);

        List<TaxonNode> nodes = new ArrayList<>();
    	nodes.add(classification.addChildTaxon(abies, null, null));
    	TaxonNode abiesAlbaNode = classification.addParentChild(abies, abiesAlba, null, null);
    	TaxonNode balsameaNode = classification.addParentChild(abies, abiesBalsamea, null, null);
    	nodes.add(balsameaNode);
    	nodes.add(abiesAlbaNode);
    	nodes.add(classification.addChildTaxon(pinus, null, null));
    	nodes.add(classification.addParentChild(pinus, pinusPampa, null, null));
    	this.taxonNodeService.save(nodes);
    	classificationService.saveClassification(classification);
    	commitAndStartNewTransaction();

    	classification = classificationDao.load(classification.getUuid());
    	TaxonNodeNaturalComparator comparator = new TaxonNodeNaturalComparator();
    	List<TaxonNode> allNodes = new ArrayList<>(classification.getAllNodes());
    	Collections.sort(allNodes, comparator);

    	Assert.assertEquals(allNodes.get(0).getTaxon(), abies );
    	Assert.assertEquals(allNodes.get(2).getTaxon(), abiesBalsamea );
    	Assert.assertEquals(allNodes.get(1).getTaxon(), abiesAlba );

    	taxonNodeService.moveTaxonNode(balsameaNode.getUuid(), abiesAlbaNode.getUuid(),1, SecReferenceHandlingEnum.KeepOrWarn, null);
    	commitAndStartNewTransaction();

    	classification = classificationService.load(classification.getUuid());
    	allNodes = new ArrayList<>(classification.getAllNodes());
        Collections.sort(allNodes, comparator);

        Assert.assertEquals(abies, allNodes.get(0).getTaxon());
        Assert.assertEquals(abiesBalsamea, allNodes.get(1).getTaxon());
        Assert.assertEquals(abiesAlba, allNodes.get(2).getTaxon());
    }

    @Test
    @DataSet(loadStrategy = CleanSweepInsertLoadStrategy.class, value = "TaxonNodeServiceImplTest.testGetUuidAndTitleCacheHierarchy.xml")
    public void testGetUuidAndTitleCacheHierarchy(){
        UUID classificationUuid = UUID.fromString("029b4c07-5903-4dcf-87e8-406ed0e0285f");
        UUID abiesUuid = UUID.fromString("f8306fd3-9825-41bf-94aa-a7b5790b553e");
        UUID abiesAlbaUuid = UUID.fromString("c70f76e5-2dcb-41c5-ae6f-d756e0a0fae0");
        UUID abiesAlbaSubBrotaUuid = UUID.fromString("06d58161-7707-44b5-b720-6c0eb916b37c");
        UUID abiesPalmaUuid = UUID.fromString("6dfd30dd-e589-493a-b66a-19c4cb374f92");
        UUID pinusUuid = UUID.fromString("5d8e8341-f5e9-4616-96cf-f0351dda42f4");
//        /*
//         * Checklist
//         *  - Abies
//         *   - Abies alba
//         *    - Abieas alba subs. brota
//         *   - Abies palma
//         *  -Pinus
//         */
//        Classification checklist = Classification.NewInstance("Checklist");
//        checklist.setUuid(classificationUuid);
//
//        BotanicalName abiesName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
//        abiesName.setGenusOrUninomial("Abies");
//        Taxon abies = Taxon.NewInstance(abiesName, null);
//
//        BotanicalName abiesAlbaName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
//        abiesAlbaName.setGenusOrUninomial("Abies");
//        abiesAlbaName.setSpecificEpithet("alba");
//        Taxon abiesAlba = Taxon.NewInstance(abiesAlbaName, null);
//
//        BotanicalName abiesAlbaSubBrotaName = TaxonNameFactory.NewBotanicalInstance(Rank.SUBSPECIES());
//        abiesAlbaSubBrotaName.setGenusOrUninomial("Abies");
//        abiesAlbaSubBrotaName.setSpecificEpithet("alba");
//        abiesAlbaSubBrotaName.setInfraSpecificEpithet("brota");
//        Taxon abiesAlbaSubBrota = Taxon.NewInstance(abiesAlbaSubBrotaName, null);
//
//        BotanicalName abiesPalmaName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
//        abiesPalmaName.setGenusOrUninomial("Abies");
//        abiesPalmaName.setSpecificEpithet("palma");
//        Taxon abiesPalma = Taxon.NewInstance(abiesPalmaName, null);
//
//        BotanicalName pinusName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
//        pinusName.setGenusOrUninomial("Pinus");
//        Taxon pinus = Taxon.NewInstance(pinusName, null);
//
//        checklist.addParentChild(null, abies, null, null);
//        checklist.addParentChild(abies, abiesAlba, null, null);
//        checklist.addParentChild(abiesAlba, abiesAlbaSubBrota, null, null);
//        checklist.addParentChild(abies, abiesPalma, null, null);
//        checklist.addParentChild(null, pinus, null, null);
//
//
//        setComplete();
//        endTransaction();
//
//        String fileNameAppendix = "testGetUuidAndTitleCacheHierarchy";
//
//        writeDbUnitDataSetFile(new String[] {
//            "TAXONBASE", "TAXONNAME",
//            "TAXONRELATIONSHIP",
//            "HOMOTYPICALGROUP",
//            "CLASSIFICATION", "TAXONNODE",
//            "HIBERNATE_SEQUENCES" // IMPORTANT!!!
//            },
//            fileNameAppendix );
        Classification classification = classificationService.load(classificationUuid);

        List<TaxonNode> expectedChildTaxonNodes = classification.getChildNodes();
        List<TaxonNodeDto> childNodesUuidAndTitleCache = taxonNodeService.listChildNodesAsTaxonNodeDto(classification.getRootNode());
        assertNotNull("child UuidAndTitleCache list is null", childNodesUuidAndTitleCache);

        compareChildren(expectedChildTaxonNodes, childNodesUuidAndTitleCache);

        //test taxon parent of sub species
        Taxon abiesAlbaSubBrota = HibernateProxyHelper.deproxy(taxonService.load(abiesAlbaSubBrotaUuid), Taxon.class);
        TaxonNode abiesAlbaSubBrotaNode = abiesAlbaSubBrota.getTaxonNodes().iterator().next();
        TaxonNode expectedTaxonParent = HibernateProxyHelper.deproxy(abiesAlbaSubBrotaNode.getParent(), TaxonNode.class);
        TaxonNodeDto taxonParent = taxonNodeService.getParentUuidAndTitleCache(abiesAlbaSubBrotaNode);
        assertEquals("Taxon Nodes do not match. ", expectedTaxonParent.getUuid(), taxonParent.getUuid());
        assertEquals("Taxon Nodes do not match. ", (Integer)expectedTaxonParent.getId(), taxonParent.getId());
        assertEquals("Taxon Nodes do not match. ", expectedTaxonParent.getTaxon().getTitleCache(), taxonParent.getTitleCache());
        assertEquals("Taxon Nodes do not match. ", expectedTaxonParent, taxonNodeService.load(taxonParent.getUuid()));

        //test classification parent
        Taxon abies = HibernateProxyHelper.deproxy(taxonService.load(abiesUuid), Taxon.class);
        TaxonNode abiesNode = abies.getTaxonNodes().iterator().next();
        TaxonNode expectedClassificationParent = HibernateProxyHelper.deproxy(abiesNode.getParent(), TaxonNode.class);
        TaxonNodeDto classificationParent= taxonNodeService.getParentUuidAndTitleCache(abiesNode);
        assertEquals("Taxon Nodes do not match. ", expectedClassificationParent.getUuid(), classificationParent.getUuid());
        assertEquals("Taxon Nodes do not match. ", (Integer)expectedClassificationParent.getId(), classificationParent.getId());
        assertEquals("Taxon Nodes do not match. ", expectedClassificationParent.getClassification().getTitleCache(), classificationParent.getTitleCache());
        assertEquals("Taxon Nodes do not match. ", expectedClassificationParent, taxonNodeService.load(classificationParent.getUuid()));
    }

    private void compareChildren(List<TaxonNode> expectedChildTaxonNodes, List<TaxonNodeDto> childNodesDto){
        assertEquals("Number of children does not match", expectedChildTaxonNodes.size(), childNodesDto.size());
        TaxonNodeDto foundMatch = null;
        for (TaxonNode taxonNode : expectedChildTaxonNodes) {
            foundMatch = null;
            for (TaxonNodeDto uuidAndTitleCache : childNodesDto) {
                if(uuidAndTitleCache.getUuid().equals(taxonNode.getUuid())){
                    Taxon taxon = HibernateProxyHelper.deproxy(taxonNode.getTaxon(), Taxon.class);
                    String titleCache = taxon.getTitleCache();
                    if(uuidAndTitleCache.getTaxonTitleCache().equals(titleCache)){
                        foundMatch = uuidAndTitleCache;
                        break;
                    }
                }
            }
            assertTrue(String.format("no matching UuidAndTitleCache found for child %s", taxonNode), foundMatch!=null);
            compareChildren(taxonNode.getChildNodes(), taxonNodeService.listChildNodesAsTaxonNodeDto(foundMatch));
        }
    }
//
//    private UuidAndTitleCache<TaxonNode> findMatchingUuidAndTitleCache(List<UuidAndTitleCache<TaxonNode>> childNodesUuidAndTitleCache,
//            UuidAndTitleCache<TaxonNode> foundMatch, TaxonNode taxonNode) {
//        for (UuidAndTitleCache<TaxonNode> uuidAndTitleCache : childNodesUuidAndTitleCache) {
//            if(uuidAndTitleCache.getUuid().equals(taxonNode.getUuid())){
//                String titleCache = taxonNode.getTaxon().getTitleCache();
//                if(uuidAndTitleCache.getTitleCache().equals(titleCache)){
//                    foundMatch = uuidAndTitleCache;
//                    break;
//                }
//            }
//        }
//        return foundMatch;
//    }

    @Test
    @DataSet("TaxonNodeServiceImplTest.testSetSecundumForSubtree.xml")
    public void testSetSecundumForSubtree(){
        Reference newSec = referenceService.find(uuidRefNewSec);
        Reference refSpPl = referenceService.find(referenceUuid);

        //assert current state
        Assert.assertNotNull(newSec);
        Assert.assertNull(taxonService.find(1).getSec());
        Assert.assertNull(taxonService.find(2).getSec());
        Assert.assertNull(taxonService.find(3).getSec());
        Assert.assertNull(taxonService.find(4).getSec());
        Taxon taxon5 = (Taxon)taxonService.find(5);
        Assert.assertNotNull(taxon5.getSec());
        Assert.assertNotEquals(newSec, taxon5.getSec());
        Assert.assertNotNull(taxon5.getSecMicroReference());
        Assert.assertEquals(1, taxon5.getMisappliedNameRelations().size());
        Assert.assertEquals(refSpPl, taxon5.getMisappliedNameRelations().iterator().next().getSource().getCitation());
        Assert.assertEquals(1, taxon5.getProParteAndPartialSynonymRelations().size());
        Assert.assertNull(taxon5.getProParteAndPartialSynonymRelations().iterator().next().getSource());

        //set secundum
        SecundumForSubtreeConfigurator config = new SecundumForSubtreeConfigurator(node1Uuid, null, null, true);
        config.setIncludeMisapplications(true);
        config.setIncludeProParteSynonyms(true);
        config.setNewSecundum(newSec);
        UpdateResult result = taxonNodeService.setSecundumForSubtree(config);
        Assert.assertTrue(result.getExceptions().isEmpty());
        Assert.assertTrue(result.isOk());
        Assert.assertEquals(6, result.getUpdatedObjects().size());  //should be 5 without workaround for #9627,#6359

        commitAndStartNewTransaction(/*new String[]{"TaxonBase","TaxonBase_AUD"}*/);
        Assert.assertEquals(newSec, taxonService.find(1).getSec());
        Assert.assertNull(taxonService.find(2).getSec());
        Assert.assertEquals(newSec, taxonService.find(3).getSec());
        Assert.assertNull(taxonService.find(4).getSec());
        taxon5 = (Taxon)taxonService.find(5);
        Assert.assertEquals(newSec, taxon5.getSec());
        Assert.assertNull(taxon5.getSecMicroReference());
        Assert.assertEquals(newSec, taxon5.getMisappliedNameRelations().iterator().next().getSource().getCitation());
        Assert.assertEquals(newSec, taxon5.getProParteAndPartialSynonymRelations().iterator().next().getSource().getCitation());
        Taxon taxon2 = (Taxon)taxonService.find(2);
        Assert.assertEquals(1, taxon2.getProParteAndPartialSynonymRelations().size());
        Assert.assertNull(taxon2.getProParteAndPartialSynonymRelations().iterator().next().getSource());

        result = taxonNodeService.setSecundumForSubtree(config);
        Assert.assertTrue(result.getExceptions().isEmpty() && result.isOk());
        Assert.assertEquals(0, result.getUpdatedObjects().size());

    }

    @Test
    @DataSet("TaxonNodeServiceImplTest.testSetSecundumForSubtree.xml")
    public void testSetSecundumForSubtreeNoOverwrite(){
        Reference newSec = referenceService.find(uuidRefNewSec);
        Reference refSpPl = referenceService.find(referenceUuid);

        //assert current state
        Assert.assertNotNull(newSec);
        Assert.assertNull(taxonService.find(1).getSec());
        Assert.assertNull(taxonService.find(2).getSec());
        Assert.assertNull(taxonService.find(3).getSec());
        Assert.assertNull(taxonService.find(4).getSec());
        Taxon taxon5 = (Taxon)taxonService.find(5);
        Assert.assertNotNull(taxon5.getSec());
        Assert.assertNotEquals(newSec, taxon5.getSec());
        Assert.assertNotNull(taxon5.getSecMicroReference());
        Assert.assertEquals(1, taxon5.getMisappliedNameRelations().size());
        Assert.assertEquals(refSpPl, taxon5.getMisappliedNameRelations().iterator().next().getSource().getCitation());
        Assert.assertEquals(1, taxon5.getProParteAndPartialSynonymRelations().size());
        Assert.assertNull(taxon5.getProParteAndPartialSynonymRelations().iterator().next().getSource());

        //set secundum
        SecundumForSubtreeConfigurator config = new SecundumForSubtreeConfigurator(node1Uuid, newSec, null, true);
        config.setOverwriteExisting(false);
        UpdateResult result = taxonNodeService.setSecundumForSubtree(config);
        Assert.assertTrue(result.getExceptions().isEmpty());
        Assert.assertTrue(result.isOk());
        Assert.assertEquals(4, result.getUpdatedObjects().size()); //should be 3 without workaround for #9627,#6359

        commitAndStartNewTransaction();  //new String[]{"TaxonBase","TaxonBase_AUD"}
        Assert.assertEquals(newSec, taxonService.find(1).getSec());
        Assert.assertNull(taxonService.find(2).getSec());
        Assert.assertEquals(newSec, taxonService.find(3).getSec());
        Assert.assertNull(taxonService.find(4).getSec());
        Reference oldTaxon5Sec = taxon5.getSec();
        taxon5 = (Taxon)taxonService.find(5);
        Assert.assertEquals(oldTaxon5Sec, taxon5.getSec());
        Assert.assertNotEquals(newSec, taxon5.getSec());
        Assert.assertNotNull(taxon5.getSecMicroReference());
        Assert.assertEquals("Should not override MAN source", refSpPl, taxon5.getMisappliedNameRelations().iterator().next().getSource().getCitation());
        Assert.assertEquals(newSec, taxon5.getProParteAndPartialSynonymRelations().iterator().next().getSource().getCitation());
        Taxon taxon2 = (Taxon)taxonService.find(2);
        Assert.assertEquals("taxon2 is not part of subtree therefore should not be updated", 1, taxon2.getProParteAndPartialSynonymRelations().size());
        Assert.assertNull(taxon2.getProParteAndPartialSynonymRelations().iterator().next().getSource());
    }

    @Test
    @DataSet("TaxonNodeServiceImplTest.testSetSecundumForSubtree.xml")
    public void testSetSecundumForSubtreeOnlyAccepted(){
        Reference newSec = referenceService.find(uuidRefNewSec);

        //assert current state
        Assert.assertNotNull(newSec);
        Assert.assertNull(taxonService.find(1).getSec());
        Assert.assertNull(taxonService.find(2).getSec());
        Assert.assertNull(taxonService.find(3).getSec());
        Assert.assertNull(taxonService.find(4).getSec());
        TaxonBase<?> taxon5 = taxonService.find(5);
        Assert.assertNotNull(taxon5.getSec());
        Assert.assertNotEquals(newSec, taxon5.getSec());
        Assert.assertNotNull(taxon5.getSecMicroReference());

        //set secundum
        SecundumForSubtreeConfigurator config = new SecundumForSubtreeConfigurator(node1Uuid, newSec, null, false);
        config.setIncludeSynonyms(false);
        taxonNodeService.setSecundumForSubtree(config);

//        commitAndStartNewTransaction(new String[]{"TaxonBase","TaxonBase_AUD"});
        Assert.assertEquals(newSec, taxonService.find(1).getSec());
        Assert.assertNull(taxonService.find(2).getSec());
        Assert.assertNull(taxonService.find(3).getSec());
        Assert.assertNull(taxonService.find(4).getSec());
        taxon5 = taxonService.find(5);
        Assert.assertEquals(newSec, taxon5.getSec());
        Assert.assertNull(taxon5.getSecMicroReference());
    }

    @Test
    @DataSet("TaxonNodeServiceImplTest.testSetSecundumForSubtree.xml")
    public void testSetSecundumForSubtreeOnlySynonyms(){
        Reference newSec = referenceService.find(uuidRefNewSec);

        //assert current state
        Assert.assertNotNull(newSec);
        Assert.assertNull(taxonService.find(1).getSec());
        Assert.assertNull(taxonService.find(2).getSec());
        Assert.assertNull(taxonService.find(3).getSec());
        Assert.assertNull(taxonService.find(4).getSec());
        TaxonBase<?> taxon5 = taxonService.find(5);
        Assert.assertNotNull(taxon5.getSec());
        Assert.assertNotEquals(newSec, taxon5.getSec());
        Assert.assertNotNull(taxon5.getSecMicroReference());

        //set secundum
        SecundumForSubtreeConfigurator config = new SecundumForSubtreeConfigurator(node1Uuid, newSec, null, false);
        config.setIncludeAcceptedTaxa(false);
        taxonNodeService.setSecundumForSubtree(config);

        commitAndStartNewTransaction(new String[]{"TaxonBase","TaxonBase_AUD"});
        Assert.assertNull(taxonService.find(1).getSec());
        Assert.assertNull(taxonService.find(2).getSec());
        Assert.assertEquals("Synonym should be updated", newSec, taxonService.find(3).getSec());
        Assert.assertNull(taxonService.find(4).getSec());
        Reference oldTaxon5Sec = taxon5.getSec();
        taxon5 = taxonService.find(5);
        Assert.assertEquals(oldTaxon5Sec, taxon5.getSec());
        Assert.assertNotEquals(newSec, taxon5.getSec());
        Assert.assertNotNull(taxon5.getSecMicroReference());
    }

    @Test
    @DataSet("TaxonNodeServiceImplTest.testSetSecundumForSubtree.xml")
    public void testSetSecundumForSubtreeNoShared(){
        Reference newSec = referenceService.find(uuidRefNewSec);

        //assert current state
        Assert.assertNotNull(newSec);
        Assert.assertNull(taxonService.find(1).getSec());
        Assert.assertNull(taxonService.find(2).getSec());
        Assert.assertNull(taxonService.find(3).getSec());
        Assert.assertNull(taxonService.find(4).getSec());
        Taxon taxon5 = (Taxon)taxonService.find(5);
        Assert.assertNotNull(taxon5.getSec());
        Assert.assertNotEquals(newSec, taxon5.getSec());
        Assert.assertNotNull(taxon5.getSecMicroReference());
        Taxon taxon1 = (Taxon)taxonService.find(1);
        taxon1.addMisappliedName(taxon5, null, null);
        Assert.assertEquals(1, taxon1.getMisappliedNameRelations().size());
        commitAndStartNewTransaction();

        //set secundum
        SecundumForSubtreeConfigurator config = new SecundumForSubtreeConfigurator(node1Uuid, newSec, null, true);
        config.setIncludeSharedTaxa(false);
        taxonNodeService.setSecundumForSubtree(config);

        commitAndStartNewTransaction();  //new String[]{"TaxonBase","TaxonBase_AUD"}
        taxon1 = (Taxon)taxonService.find(1);
        Assert.assertNull("Shared taxon must not be set", taxonService.find(1).getSec());
        Assert.assertNull(taxonService.find(2).getSec());
        Assert.assertNull("Synonym of shared taxon must not be set", taxonService.find(3).getSec());
        Assert.assertNull(taxonService.find(4).getSec());
        taxon5 = (Taxon)taxonService.find(5);
        Assert.assertEquals(newSec, taxon5.getSec());
        Assert.assertNull(taxon5.getSecMicroReference());
        Assert.assertNull("without share no citation should be set", taxon1.getMisappliedNameRelations().iterator().next().getSource());
        config.setIncludeSharedTaxa(true);
        taxonNodeService.setSecundumForSubtree(config);
        taxon1 = (Taxon)taxonService.find(1);
        Assert.assertEquals("With shared taxa citation should be set", newSec, taxon1.getMisappliedNameRelations().iterator().next().getSource().getCitation());
    }

    @Test
    @DataSet("TaxonNodeServiceImplTest.testSetSecundumForSubtree.xml")
    public void testSetPublishForSubtree(){

        assertStartingStateForSetPublish();

        boolean publish = false;
        PublishForSubtreeConfigurator config = PublishForSubtreeConfigurator.NewInstance(
                node1Uuid,  publish, null);
        config.setIncludeAcceptedTaxa(true);
        config.setIncludeSynonyms(true);
        config.setIncludeSharedTaxa(true);
        config.setIncludeHybrids(false);
        taxonNodeService.setPublishForSubtree(config);

        commitAndStartNewTransaction();

        Assert.assertEquals(publish, taxonService.find(1).isPublish());
        Assert.assertEquals("Taxon2 is not in subtree", true, taxonService.find(2).isPublish());
        Assert.assertEquals("Synonym3 not yet updated because it is hybrid", true, taxonService.find(3).isPublish());
        Assert.assertEquals("Synonym4 is not in subtree", true, taxonService.find(4).isPublish());
        Assert.assertEquals(true, taxonService.find(5).isPublish());
        Assert.assertEquals(publish, taxonService.find(6).isPublish());
        Assert.assertEquals("Synonym7 is not in subtree", true, taxonService.find(7).isPublish());

        config.setIncludeHybrids(true);
        taxonNodeService.setPublishForSubtree(config);

        commitAndStartNewTransaction();
        Assert.assertEquals(publish, taxonService.find(3).isPublish());
        Assert.assertEquals(publish, taxonService.find(5).isPublish());
        Assert.assertEquals(publish, taxonService.find(7).isPublish());

    }

    @Test
    @DataSet("TaxonNodeServiceImplTest.testSetSecundumForSubtree.xml")
    public void testSetPublishForSubtreeOnlyAccepted(){

        assertStartingStateForSetPublish();

        boolean publish = false;
        PublishForSubtreeConfigurator config = PublishForSubtreeConfigurator.NewInstance(node1Uuid,  publish, null);
        config.setIncludeAcceptedTaxa(true);
        config.setIncludeSynonyms(false);
        config.setIncludeMisapplications(false);
        config.setIncludeProParteSynonyms(false);
        config.setIncludeSharedTaxa(true);
        taxonNodeService.setPublishForSubtree(config);


        commitAndStartNewTransaction();
        Assert.assertEquals(publish, taxonService.find(1).isPublish());
        Assert.assertEquals("Taxon2 not in subtree", true, taxonService.find(2).isPublish());
        Assert.assertEquals("Synonym3 should not be updated", true, taxonService.find(3).isPublish());
        Assert.assertEquals("Synonym3 should not be updated", true, taxonService.find(4).isPublish());
        Assert.assertEquals("Accepted in subtree should be udpated", publish, taxonService.find(5).isPublish());
        Assert.assertEquals("Misapplied should not be updated", true, taxonService.find(6).isPublish());
        Assert.assertEquals("Pro parte synonym should not be updated", true, taxonService.find(7).isPublish());

        config.setIncludeMisapplications(true);
        taxonNodeService.setPublishForSubtree(config);
        commitAndStartNewTransaction();
        Assert.assertEquals("Misapplied should be updated now", publish, taxonService.find(6).isPublish());
        Assert.assertEquals("Pro parte synonym should not yet be updated", true, taxonService.find(7).isPublish());

        config.setIncludeProParteSynonyms(true);
        config.setIncludeHybrids(false);
        taxonNodeService.setPublishForSubtree(config);
        commitAndStartNewTransaction();
        Assert.assertEquals("Pro parte synonym should not yet be updated because it is a hybrid", true, taxonService.find(7).isPublish());

        config.setIncludeHybrids(true);
        taxonNodeService.setPublishForSubtree(config);
        commitAndStartNewTransaction();
        Assert.assertEquals("Pro parte synonym should be updated now", publish, taxonService.find(7).isPublish());

    }

    @Test
    @DataSet("TaxonNodeServiceImplTest.testSetSecundumForSubtree.xml")
    public void testSetPublishForSubtreeOnlySynonyms(){

        //assert current state
        assertStartingStateForSetPublish();

        boolean publish = false;

        PublishForSubtreeConfigurator config = PublishForSubtreeConfigurator.NewInstance(
                node1Uuid, publish, null);
        config.setIncludeAcceptedTaxa(false);
        config.setIncludeSynonyms(true);
        config.setIncludeSharedTaxa(true);  //should have no effect
        config.setIncludeMisapplications(false);
        config.setIncludeProParteSynonyms(false);
        taxonNodeService.setPublishForSubtree(config);

        commitAndStartNewTransaction(new String[]{});
        Assert.assertEquals(true, taxonService.find(1).isPublish());
        Assert.assertEquals(true, taxonService.find(2).isPublish());
        //Synonym should be false
        Assert.assertEquals(publish, taxonService.find(3).isPublish());
        Assert.assertEquals(true, taxonService.find(4).isPublish());
        Assert.assertEquals(true, taxonService.find(5).isPublish());
        Assert.assertEquals(true, taxonService.find(6).isPublish());
        Assert.assertEquals(true, taxonService.find(7).isPublish());

        config.setIncludeMisapplications(true);
        taxonNodeService.setPublishForSubtree(config);
        commitAndStartNewTransaction();
        Assert.assertEquals("Misapplied should be updated now", publish, taxonService.find(6).isPublish());
        Assert.assertEquals("Pro parte synonym should not yet be updated", true, taxonService.find(7).isPublish());

        config.setIncludeProParteSynonyms(true);
        taxonNodeService.setPublishForSubtree(config);
        commitAndStartNewTransaction();
        Assert.assertEquals("Pro parte synonym should be updated now", publish, taxonService.find(7).isPublish());

    }

    private void assertStartingStateForSetPublish() {
        Assert.assertTrue(taxonService.find(1).isPublish());
        Assert.assertTrue(taxonService.find(2).isPublish());
        Assert.assertTrue(taxonService.find(3).isPublish());
        Assert.assertTrue(taxonService.find(4).isPublish());
        Assert.assertTrue(taxonService.find(5).isPublish());
        Assert.assertTrue(taxonService.find(6).isPublish());
        Assert.assertTrue(taxonService.find(7).isPublish());
    }


    @Test
    @DataSet("TaxonNodeServiceImplTest.testSetSecundumForSubtree.xml")
    public void testSetPublishForSubtreeNoShared(){

        assertStartingStateForSetPublish();

        boolean publish = false;
        PublishForSubtreeConfigurator config = PublishForSubtreeConfigurator.NewInstance(
                node1Uuid, publish, null);
        config.setIncludeAcceptedTaxa(true);
        config.setIncludeSynonyms(true);
        config.setIncludeSharedTaxa(false);
        taxonNodeService.setPublishForSubtree(config);

        commitAndStartNewTransaction();
        Assert.assertEquals("Shared taxon must not be set", true, taxonService.find(1).isPublish());
        Assert.assertEquals("Taxon2 is not in subtree", true, taxonService.find(2).isPublish());
        Assert.assertEquals("Synonym of shared taxon must not be set", true, taxonService.find(3).isPublish());
        Assert.assertEquals("Synonym4 belongs to Taxon2 which is not in subtree", true, taxonService.find(4).isPublish());
        Assert.assertEquals("Taxon5 is in subtree and not shared => should be set to unpublished", publish, taxonService.find(5).isPublish());
        Assert.assertEquals("Misapplied exists as taxon in classification 2 and should not be updated"
                + " though related to taxon 5 which is updated", true, taxonService.find(6).isPublish());
        Assert.assertEquals("Pro parte synonym7 should not be updated, as it is "
                + "related to updated taxon5 but also to taxon2 which is not updated because not in subtree",
                true, taxonService.find(7).isPublish());

        taxonService.find(2).setPublish(false);
        taxonNodeService.setPublishForSubtree(config);
        commitAndStartNewTransaction();
        Assert.assertEquals("Pro parte synonym7 should be updated now, as taxon2 is now unpublished"
                + " and therefore the noShared function is not relevant anymore",
                publish, taxonService.find(7).isPublish());
    }


    @Test
    @DataSet("TaxonNodeServiceImplTest.xml")
    public void testGetTaxonDistributionDTO(){
        List<UUID> uuidList = Arrays.asList(node1Uuid, node2Uuid, node4Uuid);
        List<TaxonDistributionDTO> dtos = this.taxonNodeService.getTaxonDistributionDTO(uuidList, null, true, false);
        Assert.assertEquals("Children should be deduplicated", 3, dtos.size());
        //note: the following ordering is not given by definition (as the method does not guarantee a certain order)
        //      but is used as pseudo test here for the correctnes of the algorithm as it is currently expected
        Assert.assertEquals("First node comes first", node1Uuid, dtos.get(0).getTaxonNodeDto().getUuid());
        Assert.assertEquals("Child of first node comes second", node4Uuid, dtos.get(1).getTaxonNodeDto().getUuid());
        Assert.assertEquals("Second node comes third", node2Uuid, dtos.get(2).getTaxonNodeDto().getUuid());
        //third node is child of firt node and therefore came second already
    }

    @Test
    @DataSet("TaxonNodeServiceImplTest-testFindCommonParentNode.xml")
    public void testFindCommonParentNode(){
        UUID checklist2Uuid = UUID.fromString("c6e3a598-3b6c-4ef5-8b01-5bdb3de5a9fd");
        UUID campanulaNodeUuid = UUID.fromString("62fa918d-a1d8-4284-ae4b-93478bde8656");
        UUID campanulaPersicifoliaNodeUuid = UUID.fromString("dce3defa-5123-44a7-8008-0cc9b27461f6");

        UUID classificationUuid = UUID.fromString("029b4c07-5903-4dcf-87e8-406ed0e0285f");
        UUID abiesNodeUuid = UUID.fromString("f8306fd3-9825-41bf-94aa-a7b5790b553e");
        UUID abiesAlbaNodeUuid = UUID.fromString("c70f76e5-2dcb-41c5-ae6f-d756e0a0fae0");
        UUID abiesAlbaSubBrotaNodeUuid = UUID.fromString("06d58161-7707-44b5-b720-6c0eb916b37c");
        UUID abiesPalmaNodeUuid = UUID.fromString("6dfd30dd-e589-493a-b66a-19c4cb374f92");

        UUID pinusNodeUuid = UUID.fromString("5d8e8341-f5e9-4616-96cf-f0351dda42f4");

//        /*
//         * Checklist2
//         *  - Campanula
//         *   - Campanula persicifolia
//         * Checklist
//         *  - Abies
//         *   - Abies alba
//         *    - Abieas alba subs. brota
//         *   - Abies palma
//         *  -Pinus
//         */
//        Classification checklist2 = Classification.NewInstance("Checklist2");
//        checklist2.setUuid(checklist2Uuid);
//
//        IBotanicalName campanulaName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
//        campanulaName.setGenusOrUninomial("Campanula");
//        Taxon campanula = Taxon.NewInstance(campanulaName, null);
//
//        IBotanicalName campanulaPersicifoliaName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
//        campanulaPersicifoliaName.setGenusOrUninomial("Campanula");
//        campanulaPersicifoliaName.setSpecificEpithet("persicifolia");
//        Taxon campanulaPersicifolia = Taxon.NewInstance(campanulaPersicifoliaName, null);
//
//        TaxonNode campanulaNode = checklist2.addChildTaxon(campanula, null, null);
//        campanulaNode.setUuid(campanulaNodeUuid);
//        TaxonNode campanulaPersicifoliaNode = checklist2.addParentChild(campanula, campanulaPersicifolia, null, null);
//        campanulaPersicifoliaNode.setUuid(campanulaPersicifoliaNodeUuid);
//
//        Classification checklist = Classification.NewInstance("Checklist");
//        checklist.setUuid(classificationUuid);
//
//        IBotanicalName abiesName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
//        abiesName.setGenusOrUninomial("Abies");
//        Taxon abies = Taxon.NewInstance(abiesName, null);
//
//        IBotanicalName abiesAlbaName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
//        abiesAlbaName.setGenusOrUninomial("Abies");
//        abiesAlbaName.setSpecificEpithet("alba");
//        Taxon abiesAlba = Taxon.NewInstance(abiesAlbaName, null);
//
//        IBotanicalName abiesAlbaSubBrotaName = TaxonNameFactory.NewBotanicalInstance(Rank.SUBSPECIES());
//        abiesAlbaSubBrotaName.setGenusOrUninomial("Abies");
//        abiesAlbaSubBrotaName.setSpecificEpithet("alba");
//        abiesAlbaSubBrotaName.setInfraSpecificEpithet("brota");
//        Taxon abiesAlbaSubBrota = Taxon.NewInstance(abiesAlbaSubBrotaName, null);
//
//        IBotanicalName abiesPalmaName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
//        abiesPalmaName.setGenusOrUninomial("Abies");
//        abiesPalmaName.setSpecificEpithet("palma");
//        Taxon abiesPalma = Taxon.NewInstance(abiesPalmaName, null);
//
//        IBotanicalName pinusName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
//        pinusName.setGenusOrUninomial("Pinus");
//        Taxon pinus = Taxon.NewInstance(pinusName, null);
//
//        TaxonNode abiesNode = checklist.addChildTaxon(abies, null, null);
//        abiesNode.setUuid(abiesNodeUuid);
//        TaxonNode abiesAlbaNode = checklist.addParentChild(abies, abiesAlba, null, null);
//        abiesAlbaNode.setUuid(abiesAlbaNodeUuid);
//        TaxonNode abiesAlbaSubBrotaNode = checklist.addParentChild(abiesAlba, abiesAlbaSubBrota, null, null);
//        abiesAlbaSubBrotaNode.setUuid(abiesAlbaSubBrotaNodeUuid);
//        TaxonNode abiesPalmaNode = checklist.addParentChild(abies, abiesPalma, null, null);
//        abiesPalmaNode.setUuid(abiesPalmaNodeUuid);
//        TaxonNode pinusNode = checklist.addChildTaxon(pinus, null, null);
//        pinusNode.setUuid(pinusNodeUuid);
//
//        taxonService.saveOrUpdate(campanula);
//        taxonService.saveOrUpdate(campanulaPersicifolia);
//        classificationService.saveOrUpdate(checklist2);
//
//        taxonService.saveOrUpdate(abies);
//        taxonService.saveOrUpdate(abiesAlba);
//        taxonService.saveOrUpdate(abiesAlbaSubBrota);
//        taxonService.saveOrUpdate(abiesPalma);
//        taxonService.saveOrUpdate(pinus);
//        classificationService.saveOrUpdate(checklist);
//
//
//        setComplete();
//        endTransaction();
//
//        String fileNameAppendix = "testFindCommonParentNode";
//
//        writeDbUnitDataSetFile(new String[] {
//            "TAXONBASE", "TAXONNAME",
//            "TAXONRELATIONSHIP",
//            "HOMOTYPICALGROUP",
//            "CLASSIFICATION", "TAXONNODE",
//            "LANGUAGESTRING",
//            "HIBERNATE_SEQUENCES" // IMPORTANT!!!
//            },
//            fileNameAppendix );

        Classification classification = classificationService.load(classificationUuid);

        TaxonNode campanula = taxonNodeService.load(campanulaNodeUuid);
        TaxonNode campanulaPersicifolia = taxonNodeService.load(campanulaPersicifoliaNodeUuid);
        TaxonNode abies = taxonNodeService.load(abiesNodeUuid);
        TaxonNode abiesAlba = taxonNodeService.load(abiesAlbaNodeUuid);
        TaxonNode abiesPalma = taxonNodeService.load(abiesPalmaNodeUuid);
        TaxonNode pinus = taxonNodeService.load(pinusNodeUuid);

        //check initial state
        assertTrue(campanula!=null);
        assertTrue(campanulaPersicifolia!=null);
        assertTrue(abies!=null);
        assertTrue(abiesAlba!=null);
        assertTrue(abiesPalma!=null);
        assertTrue(pinus!=null);

        TaxonNodeDto classificationRootNodeDto = new TaxonNodeDto(classification.getRootNode());
        TaxonNodeDto campanulaDto = new TaxonNodeDto(campanula);
        TaxonNodeDto campanulaPersicifoliaDto = new TaxonNodeDto(campanulaPersicifolia);
        TaxonNodeDto abiesDto = new TaxonNodeDto(abies);
        TaxonNodeDto abiesAlbaDto = new TaxonNodeDto(abiesAlba);
        TaxonNodeDto abiesPalmaDto = new TaxonNodeDto(abiesPalma);
        TaxonNodeDto pinusDto = new TaxonNodeDto(pinus);

        List<TaxonNodeDto> nodes = new ArrayList<>();
        nodes.add(campanulaDto);
        TaxonNodeDto commonParentNodeDto = taxonNodeService.findCommonParentDto(nodes);
        assertEquals(campanulaDto.getUuid(), commonParentNodeDto.getUuid());

        nodes = new ArrayList<>();
        nodes.add(campanulaDto);
        nodes.add(campanulaPersicifoliaDto);
        commonParentNodeDto = taxonNodeService.findCommonParentDto(nodes);
        assertEquals(campanulaDto.getUuid(), commonParentNodeDto.getUuid());

        nodes = new ArrayList<>();
        nodes.add(campanulaDto);
        nodes.add(abiesAlbaDto);
        commonParentNodeDto = taxonNodeService.findCommonParentDto(nodes);
        assertNull(commonParentNodeDto);

        nodes = new ArrayList<>();
        nodes.add(abiesAlbaDto);
        nodes.add(abiesPalmaDto);
        commonParentNodeDto = taxonNodeService.findCommonParentDto(nodes);
        assertEquals(abiesDto.getUuid(), commonParentNodeDto.getUuid());

        nodes = new ArrayList<>();
        nodes.add(abiesDto);
        nodes.add(pinusDto);
        commonParentNodeDto = taxonNodeService.findCommonParentDto(nodes);
        assertEquals(classificationRootNodeDto.getUuid(), commonParentNodeDto.getUuid());
    }

    @Test  //8127  //5536 //10101
    @Ignore // see #10101
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public final void testMergeDetached(){

        //create classifications with 2 child nodes
        Classification tree = Classification.NewInstance("Classification");
        TaxonNode child1 = tree.getRootNode().addChildTaxon(Taxon.NewInstance(null, null), null);
        TaxonNode child2 = tree.getRootNode().addChildTaxon(null, null);
        classificationService.save(tree);
        taxonNodeService.save(child1);
        taxonNodeService.save(child2);
        commitAndStartNewTransaction();

        //load root node and make it detached
        TaxonNode rootNode = taxonNodeService.find(tree.getRootNode().getUuid());
        rootNode.getChildNodes().get(0).getChildNodes().size();  //initialize children
        rootNode.getChildNodes().get(1).getChildNodes().size();  //initialize children
        commitAndStartNewTransaction(); //detach

        //replace nodes and merge
        TaxonNode childToRemove = rootNode.getChildNodes().get(0);
        rootNode.deleteChildNode(childToRemove);
        TaxonNode child3 =rootNode.addChildTaxon(null, null);
        taxonNodeService.merge(rootNode, childToRemove);
        commitAndStartNewTransaction(tableNames);

        //test result
        rootNode = taxonNodeService.find(tree.getRootNode().getUuid());
        rootNode.getChildNodes();
        Assert.assertEquals(2, rootNode.getChildNodes().size());
        Assert.assertEquals(child2.getUuid(), rootNode.getChildNodes().get(0).getUuid());
        Assert.assertEquals(child3.getUuid(), rootNode.getChildNodes().get(1).getUuid());
        Assert.assertEquals("Should be root + 2 children", 3, taxonNodeService.count(TaxonNode.class));
        commitAndStartNewTransaction();

//      System.out.println("NEXT");
        //same with classification
        //load root node and make it detached
        Classification treeLoaded = classificationService.find(tree.getUuid());
        rootNode = treeLoaded.getRootNode();
        rootNode.getChildNodes().get(0);  //initialize children
        commitAndStartNewTransaction(); //detach

        //replace nodes and merge
        childToRemove = rootNode.getChildNodes().get(0);
        rootNode.deleteChildNode(childToRemove);
        TaxonNode child4 = rootNode.addChildTaxon(null, null);

        @SuppressWarnings("unused")
        Classification mergedClassification = classificationService.merge(treeLoaded, childToRemove);

      //NOTE: maybe interesting to know, that if not using orphan removal
      //      resorting the index does not take place if not touching the children list somehow.
      //      The sortindex starts than at some number > 0 and may contain nulls.
      //      If touching the list like below the index starts at 0. This is now
      //      automatically handled in PostMergeEntityListener.
      //      mergedKey.getRoot().getChildren().size();

        commitAndStartNewTransaction(tableNames);

        rootNode = taxonNodeService.find(classification.getRootNode().getUuid());
        rootNode.getChildNodes();
        Assert.assertEquals(2, rootNode.getChildNodes().size());
        Assert.assertEquals(child3.getUuid(), rootNode.getChildNodes().get(0).getUuid());
        Assert.assertEquals(child4.getUuid(), rootNode.getChildNodes().get(1).getUuid());
        Assert.assertEquals("Should be root + 2 children", 3, taxonNodeService.count(TaxonNode.class));
    }

    @Test   //#10101
    @Ignore //see #10101
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public void testMergeDetachedWithMove() {

        //create classification with 2 child nodes
        Classification tree = Classification.NewInstance("Classification");
        TaxonNode child1 = tree.getRootNode().addChildTaxon(null, null);
        TaxonNode child2 = tree.getRootNode().addChildTaxon(null, null);
        classificationService.save(tree);
        taxonNodeService.save(child1);
        taxonNodeService.save(child2);
        commitAndStartNewTransaction();

        //load root node and make it detached
        Classification keyLoaded = classificationService.find(tree.getUuid());
        TaxonNode rootNode = keyLoaded.getRootNode();
        rootNode.getChildNodes().get(0).getChildNodes().size();  //initialize children
        rootNode.getChildNodes().get(1).getChildNodes().size();  //initialize children
        commitAndStartNewTransaction(); //detach

        //replace nodes and merge
        TaxonNode childMove = rootNode.getChildNodes().get(0);
        TaxonNode newParentNode = rootNode.getChildNodes().get(1);
        TaxonNode child3 = newParentNode.addChildNode(childMove, null, null);
        TaxonNode child4 =rootNode.addChildTaxon(null, null);
        taxonNodeService.saveOrUpdate(child4);

        @SuppressWarnings("unused")
        //no removed child to delete here
        Classification mergedTree = classificationService.merge(keyLoaded, new CdmBase[]{});

        commitAndStartNewTransaction(tableNames);

        rootNode = taxonNodeService.find(tree.getRootNode().getUuid());
        rootNode.getChildNodes();
        Assert.assertEquals(2, rootNode.getChildNodes().size());
        TaxonNode firstChild = rootNode.getChildNodes().get(0);
        Assert.assertEquals(child2.getUuid(), firstChild.getUuid());
        Assert.assertEquals(child4.getUuid(), rootNode.getChildNodes().get(1).getUuid());
        Assert.assertEquals(1, firstChild.getChildNodes().size());
        Assert.assertEquals(child1.getUuid(), firstChild.getChildNodes().get(0).getUuid());
        Assert.assertEquals("Should be root + 2 children + 1 grandchild", 4, taxonNodeService.count(TaxonNode.class));
    }

    @Test //#10101
    @Ignore  //see #10101
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDBDataSet.xml")
    public final void testSaveDetached(){

        //create classifications with 2 child nodes
        Classification tree = Classification.NewInstance("Classification");
        TaxonNode child1 = tree.getRootNode().addChildTaxon(Taxon.NewInstance(null, null), null);
        TaxonNode child2 = tree.getRootNode().addChildTaxon(null, null);
        classificationService.save(tree);
        taxonNodeService.save(child1);
        taxonNodeService.save(child2);
        commitAndStartNewTransaction();

        //load root node and make it detached
        TaxonNode rootNode = taxonNodeService.find(tree.getRootNode().getUuid());
        rootNode.getChildNodes().get(0).getChildNodes().size();  //initialize children
        rootNode.getChildNodes().get(1).getChildNodes().size();  //initialize children
        commitAndStartNewTransaction(); //detach

        //replace nodes and merge
        TaxonNode childToRemove = rootNode.getChildNodes().get(0);
        rootNode.deleteChildNode(childToRemove);
        TaxonNode child3 =rootNode.addChildTaxon(null, null);
        taxonNodeService.saveOrUpdate(rootNode);
        taxonNodeService.delete(childToRemove.getUuid());  //combined method like in merge does not yet exist for saveOrUpdate
        commitAndStartNewTransaction(tableNames);

        //test result
        rootNode = taxonNodeService.find(tree.getRootNode().getUuid());
        rootNode.getChildNodes();
        Assert.assertEquals(2, rootNode.getChildNodes().size());
        Assert.assertEquals(child2.getUuid(), rootNode.getChildNodes().get(0).getUuid());
        Assert.assertEquals(child3.getUuid(), rootNode.getChildNodes().get(1).getUuid());
        Assert.assertEquals("Should be root + 2 children", 3, taxonNodeService.count(TaxonNode.class));
        commitAndStartNewTransaction();

//      System.out.println("NEXT");
        //same with classification
        //load root node and make it detached
        Classification treeLoaded = classificationService.find(tree.getUuid());
        rootNode = treeLoaded.getRootNode();
        rootNode.getChildNodes().get(0);  //initialize children
        commitAndStartNewTransaction(); //detach

        //replace nodes and merge
        childToRemove = rootNode.getChildNodes().get(0);
        rootNode.deleteChildNode(childToRemove);
        TaxonNode child4 = rootNode.addChildTaxon(null, null);

        //TODO can't work yet as TaxonNodes are not cascaded on purpose
        classificationService.saveOrUpdate(treeLoaded);
        taxonNodeService.delete(childToRemove.getUuid());

        commitAndStartNewTransaction(tableNames);

        rootNode = taxonNodeService.find(classification.getRootNode().getUuid());
        rootNode.getChildNodes();
        Assert.assertEquals(2, rootNode.getChildNodes().size());
        Assert.assertEquals(child3.getUuid(), rootNode.getChildNodes().get(0).getUuid());
        Assert.assertEquals(child4.getUuid(), rootNode.getChildNodes().get(1).getUuid());
        Assert.assertEquals("Should be root + 2 children", 3, taxonNodeService.count(TaxonNode.class));

        //TODO implement testSaveDetachedWithMove like in TermNode and PolytomousKeyNode service tests
    }

    @Test
    @DataSet("ClassificationServiceImplTest.xml")
    public final void testCloneClassification(){

        Classification originalClassification = classificationDao.load(ClassificationServiceImplTest.CLASSIFICATION_UUID);

        SubtreeCloneConfigurator config = SubtreeCloneConfigurator.NewBaseInstance(
                originalClassification.getRootNode().getUuid(), "Cloned classification");

        Classification classificationClone = (Classification) taxonNodeService.cloneSubtree(config).getCdmEntity();

        assertEquals("# of direct children does not match", originalClassification.getChildNodes().size(), classificationClone.getChildNodes().size());
        assertEquals("# of all nodes does not match", originalClassification.getAllNodes().size(), classificationClone.getAllNodes().size());

        Set<UUID> originalTaxonSecUuids = originalClassification.getAllNodes().stream().map(tn -> tn.getTaxon().getSec().getUuid()).collect(Collectors.toSet());
        for (TaxonNode clonedTaxonNode : classificationClone.getChildNodes()) {
            //test no reuse of taxon
            Taxon clonedTaxon = clonedTaxonNode.getTaxon();
            TaxonNode originalNode = originalClassification.getNode(clonedTaxon);
            assertNull(originalNode);

            //check relationship
            assertEquals(0, clonedTaxon.getRelationsFromThisTaxon().size());

            //test taxon sec
            assertTrue(originalTaxonSecUuids.contains(clonedTaxon.getSec().getUuid()));
        }
        commitAndStartNewTransaction();

        //test reuse taxon
        config.setReuseTaxa(true);
        classificationClone = (Classification) taxonNodeService.cloneSubtree(config).getCdmEntity();
        assertEquals("# of direct children does not match", originalClassification.getChildNodes().size(), classificationClone.getChildNodes().size());
        originalTaxonSecUuids = originalClassification.getAllNodes().stream().map(tn -> tn.getTaxon().getSec().getUuid()).collect(Collectors.toSet());
        for (TaxonNode taxonNode : classificationClone.getChildNodes()) {
            //test no reuse of taxon
            Taxon clonedTaxon = taxonNode.getTaxon();
            TaxonNode originalNode = originalClassification.getNode(clonedTaxon);
            assertNotNull(originalNode);
            Taxon originalTaxon = originalNode.getTaxon();
            assertNotNull(originalTaxon);

            //check relationship
            assertEquals(0, clonedTaxon.getRelationsFromThisTaxon().size());

            //test taxon sec
            assertEquals(originalTaxon.getSec().getUuid(), clonedTaxon.getSec().getUuid());
        }
        commitAndStartNewTransaction();

        config.setReuseTaxa(false);  //reset
        config.setRelationTypeToOldTaxon(TaxonRelationshipType.CONGRUENT_TO());
        Reference sec = referenceDao.findByUuid(UUID.fromString("719d136b-409e-40d0-9561-46f6999465b4"));
        config.setTaxonSecundumUuid(sec.getUuid());
        classificationClone = (Classification) taxonNodeService.cloneSubtree(config).getCdmEntity();
        originalTaxonSecUuids = originalClassification.getAllNodes().stream().map(tn -> tn.getTaxon().getSec().getUuid()).collect(Collectors.toSet());
        for (TaxonNode taxonNode : classificationClone.getChildNodes()) {
            //test no reuse of taxon
            Taxon clonedTaxon = taxonNode.getTaxon();
            TaxonNode originalNode = originalClassification.getNode(clonedTaxon);
            assertNull(originalNode);

            //check relationship
            TaxonRelationship relShip = clonedTaxon.getRelationsFromThisTaxon().iterator().next();
            Taxon relatedTaxon = relShip.getToTaxon();
            Taxon relatedOriginalTaxon = originalClassification.getNode(relatedTaxon).getTaxon();
            assertEquals(relatedOriginalTaxon.getName(), clonedTaxon.getName());
            assertTrue(relShip.getType().equals(TaxonRelationshipType.CONGRUENT_TO()));

            //test taxon sec
            assertEquals(relatedOriginalTaxon.getSec().getUuid(), clonedTaxon.getSec().getUuid());
        }
        commitAndStartNewTransaction();

        //no recursive for root
        config = SubtreeCloneConfigurator.NewBaseInstance(
                originalClassification.getRootNode().getUuid(), "Cloned classification");
        config.setDoRecursive(false);
        classificationClone = (Classification) taxonNodeService.cloneSubtree(config).getCdmEntity();
        Assert.assertTrue(classificationClone.getRootNode().getChildNodes().isEmpty());

        //no recursive for root
        config = SubtreeCloneConfigurator.NewBaseInstance(
                UUID.fromString("26cc5c08-72df-45d4-84ea-ce81e7e53114"), "Cloned classification");
        config.setDoRecursive(false);
        classificationClone = (Classification) taxonNodeService.cloneSubtree(config).getCdmEntity();
        List<TaxonNode> nodes = classificationClone.getRootNode().getChildNodes();
        Assert.assertEquals(1, nodes.size());
        Taxon clonedTaxon = nodes.iterator().next().getTaxon();
        Assert.assertEquals("Name should be the same as for the original taxon", UUID.fromString("301e2bf0-85a4-442a-93f6-63d3b9ee8c3d"), clonedTaxon.getName().getUuid());
        Assert.assertTrue(nodes.iterator().next().getChildNodes().isEmpty());
    }

    @Override
//    @Test
    public void createTestDataSet() throws FileNotFoundException {
        UUID checklist2Uuid = UUID.fromString("c6e3a598-3b6c-4ef5-8b01-5bdb3de5a9fd");
        UUID campanulaNodeUuid = UUID.fromString("62fa918d-a1d8-4284-ae4b-93478bde8656");
        UUID campanulaPersicifoliaNodeUuid = UUID.fromString("dce3defa-5123-44a7-8008-0cc9b27461f6");

        UUID classificationUuid = UUID.fromString("029b4c07-5903-4dcf-87e8-406ed0e0285f");
        UUID abiesNodeUuid = UUID.fromString("f8306fd3-9825-41bf-94aa-a7b5790b553e");
        UUID abiesAlbaNodeUuid = UUID.fromString("c70f76e5-2dcb-41c5-ae6f-d756e0a0fae0");
        UUID abiesAlbaSubBrotaNodeUuid = UUID.fromString("06d58161-7707-44b5-b720-6c0eb916b37c");
        UUID abiesPalmaNodeUuid = UUID.fromString("6dfd30dd-e589-493a-b66a-19c4cb374f92");

        UUID pinusNodeUuid = UUID.fromString("5d8e8341-f5e9-4616-96cf-f0351dda42f4");

        /*
         * Checklist2
         *  - Campanula
         *   - Campanula persicifolia
         * Checklist
         *  - Abies
         *   - Abies alba
         *    - Abieas alba subs. brota
         *   - Abies palma
         *  -Pinus
         */
        Classification checklist2 = Classification.NewInstance("Checklist2");
        checklist2.setUuid(checklist2Uuid);

        IBotanicalName campanulaName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
        campanulaName.setGenusOrUninomial("Campanula");
        Taxon campanula = Taxon.NewInstance(campanulaName, null);

        IBotanicalName campanulaPersicifoliaName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        campanulaPersicifoliaName.setGenusOrUninomial("Campanula");
        campanulaPersicifoliaName.setSpecificEpithet("persicifolia");
        Taxon campanulaPersicifolia = Taxon.NewInstance(campanulaPersicifoliaName, null);

        TaxonNode campanulaNode = checklist2.addChildTaxon(campanula, null, null);
        campanulaNode.setUuid(campanulaNodeUuid);
        TaxonNode campanulaPersicifoliaNode = checklist2.addParentChild(campanula, campanulaPersicifolia, null, null);
        campanulaPersicifoliaNode.setUuid(campanulaPersicifoliaNodeUuid);

        Classification checklist = Classification.NewInstance("Checklist");
        checklist.setUuid(classificationUuid);

        IBotanicalName abiesName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
        abiesName.setGenusOrUninomial("Abies");
        Taxon abies = Taxon.NewInstance(abiesName, null);

        IBotanicalName abiesAlbaName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        abiesAlbaName.setGenusOrUninomial("Abies");
        abiesAlbaName.setSpecificEpithet("alba");
        Taxon abiesAlba = Taxon.NewInstance(abiesAlbaName, null);

        IBotanicalName abiesAlbaSubBrotaName = TaxonNameFactory.NewBotanicalInstance(Rank.SUBSPECIES());
        abiesAlbaSubBrotaName.setGenusOrUninomial("Abies");
        abiesAlbaSubBrotaName.setSpecificEpithet("alba");
        abiesAlbaSubBrotaName.setInfraSpecificEpithet("brota");
        Taxon abiesAlbaSubBrota = Taxon.NewInstance(abiesAlbaSubBrotaName, null);

        IBotanicalName abiesPalmaName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        abiesPalmaName.setGenusOrUninomial("Abies");
        abiesPalmaName.setSpecificEpithet("palma");
        Taxon abiesPalma = Taxon.NewInstance(abiesPalmaName, null);

        IBotanicalName pinusName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
        pinusName.setGenusOrUninomial("Pinus");
        Taxon pinus = Taxon.NewInstance(pinusName, null);

        TaxonNode abiesNode = checklist.addChildTaxon(abies, null, null);
        abiesNode.setUuid(abiesNodeUuid);
        TaxonNode abiesAlbaNode = checklist.addParentChild(abies, abiesAlba, null, null);
        abiesAlbaNode.setUuid(abiesAlbaNodeUuid);
        TaxonNode abiesAlbaSubBrotaNode = checklist.addParentChild(abiesAlba, abiesAlbaSubBrota, null, null);
        abiesAlbaSubBrotaNode.setUuid(abiesAlbaSubBrotaNodeUuid);
        TaxonNode abiesPalmaNode = checklist.addParentChild(abies, abiesPalma, null, null);
        abiesPalmaNode.setUuid(abiesPalmaNodeUuid);
        TaxonNode pinusNode = checklist.addChildTaxon(pinus, null, null);
        pinusNode.setUuid(pinusNodeUuid);

        taxonService.saveOrUpdate(campanula);
        taxonService.saveOrUpdate(campanulaPersicifolia);
        classificationService.saveOrUpdate(checklist2);

        taxonService.saveOrUpdate(abies);
        taxonService.saveOrUpdate(abiesAlba);
        taxonService.saveOrUpdate(abiesAlbaSubBrota);
        taxonService.saveOrUpdate(abiesPalma);
        taxonService.saveOrUpdate(pinus);
        classificationService.saveOrUpdate(checklist);

        setComplete();
        endTransaction();

        String fileNameAppendix = "testFindCommonParentNode";

        writeDbUnitDataSetFile(new String[] {
            "TAXONBASE", "TAXONNAME",
            "TAXONRELATIONSHIP",
            "HOMOTYPICALGROUP",
            "CLASSIFICATION", "TAXONNODE",
            "LANGUAGESTRING",
            "HIBERNATE_SEQUENCES" // IMPORTANT!!!
            },
            fileNameAppendix, true );
    }
}
