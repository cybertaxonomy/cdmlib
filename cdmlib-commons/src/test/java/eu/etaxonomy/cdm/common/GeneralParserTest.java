/**
 * 
 */
package eu.etaxonomy.cdm.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author a.mueller
 *
 */
public class GeneralParserTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		String isbn1 = "ISBN 90-71236-61-7";
		Assert.assertTrue("isbn1 should be an ISBN", GeneralParser.isIsbn(isbn1));
		
		String isbn2 = "90-71236-61-7";
		Assert.assertTrue("'ISBN' string should not be required", GeneralParser.isIsbn(isbn2));
		
		String isbnToShort = "ISBN 90-7123-61-7";
		Assert.assertFalse("ISBN must have 10 or 13 numbers, 9 numbered string should not be an ISBN", GeneralParser.isIsbn(isbnToShort));
		
		String isbn3 = "ISBN 123-456-789-112-3";
		Assert.assertTrue("isbn3 (with 13 numbers) should be an ISBN", GeneralParser.isIsbn(isbn3));
		
		String isbn4 = "ISBN 123-456-789-112-3-";
		Assert.assertFalse("- at the end of ISBN is not allowed", GeneralParser.isIsbn(isbn4));

		String isbn5 = "ISBN 123-456-789-12-3";
		Assert.assertFalse("12 numbers are not allowed, either 10 or 13", GeneralParser.isIsbn(isbn5));
		
		String isbn6 = "ISBN 123-456-789-112-X";
		Assert.assertTrue("X should be allowed as a final digit", GeneralParser.isIsbn(isbn6));

	}

}
