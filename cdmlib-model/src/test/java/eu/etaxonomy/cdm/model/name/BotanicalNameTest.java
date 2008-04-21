package eu.etaxonomy.cdm.model.name;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;


public class BotanicalNameTest extends EntityTestBase{
	private static final Logger logger = Logger.getLogger(BotanicalNameTest.class);
	
	private BotanicalName botanicalName1;
	private BotanicalName botanicalName2;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		botanicalName1 = new BotanicalName();
		botanicalName2 = new BotanicalName();
	}

	@After
	public void tearDown() throws Exception {
	}
	
/****** TESTS *******************************/
	
	@Test
	public final void testPARSED_NAME() {
		String fullName = "Abies alba subsp. beta (L.) Mill.";
		BotanicalName name = BotanicalName.PARSED_NAME(fullName);
		assertFalse(name.getHasProblem());
		assertEquals("beta", name.getInfraSpecificEpithet());
	}

	@Test
	public final void testBotanicalName() {
		assertNotNull(botanicalName1);
		assertNull(botanicalName1.getRank());
	}

	@Test
	public final void testBotanicalNameRank() {
		BotanicalName rankName = BotanicalName.NewInstance(Rank.GENUS());
		assertNotNull(rankName);
		assertSame(Rank.GENUS(), rankName.getRank());
		assertTrue(rankName.getRank().isGenus());
		BotanicalName nullRankName = BotanicalName.NewInstance(null);
		assertNotNull(nullRankName);
		assertNull(nullRankName.getRank());
	}

	@Test
	public final void testBotanicalNameRankStringStringStringAgentINomenclaturalReferenceString() {
		Rank rank = Rank.SPECIALFORM();
		String genusOrUninomial = "Genus";
		String specificEpithet = "specEpi";
		String infraSpecificEpithet = "infraSpecificEpi";
		TeamOrPersonBase combinationAuthorTeam = Team.NewInstance();
		INomenclaturalReference nomenclaturalReference = new Article();
		String nomenclMicroRef = "microRef";
		HomotypicalGroup homotypicalGroup = new HomotypicalGroup();
		BotanicalName fullName = new BotanicalName(rank, genusOrUninomial, specificEpithet, infraSpecificEpithet, combinationAuthorTeam, nomenclaturalReference, nomenclMicroRef, homotypicalGroup);
		assertEquals(Rank.SPECIALFORM(), fullName.getRank());
		assertEquals("Genus", fullName.getGenusOrUninomial());
		assertEquals("specEpi", fullName.getSpecificEpithet());
		assertEquals("infraSpecificEpi", fullName.getInfraSpecificEpithet());
		assertEquals(combinationAuthorTeam, fullName.getCombinationAuthorTeam());
		assertEquals(nomenclaturalReference, fullName.getNomenclaturalReference());
		assertEquals("microRef", fullName.getNomenclaturalMicroReference());
		assertSame(homotypicalGroup, fullName.getHomotypicalGroup());
	}

	@Test
	public final void testGetHybridRelationships() {
		assertEquals(0, botanicalName1.getHybridRelationships().size());
		HybridRelationship hybridRelationship = new HybridRelationship();
		botanicalName1.addHybridRelationship(hybridRelationship);
		assertEquals(1, botanicalName1.getHybridRelationships().size());
		assertEquals(hybridRelationship, botanicalName1.getHybridRelationships().iterator().next());
		botanicalName2.addHybridRelationship(null);
		assertEquals(1, botanicalName2.getHybridRelationships().size());
	}

	@Test
	@Ignore
	public final void testSetHybridRelationships() {
		assertEquals(0, botanicalName1.getHybridRelationships().size());
		
		HybridRelationship hybridRelationship1 = new HybridRelationship();
		HybridRelationship hybridRelationship2 = new HybridRelationship();
		Set set = new HashSet<HybridRelationship>();
		set.add(hybridRelationship1);
		set.add(hybridRelationship2);
		botanicalName1.setHybridRelationships(set);
		assertEquals(2, botanicalName1.getHybridRelationships().size());
		botanicalName1.setHybridRelationships(set);
		set.add("sdfds");
		//TODO
		//assertEquals(2, botanicalName1.getHybridRelationships().size());
		logger.warn("not yet fully implemented");
		botanicalName2.setHybridRelationships(null);
		//TODO how should this be defined??
		assertNull(botanicalName2.getHybridRelationships());
	}

	@Test
	public final void testAddHybridRelationship() {
		assertEquals(0, botanicalName1.getHybridRelationships().size());
		HybridRelationship hybridRelationship = new HybridRelationship();
		botanicalName1.addHybridRelationship(hybridRelationship);
		assertEquals(1, botanicalName1.getHybridRelationships().size());
		botanicalName1.addHybridRelationship(hybridRelationship);
		assertEquals(1, botanicalName1.getHybridRelationships().size());
		assertEquals(hybridRelationship, botanicalName1.getHybridRelationships().iterator().next());
		botanicalName2.addHybridRelationship(null);
		//TODO is this wanted or should it be 0 ??
		assertEquals(1, botanicalName2.getHybridRelationships().size());
	}

	@Test
	public final void testRemoveHybridRelationship() {
		assertEquals(0, botanicalName1.getHybridRelationships().size());
		HybridRelationship hybridRelationship1 = new HybridRelationship();
		HybridRelationship hybridRelationship2 = new HybridRelationship();
		botanicalName1.addHybridRelationship(hybridRelationship1);
		botanicalName1.addHybridRelationship(hybridRelationship2);
		assertEquals(2, botanicalName1.getHybridRelationships().size());
		botanicalName1.removeHybridRelationship(hybridRelationship1);
		assertEquals(1, botanicalName1.getHybridRelationships().size());
		assertEquals(hybridRelationship2, botanicalName1.getHybridRelationships().iterator().next());
		botanicalName1.removeHybridRelationship(hybridRelationship2);
		assertEquals(0, botanicalName1.getHybridRelationships().size());
		
		//null
		botanicalName2.addHybridRelationship(null);
		botanicalName2.removeHybridRelationship(null);
		assertEquals(0, botanicalName2.getHybridRelationships().size());
	}

	@Test
	public final void testGetParentRelationships() {
		assertEquals(0, botanicalName1.getParentRelationships().size());
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

}
