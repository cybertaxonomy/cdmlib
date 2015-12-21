// $Id$
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
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;


/**
 * @author n.hoffmann
 * @created Apr 9, 2010
 * @version 1.0
 */
public interface ITaxonNodeService extends IAnnotatableService<TaxonNode>{

	/**
	 *returns the childnodes of the taxonNode, if recursive is true it returns all descendants, if sort is true the nodes are sorted
	 *
	 * @param taxonNode
	 * @param propertyPaths
	 * @param recursive
	 * @return List<TaxonNode>
	 */
	public List<TaxonNode> loadChildNodesOfTaxonNode(TaxonNode taxonNode, List<String> propertyPaths, boolean recursive, NodeSortMode sortMode);

	/**
	 * Changes the taxon associated with the given taxon node into a synonym of the new accepted taxon node.
	 * All data associated with the former taxon are moved to the newly accepted taxon.
	 *
	 * @param oldTaxonNode
	 * @param newAcceptedTaxonNode
	 * @param synonymRelationshipType
	 * @param citation
	 * @param citationMicroReference
	 * @return
	 *
	 */
	public DeleteResult makeTaxonNodeASynonymOfAnotherTaxonNode(TaxonNode oldTaxonNode, TaxonNode newAcceptedTaxonNode, SynonymRelationshipType synonymRelationshipType, Reference citation, String citationMicroReference) ;

	public UpdateResult makeTaxonNodeASynonymOfAnotherTaxonNode(UUID oldTaxonNodeUuid,
	        UUID newAcceptedTaxonNodeUUID,
	        SynonymRelationshipType synonymRelationshipType,
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
    public UpdateResult moveTaxonNode(UUID taxonNodeUuid, UUID newParentTaxonNodeUuid, boolean parent);



    /**
     * @param taxonNodeUuids
     * @param newParentNodeUuid
     * @return
     */
    UpdateResult moveTaxonNodes(Set<UUID> taxonNodeUuids, UUID newParentNodeUuid, boolean parent);

    /**
     * @param taxonNode
     * @param newParent
     * @param parent
     * @return
     */
    UpdateResult moveTaxonNode(TaxonNode taxonNode, TaxonNode newParent, boolean parent);

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


}
