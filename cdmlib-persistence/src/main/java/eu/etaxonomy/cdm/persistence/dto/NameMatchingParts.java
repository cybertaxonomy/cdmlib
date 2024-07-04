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

import eu.etaxonomy.cdm.model.name.Rank;

/**
 * @author andreabee90
 */
public class NameMatchingParts {

    private Integer taxonNameId;

    private UUID taxonNameUuid;

    private String titleCache;

    private String authorshipCache;

    private String genusOrUninomial;

    private String infraGenericEpithet;

    private String specificEpithet;

    private String infraSpecificEpithet;

    private String nameCache;

    private Rank rank;

    private String combinationAuthorship;

    private String exCombinationAuthorship;

    private String basionymAuthorship;

    private String exBasionymAuthorship;

//************ CONSTRUCTOR ***********************/

    public NameMatchingParts() {
    }

    public NameMatchingParts(Integer taxonNameId, UUID taxonNameUuid, String titleCache, String authorshipCache,
            String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet,
            String nameCache, Rank rank, String combinationAuthorship, String exCombinationAuthorship, String basionymAuthorship,
            String exBasionymAuthorship) {

        this.taxonNameId = taxonNameId;
        this.taxonNameUuid = taxonNameUuid;
        this.titleCache = titleCache;
        this.authorshipCache = authorshipCache;
        this.genusOrUninomial = genusOrUninomial;
        this.infraGenericEpithet = infraGenericEpithet;
        this.specificEpithet = specificEpithet;
        this.infraSpecificEpithet = infraSpecificEpithet;
        this.nameCache = nameCache;
        this.rank = rank;
        this.combinationAuthorship = combinationAuthorship;
        this.exCombinationAuthorship = exCombinationAuthorship;
        this.basionymAuthorship = basionymAuthorship;
        this.exBasionymAuthorship = exBasionymAuthorship;
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

    public String getNameCache() {
        return nameCache;
    }

    public void setNameCache(String nameCache) {
        this.nameCache = nameCache;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public String getCombinationAuthorship() {
        return combinationAuthorship;
    }

    public void setCombinationAuthorship(String combinationAuthorship) {
        this.combinationAuthorship = combinationAuthorship;
    }

    public String getExCombinationAuthorship() {
        return exCombinationAuthorship;
    }

    public void setExCombinationAuthorship(String exCombinationAuthorship) {
        this.exCombinationAuthorship = exCombinationAuthorship;
    }

    public String getBasionymAuthorship() {
        return basionymAuthorship;
    }

    public void setBasionymAuthorship(String basionymAuthorship) {
        this.basionymAuthorship = basionymAuthorship;
    }

    public String getExBasionymAuthorship() {
        return exBasionymAuthorship;
    }

    public void setExBasionymAuthorship(String exBasionymAuthorship) {
        this.exBasionymAuthorship = exBasionymAuthorship;
    }

 // ************************** TO STRING *************************************/

    @Override
    public String toString() {
        return "NameMatchingParts [taxonNameId=" + taxonNameId + ", genusOrUninomial=" + genusOrUninomial
                + ", infraGenericEpithet=" + infraGenericEpithet + ", specificEpithet=" + specificEpithet
                + ", infraSpecificEpithet=" + infraSpecificEpithet + ", nameCache=" + nameCache + ", rank=" + rank + "]";
    }
}