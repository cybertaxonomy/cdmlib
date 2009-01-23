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

//import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;


public interface ITaxonService extends IIdentifiableEntityService<TaxonBase>{
	
	/**
	 * FIXME candidate for harmonization? 
	 */
	public abstract TaxonBase getTaxonByUuid(UUID uuid);

	/**
	 * FIXME candidate for harmonization? 
	 * save a taxon and return its UUID
	 */
	public abstract UUID saveTaxon(TaxonBase taxon);

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
	 * Computes all Taxon instances that do not have a taxonomic parent and has at least one child.
	 * @param sec The concept reference that the taxon belongs to
	 * @return The List<Taxon> of root taxa.
	 */
	public abstract List<Taxon> getRootTaxa(ReferenceBase sec);

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
	 * Computes all relationships.
	 * @param limit
	 * @param start
	 * @return
	 */
    public abstract List<RelationshipBase> getAllRelationships(int limit, int start);

	/**
	 * Returns TaxonRelationshipType vocabulary
	 * @return
	 */
	public OrderedTermVocabulary<TaxonRelationshipType> getTaxonRelationshipTypeVocabulary();

	/** */
	public abstract List<TaxonBase> searchTaxaByName(String name, ReferenceBase sec);
		
	public Synonym makeTaxonSynonym (Taxon oldTaxon, Taxon newAcceptedTaxon, SynonymRelationshipType synonymType, ReferenceBase citation, String citationMicroReference);
	
	/**
	 * Returns the TaxonRelationships (of where relationship.type == type, if this arguement is supplied) 
	 * where the supplied taxon is relatedTo.
	 * 
	 * @param taxon The taxon that is relatedTo
	 * @param type The type of TaxonRelationship (can be null)
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a Pager of TaxonRelationship instances
	 */
	public Pager<TaxonRelationship> getRelatedTaxa(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber);
	
	/**
	 * Returns the SynonymRelationships (of where relationship.type == type, if this arguement is supplied) 
	 * where the supplied taxon is relatedTo.
	 * 
	 * @param taxon The taxon that is relatedTo
	 * @param type The type of SynonymRelationship (can be null)
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a Pager of SynonymRelationship instances
	 */
	public Pager<SynonymRelationship> getSynonyms(Taxon taxon, SynonymRelationshipType type, Integer pageSize, Integer pageNumber);
	
	/**
	 * Returns a List of TaxonBase instances (or Taxon instances, if accepted == true, or Synonym instance, if accepted == false) where the 
	 * taxonBase.name.nameCache property matches the String queryString (as interpreted by the Lucene QueryParser)
	 * 
	 * @param queryString
	 * @param accepted
	 * @param pageSize The maximum number of taxa returned (can be null for all matching taxa)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a Pager Taxon instances
	 * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
	 */
	public Pager<TaxonBase> searchTaxa(String queryString, Boolean accepted, Integer pageSize, Integer pageNumber);
	
	/**
	 * Returns a list of TaxonBase instances (or Taxon instances, if accepted == true, or Synonym instance, if accepted == false) where the
	 * taxon.name properties match the parameters passed.
	 * 
	 * @param accepted Whether the taxon is accepted (true) a synonym (false), or either (null)
	 * @param uninomial 
	 * @param infragenericEpithet
	 * @param specificEpithet
	 * @param infraspecificEpithet
	 * @param rank
	 * @param pageSize The maximum number of taxa returned (can be null for all matching taxa)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a Pager of TaxonBase instances
	 */
	public Pager<TaxonBase> findTaxaByName(Boolean accepted, String uninomial, String infragenericEpithet, String specificEpithet, String infraspecificEpithet, Rank rank, Integer pageSize, Integer pageNumber);

	/**
	 * Returns a list of IdentifiableEntity instances (in particular, TaxonNameBase and TaxonBase instances)
	 * that match the properties specified in the configurator.
	 * @param configurator
	 * @return
	 */
	public Pager<IdentifiableEntity> findTaxaAndNames(ITaxonServiceConfigurator configurator);
}
