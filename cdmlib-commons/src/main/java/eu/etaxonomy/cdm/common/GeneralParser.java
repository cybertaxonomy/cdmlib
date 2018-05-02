/**
 * 
 */
package eu.etaxonomy.cdm.common;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @since 2013-Aug-02
 *
 */
public class GeneralParser {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GeneralParser.class);
	
	static String isbnPatternStr = "^(ISBN\\s*)?(\\d-?){9}((\\d-?){3})?(\\d|X)$";
	static Pattern isbnPattern;
	
	/**
	 * Checks if a string follows the ISBN pattern (10 and 13 digits). It does not check the 
	 * checksum of an isbn (http://en.wikipedia.org/wiki/International_Standard_Book_Number#ISBN-13_check_digit_calculation).
	 * We may implement this separately as sometimes additional checksum validation is unwanted.
	 * @param isbn
	 * @return
	 */
	public static boolean isIsbn(String isbn){
//		if (isbnPattern == null){
//			isbnPattern = Pattern.compile(isbnPatternStr);
//		}
//		Matcher matcher = isbnPattern.matcher(isbn);
		if (isbn == null){
			return false;
		}else{
			return isbn.matches(isbnPatternStr);
		}
		
		
		
	}
	
}
