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

import org.hibernate.Session;
import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.query.Grouping;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * an data access interface that all data access classes implement
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:10
 */
public interface ICdmEntityDao<T extends CdmBase> {
	
	/**
	 * @param transientObject
	 * @return
	 * @throws DataAccessException
	 */
	public UUID saveOrUpdate(T transientObject) throws DataAccessException;

	//public UUID saveOrUpdateAll(Collection<T> transientObjects) throws DataAccessException;
	
	/**
	 * @param newOrManagedObject
	 * @return
	 * @throws DataAccessException
	 */
	public UUID save(T newOrManagedObject) throws DataAccessException;
	
	public UUID merge(T transientObject) throws DataAccessException;
	
	public void clear() throws DataAccessException;
		
	public Session getSession() throws DataAccessException;

		public Map<UUID, T> saveAll(Collection<T> cdmObjCollection) throws DataAccessException;

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
	public <TYPE extends T> List<TYPE> list(Class<TYPE> type, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * Returns a sublist of CdmBase instances stored in the database. A maximum
	 * of 'limit' objects are returned, starting at object with index 'start'.
	 * The bean properties specified by the parameter <code>propertyPaths</code>
	 * and recursively initialized for each of the entities in the resultset
	 * 
	 * For detailed description and examples redarding
	 * <code>propertyPaths</code> <b>please refer to:</b>
	 * {@link BeanInitializer#initialize(Object, List)}
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
	 * Returns a sublist of CdmBase instances of type <TYPE> stored in the database.
	 * A maximum of 'limit' objects are returned, starting at object with index 'start'.
	 * @param limit the maximum number of entities returned (can be null to return all entities)
	 * @param start
	 * @return
	 * @throws DataAccessException
	 */
	public <TYPE extends T> List<TYPE> list(Class<TYPE> type, Integer limit, Integer start) throws DataAccessException;
	
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
	
	public List<T> rows(String tableName, int limit, int start) throws DataAccessException;

	/**
	 * @param id
	 * @return
	 * @throws DataAccessException
	 */
	public T findById(int id) throws DataAccessException;

	/**
	 * @param Uuid
	 * @return
	 * @throws DataAccessException
	 */
	public T findByUuid(UUID Uuid) throws DataAccessException;
	
	/**
	 * @param uuidSet
	 * @return
	 * @throws DataAccessException
	 */
	public List<T> findByUuid(Set<UUID> uuidSet) throws DataAccessException;
	
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
	 * {@link BeanInitializer#initialize(Object, List)}
	 * 
	 * @param uuid
	 * @param propertyPaths properties to be initialized
	 * @return
	 */
	public T load(UUID uuid, List<String> propertyPaths);
	
	
	/**
	 * @param uuidSet
	 * @param propertyPaths
	 * @return
	 * @throws DataAccessException
	 */
	public List<T> load(Set<UUID> uuidSet, List<String> propertyPaths) throws DataAccessException;
	
	/**
	 * @param uuid
	 * @return
	 * @throws DataAccessException
	 */
	public Boolean exists(UUID uuid) throws DataAccessException;
	
	public int count();

	/**
	 * Returns the number of objects of type <TYPE> - which must extend T
	 * @param <TYPE>
	 * @param clazz
	 * @return
	 */
	public <TYPE extends T> int count(Class<TYPE> clazz);

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
}
