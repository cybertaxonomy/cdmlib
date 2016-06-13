// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate;

import java.util.Collection;

/**
 * Helper class to remove null values from collections which are left over artifacts due to
 * https://hibernate.atlassian.net/browse/HHH-9751
 *
 * @author a.kohlbecker
 * @date Jun 13, 2016
 *
 */
public class HHH_9751_Util {

    /**
     *
     * @param collection
     * @return the number of null values removed from the collection
     */
    static public int removeAllNull(Collection collection) {
        int cnt = 0;
        if (collection.contains(null)){
            while(collection.contains(null)){
                cnt++;
                collection.remove(null);
            }
        }
        return cnt;
    }

}
