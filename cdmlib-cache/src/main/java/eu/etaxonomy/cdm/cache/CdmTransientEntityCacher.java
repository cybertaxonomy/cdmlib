/**
 * Copyright (C) 2014 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import net.sf.ehcache.statistics.LiveCacheStatistics;

/**
 *
 * This cache guarantees that
 *  - all objects put will be ancestors of CdmBase
 *  - all CdmBase objects in the cache will be already de-proxied
 *  - after any CdmBase object is put in the cache,
 *  all non-null / non-proxy CdmBase objects in the sub-graph
 *  will also be present in the cache.
 *
 * @author cmathew
 * @date 14 Oct 2014
 *
 */

public class CdmTransientEntityCacher implements ICdmCacher {

    private static final Logger logger = Logger.getLogger(CdmTransientEntityCacher.class);


    // removed since unused ########################
    // private final eu.etaxonomy.cdm.session.ICdmEntitySessionManager cdmEntitySessionManager;

    /**
     * permanent cache which is usually used to cache terms permanently
     * FIXME rename to permanent cache
     */
    private static CdmCacher cdmCacher;

    private final String cacheId;

    private final Cache cache;

    private final CacheLoader cacheLoader;

    private final Map<UUID, CdmBase> newEntitiesMap = new HashMap<UUID, CdmBase>();

    public CdmTransientEntityCacher(String cacheId) {
        this.cacheId = cacheId;

        cache = new Cache(getEntityCacheConfiguration(cacheId));

        CacheManager.create().removeCache(cache.getName());
        CacheManager.create().addCache(cache);

        // removed since unused ########################
        // this.cdmEntitySessionManager = cdmEntitySessionManager;

        cacheLoader = new CacheLoader(this);

    }

    public CdmTransientEntityCacher(Object sessionOwner) {
        this(generateCacheId(sessionOwner));
    }

    public static String generateCacheId(Object sessionOwner) {
        return sessionOwner.getClass().getName() +  String.valueOf(sessionOwner.hashCode());
    }

    /**
     * Returns the default cache configuration.
     *
     * @return
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

    public static void setDefaultCacher(CdmCacher css) {
        cdmCacher = css;
    }

    public LiveCacheStatistics getCacheStatistics() {
        if(cache.getStatus() == Status.STATUS_ALIVE) {
            return cache.getLiveCacheStatistics();
        }
        return null;

    }

    /**
     * Returns the cache corresponding to the cache id
     *
     * @param cacheId
     * @return
     */
    private Cache getCache() {
        return  CacheManager.create().getCache(cacheId);
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
    public CdmBase load(CdmBase cdmEntity, boolean update) {
        return cacheLoader.load(cdmEntity, true, update);
    }


    /* ################### to be implemented by subclass in taxeditor ########################
    private CdmBase load(CdmEntityIdentifier cei, boolean update) {
        return CdmApplicationState.getCommonService().findWithUpdate(cei.getCdmClass(), cei.getId());
    }


    public UpdateResult load(UpdateResult result, boolean update) {
        // probably a good time to broadcast to other sessions

        Set<CdmBase> updatedObjects = result.getUpdatedObjects();
        Set<CdmBase> reloadedObjects = new HashSet<CdmBase>();
        Set<CdmEntityIdentifier> updatedCdmIds = result.getUpdatedCdmIds();
        boolean updatedCdmIdsIsEmpty = updatedCdmIds.isEmpty();

        // if the cdm identifier set contains identifiers of objects already
        // present in the updated objects set reomve them
        for(CdmBase updatedObject : updatedObjects) {
            if(updatedObject != null && exists(new CdmEntityCacheKey(updatedObject.getClass(), updatedObject.getId()))) {
                CdmEntityIdentifier cdmEntityIdentifier = new CdmEntityIdentifier(updatedObject.getId(), updatedObject.getClass());
                if(!updatedCdmIdsIsEmpty && updatedCdmIds.contains(cdmEntityIdentifier)) {
                    updatedCdmIds.remove(cdmEntityIdentifier);
                }
                reloadedObjects.add(cacheLoader.load(updatedObject, true, update));
            }
        }

        // remote load cdm identifiers of objects which already exist
        // in the cache

        for(CdmEntityIdentifier cei : updatedCdmIds) {
            if(exists(new CdmEntityCacheKey(cei.getCdmClass(), cei.getId()))) {
                reloadedObjects.add(load(cei, update));
            }

        }
        updatedObjects.clear();
        result.addUpdatedObjects(reloadedObjects);
        return result;
    }
    */

    public MergeResult<CdmBase> load(MergeResult<CdmBase> mergeResult, boolean update) {
        return cacheLoader.load(mergeResult, true, update);
    }

    public CdmModelFieldPropertyFromClass getFromCdmlibModelCache(String className) {
        return cacheLoader.getFromCdmlibModelCache(className);
    }


    public void addNewEntity(CdmBase newEntity) {
        if(newEntity != null && newEntity.getId() == 0 && newEntity.getUuid() != null) {
            newEntitiesMap.put(newEntity.getUuid(), newEntity);
        }
    }

    /**
     * Puts the passed <code>cdmEntity</code> into the cache as long it does not yet exist in the caches.
     */
    @Override
    public void put(CdmBase cdmEntity) {

        CdmBase cachedCdmEntity = cdmCacher.load(cdmEntity);
        if(cachedCdmEntity != null) {
            logger.info("Cdm Entity with id : " + cdmEntity.getId() + " already exists in permanent cache. Ignoring put.");
            return;
        }
        CdmEntityCacheKey id = new CdmEntityCacheKey(cdmEntity);

        cachedCdmEntity = getFromCache(id);
        if(cachedCdmEntity == null) {
            CdmBase cdmEntityToCache = cdmEntity;
            CdmBase newEntity = newEntitiesMap.get(cdmEntity.getUuid());
            if(newEntity != null) {
                newEntity.setId(cdmEntity.getId());
                cdmEntityToCache = newEntity;
            }
            getCache().put(new Element(id, cdmEntityToCache));
            cdmEntityToCache.initListener();
            newEntitiesMap.remove(cdmEntity.getUuid());
            logger.info(" - object of type " + cdmEntityToCache.getClass().getName() + " with id " + cdmEntityToCache.getId() + " put in cache");
            return;
        }
        logger.info(" - object of type " + cdmEntity.getClass().getName() + " with id " + cdmEntity.getId() + " already exists");
    }


    private Element getCacheElement(CdmEntityCacheKey key) {
        return getCache().get(key);
    }


    public CdmBase getFromCache(CdmEntityCacheKey id) {
        Element e = getCacheElement(id);

        if (e == null) {
            return null;
        } else {
            return (CdmBase) e.getObjectValue();
        }
    }

    public CdmBase getFromCache(Class<? extends CdmBase> clazz, int id) {
        CdmEntityCacheKey cacheId = generateKey(clazz,id);
        return getFromCache(cacheId);
    }

    @Override
    public CdmBase getFromCache(CdmBase cdmBase) {

        CdmEntityCacheKey cacheId = generateKey((CdmBase)ProxyUtils.deproxy(cdmBase));
        // first try this cache
        CdmBase  cachedCdmEntity = getFromCache(cacheId);

        if(cachedCdmEntity == null) {
            // ... then try the permanent cache
            cachedCdmEntity = cdmCacher.getFromCache(cdmBase.getUuid());
        }

        return cachedCdmEntity;
    }

    public CdmBase getFromCache(CdmBase cdmBase, Class<? extends CdmBase> clazz) {

        cdmBase = CdmBase.deproxy(cdmBase, clazz);
        return getFromCache(cdmBase);
    }

    public List<CdmBase> getAllEntities() {
        List<CdmBase> entities = new ArrayList<CdmBase>();
        Map<String, CdmBase> elementsMap = getCache().getAllWithLoader(getCache().getKeys(), null);
        for (Map.Entry<String, CdmBase> entry : elementsMap.entrySet()) {
            entities.add(entry.getValue());
        }
        return entities;
    }

    public boolean exists(CdmEntityCacheKey key) {
        return (getCacheElement(key) != null);
    }

    public boolean existsAndIsNotNull(CdmEntityCacheKey id) {
        return getFromCache(id) != null;
    }

    public void clear() {
        cache.removeAll();
    }

    public void dispose() {
        CacheManager.create().removeCache(cache.getName());
        cache.dispose();
        newEntitiesMap.clear();

    }


    public static CdmEntityCacheKey generateKey(Class<? extends CdmBase> clazz, int id) {
        return new CdmEntityCacheKey(clazz, id);
    }


    public static CdmEntityCacheKey generateKey(CdmBase cdmBase) {
        Class<? extends CdmBase> entityClass = cdmBase.getClass();
        int id = cdmBase.getId();
        return new CdmEntityCacheKey(entityClass, id);
    }

    @Override
    public CdmBase load(CdmBase cdmEntity) {
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
