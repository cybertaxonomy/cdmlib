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
public enum TermOrder {

    IdInVoc("IdInVoc", "Id in Vocabulary"),
    Title("Title", "Title"),
    Natural("Natural", "Natural");


    String label;
    String key;

    private TermOrder(String key, String label){
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
