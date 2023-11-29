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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.joda.time.DateTime;
import org.joda.time.Partial;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.application.CdmRepository;
import eu.etaxonomy.cdm.api.service.dto.RegistrationDTO;
import eu.etaxonomy.cdm.api.service.dto.RegistrationWorkingSet;
import eu.etaxonomy.cdm.api.service.exception.TypeDesignationSetException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.api.util.UserHelper;
import eu.etaxonomy.cdm.format.reference.ReferenceEllypsisFormatter;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.permission.CRUD;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.persistence.dao.initializer.EntityInitStrategy;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.permission.PermissionDeniedException;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;

/**
 * Provides RegistrationDTOs and RegistrationWorkingsets for Registrations in the database.
 *
 * @author a.kohlbecker
 * @since Mar 10, 2017
 */
@Service("registrationWorkingSetService")
@Transactional(readOnly=true)
public class RegistrationWorkingSetService implements IRegistrationWorkingSetService {

    private static final Logger logger = LogManager.getLogger();

    public static final EntityInitStrategy TYPEDESIGNATION_INIT_STRATEGY = new EntityInitStrategy(
            "typeStatus",
            "typifiedNames.typeDesignations", // important !!
            "typeSpecimen",
            "typeName.$",
            "designationSource.annotations",
            "designationSource.links",
            "designationSource.markers",
            "annotations",   // needed for AnnotatableEntity.clone() in DerivedUnitConverter.copyPropertiesTo
            "markers",       // needed for AnnotatableEntity.clone() in DerivedUnitConverter.copyPropertiesTo
            "registrations" // DerivedUnitConverter.copyPropertiesTo(TARGET n));
            )
            .extend("designationSource.citation", ReferenceEllypsisFormatter.INIT_STRATEGY, false);

    public static final EntityInitStrategy NAME_INIT_STRATEGY = new EntityInitStrategy(
                "$",
                "nomenclaturalSource.citation.inReference.inReference",
                "rank",
                "homotypicalGroup.typifiedNames",
                "status.type",
                "typeDesignations" // important !!!
            )
            .extend("nomenclaturalSource.citation", ReferenceEllypsisFormatter.INIT_STRATEGY, false);

    public static final EntityInitStrategy REGISTRATION_DTO_INIT_STRATEGY = new EntityInitStrategy(Arrays.asList(new String []{
                "blockedBy",
                // institution
                "institution",
                }
                )
            )
            .extend("name", NAME_INIT_STRATEGY, false)
            .extend("typeDesignations", TYPEDESIGNATION_INIT_STRATEGY, true);

    public  EntityInitStrategy DERIVEDUNIT_INIT_STRATEGY = new EntityInitStrategy(Arrays.asList(new String[]{
           "*", // initialize all related entities to allow DerivedUnit conversion, see DerivedUnitConverter.copyPropertiesTo()
           "derivedFrom.$",
           "derivedFrom.type", // TODO remove?
           "derivedFrom.originals.derivationEvents", // important!!
           "specimenTypeDesignations.typifiedNames.typeDesignations", // important!!
           "mediaSpecimen.sources.citation",
           "collection.institute"// see CollectionCaptionGenerator
    })).extend("mediaSpecimen.sources.citation", ReferenceEllypsisFormatter.INIT_STRATEGY, false);

    public List<String> FIELDUNIT_INIT_STRATEGY = Arrays.asList(new String[]{
          "$",
          "annotations.*", // * is needed as log as we are using a table in FilterableAnnotationsField
          "gatheringEvent.$",
          "gatheringEvent.country",
          "gatheringEvent.collectingAreas",
          "gatheringEvent.actor",
          "gatheringEvent.exactLocation.$",
          "derivationEvents.derivatives", // important, otherwise the DerivedUnits are not included into the graph of initialized entities!!!
    });

    public static final List<String> BLOCKING_REGISTRATION_INIT_STRATEGY = Arrays.asList(new String []{

          "blockedBy.blockedBy",
          // typeDesignation
          "blockedBy.typeDesignations.typeStatus",
//          "typeDesignations.typifiedNames.typeDesignations", // important !?
//          "blockedBy.name.$",
          "blockedBy.name.nomenclaturalSource.citation.authorship",
          "blockedBy.name.nomenclaturalSource.citation.inReference",
          "blockedBy.name.rank",
          // institution
          "blockedBy.institution",
          }
    );

    private static final int PAGE_SIZE = 50;


    @Autowired
    @Qualifier("cdmRepository")
    private CdmRepository repo;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private IBeanInitializer defaultBeanInitializer;

    public RegistrationWorkingSetService() {
    }

    /**
     * @param id the Registration entity id
     */
    @Override
    public RegistrationDTO loadDtoById(Integer id) {
        Registration reg = repo.getRegistrationService().load(id, REGISTRATION_DTO_INIT_STRATEGY.getPropertyPaths());
        inititializeSpecimen(reg);
        return new RegistrationDTO(reg);
    }

    /**
     * @param id the Registration entity uuid
     */
    @Override
    public RegistrationDTO loadDtoByUuid(UUID uuid) {
        Registration reg = repo.getRegistrationService().load(uuid, REGISTRATION_DTO_INIT_STRATEGY.getPropertyPaths());
        inititializeSpecimen(reg);
        return new RegistrationDTO(reg);
    }

    @Override
    public Pager<RegistrationDTO> pageDTOs(String identifier, Integer pageIndex,  Integer pageSize) throws IOException {

        Pager<Registration> regPager = repo.getRegistrationService().pageByIdentifier(identifier, pageIndex, pageSize, REGISTRATION_DTO_INIT_STRATEGY.getPropertyPaths());
        return convertToDTOPager(regPager);
    }

    @Override
    public Pager<RegistrationDTO> convertToDTOPager(Pager<Registration> regPager) {
        return new DefaultPagerImpl<>(regPager.getCurrentIndex(), regPager.getCount(),
                regPager.getPageSize(), makeDTOs(regPager.getRecords()));
    }

    @Override
    public Pager<RegistrationDTO> pageDTOs(Integer pageSize, Integer pageIndex) {
        return pageDTOs((UUID)null, null, null, null, null, null, pageSize, pageIndex, null);
    }

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
     * @param taxonNameFilterPattern
     *    The method matches Registrations which contain the the passed pattern in the
     *    {@link Registration#getName() Registration.name}
     * @param typeDesignationStatusUuids
     *    Filter by one or more {@link eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus} or {@link eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus}.
     *    Multiple status will be combined with OR.
     * @param pageSize
     * @param pageIndex
     * @param orderHints
     * @return
     */

    @Override
    public Pager<RegistrationDTO> pageDTOs(UUID submitterUuid, Collection<RegistrationStatus> includedStatus, String identifierFilterPattern,
            String taxonNameFilterPattern, String referenceFilterPattern, Collection<UUID> typeDesignationStatusUuids,
            Integer pageSize, Integer pageIndex, List<OrderHint> orderHints){

            if(pageSize == null){
                pageSize = PAGE_SIZE;
            }

            if(orderHints == null){
                orderHints = Arrays.asList(new OrderHint("identifier", SortOrder.ASCENDING));
            }

            Pager<Registration> pager = repo.getRegistrationService().page(submitterUuid, includedStatus,
                    identifierFilterPattern, taxonNameFilterPattern,
                    referenceFilterPattern, typeDesignationStatusUuids, PAGE_SIZE , pageIndex, orderHints, REGISTRATION_DTO_INIT_STRATEGY.getPropertyPaths());

            Pager<RegistrationDTO> dtoPager = convertToDTOPager(pager);
            if(logger.isDebugEnabled()){
                logger.debug(String.format("pageDTOs() pageIndex: $1%d, pageSize: $2%d, includedStatusUuids: $3%s, typeDesignationStatusUuids: $4%s, taxonNameFilterPattern: $5%s, submitterUuid: $6%s",
                        pageIndex, pageSize, includedStatus, identifierFilterPattern, taxonNameFilterPattern, submitterUuid));
                logger.debug("pageDTOs() result: " + pager.toString());
            }
            return dtoPager;

    }

    @Override
    public Pager<RegistrationDTO> findInTaxonGraph(UUID submitterUuid, Collection<RegistrationStatus> includedStatus,
            String taxonNameFilterPattern, MatchMode matchMode,
            Integer pageSize, Integer pageIndex, List<OrderHint> orderHints) {

        Pager<Registration> regPager = repo.getRegistrationService().pageTaxomicInclusion(null, includedStatus,
            taxonNameFilterPattern, matchMode,
            pageSize, pageIndex, orderHints, REGISTRATION_DTO_INIT_STRATEGY.getPropertyPaths());

        return convertToDTOPager(regPager);
    }


    /**
     * {@inheritDoc}
     * @throws TypeDesignationSetException
     */
    @Override
    public RegistrationWorkingSet loadWorkingSetByReferenceUuid(UUID referenceUuid, boolean resolveSections) throws TypeDesignationSetException, PermissionDeniedException {

        Reference reference = repo.getReferenceService().load(referenceUuid); // needed to use load to avoid the problem described in #7331
        if(resolveSections){
            reference = resolveSection(reference);
        }

        checkPermissions(reference);

        Pager<Registration> pager = repo.getRegistrationService().page(Optional.of(reference), null, null, null, REGISTRATION_DTO_INIT_STRATEGY.getPropertyPaths());

        /* for debugging https://dev.e-taxonomy.eu/redmine/issues/7331 */
        // debugIssue7331(pager);
        RegistrationWorkingSet registrationWorkingSet;
        if(pager.getCount() > 0) {
            registrationWorkingSet = new RegistrationWorkingSet(makeDTOs(pager.getRecords()));
        } else {
            registrationWorkingSet = new RegistrationWorkingSet(reference);
        }
        return registrationWorkingSet;
    }

    private void checkPermissions(Reference reference) throws PermissionDeniedException {

        boolean permissionDenied = isPermissionDenied(reference);
        if(permissionDenied) {
            throw new PermissionDeniedException("Access to the workingset is denied for the current user.");
        }
    }

    private boolean isPermissionDenied(Reference reference) {
        boolean permissionDenied = false;
        if(!checkReferencePublished(reference)){
            permissionDenied = !userHelper.userHasPermission(reference, CRUD.UPDATE);
        }
        return permissionDenied;
    }

    private boolean checkReferencePublished(Reference reference) {

        if(reference.getDatePublished() == null){
            return false;
        }
        Partial pubPartial = null;
        if(reference.getDatePublished().getStart() != null){
            pubPartial = reference.getDatePublished().getStart();
        } else {
            pubPartial = reference.getDatePublished().getEnd();
        }
        if(pubPartial == null){
            return !reference.getDatePublished().getFreeText().isEmpty();
        }

        DateTime nowLocal = new DateTime();
        //LocalDateTime nowUTC = nowLocal.withZone(DateTimeZone.UTC).toLocalDateTime();

        DateTime pubDateTime = pubPartial.toDateTime(null);
        return nowLocal.isAfter(pubDateTime);
    }

    /**
     * @param reference
     */
    protected Reference resolveSection(Reference reference) {
        repo.getReferenceService().load(reference.getUuid(), Arrays.asList(new String[]{"inReference"})); // needed to avoid the problem described in #7331
        if(reference.isOfType(ReferenceType.Section) && reference.getInReference() != null) {
            reference = reference.getInReference();
        }
        return reference;
    }

    /**
     * {@inheritDoc}
     * @throws TypeDesignationSetException
     */
    @Override
    public RegistrationWorkingSet loadWorkingSetByReferenceID(Integer referenceID, boolean resolveSections) throws TypeDesignationSetException, PermissionDeniedException {

        Reference reference = repo.getReferenceService().find(referenceID);
        if(resolveSections){
            reference = resolveSection(reference);
        }

        checkPermissions(reference);

        repo.getReferenceService().load(reference.getUuid()); // needed to avoid the problem described in #7331

        Pager<Registration> pager = repo.getRegistrationService().page(Optional.of(reference), null, null, null, REGISTRATION_DTO_INIT_STRATEGY.getPropertyPaths());

        /* for debugging https://dev.e-taxonomy.eu/redmine/issues/7331 */
        // debugIssue7331(pager);

        return new RegistrationWorkingSet(makeDTOs(pager.getRecords()));
    }

    @Override
    public Pager<RegistrationDTO> pageWorkingSetsByNameUUID(Collection<UUID> taxonNameUuids, Integer pageIndex, Integer pageSize, List<OrderHint> orderHints) throws TypeDesignationSetException, PermissionDeniedException {

        if(orderHints == null){
            orderHints = Arrays.asList(new OrderHint("identifier", SortOrder.ASCENDING));
        }

        Pager<Registration> pager = repo.getRegistrationService().page((UUID)null, null, taxonNameUuids, pageSize, pageIndex, orderHints, REGISTRATION_DTO_INIT_STRATEGY.getPropertyPaths());

        return new DefaultPagerImpl<>(pager.getCurrentIndex(), pager.getCount(), pager.getPageSize(), makeDTOs(pager.getRecords()));
    }

    @SuppressWarnings("unused")
    private void debugIssue7331(Pager<Registration> pager) {
        for(Registration reg : pager.getRecords()){
            if(reg.getName() != null && reg.getName().getNomenclaturalReference().getAuthorship() != null){
                Reference ref = reg.getName().getNomenclaturalReference();
                if(!Hibernate.isInitialized(ref.getAuthorship())){
                    logger.error("UNINITIALIZED");
                }
            } else {
                logger.debug("NO AUTHORS");
            }
        }
    }

    @Override
    public Set<RegistrationDTO> loadBlockingRegistrations(UUID blockedRegistrationUuid){

        Registration registration = repo.getRegistrationService().load(blockedRegistrationUuid, BLOCKING_REGISTRATION_INIT_STRATEGY);
        Set<Registration> registrations = registration.getBlockedBy();

        Set<RegistrationDTO> blockingSet = new HashSet<>();
        for(Registration reg : registrations){
            blockingSet.add(new RegistrationDTO(reg));
        }
        return blockingSet;
    }

    @Override
    public List<RegistrationDTO> makeDTOs(Collection<Registration> regs) {
        initializeSpecimens(regs);
        List<RegistrationDTO> dtos = new ArrayList<>(regs.size());
        regs.forEach(reg -> {dtos.add(new RegistrationDTO(reg));});
        return dtos;
    }

    public void initializeSpecimens(Collection<Registration> regs) {
        for(Registration reg : regs){
            inititializeSpecimen(reg);
        }
    }

    public void inititializeSpecimen(Registration reg) {

        for(TypeDesignationBase<?> td : reg.getTypeDesignations()){
            if(td instanceof SpecimenTypeDesignation){

                DerivedUnit derivedUnit = ((SpecimenTypeDesignation) td).getTypeSpecimen();
                @SuppressWarnings("rawtypes")
                Set<SpecimenOrObservationBase> sobs = new HashSet<>();
                sobs.add(HibernateProxyHelper.deproxy(derivedUnit));

                while(sobs != null && !sobs.isEmpty()){
                    @SuppressWarnings("rawtypes")
                    Set<SpecimenOrObservationBase> nextSobs = null;
                    for(@SuppressWarnings("rawtypes") SpecimenOrObservationBase sob : sobs){
                        sob = HibernateProxyHelper.deproxy(sob);
                        if(sob == null){
                            continue;
                        }
                        if(DerivedUnit.class.isAssignableFrom(sob.getClass())) {
                            defaultBeanInitializer.initialize(sob, DERIVEDUNIT_INIT_STRATEGY.getPropertyPaths());
                            nextSobs = ((DerivedUnit)sob).getOriginals();
                        }
                        if(sob instanceof FieldUnit){
                            defaultBeanInitializer.initialize(sob, FIELDUNIT_INIT_STRATEGY);
                        }
                    }
                    sobs = nextSobs;
                }
            }
        }
    }
}