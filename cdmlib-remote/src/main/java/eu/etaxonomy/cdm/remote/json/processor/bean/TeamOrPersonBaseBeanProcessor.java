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

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @author a.kohlbecker
 * @since 20.10.2010
 *
 */
public class TeamOrPersonBaseBeanProcessor extends AbstractBeanProcessor<TeamOrPersonBase> {

    @Override
    public List<String> getIgnorePropNames() {
        // nothing to ignore by default
        return null;
    }


    @Override
    public JSONObject processBeanSecondStep(TeamOrPersonBase bean, JSONObject json,
            JsonConfig jsonConfig) {
            json.element("titleCache", bean.getTitleCache());
            json.element("nomenclaturalTitleCache", bean.getNomenclaturalTitleCache());
        return json;
    }

}
