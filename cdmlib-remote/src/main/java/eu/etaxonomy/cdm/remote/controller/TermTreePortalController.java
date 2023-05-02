/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.etaxonomy.cdm.model.term.TermTree;
import io.swagger.annotations.Api;

/**
 * @author a.kohlbecker
 * @since Jun 24, 2013
 */
@Controller
@Api("portal_termTree")
@RequestMapping(value = {"/portal/termTree/{uuid}"})
public class TermTreePortalController extends TermTreeController {

    private static final Logger logger = LogManager.getLogger();

    private static final List<String> TERMTREE_INIT_STRATEGY = Arrays.asList(
            new String[]{
                    "representations",
            });

    private List<String> termTreeNodeInitStrategy = null;

    public TermTreePortalController() {
        setInitializationStrategy(TERMTREE_INIT_STRATEGY);

        termTreeNodeInitStrategy = new ArrayList<>(2);
        termTreeNodeInitStrategy.add("term.representations");
    }

    @Override
    @RequestMapping(method = RequestMethod.GET)
    public TermTree doGet(@PathVariable("uuid") UUID uuid,
                HttpServletRequest request,
                HttpServletResponse response) throws IOException {
        if(request != null) {
            logger.info("doGet() " + request.getRequestURI());
        }
        TermTree<?> termTree = null;
        try {
            termTree = service.loadWithNodes(uuid, getInitializationStrategy(), termTreeNodeInitStrategy);
        } catch(EntityNotFoundException e){
            HttpStatusMessage.UUID_NOT_FOUND.send(response);
        }
        return termTree;
    }
}
