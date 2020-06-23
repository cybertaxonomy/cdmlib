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
 * @since May 5, 2020
 */
public class DeleteDescriptiveDataSetConfigurator extends DeleteConfiguratorBase {

    boolean ignoreIsUsedInCdmLinkSource = false;
    boolean deleteAllSpecimenDescriptions = false;
    boolean deleteAllLiteratureDescriptions = false;
    boolean deleteAllDefaultDescriptions = false;
    boolean deleteAllAggregatedDescriptions = true;

    /**
     * @return the ignoreIsUsedInCdmLinkSource
     */
    public boolean isIgnoreIsUsedInCdmLinkSource() {
        return ignoreIsUsedInCdmLinkSource;
    }

    /**
     * @param ignoreIsUsedInCdmLinkSource the ignoreIsUsedInCdmLinkSource to set
     */
    public void setIgnoreIsUsedInCdmLinkSource(boolean ignoreIsUsedInCdmLinkSource) {
        this.ignoreIsUsedInCdmLinkSource = ignoreIsUsedInCdmLinkSource;
    }

    /**
     * @return the deleteAllSpecimenDescriptions
     */
    public boolean isDeleteAllSpecimenDescriptions() {
        return deleteAllSpecimenDescriptions;
    }

    /**
     * @param deleteAllSpecimenDescriptions the deleteAllSpecimenDescriptions to set
     */
    public void setDeleteAllSpecimenDescriptions(boolean deleteAllSpecimenDescriptions) {
        this.deleteAllSpecimenDescriptions = deleteAllSpecimenDescriptions;
    }

    /**
     * @return the deleteAllLiteratureDescriptions
     */
    public boolean isDeleteAllLiteratureDescriptions() {
        return deleteAllLiteratureDescriptions;
    }

    /**
     * @param deleteAllLiteratureDescriptions the deleteAllLiteratureDescriptions to set
     */
    public void setDeleteAllLiteratureDescriptions(boolean deleteAllLiteratureDescriptions) {
        this.deleteAllLiteratureDescriptions = deleteAllLiteratureDescriptions;
    }

    /**
     * @return the deleteAllDefaultDescriptions
     */
    public boolean isDeleteAllDefaultDescriptions() {
        return deleteAllDefaultDescriptions;
    }

    /**
     * @param deleteAllDefaultDescriptions the deleteAllDefaultDescriptions to set
     */
    public void setDeleteAllDefaultDescriptions(boolean deleteAllDefaultDescriptions) {
        this.deleteAllDefaultDescriptions = deleteAllDefaultDescriptions;
    }

    /**
     * @return the deleteAllAggregatedDescriptions
     */
    public boolean isDeleteAllAggregatedDescriptions() {
        return deleteAllAggregatedDescriptions;
    }

    /**
     * @param deleteAllAggregatedDescriptions the deleteAllAggregatedDescriptions to set
     */
    public void setDeleteAllAggregatedDescriptions(boolean deleteAllAggregatedDescriptions) {
        this.deleteAllAggregatedDescriptions = deleteAllAggregatedDescriptions;
    }


}
