/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.function.strategy;

import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

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
	final private String strNameAbiesBasionymAuthorUe = "Abies alba (Ciardelli) D'Mueller";

	private INonViralNameParser<BotanicalName> parser ;

/*************** TEST *********************************************/


	@Test
	public final void functionTest() {


		if (false){
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


	private void testRefParser(){
		int result = 0;
		//Barkerwebbia humilis (Becc.) Becc. ex Martelli


		String reference1 = "Sp. P. 2: 755. 1753., nom. inval.";
		//String reference1 = "Sp. P.";
		String fullRef1;

		fullRef1 = strNameAbiesBasionymAuthorUe + ", " + reference1;
//		result += parseIt(fullRef1);
		//Abies alba (Ciardelli) D'Mueller
//		result += parseIt( "Barkerwebbia humilis (Becc.) Becc. ex Martelli");
//		result += parseIt( "Heterospathe elegans subsp. versteegiana M.S.Trudgen & W.J.Baker");
//		result += parseIt( "Barkerwebbia elegans Becc., Webbia 16(456): 283. 1905");
//		result += parseIt( "Barkerwebbia elegans Becc. in Bot. J. 16(456): 283. 1905");
//		result += parseIt( "H. elegans (Becc.) Becc., Nova Guinea 8: 205. 1907");
//		result += parseIt( "Heterospathe elegans subsp. versteegiana M.S.Trudgen & W.J.Baker");
//		result += parseIt( "Hieracium asturicum Arv.-Touv.");
//		result += parseIt( "Hieracium mougeotii subsp. asturicum Zahn");
//		result += parseIt( "Hieracium vogesiacum subsp. asturicum (Zahn) O. Bolòs & Vigo");
//		result += parseIt( "Hieracium cantabricum Arv.-Touv.");
//		result += parseIt( "Hieracium mougeotii subsp. cantabricum (Arv.-Touv.) Zahn");
//		result += parseIt( "Hieracium murorramondii Mateo");
		result += parseIt( "Micrasterias denticulata Brébisson ex Ralfs 1848");
//		[13:11:55] Patricia Kelbert : Micrasterias denticulata var. angulosa (Hantzsch) W. et G.S. West 1902
//		[13:12:04] Patricia Kelbert : Micrasterias angulosa Hantzsch in Rabenhorst 1862

		System.out.println(result);
	}


	private int  parseIt(String fullRef1){
		NonViralName<?> nvName = TaxonNameFactory.NewZoologicalInstance(null);
		NonViralNameParserImpl parser = new NonViralNameParserImpl();

		//parser.parseFullName(strNameAbiesBasionymAuthorUe, null);
		nvName = parser.parseReferencedName(fullRef1, null, null);

		System.out.println(nvName.hasProblem());
		System.out.println("  Name: " + nvName.getTitleCache());
		System.out.println("  Reference: " + ((nvName.getNomenclaturalReference() == null) ? "-" : nvName.getNomenclaturalReference().getTitleCache()));
		System.out.println("  FullReference: " + ((nvName.getNomenclaturalReference() == null) ? "-" : nvName.getNomenclaturalReference().getNomenclaturalCitation(nvName.getNomenclaturalMicroReference())));
		return nvName.hasProblem() ? 1: 0;

	}


	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestTaxonNameParserBotanicalNameImpl test = new TestTaxonNameParserBotanicalNameImpl();
    	test.testRefParser();
	}


}
