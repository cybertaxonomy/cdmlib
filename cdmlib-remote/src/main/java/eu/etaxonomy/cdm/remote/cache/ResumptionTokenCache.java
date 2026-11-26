/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.Assert;

import eu.etaxonomy.cdm.remote.dto.oaipmh.ResumptionToken;

/**
 * Spring Cache facade for OAI-PMH {@link ResumptionToken} storage.
 * Replaces the former springmodules {@code CacheProviderFacade} usage.
 *
 * @author a.mueller
 * @since 26.11.2026
 */
public class ResumptionTokenCache {

    public static final String CACHE_NAME =
            "eu.etaxonomy.cdm.remote.controller.TaxonOaiPmhController.resumptionToken";

    private final Cache cache;

    public ResumptionTokenCache(CacheManager cacheManager) {
        Assert.notNull(cacheManager, "cacheManager must not be null");
        Cache resolved = cacheManager.getCache(CACHE_NAME);
        Assert.notNull(resolved, "Cache '" + CACHE_NAME + "' must be configured");
        this.cache = resolved;
    }

    public void put(String key, ResumptionToken token) {
        Assert.hasText(key, "key must not be empty");
        Assert.notNull(token, "token must not be null");
        cache.put(key, token);
    }

    public ResumptionToken get(String key) {
        if (key == null) {
            return null;
        }
        return cache.get(key, ResumptionToken.class);
    }

    public void remove(String key) {
        if (key != null) {
            cache.evict(key);
        }
    }

    public boolean contains(String key) {
        return get(key) != null;
    }
}
