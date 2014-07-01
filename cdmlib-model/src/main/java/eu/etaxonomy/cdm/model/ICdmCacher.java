package eu.etaxonomy.cdm.model;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author cmathew
 *
 * Cache class for CDM Entities based on Ehcache.
 * The class manages a singleton {@link net.sf.ehcache.CacheManager} having a 
 * {@link net.sf.ehcache.Cache} initialised with a default configuration.
 * 
 * @param <T>
 */
public interface ICdmCacher<T extends CdmBase> {

	/**
	 * Load a CDM Entity object with given UUID.
	 * This method checks the (default) cache for the entity, 
	 * else retrieves the entity from the service layer.
	 * 
	 * NOTE : Currently this method can only be used for CDM Term
	 * (DefinedTermBase) entities.
	 * 
	 * @param uuid of CDM Entity to return
	 * @return
	 */
	public T load(UUID uuid);
	
	/**
	 * Get a CDM Entity object with given UUID from the (default) cache.
	 * 
	 * @param uuid
	 * @return
	 */
	public T getFromCache(UUID uuid);
	
	/**
	 * Check if a CDM Entity with given UUID exists in the cache.
	 * 
	 * @param uuid of CDM Entity to check
	 * @return true if CDM Entity with given UUID exists in the cache, o/w false 
	 */
	public boolean exists(UUID uuid);
	
	/**
	 * Check if a CDM Entity with given UUID exists in the cache
	 * and that it is not null.
	 * 
	 * @param uuid of CDM Entity to check
	 * @return true if CDM Entity with given UUID exists in the cache and that it is not null, o/w false 
	 */
	public boolean existsAndIsNotNull(UUID uuid);

}