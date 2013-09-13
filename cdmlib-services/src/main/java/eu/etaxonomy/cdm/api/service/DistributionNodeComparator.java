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

import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.model.description.Distribution;

public class DistributionNodeComparator implements Comparator<TreeNode<Distribution>>{
	public int compare(TreeNode<Distribution> arg0, TreeNode<Distribution> arg1) {
		return arg0.data.getArea().getLabel().compareTo(arg1.data.getArea().getLabel());
	}
}
