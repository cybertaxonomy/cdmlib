/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common.function;

import java.math.BigDecimal;

/**
 * This is class is for testing how BigDecimal actually works
 * and how float can be transformed to BigDecimal.
 * Can be deleted.
 *
 * @author a.mueller
 * @since 27.04.2020
 */
public class TestBigDecimal {

    public static void main(String[] args) {
        BigDecimal a = new BigDecimal(new Float(2.600f).toString());
        System.out.println(a.toString());
        System.out.println(a.toPlainString());
        System.out.println(a.toEngineeringString());
        String str = a.toString();
        String strZero = "000";
        String strNine = "999";
        if (str.contains(strZero)){
            int pos = str.indexOf(strZero);
            String str2 = str.substring(0, pos);
            BigDecimal b = new BigDecimal(str2);
            System.out.println(b.toString());
        }else if (str.contains(strNine)){
            int pos = str.indexOf(strNine);
            String str2 = str.substring(0, pos);
            BigDecimal b = new BigDecimal(str2);
            b = b.add(new BigDecimal(1).movePointLeft(b.scale()));
            Float f = Float.parseFloat(b.toString());
            System.out.println(b.toString());
            System.out.println(f.toString());
        }
    }
}
