/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.description;

import org.junit.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.ExtendedTimePeriod;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TemporalData;

/**
 * Test class for testing {@link TemporalDataFormatter}.
 *
 * @author muellera
 * @since 27.11.2024
 */
public class TemporalDataFormatterTest {

    /**
     * Test method for {@link eu.etaxonomy.cdm.format.description.TemporalDataFormatter#doFormat(eu.etaxonomy.cdm.model.description.TemporalData, java.util.List)}.
     */
    @Test
    public void testDoFormat() {

        String sep = TimePeriod.SEP;

        TemporalData td = TemporalData.NewInstance();
        TemporalDataFormatter formatter = TemporalDataFormatter.NewInstance();
        td.setFeature(Feature.FLOWERING_PERIOD());
        td.setPeriod(ExtendedTimePeriod.NewExtendedMonthInstance(4, 5, 3, 6));
        Assert.assertEquals("(Mar"+sep+")Apr"+sep+"May("+sep+"Jun)", formatter.format(td));

        td.putModifyingText(Language.GERMAN(), "Meistens");
        Assert.assertEquals("Meistens (Mar"+sep+")Apr"+sep+"May("+sep+"Jun)", formatter.format(td));
    }
}