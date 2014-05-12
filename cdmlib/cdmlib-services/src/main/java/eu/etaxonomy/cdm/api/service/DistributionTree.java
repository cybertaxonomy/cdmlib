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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.HibernateProxyHelper;

import eu.etaxonomy.cdm.common.Tree;
import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;

/**
 * TODO javadoc.
 *
 * There is a somehow similar implementation in {@link eu.etaxonomy.cdm.model.location.NamedArea}
 */
public class DistributionTree extends Tree<Set<Distribution>, NamedArea>{

    public static final Logger logger = Logger.getLogger(DistributionTree.class);

    public DistributionTree(){
        TreeNode<Set<Distribution>, NamedArea> rootElement = new TreeNode<Set<Distribution>, NamedArea>();
        List<TreeNode<Set<Distribution>, NamedArea>> children = new ArrayList<TreeNode<Set<Distribution>, NamedArea>>();
        rootElement.setChildren(children);
        setRootElement(rootElement);
    }

    /**
     * @param parentNode
     * @param nodeToFind
     * @return false if the node was not found
     */
    public boolean hasChildNode(TreeNode<Set<Distribution>, NamedArea> parentNode, NamedArea nodeID) {
        return findChildNode(parentNode, nodeID) != null;
    }

    /**
     * @param parentNode
     * @param nodeToFind
     * @return the found node or null
     */
    public TreeNode<Set<Distribution>, NamedArea> findChildNode(TreeNode<Set<Distribution>, NamedArea> parentNode, NamedArea  nodeID) {
        if (parentNode.getChildren() == null) {
            return null;
        }

        for (TreeNode<Set<Distribution>, NamedArea> node : parentNode.getChildren()) {
            if (node.getNodeId().equals(nodeID)) {
                return node;
            }
        }
        return null;
    }

    /**
     * @param distList
     * @param omitLevels
     */
    public void orderAsTree(Collection<Distribution> distList, Set<NamedAreaLevel> omitLevels){

        for (Distribution distribution : distList) {
            // get path through area hierarchy
            List<NamedArea> namedAreaPath = getAreaLevelPath(distribution.getArea(), omitLevels);
            addDistributionToSubTree(distribution, namedAreaPath, this.getRootElement());
        }
    }

    public void recursiveSortChildrenByLabel(){
        _recursiveSortChildrenByLabel(this.getRootElement());
    }

    private void _recursiveSortChildrenByLabel(TreeNode<Set<Distribution>, NamedArea> treeNode){
        DistributionNodeByAreaLabelComparator comp = new DistributionNodeByAreaLabelComparator();
        if (treeNode.children == null) {
            //nothing => stop condition
            return;
        }else {
            Collections.sort(treeNode.children, comp);
            for (TreeNode<Set<Distribution>, NamedArea> child : treeNode.children) {
                _recursiveSortChildrenByLabel(child);
            }
        }
    }

    /**
     * Adds the given <code>distributionElement</code> to the sub tree defined by
     * the <code>root</code>.
     *
     * @param distribution
     *            the {@link Distribution} to add to the tree at the position
     *            according to the NamedArea hierarchy.
     * @param namedAreaPath
     *            the path to the root of the NamedArea hierarchy starting the
     *            area used in the given <code>distributionElement</code>. The
     *            hierarchy is defined by the {@link NamedArea#getPartOf()}
     *            relationships
     * @param root
     *            root element of the sub tree to which the
     *            <code>distributionElement</code> is to be added
     */
    private void addDistributionToSubTree(Distribution distribution, List<NamedArea> namedAreaPath, TreeNode<Set<Distribution>, NamedArea> root){


        //if the list to merge is empty finish the execution
        if (namedAreaPath.isEmpty()) {
            return;
        }

        //getting the highest area and inserting it into the tree
        NamedArea highestArea = namedAreaPath.get(0);


        TreeNode<Set<Distribution>, NamedArea> child = findChildNode(root, highestArea);
        if (child == null) {
            // the highestDistNode is not yet in the set of children, so we add it
            child = new TreeNode<Set<Distribution>, NamedArea>(highestArea);
            child.setData(new HashSet<Distribution>());
            root.addChild(child);
        }

        // add another element to the list of data
        if(namedAreaPath.get(0).equals(distribution.getArea())){
            if(namedAreaPath.size() > 1){
                logger.error("there seems to be something wrong with the area hierarchy");
            }
            child.getData().add(distribution);
            return; // done!
        }

        // Recursively proceed into the namedAreaPath to merge the next node
        List<NamedArea> newList = namedAreaPath.subList(1, namedAreaPath.size());
        addDistributionToSubTree(distribution, newList, child);
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
        // logging special case in order to help solving ticket #3891 (ordered distributions provided by portal/description/${uuid}/DistributionTree randomly broken)

        if(area.getPartOf() == null) {
            StringBuilder hibernateInfo = new StringBuilder();
            hibernateInfo.append(", area is of type: ").append(area.getClass());
            if(area instanceof HibernateProxy){
                hibernateInfo.append(" target object is ").append(HibernateProxyHelper.getClassWithoutInitializingProxy(area));
            }
            logger.warn("area.partOf is NULL for " + area.getLabel() + hibernateInfo.toString());
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
