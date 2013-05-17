package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.common.Tree;
import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;

/**
 * @author f.revilla
 * @version 1.0
 * @created 10.06.2010
 *
 * @Deprecated unused class, remove?
 */
@Deprecated
public class NamedAreaTree extends Tree<NamedArea>{

    public NamedAreaTree() {
        NamedArea data = new NamedArea();
        TreeNode<NamedArea> rootElement = new TreeNode<NamedArea>();
        List<TreeNode<NamedArea>> children = new ArrayList<TreeNode<NamedArea>>();

        rootElement.setData(data);
        rootElement.setChildren(children);
        setRootElement(rootElement);
    }
/*
    public NamedAreaTree getHiearchieTree(List<NamedArea> areaList) {
        NamedAreaTree result = new NamedAreaTree();
        for (NamedArea area : areaList) {
            List<NamedArea> levelList = area.getAreaLevelPathList(area);
            result.merge(levelList);
        }
        return result;
    }
*/
    public void merge(List<NamedArea> areaList, Set<NamedAreaLevel> omitLevels){
        for (NamedArea area : areaList) {
            List<NamedArea> levelList = this.getAreaLevelPathList(area, omitLevels);
            mergeAux(levelList, this.getRootElement());
        }
    }

    public void sortChildren(){
        sortChildrenAux(this.getRootElement());
    }

    private void sortChildrenAux(TreeNode<NamedArea> treeNode){
        NamedAreaNodeComparator comp = new NamedAreaNodeComparator();
        if (treeNode.children == null){
            //nothing => stop condition
            return;
        }else{
            Collections.sort(treeNode.children, comp);
            for (TreeNode<NamedArea> child : treeNode.children) {
                sortChildrenAux(child);
            }
        }
    }

    private void mergeAux(List<NamedArea> areaHierarchieList, TreeNode<NamedArea> root) {
        TreeNode<NamedArea> child; // the new child to add or the child to follow through the tree
        //if the list to merge is empty finish the execution
        if (areaHierarchieList.isEmpty()){
            return;
        }
        //getting the highest area and inserting it into the tree
        NamedArea highestArea = areaHierarchieList.get(0);
        TreeNode<NamedArea> highestAreaNode = new TreeNode<NamedArea>(highestArea);
        //NamedAreaLevel level = highestArea.getLevel();
        //List<TreeNode<NamedArea>> children = root.getChildren();

        //if(children == null || !children.contains(highestAreaNode)){
        if (root.getChildren().isEmpty() || !root.containsChild(highestAreaNode)){
            //if the highest level is not on the depth-1 of the tree we add it.
            child = new TreeNode<NamedArea>(highestArea);
            root.addChild(child);
            //children.add(child);
        }else{
            //if the deepth-1 of the tree contains the highest area level
            //get the subtree or create it in order to continuing merging
            child = root.getChild(highestAreaNode);
        }
        //continue merging with the next highest area of the list.
        List<NamedArea> newList = areaHierarchieList.subList(1, areaHierarchieList.size());
        mergeAux(newList, child);
    }

    private List<NamedArea> getAreaLevelPathList(NamedArea area, Set<NamedAreaLevel> omitLevels){
        List<NamedArea> result = new ArrayList<NamedArea>();
        if (!omitLevels.contains(area.getLevel())){
            result.add(area);
        }
        while (area.getPartOf() != null) {
            area = area.getPartOf();
            if (!omitLevels.contains(area.getLevel())){
                result.add(0, area);
            }
        }
        return result;
    }
}
