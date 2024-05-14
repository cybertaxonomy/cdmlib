/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.nameMatching;

/**
 * @author andreabee90
 * @since 14.05.2024
 */
public class RequestedParam {

    private String scientificName;

    private boolean compareAuthor;

    private Integer maxDistance;

    public RequestedParam (String scientificName, boolean compareAuthor, Integer maxDistance) {
        this.scientificName = scientificName;
        this.compareAuthor = compareAuthor;
        this.maxDistance = maxDistance;
    }
    /**
     * @return the scientificName
     */
    public String getScientificName() {
        return scientificName;
    }
    /**
     * @return the compareAuthor
     */
    public boolean isCompareAuthor() {
        return compareAuthor;
    }
    /**
     * @return the maxDistance
     */
    public Integer getMaxDistance() {
        return maxDistance;
    }


}
