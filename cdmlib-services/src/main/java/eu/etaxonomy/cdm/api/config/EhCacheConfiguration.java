/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.etaxonomy.cdm.api.cache.CdmCacher;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.CacheEventListenerFactoryConfiguration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * @author a.kohlbecker
 * @date Feb 1, 2017
 *
 */
@Configuration
@EnableCaching
public class EhCacheConfiguration {

    @Autowired(required = true)
    public DiskStoreConfiguration diskStoreConfiguration = null;

    @Bean
    public CacheManager cacheManager(){

        net.sf.ehcache.config.Configuration conf = new net.sf.ehcache.config.Configuration();
        if(diskStoreConfiguration != null){
            conf.addDiskStore(diskStoreConfiguration);
        }
        conf.addDefaultCache(getDefaultCacheConfiguration());

        // creates a singleton
        CacheManager mgr = CacheManager.create(conf);

        return mgr;
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

}
