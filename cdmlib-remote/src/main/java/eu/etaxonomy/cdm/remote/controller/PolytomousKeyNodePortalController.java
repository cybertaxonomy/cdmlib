/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.api.service.IPolytomousKeyNodeService;
import io.swagger.annotations.Api;

/**
 * @author a.kohlbecker
 * @since Sep 9, 2020
 */
@Controller
@Api(value="portal_polytomousKeyNode")
@RequestMapping(value = {"/portal/polytomousKeyNode/{uuid}"})
public class PolytomousKeyNodePortalController extends PolytomousKeyNodeController {

    private static final List<String> NODE_INIT_STRATEGY = Arrays.asList(new String[]{
            "$",
            "question.label",
            "statement.label",
            "children.statement.label",
            "children.subkey",
            "otherNode",
            "taxon.name.nomenclaturalSource.citation.authorship",
            "taxon.name.nomenclaturalSource.citation.inReference.authorship",
            "subkey.$"
    });

    public PolytomousKeyNodePortalController() {
        super();
        setInitializationStrategy(NODE_INIT_STRATEGY);
    }

    @Override
    @Autowired
    public void setService(IPolytomousKeyNodeService service) {
        this.service = service;
    }


}
