// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.facade;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.api.facade.CoordinateConverter.ConversionResults;

/**
 * @author a.mueller
 * @date 07.06.2010
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
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.CoordinateConverter#CoordinateConverter()}.
	 */
	@Test
	public void testCoordinateConverter() {
		Assert.assertNotNull("converter should not be null",coordinateConverter);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.CoordinateConverter#tryConvert(java.lang.String)}.
	 */
	@Test
	public void testTryConvert() {
		ConversionResults conversionResults = coordinateConverter.tryConvert("35°34'20\"S");
		Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
		Assert.assertTrue("Southern must be negative", conversionResults.convertedCoord < 0);
		Assert.assertFalse("Southern must be latitude", conversionResults.isLongitude);

		conversionResults = coordinateConverter.tryConvert("35°34.744");
		Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
		Assert.assertNull("Longitude must be undefined", conversionResults.isLongitude);

		conversionResults = coordinateConverter.tryConvert("95°34.744");
		Assert.assertTrue("Longitude must be defined", conversionResults.isLongitude);

		
		conversionResults = coordinateConverter.tryConvert("-35°34'55.67S");
		Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);

		conversionResults = coordinateConverter.tryConvert("35°11'34.744SN");
		Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);

		conversionResults = coordinateConverter.tryConvert("35°11'34.744SW");
		Assert.assertTrue("Western must be longitude", conversionResults.isLongitude);
		
		conversionResults = coordinateConverter.tryConvert("35D11M34.744S");
		Assert.assertNull("isLongitude must be undefined. S stands for second.", conversionResults.isLongitude);
		
		
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.facade.CoordinateConverter#addCustomPattern(eu.etaxonomy.cdm.api.facade.CoordinateConverter.CustomPatternIn)}.
	 */
	@Test
	public void testAddCustomPattern() {
		logger.warn("testAddCustomPattern not yet implemented"); // TODO
	}

}
