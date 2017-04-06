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

import org.hibernate.criterion.Criterion;
import org.springframework.security.provisioning.GroupManager;

import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * Service for {@link Group user groups}
 *
 * @author n.hoffmann
 * @created Mar 9, 2011
 */
public interface IGroupService extends IService<Group>, GroupManager{


    /**
     * Return a List of groups matching the given query string, optionally filtered by class, optionally with a particular MatchMode
     *
     * @param queryString the query string to filter by
     * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
     * @param criteria additional criteria to filter by
     * @param pageSize The maximum number of objects returned (can be null for all objects)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @return a list of instances of type Group matching the queryString
     *
     * @see {@link IIdentifiableEntityService#listByTitle(Class, String, MatchMode, List, Integer, Integer, List, List)}
     */
    public List<Group> listByName(String queryString, MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);

    public UUID saveGroup(Group group);


}
