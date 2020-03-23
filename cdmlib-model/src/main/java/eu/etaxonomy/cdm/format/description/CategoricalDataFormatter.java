/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.description;

import java.util.ArrayList;
import java.util.List;

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

    public CategoricalDataFormatter(Object object, FormatKey[] formatKeys) {
        super(object, formatKeys, CategoricalData.class);
    }

    @Override
    public String doFormat(CategoricalData catData) {

        List<StateData> stateDatas = catData.getStateData();
         //TODO add as method argument
        List<Language> preferredLanguages = new ArrayList<>();
        preferredLanguages.add(Language.DEFAULT());
        String stateDataText = getStateDatasText(stateDatas, preferredLanguages);
        return stateDataText;
    }

    private String getStateDatasText(List<StateData> stateDatas, List<Language> preferredLanguages) {
        String result = "";
        for (StateData stateData : stateDatas){
            result = CdmUtils.concat(", ", result, getStateDataText(stateData, preferredLanguages));
        }
        //TODO add modifier text
        return result;
    }

    private String getStateDataText(StateData stateData, List<Language> preferredLanguages) {
        String result = "";
        result += getLabel(stateData.getState(), preferredLanguages);
        //TODO modifier
        return result;
    }


}
