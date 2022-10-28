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

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;

/**
 * Default {@link Comparator} for NomenclaturalSatus.
 *
 * TODO: still preliminary, may change in future
 *
 * @author a.mueller
 * @date 27.10.2022
 */
public class NomenclaturalStatusComparator implements
        Comparator<NomenclaturalStatus>, Serializable {

    private static final long serialVersionUID = 1552709174909692541L;

    private static NomenclaturalStatusComparator singleton;

    public static NomenclaturalStatusComparator SINGLETON(){
        if (singleton == null) {
            singleton = new NomenclaturalStatusComparator();
        }
        return singleton;
    }

    @Override
    public int compare(NomenclaturalStatus o1, NomenclaturalStatus o2) {

        if (o1 == null) {
            return o2 == null? 0 : -1;
        }else if (o2 == null){
            return 1;
        }

        NomenclaturalStatusType nst1 = o1.getType();
        NomenclaturalStatusType nst2 = o2.getType();

        int compare = NomenclaturalStatusTypeComparator.SINGLETON().compare(nst1, nst2);
        if (compare != 0) {
            return compare;
        }

        String rule1 = o1.getRuleConsidered();
        String rule2 = o2.getRuleConsidered();

        compare = CdmUtils.nullSafeCompareTo(rule1, rule2);
        if (compare != 0) {
            return compare;
        }

        //no natural order exist, use uuid to have a defined order at least
        return o1.getUuid().compareTo(o2.getUuid());
    }
}