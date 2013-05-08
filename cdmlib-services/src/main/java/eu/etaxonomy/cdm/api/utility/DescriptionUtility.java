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

import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * @author a.kohlbecker
 * @date Apr 18, 2013
 *
 */
public class DescriptionUtility {

    /**
     * <b>NOTE: This method must only be used in a transactional context.</b>
     *
     * Filters the given set of {@link Distribution}s for publication.
     * Computed elements are preferred over entered or imported elements.
     * Computed description elements are identified by the {@link  MarkerType.COMPUTED()}.
     *
     * The following rules are respected during the filtering:
     * <ol>
     * <li>If a entered or imported status information exist for the same area for which computed
     *   data is available, the computed data has to be given preference over other data.</li>
     * <li>If there is an area with a sub area and both areas have the same computed status only the subarea
     *  status should be shown in the map, whereas the super area should be ignored.</li>
     * </ol>
     *
     * @param distributions
     * @return the filtered collection of distribution elements.
     */
    public static Collection<Distribution> filterDistributions(Collection<Distribution> distributions) {

        Map<String, Distribution> computedDistributions = new HashMap<String, Distribution>(distributions.size());
        Map<String, Distribution> otherDistributions = new HashMap<String, Distribution>(distributions.size());

        // 1. sort by computed / not computed
        for(Distribution distribution : distributions){
            if(distribution.hasMarker(MarkerType.COMPUTED(), true)){
                computedDistributions.put(areaKey(distribution), distribution);
            } else {
                otherDistributions.put(areaKey(distribution), distribution);
            }
        }

        // if no computed elements return all
        if(computedDistributions.size() == 0){
            return otherDistributions.values();
        }

        // 2. apply the filter rules
        // remove all not computed areas for which a computed area exists
        for(String keyComputed : computedDistributions.keySet()){
            otherDistributions.remove(otherDistributions);
        }
        Set<Distribution> removeCandidates = new HashSet<Distribution>();
        for(Distribution distribution : computedDistributions.values()){
            if(distribution.getArea() != null){
                NamedArea parentArea = distribution.getArea().getPartOf();
                while(parentArea != null){
                    Distribution parentDistribution = computedDistributions.get(areaKey(parentArea));
                    if(parentDistribution != null && parentDistribution.getStatus().equals(distribution.getStatus())){
                        removeCandidates.add(parentDistribution);

                    }
                    parentArea = parentArea.getPartOf();
                }
            }
        }
        for(Distribution distribution : removeCandidates){
            computedDistributions.remove(areaKey(distribution));
        }

        // finally combine computed and non computed distributions again
        Set<Distribution> filteredDistributions = new HashSet<Distribution>(otherDistributions.size() + computedDistributions.size());
        filteredDistributions.addAll(otherDistributions.values());
        filteredDistributions.addAll(computedDistributions.values());

        return filteredDistributions;

    }

    private static String areaKey(NamedArea area){
        return String.valueOf(area.getId());
    }

    private static String areaKey(Distribution distribution){
        StringBuilder keyBuilder = new StringBuilder();

        if(distribution.getArea() != null){
            keyBuilder.append(distribution.getArea().getId());
        } else {
            keyBuilder.append("NULL");
        }

        return keyBuilder.toString();
    }

}
