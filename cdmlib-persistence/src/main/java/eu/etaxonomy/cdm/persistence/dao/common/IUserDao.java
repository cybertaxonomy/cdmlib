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

import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public interface IUserDao extends ICdmEntityDao<User> {

    /**
     * Among other purposes this method will be used while
     * authenticating a user.
     *
     * @param username
     * @return
     */
    public User findUserByUsername(String username);

     /**
     * Return a List of users matching the given query string, optionally filtered by class, optionally with a particular MatchMode
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
     * @return a List of instances of User matching the queryString
     * @see {@link IIdentifiableDao#findByTitle(Class, String, MatchMode, List, Integer, Integer, List, List)}
     */
    public List<User> findByUsername(String queryString, MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Return a count of users matching the given query string in the username, optionally filtered by class, optionally with a particular MatchMode
     *
     * @param clazz filter by class - can be null to include all instances of type T
     * @param queryString the query string to filter by
     * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
     * @param criteria extra restrictions to apply
     * @return a count of instances of type User matching the queryString
     *
     * @see {@link IIdentifiableDao#countByTitle(Class, String, MatchMode, List)}
     */
    public long countByUsername(String queryString, MatchMode matchmode, List<Criterion> criteria);


}
