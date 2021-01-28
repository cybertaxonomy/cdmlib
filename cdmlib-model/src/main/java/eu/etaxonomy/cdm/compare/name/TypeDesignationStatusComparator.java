/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.name;

import eu.etaxonomy.cdm.compare.term.OrderedTermComparator;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;

/**
 * @author a.kohlbecker
 */
public class TypeDesignationStatusComparator<T extends TypeDesignationStatusBase<T>>
        extends OrderedTermComparator<T> {

    @Override
    public int compare(T o1, T o2) {
        // fix inverted order of cdm terms by -1*

        if(o1 == o2 || o1 instanceof NullTypeDesignationStatus && o2 instanceof NullTypeDesignationStatus){
            return 0;
        }
        if(o1 == null || o1 instanceof NullTypeDesignationStatus){
            return -1;
        }

        if(o2 == null || o2 instanceof NullTypeDesignationStatus){
            return 1;
        }

        return -1 * super.compare(o1, o2);
    }
}