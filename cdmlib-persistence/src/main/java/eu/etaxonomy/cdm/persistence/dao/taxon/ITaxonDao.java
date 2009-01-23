/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.taxon;

import java.util.List;

import org.hibernate.criterion.Criterion;

import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITitledDao;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;

/**
 * @author a.mueller
 *
 */
public interface ITaxonDao extends IIdentifiableDao<TaxonBase>, ITitledDao<TaxonBase> {
	
	/**
	 * Returns a count of TaxonBase instances (or Taxon instances, if accepted == true, or Synonym instance, if accepted == false) 
	 * where the taxonBase.name.nameCache property matches the String queryString
	 * 
	 * @param queryString
	 * @param accepted
	 * @param sec
	 * @return a count of the matching taxa
	 */
	public int countTaxaByName(String queryString, Boolean accepted, ReferenceBase sec);

	/** 
	 * Returns a list of TaxonBase instances where the taxon.titleCache property matches the name parameter, 
	 * and taxon.sec matches the sec parameter.
	 * @param name
	 * @param sec
	 * @return
	 */
	public List<TaxonBase> getTaxaByName(String name, ReferenceBase sec);
	
	/** 
	 * Returns a list of TaxonBase instances (or Taxon instances, if accepted == true, or Synonym instance, if accepted == false) 
	 * where the taxonBase.name.nameCache property matches the String queryString, and taxon.sec matches the sec parameter.
	 * @param name
	 * @param sec
	 * @return
	 */
	public List<TaxonBase> getTaxaByName(String queryString, Boolean accepted, ReferenceBase sec);

	/**
	 * Computes all Taxon instances that do not have a taxonomic parent and has at least one child.
	 * @return The List<Taxon> of root taxa.
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec);

	
	/**
	 * Computes all Taxon instances that do not have a taxonomic parent.
	 * @param sec The concept reference that the taxon belongs to
	 * @param cdmFetch not used yet !! TODO
	 * @param onlyWithChildren if true only taxa are returned that have taxonomic children. <Br>Default: true.
	 * @param withMisaplications if false only taxa are returned that have no isMisappliedNameFor relationship. 
	 * <Br>Default: true.
	 * @return The List<Taxon> of root taxa.
	 */
	public List<Taxon> getRootTaxa(ReferenceBase sec, CdmFetch cdmFetch, Boolean onlyWithChildren, Boolean withMisapplications);
	
	
	/**
	 * TODO necessary? 
	 * @param pagesize max maximum number of returned taxa
	 * @param page page to start, with 0 being first page 
	 * @return
	 */
	public List<TaxonBase> getAllTaxonBases(Integer pagesize, Integer page);
	
	
	/**
	 * @param limit
	 * @param start 
	 * @return
	 */
	public List<Taxon> getAllTaxa(Integer limit, Integer start);

	/**
	 * @param limit
	 * @param start 
	 * @return
	 */
	public List<Synonym> getAllSynonyms(Integer limit, Integer start);

	/**
	 * @param limit
	 * @param start 
	 * @return
	 */
	//public List<TaxonRelationship> getAllTaxonRelationships(Integer limit, Integer start);
	
	/**
	 * @param limit
	 * @param start 
	 * @return
	 */
	//public List<SynonymRelationship> getAllSynonymRelationships(Integer limit, Integer start);

	public List<RelationshipBase> getAllRelationships(Integer limit, Integer start); 

	/**
	 * Find taxa by searching for @{link NameBase}
	 * @param queryString
	 * @param matchMode
	 * @param page
	 * @param pagesize
	 * @param onlyAcccepted
	 * @return
	 */
	public List<Taxon> findByName(String queryString, ITitledDao.MATCH_MODE matchMode, int page, int pagesize, boolean onlyAcccepted);
	
	/**
	 * @param queryString
	 * @param matchMode
	 * @param onlyAcccepted
	 * @return
	 */
	public int countMatchesByName(String queryString, ITitledDao.MATCH_MODE matchMode, boolean onlyAcccepted);
	
	/**
	 * @param queryString
	 * @param matchMode
	 * @param onlyAcccepted
	 * @param criteria
	 * @return
	 */
	public int countMatchesByName(String queryString, ITitledDao.MATCH_MODE matchMode, boolean onlyAcccepted, List<Criterion> criteria);
	
	/**
	 * Returns a count of the TaxonRelationships (of where relationship.type == type,
	 *  if this arguement is supplied) where the supplied taxon is relatedFrom.
	 * 
	 * @param taxon The taxon that is relatedFrom
	 * @param type The type of TaxonRelationship (can be null)
	 * @return the number of TaxonRelationship instances
	 */
	public int countRelatedTaxa(Taxon taxon, TaxonRelationshipType type);
	
	/**
	 * Returns the TaxonRelationships (of where relationship.type == type, if this arguement is supplied) 
	 * where the supplied taxon is relatedTo.
	 * 
	 * @param taxon The taxon that is relatedTo
	 * @param type The type of TaxonRelationship (can be null)
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of TaxonRelationship instances
	 */
	public List<TaxonRelationship> getRelatedTaxa(Taxon taxon, TaxonRelationshipType type, Integer pageSize, Integer pageNumber);
	
	/**
	 * Returns a count of the SynonymRelationships (of where relationship.type == type,
	 *  if this arguement is supplied) where the supplied taxon is relatedTo.
	 * 
	 * @param taxon The taxon that is relatedTo
	 * @param type The type of SynonymRelationship (can be null)
	 * @return the number of SynonymRelationship instances
	 */
	public int countSynonyms(Taxon taxon, SynonymRelationshipType type);
	
	/**
	 * Returns the SynonymRelationships (of where relationship.type == type, if this arguement is supplied) 
	 * where the supplied taxon is relatedTo.
	 * 
	 * @param taxon The taxon that is relatedTo
	 * @param type The type of SynonymRelationship (can be null)
	 * @param pageSize The maximum number of relationships returned (can be null for all relationships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of SynonymRelationship instances
	 */
	public List<SynonymRelationship> getSynonyms(Taxon taxon, SynonymRelationshipType type, Integer pageSize, Integer pageNumber);
	
	/**
	 * Returns a count of TaxonBase instances (or Taxon instances, if accepted == true, or Synonym instance, if accepted == false) where the 
	 * taxonBase.name.nameCache property matches the String queryString (as interpreted by the Lucene QueryParser)
	 * 
	 * @param queryString
	 * @param accepted
	 * @return a count of the matching taxa
	 * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
	 */
	public int countTaxa(String queryString, Boolean accepted);
	
	/**
	 * Returns a List of TaxonBase instances (or Taxon instances, if accepted == true, or Synonym instance, if accepted == false) where the 
	 * taxonBase.name.nameCache property matches the String queryString (as interpreted by the Lucene QueryParser)
	 * 
	 * @param queryString
	 * @param accepted
	 * @param pageSize The maximum number of taxa returned (can be null for all matching taxa)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List Taxon instances
	 * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
	 */
	public List<TaxonBase> searchTaxa(String queryString, Boolean accepted, Integer pageSize, Integer pageNumber);
	
	/**
	 * Returns a count of TaxonBase instances (or Taxon instances, if accepted == true, or Synonym instance, if accepted == false) where the
	 * taxon.name properties match the parameters passed.
	 * 
	 * @param accepted
	 * @param uninomial
	 * @param infragenericEpithet
	 * @param specificEpithet
	 * @param infraspecificEpithet
	 * @param rank
	 * @return a count of TaxonBase instances
	 */
	public int countTaxaByName(Boolean accepted, String uninomial, String infragenericEpithet,String specificEpithet, String infraspecificEpithet, Rank rank);
	
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
	 * @return a list of TaxonBase instances
	 */
	public List<TaxonBase> findTaxaByName(Boolean accepted, String uninomial, String infragenericEpithet, String specificEpithet, String infraspecificEpithet, Rank rank, Integer pageSize, Integer pageNumber);

	/**
	 * Suggest a query that will return hits based upon an existing lucene query string (that is presumably misspelt and returns no hits)
	 * Used to implement "did you mean?"-type functionality using the lucene spellchecker.
	 * 
	 * @param string Query string to check
	 * @return a suggested query string that will return hits or null if no such alternative spelling can be found.
	 */
	public String suggestQuery(String string);
	
	/**
	 * Removes all TaxonBase entities from the index
	 */
	public void purgeIndex();

	/**
	 * Index all TaxonBase entities currenly in the database (useful in concert with purgeIndex() to (re-)create
	 * indexes or in the  case of corrupt indexes / mismatch between 
	 * the database and the free-text indices) 
	 */
	public void rebuildIndex();
	
	/**
	 * Calls optimize on the relevant index (useful periodically to increase response times on the free-text search)
	 */
	public void optimizeIndex();
}
