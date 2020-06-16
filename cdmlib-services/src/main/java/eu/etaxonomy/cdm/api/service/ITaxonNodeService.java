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
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.Authentication;

import eu.etaxonomy.cdm.api.service.config.PublishForSubtreeConfigurator;
import eu.etaxonomy.cdm.api.service.config.SecundumForSubtreeConfigurator;
import eu.etaxonomy.cdm.api.service.config.TaxonDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.dto.CreateTaxonDTO;
import eu.etaxonomy.cdm.api.service.dto.TaxonDistributionDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.ITaxonTreeNode;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author n.hoffmann
 * @since Apr 9, 2010
 */
public interface ITaxonNodeService extends IAnnotatableService<TaxonNode>{

	/**
	 * returns the childnodes of the taxonNode, if recursive is true it returns all descendants, if sort is true the nodes are sorted
	 *
	 * @param taxonNode
	 * @param propertyPaths
	 * @param recursive
	 * @return List<TaxonNode>
	 */
	public List<TaxonNode> loadChildNodesOfTaxonNode(TaxonNode taxonNode, List<String> propertyPaths, boolean recursive,  boolean includeUnpublished, NodeSortMode sortMode);

	/**
	 * Lists all direct child nodes of the given {@link UuidAndTitleCache} which
	 * represents the parent {@link TaxonNode}
	 * @param parent a UuidAndTitleCache object which represents a parent {@link TaxonNode}
	 * @return a list of UuidAndTitleCache objects that represent children of the
	 * parent
	 */
//	public List<UuidAndTitleCache<TaxonNode>> listChildNodesAsUuidAndTitleCache(UuidAndTitleCache<TaxonNode> parent);

    public List<TaxonNode> listChildrenOf(TaxonNode node, Integer pageSize, Integer pageIndex,
            boolean recursive, boolean includeUnpublished, List<String> propertyPaths);

	/**
     * Retrieves a list of {@link UuidAndTitleCache} objects that have a matchin titleCache
     *
     * @param limit the maximum results
     * @param pattern the titleCache that is searched for
     * @param classificationUuid if specified only nodes of this classification are retrieved
     * @return a list of matches
     */
	public List<TaxonNodeDto> getUuidAndTitleCache(Integer limit, String pattern, UUID classificationUuid);

    /**
     * Retrieves the parent node of the child {@link TaxonNode}
     * @param child the child for which the parent should be retrieved
     * @return the parent taxon node
     */
	public TaxonNodeDto getParentUuidAndTitleCache(ITaxonTreeNode child);

	/**
     * Retrieves the parent node of the {@link TaxonNode} represented by the given {@link UuidAndTitleCache}.
     * @param child the child for which the parent should be retrieved
     * @return an UuidAndTitleCache object representing the parent node
     */
	public TaxonNodeDto getParentUuidAndTitleCache(TaxonNodeDto child);

//	/**
//     * Lists all direct child nodes of the given {@link ITaxonTreeNode}
//     * @param parent the parent ITaxonTreeNode
//     * @return a list of UuidAndTitleCache objects that represent children of the
//     * parent
//     */
//	public List<UuidAndTitleCache<TaxonNode>> listChildNodesAsUuidAndTitleCache(ITaxonTreeNode parent);

	/**
     *Returns the childnodes of the taxonNode, if recursive is true it returns all descendants, if sort is true the nodes are sorted
     *
     * @param taxonNode
     * @param recursive
     * @param doSynonyms if true also synonyms are returned as children
     * @param sortMode
     * @param pageSize
     * @param pageIndex
     *
     * @return List<TaxonNodeDto>
     */
    public Pager<TaxonNodeDto> pageChildNodesDTOs(UUID taxonNodeUuid, boolean recursive, boolean includeUnpublished,
            boolean doSynonyms, NodeSortMode sortMode,
            Integer pageSize, Integer pageIndex);

    public TaxonNodeDto parentDto(UUID taxonNodeUuid);

	/**
	 * Changes the taxon associated with the given taxon node into a synonym of the new accepted taxon node.
	 * All data associated with the former taxon are moved to the newly accepted taxon.
	 */
	public DeleteResult makeTaxonNodeASynonymOfAnotherTaxonNode(TaxonNode oldTaxonNode, TaxonNode newAcceptedTaxonNode, SynonymType synonymType, Reference citation, String citationMicroReference, boolean setNameInSource) ;

	/**
	 * Changes the taxa associated with the given taxon nodes into synonyms of the new accepted taxon node.
	 * All data associated with the former taxa are moved to the newly accepted taxon.
	 */
	public UpdateResult makeTaxonNodeSynonymsOfAnotherTaxonNode(Set<UUID> oldTaxonNodeUuids, UUID newAcceptedTaxonNodeUUIDs,
			SynonymType synonymType, Reference citation, String citationMicroReference, boolean setNameInSource);

	public UpdateResult makeTaxonNodeASynonymOfAnotherTaxonNode(UUID oldTaxonNodeUuid,
	        UUID newAcceptedTaxonNodeUUID,
	        SynonymType synonymType,
	        Reference citation,
	        String citationMicroReference,
	        boolean setNameInSource) ;

    public DeleteResult deleteTaxonNodes(Collection<UUID> nodeUuids, TaxonDeletionConfigurator config);

	/**
	 * deletes the given taxon node the configurator defines whether the children will be deleted too or not
	 */
	public DeleteResult deleteTaxonNode(TaxonNode node, TaxonDeletionConfigurator config);

	/**
	 * Returns a List of all TaxonNodes of a given Classification.
	 *
	 * @param classification - according to the given classification the TaxonNodes are filtered.
	 * @param start -  beginning of wanted row set, i.e. 0 if one want to start from the beginning.
	 * @param end  - limit of how many rows are to be pulled from the database, i.e. 1000 rows.
	 * @return filtered List of TaxonNode according to the classification provided
	 */

    /**
     * @param nodeUuid
     * @param config
     * @return
     */
    public DeleteResult deleteTaxonNode(UUID nodeUuid, TaxonDeletionConfigurator config);

	public List<TaxonNode> listAllNodesForClassification(Classification classification, Integer start, Integer end);

	/**
	 * Counts all TaxonNodes for a given Classification
	 *
	 * @param classification - according to the given classification the TaxonNodes are filtered.
	 * @return the count result
	 */
	public int countAllNodesForClassification(Classification classification);

    public UpdateResult moveTaxonNode(UUID taxonNodeUuid, UUID newParentTaxonNodeUuid, int movingType);

    public UpdateResult moveTaxonNode(TaxonNode taxonNode, TaxonNode newParent, int movingType);

    public UpdateResult moveTaxonNodes(Set<UUID> taxonNodeUuids, UUID newParentNodeUuid, int movingType,
            IProgressMonitor monitor);

    /**
     * deletes the given taxon nodes
     */
    public DeleteResult deleteTaxonNodes(List<TaxonNode> list, TaxonDeletionConfigurator config);

    /**
     * Returns the of TaxonNodeAgentRelation entities which are associated with the TaxonNode for the
     * given TaxonUuid in the specified Classification.
     *
     * @param taxonUuid
     * @param agentUuid TODO
     * @param rankUuid TODO
     * @param relTypeUuid TODO
     * @param classification
     * @return
     */
    public Pager<TaxonNodeAgentRelation> pageTaxonNodeAgentRelations(UUID taxonUuid, UUID classificationUuid,
            UUID agentUuid, UUID rankUuid, UUID relTypeUuid, Integer pageSize, Integer pageIndex, List<String> propertyPaths);

    public UpdateResult createNewTaxonNode(UUID parentNodeUuid, CreateTaxonDTO taxonDto, UUID refUuid, String microref,
            TaxonNodeStatus status, Map<Language,LanguageString> statusNote);

    public UpdateResult addTaxonNodeAgentRelation(UUID taxonNodeUUID, UUID agentUUID, DefinedTerm relationshipType);

    /**
     * Creates a new taxon node for the given existing taxon with the given existing parent taxon node.
     */
    public UpdateResult createNewTaxonNode(UUID parentNodeUuid, UUID taxonUuid, UUID refUuid, String microref);

    /**
     * Sets the secundum reference for all taxa of the given subtree.
     * Depending on the configuration, also synonym secundum will be set.
     * See {@link SetSecundumForSubtreeConfigurator} for further configuration
     * options.
     * @return UpdateResult
     */
    public UpdateResult setSecundumForSubtree(SecundumForSubtreeConfigurator config);


    /**
     * Sets the publish flag for all taxa and/or synonyms of the subtree.
     */
    public UpdateResult setPublishForSubtree(PublishForSubtreeConfigurator configurator);

    /**
     * Returns a list of taxon node {@link UUID uuids} according to the given filter.
     */
    public long count(TaxonNodeFilter filter);

    /**
     * Returns a list of taxon node {@link UUID uuids} according to the given filter.
     */
    public List<UUID> uuidList(TaxonNodeFilter filter);

    /**
     * Returns a list of taxon node IDs according to the given filter.
     */
    public List<Integer> idList(TaxonNodeFilter filter);

    public List<TaxonNodeDto> listChildNodesAsTaxonNodeDto(TaxonNodeDto parent);


    public List<TaxonNodeDto> listChildNodesAsTaxonNodeDto(ITaxonTreeNode parent);

    /**
     * Retrieves the first taxon node that is direct or indirect parent
     * to all nodes of the given list of nodes. This can also return
     * the root node of a classification<br>
     * If no common parent node could be found <code>null</code> is returned.
     * @param nodes the direct/indirect child taxon nodes for which the common
     * parent should be retrieved
     * @return the common direct/indirect parent of all nodes
     */
    public TaxonNodeDto findCommonParentDto(Collection<TaxonNodeDto> nodes);

    public TaxonNodeDto dto(UUID taxonNodeUuid);

//    public List<TaxonDistributionDTO> getTaxonDistributionDTOForSubtree(UUID parentNodeUuid, List<String> propertyPaths, Authentication authentication, boolean openChildren);
//
//    public List<TaxonDistributionDTO> getTaxonDistributionDTOForSubtree(UUID parentNodeUuid, List<String> propertyPaths, boolean openChildren);

    public UpdateResult saveNewTaxonNode(TaxonNode newTaxonNode);

    public <S extends TaxonNode> Pager<S> page(Class<S> clazz, List<Restriction<?>> restrictions, Integer pageSize, Integer pageIndex,
            List<OrderHint> orderHints, List<String> propertyPaths, boolean includeUnpublished);

	public List<TaxonNodeDto> taxonNodeDtoParentRank(Classification classification, Rank rank, TaxonName name);

	public List<TaxonNodeDto> taxonNodeDtoParentRank(Classification classification, Rank rank, TaxonBase<?> taxonBase);

    /**
     * @param nodeUuids
     * @param propertyPaths
     * @param authentication
     * @param openChildren
     * @return
     */
    List<TaxonDistributionDTO> getTaxonDistributionDTO(List<UUID> nodeUuids, List<String> propertyPaths,
            Authentication authentication, boolean openChildren);

    /**
     * @param nodeUuids
     * @param propertyPaths
     * @return
     */
    List<TaxonDistributionDTO> getTaxonDistributionDTO(List<UUID> nodeUuids, List<String> propertyPaths, boolean openChildren);
}
