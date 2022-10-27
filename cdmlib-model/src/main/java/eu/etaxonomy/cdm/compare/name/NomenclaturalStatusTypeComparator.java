/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.name;

import java.io.Serializable;
import java.util.Comparator;

import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;

/**
 * @author a.mueller
 * @date 27.10.2022
 */
public class NomenclaturalStatusTypeComparator implements
        Comparator<NomenclaturalStatusType>, Serializable {

    private static final long serialVersionUID = -231347917366470402L;

    private static NomenclaturalStatusTypeComparator singleton;

    public static NomenclaturalStatusTypeComparator SINGLETON(){
        if (singleton == null) {
            singleton = new NomenclaturalStatusTypeComparator();
        }
        return singleton;
    }

    @Override
    public int compare(NomenclaturalStatusType o1, NomenclaturalStatusType o2) {

        if (o1 == null) {
            return o2 == null? 0 : -1;
        }else if (o2 == null){
            return 1;
        }

        //TODO includes is maybe problematic due to lazy loading
        if (o1.getIncludes().contains(o2)) {
            return -1;
        }else if (o2.getIncludes().contains(o1)) {
            return 1;
        }else {
            //no natural order exist, use uuid to have a defined order at least
            return o1.getUuid().compareTo(o2.getUuid());
        }
    }
}