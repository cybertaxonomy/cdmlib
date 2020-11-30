/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.ITaxonTreeNode;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author a.kohlbecker
 * @since Jun 13, 2016
 */
public class TaxonNodeDto extends UuidAndTitleCache<ITaxonTreeNode> {

    private static final long serialVersionUID = -7169646913528213604L;

    /**
     * count of the direct taxonomic children
     */
    private int taxonomicChildrenCount = 0;

    /**
     * The UUID of the associated secundum reference
     */
    private UUID secUuid = null;

    /**
     * The uuid of the associated Taxon entity
     */
    private UUID taxonUuid = null;

    /**
     * the taggedTitle of the associated TaxonName entity
     */
    private List<TaggedText> taggedTitle = new ArrayList<>();

    /**
     * The status of the TaxonNode entity
     */
    private TaxonNodeStatus status;

    private Map<Language, String> statusNote = new HashMap<>();


    /**
     * The Rank.label value of the rank to which the associated TaxonName entity is assigned to.
     */
    private String rankLabel = null;
    private Integer rankOrderIndex = null;

    private TaxonStatus taxonStatus;

    private UUID classificationUUID = null;
    private UUID parentUUID = null;

    private String treeIndex = null;
    private Integer sortIndex = null;


    public TaxonNodeDto(ITaxonTreeNode taxonNode) {
        this(TaxonNode.class, taxonNode);
    }

    public TaxonNodeDto(UUID uuid, Integer id, String titleCache) {
        super(uuid, id, titleCache);
    }

    public TaxonNodeDto(Class type, ITaxonTreeNode taxonTreeNode) {
        super(type, taxonTreeNode.getUuid(), taxonTreeNode.getId(), null);
        Taxon taxon = null;
        TaxonNode taxonNode = null;
        Classification classification = null;
        if (taxonTreeNode instanceof TaxonNode){
            taxonNode = (TaxonNode)taxonTreeNode;
            taxon = taxonNode.getTaxon();
        }else if (taxonTreeNode instanceof Classification){
            classification = (Classification) taxonTreeNode;
        }


        if (taxon != null){
            setTitleCache(taxon.getName() != null ? taxon.getName().getTitleCache() : taxon.getTitleCache());
            secUuid = taxon.getSec() != null ? taxon.getSec().getUuid() : null;
            taxonUuid = taxon.getUuid();
            taggedTitle = taxon.getName() != null? taxon.getName().getTaggedName() : taxon.getTaggedTitle();
            rankLabel = taxon.getNullSafeRank() != null ? taxon.getNullSafeRank().getLabel() : null;
            this.setAbbrevTitleCache(taxon.getTitleCache());
            rankOrderIndex =taxon.getNullSafeRank() != null ? taxon.getNullSafeRank().getOrderIndex() : null;
        }else{
            if (taxonNode != null && taxonNode.getClassification() != null){
                setTitleCache(taxonNode.getClassification().getTitleCache());
            } else if (classification != null){
                setTitleCache(classification.getTitleCache());
            }
            rankOrderIndex = null;
        }
        if (taxonNode != null || classification != null){
            if (classification != null){
                taxonNode = classification.getRootNode();
            }
            taxonomicChildrenCount = taxonNode.getCountChildren();
            status = taxonNode.getStatus();

            for(Language lang : taxonNode.getStatusNote().keySet()) {
                statusNote.put(lang, taxonNode.getStatusNote(lang));
            }

            treeIndex = taxonNode.treeIndex();
            if(taxonNode.getParent() != null) {
                parentUUID = taxonNode.getParent().getUuid();
            } else {
                parentUUID = null;
            }

            sortIndex = taxonNode.getSortIndex();
            if(taxonNode.getClassification() != null) {
                classificationUUID = taxonNode.getClassification().getUuid();
            } else if (classification != null){
                classificationUUID = classification.getUuid();
            }

        }
        taxonStatus = TaxonStatus.Accepted;
    }

    public TaxonNodeDto(Synonym synonym, boolean isHomotypic) {
        super(null, synonym.getName().getTitleCache());

        taxonomicChildrenCount = 0;
        secUuid = synonym.getSec().getUuid();
        taxonUuid = synonym.getUuid();
//        setTitleCache(synonym.getName().getTitleCache());
        taggedTitle = synonym.getName().getTaggedName();

        rankLabel = synonym.getNullSafeRank() != null ? synonym.getNullSafeRank().getLabel() : null;
        rankOrderIndex =synonym.getNullSafeRank() != null ? synonym.getNullSafeRank().getOrderIndex() : null;
        taxonStatus = isHomotypic ? TaxonStatus.SynonymObjective : TaxonStatus.Synonym;
        classificationUUID = null;


    }


    public int getTaxonomicChildrenCount() {
        return taxonomicChildrenCount;
    }

    public UUID getSecUuid() {
        return secUuid;
    }

    public UUID getTaxonUuid() {
        return taxonUuid;
    }

    public List<TaggedText> getTaggedTitle() {
        return taggedTitle;
    }

    public TaxonNodeStatus getStatus() {
        return status;
    }

    public boolean isUnplaced() {
        return status == null ? false : status.equals(TaxonNodeStatus.UNPLACED);
    }

    public boolean isExcluded() {
        return status == null ? false : status.equals(TaxonNodeStatus.EXCLUDED);
    }

    public boolean isDoubtful() {
        return status == null ? false : status.equals(TaxonNodeStatus.DOUBTFUL);
    }

    public String getRankLabel() {
        return rankLabel;
    }

    public TaxonStatus getTaxonStatus() {
        return taxonStatus;
    }

    public UUID getClassificationUUID() {
        return classificationUUID;
    }

    public String getTreeIndex() {
        return treeIndex;
    }

    public UUID getParentUUID() {
        return parentUUID;
    }

    public Integer getSortIndex() {
        return sortIndex;
    }

    public Integer getRankOrderIndex() {
        return rankOrderIndex;
    }

    public String getTaxonTitleCache(){
        return getAbbrevTitleCache();
    }

    public String getNameTitleCache(){
        return getTitleCache();
    }

    /**
     * Preliminary implementation. May not be exactly match
     * the real name cache.
     */
    public String getNameCache(){
        List<TaggedText> nameCacheTags = taggedTitle.stream()
                .filter(t->t.getType().isNameCachePart())
                .collect(Collectors.toList());
        return TaggedCacheHelper.createString(nameCacheTags, new HTMLTagRules());
    }

    @Override
    public boolean equals(Object node2){
        if (node2 instanceof TaxonNodeDto){
            return this.getUuid().equals(((TaxonNodeDto)node2).getUuid());
        } else{
            return false;
        }
    }

    public Map<Language, String> getStatusNote() {
        return Collections.unmodifiableMap(statusNote);
    }

}
