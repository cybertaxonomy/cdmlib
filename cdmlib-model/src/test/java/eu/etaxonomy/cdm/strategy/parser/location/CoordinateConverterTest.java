/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.parser.location;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.strategy.parser.location.CoordinateConverter.ConversionResults;

/**
 * @author a.mueller
 * @since 07.06.2010
 *
 */
public class CoordinateConverterTest {
	private static final Logger logger = Logger.getLogger(CoordinateConverterTest.class);

	private CoordinateConverter coordinateConverter;

	// Unicode constants
	private static final char DEGREE_SIGN = '\u00B0';
	private static final char MASCULINE_ORDINAL_INDICATOR = '\u00BA';

	private static final char APOSTROPHE = 0x0027;
	private static final char QUOTATION_MARK = '\u0022';
	private static final char ACUTE_ACCENT = '\u00B4';
	private static final char RIGHT_SINGLE_QUOTATION_MARK = '\u2019';
	private static final char RIGHT_DOUBLE_QUOTATION_MARK = '\u201D';

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		coordinateConverter = new CoordinateConverter();
	}

// ************************ TESTS ********************************************** /

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.location.CoordinateConverter#CoordinateConverter()}.
	 */
	@Test
	public void testCoordinateConverter() {
		Assert.assertNotNull("converter should not be null",coordinateConverter);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.location.CoordinateConverter#tryConvert(java.lang.String)}.
	 */
	@Test
	public void testTryConvert() {
		ConversionResults conversionResults = coordinateConverter.tryConvert("35" + DEGREE_SIGN + "34" + APOSTROPHE + "20" + QUOTATION_MARK + "S");
		Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
		Assert.assertTrue("Southern must be negative", conversionResults.convertedCoord < 0);
		Assert.assertFalse("Southern must be latitude", conversionResults.isLongitude);

		conversionResults = coordinateConverter.tryConvert("35" + MASCULINE_ORDINAL_INDICATOR + "34.744");
		Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
		Assert.assertNull("Longitude must be undefined", conversionResults.isLongitude);

		conversionResults = coordinateConverter.tryConvert("95" + DEGREE_SIGN + "34.744");
		Assert.assertTrue("Longitude must be defined", conversionResults.isLongitude);


		conversionResults = coordinateConverter.tryConvert("-35" + DEGREE_SIGN + "34" + APOSTROPHE + "55.67S");
		Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);

		conversionResults = coordinateConverter.tryConvert("35" + DEGREE_SIGN + "11" + APOSTROPHE + "34.744SN");
		Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);

		conversionResults = coordinateConverter.tryConvert("35" + DEGREE_SIGN + "11" + APOSTROPHE + "34.744SW");
		Assert.assertTrue("Western must be longitude", conversionResults.isLongitude);

		conversionResults = coordinateConverter.tryConvert("35" + DEGREE_SIGN + " 1" + APOSTROPHE + "34.744SW");
		Assert.assertTrue("Pattern with whitespace must be recognised", conversionResults.patternRecognised);
		Assert.assertTrue("Pattern with whitespace must be recognised", conversionResults.conversionSuccessful);

		conversionResults = coordinateConverter.tryConvert("35D11M34.744S");
		Assert.assertNull("isLongitude must be undefined. S stands for second.", conversionResults.isLongitude);

        conversionResults = coordinateConverter.tryConvert("35" + DEGREE_SIGN + " 1" + ACUTE_ACCENT + "34.744SW");
        Assert.assertTrue("Pattern with acute accent must be recognised", conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with acute accent must be recognised", conversionResults.conversionSuccessful);

        conversionResults = coordinateConverter.tryConvert("35" + DEGREE_SIGN + "1" + ACUTE_ACCENT + "34" + ACUTE_ACCENT + "" + ACUTE_ACCENT + "W");
        Assert.assertTrue("Pattern with acute accent for seconds must be recognised", conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with acute accent for seconds  must be recognised", conversionResults.conversionSuccessful);


	}

	//#5554
	@Test
    public void testApostrophs() {

	    //4º 58’ N, 118º 10’ E
	    //minutes
	    ConversionResults conversionResults = coordinateConverter.tryConvert("4" + DEGREE_SIGN + "58" + RIGHT_SINGLE_QUOTATION_MARK + "N");
	    Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
	    Assert.assertTrue("Pattern with english quotation end for minute must be recognised", conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with english quotation end for minute  must be successful", conversionResults.conversionSuccessful);

        conversionResults = coordinateConverter.tryConvert("4" + DEGREE_SIGN + "58" + RIGHT_SINGLE_QUOTATION_MARK + " N");
        Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with english quotation and whitespace must be recognised", conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with english quotation and whitespace must be successful", conversionResults.conversionSuccessful);

        conversionResults = coordinateConverter.tryConvert("4" + DEGREE_SIGN + " 58" + RIGHT_SINGLE_QUOTATION_MARK + "44\" N");
        Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with english quotation and whitespace must be recognised", conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with english quotation and whitespace must be successful", conversionResults.conversionSuccessful);

        conversionResults = coordinateConverter.tryConvert("118" + DEGREE_SIGN + " 10" + RIGHT_SINGLE_QUOTATION_MARK + "33\" E");
        Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with english quotation and whitespace must be recognised", conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with english quotation and whitespace must be successful", conversionResults.conversionSuccessful);

        //seconds
        conversionResults = coordinateConverter.tryConvert("4" + DEGREE_SIGN + "58" + RIGHT_SINGLE_QUOTATION_MARK + "44" + RIGHT_DOUBLE_QUOTATION_MARK + "N");
        Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with right double quotation for second must be recognised", conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with right double quotation for second must be successful", conversionResults.conversionSuccessful);

        conversionResults = coordinateConverter.tryConvert("4" + DEGREE_SIGN + "58" + RIGHT_SINGLE_QUOTATION_MARK + "44" + RIGHT_DOUBLE_QUOTATION_MARK + " N");
        Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with right double quotation and whitespace must be recognised", conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with right double quotation and whitespace must be successful", conversionResults.conversionSuccessful);

        conversionResults = coordinateConverter.tryConvert("118" + DEGREE_SIGN + " 10" + RIGHT_SINGLE_QUOTATION_MARK + "33" + RIGHT_DOUBLE_QUOTATION_MARK + " E");
        Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with right double quotation and whitespace must be recognised", conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with right double quotation and whitespace must be successful", conversionResults.conversionSuccessful);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.location.CoordinateConverter#addCustomPattern(eu.etaxonomy.cdm.strategy.parser.location.CoordinateConverter.CustomPatternIn)}.
	 */
	@Test
	public void testAddCustomPattern() {
		logger.warn("testAddCustomPattern not yet implemented"); // TODO
	}

}
