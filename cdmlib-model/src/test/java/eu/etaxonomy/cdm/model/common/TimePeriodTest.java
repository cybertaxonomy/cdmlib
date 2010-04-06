/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
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
		TimePeriod tp = new TimePeriod();
		Assert.assertNotNull("Time period must be created",tp);
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
		TimePeriod tp = new TimePeriod();
		Partial start = new Partial(DateTimeFieldType.year(), 1999);
		tp.setStart(start);
		Assert.assertEquals("Start year should be 1999", Integer.valueOf(1999), tp.getStartYear());
		Assert.assertEquals("Start should be 'start'", start, tp.getStart());
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#getEnd()}.
	 */
	@Test
	public void testGetEnd() {
		TimePeriod tp = new TimePeriod();
		Partial end = new Partial(DateTimeFieldType.year(), 1999);
		tp.setEnd(end);
		Assert.assertEquals("End year should be 1999", Integer.valueOf(1999), tp.getEndYear());
		Assert.assertEquals("End should be 'end'", end, tp.getEnd());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#getYear()}.
	 */
	@Test
	public void testGetYear() {
		TimePeriod tp = new TimePeriod();
		tp.setStartYear(1999);
		Assert.assertEquals("Year should be 1999", "1999", tp.getYear());
		tp.setEndYear(2002);
		Assert.assertEquals("Year should be 1999-2002", "1999-2002", tp.getYear());
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
		//unparsable
		String strUnparsablePeriod = "wef 1809-78";
		TimePeriod tpUnparsable = TimePeriod.parseString(strUnparsablePeriod);
		assertNotNull(tpUnparsable);
		Assert.assertEquals(strUnparsablePeriod, tpUnparsable.getFreeText());
		
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
		
		String strCPeriod = "c.  1806-1810";
		TimePeriod tpC = TimePeriod.parseString(strCPeriod);
		assertNotNull(tpC);
		Assert.assertEquals(strCPeriod, tpC.getFreeText());
		Assert.assertEquals(Integer.valueOf(1806), tpC.getStartYear());
		Assert.assertEquals(Integer.valueOf(1810), tpC.getEndYear());
		Assert.assertEquals("1806-1810", tpC.getYear());
		
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
		Integer startYear = 1788;
		Integer startMonth = 6;
		Integer startDay = 25;
		Integer endDay = 21;
		Integer endMonth = 12;
		Integer endYear = 1799;
		String freeText = "A free period";
		TimePeriod tp1 = TimePeriod.NewInstance(startYear,endYear);
		tp1.setStartDay(startDay);
		tp1.setStartMonth(startMonth);
		tp1.setEndDay(endDay);
		tp1.setEndMonth(endMonth);
		tp1.setFreeText(freeText);
		TimePeriod tpClone = (TimePeriod)tp1.clone();
		Assert.assertEquals("Start year must be 1788.", startYear, tpClone.getStartYear());
		Assert.assertEquals("Start month must be 6.", startMonth, tpClone.getStartMonth());
		Assert.assertEquals("Start day must be 25.", startDay, tpClone.getStartDay());
		Assert.assertEquals("End year must be 1799.", endYear, tpClone.getEndYear());
		Assert.assertEquals("End month must be 12.", endMonth, tpClone.getEndMonth());
		Assert.assertEquals("End day must be 21.", endDay, tpClone.getEndDay());
		Assert.assertEquals("Cloned time period must be equal to originial", tp1, tpClone);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#clone()}.
	 */
	@Test
	public void testEquals() {
		Integer startYear = 1788;
		Integer startMonth = 6;
		Integer startDay = 25;
		Integer endDay = 21;
		Integer endMonth = 12;
		Integer endYear = 1799;
		String freeText = "A free period";
		
		TimePeriod tp1 = TimePeriod.NewInstance(startYear);
		TimePeriod tpClone = (TimePeriod)tp1.clone();
		Assert.assertEquals("Cloned time period must be equal to originial", tp1, tpClone);
		
		tp1.setStartMonth(startMonth);
		Assert.assertFalse("Cloned time period must not be equal to originial", tp1.equals(tpClone));
		tpClone = (TimePeriod)tp1.clone();
		Assert.assertEquals("Cloned time period must be equal to originial", tp1, tpClone);
		

		tp1.setEndYear(endYear);
		Assert.assertFalse("Cloned time period must not be equal to originial", tp1.equals(tpClone));
		tpClone = (TimePeriod)tp1.clone();
		Assert.assertEquals("Cloned time period must be equal to originial", tp1, tpClone);

		tp1.setEndDay(endDay);
		Assert.assertFalse("Cloned time period must not be equal to originial", tp1.equals(tpClone));
		tpClone = (TimePeriod)tp1.clone();
		Assert.assertEquals("Cloned time period must be equal to originial", tp1, tpClone);
		
		tp1.setFreeText(freeText);
		Assert.assertFalse("Cloned time period must not be equal to originial", tp1.equals(tpClone));
		tpClone = (TimePeriod)tp1.clone();
		Assert.assertEquals("Cloned time period must be equal to originial", tp1, tpClone);

		tp1 = TimePeriod.NewInstance();
		Assert.assertFalse("Cloned time period must not be equal to originial", tp1.equals(tpClone));
		TimePeriod tp2 = TimePeriod.NewInstance();
		Assert.assertEquals("Empty time periods must be equal", tp1, tp2);

		tp1.setFreeText(freeText);
		Assert.assertFalse("Tp2 must not be equal to originial", tp1.equals(tp2));
		tp2.setFreeText("jldskjlfi");
		Assert.assertFalse("Tp2 must not be equal to originial", tp1.equals(tpClone));
		tp2.setFreeText(freeText);
		Assert.assertEquals("Tp2 must be equal", tp1, tp2);
	}

	
	
}
