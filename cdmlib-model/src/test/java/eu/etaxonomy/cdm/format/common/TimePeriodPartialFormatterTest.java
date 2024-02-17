/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.common;

import org.joda.time.Partial;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author muellera
 * @since 17.02.2024
 */
public class TimePeriodPartialFormatterTest {

    private Partial start;
    private static TimePeriodPartialFormatter formatter = TimePeriodPartialFormatter.INSTANCE();

    @Before
    public void setUp() throws Exception {
        TimePeriod tp = TimePeriodParser.parseString("15.4.1782 - 30.5.1783");
        start = tp.getStart();

    }

    @Test
    public void test() {
        Assert.assertEquals("15 Apr 1782", start.toString(formatter));
    }

}
