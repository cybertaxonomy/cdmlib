/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Cache class for CDM Entities based on Ehcache.
 * The class manages a singleton {@link net.sf.ehcache.CacheManager} having a
 * {@link net.sf.ehcache.Cache} initialised with a default configuration.
 *
 * FIXME: This interface should actually be in an external project which also
 *        includes the implemented cachers.
 *
 * @author cmathew
 */
public interface ICdmUuidCacher extends ICdmCacher {

    /**
     * Puts the (Key,Value) pair of ({@link java.util.UUID}, {@link eu.etaxonomy.cdm.model.common.CdmBase}),
     * in the cache
     */
    public void put(UUID uuid, CdmBase cdmEntity);

	/**
	 * Load a CDM Entity object with given UUID.
	 * This method checks the (default) cache for the entity,
	 * else retrieves the entity from the service layer.
	 *
	 * NOTE : Currently this method can only be used for CDM Term
	 * (DefinedTermBase) entities.
	 *
	 * @param uuid of CDM Entity to return
	 */
	public CdmBase load(UUID uuid);

	/**
	 * Get a CDM Entity object with given UUID from the (default) cache.
	 *
	 * @param uuid
	 */
	public CdmBase getFromCache(UUID uuid);



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
