/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
 
package eu.etaxonomy.cdm.strategy.parser;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.strategy.parser.ITaxonNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author a.mueller
 *
 */
public class TaxonNameParserBotanicalNameImplTest {
	private static final Logger logger = Logger.getLogger(TaxonNameParserBotanicalNameImplTest.class);
	
	final private String strNameFamily = "Asteraceae";
	final private String strNameGenus = "Abies Mueller";
	final private String strNameGenusUnicode = "Abies Müller";
	final private String strNameAbies1 = "Abies alba";
	final private String strNameAbiesSub1 = "Abies alba subsp. beta";
	final private String strNameAbiesAuthor1 = "Abies alba Mueller";
	final private String strNameAbiesAuthor1Unicode = "Abies alba Müller";
	final private String strNameAbiesBasionymAuthor1 = "Abies alba (Ciardelli) D'Mueller";
	final private String strNameAbiesBasionymAuthor1Unicode = "Abies alba (Ciardelli) D'Müller";
	final private String strNameAbiesBasionymExAuthor1 ="Abies alba (Ciardelli ex Doering) D'Mueller ex. de Greuther"; 
	final private String strNameAbiesBasionymExAuthor1Unicode ="Abies alba (Ciardelli ex Döring) D'Müller ex. de Greuther"; 
	final private String strNameTeam1 ="Abies alba Mueller & L."; 
	
	final private String strNameEmpty = "";
	final private String strNameNull = null;
	
	private ITaxonNameParser<BotanicalName> parser ;
	
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
		parser = NonViralNameParserImpl.NewInstance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

/*************** TEST *********************************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#NEW_INSTANCE()}.
	 */
	@Test
	public final void testNEW_INSTANCE() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#TaxonNameParserBotanicalNameImpl()}.
	 */
	@Test
	public final void testTaxonNameParserBotanicalNameImpl() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseSimpleName(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)}.
	 */
	@Test
	public final void testParseSimpleName() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseSubGenericFullName(java.lang.String)}.
	 */
	@Test
	public final void testParseSubGenericFullName() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseSubGenericSimpleName(java.lang.String)}.
	 */
	@Test
	public final void testParseSubGenericSimpleName() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseFullName(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)}.
	 */
	@Test
	@Ignore //TODO Character encoding in svn
	public final void testParseFullNameUnicode() {

		BotanicalName nameAuthor = parser.parseFullName(strNameAbiesAuthor1Unicode, Rank.SPECIES());
		assertEquals("Abies", nameAuthor.getGenusOrUninomial());
		assertEquals("alba", nameAuthor.getSpecificEpithet());
		assertEquals("Müller", nameAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
		
		BotanicalName nameBasionymAuthor = parser.parseFullName(strNameAbiesBasionymAuthor1Unicode, Rank.SPECIES());
		assertEquals("Abies", nameBasionymAuthor.getGenusOrUninomial());
		assertEquals("alba", nameBasionymAuthor.getSpecificEpithet());
		assertEquals("D'Müller", nameBasionymAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
		BotanicalName basionym = (BotanicalName)nameBasionymAuthor.getBasionym();
		assertEquals("Ciardelli", basionym.getCombinationAuthorTeam().getNomenclaturalTitle());
		
		BotanicalName nameBasionymExAuthor = parser.parseFullName(strNameAbiesBasionymExAuthor1Unicode, Rank.SPECIES());
		assertEquals("Abies", nameBasionymExAuthor.getGenusOrUninomial());
		assertEquals("alba", nameBasionymExAuthor.getSpecificEpithet());
		assertEquals("D'Müller", nameBasionymExAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
		assertEquals("de Greuther", nameBasionymExAuthor.getExCombinationAuthorTeam().getNomenclaturalTitle());
		BotanicalName basionym2 = (BotanicalName)nameBasionymExAuthor.getBasionym();
		assertEquals("Ciardelli", basionym2.getCombinationAuthorTeam().getNomenclaturalTitle());
		assertEquals("Döring", basionym2.getExCombinationAuthorTeam().getNomenclaturalTitle());
	}
	
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseFullName(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)}.
	 */
	@Test
	public final void testParseFullName() {
		
			BotanicalName name1 = parser.parseFullName(strNameAbies1, Rank.SPECIES());
			assertEquals(name1.getGenusOrUninomial(), "Abies");
			assertEquals(name1.getSpecificEpithet(), "alba");
			
			BotanicalName nameAuthor = parser.parseFullName(strNameAbiesAuthor1, Rank.SPECIES());
			assertEquals("Abies", nameAuthor.getGenusOrUninomial());
			assertEquals("alba", nameAuthor.getSpecificEpithet());
			assertEquals("Mueller", nameAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
			
			BotanicalName nameBasionymAuthor = parser.parseFullName(strNameAbiesBasionymAuthor1, Rank.SPECIES());
			assertEquals("Abies", nameBasionymAuthor.getGenusOrUninomial());
			assertEquals("alba", nameBasionymAuthor.getSpecificEpithet());
			assertEquals("D'Mueller", nameBasionymAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
			assertEquals("Ciardelli", nameBasionymAuthor.getBasionymAuthorTeam().getNomenclaturalTitle());
			
			BotanicalName nameBasionymExAuthor = parser.parseFullName(strNameAbiesBasionymExAuthor1, Rank.SPECIES());
			assertEquals("Abies", nameBasionymExAuthor.getGenusOrUninomial());
			assertEquals("alba", nameBasionymExAuthor.getSpecificEpithet());
			assertEquals("D'Mueller", nameBasionymExAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
			assertEquals("de Greuther", nameBasionymExAuthor.getExCombinationAuthorTeam().getNomenclaturalTitle());
			assertEquals("Ciardelli", nameBasionymExAuthor.getBasionymAuthorTeam().getNomenclaturalTitle());
			assertEquals("Doering", nameBasionymExAuthor.getExBasionymAuthorTeam().getNomenclaturalTitle());
			
			BotanicalName name2 = parser.parseFullName(strNameAbiesSub1, Rank.SPECIES());
			assertEquals(name2.getGenusOrUninomial(), "Abies");
			assertEquals(name2.getSpecificEpithet(), "alba");
			assertEquals(name2.getInfraSpecificEpithet(), "beta");
			assertEquals(Rank.SUBSPECIES(), name2.getRank());
			
			//Team
			BotanicalName nameTeam1 = parser.parseFullName(strNameTeam1);
			assertEquals( "Abies", nameTeam1.getGenusOrUninomial());
			assertEquals( "alba", nameTeam1.getSpecificEpithet());
			assertEquals("Mueller & L.",  nameTeam1.getCombinationAuthorTeam().getNomenclaturalTitle());
			assertTrue(nameTeam1.getCombinationAuthorTeam() instanceof Team);
			Team team = (Team)nameTeam1.getCombinationAuthorTeam();
			assertEquals("Mueller", team.getTeamMembers().get(0).getNomenclaturalTitle());
			assertEquals("L.", team.getTeamMembers().get(1).getNomenclaturalTitle());

			
			// unparseable *********
			String problemString = "sdfjlös wer eer wer";
			BotanicalName nameProblem = parser.parseFullName(problemString, Rank.SPECIES());
			assertTrue(nameProblem.getHasProblem());
			assertEquals(problemString, nameProblem.getTitleCache());
			
			//empty
			BotanicalName nameEmpty = parser.parseFullName(strNameEmpty);
			assertNotNull(nameEmpty);
			assertEquals("", nameEmpty.getTitleCache());
			
			//null
			BotanicalName nameNull = parser.parseFullName(strNameNull);
			assertNull(nameNull);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#fullTeams(java.lang.String)}.
	 */
	@Test
	public final void testFullTeams() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#authorTeamAndEx(java.lang.String)}.
	 */
	@Test
	public final void testAuthorTeamAndEx() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#authorTeam(java.lang.String)}.
	 */
	@Test
	public final void testAuthorTeam() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseCultivar(java.lang.String)}.
	 */
	@Test
	public final void testParseCultivar() {
		logger.warn("Not yet implemented"); // TODO
	}

}
