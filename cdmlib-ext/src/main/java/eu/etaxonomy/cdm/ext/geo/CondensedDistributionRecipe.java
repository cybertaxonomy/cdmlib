/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.geo;

import eu.etaxonomy.cdm.model.metadata.IKeyLabel;

/**
 * @author a.kohlbecker
 * @since Jun 24, 2015
 *
 * @deprecated the usage of this class is deprecated, please use {@link CondensedDistributionConfiguration} instead
 */
 @Deprecated
public enum CondensedDistributionRecipe implements IKeyLabel{

    /**
     * The recipe for creation of the condensed distribution strings
     * as used in Euro+Med.
     *
     * For reference see:
     * <ul>
     *   <li>{@link http://ww2.bgbm.org/EuroPlusMed/explanations.asp}</li>
     *   <li>{@link http://dev.e-taxonomy.eu/trac/ticket/3907}</li>
     * </ul>
     */
    EuroPlusMed("EuroPlusMed", "Euro + Med"),
    FloraCuba("FloraCuba", "Flora Cuba");

    private String label;
    private String key;

    private CondensedDistributionRecipe(String key, String label) {
        this.key = key;
        this.label = label;
    }

    @Override
    public String getLabel() {
       return label;
    }

    @Override
    public String getKey() {
        return key;
    }

    public CondensedDistributionConfiguration toConfiguration(){
        if (this == FloraCuba){
            return CondensedDistributionConfiguration.NewCubaInstance();
        } else {
            return CondensedDistributionConfiguration.NewDefaultInstance();
        }
    }

    public CondensedDistributionRecipe byKey(String key) {
        if (key == null){
            return null;
        }else{
            for (CondensedDistributionRecipe rec: values()){
                if (rec.getKey().equalsIgnoreCase(key)){
                    return rec;
                }
            }
        }
        return null;
    }
}
