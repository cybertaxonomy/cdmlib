/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

/**
 * @author k.luther
 * @since Dec 1, 2020
 */
public enum SecReferenceHandlingEnum implements IKeyLabel {

    AlwaysDelete("AlwaysDelete", "Always delete"),
    WarningSelect("WarningSelect", "Warning and select"),
    KeepWhenSame("KeepWhenSame", "Keep if syn sec and new parent sec are the same, warn otherwise"),
    KeepAlways("KeepAlways", "Keep always"),
    UseNewParentSec("UseNewParentSec", "Always use new parent sec");
//    UseOldParentSec("UseOldParentSec", "Always use old parent sec");

    String label;
    String key;

    private SecReferenceHandlingEnum(String key, String label){
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

}
