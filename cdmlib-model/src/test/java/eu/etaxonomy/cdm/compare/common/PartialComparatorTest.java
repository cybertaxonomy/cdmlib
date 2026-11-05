/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author a.mueller
 * @since 15.07.2021
 */
public class PartialComparatorTest {

    private Partial year1978;
    private Partial monthMay;
    private PartialComparator comparator;

    @BeforeEach
    public void setUp() throws Exception {
        comparator = PartialComparator.INSTANCE();

        year1978 = new Partial(DateTimeFieldType.year(), 1978);
        monthMay = new Partial(DateTimeFieldType.monthOfYear(), 4);
    }

    @Test
    public void test() {
        assertEquals(-1, comparator.compare(year1978, monthMay));
        monthMay = monthMay.with(DateTimeFieldType.year(), 1978);
        assertEquals(1, comparator.compare(year1978, monthMay));
        monthMay = monthMay.with(DateTimeFieldType.year(), 1979);
        assertEquals(-1, comparator.compare(year1978, monthMay));
        Partial may1979 = new Partial(DateTimeFieldType.year(), 1979).with(DateTimeFieldType.monthOfYear(), 4);
        assertEquals(0, comparator.compare(may1979, monthMay));
    }

    @Test
    public void testNullSmallest() {
        comparator = PartialComparator.INSTANCE_NULL_SMALLEST();
        assertEquals(1, comparator.compare(year1978, monthMay));
        monthMay = monthMay.with(DateTimeFieldType.year(), 1978);
        assertEquals(-1, comparator.compare(year1978, monthMay));
        monthMay = monthMay.with(DateTimeFieldType.year(), 1979);
        assertEquals(-1, comparator.compare(year1978, monthMay));
        Partial may1979 = new Partial(DateTimeFieldType.year(), 1979).with(DateTimeFieldType.monthOfYear(), 4);
        assertEquals(0, comparator.compare(may1979, monthMay));
    }
}
