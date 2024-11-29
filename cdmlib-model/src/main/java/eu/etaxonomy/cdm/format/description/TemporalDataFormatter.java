/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.description;

import java.util.List;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.TemporalData;

/**
 * @author muellera
 * @since 27.11.2024
 */
public class TemporalDataFormatter
        extends DesciptionElementFormatterBase<TemporalData>{

    private static final String modifierSeparator = " ";

    public static final TemporalDataFormatter NewInstance() {
        return new TemporalDataFormatter(null, null);
    }

    private TemporalDataFormatter(Object object, FormatKey[] formatKeys) {
        super(object, formatKeys, TemporalData.class);
    }

    @Override
    protected String doFormat(TemporalData tempData, List<Language> preferredLanguages) {

        String result;
        if (tempData.getPeriod() != null){
            result = tempData.getPeriod().toString();
        }else{
            result = tempData.toString();
        }

        handleModifiers(result, tempData, preferredLanguages, modifierSeparator);

        return result;
    }
}