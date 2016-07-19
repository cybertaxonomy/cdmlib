// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author pplitzner
 * @date 16.06.2015
 *
 */
public class AbcdPersonParser {

    private final String prefix;

    private final SpecimenImportReport report;

    private final ICdmApplicationConfiguration cdmAppController;


    public AbcdPersonParser(String prefix, SpecimenImportReport report, ICdmApplicationConfiguration cdmAppController) {
        this.prefix = prefix;
        this.report = report;
        this.cdmAppController = cdmAppController;
    }


    public AgentBase<?> parse(Element item) {
        AgentBase<?> agentBase = null;
        NodeList fullNameList = item.getElementsByTagName(prefix+"FullName");
        String fullName = AbcdParseUtility.parseFirstTextContent(fullNameList);
        if(fullName!=null){
            List<AgentBase> matchingAgents = cdmAppController.getAgentService().findByTitle(AgentBase.class, fullName, MatchMode.EXACT, null, null, null, null, null).getRecords();
            if(matchingAgents.size()==1){
                agentBase = matchingAgents.iterator().next();
            }
            else{
                agentBase = Person.NewTitledInstance(fullName);
            }
        }
        return agentBase;
    }


}
