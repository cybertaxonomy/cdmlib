// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import io.swagger.annotations.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.api.service.IPolytomousKeyNodeService;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;

/**
 * @author a.kohlbecker
 * @date 24.03.2011
 *
 */
@Controller
@Api("polytomousKeyNode")
@RequestMapping(value = {"/polytomousKeyNode"})
public class PolytomousKeyNodeListController extends BaseListController<PolytomousKeyNode, IPolytomousKeyNodeService> {

    @Override
    @Autowired
    public void setService(IPolytomousKeyNodeService service) {
        this.service = service;
    }
}
