/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import org.hibernate.criterion.Criterion;

import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IGroupDao extends ICdmEntityDao<Group> {

	public Group findGroupByName(String groupName);

	public List<String> listNames(Integer pageSize, Integer pageNumber);

	public List<String> listMembers(Group group, Integer pageSize, Integer pageNumber);

	 /**
	 * Return a List of groups matching the given query string, optionally filtered by class, optionally with a particular MatchMode
	 *
	 * @param queryString the query string to filter by
	 * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
	 * @param criteria extra restrictions to apply
	 * @param pageSize The maximum number of rights returned (can be null for all rights)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
	 * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
	 * @param orderHints
	 *            Supports path like <code>orderHints.propertyNames</code> which
	 *            include *-to-one properties like createdBy.username or
	 *            authorTeam.persistentTitleCache
	 *
	 * @return a List of instances of Group matching the queryString
	 *
	 * @see {@link IIdentifiableDao#findByTitle(Class, String, MatchMode, List, Integer, Integer, List, List)}
	 */
	public List<Group> findByName(String queryString, MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

	/**
	 * Return a count of groups matching the given query string in the name, optionally filtered by class, optionally with a particular MatchMode
	 *
	 * @param clazz filter by class - can be null to include all instances of type T
	 * @param queryString the query string to filter by
	 * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
	 * @param criteria extra restrictions to apply
	 * @return a count of instances of type Group matching the queryString
	 *
	 * @see {@link IIdentifiableDao#countByTitle(Class, String, MatchMode, List)}
	 */
	public long countByName(String queryString, MatchMode matchmode, List<Criterion> criteria);

}
