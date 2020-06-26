/**
 * Copyright (C) 2014 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.cache;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.management.MBeanServer;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.cache.CdmCacher;
import eu.etaxonomy.cdm.model.ICdmCacher;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dto.MergeResult;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.SizeOfPolicyConfiguration;
import net.sf.ehcache.management.ManagementService;
import net.sf.ehcache.statistics.LiveCacheStatistics;

/**
 * This cache handles transient (id>0) and volatile (id=0) CdmBase objects.
 * Volatile objects need to be added via {@link #putVolatitleEntity(CdmBase)}
 * and their id is updated as soon as a transient object with the same
 * uuid is added to the cacher.
 *
 * This cache guarantees that
 *  - all objects put will be ancestors of CdmBase
 *  - all CdmBase objects in the cache will be already de-proxied
 *  - after any CdmBase object is put in the cache,
 *  all non-null / non-proxy CdmBase objects in the sub-graph
 *  will also be present in the cache.
 *
 * @author cmathew
 * @since 14 Oct 2014
 */
public class CdmTransientEntityCacher implements ICdmCacher {

    private static final Logger logger = Logger.getLogger(CdmTransientEntityCacher.class);

    //the key for this cacher within the CacheManager
    private final String cacheId;

    //the cache
    private final Cache cache;

    //permanent cache which is usually used to cache terms permanently
    private static CdmCacher permanentCache;

    private final CacheLoader cacheLoader;

    //map for volatile entities (id=0)
    private final Map<UUID, CdmBase> volatileEntitiesMap = new HashMap<>();

    private static volatile boolean managementBeansConfigured = false;

// ********************* CONSTRUCTOR **********************************/

    public CdmTransientEntityCacher(String cacheId) {
        this.cacheId = cacheId;

        cache = new Cache(getEntityCacheConfiguration(cacheId));

        getCacheManager().removeCache(cache.getName());
        getCacheManager().addCache(cache);

        cacheLoader = new CacheLoader(this);
    }

    public CdmTransientEntityCacher(Object sessionOwner) {
        this(generateCacheId(sessionOwner));
    }

//****************************** STATIC METHODS *********************************/

    /**
     * Generates an id for this session.
     * @param sessionOwner
     * @return
     */
    private static String generateCacheId(Object sessionOwner) {
        return sessionOwner.getClass().getName() +  String.valueOf(sessionOwner.hashCode());
    }

    public static <T extends CdmBase> CdmEntityCacheKey<T> generateKey(Class<T> clazz, int id) {
        return new CdmEntityCacheKey<T>(clazz, id);
    }

    public static <T extends CdmBase> CdmEntityCacheKey<T> generateKey(T cdmBase) {
        Class<T> entityClass = (Class<T>)cdmBase.getClass();
        return new CdmEntityCacheKey<T>(entityClass, cdmBase.getId());
    }

    public static void setPermanentCacher(CdmCacher permanentCacher) {
        permanentCache = permanentCacher;
    }

//****************************** METHODS *********************************/

    /**
     * Returns the default cache configuration.
     */
    private CacheConfiguration getEntityCacheConfiguration(String cacheId) {
        SizeOfPolicyConfiguration sizeOfConfig = new SizeOfPolicyConfiguration();
        sizeOfConfig.setMaxDepth(100);
        sizeOfConfig.setMaxDepthExceededBehavior("abort");

        return new CacheConfiguration(cacheId, 0)
            .eternal(true)
            .statistics(true)
            .sizeOfPolicy(sizeOfConfig)
            .overflowToOffHeap(false);
    }

    public LiveCacheStatistics getCacheStatistics() {
        if(cache.getStatus() == Status.STATUS_ALIVE) {
            return cache.getLiveCacheStatistics();
        }
        return null;
    }

    /**
     * Returns the cache corresponding to the cache id
     */
    private Cache getCache() {
        return  getCacheManager().getCache(cacheId);
    }

    /**
     * @return the singleton cacheManager
     */
    protected CacheManager getCacheManager() {

        CacheManager cacheManager = CacheManager.create();

        if(!managementBeansConfigured){
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            boolean registerCacheManager = false;
            boolean registerCaches = true;
            boolean registerCacheConfigurations = false;
            boolean registerCacheStatistics = true;
            ManagementService.registerMBeans(cacheManager, mBeanServer, registerCacheManager, registerCaches, registerCacheConfigurations, registerCacheStatistics);
            managementBeansConfigured = true;
        }

        return cacheManager;
    }

    public <T extends Object> T load(T obj, boolean update) {
        return cacheLoader.load(obj, true, update);
    }

    public <T extends Object> Map<T,T> load(Map<T,T> map, boolean update){
        return cacheLoader.load(map, true, update);
    }

    public <T extends Object> Collection<T> load(Collection<T> collection, boolean update){
        return cacheLoader.load(collection, true, update);
    }

    /**
     * Loads the {@link eu.etaxonomy.cdm.model.common.CdmBase cdmEntity}) graph recursively into the
     * cache.
     *
     * For in depth details on the whole mechanism see
     * {@link CacheLoader#load(CdmBase, boolean, boolean)},
     * {@link CacheLoader#loadRecursive(CdmBase, List, boolean)} and
     * {@link CacheLoader#getCdmBaseTypeFieldValue(CdmBase, CdmBase, String, List, boolean)}
     *
     * @param cdmEntity
     *            the entity to be put into the cache
     * @param update
     *            all fields of the cached entity will be overwritten by setting
     *            them to the value of the cdm entity being loaded
     * @return
     */
    public <T extends CdmBase> T load(T cdmEntity, boolean update) {
        return cacheLoader.load(cdmEntity, true, update);
    }

    public MergeResult<CdmBase> load(MergeResult<CdmBase> mergeResult, boolean update) {
        return cacheLoader.load(mergeResult, true, update);
    }

    public CdmModelFieldPropertyFromClass getFromCdmlibModelCache(String className) {
        return cacheLoader.getFromCdmlibModelCache(className);
    }

    private void putVolatitleEntity(CdmBase volatileEntity) {
        if(volatileEntity != null && volatileEntity.getId() == 0 && volatileEntity.getUuid() != null) {
            CdmBase cachedEntity = volatileEntitiesMap.get(volatileEntity.getUuid());
            if (cachedEntity == null){
                volatileEntitiesMap.put(volatileEntity.getUuid(), volatileEntity);
            }
        }
    }

    /**
     * Puts the passed <code>cdmEntity</code> into the according caches
     * (cache, newEntitiesMap, permanentCache(TODO still needs to be checked, not implemented yet))
     * as long it does not yet exist there.
     * <p>
     * The adjacent <b>ENTITY GRAPH WILL NOT BE LOADED RECURSIVELY</b>
     */
    @Override
    public void putToCache(CdmBase cdmEntity) {
        if (cdmEntity == null){
            return;
        }else if (!cdmEntity.isPersited()){
            putVolatitleEntity(cdmEntity);
        }else{
            CdmBase cachedCdmEntity = permanentCache.load(cdmEntity);
            if(cachedCdmEntity != null) {
                logger.info("Cdm Entity with id : " + cdmEntity.getId() + " already exists in permanent cache. Ignoring put.");
                return;
            }
            CdmEntityCacheKey<?> key = new CdmEntityCacheKey<>(cdmEntity);

            cachedCdmEntity = getFromCache(key);
            if(cachedCdmEntity == null) {
                CdmBase cdmEntityToCache = cdmEntity;
                CdmBase cachedVolatileEntity = volatileEntitiesMap.get(cdmEntity.getUuid());
                if(cachedVolatileEntity != null) {
                    cachedVolatileEntity.setId(cdmEntity.getId());
                    cdmEntityToCache = cachedVolatileEntity;
                }
                putToTransientCache(key, cdmEntityToCache);
                cdmEntityToCache.initListener();
                volatileEntitiesMap.remove(cdmEntity.getUuid());
                if (logger.isDebugEnabled()){logger.debug(" - object of type " + cdmEntityToCache.getClass().getName() + " with id " + cdmEntityToCache.getId() + " put in cache");}
                return;
            }else{
                logger.debug(" - object of type " + cdmEntity.getClass().getName() + " with id " + cdmEntity.getId() + " already exists");
            }
        }
    }

    /**
     * Puts the entity to the cache for transient entities. If the entity is not transient
     * but volatile (id = 0) an {@link IllegalArgumentException} is thrown
     */
    protected void putToTransientCache(CdmEntityCacheKey<?> key, CdmBase cdmEntityToCache) throws IllegalArgumentException {
        if (key.getPersistenceId() == 0){
            throw new IllegalArgumentException("Volatile objects are not allowed in the transient object cache. Use newEntitiesMap instead.");
        }
        getCache().put(new Element(key, cdmEntityToCache));
    }

    private Element getCacheElement(CdmEntityCacheKey<?> key) {
        return getCache().get(key);
    }

    public <T extends CdmBase> T getFromCache(CdmEntityCacheKey<T> id) {
        Element e = getCacheElement(id);

        if (e == null) {
            return null;
        } else {
            @SuppressWarnings("unchecked")
            T result = (T) e.getObjectValue();
            return result;
        }
    }

    public <T extends CdmBase> T getFromCache(Class<T> clazz, int id) {
        CdmEntityCacheKey<T> cacheId = generateKey(clazz, id);
        return getFromCache(cacheId);
    }

    @Override
    public <T extends CdmBase> T getFromCache(T cdmBase) {
        if (!cdmBase.isPersited()){
            return (T)volatileEntitiesMap.get(cdmBase.getUuid());
        }else{
            CdmEntityCacheKey<T> cacheId = generateKey(ProxyUtils.deproxy(cdmBase));
            // first try this cache
            T  cachedCdmEntity = getFromCache(cacheId);

            if(cachedCdmEntity == null) {
                // ... then try the permanent cache
                //TODO also use generics and clazz parameter for getFromCache(uuid)
                cachedCdmEntity = (T)permanentCache.getFromCache(cdmBase.getUuid());
            }
            return cachedCdmEntity;
        }

    }

    public List<CdmBase> getAllEntities() {
        List<CdmBase> entities = new ArrayList<>();
        Map<String, CdmBase> elementsMap = getCache().getAllWithLoader(getCache().getKeys(), null);
        for (Map.Entry<String, CdmBase> entry : elementsMap.entrySet()) {
            entities.add(entry.getValue());
        }
        return entities;
    }

    public boolean exists(CdmEntityCacheKey<?> key) {
        return (getCacheElement(key) != null);
    }

    public boolean existsAndIsNotNull(CdmEntityCacheKey<?> id) {
        return getFromCache(id) != null;
    }

    public void clear() {
        cache.removeAll();
        volatileEntitiesMap.clear();
    }

    @Override
    public void dispose() {
        getCacheManager().removeCache(cache.getName());
        cache.dispose();
        volatileEntitiesMap.clear();
    }

    @Override
    public <T extends CdmBase> T load(T cdmEntity) {
        return load(cdmEntity, true);
    }

    @Override
    public boolean isCachable(CdmBase cdmEntity) {
        return true;
    }

    @Override
    public boolean exists(CdmBase cdmBase) {
        return exists(generateKey(cdmBase));
    }
}
