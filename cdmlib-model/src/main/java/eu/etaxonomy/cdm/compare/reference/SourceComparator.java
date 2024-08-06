/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.reference;

import java.util.Comparator;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;

/**
 * Source comparator, moved from CdmLightClassificationExport to here.
 * For now it compares the publication years. There might be more fine tuning in future.
 *
 * See #10578
 *
 * @author muellera
 * @since 06.08.2024
 */
public class SourceComparator implements Comparator<OriginalSourceBase> {

    private static SourceComparator singleton;
    public static final SourceComparator Instance() {
        if (singleton == null) {
            singleton = new SourceComparator();
        }
        return singleton;
    }

    @Override
    public int compare(OriginalSourceBase o1, OriginalSourceBase o2) {
        if (CdmUtils.nullSafeEqual(o1, o2)) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }else if (o2 == null) {
            return 1;
        }

        String year1 = o1.getCitation()!= null?
                o1.getCitation().getDatePublished()!= null?
                        o1.getCitation().getDatePublished().getYear() :null:null;
        String year2 = o2.getCitation()!= null?
                o2.getCitation().getDatePublished()!= null?
                        o2.getCitation().getDatePublished().getYear() :null:null;

        int c = CdmUtils.nullSafeCompareTo(CdmUtils.Nz(year1), CdmUtils.Nz(year2));
        if (c == 0) {
            c = o1.getUuid().compareTo(o2.getUuid());
        }
        return c;
    }

}
