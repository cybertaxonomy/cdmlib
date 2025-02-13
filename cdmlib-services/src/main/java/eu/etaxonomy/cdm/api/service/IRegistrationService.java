/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.kohlbecker
 * @since May 2, 2017
 */
public interface IRegistrationService extends IAnnotatableService<Registration> {

    /**
     * Returns a sublist of Registration instances stored in the database. A
     * maximum of 'limit' objects are returned, starting at object with index
     * 'start'. The bean properties specified by the parameter
     * <code>propertyPaths</code> and recursively initialized for each of the
     * entities in the resultset
     *
     * For detailed description and examples regarding
     * <code>propertyPaths</code> <b>please refer to:</b>
     * {@link IBeanInitializer#initialize(Object, List)}
     *
     * @param pageSize
     *            The maximum number of objects returned (can be null for all
     *            matching objects)
     * @param pageNumber
     *            The offset (in pageSize chunks) from the start of the result
     *            set (0 - based, can be null, equivalent of starting at the
     *            beginning of the recordset)
     * @param reference
     *            filters the Registration by the reference of the nomenclatural
     *            act for which the Registration as been created. The name and
     *            all type designations associated with the Registration are
     *            sharing the same citation. If the Optional itself is
     *            <code>null</code> the parameter is neglected. If Optional
     *            contains the value <code>null</code> all registrations with a
     *            name or type designation that has no reference are returned.
     *            Also those registrations having no name and type designation
     *            at all.
     * @param includedStatus
     *            filters the Registration by the RegistrationStatus. Only
     *            Registration having one of the supplied status will included.
     *            // * @param orderHints // * Supports path like
     *            <code>orderHints.propertyNames</code> which // * include
     *            *-to-one properties like createdBy.username or // *
     *            authorTeam.persistentTitleCache
     * @param propertyPaths
     * @return
     * @throws DataAccessException
     */
    public Pager<Registration> page(Optional<Reference> reference, Collection<RegistrationStatus> includedStatus,
            Integer pageSize, Integer pageIndex, List<String> propertyPaths);

    public Pager<Registration> pageByIdentifier(String identifier, Integer pageIndex, Integer pageSize, List<String> propertyPaths) throws IOException;

    public Map<UUID, RegistrationStatus> statusByIdentifier(String identifier) throws IOException;

    /**
     * @param submitterUuid
     *    Filter by the uuid of the {@link User} associated with the Registration as <code>Registration.submitter</code>
     * @param includedStatus
     *    Filter by one or more {@link RegistrationStatus}. Multiple status will be combined with OR. In case the current user
     *    is not authenticated (i.e. the authentication is anonymous) the includedStatus will be set to {@link RegistrationStatus#PUBLISHED}
     *    to protect all other Registrations from being undisclosed.
     * @param identifierFilterPattern
     *    Filter by the {@link Registration#getIdentifier() Registration.identifier}.
     *    The method matches Registrations which contain the the passed pattern in the identifier.
     *    The asterisk '<code>*</code>' can be used as wildcard in any position of the pattern string
     * @param taxonNameFilterPattern
     *    The method matches Registrations which contain the the passed pattern in the
     *    {@link Registration#getName() Registration.name}. The asterisk '<code>*</code>' can be used
     *    as wildcard in any position of the pattern string
     * @param referenceFilterPattern
     *    The asterisk '<code>*</code>' can be used as wildcard in any position of the pattern string
     * @param typeDesignationStatusUuids
     *    Filter by one or more {@link eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus} or {@link eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus}.
     *    Multiple status will be combined with OR.
     * @param pageSize
     * @param pageIndex
     * @param orderHints
     * @param propertyPaths
     * @return
     */
    public Pager<Registration> page(UUID submitterUuid, Collection<RegistrationStatus> includedStatus, String identifierFilterPattern,
            String taxonNameFilterPattern, String referenceFilterPattern, Collection<UUID> typeDesignationStatusUuids,
            Integer pageSize, Integer pageIndex, List<OrderHint> orderHints, List<String> propertyPaths);

    public Pager<Registration> page(UUID submitterUuid, Collection<RegistrationStatus> includedStatus,
            Collection<UUID> taxonNameUUIDs,
            Integer pageSize, Integer pageIndex, List<OrderHint> orderHints, List<String> propertyPaths);

    public Pager<Registration> pageTaxomicInclusion(UUID submitterUuid, Collection<RegistrationStatus> includedStatus,
            String taxonNameFilterPattern, MatchMode matchMode,
            Integer pageSize, Integer pageIndex, List<OrderHint> orderHints, List<String> propertyPaths);

    // ============= functionality to be moved into a "RegistrationManagerBean" ==================

    public Registration newRegistration();

    /**
     * Persists the registration in case it is not persisted yet.
     */
    public Registration assureIsPersisted(Registration reg);

    /**
     * Create and persist a new Registration for the name having the supplied UUID.
     */
    public Registration createRegistrationForName(UUID taxonNameUuid);

    boolean checkRegistrationExistsFor(TaxonName name);

    public void addTypeDesignation(UUID registrationUUID, UUID typeDesignationUuid);

    public void addTypeDesignation(Registration registration, UUID typeDesignationUuid);

    // ============================================================================================

}
