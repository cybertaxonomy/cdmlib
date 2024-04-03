/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.List;

import eu.etaxonomy.cdm.api.nameMatching.NameMatchingCombinedResult;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @author muellera
 * @since 03.04.2024
 */
public class NameMatchingResultBeanProcessor extends AbstractBeanProcessor<NameMatchingCombinedResult> {

    @Override
    public List<String> getIgnorePropNames() {
        return null;
    }

    @Override
    public JSONObject processBeanSecondStep(NameMatchingCombinedResult bean, JSONObject json, JsonConfig jsonConfig) {
        json.element("exactMatches", bean.getExactMatches(), jsonConfig);
        json.element("candidates", bean.getCandidates(), jsonConfig);
        return json;
    }
}