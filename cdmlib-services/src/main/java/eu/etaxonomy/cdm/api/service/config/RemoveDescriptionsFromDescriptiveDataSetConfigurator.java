/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

/**
 * @author k.luther
 * @since Jun 19, 2020
 */
public class RemoveDescriptionsFromDescriptiveDataSetConfigurator extends DeleteConfiguratorBase {

    private static final long serialVersionUID = -3948463852000326551L;

    boolean onlyRemoveDescriptionsFromDataSet = true;


    public boolean isOnlyRemoveDescriptionsFromDataSet() {
        return onlyRemoveDescriptionsFromDataSet;
    }
    public void setOnlyRemoveDescriptionsFromDataSet(boolean onlyRemoveDescriptionsFromDataSet) {
        this.onlyRemoveDescriptionsFromDataSet = onlyRemoveDescriptionsFromDataSet;
    }
}