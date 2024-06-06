/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;

/**
 * @author kluther
 * @since 23.05.2024
 */
public class SortableTaxonNodeWithoutSecQueryResult extends SortableTaxonNodeQueryResult {

    /**
     * @param taxonNodeUuid
     * @param taxonNodeId
     * @param taxonTitleCache
     */
    public SortableTaxonNodeWithoutSecQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String treeIndex, UUID taxonUuid,
            String taxonTitleCache, String nameTitleCache, Rank nameRank, UUID parentNodeUuid,
            Integer sortIndex, UUID classificationUuid, Boolean taxonPublish, TaxonNodeStatus status, LanguageString note,
            Integer childrenCount
            ) {
        super(taxonNodeUuid, taxonNodeId, treeIndex, taxonUuid, taxonTitleCache, nameTitleCache, nameRank, parentNodeUuid,
                sortIndex, classificationUuid, taxonPublish, status, null, childrenCount, null);

    }

}
