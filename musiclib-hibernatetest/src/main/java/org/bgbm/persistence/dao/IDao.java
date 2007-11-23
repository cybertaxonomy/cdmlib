/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package org.bgbm.persistence.dao;


import java.io.Serializable;

import org.bgbm.model.MetaUltra;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;


/**
 * an data access interface that all data access classes implement
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:10
 */
public interface IDao<T extends MetaUltra, ID extends Serializable> {
	public void save(T transientObject) throws DataAccessException;
	
	public void update(T transientObject) throws DataAccessException;

	public void delete(T persistentObject) throws DataAccessException;
	
	public T findById(ID id) throws DataAccessException;

}
