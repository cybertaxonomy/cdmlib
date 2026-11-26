/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.cache.ehcache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author a.mueller
 */
public class CdmEntityCacheManagerTest {

    private CdmEntityCacheManager manager;

    @BeforeEach
    public void setUp() {
        manager = new CdmEntityCacheManager();
    }

    @AfterEach
    public void tearDown() {
        manager.shutdown();
    }

    private CdmCache newCache(String name) {
        return manager.getOrCreate(CdmCacheConfiguration.newInstance(name).maxHeapEntries(100));
    }

    @Test
    public void testPutGetSameInstance() {
        CdmCache cache = newCache("test1");
        Object value = new Object();
        UUID key = UUID.randomUUID();
        cache.put(key, value);
        assertSame(value, cache.get(key));
        assertSame(value, cache.get(key));
        assertTrue(cache.containsKey(key));
        assertNull(cache.get(UUID.randomUUID()));
        assertFalse(cache.containsKey(UUID.randomUUID()));
        assertEquals("test1", cache.getName());
    }

    @Test
    public void testOverwrite() {
        CdmCache cache = newCache("test2");
        Object v1 = new Object();
        Object v2 = new Object();
        cache.put("key", v1);
        cache.put("key", v2);
        assertSame(v2, cache.get("key"));
        assertNotSame(v1, cache.get("key"));
        assertEquals(1, cache.getKeys().size());
    }

    @Test
    public void testNullValueIsNotPut() {
        CdmCache cache = newCache("test3");
        cache.put("key", null);
        assertFalse(cache.containsKey("key"));
        assertNull(cache.get("key"));
        cache.put("key", "a");
        cache.put("key", null);
        assertEquals("a", cache.get("key"));
        assertNull(cache.get(null));
    }

    @Test
    public void testRemoveAndClear() {
        CdmCache cache = newCache("test4");
        cache.put("a", "A");
        cache.put("b", "B");
        cache.remove("a");
        assertNull(cache.get("a"));
        assertEquals("B", cache.get("b"));
        cache.put("a", "A");
        cache.clear();
        assertTrue(cache.getKeys().isEmpty());
        assertNull(cache.get("b"));
        //cache still usable
        cache.put("c", "C");
        assertEquals("C", cache.get("c"));
    }

    @Test
    public void testGetKeysAndGetAll() {
        CdmCache cache = newCache("test5");
        cache.put("a", "A");
        cache.put("b", "B");
        cache.put("c", "C");
        assertEquals(new HashSet<>(Arrays.asList("a", "b", "c")), new HashSet<>(cache.getKeys()));
        Map<Object,Object> all = cache.getAll(Arrays.asList("a", "c", "missing"));
        assertEquals(2, all.size());
        assertEquals("A", all.get("a"));
        assertEquals("C", all.get("c"));
        assertTrue(cache.getAll(null).isEmpty());
    }

    @Test
    public void testGetOrCreateAndRemoveCache() {
        CdmCache cache = newCache("test6");
        cache.put("a", "A");
        assertSame(cache, manager.getOrCreate(CdmCacheConfiguration.newInstance("test6")));
        assertSame(cache, manager.getCache("test6"));
        assertTrue(manager.getCacheNames().contains("test6"));
        assertTrue(cache.isAlive());

        manager.removeCache("test6");
        assertNull(manager.getCache("test6"));
        assertFalse(manager.getCacheNames().contains("test6"));
        assertFalse(cache.isAlive());

        CdmCache recreated = newCache("test6");
        assertNotSame(cache, recreated);
        assertNull(recreated.get("a"));

        //removing a not existing cache is a no-op
        manager.removeCache("doesNotExist");
        assertNull(manager.getCache("doesNotExist"));
    }

    @Test
    public void testCreateCacheReplacesExisting() {
        CdmCache cache = newCache("test7");
        cache.put("a", "A");
        CdmCache replaced = manager.createCache(CdmCacheConfiguration.newInstance("test7"));
        assertNotSame(cache, replaced);
        assertNull(replaced.get("a"));
        assertSame(replaced, manager.getCache("test7"));
    }

    @Test
    public void testClose() {
        CdmCache cache = newCache("test8");
        cache.close();
        assertNull(manager.getCache("test8"));
        assertFalse(cache.isAlive());
    }

    @Test
    public void testShutdownAndReinit() {
        newCache("test9").put("a", "A");
        manager.shutdown();
        assertFalse(manager.isInitialized());
        assertTrue(manager.getCacheNames().isEmpty());
        CdmCache cache = newCache("test9");
        assertNull(cache.get("a"));
        assertTrue(manager.isInitialized());
    }

    @Test
    public void testStatistics() {
        CdmCache cache = newCache("test10");
        assertEquals(0, cache.getStatistics().getHeapEntryCount());
        cache.put("a", "A");
        cache.put("b", "B");
        CdmCacheStatistics stats = cache.getStatistics();
        assertEquals(2, stats.getHeapEntryCount());
        assertTrue(stats.getHeapSizeInBytes() >= 0);
        assertEquals(0, stats.getDiskEntryCount());
    }

    @Test
    public void testHeapLimit() {
        CdmCache cache = manager.getOrCreate(CdmCacheConfiguration.newInstance("test11").maxHeapEntries(10));
        for (int i = 0; i < 100; i++) {
            cache.put(i, "v" + i);
        }
        assertTrue(cache.getKeys().size() <= 10);
    }

    @Test
    public void testUnboundedHeap() {
        CdmCache cache = manager.getOrCreate(CdmCacheConfiguration.newInstance("test12").unboundedHeap());
        for (int i = 0; i < 1000; i++) {
            cache.put(i, "v" + i);
        }
        assertEquals(1000, cache.getKeys().size());
    }

    @Test
    public void testTimeToLive() throws InterruptedException {
        CdmCache cache = manager.getOrCreate(CdmCacheConfiguration.newInstance("test13").expiry(1, 0));
        cache.put("a", "A");
        assertEquals("A", cache.get("a"));
        Thread.sleep(1500);
        assertNull(cache.get("a"));
    }

    @Test
    public void testTimeToLiveAndIdle() throws InterruptedException {
        CdmCache cache = manager.getOrCreate(CdmCacheConfiguration.newInstance("test14").expiry(2, 1));
        Object value = new Object();
        cache.put("a", value);
        cache.put("b", "B");
        //keep "a" alive via access, "b" is idle
        Thread.sleep(600);
        assertSame(value, cache.get("a"));
        Thread.sleep(600);
        assertSame(value, cache.get("a"));
        assertNull(cache.get("b"));
        //ttl reached
        Thread.sleep(1000);
        assertNull(cache.get("a"));
    }

    @Test
    public void testDiskStorePath() throws Exception {
        File dir = Files.createTempDirectory("cdmEntityCache").toFile();
        try {
            manager.setDiskStorePath(dir);
            CdmCache cache = newCache("test15");
            assertTrue(manager.isDiskStoreActive());
            Object value = new Object();
            cache.put("a", value);
            assertSame(value, cache.get("a"));
            assertNotNull(manager.getDiskStorePath());
        } finally {
            manager.shutdown();
            deleteRecursive(dir);
        }
    }

    private void deleteRecursive(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursive(child);
            }
        }
        file.delete();
    }

    @Test
    public void testSingleton() {
        assertSame(CdmEntityCacheManager.getInstance(), CdmEntityCacheManager.getInstance());
    }
}
