/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.mueller
 * @since 10.03.2017
 *
 */
public class DistributionNodeByAreaLabelComparatorTest extends TermTestBase {

    private static DistributionNodeByAreaLabelComparator comparator = new DistributionNodeByAreaLabelComparator();


    @Test
    public void testComparatorContract() {
        NamedArea nodeId1 = NamedArea.NewInstance();
        TreeNode<Set<Distribution>,NamedArea> node1 = new TreeNode<>(nodeId1);
        nodeId1.setLabel("Germany");

        NamedArea nodeId2 = NamedArea.NewInstance();
        TreeNode<Set<Distribution>,NamedArea> node2 = new TreeNode<>(nodeId2);
        nodeId2.setLabel("Germany");

        Integer result1 = comparator.compare(node1, node2);
        Assert.assertNotEquals(Integer.valueOf(0), result1);

        Integer result2 = comparator.compare(node2, node1);
        Assert.assertNotEquals(Integer.valueOf(0), result2);

        Assert.assertTrue(result1.equals(-result2));
    }

}
