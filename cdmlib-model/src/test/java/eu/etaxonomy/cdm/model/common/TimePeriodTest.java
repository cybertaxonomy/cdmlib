/**
 * 
 */
package eu.etaxonomy.cdm.model.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author a.mueller
 *
 */
public class TimePeriodTest {
	private static final Logger logger = Logger.getLogger(TimePeriodTest.class);
	
	TimePeriod onlyStartYear;
	TimePeriod onlyEndYear;
	TimePeriod startAndEndYear;
	TimePeriod noStartAndEndYear;
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		onlyStartYear = TimePeriod.NewInstance(1922);
		onlyEndYear = TimePeriod.NewInstance(null, 1857);;
		startAndEndYear = TimePeriod.NewInstance(1931, 1957);
		Integer start = null;
		Integer end = null;
		noStartAndEndYear = TimePeriod.NewInstance(start, end);;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	
//************************ TESTS ******************************************	
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#NewInstance()}.
	 */
	@Test
	public void testNewInstance() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#NewInstance(org.joda.time.Partial)}.
	 */
	@Test
	public void testNewInstancePartial() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#NewInstance(org.joda.time.Partial, org.joda.time.Partial)}.
	 */
	@Test
	public void testNewInstancePartialPartial() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#NewInstance(java.lang.Integer)}.
	 */
	@Test
	public void testNewInstanceInteger() {
		onlyStartYear = TimePeriod.NewInstance(1922);
		assertEquals(Integer.valueOf(1922), onlyStartYear.getStartYear());
		assertNull(onlyStartYear.getEndYear());
		assertEquals("1922", onlyStartYear.getYear());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#NewInstance(java.lang.Integer, java.lang.Integer)}.
	 */
	@Test
	public void testNewInstanceIntegerInteger() {
		startAndEndYear = TimePeriod.NewInstance(1931, 1957);
		assertEquals(Integer.valueOf(1957), startAndEndYear.getEndYear());
		assertEquals(Integer.valueOf(1931), startAndEndYear.getStartYear());
		assertEquals("1931-1957", startAndEndYear.getYear());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#NewInstance(java.util.Calendar)}.
	 */
	@Test
	public void testNewInstanceCalendar() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#NewInstance(org.joda.time.ReadableInstant)}.
	 */
	@Test
	public void testNewInstanceReadableInstant() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#NewInstance(java.util.Calendar, java.util.Calendar)}.
	 */
	@Test
	public void testNewInstanceCalendarCalendar() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#NewInstance(org.joda.time.ReadableInstant, org.joda.time.ReadableInstant)}.
	 */
	@Test
	public void testNewInstanceReadableInstantReadableInstant() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#calendarToPartial(java.util.Calendar)}.
	 */
	@Test
	public void testCalendarToPartial() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#readableInstantToPartial(org.joda.time.ReadableInstant)}.
	 */
	@Test
	public void testReadableInstantToPartial() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#TimePeriod()}.
	 */
	@Test
	public void testTimePeriod() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#TimePeriod(org.joda.time.Partial)}.
	 */
	@Test
	public void testTimePeriodPartial() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#TimePeriod(org.joda.time.Partial, org.joda.time.Partial)}.
	 */
	@Test
	public void testTimePeriodPartialPartial() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#isPeriod()}.
	 */
	@Test
	public void testIsPeriod() {
		assertTrue(startAndEndYear.isPeriod());
		assertFalse(onlyStartYear.isPeriod());
		assertFalse(onlyEndYear.isPeriod());
		assertFalse(noStartAndEndYear.isPeriod());
		onlyStartYear.setEndDay(14);
		assertFalse(onlyStartYear.isPeriod());
		onlyStartYear.setEndYear(1988);
		assertTrue(onlyStartYear.isPeriod()); //may be discussed		
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#getStart()}.
	 */
	@Test
	public void testGetStart() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#setStart(org.joda.time.Partial)}.
	 */
	@Test
	public void testSetStart() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#getEnd()}.
	 */
	@Test
	public void testGetEnd() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#setEnd(org.joda.time.Partial)}.
	 */
	@Test
	public void testSetEnd() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#getYear()}.
	 */
	@Test
	public void testGetYear() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#getStartYear()}.
	 */
	@Test
	public void testGetStartYear() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#getStartMonth()}.
	 */
	@Test
	public void testGetStartMonth() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#getStartDay()}.
	 */
	@Test
	public void testGetStartDay() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#getEndYear()}.
	 */
	@Test
	public void testGetEndYear() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#getEndMonth()}.
	 */
	@Test
	public void testGetEndMonth() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#getEndDay()}.
	 */
	@Test
	public void testGetEndDay() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#setStartYear(java.lang.Integer)}.
	 */
	@Test
	public void testSetStartYear() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#setStartMonth(java.lang.Integer)}.
	 */
	@Test
	public void testSetStartMonth() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#setStartDay(java.lang.Integer)}.
	 */
	@Test
	public void testSetStartDay() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#setEndYear(java.lang.Integer)}.
	 */
	@Test
	public void testSetEndYear() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#setEndMonth(java.lang.Integer)}.
	 */
	@Test
	public void testSetEndMonth() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#setEndDay(java.lang.Integer)}.
	 */
	@Test
	public void testSetEndDay() {
		logger.warn("Not yet implemented");
	}
	
	@Test
	public void testParseSingleDateString() {
		String strDate = "1756";
		Partial date = TimePeriod.parseSingleDate(strDate);
		assertNotNull(date);
		Assert.assertEquals(Integer.parseInt(strDate), date.get(DateTimeFieldType.year()));
		try {
			date.get(DateTimeFieldType.monthOfYear());
			assertFalse(true); //should not be reached
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
		try {
			date.get(DateTimeFieldType.dayOfMonth());
			assertFalse(true); //should not be reached
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
		//to be continued
	}
	
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#parseString(java.lang.String)}.
	 */
	@Test
	public void testParseStringString() {
		String strTimePeriod = "1756";
		TimePeriod tp1 = TimePeriod.parseString(strTimePeriod);
		assertNotNull(tp1);
		Assert.assertEquals(strTimePeriod, tp1.getYear());
		Assert.assertEquals(strTimePeriod, String.valueOf(tp1.getStartYear()));
		assertNull(tp1.getEnd());
		assertNull(tp1.getStartMonth());
		strTimePeriod = "1756-88";
		tp1 = TimePeriod.parseString(strTimePeriod);
		assertNotNull(tp1);
		Assert.assertEquals("1756-1788", tp1.getYear());
		Assert.assertEquals("1756", String.valueOf(tp1.getStartYear()));
		Assert.assertEquals("1788", String.valueOf(tp1.getEndYear()));
		assertNull(tp1.getEndMonth());
		assertNull(tp1.getStartMonth());
		//"1806"[1807]
		String strCorrectedPeriod = "\"1806\"[1807]";
		TimePeriod tpcorrected = TimePeriod.parseString(strCorrectedPeriod);
		assertNotNull(tpcorrected);
		Assert.assertEquals(strCorrectedPeriod, tpcorrected.getFreeText());
		Assert.assertEquals("1807", tpcorrected.getYear());
		
		//fl. 1806
		String strFlPeriod = "fl.  1806?";
		TimePeriod tpFl = TimePeriod.parseString(strFlPeriod);
		assertNotNull(tpFl);
		Assert.assertEquals(strFlPeriod, tpFl.getFreeText());
		Assert.assertEquals("1806", tpFl.getYear());
	}
	
	@Test
	public void testToStringTimePeriod() {
		TimePeriod tp1 = TimePeriod.NewInstance(1788,1799);
		assertNotNull(tp1);
		Assert.assertEquals("1788-1799", tp1.toString());
		tp1.setStartDay(3);
		Assert.assertEquals("3.xx.1788-1799", tp1.toString());
		tp1.setEndMonth(11);
		Assert.assertEquals("3.xx.1788-11.1799", tp1.toString());
	}
	

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#clone()}.
	 */
	@Test
	public void testClone() {
		logger.warn("Not yet implemented");
	}

}
