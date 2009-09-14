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

import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.common.ISearchableDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IAgentDao extends IIdentifiableDao<AgentBase>, ISearchableDao<AgentBase> {
	
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
	public int countInstitutionalMemberships(Person person);
	
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
	public int countMembers(Team team);

	/**
	 * Return a count of addresses of a given agent
	 * 
	 * @param agent the agent
	 * @return a count of Address instances
	 */
	public Integer countAddresses(AgentBase agent);

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
	 * Return a List of teams or persons with their nomenclaturalTitle matching the given query string, optionally filtered by class, optionally with a particular MatchMode
	 * 
	 * @param clazz filter by class - can be null to include all teams or persons
	 * @param queryString the query string to filter by
	 * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
	 * @param criteria extra restrictions to apply
	 * @param pageSize The maximum number of rights returned (can be null for all rights)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @return a List of teams or persons matching the queryString
	 */
	public List<AgentBase> findByNomenclaturalTitle(Class<? extends TeamOrPersonBase> clazz, String queryString, MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber,List<OrderHint> orderHints, List<String> propertyPaths);
	
	
	/**
	 * Return a count of teams or persons with their nomenclaturalTitle matching the given query string, optionally 
	 * filtered by class, optionally with a particular MatchMode
	 * 
	 * @param clazz filter by class - can be null to include teams or persons
	 * @param queryString the query string to filter by
	 * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
	 * @param criteria extra restrictions to apply
	 * @return a count of teams or persons matching the queryString
	 */
	public Integer countByNomenclaturalTitle(Class<? extends TeamOrPersonBase> clazz, String queryString, MatchMode matchmode, List<Criterion> criteria);
}
