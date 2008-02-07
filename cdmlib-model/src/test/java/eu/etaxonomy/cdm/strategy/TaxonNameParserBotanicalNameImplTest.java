/**
 * 
 */
package eu.etaxonomy.cdm.strategy;

import static org.junit.Assert.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.strategy.exceptions.StringNotParsableException;

/**
 * @author a.mueller
 *
 */
public class TaxonNameParserBotanicalNameImplTest {
	private static final Logger logger = Logger.getLogger(TaxonNameParserBotanicalNameImplTest.class);
	
	final private String strNameFamily = "Asteraceae";
	final private String strNameGenus = "Abies Müller";
	final private String strNameAbies1 = "Abies alba";
	final private String strNameAbiesSub1 = "Abies alba subsp. beta";
	final private String strNameAbiesAuthor1 = "Abies alba Müller";
	final private String strNameAbiesBasionymAuthor1 = "Abies alba (Ciardelli) D'Müller";
	final private String strNameAbiesBasionymExAuthor1 ="Abies alba (Ciardelli ex Döhring) D'Müller ex. de Greuther"; 
	
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
		parser = TaxonNameParserBotanicalNameImpl.NEW_INSTANCE();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

/*************** TEST *********************************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.TaxonNameParserBotanicalNameImpl#NEW_INSTANCE()}.
	 */
	@Test
	public final void testNEW_INSTANCE() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.TaxonNameParserBotanicalNameImpl#TaxonNameParserBotanicalNameImpl()}.
	 */
	@Test
	public final void testTaxonNameParserBotanicalNameImpl() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.TaxonNameParserBotanicalNameImpl#parseSimpleName(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)}.
	 */
	@Test
	public final void testParseSimpleName() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.TaxonNameParserBotanicalNameImpl#parseSubGenericFullName(java.lang.String)}.
	 */
	@Test
	public final void testParseSubGenericFullName() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.TaxonNameParserBotanicalNameImpl#parseSubGenericSimpleName(java.lang.String)}.
	 */
	@Test
	public final void testParseSubGenericSimpleName() {
		logger.warn("Not yet implemented"); // TODO
	}

	
	@Test
	public final void functionTest() {
		String start = "^";
	    String end = "$";
	    String oWs = "\\s+"; //obligatory whitespaces
	    String fWs = "\\s*"; //facultative whitespcace

	    String capitalWord = "\\p{javaUpperCase}\\p{javaLowerCase}*";
	    String nonCapitalWord = "\\p{javaLowerCase}+";
	    
	    String capitalDotWord = capitalWord + "\\.?"; //capitalWord with facultativ '.' at the end
	    String nonCapitalDotWord = nonCapitalWord + "\\.?"; //nonCapitalWord with facultativ '.' at the end
	    String authorPart = "(" + "(D'|L'|'t\\s)?" + capitalDotWord + "('" + nonCapitalDotWord + ")?" + "|da|de(n|l|\\sla)?)" ;
	    String author = "(" + authorPart + "(" + fWs + "|-)" + ")+" + "(f.|fil.|secundus)?";
	    
	    String teamSplitter = fWs + "(&|,)" + fWs;
	    String authorTeam = fWs + "(" + author + teamSplitter + ")*" + author + "(" + teamSplitter + "al.)?" + fWs;
	    String exString = "(ex.?)";
	    String authorAndExTeam = authorTeam + "(" + oWs + exString + oWs + authorTeam + ")?";
	 
		
		String basStart = "\\(";
	    String basEnd = "\\)";
	    String basionym = basStart + authorAndExTeam + basEnd + "{1}.*";  // '(' and ')' is for evaluation with RE.paren(x)
	    //String basionym = basStart + "(" + authorAndExTeam + ")" + basEnd +  "{1}.";  // '(' and ')' is for evaluation with RE.paren(x)
	    
	    Pattern pattern = Pattern.compile(basionym);
		Matcher matcher = pattern.matcher("(Mueller)Ciard");
		assertTrue(matcher.matches());
		
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.TaxonNameParserBotanicalNameImpl#parseFullName(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)}.
	 */
	@Test
	public final void testParseFullName() {
		
			BotanicalName name1 = parser.parseFullName(strNameAbies1, Rank.SPECIES());
			assertEquals(name1.getUninomial(), "Abies");
			assertEquals(name1.getSpecificEpithet(), "alba");
			
			BotanicalName nameAuthor = parser.parseFullName(strNameAbiesAuthor1, Rank.SPECIES());
			assertEquals(nameAuthor.getUninomial(), "Abies");
			assertEquals(nameAuthor.getSpecificEpithet(), "alba");
			assertEquals(nameAuthor.getCombinationAuthorTeam().getTitleCache(), "Müller");
			
			BotanicalName nameBasionymAuthor = parser.parseFullName(strNameAbiesBasionymAuthor1, Rank.SPECIES());
			assertEquals("Abies", nameBasionymAuthor.getUninomial());
			assertEquals("alba", nameBasionymAuthor.getSpecificEpithet());
			assertEquals("D'Müller", nameBasionymAuthor.getCombinationAuthorTeam().getTitleCache());
			BotanicalName basionym = (BotanicalName)nameBasionymAuthor.getBasionym();
			assertEquals("Ciardelli", basionym.getCombinationAuthorTeam().getTitleCache());
			
			BotanicalName nameBasionymExAuthor = parser.parseFullName(strNameAbiesBasionymExAuthor1, Rank.SPECIES());
			assertEquals("Abies", nameBasionymExAuthor.getUninomial());
			assertEquals("alba", nameBasionymExAuthor.getSpecificEpithet());
			assertEquals("D'Müller", nameBasionymExAuthor.getCombinationAuthorTeam().getTitleCache());
			assertEquals("de Greuther", nameBasionymExAuthor.getExCombinationAuthorTeam().getTitleCache());
			BotanicalName basionym2 = (BotanicalName)nameBasionymExAuthor.getBasionym();
			assertEquals("Ciardelli", basionym2.getCombinationAuthorTeam().getTitleCache());
			assertEquals("Döhring", basionym2.getExCombinationAuthorTeam().getTitleCache());
			
			BotanicalName name2 = parser.parseFullName(strNameAbiesSub1, Rank.SPECIES());
			assertEquals(name2.getUninomial(), "Abies");
			assertEquals(name2.getSpecificEpithet(), "alba");
			assertEquals(name2.getInfraSpecificEpithet(), "beta");
			assertEquals(Rank.SUBSPECIES(), name2.getRank());
			
			// unparseable *********
			String problemString = "sdfjlös wer eer wer";
			BotanicalName nameProblem = parser.parseFullName(problemString, Rank.SPECIES());
			assertTrue(nameProblem.getHasProblem());
			assertEquals(problemString, nameProblem.getTitleCache());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.TaxonNameParserBotanicalNameImpl#fullTeams(java.lang.String)}.
	 */
	@Test
	public final void testFullTeams() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.TaxonNameParserBotanicalNameImpl#authorTeamAndEx(java.lang.String)}.
	 */
	@Test
	public final void testAuthorTeamAndEx() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.TaxonNameParserBotanicalNameImpl#authorTeam(java.lang.String)}.
	 */
	@Test
	public final void testAuthorTeam() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.TaxonNameParserBotanicalNameImpl#parseCultivar(java.lang.String)}.
	 */
	@Test
	public final void testParseCultivar() {
		logger.warn("Not yet implemented"); // TODO
	}

}
