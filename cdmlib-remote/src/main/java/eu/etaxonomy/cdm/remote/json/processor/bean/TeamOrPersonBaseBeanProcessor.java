/**
* Copyright (C) 2009 EDIT
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
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;

/**
 * @author a.kohlbecker
 * @date 20.10.2010
 *
 */
public class TeamOrPersonBaseBeanProcessor extends AbstractBeanProcessor<TeamOrPersonBase> {

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.json.processor.bean.AbstractBeanProcessor#getIgnorePropNames()
     */
    @Override
    public List<String> getIgnorePropNames() {
        // nothing to ignore by default
        return null;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.json.processor.bean.AbstractBeanProcessor#processBeanSecondStep(java.lang.Object, net.sf.json.JSONObject, net.sf.json.JsonConfig)
     */
    @Override
    public JSONObject processBeanSecondStep(TeamOrPersonBase bean, JSONObject json,
            JsonConfig jsonConfig) {
        json.element("titleCache", bean.getTitleCache());
        return json;
    }

}
