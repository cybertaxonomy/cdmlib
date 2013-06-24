// $Id$
/**
* Copyright (C) 2013 EDIT
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

/**
 * @author a.kohlbecker
 * @date Jun 24, 2013
 *
 */
@Controller
@RequestMapping(value = {"/portal/featureTree/{uuid}"})
public class FeatureTreePortalController extends FeatureTreeController {

    private static final List<String> FEATURETREE_INIT_STRATEGY = Arrays.asList(
            new String[]{
                    "representations",
                    "root.feature.representations",
                    "root.children.feature.representations",
                    "root.children.children.feature.representations",
            });

    public FeatureTreePortalController() {
        setInitializationStrategy(FEATURETREE_INIT_STRATEGY);
    }

}
