package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import eu.etaxonomy.cdm.model.common.CdmBase;

public interface ISearchableDao<T extends CdmBase> {
	
	/**
	 * Returns a count of T instances where the default field matches the String queryString (as interpreted by the Lucene QueryParser)
	 * 
	 * @param queryString
	 * @return a count of the matching entities
	 * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
	 */
	public int count(String queryString);
	
	/**
	 * Returns a List of T instances where the default field matches the String queryString (as interpreted by the Lucene QueryParser)
	 * 
	 * @param queryString
	 * @param pageSize The maximum number of entities returned (can be null for all matching entities)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List T instances
	 * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
	 */
	public List<T> search(String queryString, Integer pageSize, Integer pageNumber);
	
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
	 * Index all T entities currently in the database (useful in concert with purgeIndex() to (re-)create
	 * indexes or in the  case of corrupt indexes / mismatch between 
	 * the database and the free-text indices) 
	 */
	public void rebuildIndex();
	
	/**
	 * Calls optimize on the relevant index (useful periodically to increase response times on the free-text search)
	 */
	public void optimizeIndex();
}
