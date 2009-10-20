/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy;

/**
 * @author a.mueller
 * @created 28.06.2008
 * @version 1.0
 */
public class TaxonNameBaseTest {
	private static final Logger logger = Logger.getLogger(TaxonNameBaseTest.class);

	private TaxonNameBaseTestClass nameBase1;
	private TaxonNameBaseTestClass nameBase2;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		nameBase1 = new TaxonNameBaseTestClass(null,null);
		nameBase2 = new TaxonNameBaseTestClass(Rank.GENUS());
	}

	
	private class TaxonNameBaseTestClass extends TaxonNameBase<TaxonNameBaseTestClass, INameCacheStrategy<TaxonNameBaseTestClass>>{
		public TaxonNameBaseTestClass(){super();};
		public TaxonNameBaseTestClass(Rank rank){super(rank);};
		public TaxonNameBaseTestClass(HomotypicalGroup hg){super(hg);};
		public TaxonNameBaseTestClass(Rank rank, HomotypicalGroup hg){super(rank, hg);};
		@Override
		public boolean isCodeCompliant(){return false;};
		@Override
		public void setCacheStrategy(INameCacheStrategy strategy){};
		@Override
		public INameCacheStrategy getCacheStrategy(){return null;};
		@Override
		public NomenclaturalCode getNomenclaturalCode(){return null;};
		@Override
		public String generateFullTitle(){return null;}
		@Override
		public String generateTitle() {
			return null;
		}
		@Override
		protected Map<String, java.lang.reflect.Field> getAllFields() {
			return null;
		};
		
	}

	
/** *************************  TESTS ******************************************************/
	

	@Test
	public void testGenerateFullTitle() {
		//abstract
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#TaxonNameBase()}.
	 */
	@Test
	public void testTaxonNameBase() {
		assertNotNull(nameBase1);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#TaxonNameBase(eu.etaxonomy.cdm.model.name.Rank)}.
	 */
	@Test
	public void testTaxonNameBaseRank() {
		assertNotNull(nameBase2);
		assertEquals(Rank.GENUS(), nameBase2.getRank());
		Rank rank = null;
		TaxonNameBase testName = new TaxonNameBaseTestClass(rank);
		assertNull(testName.getRank());		
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#TaxonNameBase(eu.etaxonomy.cdm.model.name.HomotypicalGroup)}.
	 */
	@Test
	public void testTaxonNameBaseHomotypicalGroup() {
		HomotypicalGroup hg = HomotypicalGroup.NewInstance();
		TaxonNameBase testHG = new TaxonNameBaseTestClass(hg);
		assertSame(hg, testHG.getHomotypicalGroup());
		HomotypicalGroup hgNull = null;
		TaxonNameBase testHGNull = new TaxonNameBaseTestClass(hgNull);
		assertNotNull(testHGNull.getHomotypicalGroup());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#TaxonNameBase(eu.etaxonomy.cdm.model.name.Rank, eu.etaxonomy.cdm.model.name.HomotypicalGroup)}.
	 */
	@Test
	public void testTaxonNameBaseRankHomotypicalGroup() {
		Rank rank = Rank.SPECIES();
		HomotypicalGroup hg = HomotypicalGroup.NewInstance();
		TaxonNameBase testHG = new TaxonNameBaseTestClass(rank, hg);
		assertSame(rank, testHG.getRank());
		assertSame(hg, testHG.getHomotypicalGroup());
		
		Rank rankNull = null;
		HomotypicalGroup hgNull = null;
		TaxonNameBase testHGNull = new TaxonNameBaseTestClass(rankNull, hgNull);
		assertEquals(rankNull, testHGNull.getRank());
		assertNotNull(testHGNull.getHomotypicalGroup());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#isCodeCompliant()}.
	 */
	@Test
	public void testIsCodeCompliant() {
		//is abstract
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getNameRelations()}.
	 */
	@Test
	public void testGetNameRelations() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#addRelationshipToName(eu.etaxonomy.cdm.model.name.TaxonNameBase, eu.etaxonomy.cdm.model.name.NameRelationshipType, java.lang.String)}.
	 */
	@Test
	public void testAddRelationshipToName() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#addRelationshipFromName(eu.etaxonomy.cdm.model.name.TaxonNameBase, eu.etaxonomy.cdm.model.name.NameRelationshipType, java.lang.String)}.
	 */
	@Test
	public void testAddRelationshipFromName() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#addNameRelationship(eu.etaxonomy.cdm.model.name.NameRelationship)}.
	 */
	@Test
	public void testAddNameRelationship() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#removeNameRelationship(eu.etaxonomy.cdm.model.name.NameRelationship)}.
	 */
	@Test
	public void testRemoveNameRelationship() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#addRelationship(eu.etaxonomy.cdm.model.common.RelationshipBase)}.
	 */
	@Test
	public void testAddRelationship() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getRelationsFromThisName()}.
	 */
	@Test
	public void testGetRelationsFromThisName() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getRelationsToThisName()}.
	 */
	@Test
	public void testGetRelationsToThisName() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getStatus()}.
	 * and for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#addStatus(eu.etaxonomy.cdm.model.name.NomenclaturalStatus)}.
	 * and for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#removeStatus(eu.etaxonomy.cdm.model.name.NomenclaturalStatus)}.
	 */
	@Test
	public void testGetAddStatus() {
		//Empty status set
		assertNotNull(nameBase1.getStatus());
		assertEquals(0, nameBase1.getStatus().size());
		//1 status set
		NomenclaturalStatus nomStatus = NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ALTERNATIVE());
		nameBase1.addStatus(nomStatus);
		assertNotNull(nameBase1.getStatus());
		assertEquals(1, nameBase1.getStatus().size());
		assertEquals(nomStatus, nameBase1.getStatus().iterator().next());
		//2 status set
		NomenclaturalStatus nomStatus2 = NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ALTERNATIVE());
		nameBase1.addStatus(nomStatus2);
		assertEquals(2, nameBase1.getStatus().size());
		assertTrue(nameBase1.getStatus().contains(nomStatus2));
		//remove
		nameBase1.removeStatus(nomStatus);
		assertEquals(1, nameBase1.getStatus().size());
		assertTrue(nameBase1.getStatus().contains(nomStatus2));
		//remove
		nameBase1.removeStatus(nomStatus2);
		assertEquals(0, nameBase1.getStatus().size());
		assertFalse(nameBase1.getStatus().contains(nomStatus2));
		
		
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#isOriginalCombination()}.
	 */
	@Test
	public void testIsOriginalCombination() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getBasionym()}.
	 */
	@Test
	public void testGetBasionym() {
		TaxonNameBase name1 = BotanicalName.NewInstance(null);
		TaxonNameBase basionym1 = BotanicalName.NewInstance(null);
		TaxonNameBase basionym2 = BotanicalName.NewInstance(null);
		
		Assert.assertEquals(null, name1.getBasionym());	
		name1.addBasionym(basionym1);
		Assert.assertEquals(basionym1, name1.getBasionym());
		name1.addBasionym(basionym2);
		TaxonNameBase oneOfThebasionyms = name1.getBasionym();
		Assert.assertTrue(oneOfThebasionyms == basionym1 || oneOfThebasionyms == basionym2 );
		name1.removeBasionyms();
		Assert.assertEquals(null, name1.getBasionym());	
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#addBasionym(eu.etaxonomy.cdm.model.name.TaxonNameBase)}.
	 */
	@Test
	public void testAddBasionymT() {
		assertNotSame(nameBase1.getHomotypicalGroup(), nameBase2.getHomotypicalGroup());
		assertFalse(nameBase1.getHomotypicalGroup().equals(nameBase2.getHomotypicalGroup()));
		nameBase1.addBasionym(nameBase2);
		assertTrue(nameBase1.getHomotypicalGroup().equals(nameBase2.getHomotypicalGroup()));
		assertSame(nameBase1.getHomotypicalGroup(), nameBase2.getHomotypicalGroup());
		logger.warn("not yet fully implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#addBasionym(eu.etaxonomy.cdm.model.name.TaxonNameBase, java.lang.String)}.
	 */
	@Test
	public void testAddBasionymTString() {
		logger.warn("not yet implemented");
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getCacheStrategy()}.
	 */
	@Test
	public void testGetCacheStrategy() {
		//is abstract
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#setCacheStrategy(eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy)}.
	 */
	@Test
	public void testSetCacheStrategy() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getRank()}.
	 */
	@Test
	public void testGetRank() {
		TaxonNameBase name1 = BotanicalName.NewInstance(null);
		assertNull("Rank shall be null", name1.getRank());
		name1.setRank(Rank.SPECIES());
		assertNotNull("Rank shall not be null", name1.getRank());
		name1.setRank(null);
		assertNull("Rank shall be null", name1.getRank());
		
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getNomenclaturalReference()}.
	 */
	@Test
	public void testGetSetNomenclaturalReference() {
		ReferenceFactory refFactory = ReferenceFactory.newInstance();
		INomenclaturalReference nr = (INomenclaturalReference) nameBase1.getNomenclaturalReference();
		assertNull("Nomenclatural Reference shall be null", nr);
		nameBase1.setNomenclaturalReference(refFactory.newGeneric());
		nr = (INomenclaturalReference) nameBase1.getNomenclaturalReference();
		assertNotNull("Nomenclatural Reference shall not be null", nr);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getAppendedPhrase()}.
	 */
	@Test
	public void testGetAppendedPhrase() {
		TaxonNameBase name1 = BotanicalName.NewInstance(null);
		String appPhrase = "appPhrase";
		Assert.assertNull(name1.getAppendedPhrase());
		name1.setAppendedPhrase(appPhrase);
		Assert.assertSame(appPhrase, name1.getAppendedPhrase());
		name1.setAppendedPhrase(null);
		Assert.assertNull(name1.getAppendedPhrase());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getNomenclaturalMicroReference()}.
	 */
	@Test
	public void testGetSetNomenclaturalMicroReference() {
		TaxonNameBase name1 = BotanicalName.NewInstance(null);
		String microRef = "micro";
		Assert.assertNull(name1.getNomenclaturalMicroReference());
		name1.setNomenclaturalMicroReference(microRef);
		Assert.assertSame(microRef, name1.getNomenclaturalMicroReference());
		name1.setNomenclaturalMicroReference(null);
		Assert.assertNull(name1.getNomenclaturalMicroReference());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getHasProblem()}.
	 */
	@Test
	public void testGetSetHasProblem() {
		TaxonNameBase name1 = BotanicalName.NewInstance(null);
		name1.setParsingProblem(0);
		Assert.assertFalse(name1.hasProblem());
		name1.setParsingProblem(1);
		Assert.assertTrue(name1.hasProblem());
		name1.setParsingProblem(0);
		Assert.assertFalse(name1.getParsingProblem()!=0);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getNameTypeDesignations()}.
	 */
	@Test
	public void testGetNameTypeDesignations() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#setNameTypeDesignations(java.util.Set)}.
	 */
	@Test
	public void testSetNameTypeDesignations() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#addNameTypeDesignation(eu.etaxonomy.cdm.model.name.TaxonNameBase, eu.etaxonomy.cdm.model.reference.ReferenceBase, java.lang.String, java.lang.String, boolean, boolean)}.
	 */
	@Test
	public void testAddNameTypeDesignation() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#removeNameTypeDesignation(eu.etaxonomy.cdm.model.name.NameTypeDesignation)}.
	 */
	@Test
	public void testRemoveNameTypeDesignation() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getSpecimenTypeDesignations()}.
	 */
	@Test
	public void testGetSpecimenTypeDesignations() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#setSpecimenTypeDesignations(java.util.Set)}.
	 */
	@Test
	public void testSetSpecimenTypeDesignations() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getSpecimenTypeDesignationsOfHomotypicalGroup()}.
	 */
	@Test
	public void testGetSpecimenTypeDesignationsOfHomotypicalGroup() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#addSpecimenTypeDesignation(eu.etaxonomy.cdm.model.occurrence.Specimen, eu.etaxonomy.cdm.model.name.TypeDesignationStatus, eu.etaxonomy.cdm.model.reference.ReferenceBase, java.lang.String, java.lang.String, boolean)}.
	 */
	@Test
	public void testAddSpecimenTypeDesignationSpecimenTypeDesignationStatusReferenceBaseStringStringBoolean() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#addSpecimenTypeDesignation(eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation)}.
	 */
	@Test
	public void testAddSpecimenTypeDesignationSpecimenTypeDesignation() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#removeSpecimenTypeDesignation(eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation)}.
	 */
	@Test
	public void testRemoveSpecimenTypeDesignation() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#removeTypeDesignation(eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation)}.
	 */
	@Test
	public void testRemoveTypeDesignation() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getHomotypicalGroup()}.
	 */
	@Test
	public void testGetHomotypicalGroup() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#setHomotypicalGroup(eu.etaxonomy.cdm.model.name.HomotypicalGroup)}.
	 */
	@Test
	public void testSetHomotypicalGroup() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getCitation()}.
	 */
	@Test
	public void testGetCitation() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getCitationString()}.
	 */
	@Test
	public void testGetCitationString() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getReferenceYear()}.
	 */
	@Test
	public void testGetReferenceYear() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#addTaxonBase(eu.etaxonomy.cdm.model.taxon.TaxonBase)}.
	 */
	@Test
	public void testAddTaxonBase() {
		Taxon taxon = Taxon.NewInstance(null, null);  
		nameBase2.addTaxonBase(taxon);  //test if reflection method addTaxonBase() works
		assertTrue("The taxon has not properly been added to the taxonName", nameBase2.getTaxonBases().contains(taxon));
		assertEquals("The taxon name has not properly been added to the taxon", nameBase2, taxon.getName());
		nameBase2.removeTaxonBase(taxon); //test if reflection method in removeTaxonBase() works
		assertFalse("The taxon has not properly been removed from the taxon name", nameBase2.getTaxonBases().contains(taxon));
		assertEquals("The taxon name has not properly been removed from the taxon", null, taxon.getName());
	}
	
	
	@Test
	public void testAddAndRemoveDescriptionTaxonNameDescription() {
		TaxonNameDescription description = TaxonNameDescription.NewInstance();
		nameBase2.addDescription(description);  //test if reflection method in addDescription() works
		assertTrue("The description has not properly been added to the taxonName", nameBase2.getDescriptions().contains(description));
		assertEquals("The taxon name has not properly been added to the description", nameBase2, description.getTaxonName());
		nameBase2.removeDescription(description); //test if reflection method in removeDescription() works
		assertFalse("The description has not properly been removed from the taxon name", nameBase2.getDescriptions().contains(description));
		assertEquals("The taxon name has not properly been removed from the description", null, description.getTaxonName());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getTaxa()}.
	 */
	@Test
	public void testGetTaxa() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getSynonyms()}.
	 */
	@Test
	public void testGetSynonyms() {
		logger.warn("not yet implemented");
	}
	
	@Test
	public void testMergeHomotypicGroups() {
		TaxonNameBase name1 = BotanicalName.NewInstance(null);
		TaxonNameBase name2 = BotanicalName.NewInstance(null);
		TaxonNameBase name3 = BotanicalName.NewInstance(null);
		TaxonNameBase name4 = BotanicalName.NewInstance(null);
		
		Assert.assertFalse(name1.getHomotypicalGroup().equals(name2.getHomotypicalGroup()));
		int numberOfTypifiedNames = name1.getHomotypicalGroup().getTypifiedNames().size();
		Assert.assertEquals(1, numberOfTypifiedNames);
		
		name1.mergeHomotypicGroups(name2);
		Assert.assertEquals(name1.getHomotypicalGroup(), name2.getHomotypicalGroup());
		Assert.assertSame(name1.getHomotypicalGroup(), name2.getHomotypicalGroup());
		numberOfTypifiedNames = name1.getHomotypicalGroup().getTypifiedNames().size();
		Assert.assertEquals(2, numberOfTypifiedNames);
		numberOfTypifiedNames = name2.getHomotypicalGroup().getTypifiedNames().size();
		Assert.assertEquals(2, numberOfTypifiedNames);
		Assert.assertTrue(name1.getHomotypicalGroup().getTypifiedNames().contains(name2));
		Assert.assertTrue(name2.getHomotypicalGroup().getTypifiedNames().contains(name1));

		name3.mergeHomotypicGroups(name2);
		Assert.assertEquals(name1.getHomotypicalGroup(), name3.getHomotypicalGroup());
		Assert.assertSame(name1.getHomotypicalGroup(), name3.getHomotypicalGroup());
		numberOfTypifiedNames = name1.getHomotypicalGroup().getTypifiedNames().size();
		Assert.assertEquals(3, numberOfTypifiedNames);
		numberOfTypifiedNames = name2.getHomotypicalGroup().getTypifiedNames().size();
		Assert.assertEquals(3, numberOfTypifiedNames);
		numberOfTypifiedNames = name3.getHomotypicalGroup().getTypifiedNames().size();
		Assert.assertEquals(3, numberOfTypifiedNames);
		Assert.assertTrue(name1.getHomotypicalGroup().getTypifiedNames().contains(name2));
		Assert.assertTrue(name2.getHomotypicalGroup().getTypifiedNames().contains(name1));
		Assert.assertTrue(name1.getHomotypicalGroup().getTypifiedNames().contains(name3));
		Assert.assertTrue(name3.getHomotypicalGroup().getTypifiedNames().contains(name1));
		Assert.assertTrue(name2.getHomotypicalGroup().getTypifiedNames().contains(name3));
		Assert.assertTrue(name3.getHomotypicalGroup().getTypifiedNames().contains(name2));

		
	}
	
	
	@Test
	public void testIsBasionymFor() {
		TaxonNameBase name1 = BotanicalName.NewInstance(null);
		TaxonNameBase name2 = BotanicalName.NewInstance(null);
		TaxonNameBase name3 = BotanicalName.NewInstance(null);
		TaxonNameBase name4 = BotanicalName.NewInstance(null);
		
		Assert.assertFalse(name2.isBasionymFor(name1));
		Assert.assertFalse(name1.isBasionymFor(name2));
		name1.addBasionym(name2);
		Assert.assertTrue(name2.isBasionymFor(name1));
		Assert.assertFalse(name1.isBasionymFor(name2));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#isHomotypic(eu.etaxonomy.cdm.model.name.TaxonNameBase)}.
	 */
	@Test
	public void testIsHomotypic() {
		TaxonNameBase name1 = BotanicalName.NewInstance(null);
		TaxonNameBase name2 = BotanicalName.NewInstance(null);
		TaxonNameBase name3 = BotanicalName.NewInstance(null);
		TaxonNameBase name4 = BotanicalName.NewInstance(null);
		name1.mergeHomotypicGroups(name2);
		name2.mergeHomotypicGroups(name4);
		
		Assert.assertTrue(name1.isHomotypic(name4));
		Assert.assertTrue(name4.isHomotypic(name1));
		Assert.assertFalse(name1.isHomotypic(name3));
		Assert.assertFalse(name3.isHomotypic(name1));
		Assert.assertTrue(name2.isHomotypic(name1));
		
	}

	@Test
	public void testMakeGroupsBasionym(){
		TaxonNameBase name1 = BotanicalName.NewInstance(null);
		TaxonNameBase name2 = BotanicalName.NewInstance(null);
		TaxonNameBase name3 = BotanicalName.NewInstance(null);
		TaxonNameBase name4 = BotanicalName.NewInstance(null);
		
		name1.mergeHomotypicGroups(name2);
		name1.mergeHomotypicGroups(name3);
		name2.mergeHomotypicGroups(name4);
		
		name1.makeGroupsBasionym();
		
		Assert.assertEquals(1, name2.getBasionyms().size());
		Assert.assertEquals(1, name3.getBasionyms().size());
		Assert.assertEquals(1, name4.getBasionyms().size());
		Assert.assertEquals(name1, name4.getBasionym());
		
	}
	
	@Test
	public void testIsGroupsBasionym(){
		TaxonNameBase name1 = BotanicalName.NewInstance(null);
		TaxonNameBase name2 = BotanicalName.NewInstance(null);
		TaxonNameBase name3 = BotanicalName.NewInstance(null);
		TaxonNameBase name4 = BotanicalName.NewInstance(null);
		
		Assert.assertFalse(name1.isGroupsBasionym());
		
		name1.mergeHomotypicGroups(name2);
		name2.mergeHomotypicGroups(name4);
		
		name1.makeGroupsBasionym();
		
		Assert.assertTrue(name1.isGroupsBasionym());
		Assert.assertFalse(name2.isGroupsBasionym());
		name1.mergeHomotypicGroups(name3);
		Assert.assertFalse(name1.isGroupsBasionym());	
	}
	
	
	
	@Test
	public void testRemoveBasionyms(){
		TaxonNameBase name1 = BotanicalName.NewInstance(null);
		TaxonNameBase basionym = BotanicalName.NewInstance(null);
		TaxonNameBase name3 = BotanicalName.NewInstance(null);
		
		name1.addBasionym(basionym);
		Assert.assertEquals(1, name1.getBasionyms().size());
		name1.addBasionym(name3);
		Assert.assertEquals(2, name1.getBasionyms().size());
		name1.removeBasionyms();
		Assert.assertEquals(0, name1.getBasionyms().size());	
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#isSupraGeneric()}.
	 */
	@Test
	public void testIsSupraGeneric() {
		nameBase1.setRank(Rank.FAMILY());
		assertTrue(nameBase1.isSupraGeneric());
		nameBase1.setRank(Rank.GENUS());
		assertFalse(nameBase1.isSupraGeneric());
		nameBase1.setRank(Rank.FORM());
		assertFalse(nameBase1.isSupraGeneric());
		nameBase1.setRank(null);
		assertFalse(nameBase1.isSupraGeneric());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#isGenus()}.
	 */
	@Test
	public void testIsGenus() {
		nameBase1.setRank(Rank.FAMILY());
		assertFalse(nameBase1.isGenus());
		nameBase1.setRank(Rank.GENUS());
		assertTrue(nameBase1.isGenus());
		nameBase1.setRank(Rank.FORM());
		assertFalse(nameBase1.isGenus());
		nameBase1.setRank(null);
		assertFalse(nameBase1.isGenus());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#isInfraGeneric()}.
	 */
	@Test
	public void testIsInfraGeneric() {
		nameBase1.setRank(Rank.FAMILY());
		assertFalse(nameBase1.isInfraGeneric());
		nameBase1.setRank(Rank.GENUS());
		assertFalse(nameBase1.isInfraGeneric());
		nameBase1.setRank(Rank.SUBGENUS());
		assertTrue(nameBase1.isInfraGeneric());
		nameBase1.setRank(Rank.SPECIES());
		assertFalse(nameBase1.isInfraGeneric());
		nameBase1.setRank(Rank.FORM());
		assertFalse(nameBase1.isInfraGeneric());
		nameBase1.setRank(Rank.INFRAGENERICTAXON());
		assertTrue(nameBase1.isInfraGeneric());
		nameBase1.setRank(null);
		assertFalse(nameBase1.isInfraGeneric());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#isSpecies()}.
	 */
	@Test
	public void testIsSpecies() {
		nameBase1.setRank(Rank.FAMILY());
		assertFalse(nameBase1.isSpecies());
		nameBase1.setRank(Rank.GENUS());
		assertFalse(nameBase1.isSpecies());
		nameBase1.setRank(Rank.SUBGENUS());
		assertFalse(nameBase1.isSpecies());
		nameBase1.setRank(Rank.SPECIES());
		assertTrue(nameBase1.isSpecies());
		nameBase1.setRank(Rank.FORM());
		assertFalse(nameBase1.isSpecies());
		nameBase1.setRank(null);
		assertFalse(nameBase1.isSpecies());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#isInfraSpecific()}.
	 */
	@Test
	public void testIsInfraSpecific() {
		nameBase1.setRank(Rank.FAMILY());
		assertFalse(nameBase1.isInfraSpecific());
		nameBase1.setRank(Rank.GENUS());
		assertFalse(nameBase1.isInfraSpecific());
		nameBase1.setRank(Rank.SUBGENUS());
		assertFalse(nameBase1.isInfraSpecific());
		nameBase1.setRank(Rank.SPECIES());
		assertFalse(nameBase1.isInfraSpecific());
		nameBase1.setRank(Rank.FORM());
		assertTrue(nameBase1.isInfraSpecific());
		nameBase1.setRank(Rank.INFRASPECIFICTAXON());
		assertTrue(nameBase1.isInfraSpecific());
		nameBase1.setRank(null);
		assertFalse(nameBase1.isInfraSpecific());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getNomeclaturalCode()}.
	 */
	@Test
	public void testGetNomeclaturalCode() {
		//is abstract
	}
}
