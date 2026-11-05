/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author a.mueller
 * @since 12.09.2018
 */
public class TreeIndexTest {

    TreeIndex indexRoot;
    TreeIndex indexTree;

    @BeforeEach
    public void setUp() throws Exception {
        indexRoot = TreeIndex.NewInstance("#t1#222#");
        //maybe not allowed in future, see comment on TreeIndex.regEx
        indexTree = TreeIndex.NewInstance("#t1#");
    }


    @Test
    public void testParse() {
        try {
            indexTree = TreeIndex.NewInstance("#t1#11");
            fail("Index string must end with #");
        } catch (Exception e) {}
        try {
            indexTree = TreeIndex.NewInstance("t1#11#");
            fail("Index string must start with #");
        } catch (Exception e) {}

        try {
            indexTree = TreeIndex.NewInstance("#1#11#");
            fail("Index must start with tree identifier which starts with a single character a-z");
        } catch (Exception e) {}

        try {
            indexTree = TreeIndex.NewInstance("#tt1#11#");
            fail("Tree identifier must have only 1 character a-z");
        } catch (Exception e) {}

        try {
            indexTree = TreeIndex.NewInstance("#t1#t11#");
            fail("Node identifier must have no character a-z");
        } catch (Exception e) {}

    }

    @Test
    public void testIsTreeRoot() {
        assertTrue(indexRoot.isTreeRoot(), "Index should be tree root");
        assertFalse(indexRoot.isTree(), "Index should not be tree");
    }

    @Test
    public void testIsTree() {
        assertFalse(indexTree.isTreeRoot(), "Index should not be tree root");
        assertTrue(indexTree.isTree(), "Index should be tree");
    }

    @Test
    public void testParentNodeIds() {
        TreeIndex myIndex = TreeIndex.NewInstance("#t1#222#33#444#515#");
        List<Integer> nodeIds = myIndex.parentNodeIds(false);
        assertEquals((Integer)515, nodeIds.get(nodeIds.size()-1), "Last index should be 515");
        List<String> nodeIdsStr = myIndex.parentNodeIds(true, true);
        assertEquals("t1", nodeIdsStr.get(0), "1st index should be 't1'");
        assertEquals("33", nodeIdsStr.get(2), "3rd index should be '33'");
    }

}
