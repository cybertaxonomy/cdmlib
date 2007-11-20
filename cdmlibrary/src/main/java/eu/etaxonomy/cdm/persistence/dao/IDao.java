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

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import eu.etaxonomy.cdm.model.Description;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Enumeration;

/**
 * an data access interface that all data access classes implement
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:10
 */
public interface IDao<T extends CdmBase, ID extends Serializable> {
	public void saveOrUpdate(T transientObject) throws DataAccessException;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.IDAO#save(java.lang.Object)
	 */
	public Serializable save(T newInstance) throws DataAccessException;
	
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
	
	
	public List<T> find(String queryString);

	public Boolean exists(ID id);

	public List<T> list(Integer limit);

}
