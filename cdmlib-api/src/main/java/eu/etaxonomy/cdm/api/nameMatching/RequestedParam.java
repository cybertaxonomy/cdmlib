/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.nameMatching;

import java.util.List;

/**
 * @author andreabee90
 * @since 14.05.2024
 */
public class RequestedParam {

    private String scientificName;
    private List <String> scientificNameList;
    private boolean compareAuthor;
    private Integer maxDistance;
    private boolean excludeBasyonymAuthors;
    private boolean excludeExAuthors;

    public RequestedParam (String scientificName, boolean compareAuthor, Integer maxDistance,
            boolean excludeBasyonymAuthors, boolean excludeExAuthors) {
        this.scientificName = scientificName;
        this.compareAuthor = compareAuthor;
        this.maxDistance = maxDistance;
        this.excludeBasyonymAuthors = excludeBasyonymAuthors;
        this.excludeExAuthors = excludeExAuthors;
    }
    public RequestedParam (List<String> scientificNameList, boolean compareAuthor, Integer maxDistance,
            boolean excludeBasyonymAuthors, boolean excludeExAuthors) {
        this.scientificNameList = scientificNameList;
        this.compareAuthor = compareAuthor;
        this.maxDistance = maxDistance;
        this.excludeBasyonymAuthors = excludeBasyonymAuthors;
        this.excludeExAuthors = excludeExAuthors;
    }
    public String getScientificName() {
        return scientificName;
    }
    public boolean isCompareAuthor() {
        return compareAuthor;
    }
    public Integer getMaxDistance() {
        return maxDistance;
    }
    public List<String> getScientificNameList() {
        return scientificNameList;
    }
    public void setScientificNameList(List<String> scientificNameList) {
        this.scientificNameList = scientificNameList;
    }
    public boolean isExcludeBasionymAuthors() {
        return excludeBasyonymAuthors;
    }
    public void setExcludeBasyonymAuthors(boolean excludeBasyonymAuthors) {
        this.excludeBasyonymAuthors = excludeBasyonymAuthors;
    }
    public boolean isExcludeExAuthors() {
        return excludeExAuthors;
    }
    public void setExcludeExAuthors(boolean excludeExAuthors) {
        this.excludeExAuthors = excludeExAuthors;
    }
}
