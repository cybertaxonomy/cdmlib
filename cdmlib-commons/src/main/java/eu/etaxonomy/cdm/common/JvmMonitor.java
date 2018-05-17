/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author a.kohlbecker
 * @since Jul 1, 2016
 *
 */
public class JvmMonitor {


    public static final Logger logger = Logger.getLogger(JvmMonitor.class);

    private long gcTimeLast = 0;

    private long lastCheckTime = 0;

    /**
     * Returns the sum of approximate accumulated collection elapsed time in milliseconds
     * as reported by all garbage collectors.
     *
     * This method returns -1 if the collection elapsed time is undefined.
     *
     * @return
     */
    public long gcTime() {
        List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();

        //logger.setLevel(Level.DEBUG);

        long gcTime = -1;
        long collectorGcTime;
        for(GarbageCollectorMXBean gcMXBean : gcMXBeans){
                if(gcTime == -1) {
                    gcTime = 0;
                }
                collectorGcTime = gcMXBean.getCollectionTime();
                logger.debug("cgMxBean: " + gcMXBean.getName()
                        + " gcTime = " + collectorGcTime
                        + " gcCount = " + gcMXBean.getCollectionCount());
                if(collectorGcTime != -1) {
                    // only sum up if the time is defined
                    gcTime = gcMXBean.getCollectionTime();
                }
        }
        logger.debug("gcTimeSum = " + gcTime);
        return gcTime;

    }

    public MemoryUsage getHeapMemoryUsage(){

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        if(memoryMXBean != null){
            logger.debug("HeapMemoryUsage: " + memoryMXBean.getHeapMemoryUsage());
            return memoryMXBean.getHeapMemoryUsage();
        }
        return null;
    }


    public boolean hasFreeHeap(long freeHeapLimit) {

        if(!_hasFreeHeap(freeHeapLimit)) {
            Runtime.getRuntime().gc();
            return _hasFreeHeap(freeHeapLimit);
        }
        return true;
    }

    /**
     * @param maxUsedFraction
     * @return
     */
    private boolean _hasFreeHeap(long freeHeapLimit) {
        long freeHeap = getFreeHeap(false);
        return freeHeap > freeHeapLimit;
    }

    /**
     * @return
     */
    public long getFreeHeap(boolean gcBeforeMeasure) {
        if(gcBeforeMeasure) {
            Runtime.getRuntime().gc();
        }
        MemoryUsage heapUsage = getHeapMemoryUsage();
        long freeHeap =  heapUsage.getMax() - heapUsage.getUsed();
        return freeHeap;
    }

    /**
     * Returns the gcTime in milliseconds as obtained through {@link #gctime()} of the
     * time interval since this method has been called the last time and now.
     *
     * @return
     */
    public long getGCtimeSiceLastCheck() {
        long gcTimeNow = gcTime();
        long gcTimeSince = gcTimeNow - gcTimeLast;
        gcTimeLast = gcTimeNow;
        lastCheckTime  = System.currentTimeMillis();
        return gcTimeSince;
    }

    /**
     * Returns the time spend in gc as proportion (0.0 to 1.0) of the
     * time interval since this method has been called the last time and now.
     *
     * @return
     */
    public double getGCRateSiceLastCheck() {

        long gcTimeSince = getGCtimeSiceLastCheck();
        long timeDiff = System.currentTimeMillis() - lastCheckTime;
        double gcRate = gcTimeSince / (double) timeDiff;
        return gcRate;
    }




}
