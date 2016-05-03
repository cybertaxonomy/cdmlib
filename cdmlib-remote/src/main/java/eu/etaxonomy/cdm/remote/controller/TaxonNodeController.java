// $Id$
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

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author n.hoffmann
 * @created Apr 9, 2010
 * @version 1.0
 */
@Controller
@Api("taxonNode")
@RequestMapping(value = {"/taxonNode/{uuid}"})
public class TaxonNodeController extends BaseController<TaxonNode, ITaxonNodeService> {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(TaxonNodeController.class);

    private static final List<String> NODE_INIT_STRATEGY = Arrays.asList(new String[]{
            "taxon.sec",
            "taxon.name"
    });

    public TaxonNodeController(){
        super();
        setInitializationStrategy(NODE_INIT_STRATEGY);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.BaseController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Override
    @Autowired
    public void setService(ITaxonNodeService service) {
        this.service = service;
    }
}
