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
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author n.hoffmann
 * @created Dec 16, 2010
 * @version 1.0
 */

public class TaxonNodeServiceImplTest extends CdmIntegrationTest{

	@SpringBeanByType
	private ITaxonNodeService taxonNodeService;
	
	@SpringBeanByType
	private IClassificationService classificationService;
	
	@SpringBeanByType
	private IReferenceService referenceService;
	
	@SpringBeanByType
	private ITermService termService;
	
	private static final UUID t1Uuid = UUID.fromString("55c3e41a-c629-40e6-aa6a-ff274ac6ddb1");
	private static final UUID t2Uuid = UUID.fromString("2659a7e0-ff35-4ee4-8493-b453756ab955");
	private static final UUID classificationUuid = UUID.fromString("6c2bc8d9-ee62-4222-be89-4a8e31770878");
	private static final UUID referenceUuid = UUID.fromString("de7d1205-291f-45d9-9059-ca83fc7ade14");
	private static final UUID node1Uuid = UUID.fromString("484a1a77-689c-44be-8e65-347d835f47e8");
	private static final UUID node2Uuid = UUID.fromString("2d41f0c2-b785-4f73-a436-cc2d5e93cc5b");
	
	private Taxon t1;
	private Taxon t2;
	private Synonym s1;
	private SynonymRelationshipType synonymRelationshipType;
	private Reference<?> reference;
	private String referenceDetail;
	private Classification classification;
	private TaxonNode node2;
	private TaxonNode node1;

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
		node1 = taxonNodeService.load(node1Uuid);
		node2 = taxonNodeService.load(node2Uuid);
		reference = referenceService.load(referenceUuid);
//		synonymRelationshipType = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
		synonymRelationshipType = CdmBase.deproxy(termService.load(SynonymRelationshipType.uuidHomotypicSynonymOf), SynonymRelationshipType.class) ;
		referenceDetail = "test"; 

		//
		//TODO
		

		// descriptions
		t1 = node1.getTaxon();
		t2 = node2.getTaxon();
		Assert.assertEquals(2, t1.getDescriptions().size());
		Assert.assertTrue(t2.getSynonyms().isEmpty());
		Assert.assertTrue(t2.getDescriptions().size() == 0);
		
		taxonNodeService.makeTaxonNodeASynonymOfAnotherTaxonNode(node1, node2, synonymRelationshipType, reference, referenceDetail);
		termService.saveOrUpdate(synonymRelationshipType);
		Assert.assertFalse(t2.getSynonyms().isEmpty());
		Assert.assertEquals(2, t2.getDescriptions().size());
		
	}

}
