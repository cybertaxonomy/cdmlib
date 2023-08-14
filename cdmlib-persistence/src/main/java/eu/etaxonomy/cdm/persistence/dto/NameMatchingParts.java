/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dto;

import java.util.UUID;

/**
 * @author andreabee90
 *
 */
public class NameMatchingParts {

    protected Integer taxonNameId;

    protected UUID taxonNameUuid;

    protected String titleCache;

    protected String authorshipCache;

    protected String genusOrUninomial;

    protected String infraGenericEpithet;

    protected String specificEpithet;

    protected String infraSpecificEpithet;


//************ CONSTRUCTOR ***********************/

    public NameMatchingParts() {
    }

    public NameMatchingParts(Integer taxonNameId, UUID taxonNameUuid, String titleCache, String authorshipCache,
            String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet) {
        super();
        this.taxonNameId = taxonNameId;
        this.taxonNameUuid = taxonNameUuid;
        this.titleCache = titleCache;
        this.authorshipCache = authorshipCache;
        this.genusOrUninomial = genusOrUninomial;
        this.infraGenericEpithet = infraGenericEpithet;
        this.specificEpithet = specificEpithet;
        this.infraSpecificEpithet = infraSpecificEpithet;
    }

//***************** GETTER / SETTER ********************************/
    public Integer getTaxonNameId() {
        return taxonNameId;
    }

    public void setTaxonNameId(Integer taxonNameId) {
        this.taxonNameId = taxonNameId;
    }

    public UUID getTaxonNameUuid() {
        return taxonNameUuid;
    }

    public void setTaxonNameUuid(UUID taxonNameUuid) {
        this.taxonNameUuid = taxonNameUuid;
    }

    public String getTitleCache() {
        return titleCache;
    }

    public void setTitleCache(String titleCache) {
        this.titleCache = titleCache;
    }

    public String getAuthorshipCache() {
        return authorshipCache;
    }

    public void setAuthorshipCache(String authorshipCache) {
        this.authorshipCache = authorshipCache;
    }

    public String getGenusOrUninomial() {
        return genusOrUninomial;
    }

    public void setGenusOrUninomial(String genusOrUninomial) {
        this.genusOrUninomial = genusOrUninomial;
    }

    public String getInfraGenericEpithet() {
        return infraGenericEpithet;
    }

    public void setInfraGenericEpithet(String infraGenericEpithet) {
        this.infraGenericEpithet = infraGenericEpithet;
    }

    public String getSpecificEpithet() {
        return specificEpithet;
    }

    public void setSpecificEpithet(String specificEpithet) {
        this.specificEpithet = specificEpithet;
    }

    public String getInfraSpecificEpithet() {
        return infraSpecificEpithet;
    }

    public void setInfraSpecificEpithet(String infraSpecificEpithet) {
        this.infraSpecificEpithet = infraSpecificEpithet;
    }

 // ************************** TO STRING *************************************/

    @Override
    public String toString() {
        return "NameMatchingParts [taxonNameId=" + taxonNameId + ", genusOrUninomial=" + genusOrUninomial
                + ", infraGenericEpithet=" + infraGenericEpithet + ", specificEpithet=" + specificEpithet
                + ", infraSpecificEpithet=" + infraSpecificEpithet + "]";
    }
}