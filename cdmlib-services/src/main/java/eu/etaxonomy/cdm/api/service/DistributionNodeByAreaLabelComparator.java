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

import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.location.NamedArea;

public class DistributionNodeByAreaLabelComparator implements Comparator<TreeNode<Set<Distribution>, NamedArea>>{
    @Override
    public int compare(TreeNode<Set<Distribution>, NamedArea> arg0, TreeNode<Set<Distribution>, NamedArea> arg1) {
        return arg0.getNodeId().getLabel().compareTo(arg1.getNodeId().getLabel());
    }
}
