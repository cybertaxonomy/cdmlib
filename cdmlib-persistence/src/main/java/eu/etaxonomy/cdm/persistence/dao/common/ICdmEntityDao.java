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
import java.util.UUID;
import org.springframework.dao.DataAccessException;
import eu.etaxonomy.cdm.model.common.CdmBase;

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
	 * @param limit
	 * @param start
	 * @return
	 * @throws DataAccessException
	 */
	public List<T> list(int limit, int start) throws DataAccessException;

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
	
	/**
	 * 
	 */
	public void flush();
	
}
