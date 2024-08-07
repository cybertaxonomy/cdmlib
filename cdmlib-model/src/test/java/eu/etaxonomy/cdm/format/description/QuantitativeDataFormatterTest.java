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
import org.junit.Test;

import eu.etaxonomy.cdm.format.ICdmFormatter.FormatKey;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.NoDescriptiveDataStatus;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * Test for {@link QuantitativeDataFormatter}.
 *
 * @author a.mueller
 * @since 12.03.2020
 */
public class QuantitativeDataFormatterTest extends TermTestBase {

    private StatisticalMeasurementValue min1;
    private StatisticalMeasurementValue max1;
    private StatisticalMeasurementValue lowerBound1;
    private StatisticalMeasurementValue upperBound1;
    private StatisticalMeasurementValue n1;

    private static final String minSep = QuantitativeDataFormatter.minSep;
    private static final String maxSep = QuantitativeDataFormatter.maxSep;
    private static final String lowerUpperSep = QuantitativeDataFormatter.lowerUpperSep;
    private static final String modifierSep = QuantitativeDataFormatter.modifierSep;


    @Before
    public void setUp() throws Exception {
        min1 = StatisticalMeasurementValue.NewInstance(StatisticalMeasure.MIN(), new BigDecimal("0.1"));
        max1 = StatisticalMeasurementValue.NewInstance(StatisticalMeasure.MAX(), new BigDecimal("1.3"));
        lowerBound1 = StatisticalMeasurementValue.NewInstance(StatisticalMeasure.TYPICAL_LOWER_BOUNDARY(), new BigDecimal("0.2"));
        upperBound1 = StatisticalMeasurementValue.NewInstance(StatisticalMeasure.TYPICAL_UPPER_BOUNDARY(), new BigDecimal("1.0"));
        n1 = StatisticalMeasurementValue.NewInstance(StatisticalMeasure.SAMPLE_SIZE(), new BigDecimal("2"));
    }

    @Test
    public void testFormat() {
        QuantitativeData quantData = QuantitativeData.NewInstance(Feature.CHROMOSOME_NUMBER());
        FormatKey[] formatKey = null;

        quantData.addStatisticalValue(min1);
        quantData.addStatisticalValue(max1);

        QuantitativeDataFormatter formatter = new QuantitativeDataFormatter(quantData, formatKey);
        String text = formatter.format(quantData, formatKey);
        Assert.assertEquals("0.1"+lowerUpperSep+"1.3", text);
        quantData.addStatisticalValue(n1);
        MeasurementUnit unit = MeasurementUnit.METER();
        quantData.setUnit(unit);
        text = formatter.format(quantData, formatKey);
        Assert.assertEquals("0.1"+lowerUpperSep+"1.3 m [n=2]", text);

        quantData.addStatisticalValue(lowerBound1);
        quantData.addStatisticalValue(upperBound1);
        text = formatter.format(quantData, formatKey);
        Assert.assertEquals("(0.1"+minSep+")0.2"+lowerUpperSep+"1.0("+maxSep+"1.3) m [n=2]", text);

        min1.setValue(null);
        text = formatter.format(quantData, formatKey);
        Assert.assertEquals("If only type but no value is given for a StatisticalMeasurementValue"
                + " it should be handled as if it is null", "0.2"+lowerUpperSep+"1.0("+maxSep+"1.3) m [n=2]", text);

        //TODO currently not handled correctly as max value is fully neglected
//        upperBound1.setValue(null);
//        text = formatter.format(quantData, formatKey);
//        Assert.assertEquals("If only type but no value is given for a StatisticalMeasurementValue"
//                + " it should be handled as if it is null", "0.2-1.0(-1.3) m [n=2]", text);
    }

    @Test
    public void testFormatWithModifier() {
        QuantitativeData quantData = QuantitativeData.NewInstance(Feature.CHROMOSOME_NUMBER());
        FormatKey[] formatKey = null;
        QuantitativeDataFormatter formatter = new QuantitativeDataFormatter(quantData, formatKey);

        quantData.addStatisticalValue(min1);
        quantData.addStatisticalValue(max1);
        quantData.addStatisticalValue(n1);
        MeasurementUnit unit = MeasurementUnit.METER();
        quantData.setUnit(unit);

        String text = formatter.format(quantData, formatKey);
        Assert.assertEquals("0.1"+lowerUpperSep+"1.3 m [n=2]", text);

        quantData.putModifyingText(Language.DEFAULT(), "about");
        text = formatter.format(quantData, formatKey);
        Assert.assertEquals("about 0.1"+lowerUpperSep+"1.3 m [n=2]", text);

        TermVocabulary<DefinedTerm> modifierVoc = TermVocabulary.NewInstance(TermType.Modifier, DefinedTerm.class);
        DefinedTerm modifier = DefinedTerm.NewModifierInstance("My new modifier", "new mod", "n.m.");
        modifierVoc.addTerm(modifier);

        quantData.addModifier(modifier);
        text = formatter.format(quantData, formatKey);
        Assert.assertEquals("about new mod 0.1"+lowerUpperSep+"1.3 m [n=2]", text);

        DefinedTerm minModifier = DefinedTerm.NewModifierInstance("Min modifier", "min", "m.");
        modifierVoc.addTerm(minModifier);
        min1.addModifier(minModifier);

        DefinedTerm maxModifier = DefinedTerm.NewModifierInstance("Max modifier", "max", "ma.");
        modifierVoc.addTerm(maxModifier);
        max1.addModifier(maxModifier);

        text = formatter.format(quantData, formatKey);
        //TODO correct formatting still needs to be discussed
        Assert.assertEquals("about new mod min"+modifierSep+"0.1"+lowerUpperSep+"max"+modifierSep+"1.3 m [n=2]", text);

        quantData.addStatisticalValue(lowerBound1);
        text = formatter.format(quantData, formatKey);
        Assert.assertEquals("about new mod (min"+modifierSep+"0.1"+minSep+")0.2"+lowerUpperSep+"max"+modifierSep+"1.3 m [n=2]", text);
    }

    @Test
    public void testFormatNoData() {
        //no data status
        QuantitativeData quantData = QuantitativeData.NewInstance(Feature.CHROMOSOME_NUMBER());
        FormatKey[] formatKey = null;
        QuantitativeDataFormatter formatter = new QuantitativeDataFormatter(quantData, formatKey);

        //fully empty
        String text = formatter.format(quantData, formatKey);
        Assert.assertEquals("", text);  //behavior may change in future

        //data unavailable
        quantData.setNoDataStatus(NoDescriptiveDataStatus.DataUnavailable);
        text = formatter.format(quantData, formatKey);
        Assert.assertEquals("data unavailable", text);

        //data unavailable, but still data exists
        quantData.addStatisticalValue(min1);
        quantData.addStatisticalValue(max1);
        quantData.addStatisticalValue(n1);
        MeasurementUnit unit = MeasurementUnit.METER();
        quantData.setUnit(unit);
        text = formatter.format(quantData, formatKey);
        Assert.assertEquals("data unavailable (0.1"+lowerUpperSep+"1.3 m [n=2])", text);
    }

    @Test
    public void testOrdering() {
        QuantitativeData quantData = QuantitativeData.NewInstance(Feature.CHROMOSOME_NUMBER());
        FormatKey[] formatKey = null;
        QuantitativeDataFormatter formatter = new QuantitativeDataFormatter(quantData, formatKey);

        StatisticalMeasurementValue val1 = StatisticalMeasurementValue.NewInstance(StatisticalMeasure.EXACT_VALUE(), new BigDecimal("2.1"));
        StatisticalMeasurementValue val2 = StatisticalMeasurementValue.NewInstance(StatisticalMeasure.EXACT_VALUE(), new BigDecimal("1.3"));
        StatisticalMeasurementValue val3 = StatisticalMeasurementValue.NewInstance(StatisticalMeasure.EXACT_VALUE(), new BigDecimal("5"));
        StatisticalMeasurementValue val4 = StatisticalMeasurementValue.NewInstance(StatisticalMeasure.EXACT_VALUE(), new BigDecimal("4"));
        quantData.addStatisticalValue(val1);
        quantData.addStatisticalValue(val2);
        quantData.addStatisticalValue(val3);
        quantData.addStatisticalValue(val4);
        MeasurementUnit unit = MeasurementUnit.METER();
        quantData.setUnit(unit);

        String text = formatter.format(quantData, formatKey);
        Assert.assertEquals("1.3;2.1;4;5 m", text);
    }
}