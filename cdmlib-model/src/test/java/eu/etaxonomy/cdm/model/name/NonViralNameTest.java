/**
 * 
 */
package eu.etaxonomy.cdm.model.name;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 *
 */
public class NonViralNameTest extends EntityTestBase {
	private static Logger logger = Logger.getLogger(NonViralNameTest.class);

	
	NonViralName<NonViralName> nonViralName1;
	NonViralName<NonViralName> nonViralName2;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		nonViralName1 = new NonViralName<NonViralName>();
		nonViralName2 = new NonViralName<NonViralName>();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

// ******************* TESTS ***********************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#generateTitle()}.
	 */
	@Test
	public final void testGenerateTitle() {
		String fullName = "Abies alba subsp. beta (L.) Mill.";
		nonViralName1.setGenusOrUninomial("Genus");
		nonViralName1.setSpecificEpithet("spec");
		nonViralName1.setRank(Rank.SPECIES());
		assertEquals("Genus spec", nonViralName1.generateTitle());
		assertEquals("", nonViralName2.generateTitle());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#isCodeCompliant()}.
	 */
	@Test
	public final void testIsCodeCompliant() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#NonViralName()}.
	 */
	@Test
	public final void testNonViralName() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#NonViralName(eu.etaxonomy.cdm.model.name.Rank)}.
	 */
	@Test
	public final void testNonViralNameRank() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#NonViralName(eu.etaxonomy.cdm.model.name.Rank, java.lang.String, java.lang.String, java.lang.String, eu.etaxonomy.cdm.model.agent.Agent, eu.etaxonomy.cdm.model.reference.INomenclaturalReference, java.lang.String)}.
	 */
	@Test
	public final void testNonViralNameRankStringStringStringAgentINomenclaturalReferenceString() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getCombinationAuthorTeam()}.
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#setCombinationAuthorTeam(eu.etaxonomy.cdm.model.agent.Agent)}.
	 */
	@Test
	public final void testGetSetCombinationAuthorTeam() {
		Team team1 = new Team();
		nonViralName1.setCombinationAuthorTeam(team1);
		assertEquals(team1, nonViralName1.getCombinationAuthorTeam());
		nonViralName1.setCombinationAuthorTeam(null);
		assertEquals(null, nonViralName1.getCombinationAuthorTeam());
		nonViralName2.setCombinationAuthorTeam(null);
		assertEquals(null, nonViralName2.getCombinationAuthorTeam());
	}

	/**
	 */
	@Test
	public final void testSetCombinationAuthorTeam() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getExCombinationAuthorTeam()}.
	 */
	@Test
	public final void testGetSetExCombinationAuthorTeam() {
		Team team1 = new Team();
		nonViralName1.setExCombinationAuthorTeam(team1);
		assertEquals(team1, nonViralName1.getExCombinationAuthorTeam());
		nonViralName1.setExCombinationAuthorTeam(null);
		assertEquals(null, nonViralName1.getExCombinationAuthorTeam());
		nonViralName2.setExCombinationAuthorTeam(null);
		assertEquals(null, nonViralName2.getExCombinationAuthorTeam());
	}



	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#setBasionymAuthorTeam(eu.etaxonomy.cdm.model.agent.Agent)}.
	 * and Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getBasionymAuthorTeam()}.
	 */
	@Test
	public final void testGetSetBasionymAuthorTeam() {
		NonViralName nvnBasio = new NonViralName<NonViralName>();
		NonViralName<NonViralName> nvnNoBasionym = new NonViralName<NonViralName>();
		NonViralName<NonViralName> nvnNewComb = new NonViralName<NonViralName>();
		NonViralName<NonViralName> nvnNewCombNull = new NonViralName<NonViralName>();
		
		//team1
		Team team1 = new Team();
		assertNull(nvnNoBasionym.getBasionym());
		nvnNoBasionym.setBasionymAuthorTeam(team1);
		assertEquals(team1, nvnNoBasionym.getBasionymAuthorTeam());
		assertEquals(team1, nvnNoBasionym.getBasionym().getCombinationAuthorTeam());

		//Null
		assertNull(nvnNewCombNull.getBasionym());
		nvnNewCombNull.setBasionymAuthorTeam(null);
		assertEquals(null, nvnNewCombNull.getBasionymAuthorTeam());
		assertEquals(null, nvnNewCombNull.getBasionym().getCombinationAuthorTeam());
		
		//nvnBasio
		Team teamBasioOld = new Team();
		nvnBasio.setCombinationAuthorTeam(teamBasioOld);
		nvnNewComb.setBasionym(nvnBasio);
		assertEquals(teamBasioOld, nvnNewComb.getBasionymAuthorTeam());
		Team teamBasioNew = new Team();
		nvnNewComb.setBasionymAuthorTeam(teamBasioNew);
		assertEquals(teamBasioNew, nvnNewComb.getBasionymAuthorTeam());
		nvnNewComb.setBasionymAuthorTeam(null);
		assertNull(nvnNewComb.getBasionymAuthorTeam());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#setExBasionymAuthorTeam(eu.etaxonomy.cdm.model.agent.Agent)}.
	 */
	@Test
	public final void testGetSetExBasionymAuthorTeam() {
		NonViralName nvnBasio = new NonViralName<NonViralName>();
		NonViralName<NonViralName> nvnNoBasio = new NonViralName<NonViralName>();
		NonViralName<NonViralName> nvnNewComb = new NonViralName<NonViralName>();
		NonViralName<NonViralName> nvnNewCombNull = new NonViralName<NonViralName>();
		
		//team1
		Team team1 = new Team();
		assertNull(nvnNoBasio.getBasionym());
		nvnNoBasio.setExBasionymAuthorTeam(team1);
		assertEquals(team1, nvnNoBasio.getExBasionymAuthorTeam());
		assertEquals(team1, nvnNoBasio.getBasionym().getExCombinationAuthorTeam());

		//Null
		assertNull(nvnNewCombNull.getBasionym());
		nvnNewCombNull.setExBasionymAuthorTeam(null);
		assertEquals(null, nvnNewCombNull.getExBasionymAuthorTeam());
		assertEquals(null, nvnNewCombNull.getBasionym().getExCombinationAuthorTeam());

		
		//nvnBasio
		Team teamBasioOld = new Team();
		nvnBasio.setExCombinationAuthorTeam(teamBasioOld);
		nvnNewComb.setBasionym(nvnBasio);
		assertEquals(teamBasioOld, nvnNewComb.getExBasionymAuthorTeam());
		Team teamBasioNew = new Team();
		nvnNewComb.setExBasionymAuthorTeam(teamBasioNew);
		assertEquals(teamBasioNew, nvnNewComb.getExBasionymAuthorTeam());
		nvnNewComb.setExBasionymAuthorTeam(null);
		assertNull(nvnNewComb.getExBasionymAuthorTeam());

	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getGenusOrUninomial()}.
	 */
	@Test
	public final void testGetSetGenusOrUninomial() {
		nonViralName1.setGenusOrUninomial("Genus");
		assertEquals("Genus", nonViralName1.getGenusOrUninomial());
		nonViralName1.setGenusOrUninomial(null);
		assertEquals(null, nonViralName1.getGenusOrUninomial());
		nonViralName2.setGenusOrUninomial(null);
		assertEquals(null, nonViralName2.getGenusOrUninomial());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getInfraGenericEpithet()}.
	 */
	@Test
	public final void testGetSetInfraGenericEpithet() {
		nonViralName1.setInfraGenericEpithet("InfraGenus");
		assertEquals("InfraGenus", nonViralName1.getInfraGenericEpithet());
		nonViralName1.setInfraGenericEpithet(null);
		assertEquals(null, nonViralName1.getInfraGenericEpithet());
		nonViralName2.setInfraGenericEpithet(null);
		assertEquals(null, nonViralName2.getInfraGenericEpithet());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getSpecificEpithet()}.
	 */
	@Test
	public final void testGetSetSpecificEpithet() {
		nonViralName1.setSpecificEpithet("specEpi");
		assertEquals("specEpi", nonViralName1.getSpecificEpithet());
		nonViralName1.setSpecificEpithet(null);
		assertEquals(null, nonViralName1.getSpecificEpithet());
		nonViralName2.setSpecificEpithet(null);
		assertEquals(null, nonViralName2.getSpecificEpithet());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getInfraSpecificEpithet()}.
	 */
	@Test
	public final void testGetSetInfraSpecificEpithet() {
		nonViralName1.setInfraSpecificEpithet("InfraSpecEpi");
		assertEquals("InfraSpecEpi", nonViralName1.getInfraSpecificEpithet());
		nonViralName1.setInfraSpecificEpithet(null);
		assertEquals(null, nonViralName1.getInfraSpecificEpithet());
		nonViralName2.setInfraSpecificEpithet(null);
		assertEquals(null, nonViralName2.getInfraSpecificEpithet());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#getAuthorshipCache()}.
	 */
	@Test
	public final void testGetAuthorshipCache() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.name.NonViralName#setAuthorshipCache(java.lang.String)}.
	 */
	@Test
	public final void testSetAuthorshipCache() {
		logger.warn("Not yet implemented"); // TODO
	}
}
