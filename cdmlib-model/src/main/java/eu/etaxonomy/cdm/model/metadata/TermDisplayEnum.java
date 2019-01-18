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
 *
 */
public enum TermDisplayEnum {
    IdInVocabulary("IdInVocabulary", "Show IdInVocabulary"),
    Symbol1("Symbol1", "Show Symbol 1"),
    Symbol2("Symbol2", "Show Symbol 2"),
    Title("Title", "Show Title");

    String label;
    String key;

    private TermDisplayEnum(String key, String label){
        this.label = label;
        this.key = key;
    }

    public String getLabel(){
        return label;
    }

    public String getKey(){
        return key;
    }
}
