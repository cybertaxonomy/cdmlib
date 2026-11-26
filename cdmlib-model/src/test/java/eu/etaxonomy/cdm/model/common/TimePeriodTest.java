/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import eu.etaxonomy.cdm.model.common.Partial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 */
public class TimePeriodTest {

	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private TimePeriod onlyStartYear;
	private TimePeriod onlyEndYear;
	private TimePeriod startAndEndYear;
	private TimePeriod noStartAndEndYear;
	private static final Integer year = 1982;
	private static final Integer month = 1;
	private static final Integer day = 5;

	@BeforeEach
	public void setUp() throws Exception {
		onlyStartYear = TimePeriod.NewInstance(1922);
		onlyEndYear = TimePeriod.NewInstance(null, 1857);
		startAndEndYear = TimePeriod.NewInstance(1931, 1957);
		Integer start = null;
		Integer end = null;
		noStartAndEndYear = TimePeriod.NewInstance(start, end);
	}

//************************ TESTS ******************************************

	@Test
	public void testNewInstance() {
		TimePeriod tp = TimePeriod.NewInstance();
		assertNotNull(tp);
		assertTrue(tp.isEmpty(), "Timeperiod should be empty");
	}

	@Test
	public void testNewInstancePartial() {
		TimePeriod tp = TimePeriod.NewInstance(Partial.ofDay(5));
		assertNotNull(tp);
		assertFalse(tp.isEmpty(), "Timeperiod should not be empty");
	}

	@Test
	public void testNewInstancePartialPartial() {
		TimePeriod tp = TimePeriod.NewInstance(Partial.ofDay(day));
		assertNotNull(tp);
		assertFalse(tp.isEmpty(), "Timeperiod should not be empty");
		assertEquals(day, tp.getStartDay(), "Timeperiod's should not be empty");
	}

	@Test
	public void testNewInstanceInteger() {
		onlyStartYear = TimePeriod.NewInstance(1922);
		assertEquals(Integer.valueOf(1922), onlyStartYear.getStartYear());
		assertNull(onlyStartYear.getEndYear());
		assertEquals("1922", onlyStartYear.getYear());
	}

	@Test
	public void testNewInstanceIntegerInteger() {
		startAndEndYear = TimePeriod.NewInstance(1931, 1957);
		assertEquals(Integer.valueOf(1957), startAndEndYear.getEndYear());
		assertEquals(Integer.valueOf(1931), startAndEndYear.getStartYear());
		assertEquals("1931"+TimePeriodBase.SEP+"1957", startAndEndYear.getYear());
	}

	@Test
	public void testNewInstanceCalendar() {
		Calendar cal = Calendar.getInstance();
		cal.set(1982, 1, day);
		TimePeriod tp = TimePeriod.NewInstance(cal);
		assertNotNull(tp);
		assertFalse(tp.isEmpty(), "Timeperiod should not be empty");
		assertEquals(day, tp.getStartDay(), "Timeperiod's should not be empty");
	}

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
		assertNotNull(tp);
		assertFalse(tp.isEmpty(), "Timeperiod should not be empty");
		assertEquals(year, tp.getStartYear(), "Timeperiod's start should not be equal with cal1");
		//Calendar starts counting months with 0
		assertEquals((Integer)(month +1), tp.getStartMonth(), "Timeperiod's start should not be equal with cal1 ");
		assertEquals(day, tp.getStartDay(), "Timeperiod's start should not be equal with cal1");

		assertEquals(year2, tp.getEndYear(), "Timeperiod's start should not be equal with cal2");
		//Calendar starts counting months with 0
		assertEquals((Integer)(month2+1), tp.getEndMonth(), "Timeperiod's start should not be equal with cal2");
		assertEquals(day2, tp.getEndDay(), "Timeperiod's end should not be equal with cal2");
	}

	@Test
	public void testNewInstanceZonedDateTime() {
		ZonedDateTime zdt = ZonedDateTime.now();
		TimePeriod tp = TimePeriod.NewInstance(zdt);
		assertNotNull(tp);
		assertFalse(tp.isEmpty(), "Timeperiod should not be empty");
		assertEquals((Integer)zdt.getDayOfMonth(), tp.getStartDay(), "Timeperiod's should not be empty");
	}

	@Test
	public void testNewInstanceZonedDateTimeZonedDateTime() {
		ZonedDateTime zdt = ZonedDateTime.now().plusDays(5);
		ZonedDateTime zdt2 = ZonedDateTime.now();
		TimePeriod tp = TimePeriod.NewInstance(zdt, zdt2);

		assertNotNull(tp);
		assertFalse(tp.isEmpty(), "Timeperiod should not be empty");
		assertEquals((Integer)zdt.getDayOfMonth(), tp.getStartDay(), "Timeperiod's day should not be equal to zoned date time");
		assertEquals((Integer)zdt2.getDayOfMonth(), tp.getEndDay(), "Timeperiod's day should not be equal to zoned date time");
	}

	@Test
	public void testCalendarToPartial() {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day);
		Partial part = TimePeriodBase.calendarToPartial(cal);
		assertEquals(day, (Integer)part.getDay(), "Partial's day should not be equal to calednars day");
	}

	@Test
	public void testReadableInstantToPartial() {
		ZonedDateTime zdt = ZonedDateTime.now();
		Partial part = TimePeriodBase.readableInstantToPartial(zdt);
		assertEquals((Integer)zdt.getDayOfMonth(), (Integer)part.getDay(), "Partial's day should equal zoned date time day");
	}

	@Test
	public void testTimePeriod() {
		TimePeriod tp = new TimePeriod();
		assertNotNull(tp, "Time period must be created");
	}

	@Test
	public void testSetStart(){
		Partial startDate = Partial.of(2010, 12, 16);
		Partial newStartDate = Partial.of(1984, 12, 14);

		TimePeriod tp = TimePeriod.NewInstance(startDate);
		String startString = tp.toString();
		assertNull(tp.getFreeText(), "Freetext should be not set");
		tp.setStart(newStartDate);
		String changedString = tp.toString();
		assertTrue(!startString.equals(changedString), "Setting the partial should change the string representation of the TimePeriod");

		//
		tp = TimePeriodParser.parseString("1752");
		assertNull(tp.getFreeText(), "Freetext should be not set");
		startString = tp.toString();
		tp.setStart(newStartDate);
		changedString = tp.toString();
		assertTrue(!startString.equals(changedString), "Setting a partial for a parsed time period should change the string representation of the TimePeriod	");

		//
		tp = TimePeriodParser.parseString("any strange date");
		assertNotNull(tp.getFreeText(), "Freetext should be set");
		startString = tp.toString();
		tp.setStart(newStartDate);
		changedString = tp.toString();
		assertEquals(startString, changedString, "Setting a partial for a time period having the freetext set should not change the string representation of the TimePeriod	");


		//
//		tp = TimePeriodParser.parseString("15.12.1730"); //TODO currently not parsed
//
//		startString = tp.toString();
//		tp.setStart(newStartDate);
//		changedString = tp.toString();
//
//		assertTrue(!startString.equals(changedString), "Setting a partial for a parsed time period should change the string representation of the TimePeriod	");
	}

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

	@Test
	public void testGetStart() {
		TimePeriod tp = new TimePeriod();
		Partial start = Partial.ofYear(1999);
		tp.setStart(start);
		assertEquals(Integer.valueOf(1999), tp.getStartYear(), "Start year should be 1999");
		assertEquals(start, tp.getStart(), "Start should be 'start'");
	}

	@Test
	public void testGetEnd() {
		TimePeriod tp = new TimePeriod();
		Partial end = Partial.ofYear(1999);
		tp.setEnd(end);
		assertEquals(Integer.valueOf(1999), tp.getEndYear(), "End year should be 1999");
		assertEquals(end, tp.getEnd(), "End should be 'end'");
	}

	@Test
	public void testGetYear() {
		TimePeriod tp = new TimePeriod();
		tp.setStartYear(1999);
		assertEquals("1999", tp.getYear(), "Year should be 1999");
		tp.setEndYear(2002);
		assertEquals("1999"+TimePeriodBase.SEP+"2002", tp.getYear(), "Year should be 1999-2002");
		tp.setContinued(true);
		assertEquals("1999+", tp.getYear(), "Year should be 1999+");
	}

	@Test
	public void testContinued() {
	    TimePeriod tp1 = TimePeriod.NewInstance(2017, 2018);
	    assertEquals((Integer)2018, tp1.getEndYear());
	    tp1.setContinued(true);
	    assertNull(tp1.getEnd(), "The end should be removed and also the CONTINUED constant should be returned for getEnd()");
	    assertTrue(tp1.isContinued());
        assertEquals(null, tp1.getEndYear());
	    assertEquals(null, tp1.getEndMonth());
	    assertEquals(null, tp1.getEndDay());

	    //set continued to false (will not recover old end value)
	    tp1.setContinued(false);
	    assertFalse(tp1.isContinued());
        assertEquals(null, tp1.getEndYear());
        assertEquals(null, tp1.getEndMonth());
        assertEquals(null, tp1.getEndDay());

        //replace continued by end
        tp1 = TimePeriod.NewInstance(2017, 2018);
        tp1.setContinued(true);
        assertTrue(tp1.isContinued());
        tp1.setEndMonth(month);
	    assertFalse(tp1.isContinued());
        assertEquals(null, tp1.getEndYear());
        assertEquals(month, tp1.getEndMonth());
        assertEquals(null, tp1.getEndDay());
	}

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
		TimePeriod tpClone = tp1.clone();
		assertEquals(startYear, tpClone.getStartYear(), "Start year must be 1788.");
		assertEquals(startMonth, tpClone.getStartMonth(), "Start month must be 6.");
		assertEquals(startDay, tpClone.getStartDay(), "Start day must be 25.");
		assertEquals(endYear, tpClone.getEndYear(), "End year must be 1799.");
		assertEquals(endMonth, tpClone.getEndMonth(), "End month must be 12.");
		assertEquals(endDay, tpClone.getEndDay(), "End day must be 21.");
		assertEquals(tp1, tpClone, "Cloned time period must be equal to originial");
	}

	@Test
	public void testEquals() {
		Integer startYear = 1788;
		Integer startMonth = 6;
		Integer endDay = 21;
		Integer endYear = 1799;
		String freeText = "A free period";

		TimePeriod tp1 = TimePeriod.NewInstance(startYear);
		TimePeriod tpClone = tp1.clone();
		assertEquals(tp1, tpClone, "Cloned time period must be equal to originial");

		tp1.setStartMonth(startMonth);
		assertFalse(tp1.equals(tpClone), "Cloned time period must not be equal to originial");
		tpClone = tp1.clone();
		assertEquals(tp1, tpClone, "Cloned time period must be equal to originial");

		tp1.setEndYear(endYear);
		assertFalse(tp1.equals(tpClone), "Cloned time period must not be equal to originial");
		tpClone = tp1.clone();
		assertEquals(tp1, tpClone, "Cloned time period must be equal to originial");

		tp1.setEndDay(endDay);
		assertFalse(tp1.equals(tpClone), "Cloned time period must not be equal to originial");
		tpClone = tp1.clone();
		assertEquals(tp1, tpClone, "Cloned time period must be equal to originial");

		tp1.setFreeText(freeText);
		assertFalse(tp1.equals(tpClone), "Cloned time period must not be equal to originial");
		tpClone = tp1.clone();
		assertEquals(tp1, tpClone, "Cloned time period must be equal to originial");

		tp1 = TimePeriod.NewInstance();
		assertFalse(tp1.equals(tpClone), "Cloned time period must not be equal to originial");
		TimePeriod tp2 = TimePeriod.NewInstance();
		assertEquals(tp1, tp2, "Empty time periods must be equal");

		tp1.setFreeText(freeText);
		assertFalse(tp1.equals(tp2), "Tp2 must not be equal to originial");
		tp2.setFreeText("jldskjlfi");
		assertFalse(tp1.equals(tpClone), "Tp2 must not be equal to originial");
		tp2.setFreeText(freeText);
		assertEquals(tp1, tp2, "Tp2 must be equal");
	}
}
