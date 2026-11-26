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

import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @since 08.05.2018
 */
public class VerbatimTimePeriodTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private VerbatimTimePeriod onlyStartYear;
    private VerbatimTimePeriod onlyEndYear;
    private VerbatimTimePeriod startAndEndYear;
    private VerbatimTimePeriod noStartAndEndYear;
    private static final Integer year = 1982;
    private static final Integer month = 1;
    private static final Integer day = 5;

    private static final String SEP = TimePeriodBase.SEP;

    @BeforeEach
    public void setUp() throws Exception {
        onlyStartYear = VerbatimTimePeriod.NewVerbatimInstance(1922);
        onlyEndYear = VerbatimTimePeriod.NewVerbatimInstance(null, 1857);
        startAndEndYear = VerbatimTimePeriod.NewVerbatimInstance(1931, 1957);
        Integer start = null;
        Integer end = null;
        noStartAndEndYear = VerbatimTimePeriod.NewVerbatimInstance(start, end);
    }

  //************************ TESTS ******************************************

    @Test
    public void testNewVerbatimInstance() {
        VerbatimTimePeriod tp = VerbatimTimePeriod.NewVerbatimInstance();
        assertNotNull(tp);
        assertTrue(tp.isEmpty(), "VerbatimTimeperiod should be empty");
    }

    @Test
    public void testNewVerbatimInstancePartial() {
        VerbatimTimePeriod tp = VerbatimTimePeriod.NewVerbatimInstance(Partial.ofDay(5));
        assertNotNull(tp);
        assertFalse(tp.isEmpty(), "VerbatimTimeperiod should not be empty");
    }

    @Test
    public void testNewVerbatimInstancePartialPartial() {
        VerbatimTimePeriod tp = VerbatimTimePeriod.NewVerbatimInstance(Partial.ofDay(day));
        assertNotNull(tp);
        assertFalse(tp.isEmpty(), "VerbatimTimeperiod should not be empty");
        assertEquals(day, tp.getStartDay(), "VerbatimTimeperiod's should not be empty");
    }

    @Test
    public void testNewVerbatimInstanceInteger() {
        onlyStartYear = VerbatimTimePeriod.NewVerbatimInstance(1922);
        assertEquals(Integer.valueOf(1922), onlyStartYear.getStartYear());
        assertNull(onlyStartYear.getEndYear());
        assertEquals("1922", onlyStartYear.getYear());
    }

    @Test
    public void testNewVerbatimInstanceIntegerInteger() {
        startAndEndYear = VerbatimTimePeriod.NewVerbatimInstance(1931, 1957);
        assertEquals(Integer.valueOf(1957), startAndEndYear.getEndYear());
        assertEquals(Integer.valueOf(1931), startAndEndYear.getStartYear());
        assertEquals("1931"+SEP+"1957", startAndEndYear.getYear());
    }

    @Test
    public void testNewVerbatimInstanceCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.set(1982, 1, day);
        VerbatimTimePeriod tp = VerbatimTimePeriod.NewVerbatimInstance(cal);
        assertNotNull(tp);
        assertFalse(tp.isEmpty(), "Timeperiod should not be empty");
        assertEquals(day, tp.getStartDay(), "Timeperiod's should not be empty");
    }

    @Test
    public void testNewVerbatimInstanceCalendarCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        Calendar cal2 = Calendar.getInstance();
        Integer day2 = 20;
        Integer month2 = 7;
        Integer year2 = 1985;
        cal2.set(year2, month2, day2);
        VerbatimTimePeriod tp = VerbatimTimePeriod.NewVerbatimInstance(cal, cal2);
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
    public void testNewVerbatimInstanceZonedDateTime() {
        ZonedDateTime zdt = ZonedDateTime.now();
        VerbatimTimePeriod tp = VerbatimTimePeriod.NewVerbatimInstance(Partial.fromZonedDateTime(zdt));
        assertNotNull(tp);
        assertFalse(tp.isEmpty(), "Timeperiod should not be empty");
        assertEquals((Integer)zdt.getDayOfMonth(), tp.getStartDay(), "Timeperiod's should not be empty");
    }

    @Test
    public void testNewVerbatimInstanceZonedDateTimeZonedDateTime() {
        ZonedDateTime zdt = ZonedDateTime.now().plusDays(5);
        ZonedDateTime zdt2 = ZonedDateTime.now();
        VerbatimTimePeriod tp = VerbatimTimePeriod.NewVerbatimInstance(Partial.fromZonedDateTime(zdt), Partial.fromZonedDateTime(zdt2));

        assertNotNull(tp);
        assertFalse(tp.isEmpty(), "Timeperiod should not be empty");
        assertEquals((Integer)zdt.getDayOfMonth(), tp.getStartDay(), "Timeperiod's day should equal zoned date time");
        assertEquals((Integer)zdt2.getDayOfMonth(), tp.getEndDay(), "Timeperiod's day should equal zoned date time");
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

        VerbatimTimePeriod tp = VerbatimTimePeriod.NewVerbatimInstance(startDate);
        String startString = tp.toString();
        assertNull(tp.getFreeText(), "Freetext should be not set");
        tp.setStart(newStartDate);
        String changedString = tp.toString();
        assertTrue(!startString.equals(changedString), "Setting the partial should change the string representation of the TimePeriod");

        //
        tp = TimePeriodParser.parseStringVerbatim("1752");
        assertNull(tp.getFreeText(), "Freetext should be not set");
        startString = tp.toString();
        tp.setStart(newStartDate);
        changedString = tp.toString();
        assertTrue(!startString.equals(changedString), "Setting a partial for a parsed time period should change the string representation of the TimePeriod ");

        //
        tp = TimePeriodParser.parseStringVerbatim("any strange date");
        assertNotNull(tp.getFreeText(), "Freetext should be set");
        startString = tp.toString();
        tp.setStart(newStartDate);
        changedString = tp.toString();
        assertEquals(startString, changedString, "Setting a partial for a time period having the freetext set should not change the string representation of the TimePeriod  ");

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
        assertEquals("1999"+SEP+"2002", tp.getYear(), "Year should be 1999-2002");
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
        //TODO switch back to TimePeriod after migration to hibernate 6
        VerbatimTimePeriod tp1 = VerbatimTimePeriod.NewVerbatimInstance(startYear,endYear);
        tp1.setStartDay(startDay);
        tp1.setStartMonth(startMonth);
        tp1.setEndDay(endDay);
        tp1.setEndMonth(endMonth);
        tp1.setFreeText(freeText);
        TimePeriodBase tpClone = tp1.clone();
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

        VerbatimTimePeriod tp1 = VerbatimTimePeriod.NewVerbatimInstance(startYear);
        VerbatimTimePeriod tpClone = tp1.clone();
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

        tp1 = VerbatimTimePeriod.NewVerbatimInstance();
        assertFalse(tp1.equals(tpClone), "Cloned time period must not be equal to originial");
        VerbatimTimePeriod tp2 = VerbatimTimePeriod.NewVerbatimInstance();
        assertEquals(tp1, tp2, "Empty time periods must be equal");

        tp1.setFreeText(freeText);
        assertFalse(tp1.equals(tp2), "Tp2 must not be equal to originial");
        tp2.setFreeText("jldskjlfi");
        assertFalse(tp1.equals(tpClone), "Tp2 must not be equal to originial");
        tp2.setFreeText(freeText);
        assertEquals(tp1, tp2, "Tp2 must be equal");
    }
}
