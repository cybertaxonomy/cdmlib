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

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
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

	
	private class TaxonNameBaseTestClass extends TaxonNameBase{
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
		public String generateFullTitle(){return null;};
	}

	
/** *************************  TESTS ******************************************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#generateTitle()}.
	 */
	@Test
	public void testGenerateTitle() {
		logger.warn("not yet implemented");
	}

	@Test
	public void testGenerateFullTitle() {
		logger.warn("not yet implemented");
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
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#setStatus(java.util.Set)}.
	 */
	@Test
	public void testSetStatus() {
		//is protected
		logger.warn("not yet implemented");
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
		logger.warn("not yet implemented");
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
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#removeBasionym()}.
	 */
	@Test
	public void testRemoveBasionym() {
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
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#setRank(eu.etaxonomy.cdm.model.name.Rank)}.
	 */
	@Test
	public void testSetRank() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getNomenclaturalReference()}.
	 */
	@Test
	public void testGetNomenclaturalReference() {
		INomenclaturalReference nr = nameBase1.getNomenclaturalReference();
		assertNull("Nomenclatural Reference shall be null", nr);
		nameBase1.setNomenclaturalReference(Generic.NewInstance());
		nr = nameBase1.getNomenclaturalReference();
		assertNotNull("Nomenclatural Reference shall not be null", nr);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#setNomenclaturalReference(eu.etaxonomy.cdm.model.reference.INomenclaturalReference)}.
	 */
	@Test
	public void testSetNomenclaturalReference() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getAppendedPhrase()}.
	 */
	@Test
	public void testGetAppendedPhrase() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#setAppendedPhrase(java.lang.String)}.
	 */
	@Test
	public void testSetAppendedPhrase() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getNomenclaturalMicroReference()}.
	 */
	@Test
	public void testGetNomenclaturalMicroReference() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#setNomenclaturalMicroReference(java.lang.String)}.
	 */
	@Test
	public void testSetNomenclaturalMicroReference() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getHasProblem()}.
	 */
	@Test
	public void testGetHasProblem() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#setHasProblem(boolean)}.
	 */
	@Test
	public void testSetHasProblem() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#hasProblem()}.
	 */
	@Test
	public void testHasProblem() {
		logger.warn("not yet implemented");
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
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getProblems()}.
	 */
	@Test
	public void testGetProblems() {
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

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#isHomotypic(eu.etaxonomy.cdm.model.name.TaxonNameBase)}.
	 */
	@Test
	public void testIsHomotypic() {
		logger.warn("not yet implemented");
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
