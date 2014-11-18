package eu.etaxonomy.cdm.model.common.init;

import java.util.Set;
import java.util.UUID;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import eu.etaxonomy.cdm.model.ICdmCacher;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * Since cdmlib-model cannot access CdmCacher we need to create a mock class
 * for the tests.
 *
 * NOTES:
 *      - All terms are put into the cache in the constructor
 *      - The number of elements allowed in the cache is set to a big number - 10000
 *
 * FIXME : Once the CDMCacher is externalised this class should just subclass it.
 *
 * @author cmathew
 *
 */
public class MockCdmCacher implements ICdmCacher {

	private static final String DEFAULT_CACHE_NAME = "defaultCache";

	private static final String DEFAULT_CACHE_MGR_NAME = "defaultCacheManager";
	/**
	 * Constructor which initialises a singleton {@link net.sf.ehcache.CacheManager}
	 *
	 */
	public MockCdmCacher() {
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
		Set<Language> langList = Language.DEFAULT().getVocabulary().getTerms();
		for (Language lang : langList){
			put(lang.getUuid(),lang);
		}
		// We start first only with DefinedTermBase
		DefinedTermBase.setCacher(this);

	}

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
		return new CacheConfiguration(DEFAULT_CACHE_NAME, 10000)
	    .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
	    .eternal(false)
	    // default ttl and tti set to 2 hours
	    .timeToLiveSeconds(60*60*2)
	    .timeToIdleSeconds(60*60*2);

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
	 * @return
	 */
	@Override
    public CdmBase put(UUID uuid, CdmBase cdmEntity) {
	    CdmBase cachedCdmEntity = getFromCache(uuid);
        if(getFromCache(uuid) == null) {
            getDefaultCache().put(new Element(uuid, cdmEntity));
            return cdmEntity;
        } else {
            return cachedCdmEntity;
        }
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
	private CdmBase getCdmEntity(UUID uuid) {
		return  (CdmBase)getDefaultCache().get(uuid).getObjectValue();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.cache.ICdmCacher#load(java.util.UUID)
	 */

	@Override
    public CdmBase load(UUID uuid) {
		Element e = getCacheElement(uuid);
		CdmBase cdmEntity;
		if (e == null) {
			return null;
		} else {
		    return (CdmBase)e.getObjectValue();
		}
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.ICdmCacher#getFromCache(java.util.UUID)
	 */
	@Override
    public CdmBase getFromCache(UUID uuid) {
		Element e = getCacheElement(uuid);
		CdmBase cdmEntity;
		if (e == null) {
			return null;
		} else {
		    return(CdmBase)e.getObjectValue();
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
		CdmBase cdmEntity;
		if (e != null) {
			return (CdmBase)e.getObjectValue() != null;
		}
		return false;
	}

}
