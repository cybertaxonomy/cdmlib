/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author k.luther
 \* @since 10.03.2016
 *
 */
public class MediaDeletionConfigurator extends DeleteConfiguratorBase {

    private boolean deleteFromDescription = false;
    private boolean deleteFromEveryWhere = false;
    private boolean onlyRemoveFromGallery = true;
    private CdmBase deleteFrom;


    /**
     * @return the onlyRemoveFromGallery
     */
    public boolean isOnlyRemoveFromGallery() {
        return onlyRemoveFromGallery;
    }
    /**
     * @param onlyRemoveFromGallery the onlyRemoveFromGallery to set
     */
    public void setOnlyRemoveFromGallery(boolean onlyRemoveFromGallery) {
        this.onlyRemoveFromGallery = onlyRemoveFromGallery;
    }

    public CdmBase getDeleteFrom() {
        return deleteFrom;
    }
    public void setDeleteFrom(CdmBase deleteFrom) {
        this.deleteFrom = deleteFrom;
    }
    public boolean isDeleteFromEveryWhere() {
        return deleteFromEveryWhere;
    }
    public void setDeleteFromEveryWhere(boolean deleteFromEveryWhere) {
        this.deleteFromEveryWhere = deleteFromEveryWhere;
    }
    public boolean isDeleteFromDescription() {
        return deleteFromDescription;
    }
    public void setDeleteFromDescription(boolean deleteFromDescription) {
        this.deleteFromDescription = deleteFromDescription;
    }
}
