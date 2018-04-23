/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.location;


import java.text.ParseException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.location.Point.Direction;
import eu.etaxonomy.cdm.model.location.Point.Sexagesimal;

/**
 * @author a.mueller
 \* @since 04.06.2010
 *
 */
public class PointTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PointTest.class);

	private Point point1;
	private Point point2;
	
	private Integer errorRadius;
	private Double longitude1;
	private Double latitude1;
	private Double longitude2;
	private Double latitude2;
	
	private ReferenceSystem referenceSystem;
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		if (ReferenceSystem.WGS84() == null){
			new DefaultTermInitializer().initialize();
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
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
		Assert.assertNotNull("ReferenceSystem must not be null", referenceSystem);
		Assert.assertNotNull("Point1 must not be null", point1);
		Assert.assertNotNull("Point2 must not be null", point2);
		Assert.assertEquals("", longitude1, point1.getLongitude());

		Assert.assertEquals("", latitude1, point1.getLatitude());
		Assert.assertEquals("", errorRadius, point1.getErrorRadius());
		Assert.assertEquals("", referenceSystem, point1.getReferenceSystem());
		
		Assert.assertNull("LongitudeSexagesimal should be null", point2.getLongitudeSexagesimal());
		Assert.assertNull("LatitudeSexagesimal should be null", point2.getLatitudeSexagesimal());
	}

	@Test
	public void testGetSetLongitude(){
		point2.setLongitude(5.888);
		Assert.assertEquals(Double.valueOf(5.888), point2.getLongitude());
		point2.setLongitude(null);
		Assert.assertEquals(null, point2.getLongitude());
	}

	@Test
	public void testGetSetLatitude(){
		point2.setLatitude(-34.987);
		Assert.assertEquals(Double.valueOf(-34.987), point2.getLatitude());
		point2.setLatitude(null);
		Assert.assertEquals(null, point2.getLatitude());
	}
	
	@Test
	public void testGetSetErrorRadius(){
		point2.setErrorRadius(7);
		Assert.assertEquals(Integer.valueOf(7), point2.getErrorRadius());
		point2.setErrorRadius(null);
		Assert.assertEquals(null, point2.getErrorRadius());
	}
	
	@Test
	public void testGetSetReferenceSystem(){
		ReferenceSystem newRefSystem = ReferenceSystem.NewInstance();
		point2.setReferenceSystem(newRefSystem);
		Assert.assertEquals(newRefSystem, point2.getReferenceSystem());
		point2.setReferenceSystem(null);
		Assert.assertEquals(null, point2.getReferenceSystem());
	}
	
	@Test
	public void testGetLongitudeSexagesimal(){
		Assert.assertEquals("23\u00B07'24.801\"E", point1.getLongitudeSexagesimal().toString(true, false));
		
		
		point2.setLongitudeSexagesimal(Sexagesimal.NewInstance(5, 22, null, Direction.WEST));
		Assert.assertEquals((Integer)22, point2.getLongitudeSexagesimal().minutes);
		Assert.assertEquals((Integer)0, point2.getLongitudeSexagesimal().seconds);
		
		Double latitudeDouble = -45.57389326; 
		point1.setLatitudeSexagesimal(Sexagesimal.valueOf(latitudeDouble, true));
		//Not true because of rounding errors
//		Assert.assertEquals("latitudeDouble must be equal", latitudeDouble, point1.getLatitude());
		
		Sexagesimal sexagesimal1 = Sexagesimal.NewInstance(0, 0, 0, Direction.WEST);
		Sexagesimal sexagesimal2 = Sexagesimal.NewInstance(2, 2, 2, Direction.WEST);
		Assert.assertNotSame("", sexagesimal1, sexagesimal2);
	
			
	}

	@Test
	public void testParsing(){
		try {
			Assert.assertEquals("", longitude1, point1.getLongitude());
			Assert.assertTrue("", latitude1.equals(point1.getLatitude()));
			point1.setLatitudeByParsing("35\u00B034'20\"S");
			Assert.assertEquals("", longitude1, point1.getLongitude());
			Assert.assertFalse("", latitude1.equals(point1.getLatitude()));
			Assert.assertEquals("", Double.valueOf("-35.57222222222222"), point1.getLatitude());
		} catch (ParseException e) {
			Assert.fail("No parsing error should occur");
		}
		try {
			point1.setLongitudeByParsing("112\u00B034.34'N");
			Assert.assertEquals("", "112.57233", point1.getLongitude().toString().substring(0,9));
		} catch (ParseException e) {
			Assert.fail("No parsing error should occur");
		}
		try {
			point1.setLatitudeByParsing("112\u00B034.34'S");
			Assert.fail("Latitude can not be > 90");
		} catch (ParseException e) {
			Assert.assertTrue("Latitude can not be > 90", true);
		}
		try {
			point1.setLongitudeByParsing("45\u00B034.34'S");
			Assert.fail("Longitude can not be S");
		} catch (ParseException e) {
			Assert.assertTrue("Longitude can not be S", true);
		}
		//#2962 (rounding of tertiers)
		try {
			point1.setLatitudeByParsing("37\u00B07'44\"N");
			Assert.assertEquals("Result should be 37\u00B07'44\"N not 37\u00B07'44.999\"N", "37\u00B07'44\"N", point1.getLatitudeSexagesimal().toString());
			
			point1.setLatitudeByParsing("37\u00B07'45\"N");
			Assert.assertEquals("Result should be 37\u00B07'45\"N not 37\u00B07'45.\"N", "37\u00B07'45\"N", point1.getLatitudeSexagesimal().toString());
			
		} catch (ParseException e) {
			Assert.fail("No parsing error should occur");
		}
		
		
		 


		
		
		
//		Assert.assertTrue("Southern must be negative", conversionResults.convertedCoord < 0);
//		Assert.assertFalse("Southern must be latitude", conversionResults.isLongitude);
//
//		conversionResults = coordinateConverter.tryConvert("35\u00B034.744");
//		Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
//		Assert.assertNull("Longitude must be undefined", conversionResults.isLongitude);
//
//		conversionResults = coordinateConverter.tryConvert("95\u00B034.744");
//		Assert.assertTrue("Longitude must be defined", conversionResults.isLongitude);
//
//		
//		conversionResults = coordinateConverter.tryConvert("-35\u00B034'55.67S");
//		Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
//
//		conversionResults = coordinateConverter.tryConvert("35\u00B011'34.744SN");
//		Assert.assertTrue(conversionResults.conversionComments, conversionResults.patternRecognised);
//
//		conversionResults = coordinateConverter.tryConvert("35\u00B011'34.744SW");
//		Assert.assertTrue("Western must be longitude", conversionResults.isLongitude);
//		
//		conversionResults = coordinateConverter.tryConvert("35D11M34.744S");
//		Assert.assertNull("isLongitude must be undefined. S stands for second.", conversionResults.isLongitude);

	}
	

	@Test
	public void testDoubleParsing(){
		try {
			Assert.assertEquals("", longitude1, point1.getLongitude());
			Assert.assertTrue("", latitude1.equals(point1.getLatitude()));
			point1.setLatitudeByParsing("33.474");
			Assert.assertEquals("", longitude1, point1.getLongitude());
			Assert.assertFalse("", latitude1.equals(point1.getLatitude()));
			Assert.assertEquals("", Double.valueOf("33.474"), point1.getLatitude());
			point1.setLatitudeByParsing("-39,474");
			Assert.assertEquals("", Double.valueOf("-39.474"), point1.getLatitude());
		} catch (ParseException e) {
			Assert.fail("No parsing error should occur");
		}
		
		try {
			point1.setLongitudeByParsing("-120.4");
			Assert.assertEquals("", "-120.4", point1.getLongitude().toString());
			point1.setLongitudeByParsing("53,4");
			Assert.assertEquals("", "53.4", point1.getLongitude().toString());
		} catch (ParseException e) {
			Assert.fail("No parsing error should occur");
		}
		try {
			point1.setLatitudeByParsing("112.456");
			Assert.fail("Latitude can not be > 90");
		} catch (ParseException e) {
			Assert.assertTrue("Latitude can not be > 90", true);
		}
		
		try {
			point1.setLongitudeByParsing("191");
			Assert.fail("Longitude can be > 180°");
		} catch (ParseException e) {
			Assert.assertTrue("Longitude can not > 180°", true);
		}
		try {
			point1.setLatitudeByParsing("2\u00B039'38,5956\"S");
		} catch (ParseException e) {
			Assert.fail("String '2°39'38,5956\"S'should be parsable");
		}
}
	
	/**
	 * I don't exactly know what should happen here.
	 * Please see http://dev.e-taxonomy.eu/trac/ticket/2267#comment:3 on why this test was created 
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testParsingHexagesimalAndDecimalMixed() throws ParseException{
		String example = "35\u00B034'55.67\"S";
		point1.setLatitudeByParsing(example);
		Assert.assertEquals(example, point1.getLatitudeSexagesimal().toString());
	}
	
	@Test
	public void testStaticParsing(){
		try{
			Point.parseLatitude("1");
		}catch (NullPointerException e){
			Assert.fail("No NullPointerException should occur");
		} catch (ParseException e) {
			Assert.fail("No parsing error should occur");
		}
	}
	
	
	
}
