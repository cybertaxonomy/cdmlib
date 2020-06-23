/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

/**
 * @author a.mueller
 * @since 25.04.2020
 */
public final class BigDecimalUtil {

    public static final BigDecimal MAX_BIGDECIMAL = BigDecimal.valueOf(Double.MAX_VALUE);

    public static final BigDecimal MIN_BIGDECIMAL = BigDecimal.valueOf(Double.MIN_VALUE);

    public static BigDecimal average(Set<BigDecimal> bigDecimals) {
        BigDecimal sum = BigDecimal.ZERO;
        int count=0;
        for(BigDecimal bigDecimal : bigDecimals) {
            if(null != bigDecimal) {
                sum = sum.add(bigDecimal);
                count++;
            }
        }
        return sum.divide(new BigDecimal(count), RoundingMode.HALF_EVEN);
    }
}
