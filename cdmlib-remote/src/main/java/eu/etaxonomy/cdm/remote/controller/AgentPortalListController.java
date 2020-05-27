/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.Api;

/**
 * @author a.kohlbecker
 * @since May 27, 2020
 */
@Controller
@Api(value = "agent")
@RequestMapping(value = {"/portal/agent"})
public class AgentPortalListController extends AgentListController {

    public AgentPortalListController() {
        super();
        setInitializationStrategy(AgentPortalController.TEAM_OR_PERSON_BASE_INIT_STRATEGY);
    }

}
