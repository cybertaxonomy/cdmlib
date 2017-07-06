/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.name;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;

/**
 * @author a.kohlbecker
 * @since May 2, 2017
 *
 */
public interface IRegistrationDao
            extends IAnnotatableDao<Registration> {


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
     * @param reference
     *            filters the Registration by the reference of the nomenclatural act
     *            for which the Registration has been created.
     *            The name and all type designations associated with the Registration
     *            are sharing the same  reference.
     *            If <code>null</code> all registrations with a name or type designation
     *            that has no reference are returned. Also those registrations
     *            having no name and type designation at all.
     * @param includedStatus
     * @param limit
     *            the maximum number of entities returned (can be <code>null</code>
     *            to return all entities)
     * @param start
     * @param propertyPaths
     * @return
     * @throws DataAccessException
     */
    public List<Registration> list(Optional<Reference> reference, Collection<RegistrationStatus> includedStatus,
            Integer limit, Integer start, List<String> propertyPaths);

    /**
     * Counts the Registration instances stored in the database.
     *
     * For detailed description see the according list method
     * {@link #list(Optional, Collection, Integer, Integer, List)}}
     *
     * @return
     */
    public Long count(Optional<Reference> reference, Collection<RegistrationStatus> includedStatus);

}
