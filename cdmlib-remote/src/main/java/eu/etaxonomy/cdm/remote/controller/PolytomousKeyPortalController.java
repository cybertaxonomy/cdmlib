/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IPolytomousKeyService;
import eu.etaxonomy.cdm.model.description.PolytomousKey;

/**
 * @author a.kohlbecker
 * @since 24.03.2011
 * @Deprecated ONLY FOR TESTING PURPOSES
 */
@Deprecated
@Controller
@Api(value="portal_polytomousKeyNode", description="Deprecated !")
@RequestMapping(value = {"/portal/polytomousKey/{uuid}"})
public class PolytomousKeyPortalController extends BaseController<PolytomousKey, IPolytomousKeyService> {
    public static final Logger logger = Logger.getLogger(PolytomousKeyPortalController.class);

    @Override
    @Autowired
    public void setService(IPolytomousKeyService service) {
        this.service = service;
    }

    /**
     * @param uuid
     * @param request
     * @param response
     * @return
     * @throws IOException
     * @Deprecated ONLY FOR TESTING PURPOSES
     */
    @Deprecated
    @ApiOperation(notes="ONLY FOR TESTING PURPOSES", value = "/portal/polytomousKey/{uuid}/loadWithNodes")
    @RequestMapping(value = {"loadWithNodes"}, method = RequestMethod.GET)
    public ModelAndView doLoadWithNodes(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doLoadWithNodes() - " + request.getRequestURI());

        ModelAndView mv = new ModelAndView();

        List<String> nodePaths = new ArrayList<String>();
        nodePaths.add("subkey");
        nodePaths.add("taxon.name.nomenclaturalReference");

        List<String> propertyPaths = new ArrayList<String>();
        propertyPaths.add("sources");
        propertyPaths.add("annotations");

        PolytomousKey key = service.loadWithNodes(uuid, propertyPaths, nodePaths);
        mv.addObject(key);
        return mv;

    }

}

