// $Id$
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.DistributionTree;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;

/**
 * @author a.kohlbecker
 * @date Apr 18, 2013
 *
 */
public class DescriptionUtility {

    private static final Logger logger = Logger.getLogger(DescriptionUtility.class);

    /**
     * <b>NOTE: To avoid LayzyLoadingExceptions this method must be used in a transactional context.</b>
     *
     * Filters the given set of {@link Distribution}s for publication purposes
     * The following rules are respected during the filtering:
     * <ol>
     * <li>Computed elements are preferred over entered or imported elements.
     * (Computed description elements are identified by the {@link
     * MarkerType.COMPUTED()}). This means if a entered or imported status
     * information exist for the same area for which computed data is available,
     * the computed data has to be given preference over other data.</li>
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
     * {@link https://dev.e-taxonomy.eu/trac/ticket/5050})</li>
     * <li><b>Marked area filter</b>: Skip distributions where the area has a {@link Marker}
     * with one of the specified {@link MarkerType}s
     * <li><b>Fallback Area filter</b>: Areas can be tagged as fallback area by assigning
     * a {@link Marker} of the specified {@link MarkerType}.
     * These areas will be skipped as long not a a Distribution for any of sub areas exists,
     * see https://dev.e-taxonomy.eu/trac/ticket/4408 for a detailed discussion.</li>
     * </ol>
     *
     * @param distributions
     *            the distributions to filter
     * @param subAreaPreference
     *            enables the <b>Sub area preference rule</b> if set to true
     * @param statusOrderPreference
     *            enables the <b>Status order preference rule</b> if set to true,
     *            This rule can be run separately from the other filters.
     * @param hideMarkedAreas
     *            distributions where the area has a {@link Marker} with one of the specified {@link MarkerType}s will be skipped
     * @param fallbackAreaMarkerType
     *            {@link MarkerType} for the {@link Marker}s to identify fallback areas.
     * @return the filtered collection of distribution elements.
     */
    public static Set<Distribution> filterDistributions(Collection<Distribution> distributions,
            boolean subAreaPreference, boolean statusOrderPreference, Set<MarkerType> hideMarkedAreas, MarkerType fallbackAreaMarkerType) {

        Map<NamedArea, Set<Distribution>> filteredDistributions;

        Set<Distribution> removeCandidatesDistribution = new HashSet<Distribution>();

        boolean doHideMarkedAreas = hideMarkedAreas != null && !hideMarkedAreas.isEmpty();
        boolean dofallbackAreas = fallbackAreaMarkerType != null;

        if(statusOrderPreference || doHideMarkedAreas) {

            Map<NamedArea, Set<Distribution>> computedDistributions = new HashMap<NamedArea, Set<Distribution>>(distributions.size());
            Map<NamedArea, Set<Distribution>> otherDistributions = new HashMap<NamedArea, Set<Distribution>>(distributions.size());
            Set<NamedArea> areasHiddenByMarker = new HashSet<NamedArea>();

            // 1) sort by computed / not computed
            boolean doSkip = false;
            for(Distribution distribution : distributions){

                // 1.1) skip distributions having an area with markers matching hideMarkedAreas
                NamedArea area = distribution.getArea();
                if(area == null) {
                    logger.debug("skipping distribution with NULL area");
                    continue;
                } if(areasHiddenByMarker.contains(area)){
                    logger.debug("skipping distribution with marked area, area previously recognized and cached");
                    continue;
                }else {
                    doSkip = false;
                    if(doHideMarkedAreas){
                        for(MarkerType markerType : hideMarkedAreas){
                            if(area.hasMarker(markerType, true)){
                                areasHiddenByMarker.add(area);
                                logger.debug("skipping distribution with marked area");
                                doSkip = true;
                                continue;
                            }
                        }
                    }
                    if(doSkip){
                        continue;
                    }

                }

                if(distribution.hasMarker(MarkerType.COMPUTED(), true)){
                    if(!computedDistributions.containsKey(area)){
                        computedDistributions.put(area, new HashSet<Distribution>());
                    }
                    computedDistributions.get(area).add(distribution);
                } else {
                    if(!otherDistributions.containsKey(area)){
                        otherDistributions.put(area, new HashSet<Distribution>());
                    }
                    otherDistributions.get(area).add(distribution);
                }
            }

            // if there are computed elements apply the filter rules
            if(computedDistributions.size() > 0){

                // 2) apply filter rules
                // 2.a) prepare removal of all not computed areas for which a computed area exists
                for(NamedArea keyComputed : computedDistributions.keySet()){
                    otherDistributions.remove(keyComputed);
                }

            }

            filteredDistributions = new HashMap<NamedArea, Set<Distribution>>(otherDistributions.size() + computedDistributions.size());

            // finally remove computed distributions if necessary and combine computed and non computed distributions again
            // and apply the Status order preference filter
            for(Distribution removeDistribution : removeCandidatesDistribution){
                computedDistributions.remove(removeDistribution.getArea()); //FIXME is this correct? or should we only remove the specific distribution???
            }
            for(NamedArea key : computedDistributions.keySet()){
                filteredDistributions.put(key, byHighestOrderPresenceAbsenceTerm(computedDistributions.get(key)));
            }

            // add the non computed distributions to combine them again and apply the Status order preference filter
            for(NamedArea key : otherDistributions.keySet()){
                filteredDistributions.put(key, byHighestOrderPresenceAbsenceTerm(otherDistributions.get(key)));
            }
        } else {
            // no filtering happened until this point, therefore adding all given distributions
            filteredDistributions = new HashMap<NamedArea, Set<Distribution>>(distributions.size());
            for(Distribution distribution : distributions){
                NamedArea area = distribution.getArea();
                if(!filteredDistributions.containsKey(area)){
                    filteredDistributions.put(area, new HashSet<Distribution>());
                }
                filteredDistributions.get(area).add(distribution);
            }

        }

        // 3) keep or remove distributions for fallback areas
        Set<NamedArea> removeCandidatesFallback = new HashSet<NamedArea>();
        if(dofallbackAreas){
            for(NamedArea key : filteredDistributions.keySet()){
                if(removeCandidatesFallback.contains(key)){
                    continue;
                }
                if(key.getPartOf() != null && filteredDistributions.containsKey(key.getPartOf())
                        && key.getPartOf().hasMarker(fallbackAreaMarkerType, true)){
                    removeCandidatesFallback.add(key.getPartOf());
                }
            }
            for(NamedArea removeKey : removeCandidatesFallback){
                filteredDistributions.remove(removeKey);
            }
         }



        // 4) Sub area preference rule
        Set<NamedArea> removeCandidatesArea = new HashSet<NamedArea>();
        if(subAreaPreference){
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


        return valuesOfAllInnerSets(filteredDistributions.values());

    }

    /**
     * Orders the given Distribution elements in a hierarchical structure.
     * This method will not filter out any of the Distribution elements.
     * @param termDao
     * @param omitLevels
     * @param distList
     * @return
     */
    public static DistributionTree orderDistributions(IDefinedTermDao termDao, Set<NamedAreaLevel> omitLevels, Collection<Distribution> distributions) {

        DistributionTree tree = new DistributionTree(termDao);

        if (logger.isDebugEnabled()){logger.debug("order tree ...");}
        //order by areas
        tree.orderAsTree(distributions, omitLevels);
        tree.recursiveSortChildrenByLabel(); // FIXME respect current locale for sorting
        if (logger.isDebugEnabled()){logger.debug("create tree - DONE");}
        return tree;
    }

    /**
     * Implements the Status order preference filter for a given set to Distributions.
     * The distributions should all be for the same area
     *
     * @param distributions
     * @return the distribution with the highest status
     */
    private static Set<Distribution> byHighestOrderPresenceAbsenceTerm(Set<Distribution> distributions){

        Set<Distribution> preferred = new HashSet<Distribution>();
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

    /**
     * Provides a consistent string based key of the given NamedArea , see also
     * {@link #areaKey(Distribution)}
     *
     * @param area
     * @return the string representation of the NamedArea.uuid
     */
//    private static String areaKey(NamedArea area){
//        return String.valueOf(area.getUuid());
//    }

    /**
     * Provides a consistent string based key of the given NamedArea contained
     * in the given distribution, see also {@link #areaKey(Distribution)}
     *
     * @param distribution
     * @return the string representation of the NamedArea.uuid or
     *         <code>"NULL"</code> in case the Distribution had no NamedArea
     */
//    private static String areaKey(Distribution distribution){
//        StringBuilder keyBuilder = new StringBuilder();
//
//        if(distribution.getArea() != null){
//            keyBuilder.append(distribution.getArea().getUuid());
//        } else {
//            keyBuilder.append("NULL");
//        }
//
//        return keyBuilder.toString();
//    }

}
