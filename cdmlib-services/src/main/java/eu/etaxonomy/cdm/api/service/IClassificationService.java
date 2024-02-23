/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.config.CreateHierarchyForClassificationConfigurator;
import eu.etaxonomy.cdm.api.service.config.TaxonDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.dto.GroupedTaxonDTO;
import eu.etaxonomy.cdm.api.service.dto.TaxonInContextDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.compare.taxon.TaxonNodeSortMode;
import eu.etaxonomy.cdm.exception.FilterException;
import eu.etaxonomy.cdm.exception.UnpublishedException;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.ITaxonTreeNode;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dto.ClassificationLookupDTO;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;


/**
 * @author n.hoffmann
 * @since Sep 21, 2009
 */
public interface IClassificationService extends IIdentifiableEntityService<Classification> {

    public ITaxonTreeNode getTreeNodeByUuid(UUID uuid);

    /**
     * Returns the root node of the the given classification (specified by its UUID)
     * @param classificationUuid the uuid of the classification
     * @return the root node of the classification
     */
    public TaxonNode getRootNode(UUID classificationUuid);

    public UUID getTaxonNodeUuidByTaxonUuid(UUID classificationUuid, UUID taxonUuid);

    public List<Classification> listClassifications(Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     *
     * @param taxon
     * @param classificationUuid
     * @param propertyPaths
     * @return
     * @deprecated use loadTaxonNode(TaxonNode taxonNode, ...) instead
     * if you have a classification and a taxon that is in it, you should also have the according taxonNode
     */
    @Deprecated
    public TaxonNode loadTaxonNodeByTaxon(Taxon taxon, UUID classificationUuid, List<String> propertyPaths);

    /**
     * Loads all TaxonNodes of the specified classification for a given Rank or lower.
     * If a branch of the classification tree is not containing a TaxonNode with a Taxon at the given
     * Rank the according node associated with the next lower Rank is taken as root node in this case.
     * So the nodes returned may reference Taxa with different Ranks.
     *
     * If the <code>rank</code> is null the absolute root nodes will be returned.

     * @param classification may be null for all classifications
     * @param subtree filter on a taxonomic subtree
     * @param rank the set to null for to get the root nodes of classifications
     * @param includeUnpublished if <code>true</code> unpublished taxa are also exported
     * @param pageSize The maximum number of relationships returned (can be null for all relationships)
     * @param pageIndex The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths
     * @return
     * @see #pageRankSpecificRootNodes(Classification, TaxonNode, Rank, boolean, Integer, Integer, List)
     * @deprecated use according DTO method instead
     */
    @Deprecated
    public List<TaxonNode> listRankSpecificRootNodes(Classification classification, TaxonNode subtree,
            Rank rank, boolean includeUnpublished, Integer pageSize, Integer pageIndex,
            List<String> propertyPaths);

    /**
     * Loads all TaxonNodes of the specified classification for a given Rank or lower.
     * If a branch of the classification tree is not containing a TaxonNode with a Taxon at the given
     * Rank the according node associated with the next lower Rank is taken as root node in this case.
     * So the nodes returned may reference Taxa with different Ranks.
     *
     * If the <code>rank</code> is null the absolute root nodes will be returned.

     * @param classification may be null for all classifications
     * @param subtree filter on a taxonomic subtree
     * @param rank the set to null for to get the root nodes of classifications
     * @param includeUnpublished if <code>true</code> unpublished taxa are also exported
     * @param pageSize The maximum number of relationships returned (can be null for all relationships)
     * @param pageIndex The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths
     * @return
     * @see #pageRankSpecificRootNodes(Classification, TaxonNode, Rank, boolean, Integer, Integer, List)
     *
     */
    public List<TaxonNodeDto> listRankSpecificRootNodeDtos(Classification classification, TaxonNode subtree,
            Rank rank, boolean includeUnpublished, Integer pageSize, Integer pageIndex, TaxonNodeDtoSortMode sortMode,
            List<String> propertyPaths);


    /**
     * Loads all TaxonNodes of the specified classification for a given Rank or lower.
     * If a branch of the classification tree is not containing a TaxonNode with a Taxon at the given
     * Rank the according node associated with the next lower Rank is taken as root node in this case.
     * So the nodes returned may reference Taxa with different Ranks.
     *
     * If the <code>rank</code> is null the absolute root nodes will be returned.
     *
     * @param classification may be null for all classifications
     * @param subtree the taxonomic subtree filter
     * @param rank the set to null for to get the root nodes of classifications
     * @param includeUnpublished if <code>true</code> unpublished taxa are also exported
     * @param pageSize The maximum number of relationships returned (can be null for all relationships)
     * @param pageIndex The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths
     * @return
     *
     * @see #listRankSpecificRootNodes(Classification, TaxonNode, Rank, boolean, Integer, Integer, List)
     */
    public Pager<TaxonNode> pageRankSpecificRootNodes(Classification classification, TaxonNode subtree,
            Rank rank, boolean includeUnpublished, Integer pageSize, Integer pageIndex,
            List<String> propertyPaths);
    /**
     * @see #pageRankSpecificRootNodes(Classification, TaxonNode, Rank, boolean, Integer, Integer, List)
     * @deprecated keep this for compatibility to older versions, might be removed in versions >5.3
     */
    @Deprecated
    public Pager<TaxonNode> pageRankSpecificRootNodes(Classification classification,
            Rank rank, boolean includeUnpublished, Integer pageSize, Integer pageIndex,
            List<String> propertyPaths);

    /**
     * @param taxonNode
     * @param baseRank
     *            specifies the root level of the classification, may be null.
     *            Nodes of this rank or in case this rank does not exist in the
     *            current branch the next lower rank is taken as root node for
     *            this rank henceforth called the <b>base node</b>.
     * @param propertyPaths
     *            the initialization strategy for the returned TaxonNode
     *            instances.
     * @param includeUnpublished
     *            if <code>true</code> no {@link UnpublishedException}
     *            is thrown if any of the taxa in the branch are unpublished
     * @return the path of nodes from the <b>base node</b> to the node of the
     *            specified taxon.
     * @throws UnpublishedException
     *            if any of the taxa in the path is unpublished an {@link UnpublishedException} is thrown.
     */
    public List<TaxonNode> loadTreeBranch(TaxonNode taxonNode, TaxonNode subtree, Rank baseRank, boolean includeUnpublished,
            List<String> propertyPaths) throws UnpublishedException;
    public List<TaxonNode> loadTreeBranch(TaxonNode taxonNode, Rank baseRank, boolean includeUnpublished,
            List<String> propertyPaths) throws UnpublishedException;

    /**
     * Although this method seems to be a redundant alternative to {@link #loadChildNodesOfTaxonNode(TaxonNode, List)} it is an important
     * alternative from which web services benefit. Without this method the web service controller method, which operates outside of the
     * transaction, would have to initialize the full taxon tree with all nodes of the taxon.
     * This would be rather slow compared to using this method.
     * @param taxon
     * @param classification
     *            the classification to be used
     * @param baseRank
     *            specifies the root level of the classification, may be null.
     *            Nodes of this rank or in case this rank does not exist in the
     *            current branch the next lower rank is taken as as root node for
     *            this rank henceforth called the <b>base node</b>.
     * @param includeUnpublished
     *            if <code>true</code> no {@link UnpublishedException}
     *            is thrown if any of the taxa in the branch are unpublished
     * @param propertyPaths
     *            the initialization strategy for the returned TaxonNode
     *            instances.
     * @return the path of nodes from the <b>base node</b> to the node of the specified
     *            taxon.
     * @throws UnpublishedException
     *            if any of the taxa in the path is unpublished an {@link UnpublishedException} is thrown
     */
    public List<TaxonNode> loadTreeBranchToTaxon(Taxon taxon, Classification classification,
            TaxonNode subtree, Rank baseRank,
            boolean includeUnpublished, List<String> propertyPaths) throws UnpublishedException;

    /**
     * Although this method seems to be a redundant alternative to {@link #loadChildNodesOfTaxonNode(TaxonNode, List)} it is an important
     * alternative from which web services benefit. Without this method the web service controller method, which operates outside of the
     * transaction, would have to initialize the full taxon tree with all nodes of the taxon.
     * This would be rather slow compared to using this method.
     * @param taxon
     * @param classification
     *            the classification to be used
     * @param baseRank
     *            specifies the root level of the classification, may be null.
     *            Nodes of this rank or in case this rank does not exist in the
     *            current branch the next lower rank is taken as as root node for
     *            this rank henceforth called the <b>base node</b>.
     * @param includeUnpublished
     *            if <code>true</code> no {@link UnpublishedException}
     *            is thrown if any of the taxa in the branch are unpublished
     * @param propertyPaths
     *            the initialization strategy for the returned TaxonNode
     *            instances.
     * @return the path of nodes from the <b>base node</b> to the node of the specified
     *            taxon.
     * @throws UnpublishedException
     *            if any of the taxa in the path is unpublished an {@link UnpublishedException} is thrown
     */
    public List<TaxonNodeDto> loadTreeBranchDTOsToTaxon(Taxon taxon, Classification classification,
            TaxonNode subtree, Rank baseRank,
            boolean includeUnpublished, List<String> propertyPaths) throws UnpublishedException;

    public List<TaxonNode> loadTreeBranchToTaxon(Taxon taxon, Classification classification,
            Rank baseRank,
            boolean includeUnpublished, List<String> propertyPaths) throws UnpublishedException;

    public List<TaxonNode> listChildNodesOfTaxon(UUID taxonUuid, UUID classificationUuid, boolean includeUnpublished,
            Integer pageSize, Integer pageIndex, List<String> propertyPaths);

    public List<TaxonNode> listChildNodesOfTaxon(UUID taxonUuid, UUID classificationUuid, UUID subtreeUuid, boolean includeUnpublished,
            Integer pageSize, Integer pageIndex, List<String> propertyPaths) throws FilterException;

    public List<TaxonNodeDto> listChildNodeDtosOfTaxon(UUID taxonUuid, UUID classificationUuid, UUID subtreeUuid, boolean includeUnpublished,
            Integer pageSize, Integer pageIndex, TaxonNodeDtoSortMode comparator) throws FilterException;

    /**
     * @param taxonNode
     * @param propertyPaths
     * @deprecated move to TaxonNodeService
     * @return
     */
    @Deprecated
    public List<TaxonNode> loadChildNodesOfTaxonNode(TaxonNode taxonNode, List<String> propertyPaths);

    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(Classification classification);

    //FIXME seems not to be used anymore
    public Map<UUID, List<MediaRepresentation>> getAllMediaForChildNodes(TaxonNode taxonNode, List<String> propertyPaths, int size, int height, int widthOrDuration, String[] mimeTypes);

    /**
     * @param taxonNodeCollection
     * @return
     * @deprecated use TaxonNodeService instead
     */
    @Deprecated
    public Map<UUID, TaxonNode> saveTaxonNodeAll(Collection<TaxonNode> taxonNodeCollection);

    public UUID removeTreeNode(ITaxonTreeNode treeNode);

    public UUID saveTreeNode(ITaxonTreeNode treeNode);

    public List<TaxonNode> getAllNodes();

	public UpdateResult createHierarchyInClassification(Classification classification, CreateHierarchyForClassificationConfigurator configurator);

    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(UUID classificationUuid);

    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(
            UUID classificationUuid, Integer limit, String pattern);

    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(
            Classification classification, Integer limit, String pattern);

    public List<TaxonNode> listSiblingsOfTaxon(UUID taxonUuid, UUID classificationUuid, boolean includeUnpublished,
            Integer pageSize, Integer pageIndex, List<String> propertyPaths);

    public Pager<TaxonNode> pageSiblingsOfTaxon(UUID taxonUuid, UUID classificationUuid, boolean includeUnpublished, Integer pageSize, Integer pageIndex,
            List<String> propertyPaths);

    public ClassificationLookupDTO classificationLookup(Classification classification);

    public DeleteResult delete(UUID classificationUuid, TaxonDeletionConfigurator config);

    /**
     * Returns the higher taxon id for each taxon in taxonUuids.
     * The highter taxon is defined by rank where the lowest rank equal or above minRank
     * is taken. If maxRank <> null and no taxon exists with minRank <= rank <= maxRank
     * no higher taxon is returned for this taxon.
     */
    public List<GroupedTaxonDTO> groupTaxaByHigherTaxon(List<UUID> taxonUuids, UUID classificationUuid, Rank minRank, Rank maxRank);

    public List<GroupedTaxonDTO> groupTaxaByMarkedParents(List<UUID> taxonUuids, UUID classificationUuid,
            MarkerType markerType, Boolean value);

    /**
     * Returns the most relevant data of a taxon/taxon node, including children, synonyms
     * and certain ancestors if required.
     */
    public TaxonInContextDTO getTaxonInContext(UUID classificationUuid, UUID taxonUuid,
            Boolean doChildren, Boolean doSynonyms, boolean includeUnpublished, List<UUID> ancestorMarkers,
            TaxonNodeSortMode sortMode);

    public UUID saveClassification(Classification classification);

    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(
            UUID classificationUuid, Integer limit, String pattern, boolean searchForClassifications);

    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(
            Classification classification, Integer limit, String pattern, boolean searchForClassifications);

    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(
            UUID classificationUuid, boolean searchForClassifications);

    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(
            Classification classification, boolean searchForClassifications);

    List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(
            UUID classificationUuid, Integer limit, String pattern, boolean searchForClassifications,
            boolean includeDoubtful);

}
