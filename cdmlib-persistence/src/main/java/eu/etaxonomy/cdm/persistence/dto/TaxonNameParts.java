/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.model.name.Rank;

public class TaxonNameParts {

    protected Integer taxonNameId;

    protected UUID taxonNameUuid;

    protected Rank rank;

    protected String genusOrUninomial;

    protected String infraGenericEpithet;

    protected String specificEpithet;

    protected String infraSpecificEpithet;

//************ CONSTRUCTOR ***********************/

    public TaxonNameParts() {
    }

    public TaxonNameParts(Integer taxonNameId, UUID taxonNameUuid, Rank rank, String genusOrUninomial, String infraGenericEpithet,
            String specificEpithet, String infraSpecificEpithet) {

        this.taxonNameId = taxonNameId;
        this.taxonNameUuid = taxonNameUuid;
        this.rank = rank;
        this.genusOrUninomial = genusOrUninomial;
        this.infraGenericEpithet = infraGenericEpithet;
        this.specificEpithet = specificEpithet;
        this.infraSpecificEpithet = infraSpecificEpithet;
    }

//***************** GETTER / SETTER ********************************/

    public UUID getTaxonNameUuid() {
        return taxonNameUuid;
    }

    public Integer getTaxonNameId() {
        return taxonNameId;
    }
    public void setTaxonNameId(Integer taxonNameId) {
        this.taxonNameId = taxonNameId;
    }

    public Rank getRank() {
        return rank;
    }
    public void setRank(Rank rank) {
        this.rank = rank;
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

// ******************* METHODS *************************************/

    public String rankSpecificNamePart() {
        if (rank == null) {
            return null;
        }
        if(rank.isGenus() || rank.isSupraGeneric()){
            return getGenusOrUninomial();
        }
        if(rank.isInfraGeneric()){
            return getInfraGenericEpithet();
        }
        if(rank.isSpecies()){
            return getSpecificEpithet();
        }
        if(rank.isInfraSpecific()){
            return getInfraSpecificEpithet();
        }
        return "-- ERROR: INVALID OR UNSUPPORTED RANK (" + rank.getLabel() + ") --";
    }

 // ************************** TO STRING *************************************/

    @Override
    public String toString() {
        return "TaxonNameParts [taxonNameId=" + taxonNameId + ", genusOrUninomial=" + genusOrUninomial
                + ", infraGenericEpithet=" + infraGenericEpithet + ", specificEpithet=" + specificEpithet
                + ", infraSpecificEpithet=" + infraSpecificEpithet + "]";
    }
}