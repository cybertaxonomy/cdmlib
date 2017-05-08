/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.common;

import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author a.kohlbecker
 * @since May 8, 2017
 *
 */
public class PropertyNameMatchMode {

    private String propertyName;

    private MatchMode matchMode;



    /**
     * @param propertyName
     * @param matchMode
     */
    public PropertyNameMatchMode(String propertyName, MatchMode matchMode) {
        this.propertyName = propertyName;
        this.matchMode = matchMode;
    }

    /**
     * @return the propertyName
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * @param propertyName the propertyName to set
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * @return the matchMode
     */
    public MatchMode getMatchMode() {
        return matchMode;
    }

    /**
     * @param matchMode the matchMode to set
     */
    public void setMatchMode(MatchMode matchMode) {
        this.matchMode = matchMode;
    }


}
