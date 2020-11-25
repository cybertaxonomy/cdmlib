/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.List;

import eu.etaxonomy.cdm.model.name.NomenclaturalSource;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;


public class NomenclaturalSourceBeanProcessor extends AbstractBeanProcessor<NomenclaturalSource> {

    @Override
    public List<String> getIgnorePropNames() {
        return null;
    }


    @Override
    public JSONObject processBeanSecondStep(NomenclaturalSource bean, JSONObject json, JsonConfig jsonConfig) {

        if(bean.getCitation() != null) {
            json.element("nomenclaturalCitation", bean.getCitation().getNomenclaturalCitation(bean.getCitationMicroReference()));
        }
        return json;
    }



}
