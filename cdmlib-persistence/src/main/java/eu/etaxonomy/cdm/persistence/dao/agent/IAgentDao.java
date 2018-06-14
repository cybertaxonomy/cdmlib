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
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

public interface IAgentDao extends IIdentifiableDao<AgentBase> {

	public List<Institution> getInstitutionByCode(String code);

	/**
	 * Return a List of the institutional memberships of a given person
	 *
	 * @param person the person
	 * @param pageSize The maximum number of institutional memberships returned (can be null for all memberships)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of InstitutionalMembership instances
	 */
	public List<InstitutionalMembership> getInstitutionalMemberships(Person person, Integer pageSize, Integer pageNumber);

	/**
	 * Return a count of institutional memberships held by a person
	 *
	 * @param person the person
	 * @return a count of InstitutionalMembership instances
	 */
	public long countInstitutionalMemberships(Person person);

	/**
	 * Return a List of members of a given team
	 *
	 * @param team the team
	 * @param pageSize The maximum number of people returned (can be null for all members)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of Person instances
	 */
	public List<Person> getMembers(Team team, Integer pageSize, Integer pageNumber);

	/**
	 * Return a count of members of a given team
	 *
	 * @param team the team
	 * @return a count of Person instances
	 */
	public long countMembers(Team team);

	/**
	 * Return a count of addresses of a given agent
	 *
	 * @param agent the agent
	 * @return a count of Address instances
	 */
	public long countAddresses(AgentBase agent);

	/**
	 * Return a List of addresses of a given agent
	 *
	 * @param agent the agent
	 * @param pageSize The maximum number of addresses returned (can be null for all addresses)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @return a List of Address instances
	 */
	public List<Address> getAddresses(AgentBase agent, Integer pageSize,Integer pageNumber);

	/**
	 * Returns a list of <code>UuidAndTitleCache</code> containing all <code>Person</code>s
	 *
	 * @return a list of <code>UuidAndTitleCache</code> instances
	 */
	public List<UuidAndTitleCache<Person>> getPersonUuidAndTitleCache();

	/**
	 * Returns a list of <code>UuidAndTitleCache</code> containing all <code>TeamOrPersonBase</code> obejcts
	 * with their respective nomenclaturalTitle instead of regular titleCache
	 *
	 * @return a list of <code>UuidAndTitleCache</code> instances
	 */
	public List<UuidAndTitleCache<Team>> getTeamUuidAndNomenclaturalTitle();

	/**
	 * Returns a list of <code>UuidAndTitleCache</code> containing all <code>TeamOrPersonBase</code> obejcts
	 * with their respective titleCache
	 *
	 * @return a list of <code>UuidAndTitleCache</code> instances
	 */
	public List<UuidAndTitleCache<Team>> getTeamUuidAndTitleCache();

	/**
	 * @return
	 */
	public List<UuidAndTitleCache<Institution>> getInstitutionUuidAndTitleCache(Integer limit, String pattern);

    /**
     * @param limit
     * @param pattern
     * @return
     */
    List<UuidAndTitleCache<AgentBase>> getUuidAndAbbrevTitleCache(Integer limit, String pattern, Class clazz);
}
