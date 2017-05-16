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
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.config.TaxonDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.ITaxonTreeNode;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;


/**
 * @author n.hoffmann
 * @created Apr 9, 2010
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
	public List<TaxonNode> loadChildNodesOfTaxonNode(TaxonNode taxonNode, List<String> propertyPaths, boolean recursive, NodeSortMode sortMode);

	/**
	 * Lists all direct child nodes of the given {@link UuidAndTitleCache} which
	 * represents the parent {@link TaxonNode}
	 * @param parent a UuidAndTitleCache object which represents a parent {@link TaxonNode}
	 * @return a list of UuidAndTitleCache objects that represent children of the
	 * parent
	 */
	public List<UuidAndTitleCache<TaxonNode>> listChildNodesAsUuidAndTitleCache(UuidAndTitleCache<TaxonNode> parent);

	/**
     * Retrieves a list of {@link UuidAndTitleCache} objects that have a matchin titleCache
     * @param limit the maximum results
     * @param pattern the titleCache that is searched for
     * @param classificationUuid if specified only nodes of this classification are retrieved
     * @return a list of matches
     */
	public List<UuidAndTitleCache<TaxonNode>> getUuidAndTitleCache(Integer limit, String pattern, UUID classificationUuid);

    /**
     * Retrieves the parent node of the child {@link TaxonNode}
     * @param child the child for which the parent should be retrieved
     * @return the parent taxon node
     */
	public UuidAndTitleCache<TaxonNode> getParentUuidAndTitleCache(ITaxonTreeNode child);

	/**
     * Retrieves the parent node of the {@link TaxonNode} represented by the given {@link UuidAndTitleCache}.
     * @param child the child for which the parent should be retrieved
     * @return an UuidAndTitleCache object representing the parent node
     */
	public UuidAndTitleCache<TaxonNode> getParentUuidAndTitleCache(UuidAndTitleCache<TaxonNode> child);

	/**
     * Lists all direct child nodes of the given {@link ITaxonTreeNode}
     * @param parent the parent ITaxonTreeNode
     * @return a list of UuidAndTitleCache objects that represent children of the
     * parent
     */
	public List<UuidAndTitleCache<TaxonNode>> listChildNodesAsUuidAndTitleCache(ITaxonTreeNode parent);

	/**
     *returns the childnodes of the taxonNode, if recursive is true it returns all descendants, if sort is true the nodes are sorted
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
    public Pager<TaxonNodeDto> pageChildNodesDTOs(UUID taxonNodeUuid, boolean recursive,
            boolean doSynonyms, NodeSortMode sortMode,
            Integer pageSize, Integer pageIndex);

    public TaxonNodeDto parentDto(UUID taxonNodeUuid);

	/**
	 * Changes the taxon associated with the given taxon node into a synonym of the new accepted taxon node.
	 * All data associated with the former taxon are moved to the newly accepted taxon.
	 *
	 * @param oldTaxonNode
	 * @param newAcceptedTaxonNode
	 * @param synonymType
	 * @param citation
	 * @param citationMicroReference
	 * @return
	 *
	 */
	public DeleteResult makeTaxonNodeASynonymOfAnotherTaxonNode(TaxonNode oldTaxonNode, TaxonNode newAcceptedTaxonNode, SynonymType synonymType, Reference citation, String citationMicroReference) ;

	public UpdateResult makeTaxonNodeASynonymOfAnotherTaxonNode(UUID oldTaxonNodeUuid,
	        UUID newAcceptedTaxonNodeUUID,
	        SynonymType synonymType,
	        Reference citation,
	        String citationMicroReference) ;


    /**
     * @param nodeUuids
     * @param config
     * @return
     */
    public DeleteResult deleteTaxonNodes(Collection<UUID> nodeUuids, TaxonDeletionConfigurator config);

	/**
	 * deletes the given taxon node the configurator defines whether the children will be deleted too or not
	 *
	 * @param node
	 * @param conf
	 * @return
	 *
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
	 * @return
	 */
	public int countAllNodesForClassification(Classification classification);


    /**
     * @param taxonNodeUuid
     * @param newParentTaxonNodeUuid
     * @return
     */
    public UpdateResult moveTaxonNode(UUID taxonNodeUuid, UUID newParentTaxonNodeUuid, int movingType);



    /**
     * @param taxonNodeUuids
     * @param newParentNodeUuid
     * @return
     */
    UpdateResult moveTaxonNodes(Set<UUID> taxonNodeUuids, UUID newParentNodeUuid, int movingType);

    /**
     * @param taxonNode
     * @param newParent
     * @param parent
     * @return
     */
    UpdateResult moveTaxonNode(TaxonNode taxonNode, TaxonNode newParent, int movingType);

    /**
     * deletes the given taxon nodes
     *
     * @param nodes
     * @param config
     * @return
     *
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

    /**
     * @param parentNodeUuid
     * @param newTaxon
     * @param ref
     * @param microref
     * @return
     */
   public UpdateResult createNewTaxonNode(UUID parentNodeUuid, Taxon newTaxon, Reference ref, String microref);

    /**
     * @param taxonNodeUUID
     * @param agentUUID
     * @param relationshipType
     * @return
     */
    UpdateResult addTaxonNodeAgentRelation(UUID taxonNodeUUID, UUID agentUUID, DefinedTerm relationshipType);

    /**
     * @param parentNodeUuid
     * @param taxonUuid
     * @param ref
     * @param microref
     * @return
     */
    UpdateResult createNewTaxonNode(UUID parentNodeUuid, UUID taxonUuid, Reference ref, String microref);

    /**
     * Sets the secundum reference for all taxa of the given subtree.
     * Depending on the configuration, also synonym secundum will be set.
     * See {@link SetSecundumForSubtreeConfigurator} for further configuration
     * options.
     * @param b
     * @param configurator
     * @return UpdateResult
     */
    UpdateResult setSecundumForSubtree(UUID subtreeUuid,  Reference newSec, boolean includeAcceptedTaxa, boolean includeSynonyms, boolean overwriteExistingAccepted, boolean overwriteExistingSynonyms, boolean includeSharedTaxa, boolean emptyDetail, IProgressMonitor monitor);

}
