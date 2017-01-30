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
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy;

/**
 * @author a.mueller
 * @created 28.06.2008
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
		public TaxonNameBaseTestClass(){super();}
		public TaxonNameBaseTestClass(Rank rank){super(rank);}
		public TaxonNameBaseTestClass(HomotypicalGroup hg){super(hg);}
		public TaxonNameBaseTestClass(Rank rank, HomotypicalGroup hg){super(rank, hg);}
		@Override
		public void setCacheStrategy(INameCacheStrategy strategy){}
		@Override
		public INameCacheStrategy getCacheStrategy(){return null;}
		@Override
		public NomenclaturalCode getNomenclaturalCode(){return null;}
		@Override
		public String generateFullTitle(){return null;}
		@Override
		public String generateTitle() {
			return null;
		}

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
		TaxonNameBase name1 = TaxonNameBase.NewBotanicalInstance(null);
		TaxonNameBase basionym1 = TaxonNameBase.NewBotanicalInstance(null);
		TaxonNameBase basionym2 = TaxonNameBase.NewBotanicalInstance(null);

		assertEquals(null, name1.getBasionym());
		name1.addBasionym(basionym1);
		assertEquals(basionym1, name1.getBasionym());
		name1.addBasionym(basionym2);
		TaxonNameBase oneOfThebasionyms = name1.getBasionym();
		assertTrue(oneOfThebasionyms == basionym1 || oneOfThebasionyms == basionym2 );
		name1.removeBasionyms();
		assertEquals(null, name1.getBasionym());
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
		TaxonNameBase<?,?> name1 = TaxonNameBase.NewBotanicalInstance(null);
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
		INomenclaturalReference nr = nameBase1.getNomenclaturalReference();
		assertNull("Nomenclatural Reference shall be null", nr);
		nameBase1.setNomenclaturalReference(ReferenceFactory.newGeneric());
		nr = nameBase1.getNomenclaturalReference();
		assertNotNull("Nomenclatural Reference shall not be null", nr);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getAppendedPhrase()}.
	 */
	@Test
	public void testGetAppendedPhrase() {
		TaxonNameBase<?,?> name1 = TaxonNameBase.NewBotanicalInstance(null);
		String appPhrase = "appPhrase";
		assertNull(name1.getAppendedPhrase());
		name1.setAppendedPhrase(appPhrase);
		assertSame(appPhrase, name1.getAppendedPhrase());
		name1.setAppendedPhrase(null);
		assertNull(name1.getAppendedPhrase());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getNomenclaturalMicroReference()}.
	 */
	@Test
	public void testGetSetNomenclaturalMicroReference() {
		TaxonNameBase<?,?> name1 = TaxonNameBase.NewBotanicalInstance(null);
		String microRef = "micro";
		assertNull(name1.getNomenclaturalMicroReference());
		name1.setNomenclaturalMicroReference(microRef);
		assertSame(microRef, name1.getNomenclaturalMicroReference());
		name1.setNomenclaturalMicroReference(null);
		assertNull(name1.getNomenclaturalMicroReference());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getHasProblem()}.
	 */
	@Test
	public void testGetSetHasProblem() {
		TaxonNameBase<?,?> name1 = TaxonNameBase.NewBotanicalInstance(null);
		name1.setParsingProblem(0);
		assertFalse(name1.hasProblem());
		name1.setParsingProblem(1);
		assertTrue(name1.hasProblem());
		name1.setParsingProblem(0);
		assertFalse(name1.getParsingProblem()!=0);
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
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#addNameTypeDesignation(eu.etaxonomy.cdm.model.name.TaxonNameBase, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String, java.lang.String, boolean, boolean)}.
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
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#addSpecimenTypeDesignation(eu.etaxonomy.cdm.model.occurrence.Specimen, eu.etaxonomy.cdm.model.name.TypeDesignationStatus, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String, java.lang.String, boolean)}.
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
		TaxonNameBase<?,?> name1 = TaxonNameBase.NewBotanicalInstance(null);
		TaxonNameBase<?,?> name2 = TaxonNameBase.NewBotanicalInstance(null);
		TaxonNameBase<?,?> name3 = TaxonNameBase.NewBotanicalInstance(null);
		TaxonNameBase<?,?> name4 = TaxonNameBase.NewBotanicalInstance(null);

		assertFalse(name1.getHomotypicalGroup().equals(name2.getHomotypicalGroup()));
		int numberOfTypifiedNames = name1.getHomotypicalGroup().getTypifiedNames().size();
		assertEquals(1, numberOfTypifiedNames);

		name1.mergeHomotypicGroups(name2);
		assertEquals(name1.getHomotypicalGroup(), name2.getHomotypicalGroup());
		assertSame(name1.getHomotypicalGroup(), name2.getHomotypicalGroup());
		numberOfTypifiedNames = name1.getHomotypicalGroup().getTypifiedNames().size();
		assertEquals(2, numberOfTypifiedNames);
		numberOfTypifiedNames = name2.getHomotypicalGroup().getTypifiedNames().size();
		assertEquals(2, numberOfTypifiedNames);
		assertTrue(name1.getHomotypicalGroup().getTypifiedNames().contains(name2));
		assertTrue(name2.getHomotypicalGroup().getTypifiedNames().contains(name1));

		name3.mergeHomotypicGroups(name2);
		assertEquals(name1.getHomotypicalGroup(), name3.getHomotypicalGroup());
		assertSame(name1.getHomotypicalGroup(), name3.getHomotypicalGroup());
		numberOfTypifiedNames = name1.getHomotypicalGroup().getTypifiedNames().size();
		assertEquals(3, numberOfTypifiedNames);
		numberOfTypifiedNames = name2.getHomotypicalGroup().getTypifiedNames().size();
		assertEquals(3, numberOfTypifiedNames);
		numberOfTypifiedNames = name3.getHomotypicalGroup().getTypifiedNames().size();
		assertEquals(3, numberOfTypifiedNames);
		assertTrue(name1.getHomotypicalGroup().getTypifiedNames().contains(name2));
		assertTrue(name2.getHomotypicalGroup().getTypifiedNames().contains(name1));
		assertTrue(name1.getHomotypicalGroup().getTypifiedNames().contains(name3));
		assertTrue(name3.getHomotypicalGroup().getTypifiedNames().contains(name1));
		assertTrue(name2.getHomotypicalGroup().getTypifiedNames().contains(name3));
		assertTrue(name3.getHomotypicalGroup().getTypifiedNames().contains(name2));


	}


	@Test
	public void testIsBasionymFor() {
		TaxonNameBase name1 = TaxonNameBase.NewBotanicalInstance(null);
		TaxonNameBase name2 = TaxonNameBase.NewBotanicalInstance(null);
		TaxonNameBase<?,?> name3 = TaxonNameBase.NewBotanicalInstance(null);
		TaxonNameBase<?,?> name4 = TaxonNameBase.NewBotanicalInstance(null);

		assertFalse(name2.isBasionymFor(name1));
		assertFalse(name1.isBasionymFor(name2));
		name1.addBasionym(name2);
		assertTrue(name2.isBasionymFor(name1));
		assertFalse(name1.isBasionymFor(name2));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#isHomotypic(eu.etaxonomy.cdm.model.name.TaxonNameBase)}.
	 */
	@Test
	public void testIsHomotypic() {
		TaxonNameBase<?,?> name1 = TaxonNameBase.NewBotanicalInstance(null);
		TaxonNameBase<?,?> name2 = TaxonNameBase.NewBotanicalInstance(null);
		TaxonNameBase<?,?> name3 = TaxonNameBase.NewBotanicalInstance(null);
		TaxonNameBase<?,?> name4 = TaxonNameBase.NewBotanicalInstance(null);
		name1.mergeHomotypicGroups(name2);
		name2.mergeHomotypicGroups(name4);

		assertTrue(name1.isHomotypic(name4));
		assertTrue(name4.isHomotypic(name1));
		assertFalse(name1.isHomotypic(name3));
		assertFalse(name3.isHomotypic(name1));
		assertTrue(name2.isHomotypic(name1));

	}

	@Test
	public void testMakeGroupsBasionym(){
		TaxonNameBase<?,?> name1 = TaxonNameBase.NewBotanicalInstance(null);
		TaxonNameBase<?,?> name2 = TaxonNameBase.NewBotanicalInstance(null);
		TaxonNameBase<?,?> name3 = TaxonNameBase.NewBotanicalInstance(null);
		TaxonNameBase<?,?> name4 = TaxonNameBase.NewBotanicalInstance(null);

		name1.mergeHomotypicGroups(name2);
		name1.mergeHomotypicGroups(name3);
		name2.mergeHomotypicGroups(name4);

		name1.makeGroupsBasionym();

		assertEquals(1, name2.getBasionyms().size());
		assertEquals(1, name3.getBasionyms().size());
		assertEquals(1, name4.getBasionyms().size());
		assertEquals(name1, name4.getBasionym());

	}

	@Test
	public void testIsGroupsBasionym(){
		TaxonNameBase<?,?> name1 = TaxonNameBase.NewBotanicalInstance(null);
		TaxonNameBase<?,?> name2 = TaxonNameBase.NewBotanicalInstance(null);
		TaxonNameBase<?,?> name3 = TaxonNameBase.NewBotanicalInstance(null);
		TaxonNameBase<?,?> name4 = TaxonNameBase.NewBotanicalInstance(null);

		assertFalse(name1.isGroupsBasionym());

		name1.mergeHomotypicGroups(name2);
		name2.mergeHomotypicGroups(name4);

		name1.makeGroupsBasionym();

		assertTrue(name1.isGroupsBasionym());
		assertFalse(name2.isGroupsBasionym());
		name1.mergeHomotypicGroups(name3);
		assertFalse(name1.isGroupsBasionym());
	}



	@Test
	public void testRemoveBasionyms(){
		TaxonNameBase name1 = TaxonNameBase.NewBotanicalInstance(null);
		TaxonNameBase basionym = TaxonNameBase.NewBotanicalInstance(null);
		TaxonNameBase name3 = TaxonNameBase.NewBotanicalInstance(null);

		name1.addBasionym(basionym);
		assertEquals(1, name1.getBasionyms().size());
		name1.addBasionym(name3);
		assertEquals(2, name1.getBasionyms().size());
		name1.removeBasionyms();
		assertEquals(0, name1.getBasionyms().size());
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

	@Test
	public void testRemoveTaxonBases(){

		Taxon newTaxon = Taxon.NewInstance(nameBase1, null);
		assertEquals(1, nameBase1.getTaxonBases().size());


		nameBase1.removeTaxonBase(newTaxon);
		assertEquals(0, nameBase1.getTaxonBases().size());

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
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonNameBase#getNomenclaturalCode()}.
	 */
	@Test
	public void testGetNomenclaturalCode() {
		//is abstract
	}


	//descriptions, fullTitleCache, homotypicalGroup,

	//no changes to: appendedPharse, nomenclaturalReference,
	//nomenclaturalMicroReference, parsingProblem, problemEnds, problemStarts
	//protectedFullTitleCache

	@Test
	public void testClone(){
		NonViralName taxonNameBase1 = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
		NonViralName<?> genusName = TaxonNameFactory.NewNonViralInstance(Rank.GENUS());
		Taxon taxonBase = Taxon.NewInstance(taxonNameBase1, null);

		//basionym & homonym
		NonViralName<?> basionym = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
		NonViralName<?> earlierHomonym = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
		taxonNameBase1.addBasionym(basionym);
		taxonNameBase1.addRelationshipToName(earlierHomonym, NameRelationshipType.LATER_HOMONYM(), "later homonym rule");
		//status
		Reference statusReference = ReferenceFactory.newArticle();
		NomenclaturalStatus nomStatus = NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONSERVED(), statusReference, "55");
		taxonNameBase1.addStatus(nomStatus);
		//typeDesignation
		DerivedUnit typeSpecimen = DerivedUnit.NewPreservedSpecimenInstance();
		Reference specimenTypeCitation = ReferenceFactory.newArticle();
		Reference nameTypeCitation = ReferenceFactory.newArticle();
		SpecimenTypeDesignation specimenTypeDesignationOriginal = taxonNameBase1.addSpecimenTypeDesignation(typeSpecimen, SpecimenTypeDesignationStatus.HOLOTYPE(), specimenTypeCitation, null, null, false, false);
		NameTypeDesignation nameTypeDesignationOriginal = genusName.addNameTypeDesignation(taxonNameBase1, nameTypeCitation, null, null, NameTypeDesignationStatus.LECTOTYPE(), true, false, false, false);

		//description
		TaxonNameDescription description = TaxonNameDescription.NewInstance(taxonNameBase1);
		TextData textData = TextData.NewInstance(Feature.IMAGE());
		textData.putText(Language.DEFAULT(), "My media text data");
		Media media = Media.NewInstance();
		textData.addMedia(media);
		description.addElement(textData);

		//CLONE
		TaxonNameBase<?,?> clone = (TaxonNameBase)taxonNameBase1.clone();
		TaxonNameBase<?,?> genusClone = (TaxonNameBase)genusName.clone();
		assertSame("Rank should be same", taxonNameBase1.getRank(), clone.getRank());
		assertTrue("TaxonBases should not be cloned", clone.getTaxonBases().isEmpty());
		assertEquals("TaxonBases of original name should not be empty", 1, taxonNameBase1.getTaxonBases().size());
		//Homotypical group - CAUTION: behaviour may be changed in future
		//TODO still needs to be discussed
//		assertSame("The clone must have the same homotypical group as the original", taxonNameBase1.getHomotypicalGroup(), clone.getHomotypicalGroup());
//		assertSame("The genusClone must have the same homotypical group as the original genus", genusName.getHomotypicalGroup(), genusClone.getHomotypicalGroup());

		//description
		assertEquals("There should be exactly 1 name description", 1, clone.getDescriptions().size());
		TaxonNameDescription descriptionClone = clone.getDescriptions().iterator().next();
		assertEquals("There should be exactly 1 description element", 1, descriptionClone.getElements().size());
		TextData textDataClone = (TextData)descriptionClone.getElements().iterator().next();
		String text = textDataClone.getText(Language.DEFAULT());
		assertEquals("Textdata should be equal", "My media text data", text);
		assertEquals("There should be exactly 1 media attached", 1, textDataClone.getMedia().size());
		Media mediaClone = textDataClone.getMedia().get(0);
		assertSame("Media must be the same", media, mediaClone);

		//type designation
		assertEquals("There should be exactly 1 specimen type designation", 1, clone.getTypeDesignations().size());
		assertNotSame("type designation sets should not be the same", taxonNameBase1.getTypeDesignations(), clone.getTypeDesignations());
		SpecimenTypeDesignation specimenTypeDesignationClone = (SpecimenTypeDesignation)clone.getTypeDesignations().iterator().next();
		assertNotSame("The specimen type designation should not be the same", specimenTypeDesignationOriginal, specimenTypeDesignationClone);
		assertSame("The derived unit of the specimen type designation needs to be the same", specimenTypeDesignationOriginal.getTypeSpecimen(), specimenTypeDesignationClone.getTypeSpecimen());
		assertSame("The status of the specimen type designation needs to be the same", specimenTypeDesignationOriginal.getTypeStatus(), specimenTypeDesignationClone.getTypeStatus());
		assertEquals("The specimen type designation must have exactly 1 typified name which is 'clone'", 1, specimenTypeDesignationClone.getTypifiedNames().size());
		assertTrue("The specimen type designation must have 'clone' as typified name", specimenTypeDesignationClone.getTypifiedNames().contains(clone));
//		assertSame("The specimen type designation must have the same homotypical group as the typified name", specimenTypeDesignationClone.getHomotypicalGroup(), clone.getHomotypicalGroup());

		assertEquals("There should be exactly 1 name type designation", 1, genusClone.getTypeDesignations().size());
		assertNotSame("type designation sets should not be the same", genusName.getTypeDesignations(), genusClone.getTypeDesignations());
		NameTypeDesignation nameTypeDesignationClone = (NameTypeDesignation)genusClone.getTypeDesignations().iterator().next();
		assertNotSame("The name type designation should not be the same", nameTypeDesignationOriginal, nameTypeDesignationClone);
		assertSame("The nyme type of the name type designation needs to be the same", taxonNameBase1, nameTypeDesignationClone.getTypeName());
		assertSame("The status of the name type designation needs to be the same", nameTypeDesignationOriginal.getTypeStatus(), nameTypeDesignationClone.getTypeStatus());
		assertEquals("The name type designation must have exactly 1 typified name which is 'genusClone'", 1, nameTypeDesignationClone.getTypifiedNames().size());
		assertTrue("The name type designation must have 'genusClone' as typified name", nameTypeDesignationClone.getTypifiedNames().contains(genusClone));
//		assertSame("The name type designation must have the same homotypical group as the typified name", nameTypeDesignationClone.getHomotypicalGroup(), genusClone.getHomotypicalGroup());

		//status
		assertEquals("There should be exactly 1 status", 1, clone.getStatus().size());
		assertNotSame("Status sets should not be the same", taxonNameBase1.getStatus(), clone.getStatus());
		NomenclaturalStatus cloneStatus = clone.getStatus().iterator().next();
		assertSame("The type of the nomStatus needs to be the same", nomStatus.getType(), cloneStatus.getType());
		assertSame("The citation of the nomStatus needs to be the same", nomStatus.getCitation(), cloneStatus.getCitation());
		assertSame("The rule considered of the nomStatus needs to be the same", nomStatus.getRuleConsidered(), cloneStatus.getRuleConsidered());
		//DISCUSS: do we want to reuse the status
//		assertSame("The nomStatus needs to be the same", nomStatus, cloneStatus);


//		//hybrid parents of clone
//		assertEquals("There should be exactly 2 hybrid relationships in which the clone takes the child role", 2, clone.getChildRelationships().size());
//		Set<NonViralName> parentSet = new HashSet<NonViralName>();
//		Set<NonViralName> childSet = new HashSet<NonViralName>();
//		for (Object object : clone.getChildRelationships()){
//			HybridRelationship childRelation = (HybridRelationship)object;
//			NonViralName relatedFrom = childRelation.getRelatedFrom();
//			parentSet.add(relatedFrom);
//			NonViralName relatedTo = childRelation.getRelatedTo();
//			childSet.add(relatedTo);
//		}
//		assertTrue("Parent set should contain parent1", parentSet.contains(parent));
//		assertTrue("Parent set should contain parent2", parentSet.contains(parent2));
//		assertTrue("Child set should contain clone", childSet.contains(clone));

		//basionym of clone
		assertEquals("There should be exactly 1 relationship in which the clone takes the to role", 1, clone.getRelationsToThisName().size());
		NameRelationship nameRelation = clone.getRelationsToThisName().iterator().next();
		assertSame("Basionym should be from-object in relationship", basionym, nameRelation.getRelatedFrom());
		assertSame("Clone should be to-object in relationship", clone, nameRelation.getRelatedTo());
		assertSame("Relationship type should be cloned correctly", NameRelationshipType.BASIONYM(), nameRelation.getType());
//		assertEquals("Rule should be cloned correctly", "later homonym rule", nameRelation.getRuleConsidered());


		//homonym of clone
		assertEquals("There should be exactly 1 relationship in which the clone takes the from role", 1, clone.getRelationsFromThisName().size());
		nameRelation = clone.getRelationsFromThisName().iterator().next();
		assertSame("Clone should be from-object in relationship", clone, nameRelation.getRelatedFrom());
		assertSame("Homonym should be to-object in relationship", earlierHomonym, nameRelation.getRelatedTo());
		assertSame("Relationship type should be cloned correctly", NameRelationshipType.LATER_HOMONYM(), nameRelation.getType());
		assertEquals("Rule should be cloned correctly", "later homonym rule", nameRelation.getRuleConsidered());
	}

}
