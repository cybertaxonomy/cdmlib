/**
* Copyright (C) 2018 EDIT
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

/**
 * @author a.mueller
 * @since 29.04.2020
 */
public class ExtendedTimePeriodTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private ExtendedTimePeriod onlyStartYear;
    private ExtendedTimePeriod onlyEndYear;
    private ExtendedTimePeriod startAndEndYear;
    private ExtendedTimePeriod noStartAndEndYear;
    private static final Integer year = 1982;
    private static final Integer month = 1;
    private static final Integer day = 5;

    private static final String SEP = TimePeriodBase.SEP;

    @BeforeEach
    public void setUp() throws Exception {
        onlyStartYear = ExtendedTimePeriod.NewExtendedYearInstance(1922);
        onlyEndYear = ExtendedTimePeriod.NewExtendedYearInstance(null, 1857);
        startAndEndYear = ExtendedTimePeriod.NewExtendedYearInstance(1931, 1957);
        Integer start = null;
        Integer end = null;
        noStartAndEndYear = ExtendedTimePeriod.NewExtendedYearInstance(start, end);
    }

  //************************ TESTS ******************************************

    @Test
    public void testNewExtendedInstance() {
        ExtendedTimePeriod tp = ExtendedTimePeriod.NewExtendedInstance();
        assertNotNull(tp);
        assertTrue(tp.isEmpty(), "VerbatimTimeperiod should be empty");
    }

    @Test
    public void testNewExtendedInstancePartial() {
        ExtendedTimePeriod tp = ExtendedTimePeriod.NewExtendedInstance(Partial.ofDay(5));
        assertNotNull(tp);
        assertFalse(tp.isEmpty(), "VerbatimTimeperiod should not be empty");
    }

    @Test
    public void testNewExtendedInstancePartialPartial() {
        ExtendedTimePeriod tp = ExtendedTimePeriod.NewExtendedInstance(Partial.ofDay(day));
        assertNotNull(tp);
        assertFalse(tp.isEmpty(), "VerbatimTimeperiod should not be empty");
        assertEquals(day, tp.getStartDay(), "VerbatimTimeperiod's should not be empty");
    }

    @Test
    public void testNewExtendedInstanceInteger() {
        onlyStartYear = ExtendedTimePeriod.NewExtendedYearInstance(1922);
        assertEquals(Integer.valueOf(1922), onlyStartYear.getStartYear());
        assertNull(onlyStartYear.getEndYear());
        assertEquals("1922", onlyStartYear.getYear());
    }

    @Test
    public void testNewExtendedInstanceIntegerInteger() {
        startAndEndYear = ExtendedTimePeriod.NewExtendedYearInstance(1931, 1957);
        assertEquals(Integer.valueOf(1957), startAndEndYear.getEndYear());
        assertEquals(Integer.valueOf(1931), startAndEndYear.getStartYear());
        assertEquals("1931"+SEP+"1957", startAndEndYear.getYear());
    }

    @Test
    public void testNewExtendedInstanceCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.set(1982, 1, day);
        ExtendedTimePeriod tp = ExtendedTimePeriod.NewExtendedInstance(cal);
        assertNotNull(tp);
        assertFalse(tp.isEmpty(), "Timeperiod should not be empty");
        assertEquals(day, tp.getStartDay(), "Timeperiod's should not be empty");
    }

    @Test
    public void testNewExtendedInstanceCalendarCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        Calendar cal2 = Calendar.getInstance();
        Integer day2 = 20;
        Integer month2 = 7;
        Integer year2 = 1985;
        cal2.set(year2, month2, day2);
        Calendar cal3 = Calendar.getInstance();
        Integer day3 = 12;
        Integer month3 = 3;
        Integer year3 = 1980;
        cal3.set(year3, month3, day3);
        Calendar cal4 = Calendar.getInstance();
        Integer day4 = 2;
        Integer month4 = 1;
        Integer year4 = 1986;
        cal4.set(year4, month4, day4);
        ExtendedTimePeriod tp = ExtendedTimePeriod.NewExtendedInstance(cal, cal2, cal3, cal4);
        assertNotNull(tp);
        assertFalse(tp.isEmpty(), "Timeperiod should not be empty");
        assertEquals(year, tp.getStartYear(), "Timeperiod's start year shold be equal to year");
        //Calendar starts counting months with 0
        assertEquals((Integer)(month +1), tp.getStartMonth(), "Timeperiod's start month should be equal to month+1 ");
        assertEquals(day, tp.getStartDay(), "Timeperiod's start day should be equal to day");

        assertEquals(year2, tp.getEndYear(), "Timeperiod's end year should be equal to year2");
        //Calendar starts counting months with 0
        assertEquals((Integer)(month2+1), tp.getEndMonth(), "Timeperiod's end month should be equal with month2(+1)");
        assertEquals(day2, tp.getEndDay(), "Timeperiod's end day should be equal with day2");

        assertEquals(year3, tp.getExtremeStartYear(), "Timeperiod's extreme start year shold be equal to year3");
        //Calendar starts counting months with 0
        assertEquals((Integer)(month3 +1), tp.getExtremeStartMonth(), "Timeperiod's extreme start month should be equal to month3+1 ");
        assertEquals(day3, tp.getExtremeStartDay(), "Timeperiod's extreme start day should be equal to day3");

        assertEquals(year4, tp.getExtremeEndYear(), "Timeperiod's extreme end year should be equal to year4");
        //Calendar starts counting months with 0
        assertEquals((Integer)(month4+1), tp.getExtremeEndMonth(), "Timeperiod's extreme end month should be equal with month4(+1)");
        assertEquals(day4, tp.getExtremeEndDay(), "Timeperiod's extreme end day should be equal with day4");
    }

    @Test
    public void testNewExtendedInstanceZonedDateTime() {
        ZonedDateTime zdt = ZonedDateTime.now();
        ExtendedTimePeriod tp = ExtendedTimePeriod.NewExtendedInstance(Partial.fromZonedDateTime(zdt));
        assertNotNull(tp);
        assertFalse(tp.isEmpty(), "Timeperiod should not be empty");
        assertEquals((Integer)zdt.getDayOfMonth(), tp.getStartDay(), "Timeperiod's should not be empty");
    }

    @Test
    public void testNewExtendedInstanceZonedDateTimeZonedDateTime() {
        ZonedDateTime zdt = ZonedDateTime.now().plusDays(5);
        ZonedDateTime zdt2 = ZonedDateTime.now();
        ZonedDateTime zdt3 = ZonedDateTime.now().minusDays(2);
        ZonedDateTime zdt4 = ZonedDateTime.now().plusDays(2);
        ExtendedTimePeriod tp = ExtendedTimePeriod.NewExtendedInstance(
                Partial.fromZonedDateTime(zdt), Partial.fromZonedDateTime(zdt2),
                Partial.fromZonedDateTime(zdt3), Partial.fromZonedDateTime(zdt4));

        assertNotNull(tp);
        assertFalse(tp.isEmpty(), "Timeperiod should not be empty");
        assertEquals((Integer)zdt.getDayOfMonth(), tp.getStartDay(), "Timeperiod's day should be equal to zoned date time day");
        assertEquals((Integer)zdt2.getDayOfMonth(), tp.getEndDay(), "Timeperiod's day should be equal to zoned date time day");
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

        ExtendedTimePeriod tp = ExtendedTimePeriod.NewExtendedInstance(startDate);
        String startString = tp.toString();
        assertNull(tp.getFreeText(), "Freetext should be not set");
        tp.setStart(newStartDate);
        String changedString = tp.toString();
        assertTrue(!startString.equals(changedString), "Setting the partial should change the string representation of the TimePeriod");

//        //
//        tp = TimePeriodParser.parseStringVerbatim("1752");
//        assertNull(tp.getFreeText(), "Freetext should be not set");
//        startString = tp.toString();
//        tp.setStart(newStartDate);
//        changedString = tp.toString();
//        assertTrue(!startString.equals(changedString), "Setting a partial for a parsed time period should change the string representation of the TimePeriod ");
//
//        //
//        tp = TimePeriodParser.parseStringVerbatim("any strange date");
//        assertNotNull(tp.getFreeText(), "Freetext should be set");
//        startString = tp.toString();
//        tp.setStart(newStartDate);
//        changedString = tp.toString();
//        assertEquals(startString, changedString, "Setting a partial for a time period having the freetext set should not change the string representation of the TimePeriod  ");
//

        //
//      tp = TimePeriodParser.parseString("15.12.1730"); //TODO currently not parsed
//
//      startString = tp.toString();
//      tp.setStart(newStartDate);
//      changedString = tp.toString();
//
//      assertTrue(!startString.equals(changedString), "Setting a partial for a parsed time period should change the string representation of the TimePeriod ");
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
        assertEquals("1999"+SEP+"2002", tp.getYear(), "Year should be 1999"+SEP+"2002");
    }

    @Test
    public void testClone() {
        Integer startYear = 1788;
        Integer startMonth = 6;
        Integer startDay = 25;
        Integer endDay = 21;
        Integer endMonth = 12;
        Integer endYear = 1799;
        Integer startYear2 = 1787;
        Integer startMonth2 = 5;
        Integer startDay2 = 24;
        Integer endDay2 = 20;
        Integer endMonth2 = 11;
        Integer endYear2 = 1798;
        String freeText = "A free period";
        ExtendedTimePeriod tp1 = ExtendedTimePeriod.NewExtendedYearInstance(startYear,endYear,startYear2,endYear2);
        tp1.setStartDay(startDay);
        tp1.setStartMonth(startMonth);
        tp1.setEndDay(endDay);
        tp1.setEndMonth(endMonth);
        tp1.setExtremeStartDay(startDay2);
        tp1.setExtremeStartMonth(startMonth2);
        tp1.setExtremeEndDay(endDay2);
        tp1.setExtremeEndMonth(endMonth2);
        tp1.setFreeText(freeText);
        ExtendedTimePeriod tpClone = tp1.clone();
        assertEquals(startYear, tpClone.getStartYear(), "Start year must be 1788.");
        assertEquals(startMonth, tpClone.getStartMonth(), "Start month must be 6.");
        assertEquals(startDay, tpClone.getStartDay(), "Start day must be 25.");
        assertEquals(endYear, tpClone.getEndYear(), "End year must be 1799.");
        assertEquals(endMonth, tpClone.getEndMonth(), "End month must be 12.");
        assertEquals(endDay, tpClone.getEndDay(), "End day must be 21.");

        ExtendedTimePeriod tpCloneExtended = tpClone;
        assertEquals(startYear2, tpCloneExtended.getExtremeStartYear(), "Extreme start year must be 1787.");
        assertEquals(startMonth2, tpCloneExtended.getExtremeStartMonth(), "Extreme start month must be 5.");
        assertEquals(startDay2, tpCloneExtended.getExtremeStartDay(), "Extreme start day must be 24.");
        assertEquals(endYear2, tpCloneExtended.getExtremeEndYear(), "Extreme end year must be 1798.");
        assertEquals(endMonth2, tpCloneExtended.getExtremeEndMonth(), "Extreme end month must be 11.");
        assertEquals(endDay2, tpCloneExtended.getExtremeEndDay(), "Extreme end day must be 20.");
        assertEquals(tp1, tpClone, "Cloned time period must be equal to originial");
    }

    @Test
    public void testEquals() {
        Integer startYear = 1788;
        Integer startMonth = 6;
        Integer endDay = 21;
        Integer endYear = 1799;
        String freeText = "A free period";

        ExtendedTimePeriod tp1 = ExtendedTimePeriod.NewExtendedYearInstance(startYear);
        ExtendedTimePeriod tpClone = tp1.clone();
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

        tp1 = ExtendedTimePeriod.NewExtendedInstance();
        assertFalse(tp1.equals(tpClone), "Cloned time period must not be equal to originial");
        ExtendedTimePeriod tp2 = ExtendedTimePeriod.NewExtendedInstance();
        assertEquals(tp1, tp2, "Empty time periods must be equal");

        tp1.setFreeText(freeText);
        assertFalse(tp1.equals(tp2), "Tp2 must not be equal to originial");
        tp2.setFreeText("jldskjlfi");
        assertFalse(tp1.equals(tpClone), "Tp2 must not be equal to originial");
        tp2.setFreeText(freeText);
        assertEquals(tp1, tp2, "Tp2 must be equal");
    }

}
