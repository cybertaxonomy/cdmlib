/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.List;

import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.reference.IReference;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.kohlbecker
 * @since May 2, 2017
 *
 */
public interface IRegistrationService extends IAnnotatableService<Registration> {

    /**
     * Returns a sublist of Registration instances stored in the database. A maximum
     * of 'limit' objects are returned, starting at object with index 'start'.
     * The bean properties specified by the parameter <code>propertyPaths</code>
     * and recursively initialized for each of the entities in the resultset
     *
     * For detailed description and examples regarding
     * <code>propertyPaths</code> <b>please refer to:</b>
     * {@link IBeanInitializer#initialize(Object, List)}
     *
     * @param pageSize The maximum number of objects returned (can be null for all matching objects)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based,
     *                   can be null, equivalent of starting at the beginning of the recordset)
     * @param reference
     *            filters the Registration by the reference of the nomenclatural act for which the Registration as been created.
     *            The name and all type designations associated with the Registration are sharing the same  citation and citation detail.
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPaths
     * @return
     * @throws DataAccessException
     */
    public Pager<Registration> page(Integer pageSize, Integer pageIndex, IReference reference, List<OrderHint> orderHints, List<String> propertyPaths);

    /**
     * Returns a sublist of Registration instances stored in the database. A maximum
     * of 'limit' objects are returned, starting at object with index 'start'.
     * The bean properties specified by the parameter <code>propertyPaths</code>
     * and recursively initialized for each of the entities in the resultset
     *
     * For detailed description and examples regarding
     * <code>propertyPaths</code> <b>please refer to:</b>
     * {@link IBeanInitializer#initialize(Object, List)}
     *
     * @param pageSize The maximum number of objects returned (can be null for all matching objects)
     * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based,
     *                   can be null, equivalent of starting at the beginning of the recordset)
     * @param submitter
     *            The user who submitted the Registration
     * @param orderHints
     *            Supports path like <code>orderHints.propertyNames</code> which
     *            include *-to-one properties like createdBy.username or
     *            authorTeam.persistentTitleCache
     * @param propertyPaths
     * @return
     * @throws DataAccessException
     */
    public Pager<Registration> page(Integer pageSize, Integer pageIndex, User submitter, List<OrderHint> orderHints, List<String> propertyPaths);



}
