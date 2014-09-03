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
		// We start first only with DefinedTermBase
		DefinedTermBase.setCacher(this);

		initDefaultTerms();
	}

	/**
	 * Initialises any default terms if required.
	 * This method should be implemented in sub-classes if any
	 * pre-initialisation is necessary.
	 */
	protected void initDefaultTerms() {}

	/**
	 * Returns the singleton default cache manager.
	 *
	 * @return
	 */
	private static CacheManager getDefaultCacheManager() {
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
	 * Puts the (Key,Value) pair of ({@link java.util.UUID}, {@link eu.etaxonomy.cdm.model.common.CdmBase}),
	 * in the cache
	 *
	 * @param uuid
	 * @param cdmEntity
	 */
	public <T extends CdmBase> void put(UUID uuid, T cdmEntity) {
		getDefaultCache().put(new Element(uuid, cdmEntity));
	}

	/**
	 * Gets the cache element corresponding to the given {@link java.util.UUID}
	 *
	 * @param uuid
	 * @return
	 */
	private Element getCacheElement(UUID uuid) {
		return getDefaultCache().get(uuid);
	}

	/**
	 * Get CDM Entity for given {@link java.util.UUID} from the cache
	 *
	 * @param uuid
	 * @return
	 */
	private T getCdmEntity(UUID uuid) {
		return  (T)getDefaultCache().get(uuid).getObjectValue();
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
			put(uuid, cdmEntity);
		} else {
		    // there is a valid element in the cache, however getObjectValue() may be null
		    cdmEntity = (T)e.getObjectValue();
		}
		return cdmEntity;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.ICdmCacher#getFromCache(java.util.UUID)
	 */
	@Override
    public T getFromCache(UUID uuid) {
		Element e = getCacheElement(uuid);
		T cdmEntity;
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
