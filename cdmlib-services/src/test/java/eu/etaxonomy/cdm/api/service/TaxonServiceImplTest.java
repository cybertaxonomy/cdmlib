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
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
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
	
	@Test
	public final void testSaveOrUpdateTaxon() {
		Taxon expectedTaxon = Taxon.NewInstance(null, null);
		UUID uuid = service.save(expectedTaxon);
		TaxonBase<?> actualTaxon = service.find(uuid);
		assertEquals(expectedTaxon, actualTaxon);
		
		actualTaxon.setName(BotanicalName.NewInstance(Rank.SPECIES()));
		try{
			service.saveOrUpdate(actualTaxon);
		}catch(Exception e){
			Assert.fail();
		}
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

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#searchTaxaByName(java.lang.String, eu.etaxonomy.cdm.model.reference.Reference)}
	 * .
	 */
	@Test
	public final void testSearchTaxaByName() {
		logger.warn("testSearchTaxaByName not yet implemented"); // TODO
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
	public final void testChangeSynonymToAcceptedTaxon(){
		Rank rank = Rank.SPECIES();
		//HomotypicalGroup group = HomotypicalGroup.NewInstance();
		Taxon taxWithoutSyn = Taxon.NewInstance(BotanicalName.NewInstance(rank, "Test1", null, null, null, null, null, null, null), null);
		Taxon taxWithSyn = Taxon.NewInstance(BotanicalName.NewInstance(rank, "Test3", null, null, null, null, null, null, null), null);
		Synonym synonym = Synonym.NewInstance(BotanicalName.NewInstance(rank, "Test2", null, null, null, null, null, null, null), null);
		Synonym synonym2 = Synonym.NewInstance(BotanicalName.NewInstance(rank, "Test4", null, null, null, null, null, null, null), null);
		synonym2.getName().setHomotypicalGroup(synonym.getHomotypicGroup());
		//tax2.addHeterotypicSynonymName(synonym.getName());
		taxWithSyn.addSynonym(synonym, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		taxWithSyn.addSynonym(synonym2, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		
		service.save(taxWithoutSyn);
		UUID uuidSyn = service.save(synonym);
		service.save(synonym2);
		service.save(taxWithSyn);
		
		Taxon taxon = service.changeSynonymToAcceptedTaxon(synonym, taxWithSyn, true, true, null, null);
		//test flush (resave deleted object)
		TaxonBase<?> syn = service.find(uuidSyn);
		assertNull(syn);
		Assert.assertEquals("New taxon should have 1 synonym relationship (the old homotypic synonym)", 1, taxon.getSynonymRelations().size());
	}

	@Test
	public final void testGetHeterotypicSynonymyGroups(){
		Rank rank = Rank.SPECIES();
		Reference<?> ref1 = ReferenceFactory.newGeneric();
		//HomotypicalGroup group = HomotypicalGroup.NewInstance();
		Taxon taxon1 = Taxon.NewInstance(BotanicalName.NewInstance(rank, "Test3", null, null, null, null, null, null, null), null);
		Synonym synonym0 = Synonym.NewInstance(BotanicalName.NewInstance(rank, "Test2", null, null, null, null, null, null, null), null);
		Synonym synonym1 = Synonym.NewInstance(BotanicalName.NewInstance(rank, "Test2", null, null, null, null, null, null, null), null);
		Synonym synonym2 = Synonym.NewInstance(BotanicalName.NewInstance(rank, "Test4", null, null, null, null, null, null, null), null);
		synonym0.getName().setHomotypicalGroup(taxon1.getHomotypicGroup());
		synonym2.getName().setHomotypicalGroup(synonym1.getHomotypicGroup());
		//tax2.addHeterotypicSynonymName(synonym.getName());
		taxon1.addSynonym(synonym1, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		taxon1.addSynonym(synonym2, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		
		service.save(synonym1);
		service.save(synonym2);
		service.save(taxon1);
		
		List<List<Synonym>> heteroSyns = service.getHeterotypicSynonymyGroups(taxon1, null);
		Assert.assertEquals("There should be 1 heterotypic group", 1, heteroSyns.size());
		List<Synonym> synList = heteroSyns.get(0);
		Assert.assertEquals("There should be 2 heterotypic syns in group 1", 2, synList.size());
		
		//test sec
		synonym2.setSec(ref1); 
		heteroSyns = service.getHeterotypicSynonymyGroups(taxon1, null);
		Assert.assertEquals("There should be 1 heterotypic group", 1, heteroSyns.size());
		synList = heteroSyns.get(0);
		Assert.assertEquals("getHeterotypicSynonymyGroups should be independent of sec reference", 2, synList.size());
	
	}
	

	@Test
	public final void testGetHomotypicSynonymsByHomotypicGroup(){
		Rank rank = Rank.SPECIES();
		Reference<?> ref1 = ReferenceFactory.newGeneric();
		//HomotypicalGroup group = HomotypicalGroup.NewInstance();
		Taxon taxon1 = Taxon.NewInstance(BotanicalName.NewInstance(rank, "Test3", null, null, null, null, null, null, null), null);
		Synonym synonym0 = Synonym.NewInstance(BotanicalName.NewInstance(rank, "Test2", null, null, null, null, null, null, null), null);
		Synonym synonym1 = Synonym.NewInstance(BotanicalName.NewInstance(rank, "Test2", null, null, null, null, null, null, null), null);
		Synonym synonym2 = Synonym.NewInstance(BotanicalName.NewInstance(rank, "Test4", null, null, null, null, null, null, null), null);
		synonym0.getName().setHomotypicalGroup(taxon1.getHomotypicGroup());
		synonym2.getName().setHomotypicalGroup(synonym1.getHomotypicGroup());
		//tax2.addHeterotypicSynonymName(synonym.getName());
		taxon1.addSynonym(synonym0, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF());
		taxon1.addSynonym(synonym1, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		taxon1.addSynonym(synonym2, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		
		service.save(synonym1);
		service.save(synonym2);
		service.save(taxon1);
		
		List<Synonym> homoSyns = service.getHomotypicSynonymsByHomotypicGroup(taxon1, null);
		Assert.assertEquals("There should be 1 heterotypic group", 1, homoSyns.size());
		Assert.assertSame("The homotypic synonym should be synonym0", synonym0, homoSyns.get(0));
		
		//test sec
		synonym0.setSec(ref1); 
		homoSyns = service.getHomotypicSynonymsByHomotypicGroup(taxon1, null);
		Assert.assertEquals("getHeterotypicSynonymyGroups should be independent of sec reference", 1, homoSyns.size());
	
	}
	
	
}
