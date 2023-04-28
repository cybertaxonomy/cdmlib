/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.portal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.proxy.HibernateProxy;

import eu.etaxonomy.cdm.api.dto.portal.DistributionDto;
import eu.etaxonomy.cdm.api.dto.portal.DistributionTreeDto;
import eu.etaxonomy.cdm.api.dto.portal.LabeledEntityDto;
import eu.etaxonomy.cdm.api.dto.portal.NamedAreaDto;
import eu.etaxonomy.cdm.api.dto.portal.config.DistributionOrder;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
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
    public TreeNode<Set<DistributionDto>, NamedAreaDto> findChildNode(TreeNode<Set<DistributionDto>, NamedAreaDto> parentNode, NamedAreaDto  nodeID) {
        if (parentNode.getChildren() == null) {
            return null;
        }

        for (TreeNode<Set<DistributionDto>, NamedAreaDto> node : parentNode.getChildren()) {
            if (node.getNodeId().equals(nodeID)) {
                return node;
            }
        }
        return null;
    }

  /**
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
          SetMap<NamedArea, NamedArea> parentAreaMap, Set<NamedAreaLevel> omitLevels,
          Set<MarkerType> fallbackAreaMarkerTypes,
          boolean neverUseFallbackAreasAsParents){

      //compute all areas
      Set<NamedAreaDto> relevantAreas = new HashSet<>(distributions.size());
      for (DistributionDto distribution : distributions) {
          relevantAreas.add(distribution.getArea());
      }
      // preload all areas which are a parent of another one, this is a performance improvement
      loadAllParentAreasIntoSession(relevantAreas, parentAreaMap);

      Set<Integer> omitLevelIds = new HashSet<>(omitLevels.size());
      for(NamedAreaLevel level : omitLevels) {
          omitLevelIds.add(level.getId());
      }

      for (DistributionDto distribution : distributions) {
          // get path through area hierarchy
          List<NamedAreaDto> namedAreaPath = getAreaLevelPath(distribution.getArea(), parentAreaMap,
                  omitLevelIds, relevantAreas, fallbackAreaMarkerTypes, neverUseFallbackAreasAsParents);
          addDistributionToSubTree(distribution, namedAreaPath, dto.getRootElement());
      }
  }

  /**
   * This method will cause all parent areas to be loaded into the session cache so that
   * all initialization of the NamedArea term instances is ready. This improves the
   * performance of the tree building
   */
  private void loadAllParentAreasIntoSession(Set<NamedAreaDto> areas, SetMap<NamedArea, NamedArea> parentAreaMap) {

      List<NamedAreaDto> parentAreas = null;
      Set<UUID> childAreas = new HashSet<>(areas.size());
      for(NamedAreaDto area : areas) {
          childAreas.add(area.getUuid());
      }

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
          TreeNode<Set<DistributionDto>, NamedAreaDto> root){


      //if the list to merge is empty finish the execution
      if (namedAreaPath.isEmpty()) {
          return;
      }

      //getting the highest area and inserting it into the tree
      NamedAreaDto highestArea = namedAreaPath.get(0);

      TreeNode<Set<DistributionDto>, NamedAreaDto> child = findChildNode(root, highestArea);
      if (child == null) {
          // the highestDistNode is not yet in the set of children, so we add it
          child = new TreeNode<Set<DistributionDto>, NamedAreaDto>(highestArea);
          child.setData(new HashSet<DistributionDto>());
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
  private List<NamedAreaDto> getAreaLevelPath(NamedAreaDto area, SetMap<NamedArea, NamedArea> parentAreaMap,
          Set<Integer> omitLevelIds,
          Set<NamedAreaDto> distributionAreas, Set<MarkerType> fallbackAreaMarkerTypes,
          boolean neverUseFallbackAreasAsParents){

      List<NamedAreaDto> result = new ArrayList<>();
      if (!matchesLevels(area, omitLevelIds)){
          result.add(area);
      }

      if (parentAreaMap == null) { //TODO should this happen?
          while (area.getParent() != null) {
              area = area.getParent();
              if (!matchesLevels(area, omitLevelIds)){
                  if(!isFallback(fallbackAreaMarkerTypes, area) ||
                          (distributionAreas.contains(area) && !neverUseFallbackAreasAsParents ) ) {
                      result.add(0, area);
                  } else {
                      if(logger.isDebugEnabled()) {logger.debug("positive fallback area detection, skipping " + area );}
                  }
              }
          }
      } else {
          //FIXME same as above case, maybe we do not need to distinguish as parent handling is done
          // in NamedAreaDTO constructor
          while (area.getParent() != null) {
              area = area.getParent();
              if (!matchesLevels(area, omitLevelIds)){
                  if(!isFallback(fallbackAreaMarkerTypes, area) ||
                          (distributionAreas.contains(area) && !neverUseFallbackAreasAsParents ) ) {
                      result.add(0, area);
                  } else {
                      if(logger.isDebugEnabled()) {logger.debug("positive fallback area detection, skipping " + area );}
                  }
              }
          }

      }
      return result;
  }

  private boolean isFallback(Set<MarkerType> hiddenAreaMarkerTypes, NamedAreaDto area) {

      //was: DescriptionUtility.isMarkedHidden(area, hiddenAreaMarkerTypes);
      return isMarkedHidden(area, hiddenAreaMarkerTypes);
  }

  private static boolean isMarkedHidden(NamedAreaDto area, Set<MarkerType> hiddenAreaMarkerTypes) {
      if(hiddenAreaMarkerTypes != null) {
          for(MarkerType markerType : hiddenAreaMarkerTypes){
              if(area.hasMarker(markerType, true)){
                  return true;
              }
          }
      }
      return false;
  }

  private boolean matchesLevels(NamedAreaDto area, Set<Integer> omitLevelIds) {
      if(omitLevelIds.isEmpty()) {
          return false;
      }
      Serializable areaLevelId;
      LabeledEntityDto areaLevel = area.getLevel();
      //TODO remove Proxy check
      if (areaLevel instanceof HibernateProxy) {
          areaLevelId = ((HibernateProxy) areaLevel).getHibernateLazyInitializer().getIdentifier();
      } else {
          areaLevelId = areaLevel==null ? null : areaLevel.getId();
      }
      return omitLevelIds.contains(areaLevelId);
  }
}
