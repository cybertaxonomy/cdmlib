/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.ICdmEntityUuidCacher;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Complements the CdmTransientEntityCacher by the ability to get CDM entities
 * from the cache by the UUID.
 * <p>
 * Internally a Map is used to store the entity UUID together with the CdmEntityCacheKey
 * of each entity being put into the cache.
 *
 * @author a.kohlbecker
 * @since Oct 14, 2018
 */
public class CdmTransientEntityAndUuidCacher extends CdmTransientEntityCacher implements ICdmEntityUuidCacher {

    private Map<UUID, CdmEntityCacheKey<?>> uuidKeyMap = new HashMap<>();

    public CdmTransientEntityAndUuidCacher(String cacheId) {
        super(cacheId);
    }

    public CdmTransientEntityAndUuidCacher(Object sessionOwner) {
        super(sessionOwner);
    }

    @Override
    public CdmBase getFromCache(UUID uuid){
        return getFromCache(uuidKeyMap.get(uuid));
    }

    @Override
    protected void putToTransientCache(CdmEntityCacheKey<?> key, CdmBase cdmEntityToCache) {
        super.putToTransientCache(key, cdmEntityToCache);
        uuidKeyMap.put(cdmEntityToCache.getUuid(), key);
    }

    @Override
    public void dispose() {
        super.dispose();
        uuidKeyMap.clear();
    }
}
