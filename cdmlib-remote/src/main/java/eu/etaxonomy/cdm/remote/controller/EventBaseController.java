/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.api.service.IEventBaseService;
import eu.etaxonomy.cdm.model.common.EventBase;
import io.swagger.annotations.Api;

/**
 * @author a.kohlbecker
 */
@Controller
@Api("eventBase")
@RequestMapping(value = {"/eventBase/{uuid}"})
public class EventBaseController extends BaseController<EventBase, IEventBaseService> {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @Override
    @Autowired
    public void setService(IEventBaseService service) {
        this.service = service;
    }
}