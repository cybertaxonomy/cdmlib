/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

/**
 * @author k.luther
 * @since May 26, 2021
 */
public enum EnabledComputedDescription implements IKeyLabel {

    Enabled("enabled", "Enabled"),
    Disabled("disabled", "Disabled"),
    Invisible("invisible", "Invisible");

    private String label;
    private String key;

    private EnabledComputedDescription(String key, String label){
        this.label = label;
        this.key = key;
    }

    @Override
    public String getLabel() {
       return label;
    }

    @Override
    public String getKey() {
        return key;
    }

    public static EnabledComputedDescription byKey(String key){
        for (EnabledComputedDescription searchField : values()){
            if (searchField.key.equals(key)){
                return searchField;
            }
        }
        throw new IllegalArgumentException();
    }

}
