/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

public class BotanicalNameTest extends EntityTestBase{

    private static final Logger logger = LogManager.getLogger();

	private TaxonName botanicalName1;

	@BeforeEach
	public void setUp() throws Exception {
		botanicalName1 = TaxonNameFactory.NewBotanicalInstance(null);
	}

/****** TESTS *******************************/

	@Test
	public final void testPARSED_NAME() {
		String fullName = "Abies alba subsp. beta (L.) Mill.";
		IBotanicalName name = TaxonNameFactory.PARSED_BOTANICAL(fullName);
		assertFalse(name.hasProblem());
		assertEquals("beta", name.getInfraSpecificEpithet());
	}

	@Test
	public final void testBotanicalName() {
		assertNotNull(botanicalName1);
		assertNull(botanicalName1.getRank());
	}

	@Test
	public final void testBotanicalNameRank() {
		Rank genus = Rank.GENUS();
		IBotanicalName rankName = TaxonNameFactory.NewBotanicalInstance(genus);
		assertNotNull(rankName);
		assertSame(genus, rankName.getRank());
		assertTrue(rankName.getRank().isGenus());
		IBotanicalName nullRankName = TaxonNameFactory.NewBotanicalInstance(null);
		assertNotNull(nullRankName);
		assertNull(nullRankName.getRank());
	}

	@Test
	public final void testBotanicalNameRankStringStringStringAgentReferenceString() {
		Rank rank = Rank.SPECIALFORM();
		String genusOrUninomial = "Genus";
		String infraGenericEpithet = "infraGenericEpi";
		String specificEpithet = "specEpi";
		String infraSpecificEpithet = "infraSpecificEpi";
		TeamOrPersonBase<?> combinationAuthorship = Team.NewInstance();
		Reference nomenclaturalReference = ReferenceFactory.newArticle();
		String nomenclMicroRef = "microRef";
		HomotypicalGroup homotypicalGroup = new HomotypicalGroup();
		IBotanicalName fullName = TaxonNameFactory.NewBotanicalInstance(rank, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet, combinationAuthorship, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
		assertEquals(Rank.SPECIALFORM(), fullName.getRank());
		assertEquals("Genus", fullName.getGenusOrUninomial());
		assertEquals("infraGenericEpi", fullName.getInfraGenericEpithet());
		assertEquals("specEpi", fullName.getSpecificEpithet());
		assertEquals("infraSpecificEpi", fullName.getInfraSpecificEpithet());
		assertEquals(combinationAuthorship, fullName.getCombinationAuthorship());
		assertEquals(nomenclaturalReference, fullName.getNomenclaturalReference());
		assertEquals("microRef", fullName.getNomenclaturalMicroReference());
		assertSame(homotypicalGroup, fullName.getHomotypicalGroup());
	}

	@Test
	public final void testGetParentRelationships() {
		assertEquals(0, botanicalName1.getHybridParentRelations().size());
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetChildRelationships() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testIsSetHybridFormula() {
		assertFalse(botanicalName1.isHybridFormula());
		botanicalName1.setHybridFormula(true);
		assertTrue(botanicalName1.isHybridFormula());
		botanicalName1.setHybridFormula(false);
		assertFalse(botanicalName1.isHybridFormula());
	}

	@Test
	public final void testIsSetMonomHybrid() {
		assertFalse(botanicalName1.isMonomHybrid());
		botanicalName1.setMonomHybrid(true);
		assertTrue(botanicalName1.isMonomHybrid());
		botanicalName1.setMonomHybrid(false);
		assertFalse(botanicalName1.isMonomHybrid());
	}

	@Test
	public final void testIsSetBinomHybrid() {
		assertFalse(botanicalName1.isBinomHybrid());
		botanicalName1.setBinomHybrid(true);
		assertTrue(botanicalName1.isBinomHybrid());
		botanicalName1.setBinomHybrid(false);
		assertFalse(botanicalName1.isBinomHybrid());
	}

	@Test
	public final void testIsTrinomHybrid() {
		assertFalse(botanicalName1.isTrinomHybrid());
		botanicalName1.setTrinomHybrid(true);
		assertTrue(botanicalName1.isTrinomHybrid());
		botanicalName1.setTrinomHybrid(false);
		assertFalse(botanicalName1.isTrinomHybrid());
	}

	@Test
	public final void testIsAnamorphic() {
		assertFalse(botanicalName1.isAnamorphic());
		botanicalName1.setAnamorphic(true);
		assertTrue(botanicalName1.isAnamorphic());
		botanicalName1.setAnamorphic(false);
		assertFalse(botanicalName1.isAnamorphic());
	}

	@Test
	public void testClone(){
		botanicalName1.setGenusOrUninomial("Aus");
		botanicalName1.setRank(Rank.GENUS());
		botanicalName1.setAnamorphic(true);
		TaxonName clone = botanicalName1.clone();
		assertEquals(true, clone.isAnamorphic(), "Anamorphic should be equal");
		botanicalName1.setAnamorphic(false);
		clone = botanicalName1.clone();
		assertEquals(false, clone.isAnamorphic(), "Anamorphic should be equal");
	}

    @Test
    public void beanTests(){
//      #5307 Test that BeanUtils does not fail
//        BeanUtils.getPropertyDescriptors(BotanicalName.class);
        BeanUtils.getPropertyDescriptors(TaxonName.class);
//        BeanUtils.getPropertyDescriptors(NonViralName.class);
//        BeanUtils.getPropertyDescriptors(ZoologicalName.class);
//        BeanUtils.getPropertyDescriptors(ViralName.class);
//        BeanUtils.getPropertyDescriptors(CultivarPlantName.class);
    }
}
