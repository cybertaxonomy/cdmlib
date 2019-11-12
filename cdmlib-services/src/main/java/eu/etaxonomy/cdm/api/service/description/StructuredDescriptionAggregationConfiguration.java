/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.description;

/**
 * @author a.mueller
 * @since 12.11.2019
 */
public class StructuredDescriptionAggregationConfiguration
        extends DescriptionAggregationConfigurationBase {

    private static final long serialVersionUID = 7485291596888612932L;

    boolean includeDefault = true;
    boolean includeLiterature = false;

// *********************** GETTER / SETTER ****************************/

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
