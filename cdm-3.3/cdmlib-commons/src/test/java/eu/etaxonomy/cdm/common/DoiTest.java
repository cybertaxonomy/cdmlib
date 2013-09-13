/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * Test class for testing the {@link DOI} class.
 * 
 * For doi syntax see also http://www.doi.org/doi_handbook/2_Numbering.html
 * or
 * http://stackoverflow.com/questions/27910/finding-a-doi-in-a-document-or-page  
 * 
 * @author a.mueller
 *
 */
public class DoiTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testValidParser() {
		String validDoi = "10.1002/1234";
		DOI doi = DOI.fromString(validDoi);
		Assert.assertEquals("10.1002", doi.getPrefix());
		Assert.assertEquals("1234", doi.getSuffix());
		
		validDoi = "10.1002/(SICI)1522-2594(199911)42:5<952::AID-MRM16>3.0.CO;2-S";
		doi = DOI.fromString(validDoi);
		Assert.assertEquals("10.1002", doi.getPrefix());
		Assert.assertEquals("(SICI)1522-2594(199911)42:5<952::AID-MRM16>3.0.CO;2-S", doi.getSuffix());
		
		validDoi = "10.1007.10/978-3-642-28108-2_19";
		doi = DOI.fromString(validDoi);
		Assert.assertEquals("10.1007.10", doi.getPrefix());
		Assert.assertEquals("978-3-642-28108-2_19", doi.getSuffix());
		
		validDoi="10.1579/0044-7447(2006)35\\[89:RDUICP\\]2.0.CO;2";
		doi = DOI.fromString(validDoi);
		Assert.assertEquals("10.1579", doi.getPrefix());
		Assert.assertEquals("0044-7447(2006)35\\[89:RDUICP\\]2.0.CO;2", doi.getSuffix());

	}
	
	@Test
	public void testFromRegistrantCodeAndSuffix() {
		DOI doi = DOI.fromRegistrantCodeAndSuffix("1579", "978-3-642-28108-2_19");
		Assert.assertEquals("10.1579", doi.getPrefix());
		Assert.assertEquals("978-3-642-28108-2_19", doi.getSuffix());
		Assert.assertNotEquals("1234", doi.getSuffix());
	}
	
	@Test
	public void testParserFail() {
		String invalidDoi = "10.4515260,51.1656910";  //must never match to avoid matches with geo coordinates
		testInvalid(invalidDoi);
		invalidDoi = "4210.1000/123456";  //directoryIndicator must always be 10
		testInvalid(invalidDoi);
		invalidDoi = "10.1002/12\u0004345";  //control characters (here U+0004) must fail
		testInvalid(invalidDoi);
		invalidDoi = "10.1a02/12345";  //registrant code must include only number and dots (to separate sub codes)
		testInvalid(invalidDoi);
		invalidDoi = "10.1002:12345";  //column separator is only allowed (+required) in URNs
		testInvalid(invalidDoi);
		invalidDoi = "10.1002/";  //doi must always have a suffix length > 0 (if this should changed in future, please do adapt equals and hashCode)
		testInvalid(invalidDoi);
		invalidDoi = "10./1234";  //doi must always have a registrant prefix length > 0 (if this should changed in future, please do adapt equals and hashCode)
		testInvalid(invalidDoi);
	}

	@Test
	public void testParserWithPrefixes() {
		String validDoi = "DOI: 10.1002/1234";
		DOI doi = DOI.fromString(validDoi);
		Assert.assertEquals("10.1002", doi.getPrefix());
		Assert.assertEquals("1234", doi.getSuffix());
		
		validDoi = "http://doi.org/10.1002/1234";
		doi = DOI.fromString(validDoi);
		Assert.assertEquals("10.1002", doi.getPrefix());
		Assert.assertEquals("1234", doi.getSuffix());
	
		
		validDoi = "http://doi.org/urn:doi:10.123:456ABC%2Fzyz";
		doi = DOI.fromString(validDoi);
		Assert.assertEquals("10.123", doi.getPrefix());
		Assert.assertEquals("456ABC/zyz", doi.getSuffix());  //urn must be percentage encoded ( / -> %2F)
		
	}
	
	@Test
	public void testEquals() {
		String validDoi = "10.1002/12a4";
		DOI doi1 = DOI.fromString(validDoi);
		validDoi = "10.1002/12A4";
		DOI doi2 = DOI.fromString(validDoi);
		Assert.assertEquals("DOIs must be equal case insensitive", doi1, doi2);
		validDoi = "10.1002/12b4";
		DOI doi3 = DOI.fromString(validDoi);
		Assert.assertNotEquals("Different DOIs must not be equal", doi1, doi3);
	}
	
	@Test
	public void testAsURI() {
		//mandatory encoding according to http://www.doi.org/doi_handbook/2_Numbering.html#2.5.2.4
		String validDoi = "10.1002/1234%56\"78#90 12?34";
		DOI doi1 = DOI.fromString(validDoi);
		String uri = doi1.asURI();
		Assert.assertEquals(DOI.HTTP_DOI_ORG + "10.1002/1234%2556%2278%2390%2012%3f34", uri);
		
		//recommendedEncoding
		validDoi = "10.1002/1234<56>78{90}12^34";
		doi1 = DOI.fromString(validDoi);
		uri = doi1.asURI();
		Assert.assertEquals(DOI.HTTP_DOI_ORG + "10.1002/1234%3c56%3e78%7b90%7d12%5e34", uri);
		
		//recommendedEncoding (cont.)
		validDoi = "10.1002/1234[56]78`90|12\\34+56";
		doi1 = DOI.fromString(validDoi);
		uri = doi1.asURI();
		Assert.assertEquals(DOI.HTTP_DOI_ORG + "10.1002/1234%5b56%5d78%6090%7c12%5c34%2b56", uri);
		
	}

	

	
	private void testInvalid(String invalidDoi) {
		try {
			DOI.fromString(invalidDoi);
			Assert.fail("DOI should not be parsable: " + invalidDoi);
		} catch (IllegalArgumentException e) {
			//OK
		}
	}		

}
