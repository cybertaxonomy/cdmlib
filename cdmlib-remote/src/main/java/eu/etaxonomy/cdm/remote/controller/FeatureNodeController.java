/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.controller;

import io.swagger.annotations.Api;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.api.service.IFeatureNodeService;
import eu.etaxonomy.cdm.model.description.FeatureNode;

/**
 * @author n.hoffmann
 * @since Aug 6, 2010
 * @version 1.0
 */
@Controller
@Api("featureNode")
@RequestMapping(value = {"/featureNode/{uuid}"})
public class FeatureNodeController extends BaseController<FeatureNode, IFeatureNodeService> {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(FeatureNodeController.class);

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.BaseController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Override
    @Autowired
    public void setService(IFeatureNodeService service) {
        this.service = service;
    }
}
