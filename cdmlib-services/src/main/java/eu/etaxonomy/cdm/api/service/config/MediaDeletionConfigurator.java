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
 * @date 10.03.2016
 *
 */
public class MediaDeletionConfigurator extends DeleteConfiguratorBase {
    private boolean deleteFromAllTaxonDescription = false;
    private boolean deleteFromAllSpecimenDescription = false;
    private boolean deleteFromAllNameDescription = false;
    private boolean deleteFromEveryWhere = false;
    private boolean onlyRemoveFromGallery = true;
    private CdmBase deleteFrom;

    public boolean isDeleteIfUsedInSpecimenDescription() {
        return deleteFromAllSpecimenDescription;
    }
    public void setDeleteIfUsedInSpecimenDescription(boolean deleteIfUsedInSpecimenDescription) {
        this.deleteFromAllSpecimenDescription = deleteIfUsedInSpecimenDescription;
    }
    public boolean isDeleteIfUsedInTaxonDescription() {
        return deleteFromAllTaxonDescription;
    }
    public void setDeleteIfUsedInTaxonDescription(boolean deleteIfUsedInTaxonDescription) {
        this.deleteFromAllTaxonDescription = deleteIfUsedInTaxonDescription;
    }
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
    public boolean isDeleteIfUsedInNameDescription() {
        return deleteFromAllNameDescription;
    }
    public void setDeleteIfUsedInNameDescription(boolean deleteIfUsedInNameDescription) {
        this.deleteFromAllNameDescription = deleteIfUsedInNameDescription;
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
}
