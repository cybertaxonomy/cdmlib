/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.printPub;

import org.junit.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportConfigurator;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportState;

/**
 * @author k.luther
 * @since 10.06.2026
 */
public class PrintPubExportStateTest {

    @Test
    public void testIncrementShortCitation() {
        PrintPubExportConfigurator config = PrintPubExportConfigurator.NewInstance();
        PrintPubExportState state = new PrintPubExportState(config);
        final String refStr = "Xxx";
        String result = state.incrementShortCitation(refStr);
        for (int i=1;i < 12;i++) {
            result += state.incrementShortCitation(refStr);
        }
        Assert.assertEquals("abcdefghikl", result);
    }
}