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

//    AlwaysDelete("AlwaysDelete", "Remove all related secundum references"),
//    WarningSelect("WarningSelect", "If secs differ select new one"),
//    WarnWhenDifferent("WarnWhenDifferent", "Warn if secs are different"),

    AlwaysDelete("AlwaysDelete", "Always remove secundum references"),
    AlwaysSelect("AlwaysSelect", "Always select new secundum references"),
    KeepOrSelect("KeepOrSelect", "Keep if all related secs are the same, select otherwise"),
    KeepOrWarn("KeepOrWarn", "Keep if all related secs are the same, warn otherwise"),
    UseNewParentSec("UseNewParentSec", "Always use new parent sec");


    private String label;
    private String key;

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