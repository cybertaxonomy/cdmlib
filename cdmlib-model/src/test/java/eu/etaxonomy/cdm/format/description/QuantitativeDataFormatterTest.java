/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.description;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.format.ICdmFormatter;
import eu.etaxonomy.cdm.format.ICdmFormatter.FormatKey;
import eu.etaxonomy.cdm.format.description.QuantitativeDataFormatter;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.term.DefaultTermInitializer;

/**
 * Test for {@link QuantitativeDataFormatter}.
 *
 * @author a.mueller
 * @since 12.03.2020
 */
public class QuantitativeDataFormatterTest {

    private StatisticalMeasurementValue min1;
    private StatisticalMeasurementValue max1;
    private StatisticalMeasurementValue n1;

    @Before
    public void setUp() throws Exception {
        min1 = StatisticalMeasurementValue.NewInstance(StatisticalMeasure.MIN(), new BigDecimal("0.1"));
        max1 = StatisticalMeasurementValue.NewInstance(StatisticalMeasure.MAX(), new BigDecimal("1.3"));
        n1 = StatisticalMeasurementValue.NewInstance(StatisticalMeasure.SAMPLE_SIZE(), new BigDecimal("2"));
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        if (Language.DEFAULT() == null){
            new DefaultTermInitializer().initialize();
        }
    }

    @Test
    public void testFormat() {
        QuantitativeData quantData = QuantitativeData.NewInstance(Feature.CHROMOSOME_NUMBER());
        FormatKey[] formatKey = null;

        quantData.addStatisticalValue(min1);
        quantData.addStatisticalValue(max1);

        QuantitativeDataFormatter formatter = new QuantitativeDataFormatter(quantData, formatKey);
        String text = formatter.format(quantData, formatKey);
        Assert.assertEquals("0.1-1.3", text);
        quantData.addStatisticalValue(n1);
        MeasurementUnit unit = MeasurementUnit.METER();
        quantData.setUnit(unit);

        text = formatter.format(quantData, formatKey);
        Assert.assertEquals("0.1-1.3 m [n=2]", text);
    }
}
