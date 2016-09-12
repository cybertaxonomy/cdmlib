// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import org.junit.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;

/**
 * @author a.kohlbecker
 * @date Sep 12, 2016
 *
 */
public class ProgresMonitorTest extends Assert {

    @Test
    public void testPercentage() {
        DefaultProgressMonitor monitor = DefaultProgressMonitor.NewInstance();
        monitor.beginTask("test", 33);
        monitor.worked(5);
        assertEquals(Double.valueOf(15.16), Double.valueOf(monitor.getPercentage()));
    }

}
