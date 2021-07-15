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

    private static final long serialVersionUID = -333679029042923881L;

    boolean ignoreIsUsedInCdmLinkSource = false;
    boolean deleteAllSpecimenDescriptions = false;
    boolean deleteAllLiteratureDescriptions = false;
    boolean deleteAllDefaultDescriptions = false;
    boolean deleteAllAggregatedDescriptions = true;

    public boolean isIgnoreIsUsedInCdmLinkSource() {
        return ignoreIsUsedInCdmLinkSource;
    }
    public void setIgnoreIsUsedInCdmLinkSource(boolean ignoreIsUsedInCdmLinkSource) {
        this.ignoreIsUsedInCdmLinkSource = ignoreIsUsedInCdmLinkSource;
    }

    public boolean isDeleteAllSpecimenDescriptions() {
        return deleteAllSpecimenDescriptions;
    }
    public void setDeleteAllSpecimenDescriptions(boolean deleteAllSpecimenDescriptions) {
        this.deleteAllSpecimenDescriptions = deleteAllSpecimenDescriptions;
    }

    public boolean isDeleteAllLiteratureDescriptions() {
        return deleteAllLiteratureDescriptions;
    }
    public void setDeleteAllLiteratureDescriptions(boolean deleteAllLiteratureDescriptions) {
        this.deleteAllLiteratureDescriptions = deleteAllLiteratureDescriptions;
    }

    public boolean isDeleteAllDefaultDescriptions() {
        return deleteAllDefaultDescriptions;
    }
    public void setDeleteAllDefaultDescriptions(boolean deleteAllDefaultDescriptions) {
        this.deleteAllDefaultDescriptions = deleteAllDefaultDescriptions;
    }

    public boolean isDeleteAllAggregatedDescriptions() {
        return deleteAllAggregatedDescriptions;
    }
    public void setDeleteAllAggregatedDescriptions(boolean deleteAllAggregatedDescriptions) {
        this.deleteAllAggregatedDescriptions = deleteAllAggregatedDescriptions;
    }
}