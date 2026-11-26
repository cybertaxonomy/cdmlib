/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.cache.ehcache;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.core.spi.service.StatisticsService;
import org.ehcache.core.internal.statistics.DefaultStatisticsService;
import org.ehcache.expiry.ExpiryPolicy;

/**
 * Facade for the Ehcache 3 {@link CacheManager} used for CDM entity caching
 * (permanent term cache, model cache, session caches).
 * <p>
 * This manager intentionally uses the Ehcache 3 native API and <b>not</b>
 * JCache, so that it does not interfere with the JCache provider used for
 * OAI-PMH and so that values are stored by reference on heap.
 * <p>
 * The manager is a lazily initialized singleton ({@link #getInstance()}).
 * After {@link #shutdown()} it is re-initialized on next usage.
 * <p>
 * Disk store: if a disk store path is set (before the manager is initialized)
 * the Ehcache 3 persistence service is started for that directory and caches
 * with {@link CdmCacheConfiguration#maxDiskMegabytes(long)} &gt; 0 get a disk
 * tier. By default all caches are heap only.
 *
 * @author a.mueller
 */
public class CdmEntityCacheManager {

    private static final Logger logger = LogManager.getLogger();

    private static final CdmEntityCacheManager INSTANCE = new CdmEntityCacheManager();

    private final Object lock = new Object();

    private File diskStorePath;
    private CacheManager cacheManager;
    private StatisticsService statisticsService;
    private final Map<String,CdmCache> caches = new LinkedHashMap<>();
    private boolean diskStoreActive = false;

    public static CdmEntityCacheManager getInstance() {
        return INSTANCE;
    }

    /**
     * Public for subclassing / testing with isolated managers. Usually
     * {@link #getInstance()} should be used.
     */
    public CdmEntityCacheManager() {
    }

// ************************ CONFIGURATION ****************************/

    /**
     * Sets the directory used for the (optional) disk tier. Must be called
     * before the manager is initialized (first cache access). If the manager
     * is already running with another path the new value is ignored and a
     * warning is logged.
     */
    public void setDiskStorePath(File diskStorePath) {
        synchronized (lock) {
            if (cacheManager != null) {
                if (diskStorePath != null && !diskStorePath.equals(this.diskStorePath)) {
                    logger.warn("CdmEntityCacheManager is already initialized. Disk store path " + diskStorePath + " is ignored.");
                }
                return;
            }
            this.diskStorePath = diskStorePath;
        }
    }

    public void setDiskStorePath(String diskStorePath) {
        setDiskStorePath(diskStorePath == null ? null : new File(diskStorePath));
    }

    public File getDiskStorePath() {
        synchronized (lock) {
            return diskStorePath;
        }
    }

    /**
     * Whether the manager is running (initialized and not shut down).
     */
    public boolean isInitialized() {
        synchronized (lock) {
            return cacheManager != null;
        }
    }

    /**
     * Whether the Ehcache 3 persistence service (disk store) is active.
     */
    public boolean isDiskStoreActive() {
        synchronized (lock) {
            return diskStoreActive;
        }
    }

// ************************ CACHES ****************************/

    /**
     * @return the cache or <code>null</code> if it does not exist
     */
    public CdmCache getCache(String name) {
        synchronized (lock) {
            return caches.get(name);
        }
    }

    /**
     * Creates the cache. If a cache with that name exists already it is
     * returned unchanged (its configuration is not modified).
     */
    public CdmCache getOrCreate(CdmCacheConfiguration configuration) {
        synchronized (lock) {
            CdmCache existing = caches.get(configuration.getName());
            if (existing != null) {
                return existing;
            }
            return doCreate(configuration);
        }
    }

    /**
     * Creates a new cache. If a cache with that name already exists it is
     * removed first (like replacing a cache in Ehcache 2).
     */
    public CdmCache createCache(CdmCacheConfiguration configuration) {
        synchronized (lock) {
            if (caches.containsKey(configuration.getName())) {
                removeCache(configuration.getName());
            }
            return doCreate(configuration);
        }
    }

    private CdmCache doCreate(CdmCacheConfiguration cc) {
        ensureInitialized();

        long heap = cc.isUnboundedHeap() ? Long.MAX_VALUE
                : cc.getMaxHeapEntries() > 0 ? cc.getMaxHeapEntries() : CdmCacheConfiguration.DEFAULT_MAX_HEAP_ENTRIES;
        ResourcePoolsBuilder pools = ResourcePoolsBuilder.newResourcePoolsBuilder().heap(heap, EntryUnit.ENTRIES);
        if (cc.getMaxDiskMegabytes() > 0) {
            if (diskStoreActive) {
                pools = pools.disk(cc.getMaxDiskMegabytes(), MemoryUnit.MB, false);
            } else {
                logger.warn("Cache " + cc.getName() + " requests a disk tier but no disk store is available. Using heap only.");
            }
        }

        boolean wrapped = false;
        ExpiryPolicy<Object,Object> expiry;
        if (cc.isEternal()) {
            expiry = ExpiryPolicyBuilder.noExpiration();
        } else if (cc.getTimeToLiveSeconds() > 0 && cc.getTimeToIdleSeconds() > 0) {
            expiry = new CdmCache.TtlAndTtiExpiry(cc.getTimeToLiveSeconds(), cc.getTimeToIdleSeconds());
            wrapped = true;
        } else if (cc.getTimeToLiveSeconds() > 0) {
            expiry = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(cc.getTimeToLiveSeconds()));
        } else {
            expiry = ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofSeconds(cc.getTimeToIdleSeconds()));
        }

        CacheConfiguration<Object,Object> config = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Object.class, Object.class, pools)
                .withExpiry(expiry)
                .build();

        org.ehcache.Cache<Object,Object> cache = cacheManager.createCache(cc.getName(), config);
        CdmCache result = new CdmCache(cc.getName(), cache, wrapped, this, statisticsService);
        caches.put(cc.getName(), result);
        return result;
    }

    /**
     * Removes (disposes) the cache with the given name. Does nothing if no
     * such cache exists.
     */
    public void removeCache(String name) {
        synchronized (lock) {
            CdmCache cache = caches.remove(name);
            if (cache != null) {
                cache.markClosed();
                if (cacheManager != null) {
                    try {
                        cacheManager.removeCache(name);
                    } catch (RuntimeException e) {
                        logger.warn("Could not remove cache " + name + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * A snapshot of the names of all caches.
     */
    public List<String> getCacheNames() {
        synchronized (lock) {
            return new ArrayList<>(caches.keySet());
        }
    }

    boolean isAlive(CdmCache cache) {
        synchronized (lock) {
            return caches.get(cache.getName()) == cache;
        }
    }

    /**
     * Closes all caches and the underlying Ehcache 3 manager. A subsequent
     * call to {@link #getOrCreate(CdmCacheConfiguration)} initializes it again.
     */
    public void shutdown() {
        synchronized (lock) {
            for (CdmCache cache : caches.values()) {
                cache.markClosed();
            }
            caches.clear();
            if (cacheManager != null) {
                try {
                    cacheManager.close();
                } catch (RuntimeException e) {
                    logger.warn("Error while closing Ehcache 3 CacheManager: " + e.getMessage());
                }
                cacheManager = null;
            }
            statisticsService = null;
            diskStoreActive = false;
        }
    }

// ************************ INIT ****************************/

    private void ensureInitialized() {
        if (cacheManager != null) {
            return;
        }
        //NOTE: JMX registration (ehcache 2 ManagementService) is not provided anymore
        if (diskStorePath != null) {
            try {
                cacheManager = build(true);
                diskStoreActive = true;
                logger.debug("CdmEntityCacheManager created with disk store " + diskStorePath);
                return;
            } catch (RuntimeException e) {
                logger.warn("Could not start the disk store at " + diskStorePath
                        + " (e.g. used by another instance). Caches are heap only: " + e.getMessage());
            }
        }
        cacheManager = build(false);
        diskStoreActive = false;
    }

    private CacheManager build(boolean withDisk) {
        StatisticsService stats = new DefaultStatisticsService();
        CacheManagerBuilder<CacheManager> builder = CacheManagerBuilder.newCacheManagerBuilder()
                .using(stats)
                .withClassLoader(CdmEntityCacheManager.class.getClassLoader());
        CacheManager result;
        if (withDisk) {
            result = builder.with(CacheManagerBuilder.persistence(diskStorePath)).build(true);
        } else {
            result = builder.build(true);
        }
        this.statisticsService = stats;
        return result;
    }
}
