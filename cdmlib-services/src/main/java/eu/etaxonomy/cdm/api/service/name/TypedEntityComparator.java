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
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * @author pplitzner
 * @since May 3, 2018
 */
public class TypedEntityComparator implements Comparator<TypedEntityReference<TypeDesignationBase<?>> >{

    @Override
    public int compare(TypedEntityReference<TypeDesignationBase<?>> o1, TypedEntityReference<TypeDesignationBase<?>> o2) {
        if (o1==o2){
            return 0;
        }else if(o1==null){
            return 1;
        }else if(o2==null){
            return -1;
        }

        //AM: TODO why is uuid == null be sorted explicitly?
        if(o1.getUuid()==null && o2.getUuid()!=null){
            return 1;
        }
        if(o2.getUuid()==null && o1.getUuid()!=null){
            return-1;
        }

        return o1.getLabel().compareTo(o2.getLabel());
    }
}
