/**
 * 
 */
package eu.etaxonomy.cdm.test.function.strategy;

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
import eu.etaxonomy.cdm.strategy.ITaxonNameParser;
import eu.etaxonomy.cdm.strategy.TaxonNameParserBotanicalNameImpl;
import eu.etaxonomy.cdm.strategy.exceptions.StringNotParsableException;

/**
 * @author a.mueller
 *
 */
public class TestTaxonNameParserBotanicalNameImpl {
	private static final Logger logger = Logger.getLogger(TestTaxonNameParserBotanicalNameImpl.class);
	
	final private String strNameFamily = "Asteraceae";
	final private String strNameGenus = "Abies Müller";
	final private String strNameAbies1 = "Abies alba";
	final private String strNameAbiesSub1 = "Abies alba subsp. beta";
	final private String strNameAbiesAuthor1 = "Abies alba Müller";
	final private String strNameAbiesBasionymAuthor1 = "Abies alba (Ciardelli) D'Müller";
	final private String strNameAbiesBasionymExAuthor1 ="Abies alba (Ciardelli ex Döhring) D'Müller ex. de Greuther"; 
	
	private ITaxonNameParser<BotanicalName> parser ;

/*************** TEST *********************************************/

	
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
	

}
