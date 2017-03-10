/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.Comparator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.location.NamedArea;

public class DistributionNodeByAreaLabelComparator implements Comparator<TreeNode<Set<Distribution>, NamedArea>>{
    @Override
    public int compare(TreeNode<Set<Distribution>, NamedArea> node1, TreeNode<Set<Distribution>, NamedArea> node2) {
        String label1 = node1.getNodeId().getLabel();
        String label2 = node2.getNodeId().getLabel();
        if (StringUtils.isBlank(label1)){
            label1 = node1.getNodeId().getUuid().toString();
        }
        if (StringUtils.isBlank(label2)){
            label2 = node2.getNodeId().getUuid().toString();
        }
        int result = label1.compareTo(label2);

        if (result == 0){
            result = node1.getNodeId().getUuid().compareTo(node2.getNodeId().getUuid());
        }
        return result;
    }
}
