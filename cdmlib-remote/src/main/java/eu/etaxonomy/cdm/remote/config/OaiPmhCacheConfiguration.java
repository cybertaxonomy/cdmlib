/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.etaxonomy.cdm.remote.cache.ResumptionTokenCache;

/**
 * OAI-PMH resumption-token cache on Spring Cache + Ehcache 3 (JCache).
 * Kept separate from the legacy Ehcache 2 {@code cacheManager} bean used by
 * entity caching until that stack is migrated.
 *
 * @author a.mueller
 * @since 26.11.2026
 */
@Configuration
public class OaiPmhCacheConfiguration {

    public static final String SPRING_CACHE_MANAGER_BEAN = "oaiPmhSpringCacheManager";

    private static final String EHCACHE_CONFIG =
            "/eu/etaxonomy/cdm/remote/ehcache-oai-pmh.xml";

    @Bean(name = SPRING_CACHE_MANAGER_BEAN)
    public CacheManager oaiPmhSpringCacheManager() {
        CachingProvider provider = Caching.getCachingProvider(
                "org.ehcache.jsr107.EhcacheCachingProvider");
        URI configUri = resolveConfigUri();
        javax.cache.CacheManager jcacheManager = provider.getCacheManager(
                configUri, provider.getDefaultClassLoader());

        JCacheCacheManager springCacheManager = new JCacheCacheManager(jcacheManager);
        springCacheManager.afterPropertiesSet();
        return springCacheManager;
    }

    @Bean
    public ResumptionTokenCache resumptionTokenCache(
            @Qualifier(SPRING_CACHE_MANAGER_BEAN) CacheManager cacheManager) {
        return new ResumptionTokenCache(cacheManager);
    }

    private static URI resolveConfigUri() {
        URL resource = OaiPmhCacheConfiguration.class.getResource(EHCACHE_CONFIG);
        if (resource == null) {
            throw new IllegalStateException("Missing classpath resource " + EHCACHE_CONFIG);
        }
        try {
            return resource.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid URI for " + EHCACHE_CONFIG, e);
        }
    }
}
