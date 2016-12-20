/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

/**
 * @author k.luther
 * @date 10.03.2016
 *
 */
public class MediaDeletionConfigurator extends DeleteConfiguratorBase {
    private boolean deleteIfUsedInTaxonDescription = false;
    private boolean deleteIfUsedInSpecimenDescription = false;
    private boolean onlyRemoveFromGallery = false;

    public boolean isDeleteIfUsedInSpecimenDescription() {
        return deleteIfUsedInSpecimenDescription;
    }
    public void setDeleteIfUsedInSpecimenDescription(boolean deleteIfUsedInSpecimenDescription) {
        this.deleteIfUsedInSpecimenDescription = deleteIfUsedInSpecimenDescription;
    }
    public boolean isDeleteIfUsedInTaxonDescription() {
        return deleteIfUsedInTaxonDescription;
    }
    public void setDeleteIfUsedInTaxonDescription(boolean deleteIfUsedInTaxonDescription) {
        this.deleteIfUsedInTaxonDescription = deleteIfUsedInTaxonDescription;
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
}
