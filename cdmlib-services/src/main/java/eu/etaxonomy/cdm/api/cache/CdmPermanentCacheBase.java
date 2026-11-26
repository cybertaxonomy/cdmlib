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

import eu.etaxonomy.cdm.api.cache.ehcache.CdmCache;
import eu.etaxonomy.cdm.api.cache.ehcache.CdmCacheConfiguration;
import eu.etaxonomy.cdm.api.cache.ehcache.CdmCacheConfiguration.EvictionHint;
import eu.etaxonomy.cdm.api.cache.ehcache.CdmEntityCacheManager;
import eu.etaxonomy.cdm.model.ICdmUuidCacher;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * CDM Entity Cacher class based on EhCache 3 (see {@link CdmEntityCacheManager})
 * using UUID as key. Null values are not cached.
 *
 * @author cmathew
 */
public abstract class CdmPermanentCacheBase implements ICdmUuidCacher {

    private static final Logger logger = LogManager.getLogger();

    public static final String PERMANENT_CACHE_NAME = "cdmPermanentCache"; //TODO or better call it cdmTermCache? cdmProxyCache?

    @Autowired
    public CdmEntityCacheManager cacheManager;

    /**
     * Constructor which initializes the cacher
     */
    public CdmPermanentCacheBase() {
        init();
    }

    /**
     * Sets itself as the cacher object in specific CDM Entity objects.
     */
    private void init() {
        setup();
    }

    protected abstract void setup();

    /**
     * Sets the cache manager if not yet set.
     */
    protected void setCacheManager(CdmEntityCacheManager cacheManager) {

        if(this.cacheManager == null){
            this.cacheManager = cacheManager;
        } else {
            logger.error("There is already a CacheManager configured.");
        }
    }

    /**
     * Returns the default cache configuration.
     */
    protected CdmCacheConfiguration getPermanentCacheConfiguration() {

        return CdmCacheConfiguration.newInstance(PERMANENT_CACHE_NAME)
                .maxHeapEntries(500)
                .evictionHint(EvictionHint.LFU)
                // default ttl and tti set to 2 hours
                .expiry(60*60*2, 60*60*2);
    }

    /**
     * Returns the permanent cache
     */
    public CdmCache getPermanentCache() {
        CdmCache permanentCache = cacheManager.getCache(PERMANENT_CACHE_NAME);
        if(permanentCache == null) {
            permanentCache = cacheManager.getOrCreate(getPermanentCacheConfiguration());
        }
        return permanentCache;
    }

    @Override
    public void dispose(){
        CdmCache permanentCache = cacheManager.getCache(PERMANENT_CACHE_NAME);
        if (permanentCache != null) {
            permanentCache.close();
        }
    }

    /**
     * Gets the cached entity corresponding to the given {@link java.util.UUID}
     * or <code>null</code> if there is none (or it is expired).
     */
    private CdmBase getCached(UUID uuid) {
        return (CdmBase)getPermanentCache().get(uuid);
    }

    @Override
    public  void put(UUID uuid, CdmBase cdmEntity) {
        CdmBase cachedCdmEntity = getFromCache(uuid);
        if(cachedCdmEntity == null) {
            getPermanentCache().put(uuid, cdmEntity);
        }
    }

    @Override
    public CdmBase load(UUID uuid) {
        CdmBase cdmEntity = getCached(uuid);

        if (cdmEntity == null) {
            // nothing in the cache for "key" (or expired) ... re-load the entity
            cdmEntity = findByUuid(uuid);
            // currently default cache is a non-null cache
            // We would like to have the possibility to put null values in the cache,
            // but we need to first distinguish between real null values and null values
            // returned by the service is the type of class does not match
            if(cdmEntity != null) {
                put(uuid, cdmEntity);
            }
        }
        return cdmEntity;
    }

    @Override
    public  CdmBase getFromCache(UUID uuid) {
        return getCached(uuid);
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
        return getPermanentCache().containsKey(uuid);
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
        return getCached(uuid) != null;
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
