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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

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
    private Integer childrenCount;
    private UUID secUuid;
    private List<TaggedText> taggedName;

    private List<LanguageString> placementNote = new ArrayList<>();

    //  (rank.label, rank.orderIndex) => orderIndex may become a problem once rank does not include the orderindex anymore
    //taxonStatus (Accepted, Synonym, SynonymObjective)


    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String treeIndex, UUID taxonUuid,
            String taxonTitleCache, String nameTitleCache, Rank nameRank, UUID parentNodeUuid,
            Integer sortIndex, UUID classificationUuid, Boolean taxonIsPublished, TaxonNodeStatus status,
            Integer childrenCount, UUID secUuid,
            NomenclaturalCode nameType,
            String genusOrUninomial,
            String infragenericEpithet,
            String specificEpithet,
            String infraspecificEpithet,
            String apendedPhrase,
            Boolean protectedTitleCache,
            Boolean protectedNameCache,
            String nameCache,
            String authorshipCache,
            Integer publicationYear,
            Boolean nomomHybrid,
            Boolean binomHybrid,
            Boolean trinomHybrid

            ) {
        this(taxonNodeUuid, taxonNodeId, treeIndex, taxonUuid, taxonTitleCache, nameTitleCache, nameRank, parentNodeUuid, sortIndex, classificationUuid, taxonIsPublished, status, childrenCount, secUuid);
        TaxonName name = getName(nameRank, nameType,
                genusOrUninomial,
                infragenericEpithet,
                specificEpithet,
                infraspecificEpithet,
                apendedPhrase,
                nameTitleCache,
                protectedTitleCache,
                protectedNameCache,
                nameCache,
                authorshipCache,
                publicationYear,
                nomomHybrid,
                binomHybrid,
                trinomHybrid);
        setTaggedName(name.getTaggedName());
    }

    private TaxonName getName(Rank rank, NomenclaturalCode nameType, String genusOrUninomial, String infragenericEpithet,
            String specificEpithet, String infraspecificEpithet, String apendedPhrase,
            String titleCache, Boolean protectedTitleCache,
            Boolean protectedNameCache, String nameCache, String authorshipCache, Integer publicationYear,
            Boolean monomHybrid, Boolean binomHybrid, Boolean trinomHybrid) {
        TaxonName name = (TaxonName)TaxonNameFactory.NewNonViralInstance(rank, genusOrUninomial, infragenericEpithet, specificEpithet,
                infraspecificEpithet, null, null, null, null);
        name.setNameType(nameType);
        name.setAppendedPhrase(apendedPhrase);
        name.setTitleCache(titleCache, protectedTitleCache);
        name.setNameCache(nameCache, protectedNameCache);
        name.setAuthorshipCache(authorshipCache);
        name.setPublicationYear(publicationYear);
        name.setMonomHybrid(monomHybrid);
        name.setBinomHybrid(binomHybrid);
        name.setTrinomHybrid(trinomHybrid);
        return name;
    }

    /**
     * @param nameRank {@link Rank.#UNKNOWN_RANK()} will be used in case this is <code>null</code>
     */
    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String treeIndex, UUID taxonUuid,
            String taxonTitleCache, String nameTitleCache, Rank nameRank, UUID parentNodeUuid,
            Integer sortIndex, UUID classificationUuid, Boolean taxonIsPublished, TaxonNodeStatus status,
            LanguageString placementNote,
            Integer childrenCount, UUID secUuid

            ) {

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
        this.setChildrenCount(childrenCount);
        this.setSecUuid(secUuid);
        this.status = status;
        if (placementNote != null) {
            this.placementNote.add(placementNote);
        }

//        if (placementNote != null) {
//        	this.placementNote = new HashMap<Language, String>();
//        	for (Entry<Language, LanguageString> entry :placementNote.entrySet()) {
//        		this.placementNote.put(entry.getKey(), entry.getValue().getText());
//        	}
//        }
    }

    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String treeIndex, UUID taxonUuid,
            String taxonTitleCache, String nameTitleCache, Rank nameRank, UUID parentNodeUuid,
            Integer sortIndex, UUID classificationUuid, Boolean taxonPublish, TaxonNodeStatus status,
            Integer childrenCount, UUID secUuid
            ) {
        this(taxonNodeUuid, taxonNodeId, treeIndex, taxonUuid, taxonTitleCache, nameTitleCache, nameRank, parentNodeUuid,
            sortIndex, classificationUuid, taxonPublish, status, null, childrenCount, secUuid);
    }

    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String treeIndex, UUID taxonUuid,
            String taxonTitleCache, String nameTitleCache, Rank nameRank, UUID parentNodeUuid,
            Integer sortIndex, UUID classificationUuid, Boolean taxonPublish, TaxonNodeStatus status, LanguageString note
            ) {
        this(taxonNodeUuid, taxonNodeId, treeIndex, taxonUuid, taxonTitleCache, nameTitleCache, nameRank, parentNodeUuid,
            sortIndex, classificationUuid, taxonPublish, status, note, null, null);
    }

    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String treeIndex, UUID taxonUuid,
            String taxonTitleCache, String nameTitleCache, Rank nameRank, UUID parentNodeUuid) {
        this(taxonNodeUuid, taxonNodeId, treeIndex, taxonUuid, taxonTitleCache, nameTitleCache, nameRank, parentNodeUuid,
            null, null,null, null, null);
    }

    /**
     * @param nameRank {@link Rank.#UNKNOWN_RANK()} will be used in case this is <code>null</code>
     */
    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String treeIndex,
            UUID taxonUuid, String taxonTitleCache, Rank nameRank, UUID parentNodeUuid) {
        this(taxonNodeUuid, taxonNodeId, treeIndex, taxonUuid, taxonTitleCache, null, nameRank, parentNodeUuid,
                null, null, null, null, null);
    }

    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String treeIndex,
            UUID taxonUuid, String taxonTitleCache, Rank nameRank) {
        this(taxonNodeUuid, taxonNodeId, treeIndex, taxonUuid, taxonTitleCache, null, nameRank, null,
                null, null, null, null, null);
    }

    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String taxonTitleCache,
            Rank nameRank) {
        this(taxonNodeUuid, taxonNodeId, null, null, taxonTitleCache, null, nameRank, null,
                null, null, null, null, null);
    }

    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, UUID taxonUuid,
            String taxonTitleCache, UUID parentNodeUuid) {
        this(taxonNodeUuid, taxonNodeId, null, taxonUuid, taxonTitleCache, null, parentNodeUuid);
    }

    public SortableTaxonNodeQueryResult(UUID taxonNodeUuid, Integer taxonNodeId, String taxonTitleCache,
            UUID parentNodeUuid) {
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

	public List<LanguageString> getPlacementNote() {
		return placementNote;
	}

	public void setPlacementNote(List<LanguageString> placementNote) {
		this.placementNote = placementNote;
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


    public Integer getChildrenCount() {
        return childrenCount;
    }
    public void setChildrenCount(Integer childrenCount) {
        this.childrenCount = childrenCount;
    }

    public UUID getSecUuid() {
        return secUuid;
    }
    public void setSecUuid(UUID secUuid) {
        this.secUuid = secUuid;
    }


    public List<TaggedText> getTaggedName() {
        return taggedName;
    }
    public void setTaggedName(List<TaggedText> taggedName) {
        this.taggedName = taggedName;
    }

	public TaxonNodeStatus getStatus() {
		return status;
	}
	public void setStatus(TaxonNodeStatus status) {
		this.status = status;
	}

    public static List<TaxonNodeDto> toTaxonNodeDtoList(List<SortableTaxonNodeQueryResult> result) {

        List<TaxonNodeDto> nodeDtos = new ArrayList<>();
        Collections.sort(result, new SortableTaxonNodeQueryResultComparator());
        for(SortableTaxonNodeQueryResult queryDTO : result){
            TaxonNodeDto nodeDto = new TaxonNodeDto(queryDTO.getTaxonNodeUuid(),
                    queryDTO.getTaxonNodeId(), queryDTO.getTaxonUuid(), queryDTO.getTreeIndex(),
                    queryDTO.getNameTitleCache(), queryDTO.getTaxonTitleCache(),
                    queryDTO.getNameRank()!= null? queryDTO.getNameRank().getOrderIndex(): null,
                    queryDTO.getParentNodeUuid(), queryDTO.getSortIndex(), queryDTO.getClassificationUuid(),
                    queryDTO.isTaxonIsPublish(), queryDTO.getStatus(), queryDTO.getPlacementNote(),
                    queryDTO.getChildrenCount(), queryDTO.getSecUuid(),
                    queryDTO.getTaggedName());
            nodeDtos.add(nodeDto);
        }
        return nodeDtos;
    }

}