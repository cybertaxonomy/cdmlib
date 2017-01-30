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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;


public class BotanicalNameTest extends EntityTestBase{
	private static final Logger logger = Logger.getLogger(BotanicalNameTest.class);

	private BotanicalName botanicalName1;
	private BotanicalName botanicalName2;

	@BeforeClass
	public static void setUpBeforeClass() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
	}

	@Before
	public void setUp() throws Exception {
		botanicalName1 = new BotanicalName();
		botanicalName2 = new BotanicalName();
	}

/****** TESTS *******************************/

	@Test
	public final void testPARSED_NAME() {
		String fullName = "Abies alba subsp. beta (L.) Mill.";
		BotanicalName name = TaxonNameFactory.PARSED_BOTANICAL(fullName);
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
		BotanicalName rankName = TaxonNameFactory.NewBotanicalInstance(genus);
		assertNotNull(rankName);
		assertSame(genus, rankName.getRank());
		assertTrue(rankName.getRank().isGenus());
		BotanicalName nullRankName = TaxonNameFactory.NewBotanicalInstance(null);
		assertNotNull(nullRankName);
		assertNull(nullRankName.getRank());
	}

	@Test
	public final void testBotanicalNameRankStringStringStringAgentINomenclaturalReferenceString() {
		Rank rank = Rank.SPECIALFORM();
		String genusOrUninomial = "Genus";
		String infraGenericEpithet = "infraGenericEpi";
		String specificEpithet = "specEpi";
		String infraSpecificEpithet = "infraSpecificEpi";
		TeamOrPersonBase<?> combinationAuthorship = Team.NewInstance();
		INomenclaturalReference nomenclaturalReference = ReferenceFactory.newArticle();
		String nomenclMicroRef = "microRef";
		HomotypicalGroup homotypicalGroup = new HomotypicalGroup();
		BotanicalName fullName = new BotanicalName(rank, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet, combinationAuthorship, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
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
		BotanicalName clone = (BotanicalName)botanicalName1.clone();
		Assert.assertEquals("Anamorphic should be equal", true, clone.isAnamorphic());
		botanicalName1.setAnamorphic(false);
		clone = (BotanicalName)botanicalName1.clone();
		Assert.assertEquals("Anamorphic should be equal", false, clone.isAnamorphic());
	}

    @Test
    public void beanTests(){
//      #5307 Test that BeanUtils does not fail
        BeanUtils.getPropertyDescriptors(BotanicalName.class);
        BeanUtils.getPropertyDescriptors(TaxonNameBase.class);
        BeanUtils.getPropertyDescriptors(NonViralName.class);
        BeanUtils.getPropertyDescriptors(ZoologicalName.class);
        BeanUtils.getPropertyDescriptors(ViralName.class);
        BeanUtils.getPropertyDescriptors(CultivarPlantName.class);
    }
}
