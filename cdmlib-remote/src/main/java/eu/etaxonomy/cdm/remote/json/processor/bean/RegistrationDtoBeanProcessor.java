/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.Arrays;
import java.util.List;

import eu.etaxonomy.cdm.api.service.dto.RegistrationDTO;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @author a.kohlbecker
 * @since Oct 8, 2020
 */
public class RegistrationDtoBeanProcessor extends AbstractBeanProcessor<RegistrationDTO> {

    @Override
    public List<String> getIgnorePropNames() {
        return Arrays.asList(
                "blockedBy",
                "blocked",
                "submitterUserName"
                );
    }



    @Override
    public JSONObject processBeanSecondStep(RegistrationDTO bean, JSONObject json, JsonConfig jsonConfig) {
        // nothing to do here
        return json;
    }

}
