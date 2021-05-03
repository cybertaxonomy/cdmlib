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
 * @since 6 Feb 2019
 *
 */
public enum TermOrder implements IKeyLabel{

    IdInVoc("IdInVoc", "Id in Vocabulary"),
    Label("Label", "Label"),
    Natural("Natural", "Natural");

    private String label;
    private String key;

    private TermOrder(String key, String label){
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

    @Override
    public String toString(){
        return key;
    }
}
