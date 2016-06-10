/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import org.hibernate.criterion.Criterion;

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IIdentifiableDao <T extends IdentifiableEntity> extends IAnnotatableDao<T>, ITitledDao<T>, ISearchableDao<T>{

	/**
	 * Return an object by LSID. NOTE: Because of the fact that LSIDs are supposed to
	 * be globally resolvable, this method behaves in a different way to other methods
	 *
	 * In short, it attempts to find an object of type T in the current view using the LSID passed. If the LSID passed has a
	 * revision part, then this will be used in the query, but if not, then it is expected that the request is for the 'current'
	 * version of the object and the revision part will not be used as a matching criteria in the query.
	 *
	 * If the object does not appear in the current view (i.e. it has been deleted), then this method will search the history
	 * tables for a match, again using the revision if it exists, but ignoring it if not.
	 *
	 *  @param lsid a LifeScience Identifier identifying the desired object
	 */
	public T find(LSID lsid);

	/**
	 * Return a count of the sources for this identifiable entity
	 *
	 * @param identifiableEntity The identifiable entity
	 * @return a count of OriginalSource instances
	 */
	public int countSources(T identifiableEntity);

	/**
	 * Return a List of the sources for this identifiable entity
	 *
	 * @param identifiableEntity The identifiable entity
	 * @param pageSize The maximum number of sources returned (can be null for all sources)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return a List of OriginalSource instances
	 */
	public List<IdentifiableSource> getSources(T identifiableEntity, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

	/**
	 * Return a count of the rights for this identifiable entity
	 *
	 * @param identifiableEntity The identifiable entity
	 * @return a count of Rights instances
	 */
    public int countRights(T identifiableEntity);


	/**
	 * Return a List of the rights for this identifiable entity
	 *
	 * @param identifiableEntity The identifiable entity
	 * @param pageSize The maximum number of rights returned (can be null for all rights)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @return a List of Rights instances
	 */
	public List<Rights> getRights(T identifiableEntity, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

	// TODO Migrated from IOriginalSourceDao
	public List<T> findOriginalSourceByIdInSource(String idInSource, String idNamespace);

	/**
	 * Return a list of all uuids mapped to titleCache in the convenient <code>UuidAndTitleCache</code> object.
	 * Retrieving this list is considered to be significantly faster than initializing the fully fledged buiseness
	 * objects. To be used in cases where you want to present large amount of data and provide details after
	 * a selection has been made.
	 *
	 * @return a list of <code>UuidAndTitleCache</code> instances
	 */
	public List<UuidAndTitleCache<T>> getUuidAndTitleCache(Integer limit, String pattern);
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
	 * Return a List of objects matching the given query string, optionally filtered by class, optionally with a particular MatchMode
	 *
	 * @param clazz filter by class - can be null to include all instances of type T
	 * @param queryString the query string to filter by
	 * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
	 * @param criteria extra restrictions to apply
	 * @param pageSize The maximum number of rights returned (can be null for all rights)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @return a List of instances of type T matching the queryString
	 */
	public List<T> findByTitle(Class<? extends T> clazz, String queryString,MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);



	/**
	 * TODO
	 * @param clazz
	 * @param queryString
	 * @param pageSize
	 * @param pageNumber
	 * @param orderHints
	 * @param matchMode
	 * @return
	 */
	public List<T> findTitleCache(Class<? extends T> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, MatchMode matchMode);

	 /**
	 * Return a List of objects matching the given query string, optionally filtered by class, optionally with a particular MatchMode
	 *
	 * @param clazz filter by class - can be null to include all instances of type T
	 * @param queryString the query string to filter by
	 * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
	 * @param criteria extra restrictions to apply
	 * @param pageSize The maximum number of rights returned (can be null for all rights)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @return a List of instances of type T matching the queryString
	 */
	public List<T> findByReferenceTitle(Class<? extends T> clazz, String queryString,MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Return a count of objects matching the given query string in the titleCache, optionally filtered by class, optionally with a particular MatchMode
	 *
	 * @param clazz filter by class - can be null to include all instances of type T
	 * @param queryString the query string to filter by
	 * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
	 * @param criteria extra restrictions to apply
	 * @return a count of instances of type T matching the queryString
	 */
	public int countByTitle(Class<? extends T> clazz, String queryString, MatchMode matchmode, List<Criterion> criteria);

	/**
	 * Return a count of objects matching the given query string in the title, optionally filtered by class, optionally with a particular MatchMode
	 *
	 * @param clazz filter by class - can be null to include all instances of type T
	 * @param queryString the query string to filter by
	 * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
	 * @param criteria extra restrictions to apply
	 * @return a count of instances of type T matching the queryString
	 */
	public int countByReferenceTitle(Class<? extends T> clazz, String queryString, MatchMode matchmode, List<Criterion> criteria);

	/**
	 * Return a count of distinct titleCache Strings for a given {@link IdentifiableEntity}, optionally filtered by class, optionally with a particular MatchMode
	 *
	 * @param clazz filter by class - can be null to include all instances of type T
	 * @param queryString the query string to filter by
	 * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
	 * @return a count of instances of type T matching the queryString
	 */
	public abstract Long countTitleCache(Class<? extends T> clazz, String queryString, MatchMode matchMode);


	/**
	 * Return a count of entities having an {@link Identifier} that matches the given parameters.
	 * @param clazz the entities class
	 * @param identifier the identifier string
	 * @param identifierType the identifier type
	 * @param matchmode
	 * @see #findByIdentifier
	 * @return
	 */
	public <S extends T> int countByIdentifier(Class<S> clazz, String identifier, DefinedTerm identifierType, MatchMode matchmode);

    /**
     * Returns a tuple including the identifier type, the identifier string,
     * and if includeEntity is <code>false</code> the CDM entities uuid, and titleCache,
     * otherwise the CDM entity itself
     * @param clazz the identifiable entity subclass, may be null
     * @param identifier the identifier as {@link String}
     * @param identifierType the identifier type, maybe null
     * @param matchmode
     * @param includeCdmEntity
     * @param pageSize
     * @param pageNumber
     * @param propertyPaths
     * @see #countByIdentifier(Class, String, DefinedTerm, MatchMode)
     * @return
     */
    public <S extends T> List<Object[]> findByIdentifier(Class<S> clazz, String identifier, DefinedTerm identifierType, MatchMode matchmode, boolean includeCdmEntity, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

}