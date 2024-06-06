/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.geo;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.api.dto.portal.DistributionDto;
import eu.etaxonomy.cdm.api.dto.portal.DistributionInfoDto;
import eu.etaxonomy.cdm.api.dto.portal.DistributionInfoDto.InfoPart;
import eu.etaxonomy.cdm.api.dto.portal.IDistributionTree;
import eu.etaxonomy.cdm.api.dto.portal.NamedAreaDto;
import eu.etaxonomy.cdm.api.dto.portal.config.CondensedDistribution;
import eu.etaxonomy.cdm.api.dto.portal.config.CondensedDistributionConfiguration;
import eu.etaxonomy.cdm.api.dto.portal.config.DistributionInfoConfiguration;
import eu.etaxonomy.cdm.api.dto.portal.config.DistributionOrder;
import eu.etaxonomy.cdm.api.dto.portal.tmp.TermDto;
import eu.etaxonomy.cdm.api.dto.portal.tmp.TermNodeDto;
import eu.etaxonomy.cdm.api.dto.portal.tmp.TermTreeDto;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.portal.DistributionDtoLoader;
import eu.etaxonomy.cdm.api.service.portal.TermTreeDtoLoader;
import eu.etaxonomy.cdm.api.service.portal.format.CondensedDistributionComposer;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author muellera
 * @since 28.02.2024
 */
public class DistributionInfoBuilder {

    private static final Logger logger = LogManager.getLogger();

    private final boolean PREFER_AGGREGATED = true;
    private final boolean PREFER_SUBAREA = true;

    private List<Language> languages;
    private ICommonService commonService;

    private DistributionInfoConfiguration config;

    private TermTreeDto areaTree;

    public DistributionInfoBuilder(List<Language> languages, ICommonService commonService) {
        this.languages = languages;
        this.commonService = commonService;
    }

    public DistributionInfoDto build(DistributionInfoConfiguration config,
            Collection<Distribution> distributions,
            TermTree<NamedArea> areaTree,  //can be null
            TermTree<PresenceAbsenceTerm> statusTree, //can be null
            Map<UUID,Color> distributionStatusColorMap,
            IGeoServiceAreaMapping areaMapping){

        List<DistributionDto> distTmps = distributions.stream().map(d->toDistributionDto(d, config)).collect(Collectors.toList());
        TermTreeDto areaTreeDto = TermTreeDtoLoader.INSTANCE().fromEntity(areaTree);
        TermTreeDto statusTreeDto = TermTreeDtoLoader.INSTANCE().fromEntity(statusTree);

        return build(config, distributionStatusColorMap, distTmps, areaTreeDto,
                statusTreeDto, areaMapping);
    }

    //TODO shouldn't we use the loader instead?
    DistributionDto toDistributionDto(Distribution distribution, DistributionInfoConfiguration config) {

        DistributionDto dto = DistributionDtoLoader.INSTANCE().fromEntity(distribution, config);
        return dto;
    }

    public DistributionInfoDto buildFromDto(DistributionInfoConfiguration config,
            Collection<DistributionDto> distributions,
            TermTreeDto areaTree,  //can be null
            TermTreeDto statusTree, //can be null
            Map<UUID,Color> distributionStatusColorMap,
            IGeoServiceAreaMapping areaMapping) {

        return build(config, distributionStatusColorMap, distributions, areaTree, statusTree, areaMapping);
    }

    private DistributionInfoDto build(DistributionInfoConfiguration config,
            Map<UUID,Color> distributionStatusColorMap,
            Collection<DistributionDto> distributions,
            TermTreeDto areaTree,  //can be null
            TermTreeDto statusTree, //can be null
            IGeoServiceAreaMapping areaMapping) {

        this.config = config;

        EnumSet<DistributionInfoDto.InfoPart> parts = config.getInfoParts();
        boolean subAreaPreference = config.isPreferSubareas();
        boolean statusOrderPreference = config.isStatusOrderPreference();
        //TODO use uuid
        Set<MarkerType> fallbackAreaMarkerTypes = config.getFallbackAreaMarkerTypes();
        Set<UUID> fallbackAreaMarkerTypeUuids = fallbackAreaMarkerTypes.stream()
                .map(mt->mt.getUuid()).collect(Collectors.toSet());

        DistributionInfoDto dto = new DistributionInfoDto();

        areaTree = normalizeConfiguration(config, distributions, areaTree, fallbackAreaMarkerTypeUuids);

        //TODO unify to use only the node map
        SetMap<NamedAreaDto,NamedAreaDto> area2ParentAreaMap = TermTreeDtoLoader.getTerm2ParentMap(areaTree, NamedAreaDto.class);
        SetMap<NamedAreaDto,TermNodeDto> area2TermNodesMap = TermTreeDtoLoader.getTerm2NodeMap(areaTree, NamedAreaDto.class);

        // general filter
        // ... for all later applications apply the rules statusOrderPreference, hideHiddenArea
        //     and statusTree(statusFilter) to all distributions, but KEEP fallback area distributions
        boolean keepFallBackOnlyIfNoSubareaDataExists = false;
        Set<DistributionDto> filteredDistributions = filterDistributions(
                distributions, areaTree, statusTree, fallbackAreaMarkerTypeUuids, !PREFER_AGGREGATED,
                statusOrderPreference, !PREFER_SUBAREA, keepFallBackOnlyIfNoSubareaDataExists,
                area2TermNodesMap, area2ParentAreaMap);

        if(parts.contains(InfoPart.tree)) {
            IDistributionTree distributionTree = makeTree(filteredDistributions,
                    fallbackAreaMarkerTypeUuids, area2ParentAreaMap);
            dto.setTree(distributionTree);
        }

        if(parts.contains(InfoPart.condensedDistribution)) {
            CondensedDistributionConfiguration condensedDistConfig = config.getCondensedDistributionConfiguration();
            CondensedDistribution condensedDistribution = getCondensedDistribution(
                    filteredDistributions, area2TermNodesMap, condensedDistConfig, languages);
            dto.setCondensedDistribution(condensedDistribution);
        }

        if (parts.contains(InfoPart.mapUriParams)) {
            boolean IGNORE_STATUS_ORDER_PREF = false;
            Set<MarkerType> fallbackAreaMarkerType = null;
            // only apply the subAreaPreference rule for the maps
            keepFallBackOnlyIfNoSubareaDataExists = true;
            //this filters again, but this time with subarea preference rule and fallback area removal
            Set<DistributionDto> filteredMapDistributions = filterDistributions(
                    filteredDistributions, areaTree, statusTree, fallbackAreaMarkerTypeUuids, !PREFER_AGGREGATED,
                    IGNORE_STATUS_ORDER_PREF, subAreaPreference, keepFallBackOnlyIfNoSubareaDataExists,
                    area2TermNodesMap, area2ParentAreaMap);

            String mapUri = new AreaMapServiceParameterBuilder().build(
                    filteredMapDistributions,
                    areaMapping,
                    distributionStatusColorMap,
                    null, languages);
            dto.setMapUriParams(mapUri);
        }

        return dto;
    }

    private TermTreeDto normalizeConfiguration(DistributionInfoConfiguration config,
            Collection<DistributionDto> distributions, TermTreeDto areaTree, Set<UUID> fallbackAreaMarkerTypeUuids) {

        Set<UUID> omitLevels = config.getOmitLevels();
        if(omitLevels == null) {
            @SuppressWarnings("unchecked")  //2 lines to allow unchecked annotation
            Set<UUID> emptySet = Collections.EMPTY_SET;
            omitLevels = emptySet;
            config.setOmitLevels(omitLevels);
        }

        //area tree
        if (areaTree == null) {
            //TODO better use areaTree created within filterDistributions(...) but how to get it easily?
            areaTree = createAreaTreeByDistributions(distributions, fallbackAreaMarkerTypeUuids);
        }
        this.areaTree = areaTree;
        return areaTree;
    }

    private IDistributionTree makeTree(Set<DistributionDto> filteredDtoDistributions,
            Set<UUID> fallbackAreaMarkerTypes, SetMap<NamedAreaDto, NamedAreaDto> parentAreaMap) {

        IDistributionTree distributionTree;

        Set<UUID> omitLevels = config.getOmitLevels();
        //TODO use uuid
        Set<MarkerType> alternativeRootAreaMarkerTypes = config.getAlternativeRootAreaMarkerTypes();
        Set<UUID> alternativeRootAreaMarkerTypeUuids = alternativeRootAreaMarkerTypes.stream().map(mt->mt.getUuid()).collect(Collectors.toSet());
        boolean neverUseFallbackAreaAsParent = config.isNeverUseFallbackAreaAsParent();
        DistributionOrder distributionOrder = config.getDistributionOrder();

        boolean useSecondMethod = false;
        IDefinedTermDao termDao = null;  //FIXME was not null before, but don't find where it was passed
        distributionTree = DistributionServiceUtilities.buildOrderedTreeDto(omitLevels,
                filteredDtoDistributions, parentAreaMap, areaTree, fallbackAreaMarkerTypes,
                alternativeRootAreaMarkerTypeUuids, neverUseFallbackAreaAsParent,
                distributionOrder, termDao, useSecondMethod);
        return distributionTree;
    }

    /**
     * <b>NOTE: To avoid LayzyLoadingExceptions this method must be used in a transactional context.</b>
     *
     * Filters the given set of {@link Distribution}s for publication purposes.
     * The following rules are respected during the filtering:
     * <ol>
     * <li><b>Marked area filter</b>: Skip distributions for areas having a {@code TRUE} {@link Marker}
     * with one of the specified {@link MarkerType}s. Existing sub-areas of a marked area must also be marked
     * with the same marker type, otherwise the marked area acts as a <b>fallback area</b> for the sub areas.
     * An area is a <b>fallback area</b> if it is marked to be hidden and if it has at least one
     * sub area which is not marked to be hidden. The fallback area will be shown if there is no {@link Distribution}
     * for any of the non hidden sub-areas. For more detailed discussion on fallback areas see
     * https://dev.e-taxonomy.eu/redmine/issues/4408</li>
     *
     * <li><b>Prefer aggregated rule</b>: if this flag is set to <code>true</code> aggregated
     * distributions are preferred over non-aggregated elements.
     * (Aggregated descriptions are identified by their description having type
     * {@link DescriptionType.AGGREGATED_DISTRIBUTION}). This means if a non-aggregated status
     * information exists for the same area for which aggregated data is available,
     * the aggregated data has to be given preference over other data.
     * See parameter <code>preferAggregated</code></li>
     *
     * <li><b>Status order preference rule</b>: In case of multiple distribution
     * status ({@link PresenceAbsenceTermBase}) for the same area the status
     * with the highest order is preferred, see
     * {@link DefinedTermBase#compareTo(DefinedTermBase)}. This rule is
     * optional, see parameter <code>statusOrderPreference</code></li>
     *
     * <li><b>Sub area preference rule</b>: If there is an area with a <i>direct
     * sub area</i> and both areas have the same status only the
     * information on the sub area should be reported, whereas the super area
     * should be ignored. This rule is optional, see parameter
     * <code>subAreaPreference</code>. Can be run separately from the other filters.
     * This rule affects any distribution,
     * that is to be computed and edited equally. For more details see
     * {@link https://dev.e-taxonomy.eu/redmine/issues/5050})</li>
     * </ol>
     *
     * @param distributions
     *            the distributions to filter
     * @param fallbackAreaMarkerTypes
     *            distributions where the area has a {@link Marker} with one of the specified {@link MarkerType}s will
     *            be skipped or acts as fall back area. For more details see <b>Marked area filter</b> above.
     * @param preferAggregated
     *            Computed distributions for the same area will be preferred over edited distributions.
     *            <b>This parameter should always be set to <code>true</code>.</b>
     * @param statusOrderPreference
     *            enables the <b>Status order preference rule</b> if set to true,
     *            This rule can be run separately from the other filters.
     * @param subAreaPreference
     *            enables the <b>Sub area preference rule</b> if set to true
     * @param ignoreDistributionStatusUndefined
     *            workaround until #9500 is implemented
     * @return the filtered collection of distribution elements.
     */
    //TODO not private for testing
    Set<DistributionDto> filterDistributions(Collection<DistributionDto> distributions,
            TermTreeDto areaTree, TermTreeDto statusTree,
            Set<UUID> fallbackAreaMarkerTypes,
            boolean preferAggregated, boolean statusOrderPreference,
            boolean subAreaPreference, boolean keepFallBackOnlyIfNoSubareaDataExists,
            SetMap<NamedAreaDto,TermNodeDto> area2TermNodesMap,
            SetMap<NamedAreaDto,NamedAreaDto> area2ParentAreaMap) {

        SetMap<NamedAreaDto,DistributionDto> filteredDistributionsPerArea = new SetMap<>(distributions.size());

        Set<UUID> statusPositiveFilter = null;
        if (statusTree != null) {
            statusPositiveFilter = new HashSet<>();
            for (TermDto status : TermTreeDtoLoader.toList(statusTree, TermDto.class)) {
                statusPositiveFilter.add(status.getUuid());
            }
        }

        // map distributions to the area and apply status filter
        for(DistributionDto distribution : distributions){
            NamedAreaDto area = distribution.getArea();
            if(area == null) {
//                logger.debug("skipping distribution with NULL area");
                continue;
            }
            boolean removeStatus = statusPositiveFilter != null &&
                    (distribution.getStatus() == null
                      || !statusPositiveFilter.contains(distribution.getStatus().getUuid()));
            if (!removeStatus){
                filteredDistributionsPerArea.putItem(area, distribution);
            }
        }
        // remove?, as this is already part of the normalize() call earlier
        //.... but have look if area2TermNodesMap is needed
        if (areaTree == null) {
            areaTree = createAreaTreeByDistributions(distributions, fallbackAreaMarkerTypes);
            area2TermNodesMap = TermTreeDtoLoader.getTerm2NodeMap(areaTree, NamedAreaDto.class);
        }

        // -------------------------------------------------------------------
        // 1) skip distributions having an area with markers matching fallbackAreaMarkerTypes
        //    but keep distributions for fallback areas (areas with hidden marker, but with visible sub-areas)
        //TODO since using area tree this is only relevant if keepFallBackOnlyIfNoSubareaDataExists = true
        //     as the area tree should also exclude real hidden areas
//        if(!CdmUtils.isNullSafeEmpty(fallbackAreaMarkerTypes)) {
            removeHiddenAndKeepFallbackAreas(areaTree, area2TermNodesMap, fallbackAreaMarkerTypes,
                    filteredDistributionsPerArea, keepFallBackOnlyIfNoSubareaDataExists );
//        }

        // -------------------------------------------------------------------
        // 2) remove not computed distributions for areas for which computed
        //    distributions exists
        if(preferAggregated) {
            handlePreferAggregated(filteredDistributionsPerArea);
        }

        // -------------------------------------------------------------------
        // 3) status order preference rule
        if (statusOrderPreference) {
            SetMap<NamedAreaDto,DistributionDto> tmpMap = new SetMap<>(filteredDistributionsPerArea.size());
            for(NamedAreaDto key : filteredDistributionsPerArea.keySet()){
                tmpMap.put(key, filterByHighestDistributionStatusForArea(filteredDistributionsPerArea.get(key)));
            }
            filteredDistributionsPerArea = tmpMap;
        }

        // -------------------------------------------------------------------
        // 4) Sub area preference rule
        if(subAreaPreference){
            handleSubAreaPreferenceRule(filteredDistributionsPerArea, area2ParentAreaMap);
        }

        return valuesOfAllInnerSets(filteredDistributionsPerArea.values());
    }

    /**
     * Creates a term tree dto from the part-of(parentUUID) information of the distribution areas.
     * Removes hidden areas marked as such if they have no children.
     */
    private TermTreeDto createAreaTreeByDistributions(Collection<DistributionDto> distributions, Set<UUID> fallbackAreaMarkerTypeUuids) {

        //select all vocabulary IDs
        //select all distribution area IDs ?

        Set<UUID> vocabularyUuids = new HashSet<>();
        Set<Integer> distAreaIds = new HashSet<>();

        for (DistributionDto distribution : distributions) {
            NamedAreaDto area = distribution.getArea();
            if (area != null) {
               vocabularyUuids.add(area.getVocabularyUuid());
               distAreaIds.add(area.getId());
            }else {
                logger.warn("No area for distribution, distribution Id: " + distribution.getId());
            }
        }

        //use term tree loader.fromVocId to load the whole term tree
        //if >1 vocs(=trees) exist, create a super tree including all these tree's roots
        //  ... or use first voc as such a super tree and add others
        TermTreeDto termTree = TermTreeDtoLoader.loadVolatileFromVocabulary(commonService, vocabularyUuids);

        //alternatively we could load the voc tree by first loading only the relevant areas (including fallback areas)
        //... tree nodes and then via breadth-frist search there parents until all parents are loaded.
        //Then compute the tree with the computed nodes.
        //This might be recommended if there are only view distribution areas but large vocabularies.
        //It might be decided from case to case by first computing the voc size and
        //... compare it to the dist area size

        //This could be further refined by collecting hidden areas separately and after loading
        //... the tree removing those hidden area from the separate set which have been loaded
        //... anyway. For the remaining check if the have children or grandchildren not being
        //... hidden. If they have, load them to the resulting tree.

        return termTree;
    }

    private boolean isMarkedAs(NamedAreaDto area, Set<UUID> markerTypes) {
        if(markerTypes != null) {
            for(UUID markerType : markerTypes){
                if(area.hasMarker(markerType)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Remove areas not in area tree but keep fallback areas.
     */
    private void removeHiddenAndKeepFallbackAreas(TermTreeDto areaTree,
            SetMap<NamedAreaDto,TermNodeDto> area2TermNodesMap,
            Set<UUID> fallbackAreaMarkerTypes,
            SetMap<NamedAreaDto,DistributionDto> filteredDistributionsPerArea,
            boolean keepFallBackOnlyIfNoSubareaDataExists) {

        Set<NamedAreaDto> areasHiddenByMarker = new HashSet<>();
        List<NamedAreaDto> list = TermTreeDtoLoader.toList(areaTree, NamedAreaDto.class);
        Set<NamedAreaDto> availableAreas = new HashSet<>(list);

        for(NamedAreaDto area : filteredDistributionsPerArea.keySet()) {
            if (! availableAreas.contains(area)) {
                areasHiddenByMarker.add(area);
            }else if(isMarkedAs(area, fallbackAreaMarkerTypes)) {
                Set<TermNodeDto> nodes = area2TermNodesMap.get(area);

                // if at least one sub area is not hidden by a marker
                // the given area is a fall-back area for this sub area
                SetMap<NamedAreaDto,DistributionDto> distributionsForSubareaCheck = keepFallBackOnlyIfNoSubareaDataExists
                        ? filteredDistributionsPerArea
                        : null;
                boolean isFallBackArea = isRemainingFallBackArea(nodes, fallbackAreaMarkerTypes, distributionsForSubareaCheck);
                if (!isFallBackArea) {
                    // this area does not need to be shown as
                    // fall-back for another area so it will be hidden.
                    areasHiddenByMarker.add(area);
                }
            }
        }
        for(TermDto area :areasHiddenByMarker) {
            filteredDistributionsPerArea.remove(area);
        }
    }

    //if filteredDistributions == null it can be ignored if data exists or not
    private boolean isRemainingFallBackArea(Set<TermNodeDto> areaNode,
            Set<UUID> fallbackAreaMarkerTypes,
            SetMap<NamedAreaDto,DistributionDto> filteredDistributions) {

        Set<TermNodeDto> childNodes = new HashSet<>();
        areaNode.stream().filter(an->an.getChildren() != null)
            .forEach(an->childNodes.addAll(an.getChildren()));
        for(TermNodeDto included : childNodes) {
            NamedAreaDto subArea = (NamedAreaDto)included.getTerm();
            boolean noOrIgnoreData = filteredDistributions == null || !filteredDistributions.containsKey(subArea);

            Set<TermNodeDto> childNodeAsSet = new HashSet<>();
            childNodeAsSet.add(included);
            //if subarea is not hidden and data exists return true
            if (isMarkedAs(subArea, fallbackAreaMarkerTypes)){
                boolean subAreaIsFallback = isRemainingFallBackArea(childNodeAsSet, fallbackAreaMarkerTypes, filteredDistributions);
                if (subAreaIsFallback && noOrIgnoreData){
                    return true;
                }else{
                    continue;
                }
            }else{ //subarea not marked hidden
                if (noOrIgnoreData){
                    return true;
                }else{
                    continue;
                }
            }
        }
        return false;
    }

    private void handlePreferAggregated(SetMap<NamedAreaDto,DistributionDto> filteredDistributions) {

        SetMap<NamedAreaDto,DistributionDto> computedDistributions = new SetMap<>(filteredDistributions.size());
        SetMap<NamedAreaDto,DistributionDto> nonComputedDistributions = new SetMap<>(filteredDistributions.size());
        // separate computed and edited Distributions
        for (NamedAreaDto area : filteredDistributions.keySet()) {
            for (DistributionDto distribution : filteredDistributions.get(area)) {
                // this is only required for rule 1
                if(isAggregated(distribution)){
                    computedDistributions.putItem(area, distribution);
                } else {
                    nonComputedDistributions.putItem(area,distribution);
                }
            }
        }
        //remove nonComputed distributions for which computed distributions exist in the same area
        for(TermDto keyComputed : computedDistributions.keySet()){
            nonComputedDistributions.remove(keyComputed);
        }
        // combine computed and non computed Distributions again
        filteredDistributions.clear();
        for(NamedAreaDto area : computedDistributions.keySet()){
            filteredDistributions.put(area, computedDistributions.get(area));  //is it a problem that we use the same interal Set here?
        }
        for(NamedAreaDto area : nonComputedDistributions.keySet()){
            filteredDistributions.put(area, nonComputedDistributions.get(area));
        }
    }

    private boolean isAggregated(DistributionDto distribution) {
        return DescriptionBase.isAggregatedDistribution(distribution.getDescriptionType());
    }


    /**
     * Implements the Status order preference filter for a given set to Distributions.
     * The distributions should all be for the same area.
     * The method returns a site of distributions since multiple Distributions
     * with the same status are possible. For example if the same status has been
     * published in more than one literature references.
     *
     * @param distributions
     *
     * @return the set of distributions with the highest status
     */
    private Set<DistributionDto> filterByHighestDistributionStatusForArea(
            Set<DistributionDto> distributions){

        Set<DistributionDto> preferred = new HashSet<>();
        TermDto highestStatus = null;  //we need to leave generics here as for some reason highestStatus.compareTo later jumps into the wrong class for calling compareTo
        int compareResult;
        for (DistributionDto distribution : distributions) {
            if(highestStatus == null){
                highestStatus = distribution.getStatus();
                preferred.add(distribution);
            } else {
                if(distribution.getStatus() == null){
                    continue;
                } else {
//                    compareResult = highestStatus.compareTo(distribution.getStatus());
                    //orderIndex in terms still works the other way round
                    compareResult = -highestStatus.getOrderIndex().compareTo(distribution.getStatus().getOrderIndex());
                }
                if(compareResult < 0){
                    highestStatus = distribution.getStatus();
                    preferred.clear();
                    preferred.add(distribution);
                } else if(compareResult == 0) {
                    preferred.add(distribution);
                }
            }
        }

        return preferred;
    }

    /**
     * Removes all distributions that have an area being an ancestor of
     * another distribution area. E.g. removes distribution for "Europe"
     * if a distribution for "France" exists in the list, where Europe
     * is an ancestor for France.
     */
    private void handleSubAreaPreferenceRule(SetMap<NamedAreaDto,DistributionDto> filteredDistributions,
            SetMap<NamedAreaDto,NamedAreaDto> area2ParentAreaMap) {

        Set<TermDto> removeCandidateAreas = new HashSet<>();

        for(TermDto area : filteredDistributions.keySet()){
            Set<TermDto> ancestors = new HashSet<>();
            fillAncestorsRecursive(area, area2ParentAreaMap, ancestors, removeCandidateAreas);

            for (TermDto parentArea : ancestors) {
                if(parentArea != null && filteredDistributions.containsKey(parentArea)){
                    removeCandidateAreas.add(parentArea);
                }
            }
        }
        for(TermDto removeKey : removeCandidateAreas){
            filteredDistributions.remove(removeKey);
        }
    }

    private void fillAncestorsRecursive(TermDto area, SetMap<NamedAreaDto,NamedAreaDto> childToParentsMap,
            Set<TermDto> ancestors, Set<TermDto> removeCandidateAreas) {

        if(removeCandidateAreas.contains(area)){
            return;
        }
        Set<NamedAreaDto> parents = childToParentsMap.get(area);
        ancestors.addAll(parents);
        for (TermDto parent : parents) {
            if (parent != null) {
                fillAncestorsRecursive(parent, childToParentsMap, ancestors, removeCandidateAreas);
            }
        }
    }

    private static <T extends Object> Set<T> valuesOfAllInnerSets(Collection<Set<T>> collectionOfSets){
        Set<T> allValues = new HashSet<T>();
        for(Set<T> set : collectionOfSets){
            allValues.addAll(set);
        }
        return allValues;
    }


    /**
     * @param filteredDistributions
     *            A set of distributions a condensed distribution string should
     *            be created for.
     *            The set should guarantee that for each area not more than
     *            1 status exists, otherwise the behavior is not deterministic.
     *            For filtering see {@link DescriptionUtility#filterDistributions(
     *            Collection, Set, boolean, boolean, boolean, boolean, boolean)}
     * @param parentAreaMap TODO should we have a separate tree for the condensed distribution string?
     *            Add the tree to the CondensedDistributionConfiguration?
     * @param config
     *            The configuration for the condensed distribution string creation.
     * @param languages
     *            A list of preferred languages in case the status or area symbols are
     *            to be taken from the language abbreviations (not really in use)
     *            TODO could be moved to configuration or fully removed
     * @return
     *            A CondensedDistribution object that contains a string representation
     *            and a {@link TaggedText} representation of the condensed distribution string.
     */
    public CondensedDistribution getCondensedDistribution(Collection<DistributionDto> filteredDistributions,
            SetMap<NamedAreaDto,TermNodeDto> area2TermNodesMap, CondensedDistributionConfiguration config,
            List<Language> languages) {

        CondensedDistributionComposer composer = new CondensedDistributionComposer();

        CondensedDistribution condensedDistribution = composer.createCondensedDistribution(
                filteredDistributions, area2TermNodesMap, languages, config);
        return condensedDistribution;
    }
}