/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.name;

import java.util.Comparator;

import eu.etaxonomy.cdm.api.service.name.TypeDesignationSetManager.NullTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;

/**
 * @author pplitzner
 * @since May 3, 2018
 *
 */
public class TypeDesignationStatusComparator <T extends TypeDesignationStatusBase<T>>  implements Comparator<T> {
    @Override
    public int compare(T o1, T o2) {
        // fix inverted order of cdm terms by -1*
        if(o1 == null && o2 == null || o1 instanceof NullTypeDesignationStatus && o2 instanceof NullTypeDesignationStatus){
            return 0;
        }
        if(o1 == null || o1 instanceof NullTypeDesignationStatus){
            return -1;
        }

        if(o2 == null || o2 instanceof NullTypeDesignationStatus){
            return 1;
        }
        return -1 * o1.compareTo(o2);
    }
}
