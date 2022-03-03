/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.api.service.ITermNodeService;
import eu.etaxonomy.cdm.model.term.TermNode;
import io.swagger.annotations.Api;

/**
 * @author n.hoffmann
 * @since Aug 6, 2010
 */
@Controller
@Api("termNode")
@RequestMapping(value = {"/termNode/{uuid}"})
public class TermNodeController extends BaseController<TermNode, ITermNodeService> {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TermNodeController.class);

    @Override
    @Autowired
    public void setService(ITermNodeService service) {
        this.service = service;
    }
}
