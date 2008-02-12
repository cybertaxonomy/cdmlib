package eu.etaxonomy.cdm.model.name;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.log4j.Logger;

public class BotanicalNameTest {
	private static final Logger logger = Logger.getLogger(BotanicalNameTest.class);
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
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
	public final void testIsHybridFormula() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testSetHybridFormula() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testIsMonomHybrid() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testSetMonomHybrid() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testIsBinomHybrid() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testSetBinomHybrid() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testIsTrinomHybrid() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testSetTrinomHybrid() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testIsAnamorphic() {
		logger.warn("Not yet implemented"); // TODO
	}

	@Test
	public final void testSetAnamorphic() {
		logger.warn("Not yet implemented"); // TODO
	}

}
