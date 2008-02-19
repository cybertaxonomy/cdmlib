package eu.etaxonomy.cdm.model.name;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.log4j.Logger;

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
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testBotanicalNameRank() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testBotanicalNameRankStringStringStringAgentINomenclaturalReferenceString() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetHybridRelationships() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testSetHybridRelationships() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testAddHybridRelationship() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testRemoveHybridRelationship() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetParentRelationships() {
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
