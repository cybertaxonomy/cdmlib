/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author a.kohlbecker
 * @since Jul 1, 2016
 *
 */
public class JvmMonitorTest extends Assert{


    public static final Logger logger = Logger.getLogger(JvmMonitorTest.class);

    @Test
    public void testGcTime() {
        JvmMonitor jvmMonitor = new JvmMonitor();
        assertNotEquals(-1l, jvmMonitor.gcTime());

        Runtime.getRuntime().gc();
        Runtime.getRuntime().gc();
        Runtime.getRuntime().gc();
        long gcTimeLast_1 = jvmMonitor.getGCtimeSiceLastCheck();
        assertTrue(gcTimeLast_1 > 0);
        Runtime.getRuntime().gc();
        Runtime.getRuntime().gc();
        Runtime.getRuntime().gc();
        long gcTimeLast_2 = jvmMonitor.getGCtimeSiceLastCheck();
        assertTrue(gcTimeLast_1 > 0);
        assertTrue(jvmMonitor.gcTime() > gcTimeLast_2);

    }

    @Test
    public void testHeapUsage() {
        int MB = 1024 * 1024;
        int failWithMB = 300 * MB;
        JvmMonitor jvmMonitor = new JvmMonitor();

        long baseline = jvmMonitor.getHeapMemoryUsage().getUsed();
        logger.debug("before: " + baseline);
/*
        assertTrue(jvmMonitor.hasFreeHeap(0.9));

        logger.setLevel(Level.DEBUG);

        Object[] measure = new Object[MB]; // 1MB
        double bytePerObject = (jvmMonitor.getHeapMemoryUsage().getUsed() - baseline) / MB;
        long maxHeap = jvmMonitor.getHeapMemoryUsage().getMax();
        logger.debug("max: " + maxHeap);
        Object[] heapEater = new Object[(int)Math.round((failWithMB / bytePerObject))];
        logger.debug("after: " + jvmMonitor.getHeapMemoryUsage().getUsed());

        assertFalse(jvmMonitor.hasFreeHeap((failWithMB * 2) / (double)maxHeap));
*/

    }

}
