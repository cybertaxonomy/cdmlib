/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.Collection;
import java.util.List;

import eu.etaxonomy.cdm.common.TreeNode;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @author a.kohlbecker
 * @since Nov 21, 2013
 *
 */
public class TreeNodeBeanProcessor extends AbstractBeanProcessor<TreeNode> {

    @Override
    public List<String> getIgnorePropNames() {
        // nothing to ignore by default
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public JSONObject processBeanSecondStep(TreeNode bean, JSONObject json, JsonConfig jsonConfig) {

        Object data = null;
        try {
            data = json.get("data");
        } catch (JSONException e) {
            // do data? so continue
            return json;
        }

        // data is not null here
        if(data instanceof Collection){
            Collection collection = (Collection) data;
            for (Object dataEnty : collection) {
                if(dataEnty instanceof JSONObject){
                    JSONObject dataEntyJson = (JSONObject)dataEnty;

                    // handle Distributions:
                    // remove the area to prevent it from being serialized
                    // the area is already in the TreeNode.nodeId field
                    // so we avoid redundancy be this means
                    dataEntyJson.remove("area");

                }

            }
        }
        return json;
    }

}
