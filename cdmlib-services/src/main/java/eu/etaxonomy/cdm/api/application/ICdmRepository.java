/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.application;

import org.springframework.security.authentication.ProviderManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.IAnnotationService;
import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ICollectionService;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
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
import eu.etaxonomy.cdm.api.service.ITermNodeService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.ITermTreeService;
import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.longrunningService.ILongRunningTasksService;
import eu.etaxonomy.cdm.api.service.media.MediaInfoFactory;
import eu.etaxonomy.cdm.api.service.molecular.IAmplificationService;
import eu.etaxonomy.cdm.api.service.molecular.IPrimerService;
import eu.etaxonomy.cdm.api.service.molecular.ISequenceService;
import eu.etaxonomy.cdm.api.service.security.IAccountRegistrationService;
import eu.etaxonomy.cdm.api.service.security.IPasswordResetService;
import eu.etaxonomy.cdm.database.ICdmImportSource;
import eu.etaxonomy.cdm.persistence.permission.CdmPermissionEvaluator;
import eu.etaxonomy.cdm.persistence.permission.ICdmPermissionEvaluator;

/**
 * @author a.mueller
 * @since 21.05.2008
 */
public interface ICdmRepository extends ICdmImportSource {

    /**
     * Starts a read only transaction
     */
    public TransactionStatus startTransaction();

    public TransactionStatus startTransaction(Boolean readOnly);

    public void commitTransaction(TransactionStatus tx);

    public void rollbackTransaction(TransactionStatus txStatus);

	public Object getBean(String string);

    public IAnnotationService getAnnotationService();

	public INameService getNameService();

	public ITaxonService getTaxonService();

	public IClassificationService getClassificationService();

	public ITaxonNodeService getTaxonNodeService();

	public IReferenceService getReferenceService();

    public IAccountRegistrationService getAccountRegistrationService();

	public IAgentService getAgentService();

	public IDescriptionService getDescriptionService();

	public IOccurrenceService getOccurrenceService();

	public IPrimerService getPrimerService();

	public IAmplificationService getAmplificationService();

	public ISequenceService getSequenceService();

	public IEventBaseService getEventBaseService();

	public IMediaService getMediaService();

    public IMetadataService getMetadataService();

	public IDatabaseService getDatabaseService();

	public ITermService getTermService();

	public ICommonService getCommonService();

	public ILocationService getLocationService();

	public IUserService getUserService();

	public IGroupService getGroupService();

    public IPreferenceService getPreferenceService();

	public IGrantedAuthorityService getGrantedAuthorityService();

	public IDescriptiveDataSetService getDescriptiveDataSetService();

	public PlatformTransactionManager getTransactionManager();

	public ProviderManager getAuthenticationManager();

	public ConversationHolder NewConversation();

	public ICollectionService getCollectionService();

	public ILongRunningTasksService getLongRunningTasksService();

    public ITermTreeService getTermTreeService();

    public ITermNodeService getTermNodeService();

	public IVocabularyService getVocabularyService();

	public IIdentificationKeyService getIdentificationKeyService();

	public IPolytomousKeyService getPolytomousKeyService();

	public IPolytomousKeyNodeService getPolytomousKeyNodeService();

	public IProgressMonitorService getProgressMonitorService();

	public IEntityValidationService getEntityValidationService();

    public IRightsService getRightsService();

	public IEntityConstraintViolationService getEntityConstraintViolationService();

    public IRegistrationService getRegistrationService();

	/**
	 * @return the configured PermissionEvaluator, usually the
	 *         {@link CdmPermissionEvaluator}
	 */
	public ICdmPermissionEvaluator getPermissionEvaluator();

	public IPasswordResetService getPasswordResetService();

	public MediaInfoFactory getMediaInfoFactory(); // FIXME define and use interface

	void authenticate(String username, String password);

}
