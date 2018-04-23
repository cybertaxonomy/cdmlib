/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.utility;

import java.util.Comparator;
import java.util.Set;

import eu.etaxonomy.cdm.api.service.DistributionNodeByAreaLabelComparator;
import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * Enumeration that defines the ordering method for distribution trees.
 *
 * @author a.mueller
 \* @since 04.04.2016
 *
 */
public enum DistributionOrder {
    LABEL,
    AREA_ORDER;

    /**
     * Returns a comparator that implements the given distribution order.
     * @return
     */
    public Comparator<TreeNode<Set<Distribution>, NamedArea>> getComparator() {
        if (this == LABEL){
            return new DistributionNodeByAreaLabelComparator();
        }else if (this == AREA_ORDER){
            return new DistributionNodeByAreaOrderComparator();
        }else{
            throw new IllegalStateException("Comparator is not available for DistributionOrder '" + this.name() + "'");
        }
    }

    /**
     * The default distribution order. Currently this is {@link DistributionOrder#LABEL}
     * @return the default distribution order.
     */
    public static DistributionOrder getDefault() {
        return LABEL;
    }
}
