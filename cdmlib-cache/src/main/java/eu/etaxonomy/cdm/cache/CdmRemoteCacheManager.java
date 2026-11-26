/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.cache;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.api.cache.ehcache.CdmCache;
import eu.etaxonomy.cdm.api.cache.ehcache.CdmCacheConfiguration;
import eu.etaxonomy.cdm.api.cache.ehcache.CdmEntityCacheManager;

/**
 * By AM: A CacheManager wrapper singleton that starts the {@link CdmEntityCacheManager}
 *        and initializes the cdm model cache (see {@link CdmModelCacher} as
 *        cache handled by the cache manager. The model chache is configured as
 *        eternal, unbounded, in-Memory cache with name defined  by
 *        {@link CdmRemoteCacheManager#CDM_MODEL_CACHE_NAME}.
 *
 * @author c.matthew
 */
public class CdmRemoteCacheManager {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    public static final String CDM_MODEL_CACHE_NAME = "cdmModelGetMethodsCache";

    private static CdmRemoteCacheManager cdmRemoteCacheManager;

    private volatile CdmCache cdmModelCache;

    public static CdmRemoteCacheManager INSTANCE(){
        if(cdmRemoteCacheManager == null) {
            cdmRemoteCacheManager = new CdmRemoteCacheManager();
        }
        return cdmRemoteCacheManager;
    }

    private CdmRemoteCacheManager() {
        initModelCache();
    }

    private void initModelCache() {
        try {
            // eternal and never evicting, the model map is the same for the whole lifetime
            CdmCacheConfiguration cdmModelCacheConfiguration = CdmCacheConfiguration.newInstance(CDM_MODEL_CACHE_NAME)
                    .eternal()
                    .unboundedHeap();

            cdmModelCache = CdmEntityCacheManager.getInstance().createCache(cdmModelCacheConfiguration);
            CdmModelCacher cmdmc = new CdmModelCacher();
            cmdmc.cacheGetterFields(cdmModelCache);

        } catch (RuntimeException | ClassNotFoundException | IOException e) {
            throw new CdmClientCacheException(e);
        }
    }

    public CdmCache getCdmModelGetMethodsCache(){
        CdmCache result = cdmModelCache;
        if (!result.isAlive()) {
            //the cache manager was shut down in the meanwhile
            synchronized (this) {
                if (!cdmModelCache.isAlive()) {
                    initModelCache();
                }
                result = cdmModelCache;
            }
        }
        return result;
    }

    public static void removeEntityCaches() {
        CdmEntityCacheManager cm = CdmEntityCacheManager.getInstance();
        for(String cacheName : cm.getCacheNames()) {
            if(!cacheName.equals(CDM_MODEL_CACHE_NAME)) {
                cm.removeCache(cacheName);
            }
        }
    }
}