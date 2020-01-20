/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import java.util.Comparator;

/**
 * @author a.mueller
 * @since 10.01.2020
 */
public class StringComparator implements Comparator<String> {

    public static final StringComparator Instance = new StringComparator();

    @Override
    public int compare(String str1, String str2) {

        if (str1 == str2) {
            return 0;
        }
        if (str1 == null) {
            return -1;
        }
        if (str2 == null) {
            return 1;
        }
        return str1.compareTo(str2);
    }
}