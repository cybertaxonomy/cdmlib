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
import java.util.UUID;

import org.hibernate.Session;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.MetaDataPropertyName;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.match.MatchException;
import eu.etaxonomy.cdm.strategy.match.MatchStrategyConfigurator;
import eu.etaxonomy.cdm.strategy.merge.IMergable;
import eu.etaxonomy.cdm.strategy.merge.IMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.MergeException;



public interface ICommonService /*extends IService<OriginalSourceBase>*/{

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
	 * Tests if cdmBase2 can be merged into cdmBase1. This is usually the case when both
	 * objects are of the same class.
	 * If they are not they must have a common super class and all links to CdmBase2 must be
	 * re-referenceable to cdmBase1.
	 * This is not the case if cdmBase2 is linked by a property which does not support class of cdmBase1.
	 * E.g. User.person requires a person so if user u1 exists with u1.person = person1 and we want to
	 * merge person1 into team1 this is not possible because team1 is not a valid replacement for person1
	 * within the user1.person context.
	 * <BR>
	 * This method is not expensive if classes are equal but may become more expensive if not, because
	 * in this concrete references to cdmBases2 need to be evaluated.
	 * @param cdmBase1
	 * @param cdmBase2
	 * @param mergeStrategy
	 * @return
	 * @throws MergeException
	 */
	public <T extends CdmBase> boolean isMergeable(T cdmBase1, T cdmBase2, IMergeStrategy mergeStrategy) throws MergeException;


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
     * Merges mergeSecond into mergeFirst. All references to mergeSecond will be replaced by references
     * to merge first, using a merge strategy defined by the given class.
     *
     * @param mergeFirst
     * @param mergeSecond
     * @param clazz
     * @throws MergeException
     */
    public <T extends IMergable> void merge(T mergeFirst, T mergeSecond, Class<? extends CdmBase> clazz) throws MergeException;


    /**
     * Merges mergeSecond into mergeFirst. All references to mergeSecond will be replaced by references
     * to merge first, using a merge strategy defined by the mergeFirst type.
     * @param mergeFirst
     * @param mergeSecond
     * @throws MergeException
     */
    public <T extends IMergable> void merge(T mergeFirst, T mergeSecond) throws MergeException;


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
     * Initializes a collection or map.
     *
     * @param ownerUuid uuid of owner cdm entity
     * @param fieldName field name of collection or map
     * @return initialised collection or map
     */
    public Object initializeCollection(UUID ownerUuid, String fieldName);

	/**
	 * Checks if a collection or map is empty.
	 *
     * @param ownerUuid uuid of owner cdm entity
     * @param fieldName field name of collection or map
	 * @return true if the collection of map is empty, else false
	 */
	public boolean isEmpty(UUID ownerUuid, String fieldName);

	/**
	 * Returns the size of requested collection or map.
	 *
     * @param ownerUuid uuid of owner cdm entity
     * @param fieldName field name of collection or map
	 * @return the size of the persistent collection
	 */
	public int size(UUID ownerUuid, String fieldName);

	/**
	 * Returns the object contained in a collection or map at the given index.
	 *
     * @param ownerUuid uuid of owner cdm entity
     * @param fieldName field name of collection or map
	 * @param index the index of the requested element
	 * @return the object at the requested index
	 */
	public Object get(UUID ownerUuid, String fieldName, int index);

	/**
	 * Checks whether an object is contained within a persistent collection.
	 *
     * @param ownerUuid uuid of owner cdm entity
     * @param fieldName field name of collection or map
	 * @param element the element to check for
	 * @return true if the element exists in the collection, false o/w
	 */
	public boolean contains(UUID ownerUuid, String fieldName, Object element);

	/**
	 * Checks whether an index object exists within a persistent collection
	 * (usually a map)
	 *
     * @param ownerUuid uuid of owner cdm entity
     * @param fieldName field name of map
	 * @param key the index object to look for.
	 * @return true if the index object exists in the collection, false o/w
	 */
	public boolean containsKey(UUID ownerUuid, String fieldName, Object key);

	/**
	 * checks whether an value object exists within a persistent collection
	 * (usually a map)
	 *
     * @param ownerUuid uuid of owner cdm entity
     * @param fieldName field name of map
	 * @param key the value object to look for.
	 * @return true if the value object exists in the collection, false o/w
	 */
	public boolean containsValue(UUID ownerUuid, String fieldName, Object element);

	public Set<CdmBase> getReferencingObjectsForDeletion(CdmBase referencedCdmBase);

	/**
	 * Preliminary, may be moved to test later
	 */
	@Deprecated
	public void createFullSampleData();


    /**
     * Returns the number of objects that belong to a certain class.
     * @param type the CdmBase class
     * @return the number of objects in the database
     */
    public <S extends CdmBase> int count(Class<S> type);


    /**
     * Generic method to retrieve a list of objects. Use only if no specific service class
     * can be used.
     * @param type
     * @param limit
     * @param start
     * @param orderHints
     * @param propertyPaths
     * @see IService#list(Class, Integer, Integer, List, List)
     * @return
     */
    public <S extends CdmBase> List<S> list(Class<S> type, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Save a new entity (persists the entity)
     * @param newInstance the new entity to be persisted
     * @return A generated UUID for the new persistent entity
     */
    public UUID save(CdmBase newInstance);


    /**
     * Save a collection containing new entities (persists the entities)
     * @param newInstances the new entities to be persisted
     * @return A Map containing the new entities, keyed using the generated UUID's
     *         of those entities
     */
    public <T extends CdmBase> Map<UUID,T> save(Collection<T> newInstances);
}