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

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.strategy.merge.MergeException;

public interface IAgentService extends IIdentifiableEntityService<AgentBase> {

	public List<Institution> searchInstitutionByCode(String code);

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
	@Override
    public Pager<AgentBase> search(Class<? extends AgentBase> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Returns a list of <code>UuidAndTitleCache</code> containing all <code>Person</code>s
	 *
	 * @return a list of <code>UuidAndTitleCache</code> instances
	 */
	public List<UuidAndTitleCache<Person>> getPersonUuidAndTitleCache();

	/**
	 * Returns a list of <code>UuidAndTitleCache</code> containing all <code>TeamOrPersonBase</code> objects
	 * with their respective titleCache
	 *
	 * @return a list of <code>UuidAndTitleCache</code> instances
	 */
	public List<UuidAndTitleCache<Team>> getTeamUuidAndTitleCache();

	/**
	 * Returns a list of <code>UuidAndTitleCache</code> containing all <code>TeamOrPersonBase</code> objects
	 * with their respective nomenclaturalTitle instead of regular titleCache
	 *
	 * @return a list of <code>UuidAndTitleCache</code> instances
	 */
	public List<UuidAndTitleCache<Team>> getTeamUuidAndNomenclaturalTitle();

	/**
	 * Returns a list of <code>UuidAndTitleCache</code> containing all {@link Institution} objects
	 * with their respective titleCache
	 *
	 * @return a list of <code>UuidAndTitleCache</code> instances
	 */
	public List<UuidAndTitleCache<Institution>> getInstitutionUuidAndTitleCache();


	/**
     * Returns a list of <code>UuidAndTitleCache</code> containing all {@link Institution} objects
     * with their respective NomenclaturalTitleCache
     *
     * @return a list of <code>UuidAndTitleCache</code> instances
     */
    public List<UuidAndTitleCache<AgentBase>> getUuidAndAbbrevTitleCache();

    /**
     * @param teamUuid
     * @return
     * @throws MergeException
     */
    public UpdateResult convertTeam2Person(UUID teamUuid) throws MergeException;

	/**
	 * If the given {@link Team} is a single member team all links to the team are redirected to the
	 * single member and the team itself is deleted.
	 * <BR>
	 * If the given {@link Team} has no members (protected caches) a new {@link Person} is created
	 * and all links to the team are redirected to the new person and the team itself is deleted.
	 * <BR>
	 * Otherwise, an {@link IllegalArgumentException} is thrown.
	 *
	 * @param team the team to be converted
	 * @return the existing or new person that replaces the given team
	 * @throws IllegalArgumentException if team has > 1 members
	 * @throws MergeException if anything else goes wrong during merge
	 */
	public UpdateResult convertTeam2Person(Team team) throws MergeException, IllegalArgumentException;


    /**
     * @param personUuid
     * @return
     * @throws MergeException
     * @throws IllegalArgumentException
     */
    public UpdateResult convertPerson2Team(UUID personUuid) throws MergeException, IllegalArgumentException;

	/**
	 * If possible replaces the given person by a new {@link Team}.
	 * <BR>This is only possible if the person is at no place used in person only mode, which
	 * is the case e.g. at User.person or FieldUnit.primaryCollector.
	 * <BR>
	 * If the person can not be replaced a {@link MergeException} is thrown.
	 *
	 * @param person the {@link Person} to be converted
	 * @return the new team that replaces the given person
	 * @throws MergeException if anything else goes wrong during merge
	 */
	public UpdateResult convertPerson2Team(Person person) throws MergeException, IllegalArgumentException;

}
