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

import io.swagger.annotations.Api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.etaxonomy.cdm.model.description.FeatureTree;

/**
 * @author a.kohlbecker
 * @date Jun 24, 2013
 *
 */
@Controller
@Api("portal_featureTree")
@RequestMapping(value = {"/portal/featureTree/{uuid}"})
public class FeatureTreePortalController extends FeatureTreeController {

    public static final Logger logger = Logger.getLogger(FeatureTreePortalController.class);


    private static final List<String> FEATURETREE_INIT_STRATEGY = Arrays.asList(
            new String[]{
                    "representations",
            });

    private List<String> featuretreeNodeInitStrategy = null;

    public FeatureTreePortalController() {
        setInitializationStrategy(FEATURETREE_INIT_STRATEGY);

        featuretreeNodeInitStrategy = new ArrayList<String>(2);
//        featuretreeNodeInitStrategy.add("representations");
        featuretreeNodeInitStrategy.add("feature.representations");
    }

    @Override
    @RequestMapping(method = RequestMethod.GET)
    public FeatureTree doGet(@PathVariable("uuid") UUID uuid,
                HttpServletRequest request,
                HttpServletResponse response) throws IOException {
        if(request != null) {
            logger.info("doGet() " + request.getRequestURI());
        }
        FeatureTree featureTree = null;
        try {
            featureTree = service.loadWithNodes(uuid, getInitializationStrategy(), featuretreeNodeInitStrategy);
        } catch(EntityNotFoundException e){
            HttpStatusMessage.UUID_NOT_FOUND.send(response);
        }
        return featureTree;
    }
}
