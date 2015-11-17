// $Id$
/**
* Copyright (C) 2015 EDIT
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

import com.wordnik.swagger.annotations.Api;

/**
 * @author a.kohlbecker
 * @date Nov 16, 2015
 *
 */
@Controller
@Api("portal_agent")
@RequestMapping(value = {"/portal/agent/{uuid}"})
public class AgentPortalController extends AgentController {

    private static final List<String> TEAM_OR_PERSON_BASE_INIT_STRATEGY = Arrays.asList(new String[]{
            // AgentBase
//            "contact.urls",
//            "contact#phoneNumbers",
//            "contact#addresses",
//            "contact#faxNumbers",
//            "contact#emailAddresses",
            // Person
            "institutionalMemberships.$",
            // Team
            "teamMembers.$"
    });

    /**
    *
    */
   public AgentPortalController() {
       super();
       setInitializationStrategy(TEAM_OR_PERSON_BASE_INIT_STRATEGY);
   }
}
