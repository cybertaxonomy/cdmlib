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
import java.util.Set;
import java.util.UUID;

import org.hibernate.LockOptions;
import org.hibernate.Session;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.persistence.dto.MergeResult;
import eu.etaxonomy.cdm.persistence.query.Grouping;
import eu.etaxonomy.cdm.persistence.query.OrderHint;


/**
 * @author a.mueller
 *
 */
/**
 * @author a.kohlbecker
 \* @since 23.03.2009
 *
 * @param <T>
 */
public interface IService<T extends ICdmBase>{

    // FIXME what does this method do?
    public void clear();

    /**
     * Obtain the specified lock mode on the given object t
     * <BR>
     * NOTE: with hibernate 4 we changed parameter lockMode to lockOptions. LockOptions can be created from LockMode.
     */
    public void lock(T t, LockOptions lockOptions);

    /**
     * Refreshes a given object t using the specified lockmode
     *
     * All bean properties given in the <code>propertyPaths</code> parameter are recursively initialized.
     * <p>
     * For detailed description and examples <b>please refer to:</b>
     * {@link IBeanInitializer#initialize(Object, List)}
     *
     * NOTE: in the case of lockmodes that hit the database (e.g. LockMode.READ), you will need to re-initialize
     * child propertiesto avoid a HibernateLazyInitializationException (even if the properties of the child
     * were initialized prior to the refresh).
     *
     * NOTE: with hibernate 4 we changed parameter lockMode to lockOptions. LockOptions can be created from LockMode.
     *
     * @param t
     * @param lockOptions
     */
    public void refresh(T t, LockOptions lockOptions, List<String> propertyPaths);

    /**
     * Returns a count of all entities of type <T>  optionally restricted
     * to objects belonging to a class that that extends <T>
     *
     * @param clazz the class of entities to be counted (can be null to count all entities of type <T>)
     * @return a count of entities
     */
    public int count(Class<? extends T> clazz);

    /**
     * Delete an existing persistent object
     *
     * @param persistentObject the object to be deleted
     * @return the unique identifier of the deleted entity
     * @return deleteResult
     */
    public DeleteResult delete(UUID persistentObjectUUID) ;



    /**
     * Returns true if an entity of type <T> with a unique identifier matching the
     * identifier supplied exists in the database, or false if no such entity can be
     * found.
     * @param uuid the unique identifier of the entity required
     * @return an entity of type <T> matching the uuid, or null if that entity does not exist
     */
    public boolean exists(UUID uuid);

    /**
     * Return a list of persisted entities that match the unique identifier
     * set supplied as an argument
     *
     * @param uuidSet the set of unique identifiers of the entities required
     * @return a list of entities of type <T>
     */
    public List<T> find(Set<UUID> uuidSet);

    /**
     * Return a persisted entity that matches the unique identifier
     * supplied as an argument, or null if the entity does not exist
     *
     * @param uuid the unique identifier of the entity required
     * @return an entity of type <T>, or null if the entity does not exist or uuid is <code>null</code>
     */
    public T find(UUID uuid);



	/**
	 * Return a persisted entity that matches the unique identifier
     * supplied as an argument, or null if the entity does not exist.
     * <p>
     * The difference between this method and {@link #find(UUID) find} is
     * that this method makes the hibernate read query with the
     * {@link org.hibernate.FlushMode FlushMode} for the session set to 'MANUAL'
     * <p>
     * <b>WARNING:</b>This method should <em>ONLY</em> be used when it is absolutely
     * necessary and safe to ensure that the hibernate session is not flushed before a read
     * query. A use case for this is the {@link eu.etaxonomy.cdm.api.cache.CdmCacher CdmCacher},
     * (ticket #4276) where a call to {@link eu.etaxonomy.cdm.api.cache.CdmCacher#load(UUID) load}
     * the CDM Entity using the standard {@link #find(UUID) find} method results in recursion
     * due to the fact that the {@link #find(UUID) find} method triggers a hibernate session
     * flush which eventually could call {@link eu.etaxonomy.cdm.model.name.NonViralName#getNameCache getNameCache},
	 * which in turn (in the event that name cache is null) eventually calls the
	 * {@link eu.etaxonomy.cdm.api.cache.CdmCacher#load(UUID uuid) load} again.
	 * Apart from these kind of exceptional circumstances, the standard {@link #find(UUID) find}
	 * method should always be used to ensure that the persistence layer is always in sync with the
	 * underlying database.
	 *
	 * @param uuid
	 * @return an entity of type <T>, or null if the entity does not exist or uuid is <code>null</code>
	 */
	public T findWithoutFlush(UUID uuid);

    /**
     * Return a persisted entity that matches the database identifier
     * supplied as an argument, or null if the entity does not exist
     *
     * @param id the database identifier of the entity required
     * @return an entity of type <T>, or null if the entity does not exist
     */
    public T find(int id);

    /**
     * Returns a <code>List</code> of persisted entities that match the database identifiers.
     * Returns an empty list if no identifier matches.
     *
     * @param idSet
     * @return
     * @deprecated use {@link #loadByIds(Set, List)} instead
     */
    @Deprecated
    public List<T> findById(Set<Integer> idSet);  //can't be called find(Set<Integer>) as this conflicts with find(Set<UUID)


    // FIXME should we expose this method?
    public Session getSession();

    /**
     * Returns a sublist of objects matching the grouping projections supplied using the groups parameter
     *
     * It would be nice to be able to return a pager, but for the moment hibernate doesn't
     * seem to support this (HHH-3238 - impossible to get the rowcount for a criteria that has projections)
     *
     * @param clazz Restrict the query to objects of a certain class, or null for all objects of type T or subclasses
     * @param limit the maximum number of entities returned (can be null to return
     *            all entities)
     * @param start The (0-based) offset from the start of the recordset (can be null, equivalent of starting at the beginning of the recordset)
     * @param groups The grouping objects representing a projection, plus an optional ordering on that projected property
     * @param propertyPaths paths initialized on the returned objects - only applied to the objects returned from the first grouping
     * @return a list of arrays of objects, each matching the grouping objects supplied in the parameters.
     */
    public List<Object[]> group(Class<? extends T> clazz,Integer limit, Integer start, List<Grouping> groups, List<String> propertyPaths);

    /**
     * Returns a list of entities of type <T> optionally restricted
     * to objects belonging to a class that that extends <T>
     *
     * @param type  The type of entities to return (can be null to count all entities of type <T>)
     * @param limit The maximum number of objects returned (can be null for all matching objects)
     * @param start The offset from the start of the result set (0 - based, can be null - equivalent of starting at the beginning of the recordset)
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPaths properties to be initialized
     * @return
     */
    //TODO refactor to public <S extends T> List<T> list(Class<S> type, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);
    public <S extends T>  List<S> list(Class<S> type, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Finds the cdm entity specified by the <code>uuid</code> parameter and
     * initializes all its *ToOne relations.
     *
     * @param uuid
     * @return the cdm entity or <code>null</code> if not object with given uuid exists or uuid is <code>null</code>
     */
    public T load(UUID uuid);

    /**
     * Return a persisted entity that matches the database identifier
     * supplied as an argument, or null if the entity does not exist
     *
     * @param id the database identifier of the entity required
     * @param propertyPaths
     * @return
     */
    public T load(int id, List<String> propertyPaths);

    /**
     * Finds the cdm entity specified by the <code>uuid</code> parameter and
     * recursively initializes all bean properties given in the
     * <code>propertyPaths</code> parameter.
     * <p>
     * For detailed description and examples <b>please refer to:</b>
     * {@link IBeanInitializer#initialize(Object, List)}
     *
     * @param uuid
     * @return the cdm entity or <code>null</code> if not object with given uuid exists or uuid is <code>null</code>
     */
    public T load(UUID uuid, List<String> propertyPaths);


    /**
     * Finds the cdm entities specified by the <code>uuids</code>,
     * recursively initializes all bean properties given in the
     * <code>propertyPaths</code> parameter and returns the initialised
     * entity list;
     * <p>
     * For detailed description and examples <b>please refer to:</b>
     * {@link IBeanInitializer#initialize(Object, List)}
     * @param uuids
     * @param propertyPaths
     * @return
     */
    public List<T> load(List<UUID> uuids, List<String> propertyPaths);

    /**
     * Copy the state of the given object onto the persistent object with the same identifier.
     *
     * @param transientObject the entity to be merged
     * @return The unique identifier of the persisted entity
     */
    public T merge(T transientObject);

    /**
     * Returns a paged list of entities of type <T> optionally restricted
     * to objects belonging to a class that that extends <T>
     *
     * @param type  The type of entities to return (can be null to count all entities of type <T>)
     * @param pageSize The maximum number of objects returned (can be null for all matching objects)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based,
     *                   can be null, equivalent of starting at the beginning of the recordset)
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPaths properties to be initialized
     * @return a pager of objects of type <T>
     */
    public <S extends T> Pager<S> page(Class<S> type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Re-read the state of the given instance from the underlying database.
     *
     * Hibernate claims that it is inadvisable to use refresh in long-running-sessions.
     * I don't really see where we would get into a situation where problems as discussed
     * this forum thread would apply for our scenario
     *
     * http://forum.hibernate.org/viewtopic.php?t=974544
     *
     * @param persistentObject the object to be refreshed
     * @return the unique identifier
     */
    public UUID refresh(T persistentObject);

    public List<T> rows(String tableName, int limit, int start);

    /**
     * Save a collection containing new entities (persists the entities)
     * @param newInstances the new entities to be persisted
     * @return A Map containing the new entities, keyed using the generated UUID's
     *         of those entities
     */
    public Map<UUID,T> save(Collection<T> newInstances);

    /**
     * Save a new entity (persists the entity)
     * @param newInstance the new entity to be persisted
     * @return The new persistent entity
     */
    public T save(T newInstance);

    /**
     * Save a new entity or update the persistent state of an existing
     * transient entity that has been persisted previously
     *
     * @param transientObject the entity to be persisted
     * @return The unique identifier of the persisted entity
     */
    public UUID saveOrUpdate(T transientObject);

    /**
     * Save new entities or update the persistent state of existing
     * transient entities that have been persisted previously
     *
     * @param transientObjects the entities to be persisted
     * @return The unique identifier of the persisted entity
     */
    public Map<UUID,T> saveOrUpdate(Collection<T> transientObjects);

    /**
     * Update the persistent state of an existing transient entity
     * that has been persisted previously
     *
     * @param transientObject the entity to be persisted
     * @return The unique identifier of the persisted entity
     */
    public UUID update(T transientObject);

    /**
     * Simply calls the load method.
     * Required specifically for the editor to allow load calls which
     * can also update the session cache.
     *
     * @param uuid
     * @return
     */
    public T loadWithUpdate(UUID uuid);

    /**
     * Method that lists the objects matching the example provided.
     * The includeProperties property is used to specify which properties of the example are used.
     *
     * If includeProperties is null or empty, then all literal properties are used (restrictions are
     * applied as in the Hibernate Query-By-Example API call Example.create(object)).
     *
     * If includeProperties is not empty then only literal properties that are named in the set are used to
     * create restrictions, *PLUS* any *ToOne related entities. Related entities are matched on ID, not by
     * their internal literal values (e.g. the call is criteria.add(Restrictions.eq(property,relatedObject)), not
     * criteria.createCriteria(property).add(Example.create(relatedObject)))
     *
     * @param example
     * @param includeProperties
     * @param limit the maximum number of entities returned (can be null to return
     *            all entities)
     * @param start The (0-based) offset from the start of the recordset
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     * @param propertyPaths paths initialized on the returned objects - only applied to the objects returned from the first grouping
     * @return a list of matching objects
     */
    public List<T> list(T example, Set<String> includeProperties, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

	public DeleteResult delete(T persistentObject);

    /**
     * Deletes a collection of persistent objects correponding to the
     * given list of uuids. The result will have status as ok if even one
     * of the deletes is successful, else error.
     *
     * @param persistentObjectUUIDs uuids of persistent objects to delete
     * @return DeleteResult object
     */
    public DeleteResult delete(Collection<UUID> persistentObjectUUIDs);

    /**
     * Merges a list of detached objects and returns the new
     * list of merged objects
     *
     * @param detachedObjects
     * @return a list of merged objects
     */
    public List<T> merge(List<T> detachedObjects);

    /**
     * Loads a batch of entities referenced by their ids.
     *
     * @param idSet
     * @param propertyPaths
     * @return
     */
    List<T> loadByIds(List<Integer> idSet, List<String> propertyPaths);

    /**
     * This method allows for the possibility of returning the input transient
     * entities instead of the merged persistent entity
     *
     * WARNING : This method should never be used when the objective of the merge
     * is to attach to an existing session which is the standard use case.
     * This method should only be used in the
     * case of an external call which does not use hibernate sessions and is
     * only interested in the entity as a POJO. Apart from the session information
     * the only other difference between the transient and persisted object is in the case
     * of new objects (id=0) where hibernate sets the id after commit. This id is copied
     * over to the transient entity in {@link PostMergeEntityListener#onMerge(MergeEvent,Map)}
     * making the two objects identical and allowing the transient object to be used further
     * as a POJO
     *
     * @param detachedObjects
     * @param returnTransientEntity
     * @return
     */
    public List<MergeResult<T>> merge(List<T> detachedObjects, boolean returnTransientEntity);

    /**
     * This method allows for the possibility of returning the input transient
     * entity instead of the merged persistent entity
     *
     * WARNING : This method should never be used when the objective of the merge
     * is to attach to an existing session which is the standard use case.
     * This method should only be used in the case of an external call which does
     * not use hibernate sessions and is only interested in the entity as a POJO.
     * This method returns the root merged transient entity as well as all newly merged
     * persistent entities within the return object.
     *
     * @param newInstance
     * @param returnTransientEntity
     * @return
     */
    public MergeResult<T> merge(T newInstance, boolean returnTransientEntity);

}
