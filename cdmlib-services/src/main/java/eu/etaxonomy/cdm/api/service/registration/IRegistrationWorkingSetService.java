/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.registration;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.dto.RegistrationWorkingSet;
import eu.etaxonomy.cdm.api.service.dto.RegistrationWrapperDTO;
import eu.etaxonomy.cdm.api.service.exception.TypeDesignationSetException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.persistence.permission.PermissionDeniedException;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.kohlbecker
 * @since May 4, 2017
 */
public interface IRegistrationWorkingSetService {

    /**
     * @param id the CDM Entity id
     * @return
     */
    public RegistrationWrapperDTO loadDtoById(Integer id);

    public RegistrationWrapperDTO loadDtoByUuid(UUID uuid);

    public Pager<RegistrationWrapperDTO> pageDTOs(Integer pageSize, Integer pageIndex);

    public Pager<RegistrationWrapperDTO> pageDTOs(String identifier, Integer pageIndex,  Integer pageSize) throws IOException;

    /**
     * Loads the working set specified by the <code>referenceUuid</code> from the database. The list of {@link RegistrationWrapperDTO}s can be empty in case
     * there is no registration which is related to the reference.
     *
     * @param referenceUuid
     * @param resolveSections resolve the higher publication unit and build the RegistrationWorkingSet for that reference. E.e. For journal sections the
     *  use the inReference which is the journal article.
     * @return
     */
    public RegistrationWorkingSet loadWorkingSetByReferenceUuid(UUID referenceUuid, boolean resolveSections) throws TypeDesignationSetException, PermissionDeniedException;

    public Set<RegistrationWrapperDTO> loadBlockingRegistrations(UUID blockedRegistrationUuid);

    public Pager<RegistrationWrapperDTO> convertToDTOPager(Pager<Registration> regPager);

    public List<RegistrationWrapperDTO> makeDTOs(Collection<Registration> regs);

    Pager<RegistrationWrapperDTO> pageDTOs(UUID submitterUuid, Collection<RegistrationStatus> includedStatus, String identifierFilterPattern,
            String taxonNameFilterPattern, String referenceFilterPattern, Collection<UUID> typeDesignationStatusUuids, Integer pageSize,
            Integer pageIndex, List<OrderHint> orderHints);

    public Pager<RegistrationWrapperDTO> findInTaxonGraph(UUID submitterUuid, Collection<RegistrationStatus> includedStatus,
            String taxonNameFilterPattern, MatchMode matchMode, Integer pageSize, Integer pageIndex,
            List<OrderHint> orderHints);

    public Pager<RegistrationWrapperDTO> pageWorkingSetsByNameUUID(Collection<UUID> taxonNameUuids, Integer pageIndex, Integer pageSize, List<OrderHint> orderHints) throws TypeDesignationSetException, PermissionDeniedException;
}
