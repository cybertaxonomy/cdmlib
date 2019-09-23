/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

/**
 * @author k.luther
 * @since 12.09.2019
 */
public enum NamedAreaSearchField implements IKeyLabel{
    IDInVocabulary("idInVocabulary", "ID in vocabulary"),
    Symbol1("symbol1", "First symbol"),
    Symbol2("symbol2", "Second symbol"),
    NoAbbrev("titleCache", "Title cache");


    String label;
    String key;

    private NamedAreaSearchField(String key, String label){
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

    public static NamedAreaSearchField byKey(String key){
        for (NamedAreaSearchField searchField : values()){
            if (searchField.key.equals(key)){
                return searchField;
            }
        }
        throw new IllegalArgumentException();
    }

}
