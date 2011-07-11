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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.CdmMetaData;
import eu.etaxonomy.cdm.model.common.CdmMetaData.MetaDataPropertyName;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.match.MatchException;
import eu.etaxonomy.cdm.strategy.merge.IMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.MergeException;

public interface ICdmGenericDao {

	public UUID saveOrUpdate(CdmBase transientObject) throws DataAccessException;
	
	public UUID save(CdmBase newOrManagedObject) throws DataAccessException;
	
	public UUID update(CdmBase transientObject) throws DataAccessException;
	
	public UUID delete(CdmBase persistentObject) throws DataAccessException;
	
	public void saveMetaData(CdmMetaData cdmMetaData);
	
	public List<CdmMetaData> getMetaData();
	
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
	 * Returns all CdmBase objects that reference the referencedCdmBase.
	 * For example, if referencedCdmBase is an agent it may return all taxon names
	 * that have this person as an author but also all books, articles, etc. that have 
	 * this person as an author
	 * @param referencedCdmBase
	 * @return
	 */
	public Set<CdmBase> getReferencingObjects(CdmBase referencedCdmBase);
	
	/**
	 * Merges cdmBase2 into cdmBase2 and rearranges all reference to cdmBase2 by letting them point to
	 * cdmBase1. If the merge strategy is not defined (<code>null</code>)  the default merge strategy is taken instead.
	 * @param <T>  
	 * @param cdmBase1
	 * @param cdmBase2
	 * @param mergeStrategy
	 * @throws IllegalArgumentException
	 * @throws NullPointerException
	 * @throws MergeException 
	 */
	public <T extends CdmBase> void   merge(T cdmBase1, T cdmBase2, IMergeStrategy mergeStrategy) throws MergeException;
	
	/**
	 * Returns a List of matching persistent objects according to the match strategy
	 * @param <T>
	 * @param objectToMatch
	 * @param matchStrategy
	 * @return
	 * @throws MatchException 
	 */
	public <T extends IMatchable> List<T> findMatching(T objectToMatch, IMatchStrategy matchStrategy) throws MatchException; 
	
	
	//TODO remove
	public void test();
	//for testing
	public <T extends CdmBase> T find(Class<T> clazz, int id);
	
	/**
	 * Returns the result of an hql query
	 * TODO implement parameters
	 * @param hqlQuery
	 * @return
	 */
	public List getHqlResult(String hqlQuery);
}
