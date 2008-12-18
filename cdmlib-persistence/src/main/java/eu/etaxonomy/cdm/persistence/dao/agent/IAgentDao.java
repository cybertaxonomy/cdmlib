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

import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;

public interface IAgentDao extends IIdentifiableDao<Agent> {
	
	public List<Institution> getInstitutionByCode(String code);
	
//  TODO Currently Contact is a property of Person or Institution, but according 
//	to http://rs.tdwg.org/ontology/voc/Team, teams should have a Contact too - so
//  implementation of these methods is dependent upon a bit of refactoring in cdmlib-model
//	List<Address> getAddresses(Agent agent, Integer pageSize, Integer pageNumber);
//  int countAddresses(Agent agent);
	
	public List<InstitutionalMembership> getInstitutionalMemberships(Person person, Integer pageSize, Integer pageNumber);
	
	public int countInstitutionalMemberships(Person person);
	
	public List<Person> getMembers(Team team, Integer pageSize, Integer pageNumber);
	
	public int countMembers(Team team);
}
