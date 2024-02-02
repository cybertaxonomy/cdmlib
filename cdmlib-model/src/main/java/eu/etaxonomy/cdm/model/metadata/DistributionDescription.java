// $Id$
/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

/**
 * @author katjaluther
 * @date 10.10.2023
 *
 */
public enum DistributionDescription implements IKeyLabel{
    AlwaysDefault("AlwaysDefault", "Use default description (create if not exist)"),
   // OwnDescriptionForDistributions("OwnDescription", "Use own description for distributions"),
    UseAlreadyExisting("AlreadyExisting", "Use default description if exist, any otherwise");
   // DescriptionsPerSource("DescriptionPerSource", "Every source has its own description");


    private String label;
    private String key;

    private DistributionDescription(String key, String label) {
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

    @Override
    public String toString() {
        return key;
    }

    public static DistributionDescription byKey(String key){
        for (DistributionDescription distributionDesc : values()){
            if (distributionDesc.key.equals(key)){
                return distributionDesc;
            }
        }
        throw new IllegalArgumentException();
    }

}
