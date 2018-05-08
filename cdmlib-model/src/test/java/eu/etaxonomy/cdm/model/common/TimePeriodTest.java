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

import java.util.Calendar;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeFieldType;
import org.joda.time.MutableDateTime;
import org.joda.time.Partial;
import org.joda.time.ReadableInstant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.strategy.cache.common.TimePeriodPartialFormatter;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 *
 */
public class TimePeriodTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TimePeriodTest.class);

	private TimePeriod onlyStartYear;
	private TimePeriod onlyEndYear;
	private TimePeriod startAndEndYear;
	private TimePeriod noStartAndEndYear;
	private static final Integer year = 1982;
	private static final Integer month = 1;
	private static final Integer day = 5;

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
		noStartAndEndYear = TimePeriod.NewInstance(start, end);
	}

//************************ TESTS ******************************************

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#NewInstance()}.
	 */
	@Test
	public void testNewInstance() {
		TimePeriod tp = TimePeriod.NewInstance();
		Assert.assertNotNull(tp);
		Assert.assertTrue("Timeperiod should be empty",tp.isEmpty());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#NewInstance(org.joda.time.Partial)}.
	 */
	@Test
	public void testNewInstancePartial() {
		TimePeriod tp = TimePeriod.NewInstance(new Partial().with(DateTimeFieldType.dayOfWeek(), 5));
		Assert.assertNotNull(tp);
		Assert.assertFalse("Timeperiod should not be empty",tp.isEmpty());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#NewInstance(org.joda.time.Partial, org.joda.time.Partial)}.
	 */
	@Test
	public void testNewInstancePartialPartial() {
		TimePeriod tp = TimePeriod.NewInstance(new Partial().with(DateTimeFieldType.dayOfMonth(),day));
		Assert.assertNotNull(tp);
		Assert.assertFalse("Timeperiod should not be empty",tp.isEmpty());
		Assert.assertEquals("Timeperiod's should not be empty", day, tp.getStartDay());
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
		Calendar cal = Calendar.getInstance();
		cal.set(1982, 1, day);
		TimePeriod tp = TimePeriod.NewInstance(cal);
		Assert.assertNotNull(tp);
		Assert.assertFalse("Timeperiod should not be empty",tp.isEmpty());
		Assert.assertEquals("Timeperiod's should not be empty", day, tp.getStartDay());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#NewInstance(java.util.Calendar, java.util.Calendar)}.
	 */
	@Test
	public void testNewInstanceCalendarCalendar() {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day);
		Calendar cal2 = Calendar.getInstance();
		Integer day2 = 20;
		Integer month2 = 7;
		Integer year2 = 1985;
		cal2.set(year2, month2, day2);
		TimePeriod tp = TimePeriod.NewInstance(cal, cal2);
		Assert.assertNotNull(tp);
		Assert.assertFalse("Timeperiod should not be empty",tp.isEmpty());
		Assert.assertEquals("Timeperiod's start should not be equal with cal1", year, tp.getStartYear());
		//Calendar starts counting months with 0
		Assert.assertEquals("Timeperiod's start should not be equal with cal1 ", (Integer)(month +1), tp.getStartMonth());
		Assert.assertEquals("Timeperiod's start should not be equal with cal1", day, tp.getStartDay());

		Assert.assertEquals("Timeperiod's start should not be equal with cal2", year2, tp.getEndYear());
		//Calendar starts counting months with 0
		Assert.assertEquals("Timeperiod's start should not be equal with cal2", (Integer)(month2+1), tp.getEndMonth());
		Assert.assertEquals("Timeperiod's end should not be equal with cal2", day2, tp.getEndDay());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#NewInstance(org.joda.time.ReadableInstant)}.
	 */
	@Test
	public void testNewInstanceReadableInstant() {
		ReadableInstant readInst = new MutableDateTime();
		TimePeriod tp = TimePeriod.NewInstance(readInst);
		Assert.assertNotNull(tp);
		Assert.assertFalse("Timeperiod should not be empty",tp.isEmpty());
		Assert.assertEquals("Timeperiod's should not be empty", (Integer)readInst.get(DateTimeFieldType.dayOfMonth()), tp.getStartDay());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#NewInstance(org.joda.time.ReadableInstant, org.joda.time.ReadableInstant)}.
	 */
	@Test
	public void testNewInstanceReadableInstantReadableInstant() {
		ReadableInstant readInst = new MutableDateTime();
		ReadableInstant readInst2 = new MutableDateTime();
		((MutableDateTime)readInst).addDays(5);
		TimePeriod tp = TimePeriod.NewInstance(readInst, readInst2);

		Assert.assertNotNull(tp);
		Assert.assertFalse("Timeperiod should not be empty",tp.isEmpty());
		Assert.assertEquals("Timeperiod's day should not be equal to readable instant", (Integer)readInst.get(DateTimeFieldType.dayOfMonth()), tp.getStartDay());
		Assert.assertEquals("Timeperiod's day should not be equal to readable instant", (Integer)readInst2.get(DateTimeFieldType.dayOfMonth()), tp.getEndDay());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#calendarToPartial(java.util.Calendar)}.
	 */
	@Test
	public void testCalendarToPartial() {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day);
		Partial part = TimePeriod.calendarToPartial(cal);
		Assert.assertEquals("Partial's day should not be equal to calednars day", day, (Integer)part.get(DateTimeFieldType.dayOfMonth()));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#readableInstantToPartial(org.joda.time.ReadableInstant)}.
	 */
	@Test
	public void testReadableInstantToPartial() {
		ReadableInstant readInst = new MutableDateTime();
		Partial part = TimePeriod.readableInstantToPartial(readInst);
		part.get(DateTimeFieldType.dayOfMonth());
		Assert.assertEquals("Partial's day should not be equal to calednars day", (Integer)part.get(DateTimeFieldType.dayOfMonth()), (Integer)part.get(DateTimeFieldType.dayOfMonth()));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.TimePeriod#TimePeriod()}.
	 */
	@Test
	public void testTimePeriod() {
		TimePeriod tp = new TimePeriod();
		Assert.assertNotNull("Time period must be created",tp);
	}

//	@Ignore
	@Test
	public void testSetStart(){
		Partial startDate = new Partial().with(DateTimeFieldType.year(), 2010)
				.with(DateTimeFieldType.monthOfYear(), 12)
				.with(DateTimeFieldType.dayOfMonth(), 16);
		Partial newStartDate = new Partial().with(DateTimeFieldType.year(), 1984)
			.with(DateTimeFieldType.monthOfYear(), 12)
			.with(DateTimeFieldType.dayOfMonth(), 14);

		TimePeriod tp = TimePeriod.NewInstance(startDate);
		String startString = tp.toString();
		assertNull("Freetext should be not set", tp.getFreeText());
		tp.setStart(newStartDate);
		String changedString = tp.toString();
		Assert.assertTrue("Setting the partial should change the string representation of the TimePeriod", !startString.equals(changedString));

		//
		tp = TimePeriodParser.parseString("1752");
		assertNull("Freetext should be not set", tp.getFreeText());
		startString = tp.toString();
		tp.setStart(newStartDate);
		changedString = tp.toString();
		Assert.assertTrue("Setting a partial for a parsed time period should change the string representation of the TimePeriod	", !startString.equals(changedString));

		//
		tp = TimePeriodParser.parseString("any strange date");
		assertNotNull("Freetext should be set", tp.getFreeText());
		startString = tp.toString();
		tp.setStart(newStartDate);
		changedString = tp.toString();
		Assert.assertEquals("Setting a partial for a time period having the freetext set should not change the string representation of the TimePeriod	", startString, changedString);


		//
//		tp = TimePeriodParser.parseString("15.12.1730"); //TODO currently not parsed
//
//		startString = tp.toString();
//		tp.setStart(newStartDate);
//		changedString = tp.toString();
//
//		Assert.assertTrue("Setting a partial for a parsed time period should change the string representation of the TimePeriod	", !startString.equals(changedString));
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


	/**
	 * TODO should be partly moved to a test class for {@link TimePeriodPartialFormatter}
	 */
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
		Integer endDay = 21;
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
