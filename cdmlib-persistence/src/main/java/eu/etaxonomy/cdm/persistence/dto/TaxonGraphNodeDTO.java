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

/**
 * @author a.kohlbecker
 * @since Sep 28, 2018
 *
 */
public class TaxonGraphNodeDTO {

    private UUID nameUuid;
    private UUID taxonUuid;
    private String name;
    private String rank;
    private int rankOrderId;




    /**
     * @param nameUuid
     * @param taxonUuid
     * @param titleCache
     */
    public TaxonGraphNodeDTO(UUID nameUuid, UUID taxonUuid, String name, String rank, int rankOrderId) {
        this.nameUuid = nameUuid;
        this.taxonUuid = taxonUuid;
        this.name = name;
        this.rank = rank;
        this.setRankOrderId(rankOrderId);
    }

    /**
     * @return the nameUuid
     */
    public UUID getNameUuid() {
        return nameUuid;
    }
    /**
     * @param nameUuid the nameUuid to set
     */
    public void setNameUuid(UUID nameUuid) {
        this.nameUuid = nameUuid;
    }
    /**
     * @return the taxonUuid
     */
    public UUID getTaxonUuid() {
        return taxonUuid;
    }
    /**
     * @param taxonUuid the taxonUuid to set
     */
    public void setTaxonUuid(UUID taxonUuid) {
        this.taxonUuid = taxonUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    /**
     * @return the rankOrderId
     */
    public int getRankOrderId() {
        return rankOrderId;
    }

    /**
     * @param rankOrderId the rankOrderId to set
     */
    public void setRankOrderId(int rankOrderId) {
        this.rankOrderId = rankOrderId;
    }

}
