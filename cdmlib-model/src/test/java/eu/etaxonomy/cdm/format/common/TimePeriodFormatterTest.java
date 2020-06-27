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

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.common.TimePeriod;

/**
 * @author a.mueller
 * @since 27.06.2020
 */
public class TimePeriodFormatterTest {

    private TimePeriodFormatter formatter;

    @Before
    public void setUp() throws Exception {
        formatter = TimePeriodFormatter.NewDefaultInstance();
    }

    @Test
    public void test() {
        String endash = UTF8.EN_DASH.toString();
        TimePeriod tp1 = TimePeriod.NewInstance(1788,1799);
        assertNotNull(tp1);
        Assert.assertEquals("1788"+endash+"1799", formatter.format(tp1));
        tp1.setStartDay(3);
        Assert.assertEquals("3 MMM 1788"+endash+"1799", formatter.format(tp1));
        tp1.setEndMonth(11);
        Assert.assertEquals("3 MMM 1788"+endash+"Nov 1799", formatter.format(tp1));
        tp1.setContinued(true);
        Assert.assertEquals("3 MMM 1788+", formatter.format(tp1));

        tp1 = TimePeriod.NewInstance(1788,1799);
        tp1.setContinued(true);
        Assert.assertEquals("1788+", formatter.format(tp1));
        tp1 = TimePeriod.NewInstance((Integer)null);
        tp1.setContinued(true);
        //this is still undefined, could be something like 'xxxx+' in future
        Assert.assertEquals("+", formatter.format(tp1));

        tp1 = TimePeriod.NewInstance(1788, 1788);
        Assert.assertEquals("1788", formatter.format(tp1));
        tp1.setStartMonth(2);
        tp1.setEndMonth(4);
        Assert.assertEquals("Feb"+endash+"Apr 1788", formatter.format(tp1));
        tp1.setEndMonth(2);
        tp1.setStartDay(19);
        tp1.setEndDay(25);
        Assert.assertEquals("19"+endash+"25 Feb 1788", formatter.format(tp1));
        tp1.setEndYear(1789);
        Assert.assertEquals("19 Feb 1788"+endash+"25 Feb 1789", formatter.format(tp1));
        tp1.setEndYear(1788);
        tp1.setEndMonth(3);
        Assert.assertEquals("19 Feb"+endash+"25 Mar 1788", formatter.format(tp1));

    }

}
