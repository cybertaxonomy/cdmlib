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
import java.util.UUID;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.CdmBase;


/**
 * @author a.mueller
 *
 */
@Transactional(propagation=Propagation.SUPPORTS)
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
	
	public List<T> rows(String tableName, int limit, int start);
	
	/**
	 * Save a new entity (persists the entity)
	 * @param newInstance the new entity to be persisted
	 * @return A generated UUID for the new persistent entity
	 */
	public UUID save(T newInstance);
	
	/**
	 * Save a new entity or update the persistent state of an existing 
	 * transient entity that has been persisted previously
	 * 
	 * @param transientObject the entity to be persisted
	 * @return The unique identifier of the persisted entity
	 */
	public UUID saveOrUpdate(T transientObject);
	
	/**
	 * Update the persistent state of an existing transient entity 
	 * that has been persisted previously
	 * 
	 * @param transientObject the entity to be persisted
	 * @return The unique identifier of the persisted entity
	 */
	public UUID update(T transientObject);
	
	/**
	 * Save a collection containing new entities (persists the entities)
	 * @param newInstances the new entities to be persisted
	 * @return A Map containing the new entities, keyed using the generated UUID's
	 *         of those entities
	 */
	public Map<UUID,T> saveAll(Collection<T> newInstances);
	
	/**
	 * Delete an existing persistent object
	 * 
	 * @param persistentObject the object to be deleted
	 * @return the unique identifier of the deleted entity
	 */
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
	 * Returns true if an entity of type <T> with a unique identifier matching the 
	 * identifier supplied exists in the database, or false if no such entity can be 
	 * found. 
	 * @param uuid the unique identifier of the entity required
	 * @return an entity of type <T> matching the uuid, or null if that entity does not exist
	 */
	public boolean exists(UUID uuid);
}