/**
* Copyright (C) 2020 EDIT
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
 * @author a.kohlbecker
 * @since Mar 20, 2020
 *
 */
public class SortableTaxonNodeQueryResult {

    UUID taxonNodeUuid;
    Integer taxonNodeId;
    String taxonTitleCache;
    Rank nameRank = Rank.UNKNOWN_RANK();



    /**Is this the reason
     * @param taxonNodeUuid
     * @param taxonNodeId
     * @param taxonTitleCache
     * @param nameRank {@link Rank.#UNKNOWN_RANK()} will be used in case this is <code>null</code>
     */
    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String taxonTitleCache,
            Rank nameRank) {
        super();
        this.taxonNodeUuid = taxonNodeUuid;
        this.taxonNodeId = taxonNodeId;
        this.taxonTitleCache = taxonTitleCache;
        if(nameRank != null){
            this.nameRank = nameRank;
        }
    }
    public UUID getTaxonNodeUuid() {
        return taxonNodeUuid;
    }
    public void setTaxonNodeUuid(UUID taxonNodeUuid) {
        this.taxonNodeUuid = taxonNodeUuid;
    }
    public Integer getTaxonNodeId() {
        return taxonNodeId;
    }
    public void setTaxonNodeId(Integer taxonNodeId) {
        this.taxonNodeId = taxonNodeId;
    }
    public String getTaxonTitleCache() {
        return taxonTitleCache;
    }
    public void setTaxonTitleCache(String taxonTitleCache) {
        this.taxonTitleCache = taxonTitleCache;
    }
    public Rank getNameRank() {
        return nameRank;
    }
    public void setNameRank(Rank nameRank) {
        this.nameRank = nameRank;
    }




}
