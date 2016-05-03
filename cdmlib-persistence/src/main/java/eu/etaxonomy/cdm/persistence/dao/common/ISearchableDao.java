/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface ISearchableDao<T extends CdmBase> {

	/**
	 * Returns a count of T instances where entities match a given queryString (as interpreted by the Lucene QueryParser)
	 *
	 * @param clazz filter the results by class (or pass null to count all entities of type T)
	 * @param queryString
	 * @return a count of the matching entities
	 * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
	 */
	public int count(Class<? extends T> clazz, String queryString);

	/**
	 * Returns a List of T instances where the default field matches the String queryString (as interpreted by the Lucene QueryParser)
	 *
	 * @param clazz filter the results by class (or pass null to return all entities of type T)
	 * @param queryString
	 * @param pageSize The maximum number of entities returned (can be null for all matching entities)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @param propertyPaths properties to be initialized
	 * @return a List T instances
	 * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
	 */
	public List<T> search(Class<? extends T> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Suggest a query that will return hits based upon an existing lucene query string (that is presumably misspelt and returns no hits)
	 * Used to implement "did you mean?"-type functionality using the lucene spellchecker.
	 *
	 * @param string Query string to check
	 * @return a suggested query string that will return hits or null if no such alternative spelling can be found.
	 */
	public String suggestQuery(String string);

	/**
	 * Removes all entities of type T from the index
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
