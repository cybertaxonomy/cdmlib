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

import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;

/**
 * @author a.mueller
 * @since 27.06.2020
 */
public class VerbatimTimePeriodFormatterTest {

    private VerbatimTimePeriodFormatter formatter;

    private String endash = TimePeriod.SEP;

    @Before
    public void setUp() throws Exception {
        formatter = VerbatimTimePeriodFormatter.NewDefaultInstance();
    }

    @Test
    public void test() {
        VerbatimTimePeriod tp1 = VerbatimTimePeriod.NewVerbatimInstance(1788,1799);
        assertNotNull(tp1);
        Assert.assertEquals("1788"+endash+"1799", formatter.format(tp1));
        tp1.setStartDay(3);
        Assert.assertEquals("3 MMM 1788"+endash+"1799", formatter.format(tp1));
        tp1.setEndMonth(11);
        Assert.assertEquals("3 MMM 1788"+endash+"Nov 1799", formatter.format(tp1));

    }

}
