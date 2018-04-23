/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.config;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.etaxonomy.cdm.api.cache.CdmCacher;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.CacheEventListenerFactoryConfiguration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 *
 * @author a.kohlbecker
 \* @since Feb 1, 2017
 *
 */
@Configuration
// @EnableCaching // for future use
public class EhCacheConfiguration implements DisposableBean {

    public static final Logger logger = Logger.getLogger(EhCacheConfiguration.class);

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
            logger.debug("creating CacheManager with disk store");
            conf.addDiskStore(diskStoreConfiguration);
        }
        conf.addDefaultCache(getDefaultCacheConfiguration());

        // creates a singleton
        cacheManager = CacheManager.create(conf);
        logger.debug("CacheManager created");
        return cacheManager;
    }


    /**
     * Returns the default cache configuration.
     *
     * @return
     */
    protected CacheConfiguration getDefaultCacheConfiguration() {

        CacheEventListenerFactoryConfiguration factory;

        // For a better understanding on how to size caches, refer to
        // http://ehcache.org/documentation/configuration/cache-size

        CacheConfiguration cc = new CacheConfiguration(CdmCacher.DEFAULT_CACHE_NAME, 500)
                .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
                .eternal(false)
                // default ttl and tti set to 2 hours
                .timeToLiveSeconds(60*60*2)
                .timeToIdleSeconds(60*60*2)
                .statistics(true);

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
