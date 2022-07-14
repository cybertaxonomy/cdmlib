/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.value;

import java.util.Objects;

import eu.etaxonomy.cdm.api.service.l10n.TermRepresentation_L10n;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationSet;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

/**
 * @author a.mueller
 * @date 13.07.2022
 */
public class TypeDesignationSetValueProcessor implements JsonValueProcessor {

    @Override
    public Object processArrayValue(Object value, JsonConfig jsonConfig) {
        if (value == null) {
            return JSONNull.getInstance();
        }
        TypeDesignationSet tds = (TypeDesignationSet)value;
        JSONObject json = new JSONObject();
        for(TypeDesignationStatusBase<?> key : tds.keySet()){
            TermRepresentation_L10n term_L10n = new TermRepresentation_L10n(key, false);
            String label = Objects.toString(term_L10n.getLabel(), "NULL");
            json.element(label, tds.get(key), jsonConfig);
        }
        return json;
    }

    @Override
    public Object processObjectValue(String arg0, Object value, JsonConfig jsonConfig) {
        return processArrayValue(value, jsonConfig);
    }
}