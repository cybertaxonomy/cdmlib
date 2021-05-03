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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 * @since 28.06.2008
 */
public class TaxonNameTest extends EntityTestBase {

	private static final Logger logger = Logger.getLogger(TaxonNameTest.class);

	private TaxonName nameBase1;
	private TaxonName nameBase2;
	private Reference ref1;

	@Before
	public void setUp() throws Exception {
		nameBase1 = TaxonNameFactory.NewNameInstance(NomenclaturalCode.NonViral, null);
		nameBase2 = TaxonNameFactory.NewNameInstance(NomenclaturalCode.NonViral, Rank.GENUS());
	    ref1 = ReferenceFactory.newBook();
	    ref1.setTitle("My book");
	    ref1.setAbbrevTitle("M. bk.");
	    ref1.setDatePublished(TimePeriodParser.parseStringVerbatim("1988"));
	}

/***************************  TESTS ******************************************************/

	@Test
	public void testSetReferenceWithListener(){
	    //prepare
	    nameBase1.setGenusOrUninomial("Abies");
	    nameBase1.setAuthorshipCache("Auth.");

	    //before
	    Assert.assertEquals("Abies Auth.", nameBase1.getFullTitleCache());

	    //after
	    //add ref
	    nameBase1.setNomenclaturalReference(ref1);
	    Assert.assertEquals("Using transient setNomenclaturalReference should reset the fullTitleCache",
	            "Abies Auth., M. bk. 1988", nameBase1.getFullTitleCache());

	    //add detail
        nameBase1.setNomenclaturalMicroReference("55");
        Assert.assertEquals("Using transient setNomenclaturalMicroReference should reset the fullTitleCache",
                "Abies Auth., M. bk.: 55. 1988", nameBase1.getFullTitleCache());

        //remove detail
        nameBase1.setNomenclaturalMicroReference(null);
        Assert.assertEquals("Using the source microcitation setter should reset the fullTitleCache",
                "Abies Auth., M. bk. 1988", nameBase1.getFullTitleCache());

        //remove ref
        nameBase1.setNomenclaturalReference(null);
        Assert.assertEquals("Using the source citation setter should reset the fullTitleCache",
                "Abies Auth.", nameBase1.getFullTitleCache());

        Assert.assertNull("Full emptying the source should remove it from the name (by method checkNullSource)",
                nameBase1.getNomenclaturalSource());

        //do the same again with setters on source
        //after
        //add ref
        nameBase1.getNomenclaturalSource(true).setCitation(ref1);
        Assert.assertEquals("Using transient setNomenclaturalReference should reset the fullTitleCache",
                "Abies Auth., M. bk. 1988", nameBase1.getFullTitleCache());

        //add detail
        nameBase1.getNomenclaturalSource().setCitationMicroReference("55");
        Assert.assertEquals("Using transient setNomenclaturalMicroReference should reset the fullTitleCache",
                "Abies Auth., M. bk.: 55. 1988", nameBase1.getFullTitleCache());

        //remove detail
        nameBase1.getNomenclaturalSource().setCitationMicroReference(null);
        Assert.assertEquals("Using the source microcitation setter should reset the fullTitleCache",
                "Abies Auth., M. bk. 1988", nameBase1.getFullTitleCache());

        //remove ref
        nameBase1.getNomenclaturalSource().setCitation(null);;
        Assert.assertEquals("Using the source citation setter should reset the fullTitleCache",
                "Abies Auth.", nameBase1.getFullTitleCache());

        Assert.assertNull("Full emptying the source should remove it from the name (by method checkNullSource)",
                nameBase1.getNomenclaturalSource());

	}

	@Test
	public void testTaxonName() {
		assertNotNull(nameBase1);
	}

	@Test
	public void testTaxonNameRank() {
		assertNotNull(nameBase2);
		assertEquals(Rank.GENUS(), nameBase2.getRank());
		Rank rank = null;
		TaxonName testName = TaxonNameFactory.NewNonViralInstance(rank, null);
		assertNull(testName.getRank());
	}

	@Test
	public void testTaxonNameHomotypicalGroup() {
		HomotypicalGroup hg = HomotypicalGroup.NewInstance();
		TaxonName testHG = TaxonNameFactory.NewNonViralInstance(null, hg);
		assertSame(hg, testHG.getHomotypicalGroup());
		HomotypicalGroup hgNull = null;
		TaxonName testHGNull = TaxonNameFactory.NewNonViralInstance(null, hgNull);
		assertNotNull(testHGNull.getHomotypicalGroup());
	}

	@Test
	public void testTaxonNameRankHomotypicalGroup() {
		Rank rank = Rank.SPECIES();
		HomotypicalGroup hg = HomotypicalGroup.NewInstance();
		TaxonName testHG = TaxonNameFactory.NewNonViralInstance(rank, hg);
		assertSame(rank, testHG.getRank());
		assertSame(hg, testHG.getHomotypicalGroup());

		Rank rankNull = null;
		HomotypicalGroup hgNull = null;
		TaxonName testHGNull = TaxonNameFactory.NewNonViralInstance(rankNull, hgNull);
		assertEquals(rankNull, testHGNull.getRank());
		assertNotNull(testHGNull.getHomotypicalGroup());
	}

	@Test
	public void testAddRelationshipToName() {
		logger.warn("not yet implemented");
	}

	@Test
	public void testAddRelationshipFromName() {
		logger.warn("not yet implemented");
	}

	@Test
	public void testAddNameRelationship() {
		logger.warn("not yet implemented");
	}

	@Test
	public void testRemoveNameRelationship() {
		logger.warn("not yet implemented");
	}

	@Test
	public void testAddRelationship() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonName#getStatus()}.
	 * and for {@link eu.etaxonomy.cdm.model.name.TaxonName#addStatus(eu.etaxonomy.cdm.model.name.NomenclaturalStatus)}.
	 * and for {@link eu.etaxonomy.cdm.model.name.TaxonName#removeStatus(eu.etaxonomy.cdm.model.name.NomenclaturalStatus)}.
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

	@Test
	public void testIsOriginalCombination() {
		logger.warn("not yet implemented");
	}

	@Test
	public void testGetBasionym() {
		TaxonName name1 = TaxonNameFactory.NewBotanicalInstance(null);
		TaxonName basionym1 = TaxonNameFactory.NewBotanicalInstance(null);
		TaxonName basionym2 = TaxonNameFactory.NewBotanicalInstance(null);

		assertEquals(null, name1.getBasionym());
		name1.addBasionym(basionym1);
		assertEquals(basionym1, name1.getBasionym());
		name1.addBasionym(basionym2);
		TaxonName oneOfThebasionyms = name1.getBasionym();
		assertTrue(oneOfThebasionyms == basionym1 || oneOfThebasionyms == basionym2 );
		name1.removeBasionyms();
		assertEquals(null, name1.getBasionym());
	}

	@Test
	public void testAddBasionymT() {
		assertNotSame(nameBase1.getHomotypicalGroup(), nameBase2.getHomotypicalGroup());
		assertFalse(nameBase1.getHomotypicalGroup().equals(nameBase2.getHomotypicalGroup()));
		nameBase1.addBasionym(nameBase2);
		assertTrue(nameBase1.getHomotypicalGroup().equals(nameBase2.getHomotypicalGroup()));
		assertSame(nameBase1.getHomotypicalGroup(), nameBase2.getHomotypicalGroup());
		logger.warn("not yet fully implemented");
	}

	@Test
	public void testAddBasionymTaxonNameString() {
		logger.warn("not yet implemented");
	}

	@Test
	public void testGetRank() {
		TaxonName name1 = TaxonNameFactory.NewBotanicalInstance(null);
		assertNull("Rank shall be null", name1.getRank());
		name1.setRank(Rank.SPECIES());
		assertNotNull("Rank shall not be null", name1.getRank());
		name1.setRank(null);
		assertNull("Rank shall be null", name1.getRank());

	}

	@Test
	public void testGetSetNomenclaturalReference() {
		INomenclaturalReference nr = nameBase1.getNomenclaturalReference();
		assertNull("Nomenclatural Reference shall be null", nr);
		nameBase1.setNomenclaturalReference(ReferenceFactory.newGeneric());
		nr = nameBase1.getNomenclaturalReference();
		assertNotNull("Nomenclatural Reference shall not be null", nr);
	}

	@Test
	public void testGetAppendedPhrase() {
		TaxonName name1 = TaxonNameFactory.NewBotanicalInstance(null);
		String appPhrase = "appPhrase";
		assertNull(name1.getAppendedPhrase());
		name1.setAppendedPhrase(appPhrase);
		assertSame(appPhrase, name1.getAppendedPhrase());
		name1.setAppendedPhrase(null);
		assertNull(name1.getAppendedPhrase());
	}

	@Test
	public void testGetSetNomenclaturalMicroReference() {
		TaxonName name1 = TaxonNameFactory.NewBotanicalInstance(null);
		String microRef = "micro";
		assertNull(name1.getNomenclaturalMicroReference());
		name1.setNomenclaturalMicroReference(microRef);
		assertSame(microRef, name1.getNomenclaturalMicroReference());
		name1.setNomenclaturalMicroReference(null);
		assertNull(name1.getNomenclaturalMicroReference());
	}

	@Test
	public void testGetSetHasProblem() {
		TaxonName name1 = TaxonNameFactory.NewBotanicalInstance(null);
		name1.setParsingProblem(0);
		assertFalse(name1.hasProblem());
		name1.setParsingProblem(1);
		assertTrue(name1.hasProblem());
		name1.setParsingProblem(0);
		assertFalse(name1.getParsingProblem()!=0);
	}

	@Test
	public void testAddNameTypeDesignation() {
		logger.warn("not yet implemented");
	}

	@Test
	public void testRemoveNameTypeDesignation() {
		logger.warn("not yet implemented");
	}

	@Test
	public void testGetSpecimenTypeDesignationsOfHomotypicalGroup() {
		logger.warn("not yet implemented");
	}

	@Test
	public void testAddSpecimenTypeDesignationSpecimenTypeDesignationStatusReferenceBaseStringStringBoolean() {
		logger.warn("not yet implemented");
	}

	@Test
	public void testAddSpecimenTypeDesignationSpecimenTypeDesignation() {
		logger.warn("not yet implemented");
	}

	@Test
	public void testRemoveSpecimenTypeDesignation() {
		logger.warn("not yet implemented");
	}

	@Test
	public void testRemoveTypeDesignation() {
		logger.warn("not yet implemented");
	}

	@Test
	public void testGetCitation() {
		logger.warn("not yet implemented");
	}

	@Test
	public void testGetCitationString() {
		String citation = nameBase1.getCitationString();
	    logger.warn("testGetCitationString not yet implemented");
	}

	@Test
	public void testGetReferenceYear() {
		logger.warn("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.TaxonName#addTaxonBase(eu.etaxonomy.cdm.model.taxon.TaxonBase)}.
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

	@Test
	public void testGetSynonyms() {
		logger.warn("not yet implemented");
	}

	@Test
	public void testMergeHomotypicGroups() {
		TaxonName name1 = TaxonNameFactory.NewBotanicalInstance(null);
		TaxonName name2 = TaxonNameFactory.NewBotanicalInstance(null);
		TaxonName name3 = TaxonNameFactory.NewBotanicalInstance(null);

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
		TaxonName name1 = TaxonNameFactory.NewBotanicalInstance(null);
		TaxonName name2 = TaxonNameFactory.NewBotanicalInstance(null);

		assertFalse(name2.isBasionymFor(name1));
		assertFalse(name1.isBasionymFor(name2));
		name1.addBasionym(name2);
		assertTrue(name2.isBasionymFor(name1));
		assertFalse(name1.isBasionymFor(name2));
	}

	@Test
	public void testIsHomotypic() {
		TaxonName name1 = TaxonNameFactory.NewBotanicalInstance(null);
		TaxonName name2 = TaxonNameFactory.NewBotanicalInstance(null);
		TaxonName name3 = TaxonNameFactory.NewBotanicalInstance(null);
		TaxonName name4 = TaxonNameFactory.NewBotanicalInstance(null);
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
		TaxonName name1 = TaxonNameFactory.NewBotanicalInstance(null);
		TaxonName name2 = TaxonNameFactory.NewBotanicalInstance(null);
		TaxonName name3 = TaxonNameFactory.NewBotanicalInstance(null);
		TaxonName name4 = TaxonNameFactory.NewBotanicalInstance(null);

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
		TaxonName name1 = TaxonNameFactory.NewBotanicalInstance(null);
		TaxonName name2 = TaxonNameFactory.NewBotanicalInstance(null);
		TaxonName name3 = TaxonNameFactory.NewBotanicalInstance(null);
		TaxonName name4 = TaxonNameFactory.NewBotanicalInstance(null);

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
		TaxonName name1 = TaxonNameFactory.NewBotanicalInstance(null);
		TaxonName basionym1 = TaxonNameFactory.NewBotanicalInstance(null);
		TaxonName basionym2 = TaxonNameFactory.NewBotanicalInstance(null);

		name1.addBasionym(basionym1);
		assertEquals(1, name1.getBasionyms().size());
		name1.addBasionym(basionym2);
		assertEquals(2, name1.getBasionyms().size());
		name1.removeBasionyms();
		assertEquals(0, name1.getBasionyms().size());
	}

	@Test
    public void testRemoveRelationWithTaxonName(){
	    TaxonName name1 = TaxonNameFactory.NewBotanicalInstance(null);
        TaxonName basionym1 = TaxonNameFactory.NewBotanicalInstance(null);
        TaxonName basionym2 = TaxonNameFactory.NewBotanicalInstance(null);

        name1.addBasionym(basionym1);
        assertEquals(1, name1.getBasionyms().size());
        name1.addBasionym(basionym2);
        assertEquals(2, name1.getBasionyms().size());
        name1.removeRelationWithTaxonName(basionym1, Direction.relatedTo, NameRelationshipType.BASIONYM());
        assertEquals(1, name1.getBasionyms().size());
    }

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

	//descriptions, fullTitleCache, homotypicalGroup,

	//no changes to: appendedPharse, nomenclaturalReference,
	//nomenclaturalMicroReference, parsingProblem, problemEnds, problemStarts
	//protectedFullTitleCache

	@Test
	public void testClone(){

	    //Create data
	    TaxonName taxonName1 = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
	    TaxonName genusName = TaxonNameFactory.NewNonViralInstance(Rank.GENUS());
        Taxon.NewInstance(taxonName1, null);

		   //basionym & homonym
		TaxonName basionym = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
		TaxonName earlierHomonym = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
		NameRelationship basionymRelation = taxonName1.addBasionym(basionym);
		NameRelationship homonymRelation = taxonName1.addRelationshipToName(earlierHomonym, NameRelationshipType.LATER_HOMONYM(), "later homonym rule", null);
		   //status
		Reference statusReference = ReferenceFactory.newArticle();
		NomenclaturalStatus nomStatus = NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONSERVED(), statusReference, "55");
		taxonName1.addStatus(nomStatus);
		   //typeDesignation
		DerivedUnit typeSpecimen = DerivedUnit.NewPreservedSpecimenInstance();
		Reference specimenTypeCitation = ReferenceFactory.newArticle();
		Reference nameTypeCitation = ReferenceFactory.newArticle();
		SpecimenTypeDesignation specimenTypeDesignationOriginal = taxonName1.addSpecimenTypeDesignation(typeSpecimen, SpecimenTypeDesignationStatus.HOLOTYPE(), specimenTypeCitation, null, null, false, false);
		NameTypeDesignation nameTypeDesignationOriginal = genusName.addNameTypeDesignation(taxonName1, nameTypeCitation, null, null, NameTypeDesignationStatus.LECTOTYPE(), true, false, false, false);

		   //description
		TaxonNameDescription description = TaxonNameDescription.NewInstance(taxonName1);
		TextData textData = TextData.NewInstance(Feature.IMAGE());
		textData.putText(Language.DEFAULT(), "My media text data");
		Media media = Media.NewInstance();
		textData.addMedia(media);
		description.addElement(textData);

		//CLONE
		TaxonName clone = taxonName1.clone();
		TaxonName genusClone = genusName.clone();
		assertSame("Rank should be same", taxonName1.getRank(), clone.getRank());
		assertTrue("TaxonBases should not be cloned", clone.getTaxonBases().isEmpty());
		assertEquals("TaxonBases of original name should not be empty", 1, taxonName1.getTaxonBases().size());
		//homotypical groups are not cloned, see documentation
		assertNotSame("The clone must have a new homotypical group", taxonName1.getHomotypicalGroup(), clone.getHomotypicalGroup());
		assertNotSame("The genusClone must a new homotypical group", genusName.getHomotypicalGroup(), genusClone.getHomotypicalGroup());
		assertEquals("Only the name should be part of the new homotypical group", 1, clone.getHomotypicalGroup().getTypifiedNames().size());

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
		assertNotSame("type designation sets should not be the same", taxonName1.getTypeDesignations(), clone.getTypeDesignations());
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
		assertSame("The nyme type of the name type designation needs to be the same", taxonName1, nameTypeDesignationClone.getTypeName());
		assertSame("The status of the name type designation needs to be the same", nameTypeDesignationOriginal.getTypeStatus(), nameTypeDesignationClone.getTypeStatus());
		assertEquals("The name type designation must have exactly 1 typified name which is 'genusClone'", 1, nameTypeDesignationClone.getTypifiedNames().size());
		assertTrue("The name type designation must have 'genusClone' as typified name", nameTypeDesignationClone.getTypifiedNames().contains(genusClone));
//		assertSame("The name type designation must have the same homotypical group as the typified name", nameTypeDesignationClone.getHomotypicalGroup(), genusClone.getHomotypicalGroup());

		//status
		assertEquals("There should be exactly 1 status", 1, clone.getStatus().size());
		assertNotSame("Status sets should not be the same", taxonName1.getStatus(), clone.getStatus());
		NomenclaturalStatus cloneStatus = clone.getStatus().iterator().next();
		assertSame("The type of the nomStatus needs to be the same", nomStatus.getType(), cloneStatus.getType());
		assertSame("The citation of the nomStatus needs to be the same", nomStatus.getCitation(), cloneStatus.getCitation());
		assertSame("The rule considered of the nomStatus needs to be the same", nomStatus.getRuleConsidered(), cloneStatus.getRuleConsidered());
		assertNotSame("The nomStatus should be cloned as it is orphan removal", nomStatus, cloneStatus);

//		//hybrid parents of clone
//		assertEquals("There should be exactly 2 hybrid relationships in which the clone takes the child role", 2, clone.getChildRelationships().size());
//		Set<NonViralName> parentSet = new HashSet<>();
//		Set<NonViralName> childSet = new HashSet<>();
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
		assertEquals("There should be exactly 1 relationship in which the clone takes the -to- role", 1, clone.getRelationsToThisName().size());
		NameRelationship basionymRelationClone = clone.getRelationsToThisName().iterator().next();
		assertNotEquals(basionymRelation, basionymRelationClone);
		//related name should not be cloned, if needed e.g. for basionyms this should be handled by the calling method (see documentation)
		assertSame("Basionym should be from-object in relationship", basionym, basionymRelationClone.getRelatedFrom());
		assertSame("Clone should be to-object in relationship", clone, basionymRelationClone.getRelatedTo());
		assertSame("Relationship type should be cloned correctly", NameRelationshipType.BASIONYM(), basionymRelationClone.getType());

		//homonym of clone
		assertEquals("There should be exactly 1 relationship in which the clone takes the from role", 1, clone.getRelationsFromThisName().size());
		NameRelationship homonymRelationClone = clone.getRelationsFromThisName().iterator().next();
		assertNotEquals(homonymRelation, homonymRelationClone);
        assertSame("Clone should be from-object in relationship", clone, homonymRelationClone.getRelatedFrom());
		assertSame("Homonym should be to-object in relationship", earlierHomonym, homonymRelationClone.getRelatedTo());
		assertSame("Relationship type should be cloned correctly", NameRelationshipType.LATER_HOMONYM(), homonymRelationClone.getType());
		assertEquals("Rule should be cloned correctly", "later homonym rule", homonymRelationClone.getRuleConsidered());
	}


   /*
    * Moved from IdentifiableEntityTest to here due to #922
    */
   @Test
   public void testCompareTo() {
       TaxonName abies = TaxonNameFactory.NewNonViralInstance(Rank.GENUS(), null);
       abies.setNameCache("Abies");
       abies.setTitleCache("Abies", true);

       TaxonName abiesMill = TaxonNameFactory.NewNonViralInstance(Rank.GENUS(), null);
       abiesMill.setNameCache("Abies");
       abiesMill.setTitleCache("Abies Mill.", true);

       TaxonName abiesAlba = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES(), null);
       abiesAlba.setNameCache("Abies alba");
       abiesAlba.setTitleCache("Abies alba", true);

       TaxonName abiesAlbaMichx = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES(), null);
       abiesAlbaMichx.setNameCache("Abies alba");
       abiesAlbaMichx.setTitleCache("Abies alba Michx.", true);

       TaxonName abiesAutonym  = TaxonNameFactory.NewNonViralInstance(Rank.SECTION_BOTANY());
       abiesAutonym.setGenusOrUninomial("Abies");
       abiesAutonym.setInfraGenericEpithet("Abies");

       abiesAutonym.setTitleCache("Abies Mill. sect. Abies", true);
       abiesAutonym.getNameCache();

       TaxonName abiesBalsamea  = TaxonNameFactory.NewNonViralInstance(Rank.SECTION_BOTANY());
       abiesBalsamea.setGenusOrUninomial("Abies");
       abiesBalsamea.setInfraGenericEpithet("Balsamea");
       abiesBalsamea.getNameCache();
       abiesBalsamea.setTitleCache("Abies sect. Balsamea L.", true);

       TaxonName abiesAlbaxPinusBeta = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
       abiesAlbaxPinusBeta.setHybridFormula(true);
       abiesAlbaxPinusBeta.addHybridParent(abiesAlba, HybridRelationshipType.FIRST_PARENT(), null);

       int result = 0;

       // "Abies" < "Abies Mill."
       result = abies.compareToName(abiesMill);
       assertTrue(result < 0);

       // "Abies Mill." > "Abies"
       result = abiesMill.compareToName(abies);
       assertTrue(result > 0);

       // "Abies" < "Abies alba"
       result = abies.compareToName(abiesAlba);
       assertTrue(result < 0);

       // "Abies alba" > "Abies"
       result = abiesAlba.compareToName(abies);
       assertTrue(result > 0);

       // "Abies Mill." < "Abies alba Michx."
       result = abiesMill.compareToName(abiesAlbaMichx);
       assertTrue(result < 0);

       // "Abies alba Michx." > "Abies Mill."
       result = abiesAlbaMichx.compareToName(abiesMill);
       assertTrue(result > 0);

       //Autonym should be sorted without the author string

       result = abiesAutonym.compareToName(abiesBalsamea);
       assertTrue(result < 0);
       // Test consistency of compareTo() with equals():
       // Is consistent if and only if for every e1 and e2 of class C
       // e1.compareTo(e2) == 0 has the same boolean value as e1.equals(e2)

       boolean compareResult = false;
       boolean equalsResult = false;

       compareResult = (abies.compareToName(abies) == 0);
       equalsResult = abies.equals(abies);
       assertEquals(compareResult, equalsResult);

       compareResult = (abies.compareToName(abiesAlba) == 0);
       equalsResult = abies.equals(abiesAlba);
       assertEquals(compareResult, equalsResult);

       compareResult = (abiesMill.compareToName(abies) == 0);
       equalsResult = abiesMill.equals(abies);
       assertEquals(compareResult, equalsResult);

       //Abies alba x Pinus beta < Abies alba xinus
       TaxonName abiesAlbaXinus = TaxonNameFactory.NewBotanicalInstance(Rank.SUBSPECIES());
       abiesAlbaXinus.setGenusOrUninomial("Abies");
       abiesAlbaXinus.setSpecificEpithet("alba");
       abiesAlbaXinus.setInfraSpecificEpithet("xinus");
       result = abiesAlbaxPinusBeta.compareToName(abiesAlbaXinus);
       assertTrue(result < 0);
   }

   @Test
   public void testNomenclaturalStanding() {
       //The status and relationship combinations in this test are not semantically consistent
       //but only used for testing
       Assert.assertTrue(nameBase1.isNoStatus());
       nameBase1.addRelationshipFromName(nameBase2, NameRelationshipType.CONSERVED_AGAINST());
       Assert.assertTrue(nameBase1.isDesignationOnly());
       nameBase1.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.AMBIGUOUS()));
       Assert.assertTrue("nothing changed", nameBase1.isDesignationOnly());
       Assert.assertTrue(nameBase1.isInvalid());
       nameBase1.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ILLEGITIMATE()));
       Assert.assertFalse(nameBase1.isIllegitimate());
       Assert.assertTrue(nameBase1.isDesignationOnly());
       nameBase1.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.INVALID()));
       Assert.assertFalse(nameBase1.isDesignationOnly());
       Assert.assertTrue(nameBase1.isInvalid());
       nameBase1.addRelationshipToName(nameBase2, NameRelationshipType.CONSERVED_AGAINST());
       Assert.assertTrue(nameBase1.isValid());
   }
}