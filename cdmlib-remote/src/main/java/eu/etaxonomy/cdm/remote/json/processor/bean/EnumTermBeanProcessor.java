/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.taxon.TaxonNodeStatus;
import eu.etaxonomy.cdm.model.term.IEnumTerm;
import eu.etaxonomy.cdm.remote.l10n.EnumTerm_L10n;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @author a.kohlbecker
 *
 */
public class EnumTermBeanProcessor<T extends IEnumTerm> extends AbstractBeanProcessor<T> {

    public static final Logger logger = Logger.getLogger(EnumTermBeanProcessor.class);

    private static final List<String> IGNORE_LIST = new ArrayList<>();



    @Override
    public List<String> getIgnorePropNames() {
        return IGNORE_LIST;
    }

    @Override
    public JSONObject processBeanSecondStep(T bean, JSONObject json, JsonConfig jsonConfig) {

        //FIXME: WARNING the below implementation is preliminary, DO NOT USE JET!!!!
        EnumTerm_L10n<T> l10n = new EnumTerm_L10n<T>(bean);
        String message_L10n = l10n.localizedMessage();
        if(message_L10n != null) {
            json.element("message_L10n", message_L10n, jsonConfig);
        }
        if(bean instanceof TaxonNodeStatus) {
            TaxonNodeStatus tns = (TaxonNodeStatus)bean;
            if(tns.getSymbol() != null) {
                json.element("symbol", message_L10n, jsonConfig);
            }
        }
        return json;
    }

}
