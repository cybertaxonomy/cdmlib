/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.agent;

import java.util.List;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;

public interface IAgentDao extends IIdentifiableDao<Agent> {
	
	public List<Institution> getInstitutionByCode(String code);
	
}
