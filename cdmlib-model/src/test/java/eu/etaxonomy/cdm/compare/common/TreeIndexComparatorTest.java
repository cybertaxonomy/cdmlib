/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.common;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.common.TreeIndex;

/**
 * @author a.mueller
 * @since 12.09.2016
 */
public class TreeIndexComparatorTest {

    @Test
    public void test() {
        TreeIndexComparator comparator = new TreeIndexComparator();

        TreeIndex ti10 = TreeIndex.NewInstance("#t10#10#");
        TreeIndex ti10_20 = TreeIndex.NewInstance("#t10#10#20#");
        TreeIndex ti10_30 = TreeIndex.NewInstance("#t10#10#30#");
        TreeIndex ti10_30_11 = TreeIndex.NewInstance("#t10#10#30#11#");

        //both null
        assertTrue(0 == comparator.compare(null, null));
        //one null
        assertTrue(0 > comparator.compare(null, ti10));
        assertTrue(0 < comparator.compare(ti10, null));
        //equal
        assertTrue(0 == comparator.compare(ti10, ti10));

        //same start
        assertTrue(0 > comparator.compare(ti10, ti10_20));
        assertTrue(0 < comparator.compare(ti10_20, ti10));

        //different ends
        assertTrue(0 > comparator.compare(ti10_20, ti10_30));
        assertTrue(0 < comparator.compare(ti10_30, ti10_20));

        //different ends
        assertTrue(0 > comparator.compare(ti10_20, ti10_30));
        assertTrue(0 > comparator.compare(ti10_20, ti10_30_11));

        assertTrue(0 < comparator.compare(ti10_30, ti10_20));
        assertTrue(0 < comparator.compare(ti10_30_11, ti10_20));

    }
}
