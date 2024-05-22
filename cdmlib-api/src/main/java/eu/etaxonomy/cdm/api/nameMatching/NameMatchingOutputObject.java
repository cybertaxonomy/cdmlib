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
 * @since 21.05.2024
 */
public class NameMatchingOutputObject {
    private RequestedParam request;
    private NameMatchingCombinedResult results = new NameMatchingCombinedResult();
    private String warning;

    public NameMatchingCombinedResult getResults() {
        return results;
    }
    public void setResults(NameMatchingCombinedResult results) {
        this.results = results;
    }
    public RequestedParam getRequest() {
        return request;
    }
    public void setRequest(RequestedParam request) {
        this.request = request;
    }
    public String getWarning() {
        return warning;
    }
    public void setWarning(String warning) {
        this.warning = warning;
    }
}
