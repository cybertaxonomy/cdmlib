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
 * Simple, cache-implementation independent statistics of a {@link CdmCache}.
 * Values which are unknown or not applicable are <code>0</code>.
 *
 * @author a.mueller
 */
public class CdmCacheStatistics {

    private final long heapEntryCount;
    private final long heapSizeInBytes;
    private final long diskEntryCount;
    private final long diskSizeInBytes;

    public CdmCacheStatistics(long heapEntryCount, long heapSizeInBytes) {
        this(heapEntryCount, heapSizeInBytes, 0, 0);
    }

    public CdmCacheStatistics(long heapEntryCount, long heapSizeInBytes, long diskEntryCount, long diskSizeInBytes) {
        this.heapEntryCount = Math.max(0, heapEntryCount);
        this.heapSizeInBytes = Math.max(0, heapSizeInBytes);
        this.diskEntryCount = Math.max(0, diskEntryCount);
        this.diskSizeInBytes = Math.max(0, diskSizeInBytes);
    }

    /**
     * Number of entries held on heap.
     */
    public long getHeapEntryCount() {
        return heapEntryCount;
    }

    /**
     * Size of the heap tier in bytes, <code>0</code> if unknown.
     */
    public long getHeapSizeInBytes() {
        return heapSizeInBytes;
    }

    /**
     * Number of entries on the disk tier, <code>0</code> if unused.
     */
    public long getDiskEntryCount() {
        return diskEntryCount;
    }

    /**
     * Size of the disk tier in bytes, <code>0</code> if unused or unknown.
     */
    public long getDiskSizeInBytes() {
        return diskSizeInBytes;
    }

    @Override
    public String toString() {
        return "CdmCacheStatistics [heapEntryCount=" + heapEntryCount + ", heapSizeInBytes=" + heapSizeInBytes
                + ", diskEntryCount=" + diskEntryCount + ", diskSizeInBytes=" + diskSizeInBytes + "]";
    }
}
