/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.geo;

import java.util.Set;

import eu.etaxonomy.cdm.api.dto.portal.IDistributionTree;
import eu.etaxonomy.cdm.common.Tree;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * Tree implementing {@link Tree} for {@link Distribution} and {@link NamedArea}
 *
 * There is a somehow similar implementation in {@link eu.etaxonomy.cdm.model.location.NamedArea}
 */
public class DistributionTree
        extends Tree<Set<Distribution>, NamedArea>
        implements IDistributionTree {

//    private static final Logger logger = LogManager.getLogger();
//
//    private final IDefinedTermDao termDao;
//
//    public DistributionTree(IDefinedTermDao termDao){
//        TreeNode<Set<Distribution>, NamedArea> rootElement = new TreeNode<>();
//        List<TreeNode<Set<Distribution>, NamedArea>> children = new ArrayList<>();
//        rootElement.setChildren(children);
//        setRootElement(rootElement);
//        this.termDao = termDao;
//    }
//
//    /**
//     * @param parentAreaMap
//     * @param fallbackAreaMarkerTypes
//     *      Areas are fallback areas if they have a {@link Marker} with one of the specified
//     *      {@link MarkerType marker types}.
//     *      Areas identified as such are omitted from the hierarchy and the sub areas are moving one level up.
//     *      This may not be the case if the fallback area has a distribution record itself AND if
//     *      neverUseFallbackAreasAsParents is <code>false</code>.
//     *      For more details on fall back areas see <b>Marked area filter</b> of
//     *      {@link DescriptionUtility#filterDistributions(Collection, Set, boolean, boolean, boolean)}.
//     * @param neverUseFallbackAreasAsParents
//     *      if <code>true</code> a fallback area never has children even if a record exists for the area
//     */
//    public void orderAsTreeCAN_BE_REMOVED(Collection<Distribution> distributions,
//            SetMap<NamedArea, NamedArea> parentAreaMap, Set<NamedAreaLevel> omitLevels,
//            Set<MarkerType> fallbackAreaMarkerTypes,
//            boolean neverUseFallbackAreasAsParents){
//
//        //compute all areas
//        Set<NamedArea> areas = new HashSet<>(distributions.size());
//        for (Distribution distribution : distributions) {
//            areas.add(distribution.getArea());
//        }
//        // preload all areas which are a parent of another one, this is a performance improvement
//        loadAllParentAreasIntoSession_CAN_BE_REMOVED(areas);
//
//        Set<Integer> omitLevelIds = new HashSet<>(omitLevels.size());
//        for(NamedAreaLevel level : omitLevels) {
//            omitLevelIds.add(level.getId());
//        }
//
//        for (Distribution distribution : distributions) {
//            // get path through area hierarchy
//            List<NamedArea> namedAreaPath = getAreaLevelPath(distribution.getArea(), parentAreaMap, omitLevelIds,
//                    areas, fallbackAreaMarkerTypes, neverUseFallbackAreasAsParents);
//            addDistributionToSubTree(distribution, namedAreaPath, this.getRootElement());
//        }
//    }
//
//    /**
//     * This method will cause all parent areas to be loaded into the session cache so that
//     * all initialization of the NamedArea term instances is ready. This improves the
//     * performance of the tree building
//     */
//    private void loadAllParentAreasIntoSession_CAN_BE_REMOVED(Set<NamedArea> areas) {
//
//        List<NamedArea> parentAreas = null;
//        Set<NamedArea> childAreas = new HashSet<>(areas.size());
//        for(NamedArea areaProxy : areas) {
//            NamedArea deproxied = HibernateProxyHelper.deproxy(areaProxy);
//            childAreas.add(deproxied);
//        }
//
//        if(!childAreas.isEmpty()) {
//            parentAreas = termDao.getPartOf(childAreas, null, null, null);
//            childAreas.clear();
//            childAreas.addAll(parentAreas);
//        }
//    }
//
//    public void handleAlternativeRootArea_CAN_BE_REMOVED(Set<MarkerType> alternativeRootAreaMarkerTypes) {
//        //don't anything if no alternative area markers exist
//        if (CdmUtils.isNullSafeEmpty(alternativeRootAreaMarkerTypes)) {
//            return;
//        }
//        TreeNode<Set<Distribution>, NamedArea> emptyRoot = this.getRootElement();
//
//        for(TreeNode<Set<Distribution>, NamedArea> realRoot : emptyRoot.getChildren()) {
//            boolean switched = false;
//            if (CdmUtils.isNullSafeEmpty(realRoot.getData()) && realRoot.getNumberOfChildren() == 1) {
//                //real root has no data and 1 child => potential candidate to be replaced by alternative root
//                TreeNode<Set<Distribution>, NamedArea> child = realRoot.getChildren().get(0);
//                if (DistributionServiceUtilities.isMarkedAs(child.getNodeId(), alternativeRootAreaMarkerTypes)
//                        && !CdmUtils.isNullSafeEmpty(child.getData())) {
//                    //child is alternative root and has data => replace root by alternative root
//                    emptyRoot.getChildren().remove(realRoot);
//                    emptyRoot.addChild(child);
//                    switched = true;
//                }
//            }
//            if (!switched) {
//                //if root has data or >1 children test if children are alternative roots with no data => remove
//                Set<TreeNode<Set<Distribution>, NamedArea>> children = new HashSet<>(realRoot.getChildren());
//                for(TreeNode<Set<Distribution>, NamedArea> child : children) {
//                    if (DistributionServiceUtilities.isMarkedAs(child.getNodeId(), alternativeRootAreaMarkerTypes)
//                            && CdmUtils.isNullSafeEmpty(child.getData())) {
//                        replaceInBetweenNode(realRoot, child);
//                    }
//                }
//            }
//        }
//    }
//
//    private void replaceInBetweenNode(TreeNode<Set<Distribution>, NamedArea> parent,
//            TreeNode<Set<Distribution>, NamedArea> inBetweenNode) {
//        for (TreeNode<Set<Distribution>, NamedArea> child : inBetweenNode.getChildren()) {
//            parent.addChild(child);
//        }
//        parent.getChildren().remove(inBetweenNode);
//    }
//
//    public void recursiveSortChildren_CAN_BE_REMOVED(DistributionOrder distributionOrder){
//        if (distributionOrder == null){
//            distributionOrder = DistributionOrder.getDefault();
//        }
//        innerRecursiveSortChildren(this.getRootElement(), distributionOrder.getComparator());
//    }
//
//    private void innerRecursiveSortChildren(TreeNode<Set<Distribution>, NamedArea> treeNode,
//            Comparator<TreeNode<Set<Distribution>, NamedArea>> comparator){
//
//        if (treeNode.children == null) {
//            //nothing => stop condition
//            return;
//        }else {
//            Collections.sort(treeNode.getChildren(), comparator);
//            for (TreeNode<Set<Distribution>, NamedArea> child : treeNode.getChildren()) {
//                innerRecursiveSortChildren(child, comparator);
//            }
//        }
//    }
//
//    /**
//     * Adds the given <code>distributionElement</code> to the sub tree defined by
//     * the <code>root</code>.
//     *
//     * @param distribution
//     *            the {@link Distribution} to add to the tree at the position
//     *            according to the NamedArea hierarchy.
//     * @param namedAreaPath
//     *            the path to the root of the NamedArea hierarchy starting the
//     *            area used in the given <code>distributionElement</code>. The
//     *            hierarchy is defined by the {@link NamedArea#getPartOf()}
//     *            relationships
//     * @param root
//     *            root element of the sub tree to which the
//     *            <code>distributionElement</code> is to be added
//     */
//    private void addDistributionToSubTree(Distribution distribution,
//            List<NamedArea> namedAreaPath,
//            TreeNode<Set<Distribution>, NamedArea> root){
//
//        //if the list to merge is empty finish the execution
//        if (namedAreaPath.isEmpty()) {
//            return;
//        }
//
//        //getting the highest area and inserting it into the tree
//        NamedArea highestArea = namedAreaPath.get(0);
//
//        TreeNode<Set<Distribution>, NamedArea> child = root.findChildNode(highestArea);
//        if (child == null) {
//            // the highestDistNode is not yet in the set of children, so we add it
//            child = new TreeNode<Set<Distribution>, NamedArea>(highestArea);
//            child.setData(new HashSet<>());
//            root.addChild(child);
//        }
//
//        // add another element to the list of data
//        if(namedAreaPath.get(0).equals(distribution.getArea())){
//            if(namedAreaPath.size() > 1){
//                logger.error("there seems to be something wrong with the area hierarchy");
//            }
//            child.getData().add(distribution);
//            return; // done!
//        }
//
//        // Recursively proceed into the namedAreaPath to merge the next node
//        List<NamedArea> newList = namedAreaPath.subList(1, namedAreaPath.size());
//        addDistributionToSubTree(distribution, newList, child);
//    }
//
//    /**
//     * Returns the path through the area hierarchy from the root area to the <code>area</code> given as parameter.<BR><BR>
//     *
//     * Areas for which no distribution data is available and which are marked as hidden are omitted, see #5112
//     *
//     * @param area the area to get the path for
//     * @param distributionAreas the areas for which distribution data exists (after filtering by
//     *  {@link eu.etaxonomy.cdm.api.service.geo.DescriptionUtility#filterDistributions()} )
//     * @param fallbackAreaMarkerTypes
//     *      Areas not associated to a Distribution in the {@code distList} are detected as fallback area
//     *      if they are having a {@link Marker} with one of the specified {@link MarkerType}s. Areas identified as such
//     *      are omitted. For more details on fall back areas see <b>Marked area filter</b> of
//     *      {@link DescriptionUtility#filterDistributions(Collection, Set, boolean, boolean, boolean)}.
//     * @param omitLevels
//     * @return the path through the area hierarchy
//     */
//    private List<NamedArea> getAreaLevelPath(NamedArea area, SetMap<NamedArea, NamedArea> parentAreaMap,
//            Set<Integer> omitLevelIds, Set<NamedArea> distributionAreas,
//            Set<MarkerType> fallbackAreaMarkerTypes,
//            boolean neverUseFallbackAreasAsParents){
//
//        List<NamedArea> result = new ArrayList<>();
//        if (!matchesLevels(area, omitLevelIds)){
//            result.add(area);
//        }
//
//        while (parentAreaMap.getFirstValue(area) != null) {  //handle >1 parents
//            area = parentAreaMap.getFirstValue(area);
//            if (!matchesLevels(area, omitLevelIds)){
//                if(!isFallback(fallbackAreaMarkerTypes, area) ||
//                        (distributionAreas.contains(area) && !neverUseFallbackAreasAsParents ) ) {
//                    result.add(0, area);
//                } else {
//                    if(logger.isDebugEnabled()) {logger.debug("positive fallback area detection, skipping " + area );}
//                }
//            }
//        }
//
//        return result;
//    }
//
//    private boolean isFallback(Set<MarkerType> fallbackAreaMarkerTypes, NamedArea area) {
//        return DistributionServiceUtilities.isMarkedAs(area, fallbackAreaMarkerTypes);
//    }
//
//    private boolean matchesLevels(NamedArea area, Set<Integer> omitLevelIds) {
//        if(omitLevelIds.isEmpty()) {
//            return false;
//        }
//        Serializable areaLevelId;
//        NamedAreaLevel areaLevel = area.getLevel();
//        if (areaLevel instanceof HibernateProxy) {
//            areaLevelId = ((HibernateProxy) areaLevel).getHibernateLazyInitializer().getIdentifier();
//        } else {
//            areaLevelId = areaLevel==null ? null : areaLevel.getId();
//        }
//        return omitLevelIds.contains(areaLevelId);
//    }
}