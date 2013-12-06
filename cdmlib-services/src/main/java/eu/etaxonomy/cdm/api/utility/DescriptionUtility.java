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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * @author a.kohlbecker
 * @date Apr 18, 2013
 *
 */
public class DescriptionUtility {

    private static final Logger logger = Logger.getLogger(DescriptionUtility.class);

    /**
     * <b>NOTE: This method must only be used in a transactional context.</b>
     *
     * Filters the given set of {@link Distribution}s for publication purposes
     * The following rules are respected during the filtering:
     * <ol>
     * <li>Computed elements are preferred over entered or imported elements.
     * (Computed description elements are identified by the {@link
     * MarkerType.COMPUTED()}). This means if a entered or imported status
     * information exist for the same area for which computed data is available,
     * the computed data has to be given preference over other data.</li>
     * <li>In computed distributions, distributions in parent areas are
     * preferred over those for <i>direct sub areas</i> if they have the same
     * status</li>
     * <li><b>Status order preference rule</b>: In case of multiple distribution
     * status ({@link PresenceAbsenceTermBase}) for the same area the status
     * with the highest order is preferred, see
     * {@link OrderedTermBase#compareTo(OrderedTermBase)}. This rule is
     * optional, see parameter <code>statusOrderPreference</code></li>
     * <li><b>Sub area preference rule</b>: If there is an area with a <i>direct
     * sub area</i> and both areas have the same computed status only the
     * information on the sub area should be reported, whereas the super area
     * should be ignored. This rule is optional, see parameter
     * <code>subAreaPreference</code></li>
     * <li>Skip distributions without area mapping to a specific named shapefile. This rule is optional</li>
     * </ol>
     *
     * @param distributions
     *            the distributions to filter
     * @param subAreaPreference
     *            enables the <b>Sub area preference rule</b> if set to true
     * @param statusOrderPreference
     *            enables the <b>Status order preference rule</b> if set to true
     * @return the filtered collection of distribution elements.
     */
    public static Collection<Distribution> filterDistributions(Collection<Distribution> distributions,
            boolean subAreaPreference, boolean statusOrderPreference) {

        Map<NamedArea, Set<Distribution>> computedDistributions = new HashMap<NamedArea, Set<Distribution>>(distributions.size());
        Map<NamedArea, Set<Distribution>> otherDistributions = new HashMap<NamedArea, Set<Distribution>>(distributions.size());

        Map<NamedArea, Set<Distribution>> filteredDistributions;

        Set<Distribution> removeCandidatesDistribution = new HashSet<Distribution>();
        Set<NamedArea> removeCandidatesArea = new HashSet<NamedArea>();

        // 0) if desired skip distributions without area mapping to a specific named shapefile
        // TODO IMPLEMENT
        // ... collect all named areas and create exclude set

        // 1) sort by computed / not computed
        for(Distribution distribution : distributions){
            if(distribution.getArea() == null) {
                logger.debug("skipping distribution with NULL area");
                continue;
            }
            if(distribution.hasMarker(MarkerType.COMPUTED(), true)){
                if(!computedDistributions.containsKey(distribution.getArea())){
                    computedDistributions.put(distribution.getArea(), new HashSet<Distribution>());
                }
                computedDistributions.get(distribution.getArea()).add(distribution);
            } else {
                if(!otherDistributions.containsKey(distribution.getArea())){
                    otherDistributions.put(distribution.getArea(), new HashSet<Distribution>());
                }
                otherDistributions.get(distribution.getArea()).add(distribution);
            }
        }

        // if there are computed elements apply the filter rules
        if(computedDistributions.size() > 0){

            // 2) apply filter rules
            // 2.a) prepare removal of all not computed areas for which a computed area exists
            for(NamedArea keyComputed : computedDistributions.keySet()){
                otherDistributions.remove(keyComputed);
            }

            // 2.b) in computed distributions prefer parent areas over sub areas if they have the same status
            for(Distribution distribution : valuesOfAllInnerSets(computedDistributions.values())){
                if(distribution.getArea() != null){
                    NamedArea parentArea = distribution.getArea().getPartOf();
                    while(parentArea != null){
                        // get all distributions for the parent area
                        Set<Distribution> parentAreaDistributions = computedDistributions.get(parentArea);
                        if(parentAreaDistributions != null){
                            // check all computed distributions of the parent area
                            for(Distribution parentDistribution : parentAreaDistributions) {
                                if(parentDistribution != null && parentDistribution.getStatus().equals(distribution.getStatus())){
                                    removeCandidatesDistribution.add(parentDistribution);
                                }
                            }
                        }
                        parentArea = parentArea.getPartOf();
                    }
                }
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

        // 3) Sub area preference rule
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
     * Implements the Status order preference filter for a given set to Distributions.
     * The distributions should all be for the same area
     *
     * @param distributions
     * @return the distribution with the highest status
     */
    private static Set<Distribution> byHighestOrderPresenceAbsenceTerm(Set<Distribution> distributions){

        Set<Distribution> preferred = new HashSet<Distribution>();
        PresenceAbsenceTermBase highestStatus = null;
        int compareResult;
        for (Distribution distribution : distributions) {
            if(highestStatus == null){
                highestStatus = distribution.getStatus();
                preferred.add(distribution);
            } else {
                if(distribution.getStatus() == null){
                    // highestStatus is considered higher if
                    // highestStatus != null && distribution.getStatus() == null
                    compareResult = highestStatus != null ? -1 : 0;
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

    private static <T extends CdmBase> Collection<T> valuesOfAllInnerSets(Collection<Set<T>> collectionOfSets){
        Collection<T> allValues = new ArrayList<T>();
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
