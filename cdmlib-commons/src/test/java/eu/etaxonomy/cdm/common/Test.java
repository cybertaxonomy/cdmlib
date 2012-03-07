
package eu.etaxonomy.cdm.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.junit.Ignore;
@Ignore
public class Test {
	private static final Logger logger = Logger.getLogger(Test.class);
	
	
		public static final String EXAMPLE_TEST = "auct. nec. Fabricius";

		public static void main(String[] args) {
			String auctWithNecRegEx = "\\bauct\\b\\.?.*\\bnec\\b\\.?.*";
			System.out.println(EXAMPLE_TEST.matches(auctWithNecRegEx));
			String[] splitString = (EXAMPLE_TEST.split("\\s+"));
			System.out.println(splitString.length);// Should be 14
			for (String string : splitString) {
				System.out.println(string);
			}
			// Replace all whitespace with tabs
			System.out.println(EXAMPLE_TEST.replaceAll("\\s+", "\t"));
		}
		
		boolean matches(String regEx, String targetString){
			Matcher matcher = createMatcher(regEx, targetString);
			if (matcher == null) return false;
			if (matcher.find()) {
				return true;
			} else {
				return false;
			}
			
		}

		private Matcher createMatcher(String regEx, String targetString) {
			if (targetString == null) {
				return null;
			}
			Pattern pattern = Pattern.compile(regEx);
			Matcher matcher = pattern.matcher(targetString);
			return matcher;
		}
	
}
