/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.description;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.StateData;

/**
 * Formatter for {@link CategoricalData}.
 *
 * @author a.mueller
 * @since 11.03.2020
 */
public class CategoricalDataFormatter
        extends DesciptionElementFormatterBase<CategoricalData>{

    private static final String modifierSeparator = " ";

    public static final CategoricalDataFormatter NewInstance(FormatKey[] formatKeys) {
        return new CategoricalDataFormatter(null, formatKeys);
    }

    public CategoricalDataFormatter(Object object, FormatKey[] formatKeys) {
        super(object, formatKeys, CategoricalData.class);
    }

    @Override
    protected String doFormat(CategoricalData catData, List<Language> preferredLanguages) {
        List<StateData> stateDatas = catData.getStateData();
        String stateDataText = getStateDatasText(stateDatas, preferredLanguages);
        if (catData.getNoDataStatus() != null) {
            String noDataStatus = catData.getNoDataStatus().getLabel();
            stateDataText = StringUtils.isEmpty(stateDataText) ? noDataStatus : noDataStatus +" (" + stateDataText + ")";
        }
        return stateDataText;
    }

    private String getStateDatasText(List<StateData> stateDatas, List<Language> preferredLanguages) {
        String result = "";
        for (StateData stateData : stateDatas){
            result = CdmUtils.concat(", ", result, getStateDataText(stateData, preferredLanguages));
        }

        return result;
    }

    private String getStateDataText(StateData stateData, List<Language> preferredLanguages) {
        String result = handleModifiers(
                getLabel(stateData.getState(), preferredLanguages, stateData.isUsePlural()),
                stateData, preferredLanguages, modifierSeparator);

        return result;
    }
}