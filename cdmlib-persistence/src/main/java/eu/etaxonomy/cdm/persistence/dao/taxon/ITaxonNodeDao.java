/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.taxon;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TreeIndex;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.mueller
 */
public interface ITaxonNodeDao extends IAnnotatableDao<TaxonNode> {

	public UUID delete(TaxonNode persistentObject, boolean deleteChildren);

    public List<TaxonNode> getTaxonOfAcceptedTaxaByClassification(Classification classification, Integer start, Integer end);

    public int countTaxonOfAcceptedTaxaByClassification(Classification classification);

    /**
     * Lists all direct child nodes of the given {@link UuidAndTitleCache} which
     * represents the parent {@link TaxonNode}
     * @param parent a UuidAndTitleCache object which represents a parent {@link TaxonNode}
     * @return a list of UuidAndTitleCache objects that represent children of the
     * parent
     */
    public List<TaxonNodeDto> listChildNodesAsUuidAndTitleCache(TaxonNodeDto parent);

    /**
     * Retrieves the parent node of the {@link TaxonNode} represented by the given {@link UuidAndTitleCache}.
     * @param child the child for which the parent should be retrieved
     * @return an UuidAndTitleCache object representing the parent node
     */
    public TaxonNodeDto getParentUuidAndTitleCache(TaxonNodeDto child);

    /**
     * Retrieves a list of {@link UuidAndTitleCache} objects that have a matching titleCache
     * @param limit the maximum results
     * @param pattern the titleCache that is searched for
     * @param classificationUuid if specified only nodes of this classification are retrieved
     * @return a list of matches
     */
    public List<TaxonNodeDto> getUuidAndTitleCache(Integer limit, String pattern, UUID classificationUuid);

    public List<TaxonNode> listChildrenOf(TaxonNode node, Integer pageSize, Integer pageIndex,
            boolean recursive, boolean includeUnpublished, List<String> propertyPaths, Comparator<TaxonNode> comparator);

    public abstract Long countChildrenOf(TaxonNode node, Classification classification, boolean recursive, boolean includeUnpublished);

    /**
     * Returns the of TaxonNodeAgentRelation entities which are associated with the TaxonNode for the
     * given TaxonUuid in the specified Classification.
     *
     * @param taxonUuid
     * @param agentUuid TODO
     * @param relTypeUuid TODO
     * @param start
     * @param limit
     * @param propertyPaths
     * @param rankId TODO
     * @param classification
     * @return
     */
   public List<TaxonNodeAgentRelation> listTaxonNodeAgentRelations(UUID taxonUuid, UUID classificationUuid,
            UUID agentUuid, UUID rankUuid, UUID relTypeUuid, Integer start, Integer limit, List<String> propertyPaths);

    /**
     * Returns the number of TaxonNodeAgentRelation entities which are associated with the TaxonNode for the
     * given TaxonUuid in the specified Classification.
     *
     * @param taxonUuid
     * @param agentUuid TODO
     * @param relTypeUuid TODO
     * @param rankId TODO
     * @param classification
     * @return
     */
    public long countTaxonNodeAgentRelations(UUID taxonUuid, UUID classificationUuid, UUID agentUuid, UUID rankUuid, UUID relTypeUuid);

    /**
     * Computes a map treeIndex->rank(sortIndex) for each given taxon node treeIndex. Required by #5957.
     * If the taxon represented by the treeindex is not in the given rank range no record is returned for the given
     * treeindex.
     *
     * @param treeIndex the list of treeIndexes
     * @param minRankOrderIndex min rank
     * @param maxRankOrderIndex max rank
     * @return
     */
    public Map<TreeIndex, Integer> rankOrderIndexForTreeIndex(List<TreeIndex> treeIndex, Integer minRankOrderIndex,
            Integer maxRankOrderIndex);

    /**
     * For a given set of taxon node tree indexes the uuid and title cache of the taxon represented
     * by this treeindex is returned.
     * @param treeIndexSet set of taxon node tree indexes
     * @return map with treeindex and uuidAndTitleCache of the represented taxon
     */
    public Map<TreeIndex, UuidAndTitleCache<?>> taxonUuidsForTreeIndexes(Collection<TreeIndex> treeIndexSet);

//----------------------------- SEC FOR SUBTREE -------------------------------/

    public int countSecundumForSubtreeAcceptedTaxa(TreeIndex subTreeIndex, Reference newSec,
            boolean overwriteExistingAccepted, boolean includeSharedTaxa, boolean emptySecundumDetail);

    public int countSecundumForSubtreeSynonyms(TreeIndex subTreeIndex, Reference newSec,
            boolean overwriteExistingSynonyms, boolean includeSharedTaxa, boolean emptySecundumDetail);

    public int countSecundumForSubtreeRelations(TreeIndex subTreeIndex, Reference newSec,
            boolean overwriteExistingRelations, boolean includeSharedTaxa, boolean emptySecundumDetail);

    public Set<CdmBase> setSecundumForSubtreeAcceptedTaxa(TreeIndex subTreeIndex, Reference newSec,
            boolean overwriteExisting, boolean includeSharedTaxa, boolean emptyDetail, IProgressMonitor monitor);

    public  Set<CdmBase> setSecundumForSubtreeSynonyms(TreeIndex subTreeIndex, Reference newSec,
            boolean overwriteExisting, boolean includeSharedTaxa, boolean emptyDetail, IProgressMonitor monitor);

    public  Set<CdmBase> setSecundumForSubtreeRelations(TreeIndex subTreeIndex, Reference newSec,
            Set<UUID> relationTypes, boolean overwriteExisting, boolean includeSharedTaxa, boolean emptyDetail,
            IProgressMonitor monitor);

//----------------------------- PUBLISH FOR SUBTREE -------------------------------/

    public int countPublishForSubtreeAcceptedTaxa(TreeIndex subTreeIndex, boolean publish,
            boolean includeSharedTaxa, boolean includeHybrids);
    public Set<TaxonBase> setPublishForSubtreeAcceptedTaxa(TreeIndex subTreeIndex, boolean publish,
            boolean includeSharedTaxa, boolean includeHybrids, IProgressMonitor monitor);

    public int countPublishForSubtreeSynonyms(TreeIndex subTreeIndex, boolean publish,
            boolean includeSharedTaxa, boolean includeHybrids);
    public Set<TaxonBase> setPublishForSubtreeSynonyms(TreeIndex subTreeIndex, boolean publish,
            boolean includeSharedTaxa, boolean includeHybrids, IProgressMonitor monitor);

    public int countPublishForSubtreeRelatedTaxa(TreeIndex subTreeIndex, boolean publish,
            boolean includeSharedTaxa, boolean includeHybrids);
    public Set<TaxonBase> setPublishForSubtreeRelatedTaxa(TreeIndex subTreeIndex, boolean publish,
            Set<UUID> relationTypes, boolean includeSharedTaxa, boolean includeHybrids,
            IProgressMonitor monitor);

//---------------------------------------------------------------------------------/

    public List<TaxonNodeDto> listChildNodesAsTaxonNodeDto(TaxonNodeDto parent);

    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(
            Classification classification, Integer limit, String pattern, boolean searchForClassifications);

    public <S extends TaxonNode> List<S> list(Class<S> type, List<Restriction<?>> restrictions, Integer limit, Integer start,
            List<OrderHint> orderHints, List<String> propertyPaths, boolean includePublished);

    long count(Class<? extends TaxonNode> type, List<Restriction<?>> restrictions, boolean includePublished);

	public List<TaxonNodeDto> getParentTaxonNodeDtoForRank( Classification classification, Rank rank, TaxonName name);

	public List<TaxonNodeDto> getParentTaxonNodeDtoForRank( Classification classification, Rank rank, TaxonBase<?> taxonBase);

    public List<TaxonNodeDto> getTaxonNodeDto(Integer limit, String pattern, UUID classificationUuid);

    public List<TaxonNodeDto> getUuidAndTitleCache(Integer limit, String pattern, UUID classificationUuid, boolean includeDoubtful);

    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(
            Classification classification, Integer limit, String pattern, boolean searchForClassifications,
            boolean includeDoubtful);

    /**
     * @param taxonUUID
     * @param classificationUuid
     * @return
     */
    List<TaxonNodeDto> getTaxonNodeForTaxonInClassificationDto(UUID taxonUUID, UUID classificationUuid);

    /**
     * @param nodeUuid
     * @return
     */
    TaxonNodeDto getTaxonNodeDto(UUID nodeUuid);

    /**
     * @param nodeUuid
     * @return
     */
    List<TaxonNodeDto> getTaxonNodeDtos(List<UUID> nodeUuid);
}
