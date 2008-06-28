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
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author a.mueller
 *
 */
public class NonViralNameParserImplTest {
	private static final Logger logger = Logger.getLogger(NonViralNameParserImplTest.class);
	
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
	final private String strNameTeam1 = "Abies alba Mueller & L."; 
	final private String strNameZoo1 = "Abies alba Mueller & L., 1822";
	final private String strNameZoo2 = "Abies alba (Mueller, 1822) Ciardelli, 2002";
	
	final private String strNameEmpty = "";
	final private String strNameNull = null;
	
	private INonViralNameParser<NonViralName> parser ;
	private NomenclaturalCode botanicCode; 
	
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
		botanicCode = NomenclaturalCode.ICBN();
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

		NonViralName nameAuthor = parser.parseFullName(strNameAbiesAuthor1Unicode, null, Rank.SPECIES());
		assertEquals("Abies", nameAuthor.getGenusOrUninomial());
		assertEquals("alba", nameAuthor.getSpecificEpithet());
		assertEquals("Müller", nameAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
		
		NonViralName nameBasionymAuthor = parser.parseFullName(strNameAbiesBasionymAuthor1Unicode, null, Rank.SPECIES());
		assertEquals("Abies", nameBasionymAuthor.getGenusOrUninomial());
		assertEquals("alba", nameBasionymAuthor.getSpecificEpithet());
		assertEquals("D'Müller", nameBasionymAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
		BotanicalName basionym = (BotanicalName)nameBasionymAuthor.getBasionym();
		assertEquals("Ciardelli", basionym.getCombinationAuthorTeam().getNomenclaturalTitle());
		
		NonViralName nameBasionymExAuthor = parser.parseFullName(strNameAbiesBasionymExAuthor1Unicode, null, Rank.SPECIES());
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
		NonViralName name1 = parser.parseFullName(strNameAbies1, null, Rank.SPECIES());
		assertEquals(name1.getGenusOrUninomial(), "Abies");
		assertEquals(name1.getSpecificEpithet(), "alba");
		
		NonViralName nameAuthor = parser.parseFullName(strNameAbiesAuthor1, null, Rank.SPECIES());
		assertEquals("Abies", nameAuthor.getGenusOrUninomial());
		assertEquals("alba", nameAuthor.getSpecificEpithet());
		assertEquals("Mueller", nameAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
		
		NonViralName nameBasionymAuthor = parser.parseFullName(strNameAbiesBasionymAuthor1, null, Rank.SPECIES());
		assertEquals("Abies", nameBasionymAuthor.getGenusOrUninomial());
		assertEquals("alba", nameBasionymAuthor.getSpecificEpithet());
		assertEquals("D'Mueller", nameBasionymAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
		assertEquals("Ciardelli", nameBasionymAuthor.getBasionymAuthorTeam().getNomenclaturalTitle());
		
		NonViralName nameBasionymExAuthor = parser.parseFullName(strNameAbiesBasionymExAuthor1, null, Rank.SPECIES());
		assertEquals("Abies", nameBasionymExAuthor.getGenusOrUninomial());
		assertEquals("alba", nameBasionymExAuthor.getSpecificEpithet());
		assertEquals("D'Mueller", nameBasionymExAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
		assertEquals("de Greuther", nameBasionymExAuthor.getExCombinationAuthorTeam().getNomenclaturalTitle());
		assertEquals("Ciardelli", nameBasionymExAuthor.getBasionymAuthorTeam().getNomenclaturalTitle());
		assertEquals("Doering", nameBasionymExAuthor.getExBasionymAuthorTeam().getNomenclaturalTitle());
		
		NonViralName name2 = parser.parseFullName(strNameAbiesSub1, null, Rank.SPECIES());
		assertEquals(name2.getGenusOrUninomial(), "Abies");
		assertEquals(name2.getSpecificEpithet(), "alba");
		assertEquals(name2.getInfraSpecificEpithet(), "beta");
		assertEquals(Rank.SUBSPECIES(), name2.getRank());
		
		//Team
		NonViralName nameTeam1 = parser.parseFullName(strNameTeam1);
		assertEquals( "Abies", nameTeam1.getGenusOrUninomial());
		assertEquals( "alba", nameTeam1.getSpecificEpithet());
		assertEquals("Mueller & L.",  nameTeam1.getCombinationAuthorTeam().getNomenclaturalTitle());
		assertTrue(nameTeam1.getCombinationAuthorTeam() instanceof Team);
		Team team = (Team)nameTeam1.getCombinationAuthorTeam();
		assertEquals("Mueller", team.getTeamMembers().get(0).getNomenclaturalTitle());
		assertEquals("L.", team.getTeamMembers().get(1).getNomenclaturalTitle());

		//ZooName
		ZoologicalName nameZoo1 = (ZoologicalName)parser.parseFullName(strNameZoo1);
		assertEquals( "Abies", nameZoo1.getGenusOrUninomial());
		assertEquals( "alba", nameZoo1.getSpecificEpithet());
		assertEquals("Mueller & L.",  nameZoo1.getCombinationAuthorTeam().getNomenclaturalTitle());
		assertEquals(NomenclaturalCode.ICZN(), nameZoo1.getNomenclaturalCode() );
		assertEquals(Integer.valueOf(1822), nameZoo1.getPublicationYear());
		assertTrue(nameZoo1.getCombinationAuthorTeam() instanceof Team);
		Team teamZoo = (Team)nameZoo1.getCombinationAuthorTeam();
		assertEquals("Mueller", teamZoo.getTeamMembers().get(0).getNomenclaturalTitle());
		assertEquals("L.", teamZoo.getTeamMembers().get(1).getNomenclaturalTitle());

		ZoologicalName nameZoo2 = (ZoologicalName)parser.parseFullName(strNameZoo2);
		assertEquals(Integer.valueOf(2002), nameZoo2.getPublicationYear());
		assertEquals(Integer.valueOf(1822), nameZoo2.getOriginalPublicationYear());
		assertEquals("Mueller",  nameZoo2.getBasionymAuthorTeam().getNomenclaturalTitle());
		assertEquals("Ciardelli",  nameZoo2.getCombinationAuthorTeam().getNomenclaturalTitle());
		
		String strNotParsableZoo = "Abies alba M., 1923, Sp. P. xxwer4352";
		ZoologicalName nameZooRefNotParsabel = (ZoologicalName)parser.parseFullReference(strNotParsableZoo, null, null);
		assertTrue(nameZooRefNotParsabel.hasProblem());
		assertEquals(NomenclaturalCode.ICZN(), nameZooRefNotParsabel.getNomenclaturalCode());
		assertEquals(Integer.valueOf(1923), nameZooRefNotParsabel.getPublicationYear());
		
		// unparseable *********
		String problemString = "sdfjlös wer eer wer";
		NonViralName nameProblem = parser.parseFullName(problemString, null, Rank.SPECIES());
		assertTrue(nameProblem.getHasProblem());
		assertEquals(problemString, nameProblem.getTitleCache());
		
		//empty
		NonViralName nameEmpty = parser.parseFullName(strNameEmpty);
		assertNotNull(nameEmpty);
		assertEquals("", nameEmpty.getTitleCache());
		
		//null
		NonViralName nameNull = parser.parseFullName(strNameNull);
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
