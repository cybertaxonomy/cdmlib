/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.utility;

import java.util.Optional;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.persistence.dto.TaxonNameParts;

/**
 * The {@link TaxonNamePartsFilter} defines the rank and fixed name parts for the name part search. See as an example
 * {@link eu.etaxonomy.cdm.api.service.INameService#findTaxonNameParts(TaxonNamePartsFilter, String, Integer, Integer, java.util.List)}
 * <p>
 * For example: In case the <code>rank</code> is "genus", the <code>genusOrUninomial</code> will be used as search parameter which needs to match exactly.
 * The <code>namePartQueryString</code> will be used to do a wildcard search on the specificEpithet.
 * <p>
 * For name part lookup purposes the <code>TaxonNameParts</code> in the result list can be asked to return the relavant name part by
 * calling {@link TaxonNameParts#nameRankSpecificNamePart(TaxonName)}
 *
 * @author a.kohlbecker
 * @since Jun 12, 2018
 *
 */
public class TaxonNamePartsFilter extends TaxonNameParts {

    /**
     * @param taxonNameId
     * @param rank
     * @param genusOrUninomial
     * @param infraGenericEpithet
     * @param specificEpithet
     * @param infraSpecificEpithet
     */
    public TaxonNamePartsFilter(Rank rank, String genusOrUninomial, String infraGenericEpithet,
            String specificEpithet, String infraSpecificEpithet) {
        super(null, rank, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet);
    }

    public TaxonNamePartsFilter(){
        super();
    }

    public Optional<String> uninomialQueryString(String query){
         if(rank.isLower(Rank.GENUS())){
             return optionalForNonNull(genusOrUninomial);
        } else {
            return Optional.of(appendWildcard(query));
        }
    }

    public Optional<String> infraGenericEpithet(String query){
        if(rank.isInfraGeneric()){
            return Optional.of(appendWildcard(query));
        } else if(rank.isLower(Rank.GENUS())) {
            return optionalForNonNull(infraGenericEpithet);
        } else {
            // mask invalid data as null
            return null;
        }
    }

    public Optional<String> specificEpithet(String query){
        if(rank.isLower(Rank.SPECIES())){
            return optionalForNonNull(specificEpithet);
        } else if(rank.isSpecies()) {
            return Optional.of(appendWildcard(query));
        } else {
            return null;
        }
    }

    public Optional<String> infraspecificEpithet(String query){
        if(rank.isInfraSpecific()){
            return Optional.of(appendWildcard(query));
        } else {
            return null;
        }
    }

    private Optional<String> optionalForNonNull(String value){
        if(value != null){
            return Optional.of(value);
        } else {
            return null;
        }
    }

    private String appendWildcard(String query){
        if(!query.endsWith("*")){
            return query + "*";
        }
        return query;

    }
}
