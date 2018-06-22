/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.List;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @author a.kohlbecker
 * @since Nov 21, 2013
 *
 */
public class DefaultBeanProcessor extends AbstractBeanProcessor<Object> {

    @Override
    public List<String> getIgnorePropNames() {
        // nothing to ignore by default
        return null;
    }

    @Override
    public JSONObject processBeanSecondStep(Object bean, JSONObject jsonObj, JsonConfig jsonConfig) {
        return jsonObj;
    }

}
