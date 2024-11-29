/**
* Copyright (C) 2009 EDIT
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

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import io.swagger.annotations.Api;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @since 23.06.2009
 *
 */
@Controller
@Api("term")
@RequestMapping(value = {"/term"})
public class TermListController extends AbstractIdentifiableListController<DefinedTermBase, ITermService> {

    private static final List<String> TERM_INIT_STRATEGY = Arrays.asList(new String []{
            "vocabulary",
            "includes",
            "terms.$",
            "terms.includes.$",
    });


    @Autowired
    @Override
    public void setService(ITermService service) {
        this.service = service;
        setInitializationStrategy(TERM_INIT_STRATEGY);
    }


}
