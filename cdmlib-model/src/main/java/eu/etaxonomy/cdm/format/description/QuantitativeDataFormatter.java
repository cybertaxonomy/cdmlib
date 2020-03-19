/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.description;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.QuantitativeData;

/**
 * Formatter for {@link QuantitativeData}.
 *
 * @author a.mueller
 * @since 12.03.2020
 */
public class QuantitativeDataFormatter
            extends DesciptionElementFormatterBase<QuantitativeData> {

    protected static final String MISSING_TERM_LABEL = "-no state-"; //TODO

    public QuantitativeDataFormatter(Object object, FormatKey[] formatKeys) {
        super(object, formatKeys, QuantitativeData.class);
    }

    @Override
    public String doFormat(QuantitativeData quantData) {

        String result = "";

        //values
        Float min = quantData.getMin();
        Float max = quantData.getMax();
        String minMax = "";
        if (min != null){
            minMax = String.valueOf(min);
            if (max!= null && !min.equals(max)){
                minMax = CdmUtils.concat("-", minMax, String.valueOf(max));  //TODO which "-"
            }
        }else if (max != null){
            minMax = "<" + String.valueOf(max);
        }
        String exactValueStr = "";
        for(Float exactValue : quantData.getExactValues()){
            if (exactValue != null){
                exactValueStr += CdmUtils.concat(";", exactValueStr, String.valueOf(exactValue));
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
        String unitStr = unit != null ? unit.getIdInVocabulary(): null;
        result = CdmUtils.concat(" ", result, unitStr);

        //bracket
        Float n = quantData.getSampleSize();
        String size = (n == null) ? "" : "n="+String.valueOf(n);
        String strBracket = isNotBlank(size) ? "[" + size + "]" : "";

        result = CdmUtils.concat(" ", result, strBracket);
        return result;
    }

}
