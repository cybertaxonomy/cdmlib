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

import org.hibernate.criterion.Criterion;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IAgentService extends IIdentifiableEntityService<AgentBase> {
		
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
	
	/**
	 * Returns a Paged List of AgentBase instances where the default field matches the String queryString (as interpreted by the Lucene QueryParser)
	 * 
	 * @param clazz filter the results by class (or pass null to return all AgentBase instances)
	 * @param queryString
	 * @param pageSize The maximum number of agents returned (can be null for all matching agents)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @param propertyPaths properties to be initialized
	 * @return a Pager Agent instances
	 * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Apache Lucene - Query Parser Syntax</a>
	 */
	public Pager<AgentBase> search(Class<? extends AgentBase> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

      
	/**
	 * Return a Pager of agents with a nomenclatural title matching the given query string, optionally filtered by class, optionally with a particular MatchMode
	 * 
	 * @param clazz filter by class - can be null to include all agents
	 * @param queryString the query string to filter by
	 * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
	 * @param criteria additional criteria to filter by
	 * @param pageSize The maximum number of objects returned (can be null for all objects)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link BeanInitializer#initialize(Object, List)}
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 * @return a paged list of agents matching the queryString
	 */
    public Pager<AgentBase> findByNomenclaturalTitle(Class<? extends TeamOrPersonBase> clazz, String queryString,MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
}
