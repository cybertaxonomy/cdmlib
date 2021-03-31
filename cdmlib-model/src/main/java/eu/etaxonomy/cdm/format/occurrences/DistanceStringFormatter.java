/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.occurrences;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.UTF8;

/**
 * @author a.mueller
 * @since 31.03.2021
 */
public class DistanceStringFormatter {

    /**
     * Computes the correct distance string for given values for min, max and text.
     * If text is not blank, text is returned, otherwise "min - max" or a single value is returned.
     * @param min min value as number
     * @param max max value as number
     * @param text text representation of distance
     * @return the formatted distance string
     */
    public static String distanceString(Number min, Number max, String text, String unit) {
        if (StringUtils.isNotBlank(text)){
            return text;
        }else{
            String minStr = min == null? null : String.valueOf(min);
            String maxStr = max == null? null : String.valueOf(max);
            String result = CdmUtils.concat(UTF8.EN_DASH_SPATIUM.toString(), minStr, maxStr);
            if (StringUtils.isNotBlank(result) && StringUtils.isNotBlank(unit)){
                result = result + " " + unit;
            }
            return result;
        }
    }
}
