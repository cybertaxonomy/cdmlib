/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dto.MergeResult;
import eu.etaxonomy.cdm.persistence.query.Grouping;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * An data access interface that all data access classes implement
 * @author m.doering
 * @since 02-Nov-2007 19:36:10
 */
public interface ICdmEntityDao<T extends CdmBase> {

    /**
     * @param transientObject
     * @return
     * @throws DataAccessException
     */
    public UUID saveOrUpdate(T transientObject) throws DataAccessException;

    /**
     * @param newOrManagedObject
     * @return
     * @throws DataAccessException
     */
    public T save(T newOrManagedObject) throws DataAccessException;

    public T merge(T transientObject) throws DataAccessException;

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
     * @param transientObject
     * @param returnTransientEntity
     * @return transient or persistent object depending on the value of returnTransientEntity
     * @throws DataAccessException
     */
    public MergeResult<T> merge(T transientObject, boolean returnTransientEntity) throws DataAccessException;

    /**
     * Obtains the specified LockMode on the supplied object
     *
     * @param t
     * @param lockOptions
     * @throws DataAccessException
     */
    public void lock(T t, LockOptions lockOptions) throws DataAccessException;

    /**
     * Globally replace all references to instance t1 with t2 (including
     *
     * NOTE: This replaces all non-bidirectional relationships where type T is on the
     * "owning" side of the relationship (since the "owned" objects are, in theory,
     * sub-components of the entity and this kind of global replace doesn't really make sense
     *
     * Consequently it is a good idea to either map such owned relationships with cascading
     * semantics (i.e. CascadeType.DELETE, @OneToMany(orphanRemoval=true)) allowing them to be saved,
     * updated, and deleted along with the owning entity automatically.
     *
     * @param x the object to replace, must not be null
     * @param y the object that will replace. If y is null, then x will be removed from all collections
     *          and all properties that refer to x will be replaced with null
     * @return T the replaced object
     */
    public T replace(T x, T y);

    /**
     * Refreshes the state of the supplied object using the given LockMode (e.g. use LockMode.READ
     * to bypass the second-level cache and session cache and query the database directly)
     *
     * All bean properties given in the <code>propertyPaths</code> parameter are recursively initialized.
     * <p>
     * For detailed description and examples <b>please refer to:</b>
     * {@link IBeanInitializer#initialize(Object, List)}
     *
     * @param t
     * @param lockMode
     * @param propertyPaths
     * @throws DataAccessException
     */
    public void refresh(T t, LockOptions lockOptions, List<String> propertyPaths) throws DataAccessException;

    public void clear() throws DataAccessException;

    public Session getSession() throws DataAccessException;

    public Map<UUID, T> saveAll(Collection<T> cdmObjCollection) throws DataAccessException;

    public Map<UUID, T> saveOrUpdateAll(Collection<T> cdmObjCollection);

    /**
     * @param transientObject
     * @return
     * @throws DataAccessException
     */
    public UUID update(T transientObject) throws DataAccessException;

    /**
     * @param persistentObject
     * @return
     * @throws DataAccessException
     */
    public UUID refresh(T persistentObject) throws DataAccessException;

    /**
     * @param persistentObject
     * @return
     * @throws DataAccessException
     */
    public UUID delete(T persistentObject) throws DataAccessException;

    /**
     * Returns a sublist of CdmBase instances stored in the database.
     * A maximum of 'limit' objects are returned, starting at object with index 'start'.
     * @param limit the maximum number of entities returned (can be null to return all entities)
     * @param start
     * @return
     * @throws DataAccessException
     */
    public List<T> list(Integer limit, Integer start) throws DataAccessException;

    /**
     * Returns a sublist of CdmBase instances stored in the database. A maximum
     * of 'limit' objects are returned, starting at object with index 'start'.
     *
     * @param limit
     *            the maximum number of entities returned (can be null to return
     *            all entities)
     * @param start
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @return
     * @throws DataAccessException
     */
    public List<T> list(Integer limit, Integer start, List<OrderHint> orderHints);


    /**
     * Returns a sublist of CdmBase instances stored in the database. A maximum
     * of 'limit' objects are returned, starting at object with index 'start'.
     *
     * @param type
     * @param limit
     *            the maximum number of entities returned (can be null to return
     *            all entities)
     * @param start
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @return
     * @throws DataAccessException
     */
    public <S extends T> List<S> list(Class<S> type, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Returns a sublist of CdmBase instances stored in the database. A maximum
     * of 'limit' objects are returned, starting at object with index 'start'.
     * The bean properties specified by the parameter <code>propertyPaths</code>
     * and recursively initialized for each of the entities in the resultset
     *
     * For detailed description and examples regarding
     * <code>propertyPaths</code> <b>please refer to:</b>
     * {@link IBeanInitializer#initialize(Object, List)}
     *
     * @param limit
     *            the maximum number of entities returned (can be null to return
     *            all entities)
     * @param start
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPaths
     * @return
     * @throws DataAccessException
     */
    public List<T> list(Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Returns a list of Cdm entities stored in the database filtered by the restrictions defined by
     * the <code>parameters</code> <code>propertyName</code>, value and <code>matchMode</code>
     * A maximum
     * of 'limit' objects are returned, starting at object with index 'start'.
     * The bean properties specified by the parameter <code>propertyPaths</code>
     * and recursively initialized for each of the entities in the resultset
     *
     * For detailed description and examples regarding
     * <code>propertyPaths</code> <b>please refer to:</b>
     * {@link IBeanInitializer#initialize(Object, List)}
     *
     * @param type
     *          Restrict the query to objects of a certain class, or null for
     *          all objects of type T or subclasses
     * @param restrictions
     *      This defines a filter for multiple properties represented by the map keys. Sine the keys are of the type
     *      {@link Restriction} for each property a single MatchMode is defined. Multiple alternative values
     *      can be supplied per property, that is the values per property are combined with OR. The per property
     *      restrictions are combined with AND. </br>
     *      <b>NOTE:</b> For non string type properties you must use
     *      {@link MatchMode#EXACT}. If set <code>null</code> {@link MatchMode#EXACT} will be used
     *      as default.
     * @param limit
     *         the maximum number of entities returned (can be null to return
     *         all entities)
     * @param start
     *       The list of criterion objects representing the restriction to be applied.
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPaths
     * @return
     * @throws DataAccessException
     */
    public List<T> list(Class<? extends T> type, List<Restriction<?>> restrictions, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Counts the Cdm entities matching the restrictions defined by
     * the <code>parameters</code> <code>propertyName</code>, value and <code>matchMode</code>.
     *
     * @param type
     *          Restrict the query to objects of a certain class, or null for
     *          all objects of type T or subclasses
     * @param restrictions
     *      This defines a filter for multiple properties represented by the map keys. Sine the keys are of the type
     *      {@link Restriction} for each property a single MatchMode is defined. Multiple alternative values
     *      can be supplied per property, that is the values per property are combined with OR. The per property
     *      restrictions are combined with AND. </br>
     *      <b>NOTE:</b> For non string type properties you must use
     *      {@link MatchMode#EXACT}. If set <code>null</code> {@link MatchMode#EXACT} will be used
     *      as default.
     * @param criteria
     *       The list of criterion objects representing the restriction to be applied.
     *
     * @return
     */
    public int count(Class<? extends T> type, List<Restriction<?>> restrictions);

    /**
     * Returns a sublist of CdmBase instances of type <TYPE> stored in the database.
     * A maximum of 'limit' objects are returned, starting at object with index 'start'.
     * @param limit the maximum number of entities returned (can be null to return all entities)
     * @param start
     * @return
     * @throws DataAccessException
     */
    public <S extends T> List<S> list(Class<S> type, Integer limit, Integer start) throws DataAccessException;

    /**
     * Returns a sublist of objects matching the grouping projections supplied using the groups parameter
     *
     * It would be nice to have an equivalent countGroups method, but for the moment hibernate doesn't
     * seem to support this (HHH-3238 - impossible to get the rowcount for a criteria that has projections)
     *
     * @param clazz Restrict the query to objects of a certain class, or null for all objects of type T or subclasses
     * @param limit the maximum number of entities returned (can be null to return
     *            all entities)
     * @param start The (0-based) offset from the start of the recordset
     * @param groups The grouping objects representing a projection, plus an optional ordering on that projected property
     * @param propertyPaths paths initialized on the returned objects - only applied to the objects returned from the first grouping
     * @return a list of arrays of objects, each matching the grouping objects supplied in the parameters.
     */
    public List<Object[]> group(Class<? extends T> clazz,Integer limit, Integer start, List<Grouping> groups, List<String> propertyPaths);

    /**
     * @param id
     * @return
     * @throws DataAccessException
     */
    public T findById(int id) throws DataAccessException;

    /**
     * Finds the cdm entity specified by the id parameter and
     * recursively initializes all bean properties given in the
     * <code>propertyPaths</code> parameter.
     * <p>
     * For detailed description and examples <b>please refer to:</b>
     * {@link IBeanInitializer#initialize(Object, List)}
     *
     * @param id
     * @param propertyPaths properties to be initialized
     * @return
     */
    public T load(int id, List<String> propertyPaths);

    /**
     * @param ids
     * @param propertyPaths
     * @return
     * @throws DataAccessException
     */
    public List<T> loadList(Collection<Integer> ids, List<String> propertyPaths) throws DataAccessException;

    /**
     * @param Uuid
     * @return
     * @throws DataAccessException
     */
    public T findByUuid(UUID Uuid) throws DataAccessException;

    /**
     * Method to find CDM Entity by Uuid, by making sure that the underlying
     * hibernate session is not flushed (Session.FLUSH_MODE set to MANUAL temporarily)
     * when performing the read query.
     *
     * @param Uuid
     * @return
     * @throws DataAccessException
     */
    public T findByUuidWithoutFlush(UUID uuid) throws DataAccessException;


    /**
     * @param uuids
     * @param pageSize
     * @param pageNumber
     * @param orderHints
     * @param propertyPaths
     * @return
     * @throws DataAccessException
     */
    public List<T> list(Collection<UUID> uuids, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) throws DataAccessException;

    /**
     * Finds the cdm entity specified by the <code>uuid</code> parameter and
     * initializes all its *ToOne relations.
     *
     * @param uuid
     * @return
     */
    public T load(UUID uuid);

    /**
     * Finds the cdm entity specified by the <code>uuid</code> parameter and
     * recursively initializes all bean properties given in the
     * <code>propertyPaths</code> parameter.
     * <p>
     * For detailed description and examples <b>please refer to:</b>
     * {@link IBeanInitializer#initialize(Object, List)}
     *
     * @param uuid
     * @param propertyPaths properties to be initialized
     * @return
     */
    public T load(UUID uuid, List<String> propertyPaths);

    /**
     * @param uuid
     * @return
     * @throws DataAccessException
     */
    public Boolean exists(UUID uuid) throws DataAccessException;

    public int count();

    /**
     * Returns the number of objects of type <T> - which must extend T
     * @param <T>
     * @param clazz
     * @return
     */
    public int count(Class<? extends T> clazz);

    /**
     * FIXME Should this method exist : I would expect flushing of a session to be
     * something that a DAO should hide?
     */
    public void flush();

    /**
     * Convenience method which makes it easy to discover what type of object this DAO returns at runtime
     *
     * @return
     */
    public Class<T> getType();

    /**
     * Method that counts the number of objects matching the example provided.
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
     * @return a count of matching objects
     */
    public int count(T example, Set<String> includeProperties);

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

}
