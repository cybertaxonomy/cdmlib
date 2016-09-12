// $Id$
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


/**
 * @author a.mueller
 * @date 12.09.2016
 *
 */
public class TreeIndexComparatorTest {

    @Test
    public void test() {
        TreeIndexComparator comparator = new TreeIndexComparator();

        //both null
        Assert.assertTrue(0 == comparator.compare(null, null));
        //one null
        Assert.assertTrue(0 > comparator.compare(null, "#t10#10#"));
        Assert.assertTrue(0 < comparator.compare("#t10#10#", null));
        //equal
        Assert.assertTrue(0 == comparator.compare("#t10#10#", "#t10#10#"));

        //same start
        Assert.assertTrue(0 > comparator.compare("#t10#10#", "#t10#10#20#"));
        Assert.assertTrue(0 < comparator.compare("#t10#10#20#", "#t10#10#"));

        //different ends
        Assert.assertTrue(0 > comparator.compare("#t10#10#20#", "#t10#10#30#"));
        Assert.assertTrue(0 < comparator.compare("#t10#10#30#", "#t10#10#20#"));

        //different ends
        Assert.assertTrue(0 > comparator.compare("#t10#10#20#", "#t10#10#30#"));
        Assert.assertTrue(0 > comparator.compare("#t10#10#20#", "#t10#10#30#11"));

        Assert.assertTrue(0 < comparator.compare("#t10#10#30#", "#t10#10#20#"));
        Assert.assertTrue(0 < comparator.compare("#t10#10#30#11", "#t10#10#20#"));

    }

}
