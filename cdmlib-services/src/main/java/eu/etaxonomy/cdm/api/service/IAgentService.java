// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;

public interface IAgentService extends IIdentifiableEntityService<AgentBase> {
	
	// FIXME Candidate for harmonization
	public abstract AgentBase getAgentByUuid(UUID uuid);

	// FIXME Candidate for harmonization
	public abstract UUID saveAgent(AgentBase agent);
	
	// FIXME Candidate for harmonization
	public abstract Map<UUID, AgentBase> saveAgentAll(Collection<? extends AgentBase> agentCollection);
	
	// FIXME Candidate for harmonization
	public abstract List<AgentBase> findAgentsByTitle(String title);

	// FIXME Candidate for harmonization
	public abstract List<AgentBase> getAllAgents(int limit, int start);
	
	public abstract List<Institution> searchInstitutionByCode(String code);
	
	/**
	 * Return a paged list of the institutional memberships held by a person
	 * 
	 * @param person the person
	 * @param pageSize The maximum number of memberships returned (can be null for all memberships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a Pager containing InstitutionalMembership  instances
	 */
	public Pager<InstitutionalMembership> getInstitutionalMemberships(Person person, Integer pageSize, Integer pageNumber);
	
	/**
	 * Return a paged list of the members of a team
	 * 
	 * @param team the team
	 * @param pageSize The maximum number of members returned (can be null for all members)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a Pager containing Person  instances
	 */
	public Pager<Person> getMembers(Team team, Integer pageSize, Integer pageNumber);
	
	/**
	 * Return a paged list of the addresses of an agent
	 * 
	 * @param agent the agent
	 * @param pageSize The maximum number of addresses returned (can be null for all members)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a Pager containing Address  instances
	 */
	public Pager<Address> getAddresses(AgentBase agent, Integer pageSize, Integer pageNumber);
}
