/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.utility;

import java.util.Comparator;
import java.util.Set;

import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * Comparator to order distribution tree nodes according to the "natural" order
 * defined by the ordered {@link NamedArea} vocabulary.
 *
 * @author a.mueller
 * @since 04.04.2016
 *
 */
public class DistributionNodeByAreaOrderComparator implements Comparator<TreeNode<Set<Distribution>, NamedArea>>{

    @Override
    public int compare(TreeNode<Set<Distribution>, NamedArea> node1, TreeNode<Set<Distribution>, NamedArea> node2) {
        if (node1.equals(node2)){
            return 0;
        }

        NamedArea area1 = node1.getNodeId();
        NamedArea area2 = node2.getNodeId();
        if (area1 == null && area2 == null){
            return 0;
        }else if (area1 == null){
            return -1;
        }else if (area2 == null){
            return 1;
        }else{
            return - area1.compareTo(area2);  //term compare methods currently use wrong direction
        }
    }
}
