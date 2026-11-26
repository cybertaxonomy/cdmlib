/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.api.cache.ehcache.CdmCache;
import eu.etaxonomy.cdm.api.cache.ehcache.CdmEntityCacheManager;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * Tests the Ehcache 3 based model cache and the transient entity cacher.
 *
 * @author a.mueller
 */
public class CdmRemoteCacheManagerTest {

    @Test
    public void testModelCache() {
        CdmCache cache = CdmRemoteCacheManager.INSTANCE().getCdmModelGetMethodsCache();
        assertNotNull(cache);
        assertEquals(CdmRemoteCacheManager.CDM_MODEL_CACHE_NAME, cache.getName());
        assertFalse(cache.getKeys().isEmpty());
        assertNotNull(cache.get(Taxon.class.getName()));
        assertNotNull(new CacheLoader(null).getFromCdmlibModelCache(Taxon.class.getName()));
    }

    @Test
    public void testModelCacheSurvivesShutdown() {
        CdmRemoteCacheManager.INSTANCE();
        CdmEntityCacheManager.getInstance().shutdown();
        CdmCache cache = CdmRemoteCacheManager.INSTANCE().getCdmModelGetMethodsCache();
        assertTrue(cache.isAlive());
        assertNotNull(cache.get(Taxon.class.getName()));
    }

    @Test
    public void testTransientEntityCacher() {
        CdmTransientEntityCacher cacher = new CdmTransientEntityCacher("testCacher");
        try {
            assertTrue(CdmEntityCacheManager.getInstance().getCacheNames().contains("testCacher"));
            assertNotNull(cacher.getCacheStatistics());
            assertEquals(0, cacher.getCacheStatistics().getHeapEntryCount());
            assertTrue(cacher.getAllEntities().isEmpty());

            CdmEntityCacheKey<Taxon> key = CdmTransientEntityCacher.generateKey(Taxon.class, 5);
            assertNull(cacher.getFromCache(key));
            Taxon taxon = Taxon.NewInstance(null, null);
            taxon.setId(5);
            cacher.putToTransientCache(key, taxon);
            assertSame(taxon, cacher.getFromCache(key));
            assertTrue(cacher.exists(key));
            assertEquals(1, cacher.getAllEntities().size());
            assertEquals(1, cacher.getCacheStatistics().getHeapEntryCount());

            CdmRemoteCacheManager.removeEntityCaches();
            assertFalse(CdmEntityCacheManager.getInstance().getCacheNames().contains("testCacher"));
            assertTrue(CdmEntityCacheManager.getInstance().getCacheNames().contains(CdmRemoteCacheManager.CDM_MODEL_CACHE_NAME));
        } finally {
            cacher.dispose();
        }
    }
}
