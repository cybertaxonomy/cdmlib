/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common.monitor;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author a.mueller
 * @since 15.09.2016
 *
 */
public class DefaultProgressMonitorTest {

    int steps = 739893;

    @Test
    public void testRoundingWithoutSubProgressMonitor() {
        DefaultProgressMonitor monitor = new DefaultProgressMonitor();
        monitor.beginTask("Hallo", steps);
        for (int i = 0; i<steps; i++){
            monitor.worked(1);
//            System.out.println(monitor.getPercentage());
        }

        monitor.done();
        System.out.println(monitor.getPercentage());
        System.out.println(monitor.getPercentageRounded(3));
    }

    @Test
    public void testRoundingWithSubProgressMonitor() {
        DefaultProgressMonitor monitor = new DefaultProgressMonitor();
        int subTaskTicks = 3273;

        int subTasks = 130;
        monitor.beginTask("Hallo", steps);
        for (int i = 0; i<steps-(subTaskTicks*subTasks); i++){
            monitor.worked(1);
//            System.out.println(monitor.getPercentage());
        }

        for (int i = 0; i < subTasks; i++){
            SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, subTaskTicks);
            int subTicks = 2457;
            subMonitor.beginTask("SubMonitor", subTicks);
            monitor.beginTask("Hallo", steps);
            for (int j = 0; j < subTicks; j++){
                subMonitor.worked(1);
//            System.out.println(monitor.getPercentage());
            }
        }

        monitor.done();
        System.out.println(monitor.getPercentage());
        System.out.println(monitor.getPercentageRounded(3));
    }

    @Test
    public void testPercentage() {
        DefaultProgressMonitor monitor = DefaultProgressMonitor.NewInstance();
        monitor.beginTask("test", 33);
        monitor.worked(5);
        Assert.assertEquals(Double.valueOf(15.15152), Double.valueOf(monitor.getPercentage()));
    }



}
