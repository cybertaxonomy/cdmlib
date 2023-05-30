/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.application;

import java.util.EnumSet;

import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.IAnnotationService;
import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ICollectionService;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.IDescriptionElementService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IDescriptiveDataSetService;
import eu.etaxonomy.cdm.api.service.IEntityConstraintViolationService;
import eu.etaxonomy.cdm.api.service.IEntityValidationService;
import eu.etaxonomy.cdm.api.service.IEventBaseService;
import eu.etaxonomy.cdm.api.service.IGrantedAuthorityService;
import eu.etaxonomy.cdm.api.service.IGroupService;
import eu.etaxonomy.cdm.api.service.IIdentificationKeyService;
import eu.etaxonomy.cdm.api.service.ILocationService;
import eu.etaxonomy.cdm.api.service.IMediaService;
import eu.etaxonomy.cdm.api.service.IMetadataService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.IPolytomousKeyNodeService;
import eu.etaxonomy.cdm.api.service.IPolytomousKeyService;
import eu.etaxonomy.cdm.api.service.IPreferenceService;
import eu.etaxonomy.cdm.api.service.IProgressMonitorService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.IRegistrationService;
import eu.etaxonomy.cdm.api.service.IRightsService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermCollectionService;
import eu.etaxonomy.cdm.api.service.ITermNodeService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.ITermTreeService;
import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.geo.IDistributionService;
import eu.etaxonomy.cdm.api.service.longrunningService.ILongRunningTasksService;
import eu.etaxonomy.cdm.api.service.media.MediaInfoFactory;
import eu.etaxonomy.cdm.api.service.molecular.IAmplificationService;
import eu.etaxonomy.cdm.api.service.molecular.IPrimerService;
import eu.etaxonomy.cdm.api.service.molecular.ISequenceService;
import eu.etaxonomy.cdm.api.service.registration.IRegistrationWorkingSetService;
import eu.etaxonomy.cdm.api.service.security.IAccountRegistrationService;
import eu.etaxonomy.cdm.api.service.security.IPasswordResetService;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.permission.CRUD;
import eu.etaxonomy.cdm.persistence.permission.ICdmPermissionEvaluator;

/**
 * @author a.mueller
 * @date 24.11.2022
 */
public abstract class CdmApplicationControllerBase<R extends ICdmRepository>
        implements ICdmRepository {

    protected R configuration;

    //** TODO Unclear methods, should they really be in base class?

    @Override
    public final ILongRunningTasksService getLongRunningTasksService(){
        return configuration.getLongRunningTasksService();
    }

    @Override
    public IPasswordResetService getPasswordResetService() {
        return configuration.getPasswordResetService();
    }

    @Override
    public IAccountRegistrationService getAccountRegistrationService() {
        return configuration.getAccountRegistrationService();
    }


    @Override
    public IRegistrationWorkingSetService getRegistrationWorkingSetService() {
        return configuration.getRegistrationWorkingSetService();
    }

    @Override
    public MediaInfoFactory getMediaInfoFactory() {
        return configuration.getMediaInfoFactory();
    }


    @Override
    public final IDatabaseService getDatabaseService(){
        return configuration.getDatabaseService();
    }


    /*..... **** Security ***** */

    @Override
    public void authenticate(String username, String password){
        UsernamePasswordAuthenticationToken tokenForUser = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = this.getAuthenticationManager().authenticate(tokenForUser);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
    }

    @Override
    public final ProviderManager getAuthenticationManager(){
        return configuration.getAuthenticationManager();
    }

    /**
     * @see org.springframework.security.access.PermissionEvaluator#hasPermission(org.springframework.security.core.Authentication,
     *      java.lang.Object, java.lang.Object)
     *
     * @param targetDomainObject
     * @param permission
     * @return
     */
    public boolean currentAuthentiationHasPermissions(CdmBase targetDomainObject, EnumSet<CRUD> permission){
        SecurityContext context = SecurityContextHolder.getContext();
        return getPermissionEvaluator().hasPermission(context.getAuthentication(), targetDomainObject, permission);
    }

    abstract protected void init();

    /* ****** Services ******** */
    @Override
    public final IAnnotationService getAnnotationService(){
        return configuration.getAnnotationService();
    }

    @Override
    public final INameService getNameService(){
        return configuration.getNameService();
    }

    @Override
    public final ITaxonService getTaxonService(){
        return configuration.getTaxonService();
    }

    @Override
    public final IClassificationService getClassificationService(){
        return configuration.getClassificationService();
    }

    @Override
    public final ITaxonNodeService getTaxonNodeService(){
        return configuration.getTaxonNodeService();
    }

    @Override
    public final IReferenceService getReferenceService(){
        return configuration.getReferenceService();
    }

    @Override
    public final IAgentService getAgentService(){
        return configuration.getAgentService();
    }

    @Override
    public final ITermService getTermService(){
        return configuration.getTermService();
    }

    @Override
    public final IDescriptionService getDescriptionService(){
        return configuration.getDescriptionService();
    }

    @Override
    public final IDistributionService getDistributionService(){
        return configuration.getDistributionService();
    }

    @Override
    public final IDescriptionElementService getDescriptionElementService(){
        return configuration.getDescriptionElementService();
    }

    @Override
    public final IOccurrenceService getOccurrenceService(){
        return configuration.getOccurrenceService();
    }

    @Override
    public IAmplificationService getAmplificationService(){
        return configuration.getAmplificationService();
    }

    @Override
    public ISequenceService getSequenceService(){
        return configuration.getSequenceService();
    }

    @Override
    public IEventBaseService getEventBaseService() {
        return configuration.getEventBaseService();
    }

    @Override
    public final IPrimerService getPrimerService(){
        return configuration.getPrimerService();
    }


    @Override
    public final IMediaService getMediaService(){
        return configuration.getMediaService();
    }


    @Override
    public final IMetadataService getMetadataService(){
        return configuration.getMetadataService();
    }


    @Override
    public final ICommonService getCommonService(){
        return configuration.getCommonService();
    }


    @Override
    public final ILocationService getLocationService(){
        return configuration.getLocationService();
    }


    @Override
    public final IUserService getUserService(){
        return configuration.getUserService();
    }


    @Override
    public final IGrantedAuthorityService getGrantedAuthorityService(){
        return configuration.getGrantedAuthorityService();
    }


    @Override
    public IGroupService getGroupService(){
        return configuration.getGroupService();
    }


    @Override
    public final ICollectionService getCollectionService(){
        return configuration.getCollectionService();
    }

    @Override
    public final ITermTreeService getTermTreeService(){
        return configuration.getTermTreeService();
    }

    @Override
    public ITermCollectionService getTermCollectionService() {
        return configuration.getTermCollectionService();
    }

    @Override
    public final IPreferenceService getPreferenceService(){
        return configuration.getPreferenceService();
    }

    @Override
    public final ITermNodeService getTermNodeService(){
        return configuration.getTermNodeService();
    }

    @Override
    public final IVocabularyService getVocabularyService(){
        return configuration.getVocabularyService();
    }

    @Override
    public final IIdentificationKeyService getIdentificationKeyService(){
        return configuration.getIdentificationKeyService();
    }

    @Override
    public final IPolytomousKeyService getPolytomousKeyService(){
        return configuration.getPolytomousKeyService();
    }

    @Override
    public final IPolytomousKeyNodeService getPolytomousKeyNodeService(){
        return configuration.getPolytomousKeyNodeService();
    }

    @Override
    public IRightsService getRightsService() {
        return configuration.getRightsService();
    }

    @Override
    public IRegistrationService getRegistrationService() {
        return configuration.getRegistrationService();
    }

    @Override
    public IProgressMonitorService getProgressMonitorService() {
        return configuration.getProgressMonitorService();
    }

    @Override
    public IEntityValidationService getEntityValidationService(){
        return configuration.getEntityValidationService();
    }

    @Override
    public IEntityConstraintViolationService getEntityConstraintViolationService(){
        return configuration.getEntityConstraintViolationService();
    }

    @Override
    public final IDescriptiveDataSetService getDescriptiveDataSetService(){
        return configuration.getDescriptiveDataSetService();
    }

    @Override
    public ICdmPermissionEvaluator getPermissionEvaluator(){
        return configuration.getPermissionEvaluator();
    }





}