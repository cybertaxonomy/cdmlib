/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmLight;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author muellera
 * @since 11.07.2024
 */
public class CdmLightExportStateTest {

    @Test
    public void testIncrementShortCitation() {
        CdmLightExportConfigurator config = CdmLightExportConfigurator.NewInstance();
        CdmLightExportState state = new CdmLightExportState(config);
        final String refStr = "Xxx";
        String result = state.incrementShortCitation(refStr);
        for (int i=1;i < 12;i++) {
            result += state.incrementShortCitation(refStr);
        }
        Assert.assertEquals("abcdefghikl", result);
    }

}
