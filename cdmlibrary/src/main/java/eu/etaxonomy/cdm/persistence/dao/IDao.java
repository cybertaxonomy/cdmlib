/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao;


import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import eu.etaxonomy.cdm.model.Description;

/**
 * an data access interface that all data access classes implement
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:10
 */
public interface IDao<T, ID> {
	public void saveOrUpdate(T transientObject) throws DataAccessException;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.IDAO#save(java.lang.Object)
	 */
	public ID save(T newInstance) throws DataAccessException;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.IDAO#update(java.lang.Object)
	 */
	public void update(T transientObject) throws DataAccessException;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.IDAO#delete(java.lang.Object)
	 */
	public void delete(T persistentObject) throws DataAccessException;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.IDAO#findById(java.io.Serializable)
	 */
	public T findById(ID id) throws DataAccessException;

//********************************************//	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.IDAO#find(java.lang.String, java.lang.Object[])
	 */
	public List find(String queryString, Object[] args);
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.IDAO#find(java.lang.String)
	 */
	public List find(String queryString);


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.IDAO#exists(java.io.Serializable)
	 */
	public Boolean exists(ID id);

}
