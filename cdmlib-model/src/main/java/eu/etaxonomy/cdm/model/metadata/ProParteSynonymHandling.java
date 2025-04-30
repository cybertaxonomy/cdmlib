/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

/**
 * @author kluther
 * @since 10.04.2025
 */
public enum ProParteSynonymHandling implements IKeyLabel{
    EnablePP("enableProParte", "Pro parte"),
    EnablePP_Partial("enablePP_Partial", "Pro parte and partial"),
    Disable("disable", "Disable both");

    private String label;
    private String key;

    private ProParteSynonymHandling(String key, String label){
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

    public static ProParteSynonymHandling byKey(String key){
        for (ProParteSynonymHandling searchField : values()){
            if (searchField.key.equals(key)){
                return searchField;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public String toString(){
        return key;
    }

}
