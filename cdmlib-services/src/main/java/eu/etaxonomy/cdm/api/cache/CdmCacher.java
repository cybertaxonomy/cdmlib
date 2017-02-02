package eu.etaxonomy.cdm.api.cache;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.ICdmUuidCacher;
import eu.etaxonomy.cdm.model.common.CdmBase;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.CacheEventListenerFactoryConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * CDM Entity Cacher class based on EhCache.
 * The cacher allows null values to be cached.
 *
 * @author cmathew
 *
 * @param <T>
 */

public abstract class CdmCacher implements ICdmUuidCacher {

    @Autowired
    public CacheManager cacheManager;

    public static final String DEFAULT_CACHE_NAME = "defaultCache"; //TODO compare with CacheConfiguration where the name for the default cache is 'default', Why another name here?

    /**
     * Constructor which initialises a singleton {@link net.sf.ehcache.CacheManager}
     *
     */
    public CdmCacher() {
        init();
    }

    /**
     * Initialises an empty singleton {@link net.sf.ehcache.CacheManager} and
     * sets itself as the cacher object in specific CDM Entity objects.
     *
     */
    private void init() {
        setup();
    }

    protected abstract void setup();

    /**
     * Returns the singleton default cache manager.
     * @param conf
     *
     * @return
     */
//    public static CacheManager getDefaultCacheManager() {
//        // this ensures a singleton cache manager
//
//        return CacheManager.create();
//    }

    /**
     * Returns the default cache configuration.
     *
     * @return
     */
    protected CacheConfiguration getDefaultCacheConfiguration() {
        CacheEventListenerFactoryConfiguration factory;
        // For a better understanding on how to size caches, refer to
        // http://ehcache.org/documentation/configuration/cache-size

        CacheConfiguration cc = new CacheConfiguration(DEFAULT_CACHE_NAME, 500)
                .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
                .eternal(false)
                // default ttl and tti set to 2 hours
                .timeToLiveSeconds(60*60*2)
                .timeToIdleSeconds(60*60*2)
                .statistics(true);

        return cc;
    }

    /**
     * Returns the default cache
     *
     * @return
     */
    public Cache getDefaultCache() {
        Cache defaultCache = cacheManager.getCache(DEFAULT_CACHE_NAME);
        if(defaultCache == null) {
            // Create default cache
            cacheManager.addCache(DEFAULT_CACHE_NAME);
            //FIXME write test to check if default config as defined in EhCacheConfiguration is being used
        }
        return defaultCache;
    }

    /**
     * Gets the cache element corresponding to the given {@link java.util.UUID}
     *
     * @param uuid
     * @return
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
    public CdmBase getFromCache(CdmBase cdmBase) {
        return getFromCache(cdmBase.getUuid());
    }


    @Override
    public void put(CdmBase cdmEntity) {
        if(cdmEntity != null) {
            put(cdmEntity.getUuid(), cdmEntity);
        }
    }

    @Override
    public abstract CdmBase load(CdmBase cdmEntity);


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
        CdmBase cdmEntity;
        if (e != null) {
            return e.getObjectValue() != null;
        }
        return false;
    }

    /**
     * Finds CDM Entity by uuid
     *
     * @param uuid
     * @return
     */
    protected abstract CdmBase findByUuid(UUID uuid);

}
