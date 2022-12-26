/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.json.processor.value;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.api.service.l10n.KeyTerm_L10n;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.model.term.IEnumTerm;
import eu.etaxonomy.cdm.model.term.IKeyTerm;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

/**
 * @author a.kohlbecker
 */
public class KeyTermValueProcessor implements JsonValueProcessor  {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Object processArrayValue(Object value, JsonConfig jsonConfig) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object processObjectValue(String key, Object value, JsonConfig jsonConfig) {

        if(value == null) {
            return JSONNull.getInstance();
        }
        IKeyTerm term = (IKeyTerm)value;

        JSONObject json = new JSONObject();
        if(term.getClass().isEnum()) {
            json.element("name", term); // will be serialized as enum.name()
        }
        KeyTerm_L10n<?> keyTerm_L10n = new KeyTerm_L10n<>(term);
        json.element("representation_L10n", keyTerm_L10n.localizedLabel());
        if(IEnumTerm.class.isAssignableFrom(term.getClass())) {
            json.element("uuid", ((IEnumTerm)term).getUuid().toString());
        }
        if(term instanceof TaxonNodeStatus) {
            json.element("symbol", ((TaxonNodeStatus)term).getSymbol());
        }
        return json;
    }
}