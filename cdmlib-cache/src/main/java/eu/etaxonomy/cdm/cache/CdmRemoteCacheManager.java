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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.SizeOfPolicyConfiguration;

public class CdmRemoteCacheManager {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private Cache cdmModelCache;

    private static CdmRemoteCacheManager cdmRemoteCacheManager = null;

    public static final String CDM_MODEL_CACHE_NAME = "cdmModelGetMethodsCache";

    private static Thread initThread;

    private static boolean cacheInitialised = false;

    public static CdmRemoteCacheManager getInstance(){
        if(cdmRemoteCacheManager == null) {
            cdmRemoteCacheManager = new CdmRemoteCacheManager();
        }
        return cdmRemoteCacheManager;
    }

    private CdmRemoteCacheManager() {

        try {
            // NOTE:Programmatically creating the cache manager may solve the problem of
            //      recreating data written to disk on startup
            //      see https://stackoverflow.com/questions/1729605/ehcache-persist-to-disk-issues
            //String cacheFilePath = CDMLIB_CACHE_MANAGER_CONFIG_RESOURCE.getFile().getAbsolutePath();
            //InputStream in = this.getClass().getClassLoader().getResourceAsStream("cdmlib-ehcache.xml");

            SizeOfPolicyConfiguration sizeOfConfig = new SizeOfPolicyConfiguration();
            sizeOfConfig.setMaxDepth(1000);
            sizeOfConfig.setMaxDepthExceededBehavior("abort");

            CacheConfiguration cdmModelCacheConfiguration = new CacheConfiguration(CDM_MODEL_CACHE_NAME, 0)
                    .eternal(true)
                    .statistics(true)
                    .sizeOfPolicy(sizeOfConfig)
                    .overflowToOffHeap(false);

            cdmModelCache = new Cache(cdmModelCacheConfiguration);

            CacheManager.create().addCache(cdmModelCache);
            CdmModelCacher cmdmc = new CdmModelCacher();
            cmdmc.cacheGetterFields(cdmModelCache);

        } catch (CacheException | ClassNotFoundException | IOException e) {
            throw new CdmClientCacheException(e);
        }
    }

    public Cache getCdmModelGetMethodsCache(){
        return cdmModelCache;
    }

    public static void removeEntityCaches() {
        CacheManager cm = CacheManager.create();
        String[] cacheNames = CacheManager.create().getCacheNames();
        for(String cacheName : cacheNames) {
            if(!cacheName.equals(CDM_MODEL_CACHE_NAME)) {
                cm.removeCache(cacheName);
            }
        }
    }
}