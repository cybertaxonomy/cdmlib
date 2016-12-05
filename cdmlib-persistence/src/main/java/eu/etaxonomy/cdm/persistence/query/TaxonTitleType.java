/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.query;

/**
 * @author a.mueller
 * @date 03.12.2016
 *
 */
public enum TaxonTitleType {
    TAXON,  //Taxon title cache
    NAME,   //Name title cache
    PURE_NAME; //Name name cache

    /**
     * @return
     */
    public static TaxonTitleType DEFAULT() {
        return TAXON;
    }

    public String hqlJoin(){
        if (this == NAME || this == PURE_NAME){
            return " LEFT JOIN c.name as n ";
        }else{
            return "";
        }
    }

    public String hqlReplaceSelect(String select, String clazzPart){
        if (this == NAME ){
            return select.replace(clazzPart, "n.titleCache");
        }else if (this == PURE_NAME){
            return select.replace(clazzPart, "n.nameCache");
        }else{
            return select;
        }
    }

}
