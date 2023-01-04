/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.cache;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.ICdmUuidCacher;
import eu.etaxonomy.cdm.model.common.CdmBase;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * CDM Entity Cacher class based on EhCache using UUID as key.
 * The cacher allows null values to be cached.
 *
 * @author cmathew
 */
public abstract class CdmCacherBase implements ICdmUuidCacher {

    private static final Logger logger = LogManager.getLogger();

    public static final String DEFAULT_CACHE_NAME = "cdmEntityDefaultCache"; //TODO compare with CacheConfiguration where the name for the default cache is 'default', Why another name here?

    @Autowired
    public CacheManager cacheManager;

    /**
     * Constructor which initializes a singleton {@link net.sf.ehcache.CacheManager}
     * if not yet initialzed
     */
    public CdmCacherBase() {
        init();
    }

    /**
     * Initializes an empty singleton {@link net.sf.ehcache.CacheManager} and
     * sets itself as the cacher object in specific CDM Entity objects.
     */
    private void init() {
        setup();
    }

    protected abstract void setup();

    /**
     * Returns the singleton default cache manager.
     */
    protected void setCacheManager(CacheManager cacheManager) {

        if(this.cacheManager == null){
            this.cacheManager = cacheManager;
        } else {
            logger.error("There is already a CacheManager configured.");
        }
    }

    /**
     * Returns the default cache configuration.
     */
    protected CacheConfiguration getDefaultCacheConfiguration() {

        // For a better understanding on how to size caches, refer to
        // http://ehcache.org/documentation/configuration/cache-size

        CacheConfiguration cc = new CacheConfiguration(DEFAULT_CACHE_NAME, 500)
                .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
                .eternal(false)
                // default ttl and tti set to 2 hours
                .timeToLiveSeconds(60*60*2)
                .timeToIdleSeconds(60*60*2);

        return cc;
    }

    /**
     * Returns the default cache
     */
    public Cache getDefaultCache() {
        Cache defaultCache = cacheManager.getCache(DEFAULT_CACHE_NAME);
        if(defaultCache == null) {
            // Create default cache
            cacheManager.addCache(DEFAULT_CACHE_NAME);
            defaultCache = cacheManager.getCache(DEFAULT_CACHE_NAME);
            //FIXME write test to check if default config as defined in EhCacheConfiguration is being used
        }
        return defaultCache;
    }

    @Override
    public void dispose(){
        cacheManager.getCache(DEFAULT_CACHE_NAME).dispose();
    }

    /**
     * Gets the cache element corresponding to the given {@link java.util.UUID}
     */
    public Element getCacheElement(UUID uuid) {
        return getDefaultCache().get(uuid);
    }

    @Override
    public  void put(UUID uuid, CdmBase cdmEntity) {
        CdmBase cachedCdmEntity = getFromCache(uuid);
        if(cachedCdmEntity == null) {
            getDefaultCache().put(new Element(uuid, cdmEntity));
        }
    }

    @Override
    public CdmBase load(UUID uuid) {
        Element e = getCacheElement(uuid);

        CdmBase cdmEntity;
        if (e == null) {
            // nothing in the cache for "key" (or expired) ... re-load the entity
            cdmEntity = findByUuid(uuid);
            // currently default cache is a non-null cache
            // We would like to have the possibility to put null values in the cache,
            // but we need to first distinguish between real null values and null values
            // returned by the service is the type of class does not match
            if(cdmEntity != null) {
                put(uuid, cdmEntity);
            }
        } else {
            // there is a valid element in the cache, however getObjectValue() may be null
            cdmEntity = (CdmBase)e.getObjectValue();
        }
        return cdmEntity;
    }

    @Override
    public  CdmBase getFromCache(UUID uuid) {
        Element e = getCacheElement(uuid);
        if (e == null) {
            return null;
        } else {
            return(CdmBase)e.getObjectValue();
        }
    }

    @Override
    public <T extends CdmBase> T getFromCache(T cdmBase) {
        return (T)getFromCache(cdmBase.getUuid());
    }

    @Override
    public void putToCache(CdmBase cdmEntity) {
        if(cdmEntity != null) {
            put(cdmEntity.getUuid(), cdmEntity);
        }
    }

    @Override
    public abstract <T extends CdmBase> T load(T cdmEntity);


    @Override
    public boolean exists(UUID uuid) {
        return getCacheElement(uuid) != null;
    }

    @Override
    public boolean exists(CdmBase cdmBase) {
        if(cdmBase != null) {
            return exists(cdmBase.getUuid());
        }
        return false;
    }

    @Override
    public boolean existsAndIsNotNull(UUID uuid) {
        Element e = getCacheElement(uuid);
        if (e != null) {
            return e.getObjectValue() != null;
        }
        return false;
    }

    /**
     * Finds CDM Entity by uuid
     */
    protected abstract CdmBase findByUuid(UUID uuid);

    @Override
    public boolean ignoreRecursiveLoad(CdmBase cdmBase){
        return false;
    }
}
