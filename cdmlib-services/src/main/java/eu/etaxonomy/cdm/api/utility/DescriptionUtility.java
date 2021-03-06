/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.utility;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.DistributionTree;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.OrderedTermBase;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;

/**
 * @author a.kohlbecker
 * @since Apr 18, 2013
 */
public class DescriptionUtility {

    private static final Logger logger = Logger.getLogger(DescriptionUtility.class);


    /**
     * <b>NOTE: To avoid LayzyLoadingExceptions this method must be used in a transactional context.</b>
     *
     * Filters the given set of {@link Distribution}s for publication purposes
     * The following rules are respected during the filtering:
     * <ol>
     * <li><b>Marked area filter</b>: Skip distributions for areas having a {@code TRUE} {@link Marker}
     * with one of the specified {@link MarkerType}s. Existing sub-areas of a marked area must also be marked
     * with the same marker type, otherwise the marked area acts as a <b>fallback area</b> for the sub areas.
     * An area is a <b>fallback area</b> if it is marked to be hidden and if it has at least one of
     * sub area which is not marked to be hidden. The fallback area will be show if there is no {@link Distribution}
     * for any of the non hidden sub-areas. For more detailed discussion on fallback areas see
     * https://dev.e-taxonomy.eu/redmine/issues/4408</li>
     * <li><b>Prefer aggregated rule</b>: if this flag is set to <code>true</code> aggregated
     * distributions are preferred over non-aggregated elements.
     * (Aggregated description elements are identified by the description having type
     * {@link DescriptionType.AGGREGATED_DISTRIBUTION}). This means if an non-aggregated status
     * information exists for the same area for which aggregated data is available,
     * the aggregated data has to be given preference over other data.
     * see parameter <code>preferAggregated</code></li>
     * <li><b>Status order preference rule</b>: In case of multiple distribution
     * status ({@link PresenceAbsenceTermBase}) for the same area the status
     * with the highest order is preferred, see
     * {@link OrderedTermBase#compareTo(OrderedTermBase)}. This rule is
     * optional, see parameter <code>statusOrderPreference</code></li>
     * <li><b>Sub area preference rule</b>: If there is an area with a <i>direct
     * sub area</i> and both areas have the same status only the
     * information on the sub area should be reported, whereas the super area
     * should be ignored. This rule is optional, see parameter
     * <code>subAreaPreference</code>. Can be run separately from the other filters.
     * This rule affects any distribution,
     * that is to computed and edited equally. For more details see
     * {@link https://dev.e-taxonomy.eu/redmine/issues/5050})</li>
     * </ol>
     *
     * @param distributions
     *            the distributions to filter
     * @param hiddenAreaMarkerTypes
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
    public static Set<Distribution> filterDistributions(Collection<Distribution> distributions,
            Set<MarkerType> hiddenAreaMarkerTypes, boolean preferAggregated, boolean statusOrderPreference,
            boolean subAreaPreference, boolean keepFallBackOnlyIfNoSubareaDataExists, boolean ignoreDistributionStatusUndefined) {

        SetMap<NamedArea, Distribution> filteredDistributions = new SetMap<>(distributions.size());

        // sort Distributions by the area and filter undefinedStatus
        for(Distribution distribution : distributions){
            NamedArea area = distribution.getArea();
            if(area == null) {
                logger.debug("skipping distribution with NULL area");
                continue;
            }
            boolean filterUndefined = ignoreDistributionStatusUndefined && distribution.getStatus() != null
                    && distribution.getStatus().getUuid().equals(PresenceAbsenceTerm.uuidUndefined);
            if (!filterUndefined){
                filteredDistributions.putItem(area, distribution);
            }

        }

        // -------------------------------------------------------------------
        // 1) skip distributions having an area with markers matching hiddenAreaMarkerTypes
        //    but keep distributions for fallback areas (areas with hidden marker, but with visible sub-areas)
        if( hiddenAreaMarkerTypes != null && !hiddenAreaMarkerTypes.isEmpty()) {
            removeHiddenAndKeepFallbackAreas(hiddenAreaMarkerTypes, filteredDistributions, keepFallBackOnlyIfNoSubareaDataExists);
        }

        // -------------------------------------------------------------------
        // 2) remove not computed distributions for areas for which computed
        //    distributions exists
        if(preferAggregated) {
            handlePreferAggregated(filteredDistributions);
        }

        // -------------------------------------------------------------------
        // 3) status order preference rule
        if (statusOrderPreference) {
            SetMap<NamedArea, Distribution> tmpMap = new SetMap<>(filteredDistributions.size());
            for(NamedArea key : filteredDistributions.keySet()){
                tmpMap.put(key, filterByHighestDistributionStatusForArea(filteredDistributions.get(key)));
            }
            filteredDistributions = tmpMap;
        }

        // -------------------------------------------------------------------
        // 4) Sub area preference rule
        if(subAreaPreference){
            handleSubAreaPreferenceRule(filteredDistributions);
         }

        return valuesOfAllInnerSets(filteredDistributions.values());
    }

    private static void handleSubAreaPreferenceRule(SetMap<NamedArea, Distribution> filteredDistributions) {
        Set<NamedArea> removeCandidatesArea = new HashSet<>();
        for(NamedArea key : filteredDistributions.keySet()){
            if(removeCandidatesArea.contains(key)){
                continue;
            }
            if(key.getPartOf() != null && filteredDistributions.containsKey(key.getPartOf())){
                removeCandidatesArea.add(key.getPartOf());
            }
        }
        for(NamedArea removeKey : removeCandidatesArea){
            filteredDistributions.remove(removeKey);
        }
    }

    /**
     * Remove hidden areas but keep fallback areas.
     */
    private static void removeHiddenAndKeepFallbackAreas(Set<MarkerType> hiddenAreaMarkerTypes,
            SetMap<NamedArea, Distribution> filteredDistributions, boolean keepFallBackOnlyIfNoSubareaDataExists) {

        Set<NamedArea> areasHiddenByMarker = new HashSet<>();
        for(NamedArea area : filteredDistributions.keySet()) {
            if(isMarkedHidden(area, hiddenAreaMarkerTypes)) {
                  // if at least one sub area is not hidden by a marker
//                // the given area is a fall-back area for this sub area
//                for(DefinedTermBase<NamedArea> included : area.getIncludes()) {
//                    NamedArea subArea = CdmBase.deproxy(included,NamedArea.class);
//                    if (!areasHiddenByMarker.contains(subArea) && checkAreaMarkedHidden(hiddenAreaMarkerTypes, subArea)) {
//                        if(filteredDistributions.containsKey(subArea)) {
//                            areasHiddenByMarker.add(subArea);
//                        }
//                    }
//                    // if this sub-area is not marked to be hidden
//                    // the parent area must be visible if there is no
//                    // data for the sub-area
//                    boolean subAreaVisible = filteredDistributions.containsKey(subArea)
//                            && !areasHiddenByMarker.contains(subArea);
//                    showAsFallbackArea = !subAreaVisible || showAsFallbackArea;
//                }
//                if (!showAsFallbackArea) {
                SetMap<NamedArea, Distribution>  distributionsForSubareaCheck = keepFallBackOnlyIfNoSubareaDataExists ? filteredDistributions : null;
                boolean isFallBackArea = isRemainingFallBackArea(area, hiddenAreaMarkerTypes, distributionsForSubareaCheck);
                if (!isFallBackArea) {
                    // this area does not need to be shown as
                    // fall-back for another area so it will be hidden.
                    areasHiddenByMarker.add(area);
                }
            }
        }
        for(NamedArea area :areasHiddenByMarker) {
            filteredDistributions.remove(area);
        }
    }

    //if filteredDistributions == null it can be ignored if data exists or not
    private static boolean isRemainingFallBackArea(NamedArea area, Set<MarkerType> hiddenAreaMarkerTypes,
            SetMap<NamedArea, Distribution> filteredDistributions) {

        boolean result = false;
        for(DefinedTermBase<NamedArea> included : area.getIncludes()) {
            NamedArea subArea = CdmBase.deproxy(included,NamedArea.class);
            boolean noOrIgnoreData = filteredDistributions == null || !filteredDistributions.containsKey(subArea);

            //if subarea is not hidden and data exists return true
            if (isMarkedHidden(subArea, hiddenAreaMarkerTypes)){
                boolean subAreaIsFallback = isRemainingFallBackArea(subArea, hiddenAreaMarkerTypes, filteredDistributions);
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
//            boolean isNotHidden_AndHasNoData_OrDataCanBeIgnored =
//                    && noOrIgnoreData && subArea.getIncludes().isEmpty();
//            if (isNotHidden_AndHasNoData_OrDataCanBeIgnored) {
//                return true;
//            }
//            if (!isMarkedHidden(subArea, hiddenAreaMarkerTypes) ){
//
//            }
//
//            //do the same recursively
//            boolean hasVisibleSubSubarea = isRemainingFallBackArea(subArea, hiddenAreaMarkerTypes, filteredDistributions, areasHiddenByMarker);
//            if (hasVisibleSubSubarea){
//                return true;
//            }
        }
        return false;
    }

    private static void handlePreferAggregated(SetMap<NamedArea, Distribution> filteredDistributions) {
        SetMap<NamedArea, Distribution> computedDistributions = new SetMap<>(filteredDistributions.size());
        SetMap<NamedArea, Distribution> nonComputedDistributions = new SetMap<>(filteredDistributions.size());
        // separate computed and edited Distributions
        for (NamedArea area : filteredDistributions.keySet()) {
            for (Distribution distribution : filteredDistributions.get(area)) {
                // this is only required for rule 1
                if(isAggregated(distribution)){
                    computedDistributions.putItem(area, distribution);
                } else {
                    nonComputedDistributions.putItem(area,distribution);
                }
            }
        }
        //remove nonComputed distributions for which computed distributions exist in the same area
        for(NamedArea keyComputed : computedDistributions.keySet()){
            nonComputedDistributions.remove(keyComputed);
        }
        // combine computed and non computed Distributions again
        filteredDistributions.clear();
        for(NamedArea area : computedDistributions.keySet()){
            filteredDistributions.put(area, computedDistributions.get(area));  //is it a problem that we use the same interal Set here?
        }
        for(NamedArea area : nonComputedDistributions.keySet()){
            filteredDistributions.put(area, nonComputedDistributions.get(area));
        }
    }

    private static boolean isAggregated(Distribution distribution) {
        DescriptionBase<?> desc = distribution.getInDescription();
        if (desc != null && desc.isAggregatedDistribution()){
            return true;
        }
        return false;
    }

    public static boolean isMarkedHidden(NamedArea area, Set<MarkerType> hiddenAreaMarkerTypes) {
        if(hiddenAreaMarkerTypes != null) {
            for(MarkerType markerType : hiddenAreaMarkerTypes){
                if(area.hasMarker(markerType, true)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Orders the given Distribution elements in a hierarchical structure.
     * This method will not filter out any of the distribution elements.
     * @param omitLevels
     * @param distributions
     * @param fallbackAreaMarkerTypes
     *      Areas are fallback areas if they have a {@link Marker} with one of the specified
     *      {@link MarkerType marker types}.
     *      Areas identified as such are omitted from the hierarchy and the sub areas are moving one level up.
     *      This may not be the case if the fallback area has a distribution record itself AND if
     *      neverUseFallbackAreasAsParents is <code>false</code>.
     *      For more details on fall back areas see <b>Marked area filter</b> of
     *      {@link DescriptionUtility#filterDistributions(Collection, Set, boolean, boolean, boolean)}.
     * @param distributionOrder
     * @param termDao
     *      Currently used from performance reasons (preloading of parent areas), may be removed in future
     * @return the {@link DistributionTree distribution tree}
     */
    public static DistributionTree buildOrderedTree(Set<NamedAreaLevel> omitLevels,
            Collection<Distribution> distributions,
            Set<MarkerType> fallbackAreaMarkerTypes,
            boolean neverUseFallbackAreaAsParent,
            DistributionOrder distributionOrder,
            IDefinedTermDao termDao) {

        DistributionTree tree = new DistributionTree(termDao);

        if (logger.isDebugEnabled()){logger.debug("order tree ...");}
        //order by areas
        tree.orderAsTree(distributions, omitLevels, fallbackAreaMarkerTypes, neverUseFallbackAreaAsParent);
        tree.recursiveSortChildren(distributionOrder); // TODO respect current locale for sorting
        if (logger.isDebugEnabled()){logger.debug("create tree - DONE");}
        return tree;
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
    private static Set<Distribution> filterByHighestDistributionStatusForArea(Set<Distribution> distributions){

        Set<Distribution> preferred = new HashSet<>();
        PresenceAbsenceTerm highestStatus = null;  //we need to leave generics here as for some reason highestStatus.compareTo later jumps into the wrong class for calling compareTo
        int compareResult;
        for (Distribution distribution : distributions) {
            if(highestStatus == null){
                highestStatus = distribution.getStatus();
                preferred.add(distribution);
            } else {
                if(distribution.getStatus() == null){
                    continue;
                } else {
                    compareResult = highestStatus.compareTo(distribution.getStatus());
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

    private static <T extends CdmBase> Set<T> valuesOfAllInnerSets(Collection<Set<T>> collectionOfSets){
        Set<T> allValues = new HashSet<T>();
        for(Set<T> set : collectionOfSets){
            allValues.addAll(set);
        }
        return allValues;
    }

}
