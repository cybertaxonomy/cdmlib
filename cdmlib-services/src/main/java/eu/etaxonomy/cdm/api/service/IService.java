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


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.query.OrderHint;


/**
 * @author a.mueller
 *
 */
/**
 * @author a.kohlbecker
 * @date 23.03.2009
 *
 * @param <T>
 */
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
public interface IService<T extends CdmBase>{

	/**
	 * Returns a count of all entities of type <T>
	 * @return a count of all entities
	 */
	public int count();

	/**
	 * Returns a count of all entities of type <TYPE> that extend <T>
	 * @param clazz the class of entities to be counted
	 * @return a count of entities
	 */
	public <TYPE extends T> int count(Class<TYPE> clazz);
	
	/**
	 * Returns a List of entities of type <T>
	 * TODO would like to substitute List with Pager, but we need
	 * to agree on how to implement paging first
	 * 
	 * @param limit The maximum number of entities returned
	 * @param start The offset from the start of the dataset
	 * @return a List of entities
	 */
	public List<T> list(int limit, int start);
	
	/**
	 * Returns a List of entities of type <TYPE> which must extend 
	 * <T>
	 * TODO would like to substitute List with Pager, but we need
	 * to agree on how to implement paging first
	 * 
	 * @param type  The type of entities to return
	 * @param limit The maximum number of entities returned
	 * @param start The offset from the start of the dataset
	 * @return a List of entities
	 */
	public <TYPE extends T> List<TYPE> list(Class<TYPE> type, int limit, int start);
	
	
	/**
	 * @param pageSize
	 *            the maximum number of entities returned per page (can be null
	 *            to return all entities in a single page)
	 * @param pageNumber the number of the page to be returned, the first page has the
	 *            pageNumber = 1
	 * @return
	 */
	public Pager<T> list(Integer pageSize, Integer pageNumber);
	
	
	/**
	 * @param pageSize
	 *            the maximum number of entities returned per page (can be null
	 *            to return all entities in a single page)
	 * @param pageNumber
	 *            the number of the page to be returned, the first page has the
	 *            pageNumber = 1
	 * @param orderHints
	 *            may be null
	 * @return
	 */
	public Pager<T> list(Integer pageSize, Integer pageNumber, List<OrderHint> orderHints);
	
	/**
	  Returns a paged list of entities of type <TYPE> which must extend 
	 * <T>
	 * 
	 * @param type  The type of entities to return
	 * @param pageSize
	 * @param pageNumber
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
	 * @return
	 */
	public <TYPE extends T> Pager<TYPE> list(Class<TYPE> type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	/**
	 * @param pageSize
	 * @param pageNumber
	 * @param orderHints may be null
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
	 * @return
	 */
	public Pager<T> list(Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
	
	public List<T> rows(String tableName, int limit, int start);
	
	/**
	 * Save a new entity (persists the entity)
	 * @param newInstance the new entity to be persisted
	 * @return A generated UUID for the new persistent entity
	 */
	@Transactional(readOnly=false)
	public UUID save(T newInstance);
	
	/**
	 * Save a new entity or update the persistent state of an existing 
	 * transient entity that has been persisted previously
	 * 
	 * @param transientObject the entity to be persisted
	 * @return The unique identifier of the persisted entity
	 */
	@Transactional(readOnly=false)
	public UUID saveOrUpdate(T transientObject);
	
	/**
	 * Update the persistent state of an existing transient entity 
	 * that has been persisted previously
	 * 
	 * @param transientObject the entity to be persisted
	 * @return The unique identifier of the persisted entity
	 */
	@Transactional(readOnly=false)
	public UUID update(T transientObject);
	
	/**
	 * Copy the state of the given object onto the persistent object with the same identifier.
	 * 
	 * @param transientObject the entity to be merged
	 * @return The unique identifier of the persisted entity
	 */
	@Transactional(readOnly=false)
	public UUID merge(T transientObject);
	
	/**
	 * Copy the state of the given object onto the persistent object with the same identifier.
	 * 
	 * @param transientObject the entity to be merged
	 * @return The unique identifier of the persisted entity
	 */
//	@Transactional(readOnly=false)
	public void clear();
	
	public Session getSession();
	
	/**
	 * Save a collection containing new entities (persists the entities)
	 * @param newInstances the new entities to be persisted
	 * @return A Map containing the new entities, keyed using the generated UUID's
	 *         of those entities
	 */
	@Transactional(readOnly=false)
	public Map<UUID,T> saveAll(Collection<T> newInstances);
	
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
	@Transactional(readOnly=false)
	public UUID refresh(T persistentObject);
	
	/**
	 * Delete an existing persistent object
	 * 
	 * @param persistentObject the object to be deleted
	 * @return the unique identifier of the deleted entity
	 */
	@Transactional(readOnly=false)
	public UUID delete(T persistentObject);
	
	/**
	 * Return a persisted entity that matches the unique identifier
	 * supplied as an argument, or null if the entity does not exist
	 * 
	 * @param uuid the unique identifier of the entity required
	 * @return an entity of type <T>, or null if the entity does not exist
	 */
	public T findByUuid(UUID uuid);
	
	/**
	 * Return a list of persisted entities that match the unique identifier
	 * set supplied as an argument
	 * 
	 * @param uuidSet the set of unique identifiers of the entities required
	 * @return a list of entities of type <T>
	 */
	public List<T> findByUuid(Set<UUID> uuidSet);

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
	 * @return
	 */
	public T load(UUID uuid, List<String> propertyPaths);
	
	
	
	/**
	 * Returns true if an entity of type <T> with a unique identifier matching the 
	 * identifier supplied exists in the database, or false if no such entity can be 
	 * found. 
	 * @param uuid the unique identifier of the entity required
	 * @return an entity of type <T> matching the uuid, or null if that entity does not exist
	 */
	public boolean exists(UUID uuid);
}