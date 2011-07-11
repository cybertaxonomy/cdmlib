package eu.etaxonomy.cdm.api.service;

import java.util.Comparator;

import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.model.description.Distribution;

public class DistributionNodeComparator implements Comparator<TreeNode<Distribution>>{
	public int compare(TreeNode<Distribution> arg0, TreeNode<Distribution> arg1) {
		return arg0.data.getArea().getLabel().compareTo(arg1.data.getArea().getLabel());
	}
}
