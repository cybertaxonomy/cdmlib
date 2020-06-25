/**
* Copyright (C) 2018 EDIT
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

/**
 * @author a.mueller
 * @since 29.04.2020
 */
public class ExtendedTimePeriodTest {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ExtendedTimePeriodTest.class);

    private ExtendedTimePeriod onlyStartYear;
    private ExtendedTimePeriod onlyEndYear;
    private ExtendedTimePeriod startAndEndYear;
    private ExtendedTimePeriod noStartAndEndYear;
    private static final Integer year = 1982;
    private static final Integer month = 1;
    private static final Integer day = 5;

    private static final String SEP = TimePeriod.SEP;

    @Before
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
        Assert.assertNotNull(tp);
        Assert.assertTrue("VerbatimTimeperiod should be empty",tp.isEmpty());
    }

    @Test
    public void testNewExtendedInstancePartial() {
        ExtendedTimePeriod tp = ExtendedTimePeriod.NewExtendedInstance(new Partial().with(DateTimeFieldType.dayOfWeek(), 5));
        Assert.assertNotNull(tp);
        Assert.assertFalse("VerbatimTimeperiod should not be empty",tp.isEmpty());
    }

    @Test
    public void testNewExtendedInstancePartialPartial() {
        ExtendedTimePeriod tp = ExtendedTimePeriod.NewExtendedInstance(new Partial().with(DateTimeFieldType.dayOfMonth(),day));
        Assert.assertNotNull(tp);
        Assert.assertFalse("VerbatimTimeperiod should not be empty",tp.isEmpty());
        Assert.assertEquals("VerbatimTimeperiod's should not be empty", day, tp.getStartDay());
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
        Assert.assertNotNull(tp);
        Assert.assertFalse("Timeperiod should not be empty",tp.isEmpty());
        Assert.assertEquals("Timeperiod's should not be empty", day, tp.getStartDay());
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
        Assert.assertNotNull(tp);
        Assert.assertFalse("Timeperiod should not be empty",tp.isEmpty());
        Assert.assertEquals("Timeperiod's start year shold be equal to year", year, tp.getStartYear());
        //Calendar starts counting months with 0
        Assert.assertEquals("Timeperiod's start month should be equal to month+1 ", (Integer)(month +1), tp.getStartMonth());
        Assert.assertEquals("Timeperiod's start day should be equal to day", day, tp.getStartDay());

        Assert.assertEquals("Timeperiod's end year should be equal to year2", year2, tp.getEndYear());
        //Calendar starts counting months with 0
        Assert.assertEquals("Timeperiod's end month should be equal with month2(+1)", (Integer)(month2+1), tp.getEndMonth());
        Assert.assertEquals("Timeperiod's end day should be equal with day2", day2, tp.getEndDay());

        Assert.assertEquals("Timeperiod's extreme start year shold be equal to year3", year3, tp.getExtremeStartYear());
        //Calendar starts counting months with 0
        Assert.assertEquals("Timeperiod's extreme start month should be equal to month3+1 ", (Integer)(month3 +1), tp.getExtremeStartMonth());
        Assert.assertEquals("Timeperiod's extreme start day should be equal to day3", day3, tp.getExtremeStartDay());

        Assert.assertEquals("Timeperiod's extreme end year should be equal to year4", year4, tp.getExtremeEndYear());
        //Calendar starts counting months with 0
        Assert.assertEquals("Timeperiod's extreme end month should be equal with month4(+1)", (Integer)(month4+1), tp.getExtremeEndMonth());
        Assert.assertEquals("Timeperiod's extreme end day should be equal with day4", day4, tp.getExtremeEndDay());
    }

    @Test
    public void testNewExtendedInstanceReadableInstant() {
        ReadableInstant readInst = new MutableDateTime();
        ExtendedTimePeriod tp = ExtendedTimePeriod.NewExtendedInstance(readInst);
        Assert.assertNotNull(tp);
        Assert.assertFalse("Timeperiod should not be empty",tp.isEmpty());
        Assert.assertEquals("Timeperiod's should not be empty", (Integer)readInst.get(DateTimeFieldType.dayOfMonth()), tp.getStartDay());
    }

    @Test
    public void testNewExtendedInstanceReadableInstantReadableInstant() {
        ReadableInstant readInst = new MutableDateTime();
        ReadableInstant readInst2 = new MutableDateTime();
        ((MutableDateTime)readInst).addDays(5);
        ReadableInstant readInst3 = new MutableDateTime();
        ReadableInstant readInst4 = new MutableDateTime();
        ExtendedTimePeriod tp = ExtendedTimePeriod.NewExtendedInstance(readInst, readInst2, readInst3, readInst4);

        Assert.assertNotNull(tp);
        Assert.assertFalse("Timeperiod should not be empty",tp.isEmpty());
        Assert.assertEquals("Timeperiod's day should be equal to readable instant day", (Integer)readInst.get(DateTimeFieldType.dayOfMonth()), tp.getStartDay());
        Assert.assertEquals("Timeperiod's day should be equal to readable instant day", (Integer)readInst2.get(DateTimeFieldType.dayOfMonth()), tp.getEndDay());
    }

    @Test
    public void testCalendarToPartial() {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        Partial part = TimePeriod.calendarToPartial(cal);
        Assert.assertEquals("Partial's day should not be equal to calednars day", day, (Integer)part.get(DateTimeFieldType.dayOfMonth()));
    }

    @Test
    public void testReadableInstantToPartial() {
        ReadableInstant readInst = new MutableDateTime();
        Partial part = TimePeriod.readableInstantToPartial(readInst);
        part.get(DateTimeFieldType.dayOfMonth());
        Assert.assertEquals("Partial's day should not be equal to calednars day", (Integer)part.get(DateTimeFieldType.dayOfMonth()), (Integer)part.get(DateTimeFieldType.dayOfMonth()));
    }

    @Test
    public void testTimePeriod() {
        TimePeriod tp = new TimePeriod();
        Assert.assertNotNull("Time period must be created",tp);
    }

    @Test
    public void testSetStart(){
        Partial startDate = new Partial().with(DateTimeFieldType.year(), 2010)
                .with(DateTimeFieldType.monthOfYear(), 12)
                .with(DateTimeFieldType.dayOfMonth(), 16);
        Partial newStartDate = new Partial().with(DateTimeFieldType.year(), 1984)
            .with(DateTimeFieldType.monthOfYear(), 12)
            .with(DateTimeFieldType.dayOfMonth(), 14);

        ExtendedTimePeriod tp = ExtendedTimePeriod.NewExtendedInstance(startDate);
        String startString = tp.toString();
        assertNull("Freetext should be not set", tp.getFreeText());
        tp.setStart(newStartDate);
        String changedString = tp.toString();
        Assert.assertTrue("Setting the partial should change the string representation of the TimePeriod", !startString.equals(changedString));

//        //
//        tp = TimePeriodParser.parseStringVerbatim("1752");
//        assertNull("Freetext should be not set", tp.getFreeText());
//        startString = tp.toString();
//        tp.setStart(newStartDate);
//        changedString = tp.toString();
//        Assert.assertTrue("Setting a partial for a parsed time period should change the string representation of the TimePeriod ", !startString.equals(changedString));
//
//        //
//        tp = TimePeriodParser.parseStringVerbatim("any strange date");
//        assertNotNull("Freetext should be set", tp.getFreeText());
//        startString = tp.toString();
//        tp.setStart(newStartDate);
//        changedString = tp.toString();
//        Assert.assertEquals("Setting a partial for a time period having the freetext set should not change the string representation of the TimePeriod  ", startString, changedString);
//

        //
//      tp = TimePeriodParser.parseString("15.12.1730"); //TODO currently not parsed
//
//      startString = tp.toString();
//      tp.setStart(newStartDate);
//      changedString = tp.toString();
//
//      Assert.assertTrue("Setting a partial for a parsed time period should change the string representation of the TimePeriod ", !startString.equals(changedString));
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
        Partial start = new Partial(DateTimeFieldType.year(), 1999);
        tp.setStart(start);
        Assert.assertEquals("Start year should be 1999", Integer.valueOf(1999), tp.getStartYear());
        Assert.assertEquals("Start should be 'start'", start, tp.getStart());
    }

    @Test
    public void testGetEnd() {
        TimePeriod tp = new TimePeriod();
        Partial end = new Partial(DateTimeFieldType.year(), 1999);
        tp.setEnd(end);
        Assert.assertEquals("End year should be 1999", Integer.valueOf(1999), tp.getEndYear());
        Assert.assertEquals("End should be 'end'", end, tp.getEnd());
    }

    @Test
    public void testGetYear() {
        TimePeriod tp = new TimePeriod();
        tp.setStartYear(1999);
        Assert.assertEquals("Year should be 1999", "1999", tp.getYear());
        tp.setEndYear(2002);
        Assert.assertEquals("Year should be 1999"+SEP+"2002", "1999"+SEP+"2002", tp.getYear());
    }


    /**
     * TODO should be partly moved to a test class for {@link TimePeriodPartialFormatter}
     */
    @Test
    public void testToStringTimePeriod() {
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
        Integer endYear2 = 1800;
        ExtendedTimePeriod tp1 = ExtendedTimePeriod.NewExtendedYearInstance(startYear,endYear,startYear2,endYear2);

        String endash = TimePeriod.SEP;
        assertNotNull(tp1);
        Assert.assertEquals("(1787"+endash+")1788"+endash+"1799("+endash+"1800)", tp1.toString());
        tp1.setStartDay(startDay);
        tp1.setStartMonth(startMonth);
        tp1.setEndDay(endDay);
        tp1.setEndMonth(endMonth);
        tp1.setExtremeStartDay(startDay2);
        tp1.setExtremeStartMonth(startMonth2);
        tp1.setExtremeEndDay(endDay2);
        tp1.setExtremeEndMonth(endMonth2);
        Assert.assertEquals("(1787 May 24"+endash+")1788 Jun 25"+endash+"1799 Dec 21("+endash+"1800 Nov 20)", tp1.toString()); //date formatting may change in future

        tp1.setFreeText("My extended period");
        Assert.assertEquals("My extended period", tp1.toString());
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
        TimePeriod tpClone = tp1.clone();
        Assert.assertEquals("Start year must be 1788.", startYear, tpClone.getStartYear());
        Assert.assertEquals("Start month must be 6.", startMonth, tpClone.getStartMonth());
        Assert.assertEquals("Start day must be 25.", startDay, tpClone.getStartDay());
        Assert.assertEquals("End year must be 1799.", endYear, tpClone.getEndYear());
        Assert.assertEquals("End month must be 12.", endMonth, tpClone.getEndMonth());
        Assert.assertEquals("End day must be 21.", endDay, tpClone.getEndDay());

        ExtendedTimePeriod tpCloneExtended = (ExtendedTimePeriod)tpClone;
        Assert.assertEquals("Extreme start year must be 1787.", startYear2, tpCloneExtended.getExtremeStartYear());
        Assert.assertEquals("Extreme start month must be 5.", startMonth2, tpCloneExtended.getExtremeStartMonth());
        Assert.assertEquals("Extreme start day must be 24.", startDay2, tpCloneExtended.getExtremeStartDay());
        Assert.assertEquals("Extreme end year must be 1798.", endYear2, tpCloneExtended.getExtremeEndYear());
        Assert.assertEquals("Extreme end month must be 11.", endMonth2, tpCloneExtended.getExtremeEndMonth());
        Assert.assertEquals("Extreme end day must be 20.", endDay2, tpCloneExtended.getExtremeEndDay());
        Assert.assertEquals("Cloned time period must be equal to originial", tp1, tpClone);
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
        Assert.assertEquals("Cloned time period must be equal to originial", tp1, tpClone);

        tp1.setStartMonth(startMonth);
        Assert.assertFalse("Cloned time period must not be equal to originial", tp1.equals(tpClone));
        tpClone = tp1.clone();
        Assert.assertEquals("Cloned time period must be equal to originial", tp1, tpClone);


        tp1.setEndYear(endYear);
        Assert.assertFalse("Cloned time period must not be equal to originial", tp1.equals(tpClone));
        tpClone = tp1.clone();
        Assert.assertEquals("Cloned time period must be equal to originial", tp1, tpClone);

        tp1.setEndDay(endDay);
        Assert.assertFalse("Cloned time period must not be equal to originial", tp1.equals(tpClone));
        tpClone = tp1.clone();
        Assert.assertEquals("Cloned time period must be equal to originial", tp1, tpClone);

        tp1.setFreeText(freeText);
        Assert.assertFalse("Cloned time period must not be equal to originial", tp1.equals(tpClone));
        tpClone = tp1.clone();
        Assert.assertEquals("Cloned time period must be equal to originial", tp1, tpClone);

        tp1 = ExtendedTimePeriod.NewExtendedInstance();
        Assert.assertFalse("Cloned time period must not be equal to originial", tp1.equals(tpClone));
        ExtendedTimePeriod tp2 = ExtendedTimePeriod.NewExtendedInstance();
        Assert.assertEquals("Empty time periods must be equal", tp1, tp2);

        tp1.setFreeText(freeText);
        Assert.assertFalse("Tp2 must not be equal to originial", tp1.equals(tp2));
        tp2.setFreeText("jldskjlfi");
        Assert.assertFalse("Tp2 must not be equal to originial", tp1.equals(tpClone));
        tp2.setFreeText(freeText);
        Assert.assertEquals("Tp2 must be equal", tp1, tp2);
    }

}
