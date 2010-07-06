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

import org.hibernate.criterion.Criterion;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

public interface IIdentifiableEntityService<T extends IdentifiableEntity> extends IAnnotatableService<T> {

	/**
	 * (Re-)generate the title caches for all objects of this concrete IdentifiableEntity class
	 */
	public void updateTitleCache();
	
	public void updateTitleCache(Class<? extends T> clazz);
	
	public void updateTitleCache(Class<? extends T> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<T> cacheStrategy);
	
	/**
	 * Finds an object with a given LSID. If the object does not currently exist in the current view, then
	 * the most recent prior version of the object will be returned, or null if an object with this identifier
	 * has never existed
	 * 
	 * @param lsid
	 * @return an object of type T or null of the object has never existed
	 */
	public T find(LSID lsid);
	
	/**
	 * Replaces all *ToMany and *ToOne references to an object (x) with another object of the same type (y)
	 * 
	 * Ignores ManyToAny and OneToAny relationships as these are typically involved with bidirectional 
	 * parent-child relations
	 * 
	 * @param x
	 * @param y
	 * @return the replacing object (y)
	 */
	public T replace(T x, T y);
	
	/**
	 * Return a Pager of sources belonging to this object
	 * 
	 * @param t The identifiable entity
	 * @param pageSize The maximum number of sources returned (can be null for all sources)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
	 * @return a Pager of OriginalSource entities
	 */
    public Pager<IdentifiableSource> getSources(T t, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
    
	/**
	 * Return a Pager of rights belonging to this object
	 * 
	 * @param t The identifiable entity
	 * @param pageSize The maximum number of rights returned (can be null for all rights)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
	 * @return a Pager of Rights entities
	 */
    public Pager<Rights> getRights(T t, Integer pageSize, Integer pageNumber, List<String> propertyPaths);
    
    public ISourceable getSourcedObjectByIdInSource(Class clazz, String idInSource, String idNamespace);
    
	/**
	 * Return a list of all uuids mapped to titleCache in the convenient <code>UuidAndTitleCache</code> object.
	 * Retrieving this list is considered to be significantly faster than initializing the fully fledged buiseness
	 * objects. To be used in cases where you want to present large amount of data and provide details after 
	 * a selection has been made.  
	 * 
	 * @return a list of <code>UuidAndTitleCache</code> instances
	 */
	public List<UuidAndTitleCache<T>> getUuidAndTitleCache(); 
	
	/**
	 * Return a Pager of objects matching the given query string, optionally filtered by class, optionally with a particular MatchMode
	 * 
	 * @param clazz filter by class - can be null to include all instances of type T
	 * @param queryString the query string to filter by
	 * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
	 * @param criteria additional criteria to filter by
	 * @param pageSize The maximum number of objects returned (can be null for all objects)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @return a paged list of instances of type T matching the queryString
	 */
    public Pager<T> findByTitle(Class<? extends T> clazz, String queryString,MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
    
    /**
	 * Returns a Paged List of IdentifiableEntity instances where the default field matches the String queryString (as interpreted by the Lucene QueryParser)
	 * 
	 * @param clazz filter the results by class (or pass null to return all IdentifiableEntity instances)
	 * @param queryString
	 * @param pageSize The maximum number of identifiable entities returned (can be null for all matching identifiable entities)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @param propertyPaths properties to be initialized
	 * @return a Pager IdentifiableEntity instances
	 * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
	 */
	public Pager<T> search(Class<? extends T> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
}
