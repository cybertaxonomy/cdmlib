/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.common;

import java.util.Comparator;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.TimePeriod;

/**
 * Comparator for {@link TimePeriod}s.
 * @author muellera
 * @since 16.02.2024
 */
public class TimePeriodComparator implements Comparator<TimePeriod> {

    private boolean nullSmallest = false;

    private static final TimePeriodComparator instance = new TimePeriodComparator();
    private static TimePeriodComparator instanceNullSmallest;

    public static final TimePeriodComparator INSTANCE() {
        return instance;
    }

    public static final TimePeriodComparator INSTANCE_NULL_SMALLEST() {
        if (instanceNullSmallest == null) {
            instanceNullSmallest = new TimePeriodComparator();
            instanceNullSmallest.nullSmallest = true;
        }
        return instanceNullSmallest;
    }

    public static final TimePeriodComparator INSTANCE(boolean nullSmallest) {
        return nullSmallest ? INSTANCE_NULL_SMALLEST() : INSTANCE();
    }

    @Override
    public int compare(TimePeriod tp1, TimePeriod tp2) {

        PartialComparator partialComparator = nullSmallest?
                PartialComparator.INSTANCE_NULL_SMALLEST() : PartialComparator.INSTANCE();

        if (tp1 == tp2) {
            return 0;
        }else if (tp1 == null){
            return 1;
        }else if (tp2 == null){
            return -1;
        }

        int compare = partialComparator.compare(tp1.getStart(), tp2.getStart());
        if (compare != 0) {
            return compare;
        }
        //TODO handle open end flag
        compare = partialComparator.compare(tp1.getEnd(), tp2.getEnd());
        if (compare != 0) {
            return compare;
        }
        compare = CdmUtils.nullSafeCompareTo(tp1.getFreeText(), tp2.getFreeText());
        if (compare != 0) {
            return compare;
        }

        //TODO compare verbatim

        return 0;
    }
}