/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author k.luther
 * @since 30.11.2021
 */
public class FeatureStateDtoComparator implements Serializable, Comparator<FeatureStateDto> {

    private static final long serialVersionUID = -4808908193485190520L;

    @Override
    public int compare(FeatureStateDto o1, FeatureStateDto o2) {
        if(o1==null){
            return -1;
        }
        if(o2==null){
            return 1;
        }
        if (o1.getUuid() == null && o2.getUuid() != null){
            return -1;
        }
        if (o2.getUuid() == null && o1.getUuid() != null){
            return 1;
        }
        if (o1.getUuid() != null && o2.getUuid() != null){
            return o1.getFeature().getTitleCache().compareTo(o2.getTitleCache());
        } else {
            return o1.getFeature().getUuid().compareTo(o2.getFeature().getUuid());
        }

    }

}
