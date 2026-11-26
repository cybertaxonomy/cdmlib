/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.cache.ehcache;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.ehcache.Cache;
import org.ehcache.core.spi.service.StatisticsService;
import org.ehcache.core.statistics.CacheStatistics;
import org.ehcache.core.statistics.TierStatistics;
import org.ehcache.expiry.ExpiryPolicy;

/**
 * Thin wrapper around an Ehcache 3 {@link Cache}&lt;Object,Object&gt;.
 * <p>
 * The cache stores values <b>by reference</b> on heap, therefore
 * {@link #get(Object)} returns the very same instance that was put before.
 * <p>
 * <code>null</code> keys and <code>null</code> values are not supported:
 * {@link #put(Object, Object)} with a <code>null</code> value is ignored
 * (and removes nothing), {@link #get(Object)} of a missing key returns
 * <code>null</code>.
 * <p>
 * Instances are created by the {@link CdmEntityCacheManager}.
 *
 * @author a.mueller
 */
public class CdmCache {

    private static final String HEAP_TIER = "OnHeap";
    private static final String DISK_TIER = "Disk";

    private final String name;
    private final Cache<Object,Object> cache;
    private final CdmEntityCacheManager owner;
    private final StatisticsService statisticsService;
    /**
     * if <code>true</code> values are wrapped in a {@link TtlHolder} to be
     * able to combine time to live and time to idle expiry
     */
    private final boolean wrapped;
    private volatile boolean closed = false;

    CdmCache(String name, Cache<Object,Object> cache, boolean wrapped, CdmEntityCacheManager owner,
            StatisticsService statisticsService) {
        this.name = name;
        this.cache = cache;
        this.wrapped = wrapped;
        this.owner = owner;
        this.statisticsService = statisticsService;
    }

    public String getName() {
        return name;
    }

    /**
     * @return the cached value for the key or <code>null</code> if no such
     *         (non expired) entry exists
     */
    public Object get(Object key) {
        if (key == null) {
            return null;
        }
        return unwrap(cache.get(key));
    }

    /**
     * Puts a value. <code>null</code> values or keys are ignored.
     */
    public void put(Object key, Object value) {
        if (key == null || value == null) {
            return;
        }
        cache.put(key, wrapped ? new TtlHolder(value, System.currentTimeMillis()) : value);
    }

    public void remove(Object key) {
        if (key != null) {
            cache.remove(key);
        }
    }

    /**
     * Removes all entries but keeps the cache itself.
     */
    public void clear() {
        cache.clear();
    }

    public boolean containsKey(Object key) {
        return key != null && cache.containsKey(key);
    }

    /**
     * A snapshot of the keys currently in the cache.
     */
    public List<Object> getKeys() {
        List<Object> keys = new ArrayList<>();
        for (Cache.Entry<Object,Object> entry : cache) {
            keys.add(entry.getKey());
        }
        return keys;
    }

    /**
     * Returns the entries for the given keys. Keys which are not cached are
     * not part of the result.
     */
    public Map<Object,Object> getAll(Collection<?> keys) {
        Map<Object,Object> result = new HashMap<>();
        if (keys == null || keys.isEmpty()) {
            return result;
        }
        Set<Object> keySet = new LinkedHashSet<>();
        for (Object key : keys) {
            if (key != null) {
                keySet.add(key);
            }
        }
        Map<Object,Object> found = cache.getAll(keySet);
        for (Map.Entry<Object,Object> entry : found.entrySet()) {
            Object value = unwrap(entry.getValue());
            if (value != null) {
                result.put(entry.getKey(), value);
            }
        }
        return result;
    }

    public CdmCacheStatistics getStatistics() {
        long heapEntries = -1;
        long heapBytes = 0;
        long diskEntries = 0;
        long diskBytes = 0;
        try {
            CacheStatistics stats = statisticsService == null ? null : statisticsService.getCacheStatistics(name);
            if (stats != null) {
                Map<String,TierStatistics> tiers = stats.getTierStatistics();
                TierStatistics heap = tiers.get(HEAP_TIER);
                if (heap != null) {
                    heapEntries = heap.getMappings();
                    heapBytes = heap.getOccupiedByteSize();
                }
                TierStatistics disk = tiers.get(DISK_TIER);
                if (disk != null) {
                    diskEntries = disk.getMappings();
                    diskBytes = disk.getOccupiedByteSize();
                }
            }
        } catch (RuntimeException e) {
            //statistics are optional
            heapEntries = -1;
        }
        if (heapEntries < 0) {
            heapEntries = getKeys().size();
        }
        return new CdmCacheStatistics(heapEntries, heapBytes, diskEntries, diskBytes);
    }

    /**
     * Whether the cache is still registered at its manager.
     */
    public boolean isAlive() {
        return !closed && owner.isAlive(this);
    }

    /**
     * Disposes this cache by removing it from its manager. All entries are
     * lost. Further usage is not allowed.
     */
    public void close() {
        owner.removeCache(name);
    }

    void markClosed() {
        this.closed = true;
    }

    private Object unwrap(Object value) {
        if (value instanceof TtlHolder) {
            return ((TtlHolder)value).value;
        }
        return value;
    }

    @Override
    public String toString() {
        return "CdmCache [" + name + "]";
    }

// ************************* TTL + TTI support *********************************/

    /**
     * Holds the creation time to be able to combine TTL and TTI.
     */
    static final class TtlHolder implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Object value;
        private final long createdMillis;

        TtlHolder(Object value, long createdMillis) {
            this.value = value;
            this.createdMillis = createdMillis;
        }
    }

    /**
     * Expiry policy that behaves like Ehcache 2 with ttl and tti both set:
     * an entry expires if either the time to live since creation / update
     * or the time to idle since last access is exceeded.
     */
    static final class TtlAndTtiExpiry implements ExpiryPolicy<Object,Object> {

        private final long ttlMillis;
        private final long ttiMillis;

        TtlAndTtiExpiry(long ttlSeconds, long ttiSeconds) {
            this.ttlMillis = ttlSeconds * 1000;
            this.ttiMillis = ttiSeconds * 1000;
        }

        private long initial() {
            if (ttlMillis > 0 && ttiMillis > 0) {
                return Math.min(ttlMillis, ttiMillis);
            }
            return ttlMillis > 0 ? ttlMillis : ttiMillis;
        }

        @Override
        public Duration getExpiryForCreation(Object key, Object value) {
            return Duration.ofMillis(initial());
        }

        @Override
        public Duration getExpiryForAccess(Object key, Supplier<?> value) {
            long result = ttiMillis > 0 ? ttiMillis : Long.MAX_VALUE;
            Object v = value.get();
            if (ttlMillis > 0 && v instanceof TtlHolder) {
                long remaining = ((TtlHolder)v).createdMillis + ttlMillis - System.currentTimeMillis();
                result = Math.min(result, remaining);
            }
            if (result == Long.MAX_VALUE) {
                return null; //unchanged
            }
            return Duration.ofMillis(Math.max(0, result));
        }

        @Override
        public Duration getExpiryForUpdate(Object key, Supplier<?> oldValue, Object newValue) {
            return Duration.ofMillis(initial());
        }
    }
}
