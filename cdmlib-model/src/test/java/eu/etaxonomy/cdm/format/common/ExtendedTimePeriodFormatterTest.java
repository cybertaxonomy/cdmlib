/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.common;

import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.ExtendedTimePeriod;
import eu.etaxonomy.cdm.model.common.TimePeriod;

/**
 * @author a.mueller
 * @since 27.06.2020
 */
public class ExtendedTimePeriodFormatterTest {

    private ExtendedTimePeriodFormatter formatter;

    private String endash = TimePeriod.SEP;

    @Before
    public void setUp() throws Exception {
        formatter = ExtendedTimePeriodFormatter.NewDefaultInstance();
    }

    @Test
    public void test() {
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
        assertNotNull(tp1);
        Assert.assertEquals("(1787"+endash+")1788"+endash+"1799("+endash+"1800)", formatter.format(tp1));
        tp1.setStartDay(startDay);
        tp1.setStartMonth(startMonth);
        tp1.setEndDay(endDay);
        tp1.setEndMonth(endMonth);
        tp1.setExtremeStartDay(startDay2);
        tp1.setExtremeStartMonth(startMonth2);
        tp1.setExtremeEndDay(endDay2);
        tp1.setExtremeEndMonth(endMonth2);
        Assert.assertEquals("(24 May 1787"+endash+")25 Jun 1788"+endash+"21 Dec 1799("+endash+"20 Nov 1800)", formatter.format(tp1)); //date formatting may change in future

        tp1 = ExtendedTimePeriod.NewExtendedMonthInstance(startMonth, endMonth, startMonth2, endMonth2);
        assertNotNull(tp1);
        Assert.assertEquals("(May"+endash+")Jun"+endash+"Dec("+endash+"Nov)", formatter.format(tp1));
        tp1.setStartDay(startDay);
        tp1.setStartMonth(startMonth);
        tp1.setEndDay(endDay);
        tp1.setEndMonth(endMonth);
        tp1.setExtremeStartDay(startDay2);
        tp1.setExtremeStartMonth(startMonth2);
        tp1.setExtremeEndDay(endDay2);
        tp1.setExtremeEndMonth(endMonth2);
        Assert.assertEquals("(24 May"+endash+")25 Jun"+endash+"21 Dec("+endash+"20 Nov)", formatter.format(tp1)); //date formatting may change in future

        tp1.setFreeText("My extended period");
        Assert.assertEquals("My extended period", formatter.format(tp1));

    }

}
