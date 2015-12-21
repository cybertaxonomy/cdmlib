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

import io.swagger.annotations.Api;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

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
            // NOTE: all other cases are covered in the TaxonNodeDaoHibernateImpl method
            // which is using join fetches
            // AgentBase
            "contact.*",
            // Person
            "institutionalMemberships.$",
            "institutionalMemberships.institute.contact.*",
            // Team
            "teamMembers.$"
    });

    private static final List<String> TAXONNODEAGENTRELATIONS_INIT_STRATEGY = Arrays.asList(new String[]{
            // NOTE: all other cases are covered in the TaxonNodeDaoHibernateImpl method
            // which is using join fetches
            "taxonNode.taxon.name.nomenclaturalReference",
            // AgentBase
            "agent.contact.*",
            // Person
            "agent.institutionalMemberships.$",
            "agent.institutionalMemberships.institute.contact.*",
            // Team
            "agent.teamMembers.$"
            });

    @Override
    public List<String> getTaxonNodeAgentRelationsInitStrategy() {
        return TAXONNODEAGENTRELATIONS_INIT_STRATEGY;
    }

    /**
    *
    */
   public AgentPortalController() {
       super();
       setInitializationStrategy(TEAM_OR_PERSON_BASE_INIT_STRATEGY);
   }
}
