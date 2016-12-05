/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.Api;

/**
 * @author a.kohlbecker
 * @date Dec 1, 2016
 *
 */
@Controller
@Api("portal_term")
@RequestMapping(value = {"/portal/term/{uuid}"})
public class TermPortalController extends TermController {

    private static final List<String> DEFAULT_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "vocabulary",
            "countries" // NamedArea.countries
    });

    public TermPortalController() {
        setInitializationStrategy(DEFAULT_INIT_STRATEGY);
    }




}
