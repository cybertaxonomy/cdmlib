/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.compare;

import java.util.Comparator;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

/**
 * @author a.mueller
 * @date 13.04.2023
 */
public class UuidAndTitleCacheComparator<T extends ICdmBase>
        implements Comparator<UuidAndTitleCache<T>> {

    private boolean ignoreCase = false;

    public UuidAndTitleCacheComparator() {}

    public UuidAndTitleCacheComparator(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    @Override
    public int compare(UuidAndTitleCache<T> o1, UuidAndTitleCache<T> o2) {
        if (o1 == o2) {
            return 0;
        }else if (o1 == null) {
            return -1; //TODO correct?
        }else if (o2 == null) {
            return 1;
        }

        //titleCache
        int comp = CdmUtils.nullSafeCompareTo(o1.getTitleCache(), o2.getTitleCache(), ignoreCase);
        if (comp != 0) {
            return comp;
        }
        //abbrev
        comp = CdmUtils.nullSafeCompareTo(o1.getAbbrevTitleCache(), o2.getAbbrevTitleCache(), ignoreCase);
        if (comp != 0) {
            return comp;
        }
        //type
        String type1 = o1.getType() == null ? null : o1.getType().getSimpleName();
        String type2 = o2.getType() == null ? null : o2.getType().getSimpleName();
        comp = CdmUtils.nullSafeCompareTo(type1, type2);
        if (comp != 0) {
            return comp;
        }
        //id
        comp = CdmUtils.nullSafeCompareTo(o1.getId(), o2.getId());
        if (comp != 0) {
            return comp;
        }
        //uuid
        comp = CdmUtils.nullSafeCompareTo(o1.getUuid(), o2.getUuid());
        if (comp != 0) {
            return comp;
        }
        return 0;
    }
}