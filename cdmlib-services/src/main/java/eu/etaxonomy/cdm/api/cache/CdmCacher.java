package eu.etaxonomy.cdm.api.cache;

import java.util.UUID;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import eu.etaxonomy.cdm.model.ICdmCacher;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;

/**
 * CDM Entity Cacher class based on EhCache.
 * The cacher allows null values to be cached.
 *
 * @author cmathew
 *
 * @param <T>
 */

public abstract class CdmCacher<T extends CdmBase> implements ICdmCacher<T> {



	private static final String DEFAULT_CACHE_NAME = "defaultCache";

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
		// Remove all caches
		getDefaultCacheManager().removalAll();
		// Create default cache
		getDefaultCacheManager().addCache(new Cache(getDefaultCacheConfiguration()));

		setup();
	}

	protected abstract void setup();

	/**
	 * Returns the singleton default cache manager.
	 *
	 * @return
	 */
	public static CacheManager getDefaultCacheManager() {
		// this ensures a singleton cache manager
		return CacheManager.create();
	}

	/**
	 * Returns the default cache configuration.
	 *
	 * @return
	 */
	private CacheConfiguration getDefaultCacheConfiguration() {
		// For a better understanding on how to size caches, refer to
		// http://ehcache.org/documentation/configuration/cache-size
		return new CacheConfiguration(DEFAULT_CACHE_NAME, 500)
	    .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
	    .eternal(false)
	    // default ttl and tti set to 2 hours
	    .timeToLiveSeconds(60*60*2)
	    .timeToIdleSeconds(60*60*2);
	    // This is 2.6.9 API
		//.maxEntriesLocalDisk(1000);
	    //.persistence(new PersistenceConfiguration().strategy(Strategy.LOCALTEMPSWAP));
	}

	/**
	 * Returns the default cache
	 *
	 * @return
	 */
	private static Cache getDefaultCache() {
		return getDefaultCacheManager().getCache(DEFAULT_CACHE_NAME);
	}


	/**
	 * Gets the cache element corresponding to the given {@link java.util.UUID}
	 *
	 * @param uuid
	 * @return
	 */
	protected Element getCacheElement(UUID uuid) {
		return getDefaultCache().get(uuid);
	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.ICdmCacher#put(java.util.UUID, eu.etaxonomy.cdm.model.common.CdmBase)
     */
    @Override
    public  T put(UUID uuid, T cdmEntity) {
        T cachedCdmEntity = getFromCache(uuid);
        if(getFromCache(uuid) == null) {
            getDefaultCache().put(new Element(uuid, cdmEntity));
            return cdmEntity;
        } else {
            return cachedCdmEntity;
        }
    }

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.cache.ICdmCacher#load(java.util.UUID)
	 */
	@Override
    public T load(UUID uuid) {
		Element e = getCacheElement(uuid);
		T cdmEntity;
		if (e == null) {

		    // nothing in the cache for "key" (or expired) ... re-load the entity
			cdmEntity = findByUuid(uuid);
			// default cache is a non-null cache
			if(cdmEntity != null) {
			    put(uuid, cdmEntity);
			}
		} else {
		    // there is a valid element in the cache, however getObjectValue() may be null
		    cdmEntity = (T)e.getValue();
		}
		return cdmEntity;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.ICdmCacher#getFromCache(java.util.UUID)
	 */
	@Override
    public  T getFromCache(UUID uuid) {
		Element e = getCacheElement(uuid);
		if (e == null) {
			return null;
		} else {
		    return(T)e.getObjectValue();
		}
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.ICdmCacher#exists(java.util.UUID)
	 */
	@Override
    public boolean exists(UUID uuid) {
		return getCacheElement(uuid) != null;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.ICdmCacher#existsAndIsNotNull(java.util.UUID)
	 */
	@Override
    public boolean existsAndIsNotNull(UUID uuid) {
		Element e = getCacheElement(uuid);
		T cdmEntity;
		if (e != null) {
			return (T)e.getObjectValue() != null;
		}
		return false;
	}

	/**
	 * Finds CDM Entity by uuid
	 *
	 * @param uuid
	 * @return
	 */
	protected abstract T findByUuid(UUID uuid);



}
