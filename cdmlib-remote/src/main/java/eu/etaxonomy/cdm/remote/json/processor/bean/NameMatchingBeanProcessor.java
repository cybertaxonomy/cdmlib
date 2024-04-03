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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.api.service.NameMatchingServiceImpl.SingleNameMatchingResult;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @author andreabee90
 * @since 12.03.2024
 */
public class NameMatchingBeanProcessor extends AbstractBeanProcessor<SingleNameMatchingResult> {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @Override
    public List<String> getIgnorePropNames() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JSONObject processBeanSecondStep(SingleNameMatchingResult bean, JSONObject json, JsonConfig jsonConfig) {
        json.element("nameCache", bean.getNameCache(), jsonConfig);
        json.element("distance", bean.getDistance(), jsonConfig);
        json.element("genusOrUninomial", bean.getGenusOrUninomial(), jsonConfig);
        json.element("infragenericEpithet", bean.getInfraGenericEpithet(), jsonConfig);
        json.element("specificEpithet", bean.getSpecificEpithet(), jsonConfig);
        json.element("infraspecificEpithet", bean.getInfraSpecificEpithet(), jsonConfig);
        json.element("authorshipCache", bean.getAuthorshipCache(), jsonConfig);
        json.element("titleCache", bean.getTitleCache(), jsonConfig);
        json.element("taxonNameUUID", bean.getTaxonNameUuid(), jsonConfig);
        json.element("taxonNameID", bean.getTaxonNameId(), jsonConfig);
        return json;
    }


}
