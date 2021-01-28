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
 * @since 09.01.2019
 */
public enum TermDisplayEnum implements IKeyLabel{

    IdInVocabulary("IdInVocabulary", "ID in Vocabulary"),
    Symbol1("Symbol1", "Symbol 1"),
    Symbol2("Symbol2", "Symbol 2"),
    Title("Label", "Label");

    String label;
    String key;

    private TermDisplayEnum(String key, String label){
        this.label = label;
        this.key = key;
    }

    @Override
    public String getLabel(){
        return label;
    }

    @Override
    public String getKey(){
        return key;
    }

    public static TermDisplayEnum byKey(String key){
        for (TermDisplayEnum termDisplay : values()){
            if (termDisplay.key.equals(key)){
                return termDisplay;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public String toString(){
        return key;
    }
}
