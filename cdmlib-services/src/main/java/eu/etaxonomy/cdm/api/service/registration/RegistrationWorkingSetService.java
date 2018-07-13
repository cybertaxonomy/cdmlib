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

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.application.CdmRepository;
import eu.etaxonomy.cdm.api.service.dto.RegistrationDTO;
import eu.etaxonomy.cdm.api.service.dto.RegistrationWorkingSet;
import eu.etaxonomy.cdm.api.service.exception.RegistrationValidationException;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;

/**
 * Provides RegistrationDTOs and RegistrationWorkingsets for Registrations in the database.
 *
 *
 * @author a.kohlbecker
 * @since Mar 10, 2017
 *
 */
@Service("registrationWorkingSetService")
@Transactional(readOnly=true)
public class RegistrationWorkingSetService implements IRegistrationWorkingSetService {

    public static final List<String> REGISTRATION_DTO_INIT_STRATEGY = Arrays.asList(new String []{
            "blockedBy",
            // typeDesignation
            "typeDesignations.typeStatus",
            "typeDesignations.typifiedNames.typeDesignations", // important !!
            "typeDesignations.typeSpecimen",
            "typeDesignations.typeName.$",
            "typeDesignations.citation",
            "typeDesignations.citation.authorship.$",
            "typeDesignations.annotations", // needed for AnnotatableEntity.clone() in DerivedUnitConverter.copyPropertiesTo
            "typeDesignations.markers", // needed for AnnotatableEntity.clone() in DerivedUnitConverter.copyPropertiesTo
            "typeDesignations.registrations", // DerivedUnitConverter.copyPropertiesTo(TARGET n)

            // name
            "name.$",
            "name.nomenclaturalReference.authorship.$",
            "name.nomenclaturalReference.inReference",
            "name.rank",
            "name.homotypicalGroup.typifiedNames",
            "name.status.type",
            "name.typeDesignations", // important !!"
            // institution
            "institution",
            }
    );

   /**
    *
    */
    public  List<String> DERIVEDUNIT_INIT_STRATEGY = Arrays.asList(new String[]{
           "*", // initialize all related entities to allow DerivedUnit conversion, see DerivedUnitConverter.copyPropertiesTo()
           "derivedFrom.$",
           "derivedFrom.type", // TODO remove?
           "derivedFrom.originals.derivationEvents", // important!!
           "specimenTypeDesignations.typifiedNames.typeDesignations", // important!!
           "mediaSpecimen.sources"
   });

   /**
   *
   */
   public List<String> FIELDUNIT_INIT_STRATEGY = Arrays.asList(new String[]{
          "$",
          "annotations.*", // * is needed as log as we are using a table in FilterableAnnotationsField
          "gatheringEvent.$",
          "gatheringEvent.country",
          "gatheringEvent.collectingAreas",
          "gatheringEvent.actor",
          "derivationEvents.derivatives" // important, otherwise the DerivedUnits are not included into the graph of initialized entities!!!
  });

  public static final List<String> BLOCKING_REGISTRATION_INIT_STRATEGY = Arrays.asList(new String []{

          "blockedBy.blockedBy",
          // typeDesignation
          "blockedBy.typeDesignations.typeStatus",
//          "typeDesignations.typifiedNames.typeDesignations", // important !!
//          "typeDesignations.typeSpecimen",
//          "typeDesignations.typeName.$",
//          "typeDesignations.citation",
//          "typeDesignations.citation.authorship.$",
          // name
//          "blockedBy.name.$",
          "blockedBy.name.nomenclaturalReference.authorship",
          "blockedBy.name.nomenclaturalReference.inReference",
          "blockedBy.name.rank",
//          "name.homotypicalGroup.typifiedNames",
//          "name.status.type",
//          "name.typeDesignations",
          // institution
          "blockedBy.institution",
          }
  );

    /**
     *
     */
    private static final int PAGE_SIZE = 50;

    private static final Logger logger = Logger.getLogger(RegistrationWorkingSetService.class);

    @Autowired
    @Qualifier("cdmRepository")
    private CdmRepository repo;

    @Autowired
    protected IBeanInitializer defaultBeanInitializer;

    public RegistrationWorkingSetService() {

    }


    /**
     * @param id the Registration entity id
     * @return
     */
    @Override
    public RegistrationDTO loadDtoById(Integer id) {
        Registration reg = repo.getRegistrationService().load(id, REGISTRATION_DTO_INIT_STRATEGY);
        inititializeSpecimen(reg);
        return new RegistrationDTO(reg);
    }


    /**
     * @param id the Registration entity id
     * @return
     */
    @Override
    @Transactional(readOnly=true)
    public RegistrationDTO loadDtoByUuid(UUID uuid) {
        Registration reg = repo.getRegistrationService().load(uuid, REGISTRATION_DTO_INIT_STRATEGY);
        inititializeSpecimen(reg);
        return new RegistrationDTO(reg);
    }

    @Override
    @Transactional(readOnly=true)
    public Pager<RegistrationDTO> pageDTOs(String identifier, Integer pageIndex,  Integer pageSize) throws IOException {

        Pager<Registration> regPager = repo.getRegistrationService().pageByIdentifier(identifier, pageIndex, pageSize, REGISTRATION_DTO_INIT_STRATEGY);
        return convertToDTOPager(regPager);
    }


    /**
     * @param regPager
     * @return
     */
    @Override
    public Pager<RegistrationDTO> convertToDTOPager(Pager<Registration> regPager) {
        return new DefaultPagerImpl<RegistrationDTO>(regPager.getCurrentIndex(), regPager.getCount(), regPager.getPageSize(), makeDTOs(regPager.getRecords()));
    }


    @Override
    public Pager<RegistrationDTO> pageDTOs(Integer pageSize, Integer pageIndex) {

        return pageDTOs((User)null, null, null, null, null, pageSize, pageIndex, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pager<RegistrationDTO> pageDTOs(User submitter, Collection<RegistrationStatus> includedStatus,
            String identifierFilterPattern, String taxonNameFilterPattern, Set<TypeDesignationStatusBase> typeStatusFilter,
            Integer pageSize, Integer pageIndex, List<OrderHint> orderHints) {

        if(pageSize == null){
            pageSize = PAGE_SIZE;
        }

        if(orderHints == null){
            orderHints = Arrays.asList(new OrderHint("identifier", SortOrder.ASCENDING));
        }

        Pager<Registration> pager = repo.getRegistrationService().page(submitter, includedStatus, identifierFilterPattern, taxonNameFilterPattern,
                typeStatusFilter, PAGE_SIZE, pageIndex , orderHints, REGISTRATION_DTO_INIT_STRATEGY);

        Pager<RegistrationDTO> dtoPager = convertToDTOPager(pager);
        if(logger.isDebugEnabled()){
            logger.debug(String.format("pageDTOs() pageIndex: $1%d, pageSize: $2%d, includedStatus: $3%s, identifierFilterPattern: $4%s, taxonNameFilterPattern: $5%s, submitter: $6%s",
                    pageIndex, pageSize, includedStatus, identifierFilterPattern, taxonNameFilterPattern, submitter));
            logger.debug("pageDTOs() result: " + pager.toString());
        }
        return dtoPager;
    }

    @Override
    public Pager<RegistrationDTO> pageDTOs(UUID submitterUuid, Collection<RegistrationStatus> includedStatus, String identifierFilterPattern,
            String taxonNameFilterPattern, Collection<UUID> typeDesignationStatusUuids, Integer pageSize,
            Integer pageIndex, List<OrderHint> orderHints){

            if(pageSize == null){
                pageSize = PAGE_SIZE;
            }

            if(orderHints == null){
                orderHints = Arrays.asList(new OrderHint("identifier", SortOrder.ASCENDING));
            }

            Pager<Registration> pager = repo.getRegistrationService().page(submitterUuid, includedStatus,
                    identifierFilterPattern, taxonNameFilterPattern,
                    typeDesignationStatusUuids, PAGE_SIZE, pageIndex , orderHints, REGISTRATION_DTO_INIT_STRATEGY);

            Pager<RegistrationDTO> dtoPager = convertToDTOPager(pager);
            if(logger.isDebugEnabled()){
                logger.debug(String.format("pageDTOs() pageIndex: $1%d, pageSize: $2%d, includedStatusUuids: $3%s, typeDesignationStatusUuids: $4%s, taxonNameFilterPattern: $5%s, submitterUuid: $6%s",
                        pageIndex, pageSize, includedStatus, identifierFilterPattern, taxonNameFilterPattern, submitterUuid));
                logger.debug("pageDTOs() result: " + pager.toString());
            }
            return dtoPager;

    }


    /**
     * {@inheritDoc}
     * @throws RegistrationValidationException
     */
    @Override
    public RegistrationWorkingSet loadWorkingSetByReferenceUuid(UUID referenceUuid, boolean resolveSections) throws RegistrationValidationException {

        Reference reference = repo.getReferenceService().load(referenceUuid); // needed to use load to avoid the problem described in #7331
        if(resolveSections){
            reference = resolveSection(reference);
        }

        Pager<Registration> pager = repo.getRegistrationService().page(Optional.of(reference), null, null, null, REGISTRATION_DTO_INIT_STRATEGY);

        /* for debugging https://dev.e-taxonomy.eu/redmine/issues/7331 */
        // debugIssue7331(pager);
        return new RegistrationWorkingSet(makeDTOs(pager.getRecords()));
    }


    /**
     * @param reference
     * @return
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
     * @throws RegistrationValidationException
     */
    @Override
    public RegistrationWorkingSet loadWorkingSetByReferenceID(Integer referenceID, boolean resolveSections) throws RegistrationValidationException {

        Reference reference = repo.getReferenceService().find(referenceID);
        if(resolveSections){
            reference = resolveSection(reference);
        }
        repo.getReferenceService().load(reference.getUuid()); // needed to avoid the problem described in #7331

        Pager<Registration> pager = repo.getRegistrationService().page(Optional.of(reference), null, null, null, REGISTRATION_DTO_INIT_STRATEGY);

        /* for debugging https://dev.e-taxonomy.eu/redmine/issues/7331 */
        // debugIssue7331(pager);

        return new RegistrationWorkingSet(makeDTOs(pager.getRecords()));
    }


    /**
     * @param pager
     */
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

    /**
     * @param regs
     * @return
     */
    private List<RegistrationDTO> makeDTOs(List<Registration> regs) {
        initializeSpecimens(regs);
        List<RegistrationDTO> dtos = new ArrayList<>(regs.size());
        regs.forEach(reg -> {dtos.add(new RegistrationDTO(reg));});
        return dtos;
    }


    /**
     * @param regs
     */
    public void initializeSpecimens(List<Registration> regs) {
        for(Registration reg : regs){
            inititializeSpecimen(reg);
        }

    }


    /**
     * @param reg
     */
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
                            defaultBeanInitializer.initialize(sob, DERIVEDUNIT_INIT_STRATEGY);
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
