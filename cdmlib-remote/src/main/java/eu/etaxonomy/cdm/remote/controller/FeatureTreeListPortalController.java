/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import io.swagger.annotations.Api;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author a.kohlbecker
 * @since Jun 24, 2013
 *
 */
@Controller
@Api("portal_featureTree")
@RequestMapping(value = {"/portal/featureTree"})
public class FeatureTreeListPortalController extends FeatureTreeListController {

    private static final List<String> FEATURETREE_INIT_STRATEGY = Arrays.asList(
            new String[]{
                "representations",
                "root.feature.representations",
                "root.childNodes.feature.representations"
            });

    public FeatureTreeListPortalController() {
        setInitializationStrategy(FEATURETREE_INIT_STRATEGY);
    }

}
