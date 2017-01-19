/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author a.kohlbecker
 * @date Jun 13, 2016
 *
 */
public class TaxonNodeDto {

    /**
     * The TaxonNode uuid
     */
    private final UUID uuid;

    /**
     * count of the direct taxonomic children
     */
    private final int taxonomicChildrenCount;


    /**
     * The UUID of the associated secundum reference
     */
    private final UUID secUuid;

    /**
     * The uuid of the associated Taxon entity
     */
    private final UUID taxonUuid;

    /**
     * the titleCache of the associated TaxonName entity
     */
    private final String titleCache;

    /**
     * the taggedTitle of the associated TaxonName entity
     */
    private final List<TaggedText> taggedTitle;

    /**
     * The unplaced flag of the Taxon entity
     */
    private final boolean unplaced;

    /**
     * The excluded flag of the Taxon entity
     */
    private final boolean excluded;

    /**
     * The Rank.label value of the rank to which the associated TaxonName entity is assigned to.
     */
    private final String rankLabel;

    private final TaxonStatus status;

    /**
     * @param taxonNode
     */
    public TaxonNodeDto(TaxonNode taxonNode) {
        uuid = taxonNode.getUuid();
        taxonomicChildrenCount = taxonNode.getCountChildren();
        Taxon taxon = taxonNode.getTaxon();
        secUuid = taxon.getSec().getUuid();
        taxonUuid = taxon.getUuid();
        titleCache = taxon.getName().getTitleCache();
        taggedTitle = taxon.getName().getTaggedName();
        unplaced = taxonNode.isUnplaced();
        excluded = taxonNode.isExcluded();
        rankLabel = taxon.getName().getRank().getLabel();
        status = TaxonStatus.Accepted;
    }

    /**
     * @param taxonNode
     */
    public TaxonNodeDto(Synonym synonym, boolean isHomotypic) {
        uuid = null;
        taxonomicChildrenCount = 0;
        secUuid = synonym.getSec().getUuid();
        taxonUuid = synonym.getUuid();
        titleCache = synonym.getName().getTitleCache();
        taggedTitle = synonym.getName().getTaggedName();
        unplaced = false;
        excluded = false;
        rankLabel = synonym.getName().getRank().getLabel();
        status = isHomotypic ? TaxonStatus.SynonymObjective : TaxonStatus.Synonym;
    }

    /**
     * @return the uuid
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * @return the taxonomicChildrenCount
     */
    public int getTaxonomicChildrenCount() {
        return taxonomicChildrenCount;
    }

    /**
     * @return the secUuid
     */
    public UUID getSecUuid() {
        return secUuid;
    }

    /**
     * @return the taxonUuid
     */
    public UUID getTaxonUuid() {
        return taxonUuid;
    }

    /**
     * @return the titleCache
     */
    public String getTitleCache() {
        return titleCache;
    }

    /**
     * @return the taggedTitle
     */
    public List<TaggedText> getTaggedTitle() {
        return taggedTitle;
    }

    /**
     * @return the unplaced
     */
    public boolean isUnplaced() {
        return unplaced;
    }

    /**
     * @return the excluded
     */
    public boolean isExcluded() {
        return excluded;
    }

    /**
     * @return the rankLabel
     */
    public String getRankLabel() {
        return rankLabel;
    }

    /**
     * @return the status
     */
    public TaxonStatus getStatus() {
        return status;
    }


}
