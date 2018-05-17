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

import eu.etaxonomy.cdm.model.name.TypeDesignationBase;

/**
 * @author pplitzner
 * @since May 3, 2018
 *
 */
public class TypeDesignationComparator implements Comparator<TypeDesignationBase> {

    private TypeDesignationStatusComparator statusComparator = new TypeDesignationStatusComparator();

    @SuppressWarnings("unchecked")
    @Override
    public int compare(TypeDesignationBase o1, TypeDesignationBase o2) {
        if(o1==null){
            return 1;
        }
        if(o2==null){
            return -1;
        }
        if(o1.getTypeStatus()==null){
            return 1;
        }
        if(o2.getTypeStatus()==null){
            return-1;
        }
        return statusComparator.compare(o1.getTypeStatus(), o2.getTypeStatus()) ;
    }
}
