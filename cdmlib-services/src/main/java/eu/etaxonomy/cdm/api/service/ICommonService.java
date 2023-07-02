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
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData;
import eu.etaxonomy.cdm.model.metadata.CdmMetaDataPropertyName;
import eu.etaxonomy.cdm.model.reference.ISourceable;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.persistence.dto.ReferencingObjectDto;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.match.MatchException;
import eu.etaxonomy.cdm.strategy.match.MatchStrategyConfigurator;
import eu.etaxonomy.cdm.strategy.merge.IMergable;
import eu.etaxonomy.cdm.strategy.merge.IMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.MergeException;

/**
 * A generic service that is not base type dependend as are all services
 * that inherit from {@link IService}.
 *
 * @author a.mueller
 */
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
	public Map<CdmMetaDataPropertyName, CdmMetaData> getCdmMetaData();

	 /**
     * Returns a map of identifiable entities of class <code>clazz</code> which have an original source of
     * with namespace <code>idNamespace</code> and with an idInSource in <code>idInSourceSet</code> <BR>
     * The key of the map is the idInSource. If there are multiple objects that have the same id an arbitrary one is chosen.
     * @param clazz
     * @param idInSourceSet
     * @param idNamespace
     * @return
     */
	public <S extends ISourceable> Map<String, S> getSourcedObjectsByIdInSourceC(Class<S> clazz, Set<String> idInSourceSet, String idNamespace);

	/**
	 * Returns a list of identifiable entities according to their class, idInSource and idNamespace
	 * @param clazz
	 * @param idInSource
	 * @param idNamespace
	 * @return
	 */
	public <S extends ISourceable> S getSourcedObjectByIdInSource(Class<S> clazz, String idInSource, String idNamespace);


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
     * @see #getReferencingObjects(CdmBase)
     */
    public Set<ReferencingObjectDto> getReferencingObjectDtos(CdmBase referencedCdmBase);


    public Set<ReferencingObjectDto> initializeReferencingObjectDtos(Set<ReferencingObjectDto> dtos,
            boolean doReferencingEntity, boolean doTargetEntity, boolean doDescription, Language language);


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
	public <T extends IMergable> void merge(T mergeFirst, T mergeSecond, IMergeStrategy mergeStrategy) throws MergeException;

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

	public <T extends CdmBase> T findWithUpdate(Class<T> clazz, int id);


	/**
	 * A generic method to retrieve any CdmBase object by its id and class.<BR>
	 * @see ICdmGenericDao#find(Class, int)
	 * @see Session#get(Class, java.io.Serializable)
	 * @param clazz the CdmBase class
	 * @param id the cdmBase identifier
	 * @return the CdmBase object defined by clazz and id
	 * @see #find(Class, int, List)
	 */
	public <T extends CdmBase> T find(Class<T> clazz, int id);

    /**
     * @param clazz the Class of the obejct to find
     * @param id
     * @param propertyPaths the property path for bean initialization
     * @return
     * @see #find(Class, int)
     */
    public <T extends CdmBase> T find(Class<T> clazz, int id, List<String> propertyPaths);

    /**
     * A generic method to retrieve any CdmBase object by its uuid and class.<BR>
     * @param clazz the Class of the obejct to find
     * @param uuid the UUID of the object to find
     * @return
     */
    public <T extends CdmBase> T find(Class<T> clazz, UUID uuid);

    /**
     * Return a persisted entity that matches the unique identifier
     * supplied as an argument, or null if the entity does not exist.
     * <p>
     * The difference between this method and {@link #find(UUID) find} is
     * that this method makes the hibernate read query with the
     * {@link org.hibernate.FlushMode FlushMode} for the session set to 'MANUAL'
     * <p>
     * <b>WARNING:</b>This method should <em>ONLY</em> be used when it is absolutely
     * necessary and safe to ensure that the hibernate session is not flushed before a read
     * query. A use case for this is the {@link eu.etaxonomy.cdm.api.cache.CdmPermanentCacheBase CdmCacher},
     * (ticket #4276) where a call to {@link eu.etaxonomy.cdm.api.cache.CdmPermanentCacheBase#load(UUID) load}
     * the CDM Entity using the standard {@link #find(UUID) find} method results in recursion
     * due to the fact that the {@link #find(UUID) find} method triggers a hibernate session
     * flush which eventually could call {@link eu.etaxonomy.cdm.model.name.NonViralName#getNameCache getNameCache},
     * which in turn (in the event that name cache is null) eventually calls the
     * {@link eu.etaxonomy.cdm.api.cache.CdmPermanentCacheBase#load(UUID uuid) load} again.
     * Apart from these kind of exceptional circumstances, the standard {@link #find(UUID) find}
     * method should always be used to ensure that the persistence layer is always in sync with the
     * underlying database.
     *
     * @param uuid the uuid o the entity to search for
     * @param clazz the clazz of the entity to search for
     * @return an entity of type <T>, or null if the entity does not exist or uuid is <code>null</code>
     */
    public <T extends CdmBase> T findWithoutFlush(Class<T> clazz, UUID uuid);

    /**
     * A generic method to retrieve any CdmBase object by its UUID and class,
     * including initialization via property path.<BR>
     * @param clazz the Class of the obejct to find
     * @param uuid the UUID of the object to find
     * @param propertyPaths the property path for bean initialization
     * @return
     */
    public <T extends CdmBase> T find(Class<T> clazz, UUID uuid, List<String> propertyPaths);

	/**
	 * Returns the result of an HQL Query which does
	 * not inlcude parameters
	 * @see #getHqlResult(String, Object[], Class)
	 */
    public <T> List<T> getHqlResult(String hqlQuery, Class<T> clazz);

	/**
	 * Returns the result of an HQL Query which inlcudes parameters as
	 * ordinal parameters (e.g. a = ?0).
	 *
	 * @param hqlQuery the HQL query
	 * @param params the parameter values
	 * @return  the result of the HQL query
     * @see #getHqlResult(String, Class)
	 */
    public <T> List<T> getHqlResult(String hqlQuery, Object[] params, Class<T> clazz);

    /**
     * Initializes a collection or map.
     *
     * @param ownerUuid uuid of owner cdm entity
     * @param fieldName field name of collection or map
     * @return initialised collection or map
     */
    public Object initializeCollection(UUID ownerUuid, String fieldName);

    public Object initializeCollection(UUID ownerUuid, String fieldName, List<String> propertyPaths);

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
	 * Fills more or less all tables of a database with some data.
	 * Preliminary, may be moved to test later
	 *
	 * @deprecated for internal use only
	 */
	@Deprecated
	public void createFullSampleData();


    /**
     * Returns the number of objects that belong to a certain class.
     * @param type the CdmBase class
     * @return the number of objects in the database
     */
    public <S extends CdmBase> long count(Class<S> type);


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
    public CdmBase save(CdmBase newInstance);

    /**
     * Save or update a new entity
     * @param entity the entity to be persisted
     * @return The UUID of the persistent entity
     */
    public UUID saveOrUpdate(CdmBase entity);

    /**
     * Save a collection containing new entities (persists the entities)
     * @param newInstances the new entities to be persisted
     * @return A Map containing the new entities, keyed using the generated UUID's
     *         of those entities
     */
    public <T extends CdmBase> Map<UUID,T> save(Collection<T> newInstances);

    /**
     * Save or update a collection containing entities
     * @param entities the entities to be persisted
     * @return A Map containing the new entities, keyed using the UUID's
     *         of those entities
     */
    public <T extends CdmBase> Map<UUID,T> saveOrUpdate(Collection<T> entities);

    /**
     * @param instance
     * @return
     */
    public UUID delete(CdmBase instance);

    /**
     * @param mergeFirstId
     * @param mergeSecondId
     * @param clazz
     * @throws MergeException
     * @Deprecated the preferred method is to use uuids {@link #merge(UUID, UUID, Class)}
     */
    @Deprecated
    public <T extends IMergable> void merge(int mergeFirstId, int mergeSecondId, Class<? extends CdmBase> clazz) throws MergeException;

    /**
     * @param mergeFirstUuid uuid of the first object to merge
     * @param mergeSecondUuid UUID of the second object to merge
     * @param clazz
     * @throws MergeException
     */
    public <T extends IMergable> void merge(UUID mergeFirstUuid, UUID mergeSecondUuid, Class<? extends CdmBase> clazz) throws MergeException;

    public long getReferencingObjectsCount(CdmBase referencedCdmBase);

    public List<UUID> listUuid(Class<? extends CdmBase> clazz);

    public UUID refresh(CdmBase persistentObject);

}
