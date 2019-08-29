/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import java.io.Serializable;

/**
 * @author pplitzner
 * @since Aug 8, 2019
 *
 */
public class DescriptionAggregationConfiguration implements Serializable {
    boolean recursiveAggregation = true;
    boolean includeDefault = true;
    boolean includeLiterature = false;

    public boolean isRecursiveAggregation() {
        return recursiveAggregation;
    }
    public void setRecursiveAggregation(boolean recursiveAggregation) {
        this.recursiveAggregation = recursiveAggregation;
    }
    public boolean isIncludeDefault() {
        return includeDefault;
    }
    public void setIncludeDefault(boolean includeDefault) {
        this.includeDefault = includeDefault;
    }
    public boolean isIncludeLiterature() {
        return includeLiterature;
    }
    public void setIncludeLiterature(boolean includeLiterature) {
        this.includeLiterature = includeLiterature;
    }

}
