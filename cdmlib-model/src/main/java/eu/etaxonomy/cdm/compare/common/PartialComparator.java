/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.common;

import java.util.Comparator;

import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;

import eu.etaxonomy.cdm.model.common.TimePeriod;

/**
 * Simplified {@link Partial} comparator handling the 5 fields (year, month, day, hour, minute) used in CDM only.
 *
 * @author a.mueller
 * @since 15.07.2021
 */
public class PartialComparator implements Comparator<Partial> {

    private static final PartialComparator instance = new PartialComparator(false);
    private static final PartialComparator instanceNullInverse = new PartialComparator(true);

    final int nullInverse;

    private PartialComparator(boolean nullInverse) {
        this.nullInverse = nullInverse ? -1 : 1;
    }

    public static PartialComparator INSTANCE(){
        return instance;
    }

    public static PartialComparator INSTANCE_NULL_SMALLEST(){
        return instanceNullInverse;
    }

    @Override
    public int compare(Partial p1, Partial p2) {
        if (p1 == p2) {
            return 0;
        }else if (p1 == null){
            return 1 * nullInverse;
        }else if (p2 == null){
            return -1 * nullInverse;
        }

        int result = 0;
        Integer[] p1Array = getArray(p1);
        Integer[] p2Array = getArray(p2);
        for (int i = 0; i<p1Array.length ; i++){
            Integer v1 = p1Array[i];
            Integer v2 = p2Array[i];
            if (v1 == null && v2 == null){
                continue;
            }else if (v1 == null){
                return 1 * nullInverse;
            }else if (v2 == null){
                return -1 * nullInverse;
            }else{
                result = v1.compareTo(v2);
                if (result != 0){
                    return result;
                }
            }
        }

        return 0;
    }

    private Integer[] getArray(Partial p1) {
        Integer[] result = new Integer[5];
        result[0] = TimePeriod.getPartialValue(p1, DateTimeFieldType.year());
        result[1] = TimePeriod.getPartialValue(p1, DateTimeFieldType.monthOfYear());
        result[2] = TimePeriod.getPartialValue(p1, DateTimeFieldType.dayOfMonth());
        result[3] = TimePeriod.getPartialValue(p1, DateTimeFieldType.hourOfDay());
        result[4] = TimePeriod.getPartialValue(p1, DateTimeFieldType.minuteOfHour());
        return result;
    }

}
