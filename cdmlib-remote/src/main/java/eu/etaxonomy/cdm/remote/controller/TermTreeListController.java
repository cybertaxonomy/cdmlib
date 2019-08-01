/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.api.service.ITermTreeService;
import eu.etaxonomy.cdm.model.term.TermTree;
import io.swagger.annotations.Api;


@Controller
@Api("termTree")
@RequestMapping(value = {"/featureTree", "/termTree"})
public class TermTreeListController extends AbstractIdentifiableListController<TermTree, ITermTreeService> {

    @Override
    @Autowired
    public void setService(ITermTreeService service) {
        this.service = service;
    }
}
