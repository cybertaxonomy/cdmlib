/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.cache.ehcache;

/**
 * Configuration of a {@link CdmCache}. Use the fluent setters to build it.
 *
 * @author a.mueller
 */
public class CdmCacheConfiguration {

    /**
     * Heap size used if {@link #maxHeapEntries(long)} is <code>0</code>
     */
    public static final long DEFAULT_MAX_HEAP_ENTRIES = 100_000;

    /**
     * Eviction hint. Ehcache 3 does not allow to configure the eviction
     * strategy of the heap tier, therefore this is currently only a hint.
     */
    public enum EvictionHint {
        LRU,
        LFU
    }

    private final String name;
    private long maxHeapEntries = 0;
    private boolean unboundedHeap = false;
    private boolean eternal = true;
    private long timeToLiveSeconds = 0;
    private long timeToIdleSeconds = 0;
    private long maxDiskMegabytes = 0;
    private EvictionHint evictionHint = EvictionHint.LRU;

    public CdmCacheConfiguration(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Cache name must not be empty");
        }
        this.name = name;
    }

    public static CdmCacheConfiguration newInstance(String name) {
        return new CdmCacheConfiguration(name);
    }

    /**
     * Maximum number of entries on heap. <code>0</code> means
     * {@link #DEFAULT_MAX_HEAP_ENTRIES}. Use {@link #unboundedHeap()} for
     * caches that must never evict.
     */
    public CdmCacheConfiguration maxHeapEntries(long maxHeapEntries) {
        if (maxHeapEntries < 0) {
            throw new IllegalArgumentException("maxHeapEntries must not be negative");
        }
        this.maxHeapEntries = maxHeapEntries;
        this.unboundedHeap = false;
        return this;
    }

    /**
     * The cache never evicts entries because of its size.
     */
    public CdmCacheConfiguration unboundedHeap() {
        this.unboundedHeap = true;
        return this;
    }

    /**
     * Entries never expire (default).
     */
    public CdmCacheConfiguration eternal() {
        this.eternal = true;
        this.timeToLiveSeconds = 0;
        this.timeToIdleSeconds = 0;
        return this;
    }

    /**
     * Time to live and time to idle in seconds. <code>0</code> means no
     * limit for the according value. If both are <code>0</code> the cache
     * is eternal.
     */
    public CdmCacheConfiguration expiry(long timeToLiveSeconds, long timeToIdleSeconds) {
        if (timeToLiveSeconds < 0 || timeToIdleSeconds < 0) {
            throw new IllegalArgumentException("Expiry values must not be negative");
        }
        this.timeToLiveSeconds = timeToLiveSeconds;
        this.timeToIdleSeconds = timeToIdleSeconds;
        this.eternal = timeToLiveSeconds == 0 && timeToIdleSeconds == 0;
        return this;
    }

    /**
     * Optional disk tier size in MB. Only effective if the
     * {@link CdmEntityCacheManager} has a disk store path and keys and
     * values are {@link java.io.Serializable}. <code>0</code> (default) means
     * heap only.
     */
    public CdmCacheConfiguration maxDiskMegabytes(long maxDiskMegabytes) {
        if (maxDiskMegabytes < 0) {
            throw new IllegalArgumentException("maxDiskMegabytes must not be negative");
        }
        this.maxDiskMegabytes = maxDiskMegabytes;
        return this;
    }

    public CdmCacheConfiguration evictionHint(EvictionHint evictionHint) {
        this.evictionHint = evictionHint == null ? EvictionHint.LRU : evictionHint;
        return this;
    }

    public String getName() {
        return name;
    }

    public long getMaxHeapEntries() {
        return maxHeapEntries;
    }

    public boolean isUnboundedHeap() {
        return unboundedHeap;
    }

    public boolean isEternal() {
        return eternal;
    }

    public long getTimeToLiveSeconds() {
        return timeToLiveSeconds;
    }

    public long getTimeToIdleSeconds() {
        return timeToIdleSeconds;
    }

    public long getMaxDiskMegabytes() {
        return maxDiskMegabytes;
    }

    public EvictionHint getEvictionHint() {
        return evictionHint;
    }
}
