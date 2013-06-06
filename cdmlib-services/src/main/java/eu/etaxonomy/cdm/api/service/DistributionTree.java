/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.Tree;
import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;

/**
 * TODO javadoc.
 * 
 * There is a somehow similar implementation in {@link eu.etaxonomy.cdm.model.location.NamedArea} 
 */
public class DistributionTree extends Tree<Distribution>{

    public static final Logger logger = Logger.getLogger(DistributionTree.class);

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
             TreeNode<Distribution> node = it.next();
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
            result = it.next();
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

        if(distList == null){
            distList = new ArrayList<Distribution>();
        }
        if(distList.size() == 0){
            return distList;
        }

        Distribution dist;
        List<Distribution> orderedList = new ArrayList<Distribution>(distList.size());
        orderedList.addAll(distList);

        int length = -1;
        boolean flag = true;
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

    /**
     * @param distList
     * @param omitLevels
     */
    public void orderAsTree(List<Distribution> distList, Set<NamedAreaLevel> omitLevels){

        List<Distribution> orderedDistList = orderDistributionsByLevel(distList);

        for (Distribution distributionElement : orderedDistList) {
            // get path through area hierarchy
            List<NamedArea> namedAreaPath = getAreaLevelPath(distributionElement.getArea(), omitLevels);
            // order by merging
            mergeAux(distributionElement, namedAreaPath, this.getRootElement());
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

    private void mergeAux(Distribution distributionElement, List<NamedArea> namedAreaPath, TreeNode<Distribution> root){

        TreeNode<Distribution> highestDistNode;
        TreeNode<Distribution> child;// the new child to add or the child to follow through the tree

         //if the list to merge is empty finish the execution
        if (namedAreaPath.isEmpty()) {
            return;
        }

        //getting the highest area and inserting it into the tree
        NamedArea highestArea = namedAreaPath.get(0);

        boolean isOnTop = false;
        if(distributionElement.getArea().getLevel() == null) {
            // is level is null compare by area only
//            isOnTop = distributionElement.getArea().getUuid().equals(highestArea.getUuid());
            isOnTop = false;
        } else {
            // otherwise compare by level
            isOnTop = highestArea.getLevel().getUuid().equals((distributionElement.getArea().getLevel().getUuid()));
        }

        if (isOnTop) {
            highestDistNode = new TreeNode<Distribution>(distributionElement); //distribution.area comes from proxy!!!!
        }else{
            //if distribution.status is not relevant
            Distribution dummyDistributionElement = Distribution.NewInstance(highestArea, null);
            highestDistNode = new TreeNode<Distribution>(dummyDistributionElement);
        }

        if (root.getChildren().isEmpty() || !containsChild(root, highestDistNode)) {
            //if the highest level is not on the depth-1 of the tree we add it.
            //child = highestDistNode;
            child = new TreeNode<Distribution>(highestDistNode.data);
            root.addChild(child);//child.getData().getArea().getUuid().toString().equals("8cfc1722-e1e8-49d3-95a7-9879de6de490");
        }else {
            //if the depth-1 of the tree contains the highest area level
            //get the subtree or create it in order to continuing merging
            child = getChild(root,highestDistNode);
        }
        //continue merging with the next highest area of the list.
        List<NamedArea> newList = namedAreaPath.subList(1, namedAreaPath.size());
        mergeAux(distributionElement, newList, child);
    }

    /**
     * @param area
     * @param omitLevels
     * @return the path through area hierarchy from the <code>area</code> given as parameter to the root
     */
    private List<NamedArea> getAreaLevelPath(NamedArea area, Set<NamedAreaLevel> omitLevels){
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
