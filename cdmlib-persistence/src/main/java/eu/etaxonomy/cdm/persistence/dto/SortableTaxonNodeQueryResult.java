/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;

/**
 * @author a.kohlbecker
 * @since Mar 20, 2020
 */
public class SortableTaxonNodeQueryResult {

    private UUID taxonNodeUuid;
    private Integer taxonNodeId;
    private String treeIndex;
    private UUID taxonUuid;
    private String taxonTitleCache;
    private String nameTitleCache;
    private Rank nameRank = Rank.UNKNOWN_RANK();
    private UUID parentNodeUuid;
    private Integer sortIndex;
    private UUID classificationUuid;
    private Boolean taxonIsPublish = true;
    private TaxonNodeStatus status;

    private List<LanguageString> statusNote = new ArrayList<>();


    /**Is this the reason
     * @param taxonNodeUuid
     * @param taxonNodeId
     * @param taxonTitleCache
     * @param nameRank {@link Rank.#UNKNOWN_RANK()} will be used in case this is <code>null</code>
     */
    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String treeIndex, UUID taxonUuid, String taxonTitleCache, String nameTitleCache,
            Rank nameRank, UUID parentNodeUuid, Integer sortIndex, UUID classificationUuid,  Boolean taxonIsPublished, TaxonNodeStatus status, LanguageString statusNote) {
        this.taxonNodeUuid = taxonNodeUuid;
        this.taxonNodeId = taxonNodeId;
        this.treeIndex = treeIndex;
        this.taxonUuid = taxonUuid;
        this.taxonTitleCache = taxonTitleCache;
        this.nameTitleCache = nameTitleCache;
        if (nameRank != null) {
        	this.nameRank = nameRank;
        }
        this.parentNodeUuid = parentNodeUuid;
        this.sortIndex = sortIndex;
        this.classificationUuid = classificationUuid;
        this.taxonIsPublish = taxonIsPublished;
        this.status = status;
        if (statusNote != null) {
		this.statusNote.add(statusNote);
        }
        
//        if (statusNote != null) {
//        	this.statusNote = new HashMap<Language, String>();
//        	for (Entry<Language, LanguageString> entry :statusNote.entrySet()) {
//        		this.statusNote.put(entry.getKey(), entry.getValue().getText());
//        	}
//        }
    }

    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String treeIndex, UUID taxonUuid, String taxonTitleCache, String nameTitleCache,
            Rank nameRank, UUID parentNodeUuid) {
	this(taxonNodeUuid, taxonNodeId, treeIndex, taxonUuid, taxonTitleCache, nameTitleCache, nameRank, parentNodeUuid, null, null,null, null, null);
    }
    /**
     * @param taxonNodeUuid
     * @param taxonNodeId
     * @param taxonTitleCache
     * @param nameRank {@link Rank.#UNKNOWN_RANK()} will be used in case this is <code>null</code>
     */
    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String treeIndex, UUID taxonUuid, String taxonTitleCache,
            Rank nameRank, UUID parentNodeUuid) {
        this(taxonNodeUuid, taxonNodeId, treeIndex, taxonUuid, taxonTitleCache, null, nameRank, parentNodeUuid, null, null, null, null, null);
    }


    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String treeIndex, UUID taxonUuid, String taxonTitleCache,
            Rank nameRank) {
        this(taxonNodeUuid, taxonNodeId, treeIndex, taxonUuid, taxonTitleCache, null, nameRank, null, null, null, null, null, null);
    }

    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String taxonTitleCache,
            Rank nameRank) {
        this(taxonNodeUuid, taxonNodeId, null, null, taxonTitleCache, null, nameRank, null, null, null, null, null, null);
    }
    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, UUID taxonUuid, String taxonTitleCache, UUID parentNodeUuid) {
        this(taxonNodeUuid, taxonNodeId, null, taxonUuid, taxonTitleCache, null, parentNodeUuid);
    }
    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String taxonTitleCache, UUID parentNodeUuid) {
        this(taxonNodeUuid, taxonNodeId, null, null, taxonTitleCache, null, parentNodeUuid);
    }

    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, UUID taxonUuid, String taxonTitleCache, String treeIndex) {
        this(taxonNodeUuid, taxonNodeId, treeIndex, taxonUuid, taxonTitleCache, null, null);
    }
    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String taxonTitleCache) {
        this(taxonNodeUuid, taxonNodeId, null, null, taxonTitleCache, null, null);
    }

    //tn.uuid, tn.id, t.uuid, t.titleCache, name.titleCache, rank, cl.uuid,  t.publish, tn.status, note

    public UUID getTaxonNodeUuid() {
        return taxonNodeUuid;
    }
    public void setTaxonNodeUuid(UUID taxonNodeUuid) {
        this.taxonNodeUuid = taxonNodeUuid;
    }

    public String getTreeIndex() {
        return treeIndex;
    }
    public void setTreeIndex(String treeIndex) {
        this.treeIndex = treeIndex;
    }

    public Integer getSortIndex() {
		return sortIndex;
	}

	public void setSortIndex(Integer sortIndex) {
		this.sortIndex = sortIndex;
	}

    public UUID getTaxonUuid() {
        return taxonUuid;
    }
    public void setTaxonUuid(UUID taxonUuid) {
        this.taxonUuid = taxonUuid;
    }

    public UUID getParentNodeUuid() {
        return parentNodeUuid;
    }
    public void setParentNodeUuid(UUID parentNodeUuid) {
        this.parentNodeUuid = parentNodeUuid;
    }

    public UUID getClassificationUuid() {
        return classificationUuid;
    }
    public void setClassificationUuid(UUID classificationUuid) {
        this.classificationUuid = classificationUuid;
    }

    public Boolean isTaxonIsPublish() {
		return taxonIsPublish;
	}
	public void setTaxonIsPublish(boolean taxonIsPublish) {
		this.taxonIsPublish = taxonIsPublish;
	}

	public List<LanguageString> getStatusNote() {
		return statusNote;
	}

	public void setStatusNote(List<LanguageString> statusNote) {
		this.statusNote = statusNote;
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

    public String getNameTitleCache() {
        return nameTitleCache;
    }
    public void setNameTitleCache(String nameTitleCache) {
        this.nameTitleCache = nameTitleCache;
    }

	public TaxonNodeStatus getStatus() {
		return status;
	}

	public void setStatus(TaxonNodeStatus status) {
		this.status = status;
	}


}
