/**
 * 
 */
package eu.etaxonomy.cdm.model.common;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
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

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#clone()}.
	 */
	@Test
	public void testClone() {
		logger.warn("Not yet implemented");
	}

}
