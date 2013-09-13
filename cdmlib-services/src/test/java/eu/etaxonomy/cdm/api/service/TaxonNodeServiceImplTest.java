// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author n.hoffmann
 * @created Dec 16, 2010
 */
@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-test.xml")
public class TaxonNodeServiceImplTest extends CdmTransactionalIntegrationTest{

	
	@SpringBeanByType
	private ITaxonNodeService taxonNodeService;

	@SpringBeanByType
	private IClassificationService classificationService;

	@SpringBeanByType
	private IReferenceService referenceService;

	@SpringBeanByType
	private ITermService termService;

//	private static final UUID t2Uuid = UUID.fromString("55c3e41a-c629-40e6-aa6a-ff274ac6ddb1");
//	private static final UUID t3Uuid = UUID.fromString("2659a7e0-ff35-4ee4-8493-b453756ab955");
	private static final UUID classificationUuid = UUID.fromString("6c2bc8d9-ee62-4222-be89-4a8e31770878");
	private static final UUID classification2Uuid = UUID.fromString("43d67247-936f-42a3-a739-bbcde372e334");
	private static final UUID referenceUuid = UUID.fromString("de7d1205-291f-45d9-9059-ca83fc7ade14");
	private static final UUID node2Uuid= UUID.fromString("484a1a77-689c-44be-8e65-347d835f47e8");
	private static final UUID node3Uuid = UUID.fromString("2d41f0c2-b785-4f73-a436-cc2d5e93cc5b");
//	private static final UUID node4Uuid = UUID.fromString("fdaec4bd-c78e-44df-ae87-28f18110968c");
	private static final UUID node5Uuid = UUID.fromString("c4d5170a-7967-4dac-ab76-ae2019eefde5");
	private static final UUID node6Uuid = UUID.fromString("b419ba5e-9c8b-449c-ad86-7abfca9a7340");

	private Taxon t1;
	private Taxon t2;
//	private Synonym s1;
	private SynonymRelationshipType synonymRelationshipType;
	private Reference<?> reference;
	private String referenceDetail;
	private Classification classification;
	private TaxonNode node3;
	private TaxonNode node2;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonNodeServiceImpl#makeTaxonNodeASynonymOfAnotherTaxonNode(eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String)}.
	 */
	@Test
	@DataSet
	public final void testMakeTaxonNodeASynonymOfAnotherTaxonNode() {
		classification = classificationService.load(classificationUuid);
		node2 = taxonNodeService.load(node2Uuid);
		node3 = taxonNodeService.load(node3Uuid);
		reference = referenceService.load(referenceUuid);
//		synonymRelationshipType = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
		synonymRelationshipType = CdmBase.deproxy(termService.load(SynonymRelationshipType.uuidHomotypicSynonymOf), SynonymRelationshipType.class) ;
		referenceDetail = "test";

		//
		//TODO

//		printDataSet(System.err, new String [] {"TaxonNode"});

		// descriptions
		t1 = node2.getTaxon();
		t2 = node3.getTaxon();
		Assert.assertEquals(2, t1.getDescriptions().size());
		Assert.assertTrue(t2.getSynonyms().isEmpty());
		Assert.assertTrue(t2.getDescriptions().size() == 0);

		taxonNodeService.makeTaxonNodeASynonymOfAnotherTaxonNode(node2, node3, synonymRelationshipType, reference, referenceDetail);
		termService.saveOrUpdate(synonymRelationshipType);
		Assert.assertFalse(t2.getSynonyms().isEmpty());
		Assert.assertEquals(2, t2.getDescriptions().size());
	}
	
	@Test
	@DataSet(value="TaxonNodeServiceImplTest-indexing.xml")
	public final void testIndexCreateNode() {
		Taxon taxon = Taxon.NewInstance(null, null);
		classification = classificationService.load(classificationUuid);
		node2 = taxonNodeService.load(node2Uuid);
		String oldTreeIndex = node2.treeIndex();
		
		TaxonNode newNode = node2.addChildTaxon(taxon, null, null);
		taxonNodeService.saveOrUpdate(node2);
		commitAndStartNewTransaction(new String[]{"TaxonNode"});
		newNode = taxonNodeService.load(newNode.getUuid());
		Assert.assertEquals("", oldTreeIndex + newNode.getId() + "#", newNode.treeIndex());
	}

	
	@Test
	@DataSet(value="TaxonNodeServiceImplTest-indexing.xml")
	public final void testIndexMoveNode() {
		//in classification
		classification = classificationService.load(classificationUuid);
		node2 = taxonNodeService.load(node2Uuid);
		node3 = taxonNodeService.load(node3Uuid);
		node3.addChildNode(node2, null, null);
		taxonNodeService.saveOrUpdate(node2);
		commitAndStartNewTransaction(new String[]{"TaxonNode"});
		TaxonNode node6 = taxonNodeService.load(node6Uuid);
		Assert.assertEquals("Node6 treeindex is not correct", node3.treeIndex() + "2#4#6#", node6.treeIndex());

		//root of new classification
		Classification classification2 = classificationService.load(classification2Uuid);
		node2 = taxonNodeService.load(node2Uuid);
		classification2.addChildNode(node2, null, null);
		taxonNodeService.saveOrUpdate(node2);
		commitAndStartNewTransaction(new String[]{"TaxonNode"});
		node2 = taxonNodeService.load(node2Uuid);
		Assert.assertEquals("Node2 treeindex is not correct", "#t2#2#", node2.treeIndex());
		node6 = taxonNodeService.load(node6Uuid);
		Assert.assertEquals("Node6 treeindex is not correct", "#t2#2#4#6#", node6.treeIndex());

		//into new classification
		node3 = taxonNodeService.load(node3Uuid);
		TaxonNode node5 = taxonNodeService.load(node5Uuid);
		node5.addChildNode(node3, null, null);
		taxonNodeService.saveOrUpdate(node5);
		commitAndStartNewTransaction(new String[]{"TaxonNode"});
		node3 = taxonNodeService.load(node3Uuid);
		Assert.assertEquals("Node3 treeindex is not correct", "#t2#2#5#3#", node3.treeIndex());

	}

	@Test  //here we may have a test for testing delete of a node and attaching the children
	//to its parents, however this depends on the way delete is implemented and therefore needs
	//to wait until this is finally done
	public final void testIndexDeleteNode() {
//		node2 = taxonNodeService.load(node2Uuid);
//		node2.getParent().deleteChildNode(node2);
//		
//		node5.addChildNode(node3, null, null);
//		taxonNodeService.saveOrUpdate(node5);
//		commitAndStartNewTransaction(new String[]{"TaxonNode"});
//		node3 = taxonNodeService.load(node3Uuid);
//		Assert.assertEquals("Node3 treeindex is not correct", "#t2#2#5#3#", node3.getTreeIndex());
	}
	
	
	
	

}
