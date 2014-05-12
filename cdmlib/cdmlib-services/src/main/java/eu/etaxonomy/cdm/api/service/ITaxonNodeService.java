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

import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.config.TaxonDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.config.TaxonNodeDeletionConfigurator;
import eu.etaxonomy.cdm.api.service.exception.DataChangeNoRollbackException;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.ITaxonTreeNode;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;


/**
 * @author n.hoffmann
 * @created Apr 9, 2010
 * @version 1.0
 */
public interface ITaxonNodeService extends IAnnotatableService<TaxonNode>{

	/**
	 *
	 * @param uuid
	 */
	@Deprecated // use findByUuid() instead; TODO will be removed in the next version
	public TaxonNode getTaxonNodeByUuid(UUID uuid);

	/**
	 *
	 * @param taxonNode
	 * @param propertyPaths
	 * @return
	 */
	public List<TaxonNode> loadChildNodesOfTaxonNode(TaxonNode taxonNode, List<String> propertyPaths);

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
	public Synonym makeTaxonNodeASynonymOfAnotherTaxonNode(TaxonNode oldTaxonNode, TaxonNode newAcceptedTaxonNode, SynonymRelationshipType synonymRelationshipType, Reference citation, String citationMicroReference) ;

	/**
	 * deletes the given taxon nodes
	 *
	 * @param nodes
	 * @param config
	 * @return
	 *
	 */
	List<UUID> deleteTaxonNodes(Set<ITaxonTreeNode> nodes,
			TaxonDeletionConfigurator config) ;
	/**
	 * deletes the given taxon node the configurator defines whether the children will be deleted too or not
	 *
	 * @param node
	 * @param conf
	 * @return
	 *
	 */
	public String deleteTaxonNode(TaxonNode node, TaxonDeletionConfigurator config);
	/**
	 * Returns a List of all TaxonNodes of a given Classification.
	 *
	 * @param classification - according to the given classification the TaxonNodes are filtered.
	 * @param start -  beginning of wanted row set, i.e. 0 if one want to start from the beginning.
	 * @param end  - limit of how many rows are to be pulled from the database, i.e. 1000 rows.
	 * @return filtered List of TaxonNode according to the classification provided
	 */
	public List<TaxonNode> listAllNodesForClassification(Classification classification, Integer start, Integer end);

	/**
	 * Counts all TaxonNodes for a given Classification
	 *
	 * @param classification - according to the given classification the TaxonNodes are filtered.
	 * @return
	 */
	public int countAllNodesForClassification(Classification classification);






}
