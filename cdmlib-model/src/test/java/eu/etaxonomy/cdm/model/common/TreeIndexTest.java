/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author a.mueller
 * @since 12.09.2018
 *
 */
public class TreeIndexTest {

    TreeIndex indexRoot;
    TreeIndex indexTree;

    @Before
    public void setUp() throws Exception {
        indexRoot = TreeIndex.NewInstance("#t1#222#");
        //maybe not allowed in future, see comment on TreeIndex.regEx
        indexTree = TreeIndex.NewInstance("#t1#");
    }


    @Test
    public void testParse() {
        try {
            indexTree = TreeIndex.NewInstance("#t1#11");
            Assert.fail("Index string must end with #");
        } catch (Exception e) {}
        try {
            indexTree = TreeIndex.NewInstance("t1#11#");
            Assert.fail("Index string must start with #");
        } catch (Exception e) {}

        try {
            indexTree = TreeIndex.NewInstance("#1#11#");
            Assert.fail("Index must start with tree identifier which starts with a single character a-z");
        } catch (Exception e) {}

        try {
            indexTree = TreeIndex.NewInstance("#tt1#11#");
            Assert.fail("Tree identifier must have only 1 character a-z");
        } catch (Exception e) {}

        try {
            indexTree = TreeIndex.NewInstance("#t1#t11#");
            Assert.fail("Node identifier must have no character a-z");
        } catch (Exception e) {}

    }

    @Test
    public void testIsTreeRoot() {
        Assert.assertTrue("Index should be tree root", indexRoot.isTreeRoot());
        Assert.assertFalse("Index should not be tree", indexRoot.isTree());
    }

    @Test
    public void testIsTree() {
        Assert.assertFalse("Index should not be tree root", indexTree.isTreeRoot());
        Assert.assertTrue("Index should be tree", indexTree.isTree());
    }

}
