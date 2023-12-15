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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import io.swagger.annotations.Api;

/**
 * @author a.kohlbecker
 * @since 22.07.2010
 */
@Controller
@Api("termVocabulary")
@RequestMapping(value = {"/termVocabulary"})
public class VocabularyListController extends AbstractIdentifiableListController<TermVocabulary, IVocabularyService> {

    private static final Logger logger = LogManager.getLogger();
    private static final List<String> TERM_INIT_STRATEGY = Arrays.asList(
            new String[]{
                "terms.$",
                "terms.includes.$",
                "includes"
            });


    @Autowired
    @Override
    public void setService(IVocabularyService service) {
        this.service = service;
        setInitializationStrategy(TERM_INIT_STRATEGY);
    }


}
