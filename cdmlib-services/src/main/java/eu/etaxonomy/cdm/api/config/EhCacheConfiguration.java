/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.etaxonomy.cdm.api.cache.CdmPermanentCacheBase;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * @author a.kohlbecker
 * @since Feb 1, 2017
 */
@Configuration
// @EnableCaching // for future use
public class EhCacheConfiguration implements DisposableBean {

    private static final Logger logger = LogManager.getLogger();

    @Autowired(required = false)
    public DiskStoreConfiguration diskStoreConfiguration = null;

    /**
     * The DiskStoreConfiguration can either be autowired or set explicite
     * @param diskStoreConfiguration the diskStoreConfiguration to set
     */
    public void setDiskStoreConfiguration(DiskStoreConfiguration diskStoreConfiguration) {
        this.diskStoreConfiguration = diskStoreConfiguration;
    }

    private CacheManager cacheManager = null;

    @Bean
    public CacheManager cacheManager(){

        net.sf.ehcache.config.Configuration conf = new net.sf.ehcache.config.Configuration();
        if(diskStoreConfiguration != null){
            if (logger.isDebugEnabled()) { logger.debug("creating CacheManager with disk store");}
            conf.addDiskStore(diskStoreConfiguration);
        }
        CacheConfiguration defaultConfig = getDefaultCacheConfiguration();
        conf.addDefaultCache(defaultConfig);

        //FIXME Caching by AM: setting the configuration does not work if the CacheManger
        //                     singleton exists already (like in the TaxEditor)
        // creates a singleton
        cacheManager = CacheManager.create(conf);
        if (logger.isDebugEnabled()) { logger.debug("CacheManager created");}
        return cacheManager;
    }

    /**
     * Returns the default cache configuration for the cache
     * named {@link CdmPermanentCacheBase#PERMANENT_CACHE_NAME "cdmDefaultCache"}
     */
    protected CacheConfiguration getDefaultCacheConfiguration() {

        // For a better understanding on how to size caches, refer to
        // http://ehcache.org/documentation/configuration/cache-size

        CacheConfiguration cc = new CacheConfiguration(CdmPermanentCacheBase.PERMANENT_CACHE_NAME, 500)
                .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
                .maxEntriesLocalHeap(10) // avoid ehache consuming too much heap
                .eternal(false)
                // default ttl and tti set to 2 hours
                .timeToLiveSeconds(60*60*2)
                .timeToIdleSeconds(60*60*2);

        return cc;
    }

    @Override
    public void destroy() {
        if (cacheManager != null) {
            logger.info("Shutting down EhCache CacheManager");
            this.cacheManager.shutdown();
        }
    }
}