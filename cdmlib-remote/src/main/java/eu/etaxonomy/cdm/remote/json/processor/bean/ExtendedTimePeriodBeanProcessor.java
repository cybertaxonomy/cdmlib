/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.List;

import eu.etaxonomy.cdm.model.common.ExtendedTimePeriod;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @author a.kohlbecker
 * @since Feb 11, 2022
 */
public class ExtendedTimePeriodBeanProcessor extends AbstractBeanProcessor<ExtendedTimePeriod> {

    @Override
    public List<String> getIgnorePropNames() {
        return null;
    }

    @Override
    public JSONObject processBeanSecondStep(ExtendedTimePeriod bean, JSONObject json, JsonConfig jsonConfig) {
        json.element("label", bean.toString());
        return json;
    }

}