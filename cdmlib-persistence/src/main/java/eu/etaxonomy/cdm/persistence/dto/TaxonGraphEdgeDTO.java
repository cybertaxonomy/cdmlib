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
public class TaxonGraphEdgeDTO {

    private TaxonGraphNodeDTO from, to;

    private UUID citationUuid;
    private String citationTitleCache;

    /**
     * @param from
     * @param to
     * @param citationUuid
     * @param citationTitleCache
     */
    public TaxonGraphEdgeDTO(TaxonGraphNodeDTO from, TaxonGraphNodeDTO to, UUID citationUuid,
            String citationTitleCache) {
        super();
        this.from = from;
        this.to = to;
        this.citationUuid = citationUuid;
        this.citationTitleCache = citationTitleCache;
    }

    public TaxonGraphEdgeDTO(UUID fromTaxonUuid, String fromName, String fromRank, Integer fromRankOrderId,
            UUID toTaxonUuid, String toName, String toRank, Integer toRankOrderId, UUID citationUuid,
            String citationTitleCache) {
        super();
        this.from = new TaxonGraphNodeDTO(null, fromTaxonUuid, fromName, fromRank, fromRankOrderId);
        this.to = new TaxonGraphNodeDTO(null, toTaxonUuid, toName, toRank, toRankOrderId);
        this.citationUuid = citationUuid;
        this.citationTitleCache = citationTitleCache;
    }

    /**
     * @return the from
     */
    public TaxonGraphNodeDTO getFrom() {
        return from;
    }
    /**
     * @param from the from to set
     */
    public void setFrom(TaxonGraphNodeDTO from) {
        this.from = from;
    }
    /**
     * @return the to
     */
    public TaxonGraphNodeDTO getTo() {
        return to;
    }
    /**
     * @param to the to to set
     */
    public void setTo(TaxonGraphNodeDTO to) {
        this.to = to;
    }
    /**
     * @return the citationUuid
     */
    public UUID getCitationUuid() {
        return citationUuid;
    }
    /**
     * @param citationUuid the citationUuid to set
     */
    public void setCitationUuid(UUID citationUuid) {
        this.citationUuid = citationUuid;
    }
    /**
     * @return the citationTitleCache
     */
    public String getCitationTitleCache() {
        return citationTitleCache;
    }
    /**
     * @param citationTitleCache the citationTitleCache to set
     */
    public void setCitationTitleCache(String citationTitleCache) {
        this.citationTitleCache = citationTitleCache;
    }



}
