package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.common.Tree;
import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OriginalSourceBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;

public class DistributionTree extends Tree<Distribution>{
	
	public DistributionTree(){
		NamedArea area = new NamedArea();
		Distribution data = Distribution.NewInstance();
		data.setArea(area);
		data.putModifyingText(Language.ENGLISH(), "test");
		TreeNode<Distribution> rootElement = new TreeNode<Distribution>();
		List<TreeNode<Distribution>> children = new ArrayList<TreeNode<Distribution>>();
		
		rootElement.setData(data);
		rootElement.setChildren(children);
		setRootElement(rootElement);
	}
	
    public boolean containsChild(TreeNode<Distribution> root, TreeNode<Distribution> treeNode){
  	   boolean result = false; 	   
  	   Iterator<TreeNode<Distribution>> it = root.getChildren().iterator();
  	   while (it.hasNext() && !result) {
  		   TreeNode<Distribution> node = (TreeNode<Distribution>) it.next();
  		   if (node.getData().equalsForTree(treeNode.getData())) {
  			   result = true;
  		   }		
  	   }
  	   /*
  	   while (!result && it.hasNext()) {
 		     if (it.next().data.equalsForTree(treeNode.data)){
 		    	 result = true;
 		     }
  	   }
  	   */
  	   return result;
     }
    
	public TreeNode<Distribution> getChild(TreeNode<Distribution> root, TreeNode<Distribution> TreeNode) {
		boolean found = false;
		TreeNode<Distribution> result = null;
		Iterator<TreeNode<Distribution>> it = root.children.iterator();
		while (!found && it.hasNext()) {
			result = (TreeNode<Distribution>) it.next();
			if (result.data.equalsForTree(TreeNode.data)){
				found = true;						
			}
		}
		if (!found){
			try {
				throw new Exception("The node was not found in among children and that is a precondition of getChild(node) method");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	private List<Distribution> orderDistributionsByLevel(List<Distribution> distList){
		boolean flag = true;
		int length = distList.size()-1;
		Distribution dist;
		List<Distribution> orderedList = new ArrayList<Distribution>(length);
		orderedList.addAll(distList);
		
		for (int i = 0; i < length && flag; i++) {
			flag = false;
			for (int j = 0; j < length-1; j++) {
				String level1 = orderedList.get(j).getArea().getLevel().toString();
				String level2 = orderedList.get(j+1).getArea().getLevel().toString();
				//if level from j+1 is greater than level from j
				if (level2.compareTo(
						level1) < 0) {
					dist = orderedList.get(j);
					orderedList.set(j, orderedList.get(j+1));
					orderedList.set(j+1, dist);
					flag = true;					
				}
			}
		}
		return orderedList;
	}
	
	public void merge(List<Distribution> distList, Set<NamedAreaLevel> omitLevels){
		List<Distribution> orderedDistList = orderDistributionsByLevel(distList);
	
		for (Distribution distribution : orderedDistList) {
			List<NamedArea> levelList = 
				this.getAreaLevelPathList(distribution.getArea(), omitLevels);
			mergeAux(distribution, distribution.getArea().getLevel(), levelList, this.getRootElement());
		}
	}
	
	public void sortChildren(){
		sortChildrenAux(this.getRootElement());
	}
	
	private void sortChildrenAux(TreeNode<Distribution> treeNode){
		DistributionNodeComparator comp = new DistributionNodeComparator();
		if (treeNode.children == null) {
			//nothing => stop condition
			return;
		}else {
			Collections.sort(treeNode.children, comp);
			for (TreeNode<Distribution> child : treeNode.children) {
				sortChildrenAux(child);
			}
		}
	}
	
	private void mergeAux(Distribution distribution,
						  NamedAreaLevel level,
				 		  List<NamedArea> areaHierarchieList, 
				 		  TreeNode<Distribution> root){
		TreeNode<Distribution> highestDistNode;
		TreeNode<Distribution> child;// the new child to add or the child to follow through the tree
				
		//if the list to merge is empty finish the execution
		if (areaHierarchieList.isEmpty()) {
			return;
		}
		//getting the highest area and inserting it into the tree
		NamedArea highestArea = areaHierarchieList.get(0);
		//NamedAreaLevel highestAreaLevel = (NamedAreaLevel) HibernateProxyHelper.deproxy(highestArea.getLevel());
		//NamedAreaLevel currentLevel = (NamedAreaLevel) HibernateProxyHelper.deproxy(level);
		//if (highestAreaLevel.compareTo(currentLevel) == 0){//if distribution.status is relevant
		
		if (highestArea.getLevel().getLabel().compareTo(level.getLabel()) == 0){
			highestDistNode = new TreeNode<Distribution>(distribution);//distribution.area comes from proxy!!!!
		}else{ //if distribution.status is not relevant
			Distribution data = Distribution.NewInstance(highestArea, null);
			highestDistNode = new TreeNode<Distribution>(data);
		}
		if(highestDistNode.data.getModifyingText().isEmpty()){
			highestDistNode.data.putModifyingText(Language.ENGLISH(), "test");
		}

		if (root.getChildren().isEmpty() || !containsChild(root, highestDistNode)) {
			//if the highest level is not on the depth-1 of the tree we add it.
			//child = highestDistNode;
			child = new TreeNode<Distribution>(highestDistNode.data);
			root.addChild(child);//child.getData().getArea().getUuid().toString().equals("8cfc1722-e1e8-49d3-95a7-9879de6de490");
		}else {
			//if the deepth-1 of the tree contains the highest area level
			//get the subtree or create it in order to continuing merging
			child = getChild(root,highestDistNode);
		}
		//continue merging with the next highest area of the list.
		List<NamedArea> newList = areaHierarchieList.subList(1, areaHierarchieList.size());
		mergeAux(distribution, level, newList, child);
	}
	
	private List<NamedArea> getAreaLevelPathList(NamedArea area, Set<NamedAreaLevel> omitLevels){
		List<NamedArea> result = new ArrayList<NamedArea>();
		if (omitLevels == null || !omitLevels.contains(area.getLevel())){
			result.add(area);		
		}
		while (area.getPartOf() != null) {
			area = area.getPartOf();
			if (omitLevels == null || !omitLevels.contains(area.getLevel())){
				result.add(0, area);
			}
		}
		return result;
	}
}
