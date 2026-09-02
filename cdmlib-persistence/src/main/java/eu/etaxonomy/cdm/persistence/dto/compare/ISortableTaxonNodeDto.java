/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto.compare;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

public interface ISortableTaxonNodeDto {

    /**
     * The TaxonNode id
     */
    public Integer getId();

    /**
     * The TaxonNode uuid
     */
    public UUID getUuid();

    /**
     * The TaxonNode status
     */
    public TaxonNodeStatus getStatus();

    /**
     * The taxon node uuid
     */
    public String getTreeIndex();

    /**
     * The order index of the taxon name's rank
     */
    public Integer getRankOrderIndex();

    public List<TaggedText> getTaggedTitle();

    /**
     * The taxon's uuid
     */
    public UUID getTaxonUuid();

    /**
     * The name's titleCache
     */
    public String getNameTitleCache();

    /**
     * The taxon node's parent uuid
     */
    public UUID getParentUUID();

    /**
     * The taxon node's sort index
     */
    public Integer getSortIndex();
}