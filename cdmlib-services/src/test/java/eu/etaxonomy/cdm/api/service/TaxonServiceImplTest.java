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
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 *
 */
public class TaxonServiceImplTest extends CdmTransactionalIntegrationTest {
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
	
	@Test
	@DataSet("TaxonServiceImplTest.testDeleteSynonym.xml")
	public final void testDeleteSynonymSynonymTaxonBoolean(){
		final String[]tableNames = {"TaxonBase","TaxonBase_AUD", "TaxonNameBase","TaxonNameBase_AUD",
				"SynonymRelationship","SynonymRelationship_AUD",
				"HomotypicalGroup","HomotypicalGroup_AUD"};
//		BotanicalName taxonName1 = BotanicalName.NewInstance(Rank.SPECIES());
//		taxonName1.setTitleCache("TaxonName1",true);
//		BotanicalName taxonName2 = BotanicalName.NewInstance(Rank.SPECIES());
//		taxonName2.setTitleCache("TaxonName2",true);
//		BotanicalName synonymName1 = BotanicalName.NewInstance(Rank.SPECIES());
//		synonymName1.setTitleCache("Synonym1",true);
//		BotanicalName synonymName2 = BotanicalName.NewInstance(Rank.SPECIES());
//		synonymName2.setTitleCache("Synonym2",true);
//		
//		Reference<?> sec = null;
//		Taxon taxon1 = Taxon.NewInstance(taxonName1, sec);
//		Taxon taxon2 = Taxon.NewInstance(taxonName2, sec);
//		Synonym synonym1 = Synonym.NewInstance(synonymName1, sec);
//		Synonym synonym2 = Synonym.NewInstance(synonymName2, sec);
//		
//		SynonymRelationship rel1 = taxon1.addSynonym(synonym1, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
//		SynonymRelationship rel = taxon2.addSynonym(synonym1, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
//		rel.setProParte(true);
//		rel1.setProParte(true);
//		
//		service.save(taxon1);
//		service.save(synonym2);
//		
//		this.setComplete();
//		this.endTransaction();
//		
//		
		int nSynonyms = service.count(Synonym.class);
		Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
		int nNames = nameService.count(TaxonNameBase.class);
		Assert.assertEquals("There should  be 4 names in the database", 4, nNames);
		
//		UUID uuidTaxon1=UUID.fromString("c47fdb72-f32c-452e-8305-4b44f01179d0");
//		UUID uuidTaxon2=UUID.fromString("2d9a642d-5a82-442d-8fec-95efa978e8f8");
		UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");
//		UUID uuidSynonym2=UUID.fromString("f8d86dc9-5f18-4877-be46-fbb9412465e4");
		
		Synonym synonym1 = (Synonym)service.load(uuidSynonym1);
		service.deleteSynonym(synonym1, null, true);
		
		this.commitAndStartNewTransaction(tableNames);
		
		nSynonyms = service.count(Synonym.class);
		Assert.assertEquals("There should be 1 synonym left in the database", 1, nSynonyms);
		nNames = nameService.count(TaxonNameBase.class);
		Assert.assertEquals("There should be 3 names left in the database", 3, nNames);
		int nRelations = service.countAllRelationships();
		Assert.assertEquals("There should be no relationship left in the database", 0, nRelations);
	}
	
	@Test
	@DataSet("TaxonServiceImplTest.testDeleteSynonym.xml")
	public final void testDeleteSynonymSynonymTaxonBooleanRelToOneTaxon(){
		final String[]tableNames = {"TaxonBase","TaxonBase_AUD", "TaxonNameBase","TaxonNameBase_AUD",
				"SynonymRelationship","SynonymRelationship_AUD",
				"HomotypicalGroup","HomotypicalGroup_AUD"};
		
		int nSynonyms = service.count(Synonym.class);
		Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
		int nNames = nameService.count(TaxonNameBase.class);
		Assert.assertEquals("There should  be 4 names in the database", 4, nNames);
		
		UUID uuidTaxon1=UUID.fromString("c47fdb72-f32c-452e-8305-4b44f01179d0");
		UUID uuidTaxon2=UUID.fromString("2d9a642d-5a82-442d-8fec-95efa978e8f8");
		UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");
//		UUID uuidSynonym2=UUID.fromString("f8d86dc9-5f18-4877-be46-fbb9412465e4");
		
		Taxon taxon2 = (Taxon)service.load(uuidTaxon2);
		Synonym synonym1 = (Synonym)service.load(uuidSynonym1);
		
		taxon2.removeSynonym(synonym1, false);
		service.saveOrUpdate(taxon2);
		this.setComplete();
		this.endTransaction();
		
		this.startNewTransaction();
		nSynonyms = service.count(Synonym.class);
		Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
		nNames = nameService.count(TaxonNameBase.class);
		Assert.assertEquals("There should  be 4 names in the database", 4, nNames);
		int nRelations = service.countAllRelationships();
		Assert.assertEquals("There should be 1 relationship left in the database", 1, nRelations);

		taxon2 = (Taxon)service.load(uuidTaxon2);
		synonym1 = (Synonym)service.load(uuidSynonym1);
		
		service.deleteSynonym(synonym1, null, true);
		
		this.commitAndStartNewTransaction(tableNames);
		
		nSynonyms = service.count(Synonym.class);
		Assert.assertEquals("There should be 1 synonym left in the database", 1, nSynonyms);
		nNames = nameService.count(TaxonNameBase.class);
		Assert.assertEquals("There should be 3 names left in the database", 3, nNames);
		nRelations = service.countAllRelationships();
		Assert.assertEquals("There should be no relationship left in the database", 0, nRelations);

	}
	
	@Test
	@DataSet("TaxonServiceImplTest.testDeleteSynonym.xml")
	public final void testDeleteSynonymSynonymTaxonBooleanDeleteOneTaxon(){
		final String[]tableNames = {"TaxonBase","TaxonBase_AUD", "TaxonNameBase","TaxonNameBase_AUD",
				"SynonymRelationship","SynonymRelationship_AUD",
				"HomotypicalGroup","HomotypicalGroup_AUD"};
		
		int nSynonyms = service.count(Synonym.class);
		Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
		int nNames = nameService.count(TaxonNameBase.class);
		Assert.assertEquals("There should  be 4 names in the database", 4, nNames);
		
		UUID uuidTaxon1=UUID.fromString("c47fdb72-f32c-452e-8305-4b44f01179d0");
		UUID uuidTaxon2=UUID.fromString("2d9a642d-5a82-442d-8fec-95efa978e8f8");
		UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");
		UUID uuidSynonym2=UUID.fromString("f8d86dc9-5f18-4877-be46-fbb9412465e4");
		
		Taxon taxon1 = (Taxon)service.load(uuidTaxon1);
		Taxon taxon2 = (Taxon)service.load(uuidTaxon2);
		Synonym synonym1 = (Synonym)service.load(uuidSynonym1);
		
		service.deleteSynonym(synonym1, taxon1, true);
		
		this.commitAndStartNewTransaction(tableNames);
		
		nSynonyms = service.count(Synonym.class);
		Assert.assertEquals("There should still be 2 synonyms left in the database (synonym is related to taxon2)", 2, nSynonyms);
		nNames = nameService.count(TaxonNameBase.class);
		Assert.assertEquals("There should be 4 names left in the database (name not deleted as synonym was not deleted)", 4, nNames);
		int nRelations = service.countAllRelationships();
		Assert.assertEquals("There should be 1 relationship left in the database", 1, nRelations);
		
	}
	
	@Test
	@DataSet("TaxonServiceImplTest.testDeleteSynonym.xml")
	public final void testDeleteSynonymSynonymTaxonBooleanWithRelatedName(){
		final String[]tableNames = {"TaxonBase","TaxonBase_AUD", "TaxonNameBase","TaxonNameBase_AUD",
				"SynonymRelationship","SynonymRelationship_AUD",
				"HomotypicalGroup","HomotypicalGroup_AUD"};
		
		int nSynonyms = service.count(Synonym.class);
		Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
		int nNames = nameService.count(TaxonNameBase.class);
		Assert.assertEquals("There should  be 4 names in the database", 4, nNames);
		
		UUID uuidTaxon1=UUID.fromString("c47fdb72-f32c-452e-8305-4b44f01179d0");
		UUID uuidTaxon2=UUID.fromString("2d9a642d-5a82-442d-8fec-95efa978e8f8");
		UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");
		UUID uuidSynonym2=UUID.fromString("f8d86dc9-5f18-4877-be46-fbb9412465e4");
		UUID uuidSynonymName2=UUID.fromString("613f3c93-013e-4ffc-aadc-1c98d71c335e");
		
		Synonym synonym1 = (Synonym)service.load(uuidSynonym1);
		TaxonNameBase name2 = (TaxonNameBase)nameService.load(uuidSynonymName2);
		synonym1.getName().addRelationshipFromName(name2, NameRelationshipType.LATER_HOMONYM(), null);
		
		service.deleteSynonym(synonym1, null, true);
		
		this.commitAndStartNewTransaction(tableNames);
		
		nSynonyms = service.count(Synonym.class);
		Assert.assertEquals("There should still be 1 synonyms left in the database", 1, nSynonyms);
		nNames = nameService.count(TaxonNameBase.class);
		Assert.assertEquals("There should be 4 names left in the database (name is related to synonymName2)", 4, nNames);
		int nRelations = service.countAllRelationships();
		//may change with better implementation of countAllRelationships (see #2653)
		Assert.assertEquals("There should be 1 relationship left in the database (the name relationship)", 1, nRelations);

		//clean up database
		name2 = (TaxonNameBase)nameService.load(uuidSynonymName2);
		NameRelationship rel = CdmBase.deproxy(name2.getNameRelations().iterator().next(), NameRelationship.class);
		name2.removeNameRelationship(rel);
		nameService.save(name2);
		this.setComplete();
		this.endTransaction();
		
	}

	@Test
	@DataSet("TaxonServiceImplTest.testDeleteSynonym.xml")
	public final void testDeleteSynonymSynonymTaxonBooleanWithRollback(){
		final String[]tableNames = {"TaxonBase","TaxonBase_AUD", "TaxonNameBase","TaxonNameBase_AUD",
				"SynonymRelationship","SynonymRelationship_AUD",
				"HomotypicalGroup","HomotypicalGroup_AUD"};
		
		int nSynonyms = service.count(Synonym.class);
		Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
		int nNames = nameService.count(TaxonNameBase.class);
		Assert.assertEquals("There should  be 4 names in the database", 4, nNames);
		int nRelations = service.countAllRelationships();
		//may change with better implementation of countAllRelationships (see #2653)
		Assert.assertEquals("There should be 2 relationship in the database (the 2 synonym relationship) but no name relationship", 2, nRelations);
		
		UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");
		UUID uuidSynonymName2=UUID.fromString("613f3c93-013e-4ffc-aadc-1c98d71c335e");
		
		Synonym synonym1 = (Synonym)service.load(uuidSynonym1);
		TaxonNameBase name2 = (TaxonNameBase)nameService.load(uuidSynonymName2);
		synonym1.getName().addRelationshipFromName(name2, NameRelationshipType.LATER_HOMONYM(), null);
		
		service.deleteSynonym(synonym1, null, true);
		
		this.rollback();
//		printDataSet(System.out, tableNames);
		this.startNewTransaction();
		
		nSynonyms = service.count(Synonym.class);
		Assert.assertEquals("There should still be 2 synonyms left in the database", 2, nSynonyms);
		nNames = nameService.count(TaxonNameBase.class);
		Assert.assertEquals("There should be 4 names left in the database", 4, nNames);
		nRelations = service.countAllRelationships();
		//may change with better implementation of countAllRelationships (see #2653)
		Assert.assertEquals("There should be 2 relationship in the database (the 2 synonym relationship) but no name relationship", 2, nRelations);
		
	}
	
	@Test
	@DataSet("TaxonServiceImplTest.testDeleteSynonym.xml")
	public final void testDeleteSynonymSynonymTaxonBooleanWithoutTransaction(){
		final String[]tableNames = {"TaxonBase","TaxonBase_AUD", "TaxonNameBase","TaxonNameBase_AUD",
				"SynonymRelationship","SynonymRelationship_AUD",
				"HomotypicalGroup","HomotypicalGroup_AUD"};
		
		int nSynonyms = service.count(Synonym.class);
		Assert.assertEquals("There should be 2 synonyms in the database", 2, nSynonyms);
		int nNames = nameService.count(TaxonNameBase.class);
		Assert.assertEquals("There should  be 4 names in the database", 4, nNames);
		int nRelations = service.countAllRelationships();
		//may change with better implementation of countAllRelationships (see #2653)
		Assert.assertEquals("There should be 2 relationship in the database (the 2 synonym relationship) but no name relationship", 2, nRelations);
		
		UUID uuidSynonym1=UUID.fromString("7da85381-ad9d-4886-9d4d-0eeef40e3d88");
		UUID uuidSynonymName2=UUID.fromString("613f3c93-013e-4ffc-aadc-1c98d71c335e");
		
		Synonym synonym1 = (Synonym)service.load(uuidSynonym1);
		TaxonNameBase name2 = (TaxonNameBase)nameService.load(uuidSynonymName2);
		synonym1.getName().addRelationshipFromName(name2, NameRelationshipType.LATER_HOMONYM(), null);
		
		service.saveOrUpdate(synonym1);
		
		this.setComplete();
		this.endTransaction();
		
		printDataSet(System.out, tableNames);
		
		//out of wrapping transaction
		service.deleteSynonym(synonym1, null, true);
		
		this.startNewTransaction();
		
		nSynonyms = service.count(Synonym.class);
		Assert.assertEquals("There should still be 1 synonyms left in the database. The rollback on name delete should not lead to rollback in synonym delete.", 1, nSynonyms);
		nNames = nameService.count(TaxonNameBase.class);
		Assert.assertEquals("There should be 4 names left in the database", 4, nNames);
		nRelations = service.countAllRelationships();
		//may change with better implementation of countAllRelationships (see #2653)
		Assert.assertEquals("There should be 1 name relationship and no synonym relationship in the database", 1, nRelations);
		
	}
	
}
