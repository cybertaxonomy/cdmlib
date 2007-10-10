/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao;

import java.io.Serializable;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.apache.log4j.Logger;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 17:39:52
 */

public interface IDao<T, ID extends Serializable> {
	static Logger logger = Logger.getLogger(IDao.class);
	
	/** Persist the newInstance object into database */
    public ID save(T newInstance);
    
    /** Save changes made to a persistent object.  */
    public void update(T transientObject) 
        throws DataAccessException;
    
	/** Persist the newInstance object into database */
    public void saveOrUpdate(T transientObject);

    /** Remove an object from persistent storage in the database */
    public void delete(T persistentObject)
    	throws DataAccessException ;
    
    /** Retrieve an object that was previously persisted to the database using
     *   the indicated id as primary key
     */
    public T findById(ID id) 
        throws DataAccessException;
 
    public List<T> find(String queryString, Object[] args);
    
    public List<T> find(String queryString);
    
    public Boolean exists(ID id);
}
