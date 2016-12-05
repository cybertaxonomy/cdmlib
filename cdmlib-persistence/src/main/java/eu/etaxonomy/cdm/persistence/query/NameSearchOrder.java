/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.query;

/**
 * @author a.mueller
 * @date 02.12.2016
 *
 */
public enum NameSearchOrder {
    ALPHA,
    LENGTH_ALPHA_NAME,
    LENGTH_ALPHA_TITLE,
    ;

    /**
     * @return
     */
    public static NameSearchOrder DEFAULT() {
        return ALPHA;
    }
}
