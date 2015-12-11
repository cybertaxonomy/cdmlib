// $Id$
/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.controller;

import io.swagger.annotations.Api;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author a.kohlbecker
 * @date Jun 25, 2013
 *
 */
@Controller
@Api("portal_description")
@RequestMapping(value = {"/portal/descriptionElement"})
public class DescriptionElementListPortalController extends DescriptionElementListController {

   public static final Logger logger = Logger.getLogger(DescriptionElementListPortalController.class);


    protected static final List<String> DESCRIPTION_ELEMENT_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "annotations",
            "markers",
            "stateData.$",
            "statisticalValues.*",
            "sources.citation.authorship",
            "sources.nameUsedInSource",
            "multilanguageText",
            "media",
            "name.$",
            "name.rank.representations",
            "name.status.type.representations",
            "taxon2.name"
    });

    /**
     * @return
     */
    @Override
    protected List<String> getInitializationStrategy() {
        return DESCRIPTION_ELEMENT_INIT_STRATEGY;
    }

}