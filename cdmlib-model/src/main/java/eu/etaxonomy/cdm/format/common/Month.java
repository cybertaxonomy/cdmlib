/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.common;

/**
 * @author a.mueller
 * @since 25.06.2020
 */
public enum Month {
    JANUARY(1,"Jan"),
    FEBRUARY(2,"Feb"),
    MARCH(3,"Mar"),
    APRIL(4,"Apr"),
    MAY(5,"May"),
    JUNE(6,"Jun"),
    JULY(7,"Jul"),
    AUGUST(8,"Aug"),
    SEPTEMRBER(9,"Sep"),
    OCTOBER(10,"Oct"),
    NOVEMBER(11,"Nov"),
    DECEMBER(12,"Dec");

    int n;
    String abbrev;

    private Month(int n, String abbrev){
        this.n = n;
        this.abbrev = abbrev;
    }

    public static Month valueOf(int nr){
        for (Month month : values()){
            if (month.n == nr){
                return month;
            }
        }
        return null;
    }

    public String abbrev(){
        return abbrev;
    }
}
