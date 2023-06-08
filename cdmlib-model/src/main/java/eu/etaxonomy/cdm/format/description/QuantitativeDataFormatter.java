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
import java.util.List;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;

/**
 * Formatter for {@link QuantitativeData}.
 *
 * @author a.mueller
 * @since 12.03.2020
 */
public class QuantitativeDataFormatter
            extends DesciptionElementFormatterBase<QuantitativeData> {

    static final String sepDash = "-";   //TODO which "-"
    static final String minSep = UTF8.NARROW_NO_BREAK + sepDash;
    static final String maxSep = sepDash + UTF8.NARROW_NO_BREAK;
    public static final String lowerUpperSep = UTF8.NARROW_NO_BREAK + sepDash + UTF8.NARROW_NO_BREAK;

    static final String modifierSep = UTF8.NARROW_NO_BREAK.toString();

    public static final QuantitativeDataFormatter NewInstance(FormatKey[] formatKeys) {
        return new QuantitativeDataFormatter(null, formatKeys);
    }

    public QuantitativeDataFormatter(Object object, FormatKey[] formatKeys) {
        super(object, formatKeys, QuantitativeData.class);
    }

    @Override
    protected String doFormat(QuantitativeData quantData, List<Language> preferredLanguages) {

        String result = "";

        //values
        StatisticalMeasurementValue minValue = quantData.getSpecificStatisticalValueAsSMV(StatisticalMeasure.MIN());
        StatisticalMeasurementValue lowerValue = quantData.getSpecificStatisticalValueAsSMV(StatisticalMeasure.TYPICAL_LOWER_BOUNDARY());
        StatisticalMeasurementValue maxValue = quantData.getSpecificStatisticalValueAsSMV(StatisticalMeasure.MAX());
        StatisticalMeasurementValue upperValue = quantData.getSpecificStatisticalValueAsSMV(StatisticalMeasure.TYPICAL_UPPER_BOUNDARY());
        if (lowerValue == null && minValue != null) {
            lowerValue = minValue;
            minValue = null;
        }
        if (upperValue == null && maxValue != null) {
            upperValue = maxValue;
            maxValue = null;
        }

        BigDecimal lowerBD = lowerValue != null ? lowerValue.getValue() : null;
        BigDecimal upperBD = upperValue != null ? upperValue.getValue() : null;
        String lower = null;
        String upper = null;
        if (lowerBD != null) {
            lower = handleModifiers(String.valueOf(lowerBD), lowerValue, preferredLanguages, modifierSep);
            if (minValue != null) {
                BigDecimal minBD = minValue.getValue();
                String minBDStr = minBD == null ? null : String.valueOf(minBD);
                String min = handleModifiers(minBDStr, minValue, preferredLanguages, modifierSep);
                if (isNotBlank(min)) {
                    lower = "("+min+ minSep + ")" + lower;
                }
            }
        }
        if (upperBD != null) {
            upper = handleModifiers(String.valueOf(upperBD), upperValue, preferredLanguages, modifierSep);
            if (maxValue != null) {
                BigDecimal maxBD = maxValue.getValue();
                String maxBDStr = (maxBD == null ? null : String.valueOf(maxBD));
                String max = handleModifiers(maxBDStr, maxValue, preferredLanguages, modifierSep);
                if (isNotBlank(max)) {
                    upper = upper + "("+ maxSep + max+")";
                }
            }
        }

        String minMax = "";
        if (lower != null){
            minMax = lower;
            if (upper != null && !lower.equals(upper)){
                minMax = CdmUtils.concat(lowerUpperSep, minMax, upper);
            }
        }else if (upper != null){
            minMax = "<" + upper;
        }
        String exactValueStr = "";
        for(BigDecimal exactValue : quantData.getExactValues()){
            if (exactValue != null){
                exactValueStr = CdmUtils.concat(";", exactValueStr, String.valueOf(exactValue));
            }
        }
        if (isNotBlank(minMax)){
            result = minMax;
            if (isNotBlank(exactValueStr)){
                result = result + "(" + exactValueStr + ")";
            }
        }else if(isNotBlank(exactValueStr)){
            result = exactValueStr;
        }

        //unit
        MeasurementUnit unit = quantData.getUnit();
        //TODO
        String unitStr = (unit != null) ? unit.getIdInVocabulary(): null;
        result = CdmUtils.concat(" ", result, unitStr);

        //bracket
        BigDecimal n = quantData.getSampleSize();
        String size = (n == null) ? "" : "n="+String.valueOf(n);
        String strBracket = isNotBlank(size) ? "[" + size + "]" : "";

        //modif

        result = CdmUtils.concat(" ", result, strBracket);
        return result;
    }
}