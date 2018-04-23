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
 \* @since 07.06.2010
 *
 */
public class CoordinateConverterTest {
	private static final Logger logger = Logger.getLogger(CoordinateConverterTest.class);

	private CoordinateConverter coordinateConverter;

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
		ConversionResults conversionResults = coordinateConverter.tryConvert("35\u00B034'20\"S");
		Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
		Assert.assertTrue("Southern must be negative", conversionResults.convertedCoord < 0);
		Assert.assertFalse("Southern must be latitude", conversionResults.isLongitude);

		conversionResults = coordinateConverter.tryConvert("35\u00BA34.744");
		Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
		Assert.assertNull("Longitude must be undefined", conversionResults.isLongitude);

		conversionResults = coordinateConverter.tryConvert("95\u00B034.744");
		Assert.assertTrue("Longitude must be defined", conversionResults.isLongitude);


		conversionResults = coordinateConverter.tryConvert("-35\u00B034'55.67S");
		Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);

		conversionResults = coordinateConverter.tryConvert("35\u00B011'34.744SN");
		Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);

		conversionResults = coordinateConverter.tryConvert("35\u00B011'34.744SW");
		Assert.assertTrue("Western must be longitude", conversionResults.isLongitude);

		conversionResults = coordinateConverter.tryConvert("35\u00B0 1'34.744SW");
		Assert.assertTrue("Pattern with whitespace must be recognised", conversionResults.patternRecognised);
		Assert.assertTrue("Pattern with whitespace must be recognised", conversionResults.conversionSuccessful);

		conversionResults = coordinateConverter.tryConvert("35D11M34.744S");
		Assert.assertNull("isLongitude must be undefined. S stands for second.", conversionResults.isLongitude);

        conversionResults = coordinateConverter.tryConvert("35\u00B0 1\u00B434.744SW");
        Assert.assertTrue("Pattern with acute accent must be recognised", conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with acute accent must be recognised", conversionResults.conversionSuccessful);

        conversionResults = coordinateConverter.tryConvert("35\u00B01\u00B434\u00B4\u00B4W");
        Assert.assertTrue("Pattern with acute accent for seconds must be recognised", conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with acute accent for seconds  must be recognised", conversionResults.conversionSuccessful);


	}

	//#5554
	@Test
    public void testApostrophs() {

	    //4º 58’ N, 118º 10’ E
	    //minutes
	    ConversionResults conversionResults = coordinateConverter.tryConvert("4\u00B058\u2019N");
	    Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
	    Assert.assertTrue("Pattern with english quotation end for minute must be recognised", conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with english quotation end for minute  must be successful", conversionResults.conversionSuccessful);

        conversionResults = coordinateConverter.tryConvert("4\u00B058\u2019 N");
        Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with english quotation and whitespace must be recognised", conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with english quotation and whitespace must be successful", conversionResults.conversionSuccessful);

        conversionResults = coordinateConverter.tryConvert("4\u00B0 58\u201944\" N");
        Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with english quotation and whitespace must be recognised", conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with english quotation and whitespace must be successful", conversionResults.conversionSuccessful);

        conversionResults = coordinateConverter.tryConvert("118\u00B0 10\u201933\" E");
        Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with english quotation and whitespace must be recognised", conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with english quotation and whitespace must be successful", conversionResults.conversionSuccessful);

        //seconds
        conversionResults = coordinateConverter.tryConvert("4\u00B058\u201944\u201DN");
        Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with right double quotation for second must be recognised", conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with right double quotation for second must be successful", conversionResults.conversionSuccessful);

        conversionResults = coordinateConverter.tryConvert("4\u00B058\u201944\u201D N");
        Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with right double quotation and whitespace must be recognised", conversionResults.patternRecognised);
        Assert.assertTrue("Pattern with right double quotation and whitespace must be successful", conversionResults.conversionSuccessful);

        conversionResults = coordinateConverter.tryConvert("118\u00B0 10\u201933\u201D E");
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
