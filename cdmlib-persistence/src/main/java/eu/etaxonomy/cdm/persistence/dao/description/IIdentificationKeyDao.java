/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.description;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.IIdentificationKey;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.query.Grouping;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * A read-only interface to allow querying across all IIdentificationKey instances, regardless of type
 * @author ben.clark
 * @version 1.0
 * @created 21-Dec-2009 13:48:10
 */
public interface IIdentificationKeyDao {
	

	/**
	 * Returns a sublist of IIdentificationKey instances stored in the database. A maximum
	 * of 'limit' objects are returned, starting at object with index 'start'.
	 * 
	 * @param type 
	 * @param limit
	 *            the maximum number of entities returned (can be null to return
	 *            all entities)
	 * @param start
	 * @return
	 * @throws DataAccessException
	 */
	public List<IIdentificationKey> list(Integer limit, Integer start, List<String> propertyPaths);

	/**
	 * Returns the number of objects of type IIdentificationKey
	 * @return
	 */
	public int count();
} 
