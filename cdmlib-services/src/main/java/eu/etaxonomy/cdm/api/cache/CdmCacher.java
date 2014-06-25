package eu.etaxonomy.cdm.api.cache;

import java.util.UUID;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.ICdmCacher;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;

/**
 * @author cmathew
 *
 * @param <T>
 */
@Component
public class CdmCacher<T extends CdmBase> implements ICdmCacher<T> {
			
	@Autowired
	private ITermService termService;
	
	private static final String DEFAULT_CACHE_NAME = "defaultCache";
	
	private static final String DEFAULT_CACHE_MGR_NAME = "defaultCacheManager";

	
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
		return new CacheConfiguration(DEFAULT_CACHE_NAME, 50)
	    .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
	    .eternal(false)
	    // default ttl and tti set to 2 hours
	    .timeToLiveSeconds(60*60*2)
	    .timeToIdleSeconds(60*60*2)
	    .diskExpiryThreadIntervalSeconds(0)
	    .persistence(new PersistenceConfiguration().strategy(Strategy.LOCALTEMPSWAP));
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
	private void put(UUID uuid, T cdmEntity) {
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
	
	public T load(UUID uuid) {
		Element e = getCacheElement(uuid);			
		T cdmEntity;
		if (e == null) {

		    // nothing in the cache for "key" (or expired) ... re-load the entity
			cdmEntity = (T)termService.find(uuid);
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
	public boolean exists(UUID uuid) {
		return getCacheElement(uuid) != null;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.ICdmCacher#existsAndIsNotNull(java.util.UUID)
	 */
	public boolean existsAndIsNotNull(UUID uuid) {
		Element e = getCacheElement(uuid);			
		T cdmEntity;
		if (e != null) {
			return (T)e.getObjectValue() != null;		   
		} 
		return false;
	}
	


}
