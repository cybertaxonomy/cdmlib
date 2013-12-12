// $Id: CdmApplicationDefaultConfiguration.java 11680 2011-04-04 17:07:39Z a.mueller $
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.application;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.api.conversation.ConversationHolderMock;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ICollectionService;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IFeatureNodeService;
import eu.etaxonomy.cdm.api.service.IFeatureTreeService;
import eu.etaxonomy.cdm.api.service.IGrantedAuthorityService;
import eu.etaxonomy.cdm.api.service.IGroupService;
import eu.etaxonomy.cdm.api.service.IIdentificationKeyService;
import eu.etaxonomy.cdm.api.service.ILocationService;
import eu.etaxonomy.cdm.api.service.IMediaService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.IPolytomousKeyNodeService;
import eu.etaxonomy.cdm.api.service.IPolytomousKeyService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.IWorkingSetService;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 21.05.2008
 * @version 1.0
 */
/**
 * @author a.mueller
 * @author j.koch
 */
@Component
public class CdmApplicationRemoteDefaultConfiguration implements ICdmApplicationRemoteConfiguration, ApplicationContextAware {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmApplicationRemoteDefaultConfiguration.class);

	@Autowired
	//@Qualifier("nameService")
	private INameService nameService;
	@Autowired
	//@Qualifier("taxonService")
	private ITaxonService taxonService;
	@Autowired
	//@Qualifier("classificationService")
	private IClassificationService classificationService;
	@Autowired
	//@Qualifier("referenceService")
	private IReferenceService referenceService;
	@Autowired
	//@Qualifier("agentService")
	private IAgentService agentService;
	@Autowired
	//@Qualifier("termService")
	private ITermService termService;
	@Autowired
	//@Qualifier("descriptionService")
	private IDescriptionService descriptionService;
	@Autowired
	//@Qualifier("occurrenceService")
	private IOccurrenceService occurrenceService;
	@Autowired
	//@Qualifier("mediaService")
	private IMediaService mediaService;
	@Autowired
	//@Qualifier("commonService")
	private ICommonService commonService;
	@Autowired
	private ILocationService locationService;
	@Autowired
	private IUserService userService;
	@Autowired
	private IGroupService groupService;
	@Autowired
	private ICollectionService collectionService;
	@Autowired
	private IFeatureTreeService featureTreeService;
	@Autowired
	private IFeatureNodeService featureNodeService;
	@Autowired
	private IVocabularyService vocabularyService;
	@Autowired
	private ITaxonNodeService taxonNodeService;
	@Autowired
	private IIdentificationKeyService identificationKeyService;
	@Autowired
	private IPolytomousKeyService polytomousKeyService;
	@Autowired
	private IPolytomousKeyNodeService polytomousKeyNodeService;
    @Autowired
    private IGrantedAuthorityService grantedAuthorityService;
//	@Autowired
	//@Qualifier("mainService")
	private IService<CdmBase> mainService;

	@Autowired
	private IWorkingSetService workingSetService;
	@Autowired
	private ProviderManager authenticationManager;
    @Autowired
    private PermissionEvaluator permissionEvaluator;
	
    protected ApplicationContext applicationContext;
    
	/**
	 * 
	 */
	public CdmApplicationRemoteDefaultConfiguration() {
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getAgentService()
	 */
	public IAgentService getAgentService() {
		return this.agentService;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getNameService()
	 */
	public INameService getNameService() {
		return this.nameService;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getReferenceService()
	 */
	public IReferenceService getReferenceService() {
		return this.referenceService;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getTaxonService()
	 */
	public ITaxonService getTaxonService() {
		return this.taxonService;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getClassificationService()
	 */
	public IClassificationService getClassificationService() {
		return this.classificationService;
	}
	
	public ITaxonNodeService getTaxonNodeService(){
		return this.taxonNodeService;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getDescriptionService()
	 */
	public IDescriptionService getDescriptionService(){
		return this.descriptionService;
	}

	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getOccurrenceService()
	 */
	public IOccurrenceService getOccurrenceService(){
		return this.occurrenceService;
	}

	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getMediaService()
	 */
	public IMediaService getMediaService(){
		return this.mediaService;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getTermService()
	 */
	public ITermService getTermService() {
		return this.termService;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getCommonService()
	 */
	public ICommonService getCommonService(){
		return this.commonService;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getLocationService()
	 */
	public ILocationService getLocationService() {
		return this.locationService;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getUserService()
	 */
	public IUserService getUserService() {
		return this.userService;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getCommonService()
	 */
	public IService<CdmBase> getMainService(){
		return this.mainService;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getCollectionService()
	 */
	public ICollectionService getCollectionService() {
		return collectionService;
	}

	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getFeatureTreeService()
	 */
	public IFeatureTreeService getFeatureTreeService() {
		return featureTreeService;
	}

	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getFeatureNodeService()
	 */
	public IFeatureNodeService getFeatureNodeService(){
		return featureNodeService;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getVocabularyService()
	 */
	public IVocabularyService getVocabularyService() {
		return vocabularyService;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getIdentificationKeyService()
	 */
	public IIdentificationKeyService getIdentificationKeyService(){
		return identificationKeyService;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getPolytomousKeyService()
	 */
	public IPolytomousKeyService getPolytomousKeyService(){
		return polytomousKeyService;
	}
	
	public IPolytomousKeyNodeService getPolytomousKeyNodeService(){
		return polytomousKeyNodeService;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getWorkingSetService()
	 */
	@Override
	public IWorkingSetService getWorkingSetService() {
		return workingSetService;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmApplicationRemoteConfiguration#getGroupService()
	 */
	@Override
	public IGroupService getGroupService() {
		return groupService;
	}

	@Override
	public IDatabaseService getDatabaseService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProviderManager getAuthenticationManager() {
		return authenticationManager;
	}
	
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

	
    @Override
    public final Object getBean(String name){
        return this.applicationContext.getBean(name);
    }

	@Override
	public ConversationHolder NewConversation() {
		return new ConversationHolderMock();
	}
	
    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#authenticate(java.lang.String, java.lang.String)
     */
    @Override
	public void authenticate(String username, String password){
		UsernamePasswordAuthenticationToken tokenForUser = new UsernamePasswordAuthenticationToken(username, password);
		Authentication authentication = this.getAuthenticationManager().authenticate(tokenForUser);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
	}

	@Override
	public IGrantedAuthorityService getGrantedAuthorityService() {		
		return this.grantedAuthorityService;
	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration#getPermissionEvaluator()
     */
    @Override
    public PermissionEvaluator getPermissionEvaluator() {
        return permissionEvaluator;
    }
	
}
