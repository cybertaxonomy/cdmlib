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
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.ITreeNode;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.persistence.query.OrderHint;


/**
 * @author n.hoffmann
 * @created Sep 21, 2009
 * @version 1.0
 */
public interface IClassificationService extends IIdentifiableEntityService<Classification> {

	/**
	 * 
	 * @param uuid
	 * @return
	 */
	public TaxonNode getTaxonNodeByUuid(UUID uuid);
	
	/**
	 * 
	 * @param uuid
	 * @return
	 */
	public ITreeNode getTreeNodeByUuid(UUID uuid);

	/**
	 * 
	 * @param limit
	 * @param start
	 * @param orderHints
	 * @param propertyPaths
	 * @return
	 */
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
	public TaxonNode loadTaxonNodeByTaxon(Taxon taxon, UUID classificationUuid, List<String> propertyPaths);
	
	/**
	 * 
	 * @param taxonNode
	 * @param propertyPaths
	 * @return
	 * @deprecated use TaxonNodeService instead
	 */
	public TaxonNode loadTaxonNode(TaxonNode taxonNode, List<String> propertyPaths);
	
	/**
	 * Loads all TaxonNodes of the specified tree for a given Rank.
	 * If a branch does not contain a TaxonNode with a TaxonName at the given
	 * Rank the node associated with the next lower Rank is taken as root node.
	 * If the <code>rank</code> is null the absolute root nodes will be returned.
	 *
	 * @param classification
	 * @param rank may be null
	 * @param propertyPaths
	 * @return
	 */
	public List<TaxonNode> loadRankSpecificRootNodes(Classification classification, Rank rank, List<String> propertyPaths);

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
	 * @return the path of nodes from the <b>base node</b> to the node of the
	 *         specified taxon.
	 */
	public List<TaxonNode> loadTreeBranch(TaxonNode taxonNode, Rank baseRank, List<String> propertyPaths);
	
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
	 * @param propertyPaths
	 *            the initialization strategy for the returned TaxonNode
	 *            instances.
	 * @return the path of nodes from the <b>base node</b> to the node of the specified
	 *         taxon.
	 */
	public List<TaxonNode> loadTreeBranchToTaxon(Taxon taxon, Classification classification, Rank baseRank, List<String> propertyPaths);
		
	
	
	/**
	 * Although this method seems to be a redundant alternative to {@link #loadChildNodesOfTaxonNode(TaxonNode, List)} it is an important 
	 * alternative from which web services benefit. Without this method the web service controller method, which operates outside of the 
	 * transaction, would have to initialize the full taxon tree with all nodes of the taxon. 
	 * This would be rather slow compared to using this method. 
	 * @param taxon
	 * @param classification
	 * @param propertyPaths
	 * @return
	 */
	public List<TaxonNode> loadChildNodesOfTaxon(Taxon taxon, Classification classification, List<String> propertyPaths);
	
	/**
	 * @param taxonNode
	 * @param propertyPaths
	 * @deprecated move to TaxonNodeService
	 * @return
	 */
	public List<TaxonNode> loadChildNodesOfTaxonNode(TaxonNode taxonNode, List<String> propertyPaths);
	
	/**
	 * 
	 * @param classification
	 * @return
	 */
	public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(Classification classification);
	
	/**
	 * @param taxon
	 * @param taxTree
	 * @param propertyPaths
	 * @param size
	 * @param height
	 * @param widthOrDuration
	 * @param mimeTypes
	 * @return
	 *  @deprecated use getAllMediaForChildNodes(TaxonNode taxonNode, ...) instead
	 * if you have a classification and a taxon that is in it, you should also have the according taxonNode
	 */
	public Map<UUID, List<MediaRepresentation>> getAllMediaForChildNodes(Taxon taxon, Classification taxTree, List<String> propertyPaths, int size, int height, int widthOrDuration, String[] mimeTypes);
	
	/**
	 * 
	 * @param taxonNode
	 * @param propertyPaths
	 * @param size
	 * @param height
	 * @param widthOrDuration
	 * @param mimeTypes
	 * @return
	 */
	public Map<UUID, List<MediaRepresentation>> getAllMediaForChildNodes(TaxonNode taxonNode, List<String> propertyPaths, int size, int height, int widthOrDuration, String[] mimeTypes);
	
	/**
	 * 
	 * @param taxonNode
	 * @return
	 * @deprecated use TaxonNodeService instead
	 */
	public UUID removeTaxonNode(TaxonNode taxonNode);
	
	/**
	 * 
	 * @param taxonNode
	 * @return
	 * @deprecated use TaxonNodeService instead
	 */
	public UUID saveTaxonNode(TaxonNode taxonNode);
	
	/**
	 * 
	 * @param taxonNodeCollection
	 * @return
	 * @deprecated use TaxonNodeService instead
	 */
	public Map<UUID, TaxonNode> saveTaxonNodeAll(Collection<TaxonNode> taxonNodeCollection);
	
	/**
	 * 
	 * @param treeNode
	 * @return
	 */
	public UUID removeTreeNode(ITreeNode treeNode);
	
	/**
	 * 
	 * @param treeNode
	 * @return
	 */
	public UUID saveTreeNode(ITreeNode treeNode);

	
	public List<TaxonNode> getAllNodes();


}
