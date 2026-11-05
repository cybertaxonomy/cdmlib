/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.common.TimePeriodBase;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;

/**
 * @author a.mueller
 * @since 27.06.2020
 */
public class VerbatimTimePeriodFormatterTest {

    private VerbatimTimePeriodFormatter formatter;

    private String endash = TimePeriodBase.SEP;

    @BeforeEach
    public void setUp() throws Exception {
        formatter = VerbatimTimePeriodFormatter.NewDefaultInstance();
    }

    @Test
    public void test() {
        VerbatimTimePeriod tp1 = VerbatimTimePeriod.NewVerbatimInstance(1788,1799);
        tp1.setVerbatimDate("1785");
        assertEquals("1788"+endash+"1799 [\"1785\"]", formatter.format(tp1));
        tp1.setVerbatimDate(null);
        assertNotNull(tp1);
        assertEquals("1788"+endash+"1799", formatter.format(tp1));
        tp1.setStartDay(3);
        assertEquals("3 MMM 1788"+endash+"1799", formatter.format(tp1));
        tp1.setEndMonth(11);
        assertEquals("3 MMM 1788"+endash+"Nov 1799", formatter.format(tp1));

    }

}
