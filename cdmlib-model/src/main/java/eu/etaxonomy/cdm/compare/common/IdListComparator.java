/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.common;

import java.util.Comparator;
import java.util.List;

import eu.etaxonomy.cdm.model.common.ICdmBase;

/**
 * {@link Comparator} implementation that compares {@link ICdmBase} objects
 * according to a given ordered list of ids.
 *
 * @author muellera
 * @since 04.09.2026
 */
public class IdListComparator implements Comparator<ICdmBase> {

    private final List<Integer> idList;

    public IdListComparator(List<Integer> idList) {
        idList = idList == null ? List.of() : idList;
        this.idList = idList;
    }

    @Override
    public int compare(ICdmBase o1, ICdmBase o2) {

        int index1 = o1 == null ? -1 : idList.indexOf(o1.getId());
        int index2 = o2 == null ? -1 : idList.indexOf(o2.getId());
        return Integer.compare(index1, index2);
    }
}