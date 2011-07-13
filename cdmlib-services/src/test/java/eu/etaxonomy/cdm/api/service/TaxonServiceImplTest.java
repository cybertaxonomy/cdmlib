/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.mueller
 *
 */
public class TaxonServiceImplTest extends CdmIntegrationTest {
	private static final Logger logger = Logger.getLogger(TaxonServiceImplTest.class);
	
	@SpringBeanByType
	private ITaxonService service;
	
	@SpringBeanByType
	private INameService nameService;
	
/****************** TESTS *****************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#getTaxonByUuid(java.util.UUID)}.
	 */
	@Test
	public final void testGetTaxonByUuid() {
		Taxon expectedTaxon = Taxon.NewInstance(null, null);
		UUID uuid = service.save(expectedTaxon);
		TaxonBase<?> actualTaxon = service.find(uuid);
		assertEquals(expectedTaxon, actualTaxon);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#saveTaxon(eu.etaxonomy.cdm.model.taxon.TaxonBase)}.
	 */
	@Test
	public final void testSaveTaxon() {
		Taxon expectedTaxon = Taxon.NewInstance(null, null);
		UUID uuid = service.save(expectedTaxon);
		TaxonBase<?> actualTaxon = service.find(uuid);
		assertEquals(expectedTaxon, actualTaxon);
	}
	
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#removeTaxon(eu.etaxonomy.cdm.model.taxon.TaxonBase)}.
	 */
	@Test
	public final void testRemoveTaxon() {
		Taxon taxon = Taxon.NewInstance(BotanicalName.NewInstance(null), null);
		UUID uuid = service.save(taxon);
		service.delete(taxon);
		TaxonBase<?> actualTaxon = service.find(uuid);
		assertNull(actualTaxon);
	}
	
	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#loadTreeBranchTo(eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.name.Rank, java.util.List)}
	 * .
	 */
	@Test
	public final void loadTreeBranchTo() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#searchTaxaByName(java.lang.String, eu.etaxonomy.cdm.model.reference.Reference)}
	 * .
	 */
	@Test
	public final void testSearchTaxaByName() {
		logger.warn("Not yet implemented"); // TODO
	}

	
	@Test
	public final void testPrintDataSet() {
		//printDataSet(System.out);
	}
	
	@Test
	public final void testMakeTaxonSynonym() {
		Rank rank = Rank.SPECIES();
		Taxon tax1 = Taxon.NewInstance(BotanicalName.NewInstance(rank, "Test1", null, null, null, null, null, null, null), null);
		Synonym synonym = Synonym.NewInstance(BotanicalName.NewInstance(rank, "Test2", null, null, null, null, null, null, null), null);
		tax1.addHomotypicSynonym(synonym, null, null);
		UUID uuidTaxon = service.save(tax1);
		UUID uuidSyn = service.save(synonym);
		
		service.swapSynonymAndAcceptedTaxon(synonym, tax1);
		
		TaxonBase<?> tax = service.find(uuidTaxon);
		TaxonBase<?> syn = service.find(uuidSyn);
		HomotypicalGroup groupTest = tax.getHomotypicGroup();
		HomotypicalGroup groupTest2 = syn.getHomotypicGroup();
		assertEquals(groupTest, groupTest2);
	}
	
	@Test
	public final void testMakeSynonymTaxon(){
		Rank rank = Rank.SPECIES();
		//HomotypicalGroup group = HomotypicalGroup.NewInstance();
		Taxon tax1 = Taxon.NewInstance(BotanicalName.NewInstance(rank, "Test1", null, null, null, null, null, null, null), null);
		Taxon tax2 = Taxon.NewInstance(BotanicalName.NewInstance(rank, "Test3", null, null, null, null, null, null, null), null);
		Synonym synonym = Synonym.NewInstance(BotanicalName.NewInstance(rank, "Test2", null, null, null, null, null, null, null), null);
		//tax2.addHeterotypicSynonymName(synonym.getName());
		tax2.addSynonym(synonym, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		BotanicalName name = (BotanicalName)synonym.getName();
		UUID uuidTaxon = service.save(tax1);
		UUID uuidSyn = service.save(synonym);
		UUID uuidGenus = service.save(tax2);
		
		Taxon tax = service.changeSynonymToAcceptedTaxon(synonym, tax2, true, true, null, null);
		TaxonBase<?> syn = service.find(uuidSyn);
		assertNull(syn);
		
		
	
	}

}
