// $Id: CdmApplicationController.java 11680 2011-04-04 17:07:39Z a.mueller $
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */


package eu.etaxonomy.cdm.api.application;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ICollectionService;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IFeatureNodeService;
import eu.etaxonomy.cdm.api.service.IFeatureTreeService;
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
import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.common.NullProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;


/**
 * @author a.mueller
 * @author j.koch
 *
 */
public class CdmApplicationRemoteController implements ICdmApplicationRemoteConfiguration{
	private static final Logger logger = Logger.getLogger(CdmApplicationRemoteController.class);
	
	public static final String DEFAULT_APPLICATION_CONTEXT_RESOURCE = "/eu/etaxonomy/cdm/remoteApplicationContext.xml";
	
	public AbstractApplicationContext applicationContext;
	private ICdmApplicationRemoteConfiguration configuration; 
	private Resource applicationContextResource;
	private IProgressMonitor progressMonitor;
	
	/**
	 * Constructor, opens a spring ApplicationContext with defaults
	 */
	public static CdmApplicationRemoteController NewInstance() {
		logger.info("Configure CdmApplicationRemoteController with defaults");
		return new CdmApplicationRemoteController(null, null);
	}
	
	/**
	 * Constructor, opens a spring ApplicationContext with given application context
	 * @param applicationContextResource
	 */
	public static CdmApplicationRemoteController NewInstance(Resource applicationContextResource, IProgressMonitor progressMonitor) {
		logger.info("Configure CdmApplicationRemoteController with given application context");
		return new CdmApplicationRemoteController(applicationContextResource, progressMonitor);
	}

	/**
	 * Constructor, starts the application remote controller
	 * @param applicationContextResource
	 */
	private CdmApplicationRemoteController(Resource applicationContextResource, IProgressMonitor progressMonitor){
		logger.info("Start CdmApplicationRemoteController");
		this.applicationContextResource = applicationContextResource != null ? applicationContextResource : new ClassPathResource(DEFAULT_APPLICATION_CONTEXT_RESOURCE);
		this.progressMonitor = progressMonitor != null ? progressMonitor : new NullProgressMonitor();
		setNewApplicationContext();
	}
		
	/**
	 * Sets the application context to a new spring ApplicationContext and initializes the Controller
	 */
	private boolean setNewApplicationContext(){
		logger.info("Set new application context");
		progressMonitor.beginTask("Start application context.", 6);
		progressMonitor.worked(1);

		GenericApplicationContext applicationContext =  new GenericApplicationContext();
		
		XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(applicationContext);
		progressMonitor.subTask("Registering resources.");
		xmlReader.loadBeanDefinitions(applicationContextResource);
		progressMonitor.worked(1);
				
		progressMonitor.subTask("This might take a while ...");
		applicationContext.refresh();
		applicationContext.start();
		progressMonitor.worked(1);
		
		progressMonitor.subTask("Cleaning up.");
		setApplicationContext(applicationContext);
		progressMonitor.done();
		return true;
	}
	
	/**
	 * Tests if some DefinedTermsAreMissing.
	 * @return true, if at least one is missing, else false
	 */
	public boolean testDefinedTermsAreMissing(){
		UUID englishUuid = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
		DefinedTermBase<?> english = this.getTermService().getByUri(englishUuid.toString());
		if ( english == null || ! english.getUuid().equals(englishUuid)){
			return true;
		}else{
			return false;
		}
	}
		
	/**
	 * Sets a new application Context.
	 * @param ac
	 */
	public void setApplicationContext(AbstractApplicationContext ac){
		closeApplicationContext(); //closes old application context if necessary
		applicationContext = ac;
		applicationContext.registerShutdownHook();
		init();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	public void finalize(){
		close();
	}
	
	/**
	 * closes the application
	 */
	public void close(){
		closeApplicationContext();
	}
	
	/**
	 * closes the application context
	 */
	private void closeApplicationContext(){
		if (applicationContext != null){
			logger.info("Close ApplicationContext");
			applicationContext.close();
		}
	}
	
	private void init(){
		logger.info("Init " +  this.getClass().getName() + " ... ");
		if (logger.isInfoEnabled()){for (String beanName : applicationContext.getBeanDefinitionNames()){ logger.debug(beanName);}}
		//TODO delete next row (was just for testing)
		if (logger.isInfoEnabled()){
			logger.info("Registered Beans: ");
			String[] beanNames = applicationContext.getBeanDefinitionNames();
			for (String beanName : beanNames){
				logger.info(beanName);
			}
		}
		configuration = (ICdmApplicationRemoteConfiguration)applicationContext.getBean("cdmApplicationRemoteDefaultConfiguration");
	}
	

	/* ******  Services *********/
	
	public final INameService getNameService(){
		return configuration.getNameService();
	}

	public final ITaxonService getTaxonService(){
		return configuration.getTaxonService();
	}
	
	public final IClassificationService getClassificationService(){
		return configuration.getClassificationService();
	}
	
	public final ITaxonNodeService getTaxonNodeService(){
		return configuration.getTaxonNodeService();
	}

	public final IReferenceService getReferenceService(){
		return configuration.getReferenceService();
	}
	
	public final IAgentService getAgentService(){
		return configuration.getAgentService();
	}
	
//	public final IDatabaseService getDatabaseService(){
//		return null;
//	}
	
	public final ITermService getTermService(){
		return configuration.getTermService();
	}

	public final IDescriptionService getDescriptionService(){
		return configuration.getDescriptionService();
	}
	
	public final IOccurrenceService getOccurrenceService(){
		return configuration.getOccurrenceService();
	}

	public final IMediaService getMediaService(){
		return configuration.getMediaService();
	}

	public final ICommonService getCommonService(){
		return configuration.getCommonService();
	}
	
	public final ILocationService getLocationService(){
		return configuration.getLocationService();
	}
	
	public final IUserService getUserService(){
		return configuration.getUserService();
	}

	public IGroupService getGroupService() {
		return configuration.getGroupService();
	}
	
	public final ICollectionService getCollectionService(){
		return configuration.getCollectionService();
	}
	
	public final IFeatureTreeService getFeatureTreeService(){
		return configuration.getFeatureTreeService();
	}
	
	public final IFeatureNodeService getFeatureNodeService(){
		return configuration.getFeatureNodeService();
	}
	
	public final IVocabularyService getVocabularyService(){
		return configuration.getVocabularyService();
	}
	
	public final IIdentificationKeyService getIdentificationKeyService(){
		return configuration.getIdentificationKeyService();
	}

	public final IPolytomousKeyService getPolytomousKeyService(){
		return configuration.getPolytomousKeyService();
	}

	public final IPolytomousKeyNodeService getPolytomousKeyNodeService(){
		return configuration.getPolytomousKeyNodeService();
	}
	
	public final IService<CdmBase> getMainService(){
		return configuration.getMainService();
	}
	
	public final IWorkingSetService getWorkingSetService(){
		return configuration.getWorkingSetService();
	}
	
//	public final ConversationHolder NewConversation(){
//		//return (ConversationHolder)applicationContext.getBean("conversationHolder");
//		return configuration.NewConversation();
//	}
//	
//	public final ProviderManager getAuthenticationManager(){
//		return configuration.getAuthenticationManager();
//	}
//	
//	@Override
//	public final PlatformTransactionManager getTransactionManager() {
//		return configuration.getTransactionManager();
//	}
	
	public final Object getBean(String name){
		return this.applicationContext.getBean(name);
	}

}
