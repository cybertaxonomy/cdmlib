/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.portal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.api.dto.portal.DistributionDto;
import eu.etaxonomy.cdm.api.dto.portal.DistributionTreeDto;
import eu.etaxonomy.cdm.api.dto.portal.NamedAreaDto;
import eu.etaxonomy.cdm.api.dto.portal.config.DistributionOrder;
import eu.etaxonomy.cdm.api.dto.portal.tmp.TermDto;
import eu.etaxonomy.cdm.api.dto.portal.tmp.TermNodeDto;
import eu.etaxonomy.cdm.api.dto.portal.tmp.TermTreeDto;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;

/**
 * @author a.mueller
 * @date 09.02.2023
 */
public class DistributionTreeDtoLoader {

    private static final Logger logger = LogManager.getLogger();

    private final IDefinedTermDao termDao;

    public DistributionTreeDtoLoader(IDefinedTermDao termDao){
        this.termDao = termDao;
    }

    public DistributionTreeDto load() {
        DistributionTreeDto dto = new DistributionTreeDto();
        TreeNode<Set<DistributionDto>, NamedAreaDto> rootElement = new TreeNode<>();
        List<TreeNode<Set<DistributionDto>, NamedAreaDto>> children = new ArrayList<>();
        rootElement.setChildren(children);
        dto.setRootElement(rootElement);
        return dto;
    }

    /**
     * Returns the (first) child node (of type TreeNode) with the given nodeID.
     * @return the found node or null
     */
    public TreeNode<Set<DistributionDto>, NamedAreaDto> findChildNode(TreeNode<Set<DistributionDto>,NamedAreaDto> parentNode, NamedAreaDto  nodeID) {
        if (parentNode.getChildren() == null) {
            return null;
        }

        for (TreeNode<Set<DistributionDto>, NamedAreaDto> node : parentNode.getChildren()) {
            if (node.getNodeId().getUuid().equals(nodeID.getUuid())) {
                return node;
            }
        }
        return null;
    }

  /**
   * Loading of distribution tree by minimized loading of areas. This methods loads only those areas
   * which are referenced by any distributions and from there up to the root area.
   * This happens by merging all these root paths.
   * However, this merging becomes more difficult once an area may have more then 1 path (e.g.
   * areas having >1 parent).
   * Therefore there is another loading algorithm {@link #orderAsTree2(DistributionTreeDto, Collection, TermTree, Set, Set, boolean)}
   * which works a bit different.
   *
   * @param parentAreaMap
   * @param fallbackAreaMarkerTypes
   *      Areas are fallback areas if they have a {@link Marker} with one of the specified
   *      {@link MarkerType marker types}.
   *      Areas identified as such are omitted from the hierarchy and the sub areas are moving one level up.
   *      This may not be the case if the fallback area has a distribution record itself AND if
   *      neverUseFallbackAreasAsParents is <code>false</code>.
   *      For more details on fall back areas see <b>Marked area filter</b> of
   *      {@link DescriptionUtility#filterDistributions(Collection, Set, boolean, boolean, boolean)}.
   * @param neverUseFallbackAreasAsParents
   *      if <code>true</code> a fallback area never has children even if a record exists for the area
   */
  public void orderAsTree(DistributionTreeDto dto, Collection<DistributionDto> distributions,
          SetMap<NamedAreaDto,NamedAreaDto> area2ParentAreaMap, Set<NamedAreaLevel> omitLevels,
          Set<UUID> fallbackAreaMarkerTypes,
          boolean neverUseFallbackAreasAsParents){

      //compute all areas
      Set<NamedAreaDto> relevantAreas = new HashSet<>(distributions.size());
      for (DistributionDto distribution : distributions) {
          relevantAreas.add(distribution.getArea());
      }
      // preload all areas which are a parent of another one, this is a performance improvement
//      loadAllParentAreasIntoSession(relevantAreas, parentAreaMap);

      Set<UUID> omitLevelIds = new HashSet<>(omitLevels.size());
      for(NamedAreaLevel level : omitLevels) {
          omitLevelIds.add(level.getUuid());
      }

      for (DistributionDto distribution : distributions) {
          // get path through area hierarchy
          List<NamedAreaDto> namedAreaPath = getAreaLevelPath(distribution.getArea(), area2ParentAreaMap,
                  omitLevelIds, relevantAreas, fallbackAreaMarkerTypes, neverUseFallbackAreasAsParents);
          addDistributionToSubTree(distribution, namedAreaPath, dto.getRootElement());
      }
  }

    /**
     * Alternative distribution tree loading which loads the complete area tree and
     * then removes those areas with no data or being fallback areas, etc.
     * This method allows multiple parents for an area.
     *
     * NOTE: this method handles fallback and other markers only on tree level,
     *       and not if attached to the area itself.
     *
     * @see #orderAsTree(DistributionTreeDto, Collection, SetMap, Set, Set, boolean)
     */
    public void orderAsTree2(DistributionTreeDto dto, Collection<DistributionDto> distributions,
            TermTreeDto areaTree, Set<NamedAreaLevel> omitLevels,
            Set<UUID> fallbackAreaMarkerTypes,
            boolean neverUseFallbackAreasAsParents){

        TreeNode<Set<DistributionDto>,NamedAreaDto> rootAreaNode = transformToDtoTree(areaTree.getRoot());
        dto.setRootElement(rootAreaNode);

        addDistributions(rootAreaNode, distributions);
        Set<UUID> omitLevelUuids = omitLevels.stream().map(e->e.getUuid()).collect(Collectors.toSet());
        removeEmptySubtrees(rootAreaNode);
        //TODO empty children should not be necessary anymore due to removeEmptySubtrees()
        removeFallbackAreasAndOmitLevelRecursive(rootAreaNode, fallbackAreaMarkerTypes,
                omitLevelUuids, neverUseFallbackAreasAsParents);
        //TODO deduplicate
        //TODO alternativeRootArea
        //TODO ...,
    }

    private void addDistributions(TreeNode<Set<DistributionDto>,NamedAreaDto> rootNode, Collection<DistributionDto> distributions) {

        //fill area2DistMap from distributions
        SetMap<UUID,DistributionDto> area2DistMap = new SetMap<>();
        for (DistributionDto distDto : distributions) {
            if (distDto.getArea() != null) {
                area2DistMap.putItem(distDto.getArea().getUuid(), distDto);
            }
        }

        //fill areaTree recursive
        addDistributionsRecursive(rootNode, area2DistMap);
    }

    private void addDistributionsRecursive(TreeNode<Set<DistributionDto>,NamedAreaDto> node, SetMap<UUID, DistributionDto> area2DistMap) {
        NamedAreaDto nodeId = node.getNodeId();
        if (nodeId != null && nodeId.getUuid() != null) {
            Set<DistributionDto> distDto = area2DistMap.get(nodeId.getUuid());
            node.setData(distDto);
        }
        for (TreeNode<Set<DistributionDto>,NamedAreaDto> child : node.getChildren()) {
            addDistributionsRecursive(child, area2DistMap);
        }
    }

    private void removeEmptySubtrees(TreeNode<Set<DistributionDto>, NamedAreaDto> rootNode) {
        List<TreeNode<Set<DistributionDto>, NamedAreaDto>> children = new ArrayList<>(rootNode.getChildren());
        for (TreeNode<Set<DistributionDto>,NamedAreaDto> child : children) {

            if (isEmptySubtree(child)) {
                rootNode.getChildren().remove(child);
            }else {
                if (logger.isDebugEnabled()) {logger.debug("Keep non-empty node: " + (child.getNodeId() == null ? "-" : child.getNodeId().toString()));}
                removeEmptySubtrees(child);
            }
        }
    }

    private boolean isEmptySubtree(TreeNode<Set<DistributionDto>, NamedAreaDto> node) {
        return (CdmUtils.isNullSafeEmpty(node.getData()) && !childrenHaveData(node));
    }

    private void removeFallbackAreasAndOmitLevelRecursive(TreeNode<Set<DistributionDto>,NamedAreaDto> rootNode,
            Set<UUID> fallbackAreaMarkerTypes,
            Set<UUID> omitLevelUuids, boolean neverUseFallbackAreasAsParents) {

        List<TreeNode<Set<DistributionDto>,NamedAreaDto>> children = new ArrayList<>(rootNode.getChildren());
        for (TreeNode<Set<DistributionDto>,NamedAreaDto> child : children) {
            List<TreeNode<Set<DistributionDto>,NamedAreaDto>> movedChildren = new ArrayList<>();
            if (isOmitLevel(child, omitLevelUuids)) {
                movedChildren = replaceInBetweenNode(rootNode, child, false);
            }else if (isFallback(child, fallbackAreaMarkerTypes, neverUseFallbackAreasAsParents)) {
                boolean isEmpty = CdmUtils.isNullSafeEmpty(child.getData()); //TODO only false with sources

                movedChildren = replaceInBetweenNode(rootNode, child, !isEmpty);
                movedChildren.stream().forEach(c->removeFallbackAreasAndOmitLevelRecursive(c, fallbackAreaMarkerTypes, omitLevelUuids, neverUseFallbackAreasAsParents));
            }else {
                if (logger.isDebugEnabled()) {logger.debug("Not to replace: " + (child.getNodeId() == null ? "-" : child.getNodeId().toString()));}
                removeFallbackAreasAndOmitLevelRecursive(child, fallbackAreaMarkerTypes, omitLevelUuids, neverUseFallbackAreasAsParents);
            }
            movedChildren.stream().forEach(c->removeFallbackAreasAndOmitLevelRecursive(c, fallbackAreaMarkerTypes, omitLevelUuids, neverUseFallbackAreasAsParents));
        }
    }

    private boolean isOmitLevel(TreeNode<Set<DistributionDto>,NamedAreaDto> treeNode,
            Set<UUID> omitLevelUuids) {
        //omit level
        NamedAreaDto areaNode = treeNode.getNodeId();
        UUID uuidLevel = areaNode.getLevelUuid();
        return omitLevelUuids.contains(uuidLevel);
    }

    private boolean isFallback(TreeNode<Set<DistributionDto>,NamedAreaDto> treeNode,
            Set<UUID> fallbackAreaMarkerTypes,
            boolean neverUseFallbackAreasAsParents) {

        NamedAreaDto areaNode = treeNode.getNodeId();

        //fall back and empty
        boolean isFallback = false;
        for (UUID mt : fallbackAreaMarkerTypes) {
            if (areaNode.hasMarker(mt)) {
                isFallback = true;
                break;
            }
        }

        //empty
        boolean isEmpty = CdmUtils.isNullSafeEmpty(treeNode.getData()); //TODO only false with sources

        if (isFallback){
            return neverUseFallbackAreasAsParents || (childrenHaveData(treeNode) && isEmpty);
        } else {
            //should not happen since empty subtrees are removed before
            return isEmpty && !childrenHaveData(treeNode);
        }
    }

    private boolean childrenHaveData(TreeNode<Set<DistributionDto>,NamedAreaDto> areaNode) {
        //TODO omit levels here?
        for (TreeNode<Set<DistributionDto>,NamedAreaDto> child : areaNode.getChildren()) {
            if (!CdmUtils.isNullSafeEmpty(child.getData())) {
                return true;
            }
            if (childrenHaveData(child)) {
                return true;
            }
        }
        return false;
    }

    private TreeNode<Set<DistributionDto>, NamedAreaDto> transformToDtoTree(TermNodeDto areaNode) {

        NamedAreaDto nodeId = (NamedAreaDto)areaNode.getTerm();
//        NamedAreaDto nodeId = NamedAreaDtoLoader.INSTANCE().fromEntity(area);

        Set<DistributionDto> data = new HashSet<>();
        TreeNode<Set<DistributionDto>, NamedAreaDto> treeNode = new TreeNode<>(nodeId, data);

        if (areaNode.getChildren() != null) {
            for (TermNodeDto childNode : areaNode.getChildren()) {
                TreeNode<Set<DistributionDto>, NamedAreaDto> child = transformToDtoTree(childNode);
                treeNode.addChild(child);
            }
        }
        return treeNode;
    }

  /**
   * This method will cause all parent areas to be loaded into the session cache so that
   * all initialization of the NamedArea term instances is ready. This improves the
   * performance of the tree building
   */
  //TODO still needed? If not, we can maybe also remove the termDao dependency
    //call is currently commented
  private void loadAllParentAreasIntoSession(Set<NamedAreaDto> areas,
          SetMap<NamedArea,NamedArea> parentAreaMap) {

      if (areas == null || parentAreaMap == null || termDao == null) {
          return;
      }
      Set<UUID> childAreas = new HashSet<>(areas.size());
      for(TermDto area : areas) {
          childAreas.add(area.getUuid());
      }

      List<NamedAreaDto> parentAreas = null;
      if(!childAreas.isEmpty()) {
          parentAreas = termDao.getPartOfNamedAreas(childAreas, parentAreaMap);
          childAreas.clear();
//          cdhildAreas.addAll(parentAreas);
      }
  }

  public void recursiveSortChildren(DistributionTreeDto dto, DistributionOrder distributionOrder){
      if (distributionOrder == null){
          distributionOrder = DistributionOrder.getDefault();
      }
      innerRecursiveSortChildren(dto.getRootElement(), distributionOrder.getDtoComparator());
  }

  private void innerRecursiveSortChildren(TreeNode<Set<DistributionDto>, NamedAreaDto> treeNode,
          Comparator<TreeNode<Set<DistributionDto>, NamedAreaDto>> comparator){

      if (treeNode.children == null) {
          //nothing => stop condition
          return;
      }else {
          Collections.sort(treeNode.getChildren(), comparator);
          for (TreeNode<Set<DistributionDto>, NamedAreaDto> child : treeNode.getChildren()) {
              innerRecursiveSortChildren(child, comparator);
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
  private void addDistributionToSubTree(DistributionDto distribution,
          List<NamedAreaDto> namedAreaPath,
          TreeNode<Set<DistributionDto>,NamedAreaDto> root){

      //if the list to merge is empty finish the execution
      if (namedAreaPath.isEmpty()) {
          return;
      }

      //getting the highest area and inserting it into the tree
      NamedAreaDto highestArea = namedAreaPath.get(0);

      TreeNode<Set<DistributionDto>, NamedAreaDto> child = findChildNode(root, highestArea);
      if (child == null) {
          // the highestDistNode is not yet in the set of children, so we add it
          child = new TreeNode<>(highestArea);
          child.setData(new HashSet<>());
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
      List<NamedAreaDto> newList = namedAreaPath.subList(1, namedAreaPath.size());
      addDistributionToSubTree(distribution, newList, child);
  }

  /**
   * Returns the path through the area hierarchy from the root area to the <code>area</code> given as parameter.<BR><BR>
   *
   * Areas for which no distribution data is available and which are marked as hidden are omitted, see #5112
   *
   * @param area
 * @param parentAreaMap
   * @param distributionAreas the areas for which distribution data exists (after filtering by
   *  {@link eu.etaxonomy.cdm.api.service.geo.DescriptionUtility#filterDistributions()} )
   * @param fallbackAreaMarkerTypes
   *      Areas not associated to a Distribution in the {@code distList} are detected as fallback area
   *      if they are having a {@link Marker} with one of the specified {@link MarkerType}s. Areas identified as such
   *      are omitted. For more details on fall back areas see <b>Marked area filter</b> of
   *      {@link DescriptionUtility#filterDistributions(Collection, Set, boolean, boolean, boolean)}.
   * @param omitLevels
   * @return the path through the area hierarchy
   */
  private List<NamedAreaDto> getAreaLevelPath(NamedAreaDto area,
          SetMap<NamedAreaDto,NamedAreaDto> area2parentAreaMap,
          Set<UUID> omitLevelIds,
          Set<NamedAreaDto> distributionAreas,
          Set<UUID> fallbackAreaMarkerTypes,
          boolean neverUseFallbackAreasAsParents){

      List<NamedAreaDto> path = new ArrayList<>();
      if (!matchesLevels(area, omitLevelIds)){
          path.add(area);
      }

      while (area2parentAreaMap.get(area) != null) {
          area = getPreferredParent(area2parentAreaMap.get(area), fallbackAreaMarkerTypes);  //new parent
          //omit omit-levels
          if (area != null && !matchesLevels(area, omitLevelIds)){
              if(!isFallback(fallbackAreaMarkerTypes, area)
                      || (distributionAreas.contains(area) && !neverUseFallbackAreasAsParents )
                      ) {
                  //add parent if it is not a fallback or if it is a fallback but data for this area exists and
                  //   the neverUse... parameter allows adding fallback areas in this case
                  path.add(0, area);
              } else {
                  if(logger.isDebugEnabled()) {logger.debug("positive fallback area detection, skipping " + area );}
              }
          }
      }
      return path;
  }

    /**
     * Most areas have only 1 parent. If they have >1 parent then usually
     * one of the parents is a fallback area which is not preferred.
     * If this is not the case for now we return an arbitrary parent.<BR>
     * Note: This method does not filter out fallback areas. It only gives
     *       them lower priority if >1 parents exist.
     */
    private NamedAreaDto getPreferredParent(Set<NamedAreaDto> parentSet, Set<UUID> fallbackAreas) {
        if (parentSet == null) {
            return null;
        }
        for (NamedAreaDto parent : parentSet) {
            boolean isFallback = false;
            for (UUID uuid : fallbackAreas) {
                if (parent.hasMarker(uuid)) {
                    isFallback = true;
                }
            }
            if (!isFallback) {
                return parent;
            }
        }
        return parentSet.stream().findAny().orElse(null);
    }

    private boolean isFallback(Set<UUID> fallbackAreaMarkerTypes, NamedAreaDto area) {

        //was: DescriptionUtility.isMarkedHidden(area, fallbackAreaMarkerTypes);
        return isMarkedAs(area, fallbackAreaMarkerTypes);
    }

    private static boolean isMarkedAs(NamedAreaDto area, Set<UUID> markerTypes) {
        if(markerTypes != null) {
            for(UUID markerType : markerTypes){
                if(area.hasMarker(markerType)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesLevels(NamedAreaDto area, Set<UUID> omitLevelIds) {
        if(omitLevelIds.isEmpty()) {
            return false;
        }
        UUID areaLevelUuid = area.getLevelUuid();
        return omitLevelIds.contains(areaLevelUuid);
    }

    public void handleAlternativeRootArea(DistributionTreeDto dto, Set<UUID> alternativeRootAreaMarkerTypes) {

        //don't do anything if no alternative area markers exist
        if (CdmUtils.isNullSafeEmpty(alternativeRootAreaMarkerTypes)) {
            return;
        }

        TreeNode<Set<DistributionDto>, NamedAreaDto> emptyRoot = dto.getRootElement();
        for(TreeNode<Set<DistributionDto>, NamedAreaDto> realRoot : emptyRoot.getChildren()) {
            boolean switched = false;
            if (CdmUtils.isNullSafeEmpty(realRoot.getData()) && realRoot.getNumberOfChildren() == 1) {
                //real root has no data and 1 child => potential candidate to be replaced by alternative root
                TreeNode<Set<DistributionDto>, NamedAreaDto> child = realRoot.getChildren().get(0);
                if (isMarkedAs(child.getNodeId(), alternativeRootAreaMarkerTypes)
                        && !CdmUtils.isNullSafeEmpty(child.getData())) {
                    //child is alternative root and has data => replace root by alternative root
                    emptyRoot.getChildren().remove(realRoot);
                    emptyRoot.addChild(child);
                }
            }
            if (!switched) {
                //if root has data or >1 children test if children are alternative roots with no data => remove
                Set<TreeNode<Set<DistributionDto>, NamedAreaDto>> children = new HashSet<>(realRoot.getChildren());
                for(TreeNode<Set<DistributionDto>, NamedAreaDto> child : children) {
                    if (isMarkedAs(child.getNodeId(), alternativeRootAreaMarkerTypes)
                            && CdmUtils.isNullSafeEmpty(child.getData())) {
                        replaceInBetweenNode(realRoot, child, false);
                    }
                }
            }
        }
    }

    private List<TreeNode<Set<DistributionDto>, NamedAreaDto>> replaceInBetweenNode(TreeNode<Set<DistributionDto>, NamedAreaDto> parent,
            TreeNode<Set<DistributionDto>, NamedAreaDto> inBetweenNode, boolean moveChildrenOnly) {

        List<TreeNode<Set<DistributionDto>, NamedAreaDto>> movedChildren = new ArrayList<>();
        for (TreeNode<Set<DistributionDto>, NamedAreaDto> child : inBetweenNode.getChildren()) {
            parent.addChild(child);
            movedChildren.add(child);
        }
        if (!moveChildrenOnly) {
            parent.getChildren().remove(inBetweenNode);
        }
        movedChildren.stream().forEach(c->inBetweenNode.children.remove(c));
        return movedChildren;
    }
}
