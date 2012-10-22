package eu.etaxonomy.cdm.api.service;

import java.util.Comparator;

import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.model.location.NamedArea;

public class NamedAreaNodeComparator implements Comparator<TreeNode<NamedArea>>{

	public int compare(TreeNode<NamedArea> arg0, TreeNode<NamedArea> arg1) {
		return arg0.data.getLabel().compareTo(arg1.data.getLabel());
	}
}
