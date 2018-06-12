/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;

public class TaxonNameParts {

    Integer taxonNameId;

    Rank rank;

    private String uninomial;

    private String infraGenericEpithet;

    private String specificEpithet;

    private String infraSpecificEpithet;


    /**
     * @param taxonNameId
     * @param rank
     * @param uninomial
     * @param infraGenericEpithet
     * @param specificEpithet
     * @param infraSpecificEpithet
     */
//    public TaxonNameParts(Integer taxonNameId, String uninomial, String infraGenericEpithet,
//            String specificEpithet, String infraSpecificEpithet) {
//        super();
//        this.taxonNameId = taxonNameId;
//        this.uninomial = uninomial;
//        this.infraGenericEpithet = infraGenericEpithet;
//        this.specificEpithet = specificEpithet;
//        this.infraSpecificEpithet = infraSpecificEpithet;
//    }


    /**
     * @param taxonNameId
     * @param rank
     * @param uninomial
     * @param infraGenericEpithet
     * @param specificEpithet
     * @param infraSpecificEpithet
     */
    public TaxonNameParts(Integer taxonNameId, Rank rank, String uninomial, String infraGenericEpithet,
            String specificEpithet, String infraSpecificEpithet) {
        super();
        this.taxonNameId = taxonNameId;
        this.rank = rank;
        this.uninomial = uninomial;
        this.infraGenericEpithet = infraGenericEpithet;
        this.specificEpithet = specificEpithet;
        this.infraSpecificEpithet = infraSpecificEpithet;
    }


    /**
     * @param tn
     * @return
     */
    public String nameRankSpecificNamePart(TaxonName tn) {
        if(rank.isSpecies()){
            return tn.getGenusOrUninomial();
        }
        if(rank.isInfraSpecific()){
            return tn.getSpecificEpithet();
        }
        return "--ERROR: INVALID RANK (" + rank.getLabel() + ")--";
    }


    public String uninomialQueryString(String query){
         if(rank.isLower(Rank.GENUS())){
            return uninomial;
        } else {
            return query;
        }
    }

    public String infraGenericEpithet(String query){
        if(rank.isInfraGeneric()){
            return query;
        } else if(rank.isLower(Rank.GENUS())) {
            return infraGenericEpithet;
        } else {
            // mask invalid data as null
            return null;
        }
    }

    public String specificEpithet(String query){
        if(rank.isLower(Rank.SPECIES())){
            return specificEpithet;
        } else if(rank.isSpecies()) {
            return query;
        } else {
            return null;
        }
    }

    public String infraspecificEpithet(String query){
        if(rank.isInfraSpecific()){
            return query;
        } else {
            return null;
        }
    }

}