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
import java.util.UUID;

import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.model.common.CdmBase;
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
	 * Returns a sublist of CdmBase instances stored in the database.
	 * A maximum of 'limit' objects are returned, starting at object with index 'start'.
	 * @param limit the maximum number of entities returned (can be null to return all entities)
	 * @param start
	 * @param orderHints
	 * @return
	 * @throws DataAccessException
	 */
	public <TYPE extends T> List<TYPE> list(Integer limit, Integer start, List<OrderHint> orderHints);
	
	/**
	 * Returns a sublist of CdmBase instances of type <TYPE> stored in the database.
	 * A maximum of 'limit' objects are returned, starting at object with index 'start'.
	 * @param limit the maximum number of entities returned (can be null to return all entities)
	 * @param start
	 * @return
	 * @throws DataAccessException
	 */
	public <TYPE extends T> List<TYPE> list(Class<TYPE> type, Integer limit, Integer start) throws DataAccessException;
	

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
	 * 
	 */
	public void flush();
	
		
	
}
