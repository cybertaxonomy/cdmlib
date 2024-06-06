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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData;
import eu.etaxonomy.cdm.persistence.dto.ReferencingObjectDto;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.match.MatchException;
import eu.etaxonomy.cdm.strategy.merge.IMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.MergeException;

public interface ICdmGenericDao {

	public UUID saveOrUpdate(CdmBase transientObject) throws DataAccessException;

	public <S extends CdmBase> CdmBase save(S newInstance) throws DataAccessException;

	public UUID update(CdmBase transientObject) throws DataAccessException;

	public UUID delete(CdmBase persistentObject) throws DataAccessException;

	public void saveMetaData(CdmMetaData cdmMetaData);

	public List<CdmMetaData> getMetaData();

	/**
	 * Returns a CdmBase object of class <code>clazz</code> that has a property with name
	 * <code>propertyName</code> that references the CdmBase object <code>referencedCdmBase</code>.
	 * @param clazz
	 * @param propertyName
	 * @param value
	 * @return
	 */
	public List<CdmBase> getCdmBasesByFieldAndClass(Class<? extends CdmBase> clazz, String propertyName, CdmBase referencedCdmBase, Integer limit);

	/**
	 * Returns ...
	 * @param thisClass
	 * @param otherClazz
	 * @param propertyName
	 * @param referencedCdmBase
	 * @return
	 */
	public List<CdmBase> getCdmBasesWithItemInCollection(Class<?> itemClass,
	        Class<?> clazz, String propertyName, CdmBase item, Integer limit);

	public List<ReferencingObjectDto> getCdmBasesWithItemInCollectionDto(Class<?> itemClass,
            Class<? extends CdmBase> clazz, String propertyName, CdmBase item, Integer limit);

	/**
	 * Returns all classes that are persisted via the persisting framework.
	 * E.g. in hibernate these are all classes registered in the session factory
	 * (via e.g. hibernate.cfg.xml)
	 * <BR>
	 * @param includeAbstractClasses if <code>false</code> the abstract classes
	 * will not be in the result set.
	 * @return
	 */
	public Set<Class<? extends CdmBase>> getAllPersistedClasses(boolean includeAbstractClasses);

	/**
	 * Returns all CdmBase objects that reference the referencedCdmBase.
	 * For example, if referencedCdmBase is an agent it may return all taxon names
	 * that have this person as an author but also all books, articles, etc. that have
	 * this person as an author
	 * @param referencedCdmBase
	 * @return
	 */
	public Set<CdmBase> getReferencingObjects(CdmBase referencedCdmBase);

	public Set<ReferencingObjectDto> getReferencingObjectsDto(CdmBase referencedCdmBase);

	/**
	 * Merges cdmBase2 into cdmBase1 and rearranges all reference to cdmBase2 by letting them point to
	 * cdmBase1. If the merge strategy is not defined (<code>null</code>) the default merge strategy is taken instead.
	 *
	 * @param cdmBase1
	 * @param cdmBase2
	 * @param mergeStrategy
	 * @throws IllegalArgumentException
	 * @throws NullPointerException
	 * @throws MergeException
	 */
	public <T extends CdmBase> void merge(T cdmBase1, T cdmBase2, IMergeStrategy mergeStrategy) throws MergeException;

	/**
	 * Computes if cdmBase2 can be merged into cdmBase1. This is usually the case when both
	 * objects are of the same class.
	 * If they are not they must have a common super class and all links to CdmBase2 must be
	 * re-referenceable to cdmBase1.
	 * This is not the case if cdmBase2 is linked by a property which does not support class of cdmBase1.
	 * E.g. User.person requires a person so if user u1 exists with u1.person = person1 and we want to
	 * merge person1 into team1 this is not possible because team1 is not a valid replacement for person1
	 * within the user1.person context.
	 * @param cdmBase1
	 * @param cdmBase2
	 * @param mergeStrategy
	 * @return true if objects are mergeable
	 * @throws IllegalArgumentException
	 * @throws NullPointerException
	 * @throws MergeException
	 */
	public <T extends CdmBase> boolean isMergeable(T cdmBase1, T cdmBase2, IMergeStrategy mergeStrategy) throws MergeException;

	/**
     * Returns a list of matching persistent objects according to the match strategy
     * @param <T>
     * @param objectToMatch
     * @param matchStrategy
     */
    public <T extends IMatchable> List<T> findMatching(
            T objectToMatch, IMatchStrategy matchStrategy) throws MatchException;

	/**
	 * A generic method to retrieve any CdmBase object by its id and class.<BR>
	 * Return the persistent instance of the given entity class with the given identifier,
	 * or null if there is no such persistent instance. (If the instance is already
	 * associated with the session, return that instance. This method never returns
	 * an uninitialized instance.)<BR>
	 * TODO: the behaviour for abstract high level classes (such as CdmBase itself)
	 * is not yet tested.
	 * @see Session#get(Class, java.io.Serializable)
	 * @param clazz the CdmBase class
	 * @param id the identifier
	 * @return the CdmBase instance
	 * @see #find(Class, UUID)
     * @see #find(Class, int, List)
	 */
	public <T extends CdmBase> T find(Class<T> clazz, int id);

	/**
     * A generic method to retrieve any CdmBase object by its id and class.<BR>
     * Return the persistent instance of the given entity class with the given identifier,
     * or <code>null</code> if there is no such persistent instance. (If the instance is already
     * associated with the session, return that instance. This method never returns
     * an uninitialized instance.)
     * @see Session#get(Class, java.io.Serializable)
     * @param clazz the CdmBase class
     * @param uuid the identifier
     * @return the CdmBase instance
     * @see #find(Class, int)
     * @see #find(Class, UUID, List)
     */
    public <T extends CdmBase> T find(Class<T> clazz, UUID uuid);

    /**
     * Method to find CDM Entity by Uuid, by making sure that the underlying
     * hibernate session is not flushed (Session.FLUSH_MODE set to MANUAL temporarily)
     * when performing the read query.
     *
     * @param Uuid
     * @return
     * @throws DataAccessException
     */
    public <T extends CdmBase> T findWithoutFlush(Class<T> clazz, UUID uuid);

	/**
     * Does the same as {@link #find(Class, int)} but also initializes the returned
     * object according to the property path.
     *
     * @param clazz class of the object to be loaded and initialized
     * @param id the identifier
     * @param propertyPaths the property path for initialization
     * @return The initialized object
     * @see #find(Class, UUID)
     */
    public <T extends CdmBase> T find(Class<T> clazz, int id, List<String> propertyPaths);

    /**
     * Does the same as {@link #find(Class, UUID)} but also initializes the returned
     * object according to the property path.
     * @param clazz
     * @param uuid
     * @param propertyPaths
     * @return
     * @see #find(Class, UUID)
     */
    public <T extends CdmBase> T find(Class<T> clazz, UUID uuid, List<String> propertyPaths);

	/**
	 * Returns the result of an hql query
	 * TODO implement parameters
	 * TODO maybe not correct as it is hibernate specific
	 * @param hqlQuery
	 * @return the result of the hibernate query
	 */
    public <T> List<T> getHqlResult(String hqlQuery, Object[] params, Class<T> clazz) throws UnsupportedOperationException;

    public <T> List<T> getHqlResult(String hqlQuery, Map<String, Object> params, Class<T> clazz);

    public <T> List<Map<String,T>> getHqlMapResult(String hqlQuery, Class<T> clazz) throws UnsupportedOperationException;

    /**
     * Returns the result of an hql map query (a query which returns a map)
     * @param hqlQuery
     * @return the result of the hibernate query
     */
    public <T> List<Map<String,T>> getHqlMapResult(String hqlQuery, Object[] params, Class<T> clazz) throws UnsupportedOperationException;

    /**
     * Returns the result of an hql map query (a query which returns a map)
     * @param hqlQuery
     * @return the result of the hibernate query
     */
    public <T> List<Map<String,T>> getHqlMapResult(String hqlQuery, Map<String,Object> params, Class<T> clazz) throws UnsupportedOperationException;

    /**
     * Same as {@link #getHqlResult(String, Object[], Class)} but without hql parameters.
     */
    public <T> List<T> getHqlResult(String hqlQuery, Class<T> clazz);

	/**
	 * TODO remove as this is Hibernate specific.
	 * Returns a Query.
	 * @deprecated this is not clean implemantation as it is hibernate related.
	 * Will be replaced in future
	 * @param hqlQuery
	 * @return
	 */
	@Deprecated
	public Query<?> getHqlQuery(String hqlQuery) throws UnsupportedOperationException;

    public <T> Query<T> getHqlQuery(String hqlQuery, Class<T> clazz) throws UnsupportedOperationException;

	public Set<CdmBase> getReferencingObjectsForDeletion(CdmBase referencedCdmBase);

	public void createFullSampleData();


    /**
     * Returns the number of objects belonging to a certain class being stored
     * in the database
     * @param type
     * @return
     */
    public long count(Class<? extends CdmBase> type);


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
    public <S extends CdmBase> List<S> list(Class<S> type, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);


    /**
     * @param ownerUuid
     * @param fieldName
     * @param appendedPropertyPaths
     * @return
     */
    public Object initializeCollection(UUID ownerUuid, String fieldName, List<String> appendedPropertyPaths);

    /**
     * Initializes a collection or map.
     *
     * @param ownerUuid uuid of owner cdm entity
     * @param fieldName field name of collection or map
     * @return initialised collection or map
     */
    public Object initializeCollection(UUID ownerUuid, String fieldName);

    /**
     * Checks if a collection or map is empty.
     *
     * @param ownerUuid uuid of owner cdm entity
     * @param fieldName field name of collection or map
     * @return true if the collection of map is empty, else false
     */
    public boolean isEmpty(UUID ownerUuid, String fieldName);

    /**
     * Returns the size of requested collection or map.
     *
     * @param ownerUuid uuid of owner cdm entity
     * @param fieldName field name of collection or map
     * @return the size of the persistent collection
     */
    public int size(UUID ownerUuid, String fieldName);

    /**
     * Returns the object contained in a collection or map at the given index.
     *
     * @param ownerUuid uuid of owner cdm entity
     * @param fieldName field name of collection or map
     * @param index the index of the requested element
     * @return the object at the requested index
     */
    public Object get(UUID ownerUuid, String fieldName, int index);

    /**
     * Checks whether an object is contained within a persistent collection.
     *
     * @param ownerUuid uuid of owner cdm entity
     * @param fieldName field name of collection or map
     * @param element the element to check for
     * @return true if the element exists in the collection, false o/w
     */
    public boolean contains(UUID ownerUuid, String fieldName, Object element);

    /**
     * Checks whether an index object exists within a persistent collection
     * (usually a map)
     *
     * @param ownerUuid uuid of owner cdm entity
     * @param fieldName field name of map
     * @param key the index object to look for.
     * @return true if the index object exists in the collection, false o/w
     */
    public boolean containsKey(UUID ownerUuid, String fieldName, Object key);

    /**
     * checks whether an value object exists within a persistent collection
     * (usually a map)
     *
     * @param ownerUuid uuid of owner cdm entity
     * @param fieldName field name of map
     * @param key the value object to look for.
     * @return true if the value object exists in the collection, false o/w
     */
    public boolean containsValue(UUID ownerUuid, String fieldName, Object element);

    public long getCountWithItemInCollection(Class<?> itemClass, Class<?> clazz, String propertyName, CdmBase item);

    public long getCountByFieldAndClass(Class<? extends CdmBase> clazz, String propertyName, CdmBase referencedCdmBase);

    public long getReferencingObjectsCount(CdmBase referencedCdmBase);

    public List<UUID> listUuid(Class<? extends CdmBase> clazz);

    public UUID refresh(CdmBase persistentObject) throws DataAccessException;
}
