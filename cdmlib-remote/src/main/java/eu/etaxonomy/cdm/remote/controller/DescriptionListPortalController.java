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
@RequestMapping(value = {"/portal/description"})
public class DescriptionListPortalController extends DescriptionListController {

   public static final Logger logger = Logger.getLogger(DescriptionListPortalController.class);

    protected static final List<String> DESCRIPTION_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "elements.$",
            "elements.annotations",
            "elements.markers",
            "elements.stateData.$",
            "elements.sources.citation.authorship",
            "elements.sources.nameUsedInSource",
            "elements.multilanguageText",
            "elements.media",
            "elements.kindOfUnit"
    });


    protected static final List<String> DISTRIBUTION_INFO_INIT_STRATEGY = Arrays.asList(new String []{
            "sources.citation.authorship.$",
            "sources.nameUsedInSource",
            "annotations"
    });


    public DescriptionListPortalController() {
        super();
        setInitializationStrategy(DESCRIPTION_INIT_STRATEGY);
    }

    @Override
    protected List<String> getDescriptionInfoInitStrategy(){
        return DISTRIBUTION_INFO_INIT_STRATEGY;
    }

}