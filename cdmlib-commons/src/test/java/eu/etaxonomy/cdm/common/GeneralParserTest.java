/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author a.mueller
 */
public class GeneralParserTest {

	@BeforeEach
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		String isbn1 = "ISBN 90-71236-61-7";
		assertTrue(GeneralParser.isIsbn(isbn1), "isbn1 should be an ISBN");

		String isbn2 = "90-71236-61-7";
		assertTrue(GeneralParser.isIsbn(isbn2), "'ISBN' string should not be required");

		String isbnToShort = "ISBN 90-7123-61-7";
		assertFalse(GeneralParser.isIsbn(isbnToShort), "ISBN must have 10 or 13 numbers, 9 numbered string should not be an ISBN");

		String isbn3 = "ISBN 123-456-789-112-3";
		assertTrue(GeneralParser.isIsbn(isbn3), "isbn3 (with 13 numbers) should be an ISBN");

		String isbn4 = "ISBN 123-456-789-112-3-";
		assertFalse(GeneralParser.isIsbn(isbn4), "- at the end of ISBN is not allowed");

		String isbn5 = "ISBN 123-456-789-12-3";
		assertFalse(GeneralParser.isIsbn(isbn5), "12 numbers are not allowed, either 10 or 13");

		String isbn6 = "ISBN 123-456-789-112-X";
		assertTrue(GeneralParser.isIsbn(isbn6), "X should be allowed as a final digit");

	}

}
