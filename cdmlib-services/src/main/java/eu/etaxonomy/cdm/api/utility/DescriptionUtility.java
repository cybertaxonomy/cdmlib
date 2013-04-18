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

import java.util.HashSet;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;

/**
 * @author a.kohlbecker
 * @date Apr 18, 2013
 *
 */
public class DescriptionUtility {

    /**
     * Filters the given set of description elements and prefers computed elements over
     * others. Computed description elements are identified by the {@link  MarkerType.COMPUTED()}.
     *
     * If the given set contains at least one computed element only the computed elements
     * returned.
     *
     * @param descriptionElements
     * @return only the computed description elements other wise all others.
     */
    public static <T extends DescriptionElementBase> Set<T> preferComputed(Set<T> descriptionElements) {

        Set<T> computedDistributions = new HashSet<T>(descriptionElements.size());
        Set<T> otherDistributions = new HashSet<T>(descriptionElements.size());
        for(T distribution : descriptionElements){
            if(distribution.hasMarker(MarkerType.COMPUTED(), true)){
                computedDistributions.add(distribution);
            } else {
                otherDistributions.add(distribution);
            }
        }
        if(computedDistributions.size() > 0){
            return computedDistributions;
        } else {
            return otherDistributions;
        }
    }

}
