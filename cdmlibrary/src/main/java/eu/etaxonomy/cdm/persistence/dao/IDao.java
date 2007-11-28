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


import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * an data access interface that all data access classes implement
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:10
 */
public interface IDao<T extends CdmBase> {
	public String saveOrUpdate(T transientObject) throws DataAccessException;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.IDAO#save(java.lang.Object)
	 */
	public String save(T newOrManagedObject) throws DataAccessException;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.IDAO#update(java.lang.Object)
	 */
	public String update(T transientObject) throws DataAccessException;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.IDAO#delete(java.lang.Object)
	 */
	public String delete(T persistentObject) throws DataAccessException;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.IDAO#findById(java.io.Serializable)
	 */
	public T findById(int id) throws DataAccessException;

	public T findByUuid(String Uuid) throws DataAccessException;
	
	public Boolean exists(String uuid);

	public List<T> find(String queryString);

	public List<CdmBase> executeHsql(String hsql);

	public List<T> list(int limit, int start);

}
