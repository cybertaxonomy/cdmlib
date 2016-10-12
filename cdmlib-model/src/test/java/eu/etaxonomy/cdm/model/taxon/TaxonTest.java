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
	@SuppressWarnings("unused")
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
		assertTrue(syn1.getAcceptedTaxon().equals(freeT));
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

    @Test
    public void testAddHomotypicSynonymName(){
        TaxonNameBase<?,?> taxonName = BotanicalName.NewInstance(null);
        Taxon taxon = Taxon.NewInstance(taxonName, null);
        TaxonNameBase<?,?> synonymName1 = BotanicalName.NewInstance(null);

        // add a synonym to the taxon
        Synonym synonym1 = taxon.addHomotypicSynonymName(synonymName1);
        // get the homotypic group of that synonym
        HomotypicalGroup homotypicGroupOfSynonym = synonym1.getHomotypicGroup();
        // everything is fine
        Assert.assertEquals("We should have two names in the homotypic group",
                2, homotypicGroupOfSynonym.getTypifiedNames().size());
    }

    @Test
    public void testAddHomotypicSynonym(){
        TaxonNameBase<?,?> taxonName = BotanicalName.NewInstance(null);
        Taxon taxon = Taxon.NewInstance(taxonName, null);
        TaxonNameBase<?,?> synonymName1 = BotanicalName.NewInstance(null);
        Synonym synonym = Synonym.NewInstance(synonymName1, null);

        // add a synonym to the taxon
        taxon.addHomotypicSynonym(synonym);
        //synonym type must be homotypic
        Assert.assertEquals("Synonym must be homotypic", SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF(), synonym.getType());
        // get the homotypic group of that synonym
        HomotypicalGroup homotypicGroupOfSynonym = synonym.getHomotypicGroup();
        // everything is fine
        Assert.assertEquals("We should have two names in the homotypic group",
                2, homotypicGroupOfSynonym.getTypifiedNames().size());
    }


	@Test
	public void testAddRemoveSynonymInSameGroup(){
		TaxonNameBase<?,?> taxonName = BotanicalName.NewInstance(null);
		Taxon taxon = Taxon.NewInstance(taxonName, null);
		TaxonNameBase<?,?> synonymName1 = BotanicalName.NewInstance(null);
		TaxonNameBase<?,?> synonymName2 = BotanicalName.NewInstance(null);

		// add a synonym to the taxon
		Synonym synonym1 = taxon.addHeterotypicSynonymName(synonymName1);
		// get the homotypic group of that synonym
		HomotypicalGroup homotypicGroupOfSynonym = synonym1.getHomotypicGroup();
		// add another synonym into the homotypic group we just created
		Synonym synonym2 = taxon.addHeterotypicSynonymName(synonymName2, null, null, homotypicGroupOfSynonym);
		// everything is fine
		Assert.assertEquals("We should have two synonyms in the group", 2, taxon.getSynonymsInGroup(homotypicGroupOfSynonym).size());

		// removing the synonym from the taxon
		taxon.removeSynonym(synonym2);

		// get the homotypical group via the methods in Taxon
		HomotypicalGroup homotypicGroupViaTaxon = taxon.getHeterotypicSynonymyGroups().iterator().next();

		// the group is for sure the same as the synonyms one
		Assert.assertSame("Accessing the homotypic group via the taxon methods should result in the same object",
		        homotypicGroupOfSynonym, homotypicGroupViaTaxon);

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
