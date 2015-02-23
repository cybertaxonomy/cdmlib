// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.collection.spi.PersistentCollection;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.OriginalSourceBase;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.MetaDataPropertyName;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.match.MatchException;
import eu.etaxonomy.cdm.strategy.match.MatchStrategyConfigurator;
import eu.etaxonomy.cdm.strategy.merge.IMergable;
import eu.etaxonomy.cdm.strategy.merge.IMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.MergeException;


public interface ICommonService extends IService<OriginalSourceBase>{
//
//	/** find cdmBase by UUID**/
//	public abstract CdmBase getCdmBaseByUuid(UUID uuid);
//
//	/** save a reference and return its UUID**/
//	public abstract UUID saveCdmBase(CdmBase cdmBase);

	/**
	 * Saves all meta data
	 * @param metaData
	 */
	public void saveAllMetaData(Collection<CdmMetaData> metaData);

	/**
	 * Returns all meta data.
	 * @return
	 */
	public Map<MetaDataPropertyName, CdmMetaData> getCdmMetaData();


	/**
	 * Returns a map of identifiable entities of class <code>clazz</code> which have an original source of
	 * with namespace <code>idNamespace</code> and with an idInSource in <code>idInSourceSet</code> <BR>
	 * The key of the map is the idInSource. If there are multiple objects that have the same id an arbitrary one is chosen.
	 * @param clazz
	 * @param idInSourceSet
	 * @param idNamespace
	 * @return
	 */
	public Map<String, ? extends ISourceable> getSourcedObjectsByIdInSource(Class clazz, Set<String> idInSourceSet, String idNamespace);

	/**
	 * Returns a list of identifiable entities according to their class, idInSource and idNamespace
	 * @param clazz
	 * @param idInSource
	 * @param idNamespace
	 * @return
	 */
	public ISourceable getSourcedObjectByIdInSource(Class clazz, String idInSource, String idNamespace);


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
	 * Merges mergeSecond into mergeFirst. All references to mergeSecond will be replaced by references
	 * to merge first. If no merge strategy is defined (null), the DefaultMergeStrategy will be taken as default.
	 * @param <T>
	 * @param mergeFirst
	 * @param mergeSecond
	 * @param mergeStrategy
	 * @throws MergeException
	 */
	public <T extends IMergable> void   merge(T mergeFirst, T mergeSecond, IMergeStrategy mergeStrategy) throws MergeException;

	/**
	 * Returns all objects that match the object to match according to the given match strategy.
	 * If no match strategy is defined the default match strategy is taken.
	 * @param <T>
	 * @param objectToMatch
	 * @param matchStrategy
	 * @return
	 * @throws MatchException
	 */
	public <T extends IMatchable> List<T> findMatching(T objectToMatch, IMatchStrategy matchStrategy) throws MatchException;

	public <T extends IMatchable> List<T> findMatching(T objectToMatch, MatchStrategyConfigurator.MatchStrategy strategy) throws MatchException;

	/**
	 * A generic method to retrieve any CdmBase object by its id and class.<BR>
	 * @see ICdmGenericDao#find(Class, int)
	 * @see Session#get(Class, java.io.Serializable)
	 * @param clazz the CdmBase class
	 * @param id the cdmBase identifier
	 * @return the CdmBase object defined by clazz and id
	 */
	public CdmBase find(Class<? extends CdmBase> clazz, int id);

	public List getHqlResult(String hqlQuery);

	/**
	 * Initializes a lazy loaded persistent collection.
	 *
	 * @param col the persistent collection to initialize
	 * @return the initialized persistent collection
	 */
	public PersistentCollection initializeCollection(PersistentCollection col);

	/**
	 * Checks if a lazy loaded persistent collection is empty.
	 *
	 * @param col the persistent collection
	 * @return the initialized persistent collection
	 */
	public boolean isEmpty(PersistentCollection col);

	/**
	 * Returns the size of a persistent collection.
	 *
	 * @param col the persistent collection to initialize
	 * @return the size of the persistent collection
	 */
	public int size(PersistentCollection col);

	/**
	 * Returns the object contained in a persistent collection at the given index.
	 *
	 * @param col the persistent collection
	 * @param index the index of the requested element
	 * @return the object at the requested index
	 */
	public Object get(PersistentCollection col, int index);

	/**
	 * checks whether an object is contained within a persistent collection.
	 *
	 * @param col the persistent collection
	 * @param element the element to check for
	 * @return true if the element exists in the collection, false o/w
	 */
	public boolean contains(PersistentCollection col, Object element);

	/**
	 * checks whether an index object exists within a persistent collection
	 * (usually a map)
	 *
	 * @param col the persistent collection
	 * @param key the index object to look for.
	 * @return true if the index object exists in the collection, false o/w
	 */
	public boolean containsKey(PersistentCollection col, Object key);

	/**
	 * checks whether an value object exists within a persistent collection
	 * (usually a map)
	 *
	 * @param col the persistent collection
	 * @param key the value object to look for.
	 * @return true if the value object exists in the collection, false o/w
	 */
	public boolean containsValue(PersistentCollection col, Object element);

	public Set<CdmBase> getReferencingObjectsForDeletion(CdmBase referencedCdmBase);

	/**
	 * Preliminary, may be moved to test later
	 */
	@Deprecated
	public void createFullSampleData();


}