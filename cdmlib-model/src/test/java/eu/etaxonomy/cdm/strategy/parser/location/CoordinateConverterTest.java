/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.parser.location;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.strategy.parser.location.CoordinateConverter.ConversionResults;

/**
 * @author a.mueller
 * @since 07.06.2010
 */
public class CoordinateConverterTest {

    private static final Logger logger = LogManager.getLogger();

	private CoordinateConverter coordinateConverter;

	// Unicode constants
	private static final String DEGREE_SIGN = UTF8.DEGREE_SIGN.toString();
	private static final char MASCULINE_ORDINAL_INDICATOR = '\u00BA';

	private static final String APOSTROPHE = "'";
	private static final String QUOTATION_MARK = "\"";
	private static final String ACUTE_ACCENT = UTF8.ACUTE_ACCENT.toString();
	private static final String RIGHT_SINGLE_QUOTATION_MARK = UTF8.QUOT_SINGLE_RIGHT.toString();
	private static final String RIGHT_DOUBLE_QUOTATION_MARK = UTF8.QUOT_DBL_RIGHT.toString();

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
	}

	@BeforeEach
	public void setUp() throws Exception {
		coordinateConverter = new CoordinateConverter();
	}

// ************************ TESTS ********************************************** /

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.location.CoordinateConverter#CoordinateConverter()}.
	 */
	@Test
	public void testCoordinateConverter() {
		assertNotNull(coordinateConverter, "converter should not be null");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.location.CoordinateConverter#tryConvert(java.lang.String)}.
	 */
	@Test
	public void testTryConvert() {
		ConversionResults conversionResults = coordinateConverter.tryConvert("35" + DEGREE_SIGN + "34" + APOSTROPHE + "20" + QUOTATION_MARK + "S");
		assertTrue(conversionResults.patternRecognised, conversionResults.conversionComments);
		assertTrue(conversionResults.convertedCoord < 0, "Southern must be negative");
		assertFalse(conversionResults.isLongitude, "Southern must be latitude");

		conversionResults = coordinateConverter.tryConvert("35" + MASCULINE_ORDINAL_INDICATOR + "34.744");
		assertTrue(conversionResults.patternRecognised, conversionResults.conversionComments);
		assertNull(conversionResults.isLongitude, "Longitude must be undefined");

		conversionResults = coordinateConverter.tryConvert("95" + DEGREE_SIGN + "34.744");
		assertTrue(conversionResults.isLongitude, "Longitude must be defined");


		conversionResults = coordinateConverter.tryConvert("-35" + DEGREE_SIGN + "34" + APOSTROPHE + "55.67S");
		assertTrue(conversionResults.patternRecognised, conversionResults.conversionComments);

		conversionResults = coordinateConverter.tryConvert("35" + DEGREE_SIGN + "11" + APOSTROPHE + "34.744SN");
		assertTrue(conversionResults.patternRecognised, conversionResults.conversionComments);

		conversionResults = coordinateConverter.tryConvert("35" + DEGREE_SIGN + "11" + APOSTROPHE + "34.744SW");
		assertTrue(conversionResults.isLongitude, "Western must be longitude");

		conversionResults = coordinateConverter.tryConvert("35" + DEGREE_SIGN + " 1" + APOSTROPHE + "34.744SW");
		assertTrue(conversionResults.patternRecognised, "Pattern with whitespace must be recognised");
		assertTrue(conversionResults.conversionSuccessful, "Pattern with whitespace must be recognised");

		conversionResults = coordinateConverter.tryConvert("35D11M34.744S");
		assertNull(conversionResults.isLongitude, "isLongitude must be undefined. S stands for second.");

        conversionResults = coordinateConverter.tryConvert("35" + DEGREE_SIGN + " 1" + ACUTE_ACCENT + "34.744SW");
        assertTrue(conversionResults.patternRecognised, "Pattern with acute accent must be recognised");
        assertTrue(conversionResults.conversionSuccessful, "Pattern with acute accent must be recognised");

        conversionResults = coordinateConverter.tryConvert("35" + DEGREE_SIGN + "1" + ACUTE_ACCENT + "34" + ACUTE_ACCENT + ACUTE_ACCENT + "W");
        assertTrue(conversionResults.patternRecognised, "Pattern with acute accent for seconds must be recognised");
        assertTrue(conversionResults.conversionSuccessful, "Pattern with acute accent for seconds  must be recognised");


	}

	//#5554
	@Test
    public void testApostrophs() {

	    //4º 58’ N, 118º 10’ E
	    //minutes
	    ConversionResults conversionResults = coordinateConverter.tryConvert("4" + DEGREE_SIGN + "58" + RIGHT_SINGLE_QUOTATION_MARK + "N");
	    assertTrue(conversionResults.patternRecognised, conversionResults.conversionComments);
	    assertTrue(conversionResults.patternRecognised, "Pattern with english quotation end for minute must be recognised");
        assertTrue(conversionResults.conversionSuccessful, "Pattern with english quotation end for minute  must be successful");

        conversionResults = coordinateConverter.tryConvert("4" + DEGREE_SIGN + "58" + RIGHT_SINGLE_QUOTATION_MARK + " N");
        assertTrue(conversionResults.patternRecognised, conversionResults.conversionComments);
        assertTrue(conversionResults.patternRecognised, "Pattern with english quotation and whitespace must be recognised");
        assertTrue(conversionResults.conversionSuccessful, "Pattern with english quotation and whitespace must be successful");

        conversionResults = coordinateConverter.tryConvert("4" + DEGREE_SIGN + " 58" + RIGHT_SINGLE_QUOTATION_MARK + "44\" N");
        assertTrue(conversionResults.patternRecognised, conversionResults.conversionComments);
        assertTrue(conversionResults.patternRecognised, "Pattern with english quotation and whitespace must be recognised");
        assertTrue(conversionResults.conversionSuccessful, "Pattern with english quotation and whitespace must be successful");

        conversionResults = coordinateConverter.tryConvert("118" + DEGREE_SIGN + " 10" + RIGHT_SINGLE_QUOTATION_MARK + "33\" E");
        assertTrue(conversionResults.patternRecognised, conversionResults.conversionComments);
        assertTrue(conversionResults.patternRecognised, "Pattern with english quotation and whitespace must be recognised");
        assertTrue(conversionResults.conversionSuccessful, "Pattern with english quotation and whitespace must be successful");

        //seconds
        conversionResults = coordinateConverter.tryConvert("4" + DEGREE_SIGN + "58" + RIGHT_SINGLE_QUOTATION_MARK + "44" + RIGHT_DOUBLE_QUOTATION_MARK + "N");
        assertTrue(conversionResults.patternRecognised, conversionResults.conversionComments);
        assertTrue(conversionResults.patternRecognised, "Pattern with right double quotation for second must be recognised");
        assertTrue(conversionResults.conversionSuccessful, "Pattern with right double quotation for second must be successful");

        conversionResults = coordinateConverter.tryConvert("4" + DEGREE_SIGN + "58" + RIGHT_SINGLE_QUOTATION_MARK + "44" + RIGHT_DOUBLE_QUOTATION_MARK + " N");
        assertTrue(conversionResults.patternRecognised, conversionResults.conversionComments);
        assertTrue(conversionResults.patternRecognised, "Pattern with right double quotation and whitespace must be recognised");
        assertTrue(conversionResults.conversionSuccessful, "Pattern with right double quotation and whitespace must be successful");

        conversionResults = coordinateConverter.tryConvert("118" + DEGREE_SIGN + " 10" + RIGHT_SINGLE_QUOTATION_MARK + "33" + RIGHT_DOUBLE_QUOTATION_MARK + " E");
        assertTrue(conversionResults.patternRecognised, conversionResults.conversionComments);
        assertTrue(conversionResults.patternRecognised, "Pattern with right double quotation and whitespace must be recognised");
        assertTrue(conversionResults.conversionSuccessful, "Pattern with right double quotation and whitespace must be successful");
	}

	@Test
    public void testIssue7217() {
	    // 49°13'39.0''N
	    ConversionResults conversionResults = coordinateConverter.tryConvert("49" + DEGREE_SIGN + "13" + APOSTROPHE +"39.0" + APOSTROPHE + APOSTROPHE + "N");
        assertTrue(conversionResults.patternRecognised, conversionResults.conversionComments);
        assertTrue(conversionResults.patternRecognised, "Pattern with APOSTROPHE for minute must be recognised");
        assertTrue(conversionResults.conversionSuccessful, "Pattern with APOSTROPHE end for minute  must be successful");

        // 20°00'31.5''E
        conversionResults = coordinateConverter.tryConvert("20" + DEGREE_SIGN + "00" + APOSTROPHE +"31.5" + APOSTROPHE + APOSTROPHE + "E");
        assertTrue(conversionResults.patternRecognised, conversionResults.conversionComments);
        assertTrue(conversionResults.patternRecognised, "Pattern with APOSTROPHE for minute and second must be recognised");
        assertTrue(conversionResults.conversionSuccessful, "Pattern with APOSTROPHE for minute and second must be successful");
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.location.CoordinateConverter#addCustomPattern(eu.etaxonomy.cdm.strategy.parser.location.CoordinateConverter.CustomPatternIn)}.
	 */
	@Test
	public void testAddCustomPattern() {
		logger.warn("testAddCustomPattern not yet implemented"); // TODO
	}

}
