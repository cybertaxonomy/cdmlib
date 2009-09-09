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
import java.util.TreeMap;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;
import eu.etaxonomy.cdm.persistence.query.OrderHint;


public interface ITaxonService extends IIdentifiableEntityService<TaxonBase>{
	
	/**
	 * FIXME candidate for harmonization? 
	 */
	public abstract TaxonBase getTaxonByUuid(UUID uuid);
	
	/**
	 * 
	 * @param uuid
	 * @return
	 */
	public abstract TaxonNode getTaxonNodeByUuid(UUID uuid);
	
	/**
	 * @param taxon
	 * @param taxonomicTree
	 * @param propertyPath
	 * @return
	 */
	public TaxonNode loadTaxonNodeByTaxon(Taxon taxon, UUID taxonomicTreeUuid, List<String> propertyPaths);
	
	/**
	 * @param taxonNode
	 * @param baseRank
	 *            specifies the root level of the taxonomic tree, may be null.
	 *            Nodes of this rank or in case this rank does not exist in the
	 *            current branch the next lower rank is taken as root node for
	 *            this rank henceforth called the <b>base node</b>.
	 * @param propertyPaths
	 *            the initialization strategy for the returned TaxonNode
	 *            instances.
	 * @return the path of nodes from the <b>base node</b> to the node of the
	 *         specified taxon.
	 */
	public List<TaxonNode> loadTreeBranchTo(TaxonNode taxonNode, Rank baseRank, List<String> propertyPaths);

	/**
	 * @param taxon
	 * @param taxonomicTree
	 *            the taxonomic tree to be used
	 * @param baseRank
	 *            specifies the root level of the taxonomic tree, may be null.
	 *            Nodes of this rank or in case this rank does not exist in the
	 *            current branch the next lower rank is taken as as root node for
	 *            this rank henceforth called the <b>base node</b>.
	 * @param propertyPaths
	 *            the initialization strategy for the returned TaxonNode
	 *            instances.
	 * @return the path of nodes from the <b>base node</b> to the node of the specified
	 *         taxon.
	 */
	public List<TaxonNode> loadTreeBranchToTaxon(Taxon taxon, TaxonomicTree taxonomicTree, Rank baseRank,
			List<String> propertyPaths);
	
	/**
	 * @param taxon
	 * @param taxonomicTree
	 * @param propertyPaths
	 * @return
	 */
	public List<TaxonNode> loadChildNodesOfTaxon(Taxon taxon, TaxonomicTree taxonomicTree, List<String> propertyPaths);
	
	
	/**
	 * FIXME candidate for harmonization? 
	 * save a taxon and return its UUID
	 */
	public abstract UUID saveTaxon(TaxonBase taxon);

	/**
	 * 
	 * @param taxonNode
	 * @return
	 */
	public abstract UUID saveTaxonNode(TaxonNode taxonNode);
	
	/**
	 * FIXME candidate for harmonization?
	 *  save a taxon and return its UUID
	 */
	//public abstract UUID saveTaxon(TaxonBase taxon, TransactionStatus txStatus);
	
	/**
	 * FIXME candidate for harmonization?
	 * save a collection of taxa and return its UUID
	 */
	public abstract Map<UUID, ? extends TaxonBase> saveTaxonAll(Collection<? extends TaxonBase> taxonCollection);

	/**
	 * 
	 * @param taxonNodeCollection
	 * @return
	 */
	public abstract Map<UUID, TaxonNode> saveTaxonNodeAll(Collection<TaxonNode> taxonNodeCollection);
	
	/**
	 * FIXME candidate for harmonization?
	 * delete a taxon and return its UUID
	 */
	public abstract UUID removeTaxon(TaxonBase taxon);
	
	/**
	 * Computes all taxon bases.
	 * FIXME could substitute with list(Synonym.class, limit, start)
	 * @param limit
	 * @param start
	 * @return
	 */
	public abstract List<Synonym> getAllSynonyms(int limit, int start);
	
	/**
	 * Computes all taxon bases.
	 * FIXME could substitute with list(Taxon.class, limit,start)
	 * @param limit
	 * @param start
	 * @return
	 */
	public abstract List<Taxon> getAllTaxa(int limit, int start);
	
	/**
	 * Computes all taxon bases.
	 * FIXME could substitute with list(limit,start) from superclass
	 * @param limit
	 * @param start
	 * @return
	 */
	public abstract List<TaxonBase> getAllTaxonBases(int limit, int start);
	
	
	/**
	 * Computes all taxonomic trees.
	 * FIXME candidate for harmonization
	 * @param limit
	 * @param start
	 * @return
	 * @deprecated use {#listTaxonomicTrees} instead
	 */
	@Deprecated
	public List<TaxonomicTree> getAllTaxonomicTrees(int limit, int start);

	/**
	 * @param limit
	 * @param start
	 * @param orderHints
	 * @param propertyPaths
	 * @return
	 */
	public List<TaxonomicTree> listTaxonomicTrees(Integer limit, Integer start, List<OrderHint> orderHints,
			List<String> propertyPaths);
	
	
	/**
	 * Loads all TaxonNodes of the specified tree for a given Rank.
	 * If a branch does not contain a TaxonNode with a TaxonName at the given
	 * Rank the node associated with the next lower Rank is taken as root node.
	 * If the <code>rank</code> is null the absolute root nodes will be returned.
	 *
	 * @param taxonomicTree
	 * @param rank may be null
	 * @param propertyPaths
	 * @return
	 */
	public List<TaxonNode> loadRankSpecificRootNodes(TaxonomicTree taxonomicTree, Rank rank, List<String> propertyPaths);
	
	
	/**
	 * Returns a taxonomic tree by it's uuid.
	 * @param uuid
	 * @return
	 */
	public TaxonomicTree getTaxonomicTreeByUuid(UUID uuid);
	
	/**
	 * Returns a taxonomic tree by it's uuid.
	 * @param uuid
	 * @return
	 */
	public UUID saveTaxonomicTree(TaxonomicTree tree);
	
	/**
	 * Remove the taxonomic tree from the persitence context
	 * 
	 * @param taxonomicTree
	 * @return
	 */
	public UUID removeTaxonomicTree(TaxonomicTree taxonomicTree);
	
	/**
	 * Computes all Taxon instances that do not have a taxonomic parent and has at least one child.
	 * @param sec The concept reference that the taxon belongs to
	 * @return The List<Taxon> of root taxa.
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec);
	

	/**
	 * Computes all Taxon instances that do not have a taxonomic parent.
	 * @param sec The concept reference that the taxon belongs to
	 * 
	 * @param onlyWithChildren if true only taxa are returned that have taxonomic children. <Br>Default: true.
	 * @return The List<Taxon> of root taxa.
	 */
	public abstract List<Taxon> getRootTaxa(ReferenceBase sec, CdmFetch cdmFetch, boolean onlyWithChildren);

	/**
	 * Computes all Taxon instances that do not have a taxonomic parent.
	 * @param sec The concept reference that the taxon belongs to
	 * @param onlyWithChildren if true only taxa are returned that have taxonomic children. <Br>Default: true.
	 * @param withMisapplications if false taxa that have at least one misapplied name relationship in which they are
	 * the misapplied name are not returned.<Br>Default: true.
	 * @return The List<Taxon> of root taxa.
	 */
	public abstract List<Taxon> getRootTaxa(ReferenceBase sec, boolean onlyWithChildren, boolean withMisapplications);

	/**
	 * Computes all Taxon instances which name is of a certain Rank.
	 * @param rank The rank of the taxon name
	 * @param sec The concept reference that the taxon belongs to
	 * @param onlyWithChildren if true only taxa are returned that have taxonomic children. <Br>Default: true.
	 * @param withMisapplications if false taxa that have at least one misapplied name relationship in which they are
	 * the misapplied name are not returned.<Br>Default: true.
	 * @param propertyPaths
	 *            properties to be initialized, For detailed description and
	 *            examples <b>please refer to:</b>
	 *            {@link BeanInitializer#initialize(Object, List)}. <Br>
	 *            Default: true.
	 * @return The List<Taxon> of root taxa.
	 */
	public abstract List<Taxon> getRootTaxa(Rank rank, ReferenceBase sec, boolean onlyWithChildren, boolean withMisapplications, List<String> propertyPaths);
	
	/**
	 * Computes all relationships.
	 * @param limit
	 * @param start
	 * @return
	 */
    public abstract List<RelationshipBase> getAllRelationships(int limit, int start);

	/**
	 * Returns TaxonRelationshipType vocabulary
	 * @return
	 * @deprecated use TermService#getVocabulary(VocabularyType) instead
	 */
	public OrderedTermVocabulary<TaxonRelationshipType> getTaxonRelationshipTypeVocabulary();

	/** */
	public abstract List<TaxonBase> searchTaxaByName(String name, ReferenceBase sec);
		
	/**
	 * Changes an accepted taxon to a synonym of another taxon. 
	 * 
	 * @param oldTaxon
	 * 				the accepted taxon that will be changed into a synonym
	 * @param newAcceptedTaxon
	 * 				the accepted taxon, the old taxon will become a synonym of
	 * @param synonymType
	 * 				<code>SynonymRelationshipType</code> to indicate wether hetero or homotypic
	 * @param citation
	 * 				the reference for this assumption
	 * @param citationMicroReference
	 * 				the micro citation
	 * @return
	 * 				the newly created synonym
	 */
	public Synonym makeTaxonSynonym (Taxon oldTaxon, Taxon newAcceptedTaxon, SynonymRelationshipType synonymType, ReferenceBase citation, String citationMicroReference);
	
	/**
	 * Sets the synonyms name as the accepted taxons name. Deletes the synonym from the accepted taxon synonym list
	 * and attaches a new synonym created with the former name of the accepted taxon 
	 * 
	 * @param synonym 
	 * @param acceptedTaxon
	 * @param synonymRelationshipType the relationship type the newly created synonym will have. Defaults to SYNONYM_OF
	 */
	public void makeSynonymAcceptedTaxon(Synonym synonym, Taxon acceptedTaxon, SynonymRelationshipType synonymRelationshipType );
	
	/**
	 * Returns the TaxonRelationships (of where relationship.type == type, if this argument is supplied) 
	 * where the supplied taxon is relatedTo.
	 * 
	 * @param taxon The taxon that is relatedTo
	 * @param type The type of TaxonRelationship (can be null)
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints Properties to order by
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
	 * @return a List of TaxonRelationship instances
	 */
	public List<TaxonRelationship> listToTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
		
	/**
	 * Returns the TaxonRelationships (of where relationship.type == type, if this arguement is supplied) 
	 * where the supplied taxon is relatedTo.
	 * 
	 * @param taxon The taxon that is relatedTo
	 * @param type The type of TaxonRelationship (can be null)
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints Properties to order by
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
	 * @return a Pager of TaxonRelationship instances
	 */
	public Pager<TaxonRelationship> pageToTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * Returns the TaxonRelationships (of where relationship.type == type, if this argument is supplied) 
	 * where the supplied taxon is relatedFrom.
	 * 
	 * @param taxon The taxon that is relatedFrom
	 * @param type The type of TaxonRelationship (can be null)
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints Properties to order by
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
	 * @return a List of TaxonRelationship instances
	 */
	public List<TaxonRelationship> listFromTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * Returns the TaxonRelationships (of where relationship.type == type, if this argument is supplied) 
	 * where the supplied taxon is relatedFrom.
	 * 
	 * @param taxon The taxon that is relatedFrom
	 * @param type The type of TaxonRelationship (can be null)
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints Properties to order by
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
	 * @return a Pager of TaxonRelationship instances
	 */
	public Pager<TaxonRelationship> pageFromTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * Returns the SynonymRelationships (of where relationship.type == type, if this argument is supplied) 
	 * where the supplied taxon is relatedTo.
	 * 
	 * @param taxon The taxon that is relatedTo
	 * @param type The type of SynonymRelationship (can be null)
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * * @param orderHints Properties to order by
	 * @param propertyPaths Properties to initialize in the returned entities, following the syntax described in {@link BeanInitializer#initialize(Object, List)}
	 * @return a Pager of SynonymRelationship instances
	 */
	public Pager<SynonymRelationship> getSynonyms(Taxon taxon, SynonymRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	public List<Synonym> getHomotypicSynonymsByHomotypicGroup(Taxon taxon, List<String> propertyPaths);
	
	public List<List<Synonym>> getHeterotypicSynonymyGroups(Taxon taxon, List<String> propertyPaths);
	
	/**
	 * Returns a Paged List of TaxonBase instances where the default field matches the String queryString (as interpreted by the Lucene QueryParser)
	 * 
	 * @param clazz filter the results by class (or pass null to return all TaxonBase instances)
	 * @param queryString
	 * @param pageSize The maximum number of taxa returned (can be null for all matching taxa)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @param propertyPaths properties to be initialized
	 * @return a Pager Taxon instances
	 * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
	 */
	public Pager<TaxonBase> search(Class<? extends TaxonBase> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * Returns a list of TaxonBase instances where the
	 * taxon.name properties match the parameters passed.
	 * 
	 * @param clazz optionally filter by class 
	 * @param uninomial 
	 * @param infragenericEpithet
	 * @param specificEpithet
	 * @param infraspecificEpithet
	 * @param rank
	 * @param pageSize The maximum number of taxa returned (can be null for all matching taxa)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a Pager of TaxonBase instances
	 */
	public Pager<TaxonBase> findTaxaByName(Class<? extends TaxonBase> clazz, String uninomial, String infragenericEpithet, String specificEpithet, String infraspecificEpithet, Rank rank, Integer pageSize, Integer pageNumber);

	/**
	 * Returns a list of IdentifiableEntity instances (in particular, TaxonNameBase and TaxonBase instances)
	 * that match the properties specified in the configurator.
	 * @param configurator
	 * @return
	 */
	public Pager<IdentifiableEntity> findTaxaAndNames(ITaxonServiceConfigurator configurator);
	
	/**
	 * Returns a map that holds uuid, titleCache pairs of all accepted taxa for a given taxonomic tree
	 * 
	 * @param taxonomicTree
	 * 			the taxonomic tree to look for taxa in
	 * @return 
	 * 			a <code>Map</code> containing uuid and titleCache of accepted taxa
	 */
	public List<UuidAndTitleCache> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByTaxonomicTree(TaxonomicTree taxonomicTree);
	/**
	 * Returns a map that holds Taxon, titleCache pairs of all accepted taxa for a given taxonomic tree
	 * 
	 * @param taxonomicTree
	 * 			the taxonomic tree in which the taxon is looked for
	 * @param taxon
	 * 			the taxon to look for 
	 * @return 
	 * 			a <code>Map</code> containing taxon and media uri
	 */
	public Map<UUID, List<MediaRepresentation>> getAllMediaForChildNodes(Taxon taxon, TaxonomicTree taxTree, List<String> propertyPaths,  int size, int height, int widthOrDuration, String[] mimeTypes);

}
