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
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.model.common.CdmBase;

public interface ICdmGenericDao {

	public UUID saveOrUpdate(CdmBase transientObject) throws DataAccessException;
	
	public UUID save(CdmBase newOrManagedObject) throws DataAccessException;
	
	public UUID update(CdmBase transientObject) throws DataAccessException;
	
	public UUID delete(CdmBase persistentObject) throws DataAccessException;
	
	/**
	 * Returns a CdmBase object of class <code>clazz</code> that has a property with name
	 * <code>propertyName</code> that references the CdmBase object <code>referencedCdmBase</code>.
	 * @param clazz
	 * @param propertyName
	 * @param value
	 * @return
	 */
	public List<CdmBase> getCdmBasesByFieldAndClass(Class clazz, String propertyName, CdmBase referencedCdmBase);
	
	/**
	 * Returns ...
	 * @param thisClass
	 * @param otherClazz
	 * @param propertyName
	 * @param referencedCdmBase
	 * @return
	 */
	public List<CdmBase> getCdmBasesWithItemInCollection(Class itemClass, Class clazz, String propertyName, CdmBase item);
	
	/**
	 * Returns all CDM classes. If includeAbstractClasses is false the abstract classes
	 * will not be in the resultset.
	 * @param includeAbstractClasses
	 * @return
	 */
	public Set<Class<? extends CdmBase>> getAllCdmClasses(boolean includeAbstractClasses);
	
	
	/**
	 * Returns the result of an hql query
	 * TODO implement parameters
	 * @param hqlQuery
	 * @return
	 */
	public List getHqlResult(String hqlQuery);
}
