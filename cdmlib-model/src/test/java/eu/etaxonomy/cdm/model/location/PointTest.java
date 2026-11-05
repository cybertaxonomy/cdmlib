/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.ParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.location.Point.Direction;
import eu.etaxonomy.cdm.model.location.Point.Sexagesimal;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 * @since 04.06.2010
 */
public class PointTest extends EntityTestBase {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private Point point1;
	private Point point2;

	private Integer errorRadius;
	private Double longitude1;
	private Double latitude1;
	private Double longitude2;
	private Double latitude2;

	private ReferenceSystem referenceSystem;

	@BeforeEach
	public void setUp() throws Exception {
		longitude1 = 23.123556;
		latitude1 = -13.975556;

		longitude2 = 28.48556;
		latitude2 = 12.656;

		errorRadius = 5;
		referenceSystem = ReferenceSystem.GOOGLE_EARTH();

		point1 = Point.NewInstance(longitude1, latitude1, referenceSystem, errorRadius);
		point2 = Point.NewInstance();
	}

//********************** TESTS *****************************

	@Test
	public void testNewInstance(){
		assertNotNull(referenceSystem, "ReferenceSystem must not be null");
		assertNotNull(point1, "Point1 must not be null");
		assertNotNull(point2, "Point2 must not be null");
		assertEquals(longitude1, point1.getLongitude(), "");

		assertEquals(latitude1, point1.getLatitude(), "");
		assertEquals(errorRadius, point1.getErrorRadius(), "");
		assertEquals(referenceSystem, point1.getReferenceSystem(), "");

		assertNull(point2.getLongitudeSexagesimal(), "LongitudeSexagesimal should be null");
		assertNull(point2.getLatitudeSexagesimal(), "LatitudeSexagesimal should be null");
	}

	@Test
	public void testGetSetLongitude(){
		point2.setLongitude(5.888);
		assertEquals(Double.valueOf(5.888), point2.getLongitude());
		point2.setLongitude(null);
		assertEquals(null, point2.getLongitude());
	}

	@Test
	public void testGetSetLatitude(){
		point2.setLatitude(-34.987);
		assertEquals(Double.valueOf(-34.987), point2.getLatitude());
		point2.setLatitude(null);
		assertEquals(null, point2.getLatitude());
	}

	@Test
	public void testGetSetErrorRadius(){
		point2.setErrorRadius(7);
		assertEquals(Integer.valueOf(7), point2.getErrorRadius());
		point2.setErrorRadius(null);
		assertEquals(null, point2.getErrorRadius());
	}

	@Test
	public void testGetSetReferenceSystem(){
		ReferenceSystem newRefSystem = ReferenceSystem.NewInstance();
		point2.setReferenceSystem(newRefSystem);
		assertEquals(newRefSystem, point2.getReferenceSystem());
		point2.setReferenceSystem(null);
		assertEquals(null, point2.getReferenceSystem());
	}

	@Test
	public void testGetLongitudeSexagesimal(){
		assertEquals("23\u00B07'24.801\"E", point1.getLongitudeSexagesimal().toString(true, false));


		point2.setLongitudeSexagesimal(Sexagesimal.NewInstance(5, 22, null, Direction.WEST));
		assertEquals((Integer)22, point2.getLongitudeSexagesimal().minutes);
		assertEquals((Integer)0, point2.getLongitudeSexagesimal().seconds);

		Double latitudeDouble = -45.57389326;
		point1.setLatitudeSexagesimal(Sexagesimal.valueOf(latitudeDouble, true));
		//Not true because of rounding errors
//		assertEquals(latitudeDouble, point1.getLatitude(), "latitudeDouble must be equal");

		Sexagesimal sexagesimal1 = Sexagesimal.NewInstance(0, 0, 0, Direction.WEST);
		Sexagesimal sexagesimal2 = Sexagesimal.NewInstance(2, 2, 2, Direction.WEST);
		assertNotSame(sexagesimal1, sexagesimal2, "");
	}

	@Test
	public void testParsing(){
		try {
			assertEquals(longitude1, point1.getLongitude(), "");
			assertTrue(latitude1.equals(point1.getLatitude()), "");
			point1.setLatitudeByParsing("35\u00B034'20\"S");
			assertEquals(longitude1, point1.getLongitude(), "");
			assertFalse(latitude1.equals(point1.getLatitude()), "");
			assertEquals(Double.valueOf("-35.57222222222222"), point1.getLatitude(), "");
		} catch (ParseException e) {
			fail("No parsing error should occur");
		}
		try {
			point1.setLongitudeByParsing("112\u00B034.34'N");
			assertEquals("112.57233", point1.getLongitude().toString().substring(0,9), "");
		} catch (ParseException e) {
			fail("No parsing error should occur");
		}
		try {
			point1.setLatitudeByParsing("112\u00B034.34'S");
			fail("Latitude can not be > 90");
		} catch (ParseException e) {
			assertTrue(true, "Latitude can not be > 90");
		}
		try {
			point1.setLongitudeByParsing("45\u00B034.34'S");
			fail("Longitude can not be S");
		} catch (ParseException e) {
			assertTrue(true, "Longitude can not be S");
		}
		//#2962 (rounding of tertiers)
		try {
			point1.setLatitudeByParsing("37\u00B07'44\"N");
			assertEquals("37\u00B07'44\"N", point1.getLatitudeSexagesimal().toString(), "Result should be 37\u00B07'44\"N not 37\u00B07'44.999\"N");

			point1.setLatitudeByParsing("37\u00B07'45\"N");
			assertEquals("37\u00B07'45\"N", point1.getLatitudeSexagesimal().toString(), "Result should be 37\u00B07'45\"N not 37\u00B07'45.\"N");

		} catch (ParseException e) {
			fail("No parsing error should occur");
		}


//		assertTrue(conversionResults.convertedCoord < 0, "Southern must be negative");
//		assertFalse(conversionResults.isLongitude, "Southern must be latitude");
//
//		conversionResults = coordinateConverter.tryConvert("35\u00B034.744");
//		assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
//		assertNull(conversionResults.isLongitude, "Longitude must be undefined");
//
//		conversionResults = coordinateConverter.tryConvert("95\u00B034.744");
//		assertTrue(conversionResults.isLongitude, "Longitude must be defined");
//
//
//		conversionResults = coordinateConverter.tryConvert("-35\u00B034'55.67S");
//		assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
//
//		conversionResults = coordinateConverter.tryConvert("35\u00B011'34.744SN");
//		assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
//
//		conversionResults = coordinateConverter.tryConvert("35\u00B011'34.744SW");
//		assertTrue(conversionResults.isLongitude, "Western must be longitude");
//
//		conversionResults = coordinateConverter.tryConvert("35D11M34.744S");
//		assertNull(conversionResults.isLongitude, "isLongitude must be undefined. S stands for second.");

	}


	@Test
	public void testDoubleParsing(){
		try {
			assertEquals(longitude1, point1.getLongitude(), "");
			assertTrue(latitude1.equals(point1.getLatitude()), "");
			point1.setLatitudeByParsing("33.474");
			assertEquals(longitude1, point1.getLongitude(), "");
			assertFalse(latitude1.equals(point1.getLatitude()), "");
			assertEquals(Double.valueOf("33.474"), point1.getLatitude(), "");
			point1.setLatitudeByParsing("-39,474");
			assertEquals(Double.valueOf("-39.474"), point1.getLatitude(), "");
		} catch (ParseException e) {
			fail("No parsing error should occur");
		}

		try {
			point1.setLongitudeByParsing("-120.4");
			assertEquals("-120.4", point1.getLongitude().toString(), "");
			point1.setLongitudeByParsing("53,4");
			assertEquals("53.4", point1.getLongitude().toString(), "");
		} catch (ParseException e) {
			fail("No parsing error should occur");
		}
		try {
			point1.setLatitudeByParsing("112.456");
			fail("Latitude can not be > 90");
		} catch (ParseException e) {
			assertTrue(true, "Latitude can not be > 90");
		}

		try {
			point1.setLongitudeByParsing("191");
			fail("Longitude can be > 180°");
		} catch (ParseException e) {
			assertTrue(true, "Longitude can not > 180°");
		}
		try {
			point1.setLatitudeByParsing("2\u00B039'38,5956\"S");
		} catch (ParseException e) {
			fail("String '2°39'38,5956\"S'should be parsable");
		}
}

	/**
	 * I don't exactly know what should happen here.
	 * Please see https://dev.e-taxonomy.eu/redmine/issues/2267#comment:3 on why this test was created
	 *
	 * @throws ParseException
	 */
	@Test
	public void testParsingHexagesimalAndDecimalMixed() throws ParseException{
		String example = "35\u00B034'55.67\"S";
		point1.setLatitudeByParsing(example);
		assertEquals(example, point1.getLatitudeSexagesimal().toString());
	}

	@Test
	public void testStaticParsing(){
		try{
			Point.parseLatitude("1");
		}catch (NullPointerException e){
			fail("No NullPointerException should occur");
		} catch (ParseException e) {
			fail("No parsing error should occur");
		}
	}
}
