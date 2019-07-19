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
 * @since 19.11.2018
 *
 */
public enum NodeOrderEnum implements IKeyLabel{
    AlphabeticalOrder("AlphabeticalOrder", "Alphabetical"), //$NON-NLS-1$
    NaturalOrder("NaturalOrder", "Natural"), //$NON-NLS-1$
    RankAndNameOrder("RankAndNameOrder", "Rank and Name"); //$NON-NLS-1$


    String label;
    String key;

    private NodeOrderEnum(String key, String label){
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
