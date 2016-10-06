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
import java.util.UUID;

import org.hibernate.criterion.Criterion;

import eu.etaxonomy.cdm.api.service.config.IIdentifiableEntityServiceConfigurator;
import eu.etaxonomy.cdm.api.service.dto.IdentifiedEntityDTO;
import eu.etaxonomy.cdm.api.service.dto.MarkedEntityDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.merge.IMergable;
import eu.etaxonomy.cdm.strategy.merge.IMergeStrategy;

public interface IIdentifiableEntityService<T extends IdentifiableEntity> extends IAnnotatableService<T> {

    /**
     * (Re-)generate the title caches for all objects of this concrete IdentifiableEntity class.
     * Uses default values.
     * @see #updateTitleCache(Class, Integer, IIdentifiableEntityCacheStrategy, IProgressMonitor)
     */
    public void updateTitleCache();

    /**
     * (Re-)generate the title caches for all objects of this concrete IdentifiableEntity class
     *
     * @param clazz class of objects to be updated
     * @param stepSize number of objects loaded per step. If <code>null</code> use default.
     * @param cacheStrategy cachestrategy used for title cache. If <code>null</code> use default.
     * @param monitor progress monitor. If <code>null</code> use default.
     */
    public void updateTitleCache(Class<? extends T> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<T> cacheStrategy, IProgressMonitor monitor);

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
     * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager of OriginalSource entities
     */
    public Pager<IdentifiableSource> getSources(T t, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Return a Pager of rights belonging to this object
     *
     * @param t The identifiable entity
     * @param pageSize The maximum number of rights returned (can be null for all rights)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
     * @return a Pager of Rights entities
     */
    public Pager<Rights> getRights(T t, Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Return a list of all uuids mapped to titleCache in the convenient <code>UuidAndTitleCache</code> object.
     * Retrieving this list is considered to be significantly faster than initializing the fully fledged business
     * objects. To be used in cases where you want to present large amount of data and provide details after
     * a selection has been made.
     *
     * @return a list of <code>UuidAndTitleCache</code> instances
     *
     * @see #getUuidAndTitleCache(Class, Integer, String)
     */
    public List<UuidAndTitleCache<T>> getUuidAndTitleCache(Integer limit, String pattern);

    /**
     * Returns the titleCache for a given object defined by uuid.
     * @param uuid the uuid of the requested object.
     * @param refresh if false the value as stored in the DB is returned,
     *      otherwise it is recomputed by loading the object and calling the formatter.
     * @return the titleCache of the requested object
     */
    public String getTitleCache(UUID uuid, boolean refresh);

    /**
     * Like {@link #getUuidAndTitleCache(Integer, String)} but searching only on a subclass
     * of the type handled by the DAO.
     *
     * @param clazz the (sub)class
     * @param limit max number of results
     * @param pattern search pattern

     * @see #getUuidAndTitleCache(Integer, String)
     */
    public <S extends T> List<UuidAndTitleCache<S>> getUuidAndTitleCache(Class<S> clazz, Integer limit, String pattern);

    /**
     * Return a Pager of objects matching the given query string, optionally filtered by class, optionally with a particular MatchMode
     *
     * @param clazz filter by class - can be null to include all instances of type T
     * @param queryString the query string to filter by
     * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
     * @param criteria additional criteria to filter by
     * @param pageSize The maximum number of objects returned (can be null for all objects)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @return a paged list of instances of type T matching the queryString
     */
    public Pager<T> findByTitle(Class<? extends T> clazz, String queryString,MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);


    /**
     * Return a Pager of objects matching the given query string, optionally filtered by class,
     * optionally with a particular MatchMode
     *
     * @return a paged list of instances of type T matching the queryString
     */
    public Pager<T> findByTitle(IIdentifiableEntityServiceConfigurator<T> configurator);


    /**
     * Return an Integer of how many objects matching the given query string, optionally filtered by class, optionally with a particular MatchMode
     *
     * @param clazz filter by class - can be null to include all instances of type T
     * @param queryString the query string to filter by
     * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
     * @param criteria additional criteria to filter by
     *
     * @return
     */
    public Integer countByTitle(Class<? extends T> clazz, String queryString,MatchMode matchmode, List<Criterion> criteria);

    /**
     * Return an Integer of how many objects matching the given query string, optionally filtered by class, optionally with a particular MatchMode
     *
     * @param configurator an {@link IIdentifiableEntityServiceConfigurator} object
     *
     * @return
     */
    public Integer countByTitle(IIdentifiableEntityServiceConfigurator<T> configurator);


    /**
     * Return a List of objects matching the given query string, optionally filtered by class, optionally with a particular MatchMode
     *
     * @param clazz filter by class - can be null to include all instances of type T
     * @param queryString the query string to filter by
     * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
     * @param criteria additional criteria to filter by
     * @param pageSize The maximum number of objects returned (can be null for all objects)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @return a list of instances of type T matching the queryString
     */
    public List<T> listByTitle(Class<? extends T> clazz, String queryString,MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Return a List of objects matching the given query string, optionally filtered by class, optionally with a particular MatchMode
     *
     * @param clazz filter by class - can be null to include all instances of type T
     * @param queryString the query string to filter by
     * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
     * @param criteria additional criteria to filter by
     * @param pageSize The maximum number of objects returned (can be null for all objects)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @return a list of instances of type T matching the queryString
     */
    public List<T> listByReferenceTitle(Class<? extends T> clazz, String queryString,MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

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


    /**
     * This method tries to deduplicate all objects of a certain class by first trying to find matchabel objects and
     * merging them in a second step. For performance reasons implementing classes must not guarantee that ALL
     * matching object pairs are found but only a subset. But it must guarantee that only matching objects are merged.
     *<BR> Matching is defined by the given matching strategy or if no matching strategy is given the default matching
     *strategy is used.
     *<BR>clazz must implement {@link IMatchable} and {@link IMergable} otherwise no deduplication is performed.
     *<BR>The current implementation in IdentifiableServiceBase tries to match and merge all objects with an identical non
     *empty titleCache.
     * @param clazz
     * @param matchStrategy
     * @param mergeStrategy
     * @return the number of merges performed during deduplication
     */
    public int deduplicate(Class<? extends T> clazz, IMatchStrategy matchStrategy, IMergeStrategy mergeStrategy);


    /**
     * Return a Pager of objects with distinct titleCache strings filtered by the given query string, optionally filtered by class, optionally with a particular MatchMode
     * @param clazz
     * @param queryString
     * @param pageSize
     * @param pageNumber
     * @param orderHints
     * @param matchMode
     * @return
     */
    public Pager<T> findTitleCache(Class<? extends T> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, MatchMode matchMode);


    /**
     * Returns an Sourceable object according the
     * @param clazz
     * @param idInSource
     * @param idNamespace
     * @return
     */
    //TODO shouldn't we move this to CommonService or to a new SourceService ?
    //TODO should this return a List ?
    public ISourceable getSourcedObjectByIdInSource(Class clazz, String idInSource, String idNamespace);

    /**
     * Returns a Pager for {@link IdentifiedEntityDTO DTOs} that hold the identifier including type, title and uuid
     * and the according CDM Object information (uuid, title and the object itself (optional)).
     *
     * @param clazz the identifiable entity subclass, may be null
     * @param identifier the identifier as {@link String}
     * @param identifierType the identifier type, maybe null
     * @param matchmode
     * @param includeCdmEntity if true the CDM entity is also returned (this may slow down performance for large datasets)
     * @param pageSize
     * @param pageNumber
     * @param propertyPaths
     * @return all {@link IdentifiableEntity identifiable entities} which have the according
     * identifier attached
     */
    public <S extends T> Pager<IdentifiedEntityDTO<S>> findByIdentifier(
            Class<S> clazz, String identifier, DefinedTerm identifierType,
            MatchMode matchmode, boolean includeCdmEntity,
            Integer pageSize, Integer pageNumber, List<String> propertyPaths);

    /**
     * Returns a Pager for {@link MarkedEntityDTO DTOs} that hold the marker including type, title and uuid
     * and the according CDM object information (uuid, title and the object itself (optional)).
     *
     * @param clazz
     * @param markerType
     * @param markerValue
     * @param includeEntity
     * @param pageSize
     * @param pageNumber
     * @param propertyPaths
     * @return all {@link IdentifiableEntity identifiable entities} which have the according
     * marker with the given flag value attached
     */
    public <S extends T> Pager<MarkedEntityDTO<S>> findByMarker(
            Class<S> clazz, MarkerType markerType, Boolean markerValue,
            boolean includeEntity, Integer pageSize,
            Integer pageNumber, List<String> propertyPaths);
}
