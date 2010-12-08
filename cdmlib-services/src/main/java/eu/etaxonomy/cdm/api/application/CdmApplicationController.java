// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */


package eu.etaxonomy.cdm.api.application;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.ICollectionService;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IFeatureNodeService;
import eu.etaxonomy.cdm.api.service.IFeatureTreeService;
import eu.etaxonomy.cdm.api.service.IIdentificationKeyService;
import eu.etaxonomy.cdm.api.service.ILocationService;
import eu.etaxonomy.cdm.api.service.IMediaService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.IPolytomousKeyService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.IWorkingSetService;
import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.common.NullProgressMonitor;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.CdmMetaData;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.User;


/**
 * @author a.mueller
 *
 */
public class CdmApplicationController implements ICdmApplicationConfiguration{
	private static final Logger logger = Logger.getLogger(CdmApplicationController.class);
	
	public static final String DEFAULT_APPLICATION_CONTEXT_RESOURCE = "/eu/etaxonomy/cdm/defaultApplicationContext.xml";
	
	public AbstractApplicationContext applicationContext;
	private ICdmApplicationConfiguration configuration; 
	private Resource applicationContextResource;

	private IProgressMonitor progressMonitor;
	
	final static DbSchemaValidation defaultDbSchemaValidation = DbSchemaValidation.VALIDATE;
	
	
	
	/**
	 * Constructor, opens a spring ApplicationContext by using the default data source
	 */
	public static CdmApplicationController NewInstance() {
		logger.info("Start CdmApplicationController with default data source");
		CdmPersistentDataSource dataSource = CdmPersistentDataSource.NewDefaultInstance();
		DbSchemaValidation dbSchemaValidation = defaultDbSchemaValidation;
		return CdmApplicationController.NewInstance(null, dataSource, dbSchemaValidation, false);
	}
	
	/**
	 * Constructor, opens a spring ApplicationContext by using the default data source
	 * @param dbSchemaValidation validation type for database schema
	 */
	public static CdmApplicationController NewInstance(DbSchemaValidation dbSchemaValidation) {
		logger.info("Start CdmApplicationController with default data source");
		CdmPersistentDataSource dataSource = CdmPersistentDataSource.NewDefaultInstance();
		return CdmApplicationController.NewInstance(null, dataSource, dbSchemaValidation, false);
	}

	
	/**
	 * Constructor, opens an spring ApplicationContext by using the according data source and the
	 * default database schema validation type
	 * @param dataSource
	 */
	public static CdmApplicationController NewInstance(ICdmDataSource dataSource) {
		return CdmApplicationController.NewInstance(null, dataSource, defaultDbSchemaValidation, false);
	}
	
	public static CdmApplicationController NewInstance(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation) {
		return CdmApplicationController.NewInstance(null, dataSource, dbSchemaValidation, false);
	}

	public static CdmApplicationController NewInstance(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading) {
		return CdmApplicationController.NewInstance(null, dataSource, dbSchemaValidation, omitTermLoading);
	}
	
	public static CdmApplicationController NewInstance(Resource applicationContextResource, ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading) {
		return CdmApplicationController.NewInstance(applicationContextResource, dataSource, dbSchemaValidation, omitTermLoading, null);
	}
	
	public static CdmApplicationController NewInstance(Resource applicationContextResource, ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading, IProgressMonitor progressMonitor) {
		return new CdmApplicationController(applicationContextResource, dataSource, dbSchemaValidation, omitTermLoading, progressMonitor);
	}

	/**
	 * Constructor, opens an spring 2.5 ApplicationContext by using the according data source
	 * @param dataSource
	 * @param dbSchemaValidation
	 * @param omitTermLoading
	 */
	private CdmApplicationController(Resource applicationContextResource, ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading, IProgressMonitor progressMonitor){
		logger.info("Start CdmApplicationController with datasource: " + dataSource.getName());
		
		if (dbSchemaValidation == null){
			dbSchemaValidation = defaultDbSchemaValidation;
		}
		
		this.applicationContextResource = applicationContextResource != null ? applicationContextResource : new ClassPathResource(DEFAULT_APPLICATION_CONTEXT_RESOURCE);
		this.progressMonitor = progressMonitor != null ? progressMonitor : new NullProgressMonitor();
		
		setNewDataSource(dataSource, dbSchemaValidation, omitTermLoading);
	}
		
	
	/**
	 * Sets the application context to a new spring ApplicationContext by using the according data source and initializes the Controller.
	 * @param dataSource
	 */
	private boolean setNewDataSource(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading){
		if (dbSchemaValidation == null){
			dbSchemaValidation = defaultDbSchemaValidation;
		}
		logger.info("Connecting to '" + dataSource.getName() + "'");
		progressMonitor.beginTask("Connecting to '" + dataSource.getName() + "'", 6);
		progressMonitor.worked(1);

		GenericApplicationContext applicationContext =  new GenericApplicationContext();
		
		BeanDefinition datasourceBean = dataSource.getDatasourceBean();
		datasourceBean.setAttribute("isLazy", false);
		progressMonitor.subTask("Registering datasource.");
		applicationContext.registerBeanDefinition("dataSource", datasourceBean);
		progressMonitor.worked(1);
		
		BeanDefinition hibernatePropBean= dataSource.getHibernatePropertiesBean(dbSchemaValidation);
		applicationContext.registerBeanDefinition("hibernateProperties", hibernatePropBean);
		
		XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(applicationContext);
		progressMonitor.subTask("Registering resources.");
		xmlReader.loadBeanDefinitions(applicationContextResource);
		progressMonitor.worked(1);
		
		//omitTerms
		/*String initializerName = "persistentTermInitializer";
		BeanDefinition beanDef = applicationContext.getBeanDefinition(initializerName);
		MutablePropertyValues values = beanDef.getPropertyValues();
		values.addPropertyValue("omit", omitTermLoading);*/
		
		progressMonitor.subTask("This might take a while ...");
		applicationContext.refresh();
		applicationContext.start();
		progressMonitor.worked(1);
		
		progressMonitor.subTask("Cleaning up.");
		setApplicationContext(applicationContext);
		progressMonitor.worked(1);
		
		//initialize user and metaData for new databases
		int userCount = getUserService().count(User.class);
		if (userCount == 0 ){
			progressMonitor.subTask("Creating Admin User");
			createAdminUser();
			progressMonitor.worked(1);
		}
		int metaDataCount = getCommonService().getCdmMetaData().size();
		if (metaDataCount == 0){
			progressMonitor.subTask("Creating Meta Data");
			createMetadata();
			progressMonitor.worked(1);
		}
		progressMonitor.done();
		return true;
	}

	private void createAdminUser(){
		User firstUser = User.NewInstance("admin", "0000");
		getUserService().save(firstUser);
		logger.info("Admin user created.");
	}
	
	private void createMetadata(){
		List<CdmMetaData> metaData = CdmMetaData.defaultMetaData();
		getCommonService().saveAllMetaData(metaData);
		logger.info("Metadata created.");
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
	 * Changes the ApplicationContext to the new dataSource
	 * @param dataSource
	 */
	public boolean changeDataSource(ICdmDataSource dataSource){
		//logger.info("Change datasource to : " + dataSource);
		return setNewDataSource(dataSource, DbSchemaValidation.VALIDATE, false);
	}
	
	/**
	 * Changes the ApplicationContext to the new dataSource
	 * @param dataSource
	 * @param dbSchemaValidation
	 */
	public boolean changeDataSource(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation){
		//logger.info("Change datasource to : " + dataSource);
		return setNewDataSource(dataSource, dbSchemaValidation, false);
	}
	
	/**
	 * Changes the ApplicationContext to the new dataSource
	 * @param dataSource
	 * @param dbSchemaValidation
	 * @param omitTermLoading
	 */
	public boolean changeDataSource(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading){
		logger.info("Change datasource to : " + dataSource);
		return setNewDataSource(dataSource, dbSchemaValidation, omitTermLoading);
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
		logger.debug("Init " +  this.getClass().getName() + " ... ");
		if (logger.isDebugEnabled()){for (String beanName : applicationContext.getBeanDefinitionNames()){ logger.debug(beanName);}}
		//TODO delete next row (was just for testing)
		if (logger.isInfoEnabled()){
			logger.info("Registered Beans: ");
			String[] beanNames = applicationContext.getBeanDefinitionNames();
			for (String beanName : beanNames){
				logger.info(beanName);
			}
		}
		configuration = (ICdmApplicationConfiguration)applicationContext.getBean("cdmApplicationDefaultConfiguration");
		getDatabaseService().setApplicationController(this);
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
	
	public final IDatabaseService getDatabaseService(){
		return configuration.getDatabaseService();
	}
	
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
	
	public final IService<CdmBase> getMainService(){
		return configuration.getMainService();
	}
	
	public final IWorkingSetService getWorkingSetService(){
		return configuration.getWorkingSetService();
	}
	
	public final ConversationHolder NewConversation(){
		//return (ConversationHolder)applicationContext.getBean("conversationHolder");
		return configuration.NewConversation();
	}
	
	
	public final ProviderManager getAuthenticationManager(){
		return configuration.getAuthenticationManager();
	}
	

	@Override
	public final PlatformTransactionManager getTransactionManager() {
		return configuration.getTransactionManager();
	}
	
	public final Object getBean(String name){
		return this.applicationContext.getBean(name);
	}
	
	/*
	 * OLD TRANSACTION STUFF 
	 */
	
	/* **** flush ***********/
	public void flush() {
		SessionFactory sf = (SessionFactory)applicationContext.getBean("sessionFactory");
		sf.getCurrentSession().flush();
	}
	
	public SessionFactory getSessionFactory(){
		return (SessionFactory)applicationContext.getBean("sessionFactory");
	}
	
	public TransactionStatus startTransaction() {
		
		return startTransaction(false);
	}
	
	public TransactionStatus startTransaction(Boolean readOnly) {
		
		PlatformTransactionManager txManager = configuration.getTransactionManager();
		
		DefaultTransactionDefinition defaultTxDef = new DefaultTransactionDefinition();
		defaultTxDef.setReadOnly(readOnly);
		TransactionDefinition txDef = defaultTxDef;

		// Log some transaction-related debug information.
		if (logger.isDebugEnabled()) {
			logger.debug("Transaction name = " + txDef.getName());
			logger.debug("Transaction facets:");
			logger.debug("Propagation behavior = " + txDef.getPropagationBehavior());
			logger.debug("Isolation level = " + txDef.getIsolationLevel());
			logger.debug("Timeout = " + txDef.getTimeout());
			logger.debug("Read Only = " + txDef.isReadOnly());
			// org.springframework.orm.hibernate3.HibernateTransactionManager
			// provides more transaction/session-related debug information.
		}
		
		TransactionStatus txStatus = txManager.getTransaction(txDef);
		return txStatus;
	}

	public void commitTransaction(TransactionStatus txStatus){
		PlatformTransactionManager txManager = configuration.getTransactionManager();
		txManager.commit(txStatus);
		return;
	}

}
