/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
//import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

public class TaxonTest extends EntityTestBase {
	private static final Logger logger = Logger.getLogger(TaxonTest.class);


	private Reference sec;
	private Reference misSec;
	private ZoologicalName name1;
	private BotanicalName name2;
	private Taxon rootT;
	private Taxon child1;
	private Taxon child2;
	private Synonym syn1;
	private Synonym syn2;
	private BotanicalName name3;
	private BotanicalName name4;
	private Taxon freeT;
	private Taxon misTaxon1;
	private Taxon misTaxon2;

	@BeforeClass
	public static void setUpBeforeClass() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
	}

	@Before
	public void setUp() throws Exception {
		Person linne =new Person("Carl", "Linné", "L.");
		sec= ReferenceFactory.newBook();
		sec.setAuthorship(linne);
		sec.setTitleCache("Schönes saftiges Allgäu", true);
		misSec = ReferenceFactory.newBook();
		misSec.setTitleCache("Stupid book", true);

		name1 = ZoologicalName.NewInstance(Rank.SPECIES(),"Panthera",null,"onca",null,linne,null,"p.1467", null);
		name2 = BotanicalName.NewInstance(Rank.SPECIES(),"Abies",null,"alba",null,linne,null,"p.317", null);
		name3 = BotanicalName.NewInstance(Rank.SUBSPECIES(),"Polygala",null,"vulgaris","alpina",linne,null,"p.191", null);
		name4 = BotanicalName.NewInstance(Rank.SPECIES(),"Cichoria",null,"carminata",null,linne,null,"p.14", null);
		rootT = Taxon.NewInstance(name1,sec);
		freeT = Taxon.NewInstance(name4,sec);
		// taxonomic children
		child1 = Taxon.NewInstance(name2,sec);
		child2 = Taxon.NewInstance(name3,sec);

//		Classification newTree = Classification.NewInstance("testTree");
//		newTree.addParentChild(rootT, child1, sec, "p.998");
//		newTree.addParentChild(rootT, child2, sec, "p.987");

		rootT.addTaxonomicChild(child1, sec, "p.998");
		rootT.addTaxonomicChild(child2, sec, "p.987");
		// synonymy
		syn1=Synonym.NewInstance(name1,sec);
		syn2=Synonym.NewInstance(name2,sec);
		child1.addSynonym(syn1, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		child2.addSynonym(syn2, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF());
		//misaplication
		misTaxon1 = Taxon.NewInstance(name4, misSec);
		misTaxon2 = Taxon.NewInstance(name4, misSec);
		rootT.addMisappliedName(misTaxon1, null, "99");
		child1.addMisappliedName(misTaxon2, null, "44");
	}

	@Test
	public void testGenerateTitle() {
		assertTrue(rootT.generateTitle().startsWith(rootT.getName().getTitleCache()));
	}

	@Test
	public void testAddTaxonomicChild() {

		rootT.addTaxonomicChild(freeT, null, null);
		Assert.assertEquals(Integer.valueOf(3), Integer.valueOf(rootT.getTaxonomicChildren().size()));
	}

	@Test
	public void testGetTaxonomicParent() {
		assertEquals(rootT, child2.getTaxonomicParent());
	}

	@Test
	public void testSetTaxonomicParent() {
		child2.setTaxonomicParent(child1, null, null);
		assertEquals(child1, child2.getTaxonomicParent());
	}

	@Test
	public void testGetTaxonomicChildren() {
		Set<Taxon> kids=rootT.getTaxonomicChildren();
		assertTrue(kids.size()==2 && kids.contains(child1) && kids.contains(child2));
	}

	@Test
	public void testHasTaxonomicChildren() {
		assertFalse(child2.hasTaxonomicChildren());
		assertTrue(rootT.hasTaxonomicChildren());
		rootT.removeTaxonomicChild(child1);
		assertTrue(rootT.hasTaxonomicChildren());
		rootT.removeTaxonomicChild(child2);
		assertFalse(rootT.hasTaxonomicChildren());
	}

	@Test
	public void testGetTaxonomicChildrenCount() {
		assertEquals(0, child2.getTaxonomicChildrenCount());
		assertEquals(2, rootT.getTaxonomicChildrenCount());
		rootT.removeTaxonomicChild(child1);
		assertEquals(1, rootT.getTaxonomicChildrenCount());
		rootT.removeTaxonomicChild(child2);
		assertEquals(0, rootT.getTaxonomicChildrenCount());
	}

	@Test
	public void testIsMisappliedName() {
		assertFalse(child2.isMisapplication());
		assertFalse(rootT.isMisapplication());
		assertTrue(misTaxon1.isMisapplication());
		assertTrue(misTaxon2.isMisapplication());
	}

	@Test
	public void testGetSynonyms() {
		assertTrue(child1.getSynonyms().contains(syn1));
		assertTrue(child2.getSynonyms().contains(syn2));
		assertTrue(rootT.getSynonyms().isEmpty());
	}

	@Test
	public void testGetSynonymNames() {
		assertTrue(child1.getSynonymNames().contains(name1));
		assertTrue(child2.getSynonymNames().contains(name2));
		assertTrue(rootT.getSynonymNames().isEmpty());
	}

	@Test
	public void testAddSynonym() {
		freeT.addSynonym(syn1, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		assertTrue(freeT.getSynonyms().contains(syn1));
		assertTrue(syn1.getAcceptedTaxa().contains(freeT));
		assertFalse(freeT.getSynonyms().contains(syn2));
	}

	@Test
	public void testAddAndRemoveDescriptionTaxonDescription() {
		TaxonDescription desc = TaxonDescription.NewInstance();
		rootT.addDescription(desc);  //test if reflection method in addDescription() works
		assertTrue("The description has not properly been added to the taxon", rootT.getDescriptions().contains(desc));
		assertEquals("The taxon has not properly been added to the description", rootT, desc.getTaxon());
		rootT.removeDescription(desc); //test if reflection method in removeDescription() works
		assertFalse("The description has not properly been removed from the taxon", rootT.getDescriptions().contains(desc));
		assertEquals("The taxon has not properly been removed from the description", null, desc.getTaxon());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#getDescriptions()}.
	 */
	@Test
	public void testGetDescriptions() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#setDescriptions(java.util.Set)}.
	 */
	@Test
	public void testSetDescriptions() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#addDescription(eu.etaxonomy.cdm.model.description.TaxonDescription)}.
	 */
	@Test
	public void testAddDescription() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#removeDescription(eu.etaxonomy.cdm.model.description.TaxonDescription)}.
	 */
	@Test
	public void testRemoveDescription() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#getSynonymRelations()}.
	 */
	@Test
	public void testGetSynonymRelations() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#setSynonymRelations(java.util.Set)}.
	 */
	@Test
	public void testSetSynonymRelations() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#addSynonymRelation(eu.etaxonomy.cdm.model.taxon.SynonymRelationship)}.
	 */
	@Test
	public void testAddSynonymRelation() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#removeSynonymRelation(eu.etaxonomy.cdm.model.taxon.SynonymRelationship)}.
	 */
	@Test
	public void testRemoveSynonymRelation() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#getRelationsFromThisTaxon()}.
	 */
	@Test
	public void testGetRelationsFromThisTaxon() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#setRelationsFromThisTaxon(java.util.Set)}.
	 */
	@Test
	public void testSetRelationsFromThisTaxon() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#getRelationsToThisTaxon()}.
	 */
	@Test
	public void testGetRelationsToThisTaxon() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#setRelationsToThisTaxon(java.util.Set)}.
	 */
	@Test
	public void testSetRelationsToThisTaxon() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#getTaxonRelations()}.
	 */
	@Test
	public void testGetTaxonRelations() {
		Taxon taxon = Taxon.NewInstance(null, null);
		taxon.addTaxonRelation(Taxon.NewInstance(null, null), TaxonRelationshipType.CONTRADICTION(), null, null);
		Set<TaxonRelationship> relationships = taxon.getTaxonRelations();
		assertTrue("There should be exactly one relationship", relationships.size() == 1);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#removeTaxonRelation(eu.etaxonomy.cdm.model.taxon.TaxonRelationship)}.
	 */
	@Test
	public void testRemoveTaxonRelation() {
		Taxon taxon = Taxon.NewInstance(null, null);
		taxon.addTaxonRelation(Taxon.NewInstance(null, null), TaxonRelationshipType.CONTRADICTION(), null, null);
		assertTrue("There should be exactly one taxon relationship", taxon.getTaxonRelations().size() == 1);
		TaxonRelationship relationship = (TaxonRelationship) taxon.getTaxonRelations().toArray()[0];
		assertNotNull("Relationship should not be null", relationship);
		taxon.removeTaxonRelation(relationship);
		assertTrue("There should be no taxon relationships", taxon.getTaxonRelations().size() == 0);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#addTaxonRelation(eu.etaxonomy.cdm.model.taxon.TaxonRelationship)}.
	 */
	@Test
	public void testAddTaxonRelationTaxonRelationship() {
		Taxon taxon = Taxon.NewInstance(null, null);
		taxon.addTaxonRelation(Taxon.NewInstance(null, null), TaxonRelationshipType.CONTRADICTION(), null, null);
		assertTrue("There should be exactly one taxon relationship", taxon.getTaxonRelations().size() == 1);
		TaxonRelationship relationship = (TaxonRelationship) taxon.getTaxonRelations().toArray()[0];
		assertNotNull("Relationship should not be null", relationship);
		taxon.removeTaxonRelation(relationship);
		assertTrue("There should be no taxon relationships", taxon.getTaxonRelations().size() == 0);
		taxon.addTaxonRelation(relationship);
		assertEquals("There should be exactly one taxon relationships", 1, taxon.getTaxonRelations().size());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#addRelationship(eu.etaxonomy.cdm.model.common.RelationshipBase)}.
	 */
	@Test
	public void testAddRelationship() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#addTaxonRelation(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String)}.
	 */
	@Test
	public void testAddTaxonRelationTaxonTaxonRelationshipTypeReferenceBaseString() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#addMisappliedName(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String)}.
	 */
	@Test
	public void testAddMisappliedName() {
		logger.warn("Not yet implemented");
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#hasSynonyms()}.
	 */
	@Test
	public void testHasSynonyms() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#getMisappliedNames()}.
	 */
	@Test
	public void testGetMisappliedNames() {
		logger.warn("Not yet implemented");
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#getSynonymsSortedByType()}.
	 */
	@Test
	public void testGetSynonymsSortedByType() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#addSynonym(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType)}.
	 */
	@Test
	public void testAddSynonymSynonymSynonymRelationshipType() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#addSynonym(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String)}.
	 */
	@Test
	public void testAddSynonymSynonymSynonymRelationshipTypeReferenceBaseString() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#addSynonymName(eu.etaxonomy.cdm.model.name.TaxonNameBase, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType)}.
	 */
	@Test
	public void testAddSynonymNameTaxonNameBaseSynonymRelationshipType() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#addSynonymName(eu.etaxonomy.cdm.model.name.TaxonNameBase, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String)}.
	 */
	@Test
	public void testAddSynonymNameTaxonNameBaseSynonymRelationshipTypeReferenceBaseString() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#addHeterotypicSynonymName(eu.etaxonomy.cdm.model.name.TaxonNameBase)}.
	 */
	@Test
	public void testAddHeterotypicSynonymNameTaxonNameBase() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#addHeterotypicSynonymName(eu.etaxonomy.cdm.model.name.TaxonNameBase, eu.etaxonomy.cdm.model.name.HomotypicalGroup, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String)}.
	 */
	@Test
	public void testAddHeterotypicSynonymNameTaxonNameBaseHomotypicalGroupReferenceBaseString() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#addHomotypicSynonymName(eu.etaxonomy.cdm.model.name.TaxonNameBase, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String)}.
	 */
	@Test
	public void testAddHomotypicSynonymName() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#addHomotypicSynonym(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String)}.
	 */
	@Test
	public void testAddHomotypicSynonym() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#removeSynonym(eu.etaxonomy.cdm.model.taxon.Synonym)}.
	 */
	@Test
	public void testRemoveSynonym() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#iterator()}.
	 */
	@Test
	public void testIterator() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#getHomotypicSynonymsByHomotypicGroup()}.
	 */
	@Test
	public void testGetHomotypicSynonymsByHomotypicGroup() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#getHomotypicSynonymsByHomotypicRelationship()}.
	 */
	@Test
	public void testGetHomotypicSynonymsByHomotypicRelationship() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#getHomotypicSynonymyGroups()}.
	 */
	@Test
	public void testGetHomotypicSynonymyGroups() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Taxon#getHeterotypicSynonymyGroups()}.
	 */
	@Test
	public void testGetHeterotypicSynonymyGroups() {
		logger.warn("Not yet implemented");
	}


	@Test
	public void testAddRemoveSynonymInSameGroup(){
		TaxonNameBase<?,?> taxonName = BotanicalName.NewInstance(null);
		Taxon taxon = Taxon.NewInstance(taxonName, null);
		TaxonNameBase<?,?> synonymName1 = BotanicalName.NewInstance(null);
		TaxonNameBase<?,?> synonymName2 = BotanicalName.NewInstance(null);

		// add a synonym to the taxon
		Synonym synonym1 = taxon.addHeterotypicSynonymName(synonymName1).getSynonym();
		// get the homotypic group of that synonym
		HomotypicalGroup homotypicGroupOfSynonym = synonym1.getHomotypicGroup();
		// add another synonym into the homotypic group we just created
		Synonym synonym2 = taxon.addHeterotypicSynonymName(synonymName2, homotypicGroupOfSynonym, null, null).getSynonym();
		// everything is fine
		Assert.assertEquals("We should have two synonyms in the group", 2, taxon.getSynonymsInGroup(homotypicGroupOfSynonym).size());

		// removing the synonym from the taxon
		taxon.removeSynonym(synonym2);

		// get the homotypical group via the methods in Taxon
		HomotypicalGroup homotypicGroupViaTaxon = taxon.getHeterotypicSynonymyGroups().iterator().next();

		// the group is for sure the same as the synonyms one
		Assert.assertSame("Accessing the homotypic group via the taxon methods should result in the same object", homotypicGroupOfSynonym, homotypicGroupViaTaxon);

		// although it might be correct that the synonym is not deleted from the taxonomic group
		// we would not expect it to be here, since we just deleted it from the taxon and are accessing synonyms
		// via methods in Taxon
		Assert.assertEquals("When accessing the homotypic groups via taxon we would not expect the synonym we just deleted",
				1, taxon.getSynonymsInGroup(homotypicGroupViaTaxon).size());
	}


	@Test
	public void testClone(){
		Taxon clone = (Taxon)child2.clone();
		assertNotNull(clone);
		assertEquals(0,clone.getTaxonNodes().size());
		assertSame(clone.getName(), child2.getName());
	}

	@Test
	public void beanTests(){
//	    #5307 Test that BeanUtils does not fail
	    BeanUtils.getPropertyDescriptors(Taxon.class);
	    BeanUtils.getPropertyDescriptors(Synonym.class);
	}

}
