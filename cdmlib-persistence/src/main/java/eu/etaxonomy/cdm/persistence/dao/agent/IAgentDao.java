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

import org.hibernate.criterion.Criterion;

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dto.TeamOrPersonUuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IAgentDao extends IIdentifiableDao<AgentBase> {

	public List<Institution> getInstitutionByCode(String code);

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

    public <T extends AgentBase> List<TeamOrPersonUuidAndTitleCache<T>> getUuidAndAbbrevTitleCache(Class<T> clazz, Integer limit, String pattern);

    public <T extends AgentBase<?>> List<T> findByTitleAndAbbrevTitle(Class<T> clazz, String queryString, MatchMode matchmode,
            List<Criterion> criterion, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths);

    public <T extends AgentBase> List<TeamOrPersonUuidAndTitleCache<T>> getUuidAndTitleCacheWithCollector(Class<T> clazz, Integer limit, String pattern);

    public <T extends AgentBase> List<TeamOrPersonUuidAndTitleCache<T>> getTeamOrPersonUuidAndTitleCache(Class<T> clazz, Integer limit, String pattern);
}
