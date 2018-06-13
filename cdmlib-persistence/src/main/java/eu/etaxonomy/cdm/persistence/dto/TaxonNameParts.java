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

    /**
     * @return the taxonNameId
     */
    public Integer getTaxonNameId() {
        return taxonNameId;
    }


    /**
     * @param taxonNameId the taxonNameId to set
     */
    public void setTaxonNameId(Integer taxonNameId) {
        this.taxonNameId = taxonNameId;
    }


    /**
     * @return the rank
     */
    public Rank getRank() {
        return rank;
    }


    /**
     * @param rank the rank to set
     */
    public void setRank(Rank rank) {
        this.rank = rank;
    }


    /**
     * @return the uninomial
     */
    public String getGenusOrUninomial() {
        return genusOrUninomial;
    }


    /**
     * @param uninomial the genusOrUninomial to set
     */
    public void setGenusOrUninomial(String genusOrUninomial) {
        this.genusOrUninomial = genusOrUninomial;
    }


    /**
     * @return the infraGenericEpithet
     */
    public String getInfraGenericEpithet() {
        return infraGenericEpithet;
    }


    /**
     * @param infraGenericEpithet the infraGenericEpithet to set
     */
    public void setInfraGenericEpithet(String infraGenericEpithet) {
        this.infraGenericEpithet = infraGenericEpithet;
    }


    /**
     * @return the specificEpithet
     */
    public String getSpecificEpithet() {
        return specificEpithet;
    }


    /**
     * @param specificEpithet the specificEpithet to set
     */
    public void setSpecificEpithet(String specificEpithet) {
        this.specificEpithet = specificEpithet;
    }


    /**
     * @return the infraSpecificEpithet
     */
    public String getInfraSpecificEpithet() {
        return infraSpecificEpithet;
    }


    /**
     * @param infraSpecificEpithet the infraSpecificEpithet to set
     */
    public void setInfraSpecificEpithet(String infraSpecificEpithet) {
        this.infraSpecificEpithet = infraSpecificEpithet;
    }


    /**
     * @param taxonNameId
     * @param rank
     * @param uninomial
     * @param infraGenericEpithet
     * @param specificEpithet
     * @param infraSpecificEpithet
     */
    public TaxonNameParts(Integer taxonNameId, Rank rank, String genusOrUninomial, String infraGenericEpithet,
            String specificEpithet, String infraSpecificEpithet) {
        super();
        this.taxonNameId = taxonNameId;
        this.rank = rank;
        this.genusOrUninomial = genusOrUninomial;
        this.infraGenericEpithet = infraGenericEpithet;
        this.specificEpithet = specificEpithet;
        this.infraSpecificEpithet = infraSpecificEpithet;
    }


    /**
     *
     */
    public TaxonNameParts() {
    }


    /**
     * @param tn
     * @return
     */
    public String rankSpecificNamePart() {
        if(rank.isGenus() || rank.isHigher(Rank.GENUS())){
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

}