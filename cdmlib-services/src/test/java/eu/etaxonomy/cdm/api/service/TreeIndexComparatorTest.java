/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import org.junit.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.TreeIndex;
import eu.etaxonomy.cdm.model.common.TreeIndexComparator;


/**
 * @author a.mueller
 \* @since 12.09.2016
 *
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
        Assert.assertTrue(0 == comparator.compare(null, null));
        //one null
        Assert.assertTrue(0 > comparator.compare(null, ti10));
        Assert.assertTrue(0 < comparator.compare(ti10, null));
        //equal
        Assert.assertTrue(0 == comparator.compare(ti10, ti10));

        //same start
        Assert.assertTrue(0 > comparator.compare(ti10, ti10_20));
        Assert.assertTrue(0 < comparator.compare(ti10_20, ti10));

        //different ends
        Assert.assertTrue(0 > comparator.compare(ti10_20, ti10_30));
        Assert.assertTrue(0 < comparator.compare(ti10_30, ti10_20));

        //different ends
        Assert.assertTrue(0 > comparator.compare(ti10_20, ti10_30));
        Assert.assertTrue(0 > comparator.compare(ti10_20, ti10_30_11));

        Assert.assertTrue(0 < comparator.compare(ti10_30, ti10_20));
        Assert.assertTrue(0 < comparator.compare(ti10_30_11, ti10_20));

    }

}
