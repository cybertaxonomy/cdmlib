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

import eu.etaxonomy.cdm.api.service.dto.RegistrationDTO;
import eu.etaxonomy.cdm.api.service.dto.RegistrationWorkingSet;
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
    public RegistrationDTO loadDtoById(Integer id);

    public RegistrationDTO loadDtoByUuid(UUID uuid);

    public Pager<RegistrationDTO> pageDTOs(Integer pageSize, Integer pageIndex);

    public Pager<RegistrationDTO> pageDTOs(String identifier, Integer pageIndex,  Integer pageSize) throws IOException;

    /**
     * @param referenceID
     * @param resolveSections resolve the higher publication unit and build the RegistrationWorkingSet for that reference. E.e. For journal sections the
     *  use the inReference which is the journal article.
     *
     * @return
     */
    @Deprecated
    public RegistrationWorkingSet loadWorkingSetByReferenceID(Integer referenceID, boolean resolveSections) throws TypeDesignationSetException;

    /**
     * Loads the working set specified by the <code>referenceUuid</code> from the database. The list of {@link RegistrationDTO}s can be empty in case
     * there is no registration which is related to the reference.
     *
     * @param referenceUuid
     * @param resolveSections resolve the higher publication unit and build the RegistrationWorkingSet for that reference. E.e. For journal sections the
     *  use the inReference which is the journal article.
     * @return
     */
    public RegistrationWorkingSet loadWorkingSetByReferenceUuid(UUID referenceUuid, boolean resolveSections) throws TypeDesignationSetException, PermissionDeniedException;

    public Set<RegistrationDTO> loadBlockingRegistrations(UUID blockedRegistrationUuid);

    public Pager<RegistrationDTO> convertToDTOPager(Pager<Registration> regPager);

    public List<RegistrationDTO> makeDTOs(Collection<Registration> regs);

    Pager<RegistrationDTO> pageDTOs(UUID submitterUuid, Collection<RegistrationStatus> includedStatus, String identifierFilterPattern,
            String taxonNameFilterPattern, String referenceFilterPattern, Collection<UUID> typeDesignationStatusUuids, Integer pageSize,
            Integer pageIndex, List<OrderHint> orderHints);

    public Pager<RegistrationDTO> findInTaxonGraph(UUID submitterUuid, Collection<RegistrationStatus> includedStatus,
            String taxonNameFilterPattern, MatchMode matchMode, Integer pageSize, Integer pageIndex,
            List<OrderHint> orderHints);

    public Pager<RegistrationDTO> pageWorkingSetsByNameUUID(Collection<UUID> taxonNameUuids, Integer pageIndex, Integer pageSize, List<OrderHint> orderHints) throws TypeDesignationSetException, PermissionDeniedException;
}
