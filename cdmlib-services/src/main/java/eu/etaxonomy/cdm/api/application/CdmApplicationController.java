
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

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
import eu.etaxonomy.cdm.api.service.IEntityConstraintViolationService;
import eu.etaxonomy.cdm.api.service.IEntityValidationService;
import eu.etaxonomy.cdm.api.service.IEventBaseService;
import eu.etaxonomy.cdm.api.service.IFeatureNodeService;
import eu.etaxonomy.cdm.api.service.IFeatureTreeService;
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
import eu.etaxonomy.cdm.api.service.IProgressMonitorService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.IWorkingSetService;
import eu.etaxonomy.cdm.api.service.molecular.IAmplificationService;
import eu.etaxonomy.cdm.api.service.molecular.IPrimerService;
import eu.etaxonomy.cdm.api.service.molecular.ISequenceService;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.NullProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.SubProgressMonitor;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CRUD;
import eu.etaxonomy.cdm.persistence.hibernate.permission.ICdmPermissionEvaluator;

/**
 * @author a.mueller
 */
public class CdmApplicationController implements ICdmApplicationConfiguration {
	private static final Logger logger = Logger.getLogger(CdmApplicationController.class);

	public static final String DEFAULT_APPLICATION_CONTEXT_RESOURCE = "/eu/etaxonomy/cdm/defaultApplicationContext.xml";

	public AbstractApplicationContext applicationContext;
	protected ICdmApplicationConfiguration configuration;
	private final Resource applicationContextResource;

	private final IProgressMonitor progressMonitor;

	final protected static DbSchemaValidation defaultDbSchemaValidation = DbSchemaValidation.VALIDATE;


	/**
	 * Constructor, opens a spring ApplicationContext by using the default data source
	 *
	 * @throws DataSourceNotFoundException
	 */
	public static CdmApplicationController NewInstance() throws DataSourceNotFoundException{
		logger.info("Start CdmApplicationController with default data source");
		CdmPersistentDataSource dataSource = getDefaultDatasource();
		DbSchemaValidation dbSchemaValidation = defaultDbSchemaValidation;
		return CdmApplicationController.NewInstance(null, dataSource, dbSchemaValidation, false);
	}


	/**
	 * Constructor, opens a spring ApplicationContext by using the default data source
	 *
	 * @param dbSchemaValidation
	 *            validation type for database schema
	 * @throws DataSourceNotFoundException
	 */
	public static CdmApplicationController NewInstance(DbSchemaValidation dbSchemaValidation) throws DataSourceNotFoundException{
		logger.info("Start CdmApplicationController with default data source");
		CdmPersistentDataSource dataSource = getDefaultDatasource();
		return CdmApplicationController.NewInstance(null, dataSource, dbSchemaValidation, false);
	}


	/**
	 * Constructor, opens an spring ApplicationContext by using the according data source
	 * and the default database schema validation type
	 *
	 * @param dataSource
	 */
	public static CdmApplicationController NewInstance(ICdmDataSource dataSource){
		return CdmApplicationController.NewInstance(null, dataSource, defaultDbSchemaValidation, false);
	}


	public static CdmApplicationController NewInstance(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation){
		return CdmApplicationController.NewInstance(null, dataSource, dbSchemaValidation, false);
	}


	public static CdmApplicationController NewInstance(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading){
		return CdmApplicationController.NewInstance(null, dataSource, dbSchemaValidation, omitTermLoading);
	}

	public static CdmApplicationController NewInstance(Resource applicationContextResource, ICdmDataSource dataSource,
			DbSchemaValidation dbSchemaValidation, boolean omitTermLoading){
		return CdmApplicationController.NewInstance(applicationContextResource, dataSource, dbSchemaValidation, omitTermLoading, null);
	}

	public static CdmApplicationController NewInstance(Resource applicationContextResource, ICdmDataSource dataSource,
			DbSchemaValidation dbSchemaValidation, boolean omitTermLoading, IProgressMonitor progressMonitor){
		return new CdmApplicationController(applicationContextResource, dataSource, dbSchemaValidation, omitTermLoading, progressMonitor, null);
	}


	//TODO discuss need for listeners before commit to trunk
	//	public static CdmApplicationController NewInstance(Resource applicationContextResource, ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading, IProgressMonitor progressMonitor, List<ApplicationListener> listeners) {
	//		return new CdmApplicationController(applicationContextResource, dataSource, dbSchemaValidation, omitTermLoading, progressMonitor,listeners);
	//	}

	/**
	 * @return
	 */
	protected static ClassPathResource getClasspathResource(){
		return new ClassPathResource(DEFAULT_APPLICATION_CONTEXT_RESOURCE);
	}


	/**
	 * @return
	 * @throws DataSourceNotFoundException
	 */
	protected static CdmPersistentDataSource getDefaultDatasource() throws DataSourceNotFoundException{
		CdmPersistentDataSource dataSource = CdmPersistentDataSource.NewDefaultInstance();
		return dataSource;
	}


	/**
	 *
	 * FIXME:Remoting this constructor is added only to allow extension of this cntroller
	 * class. and should be removed after refactoring
	 */
	protected CdmApplicationController(){
		applicationContextResource = null;
		progressMonitor = null;
	}


	/**
	 * Constructor, opens an spring 2.5 ApplicationContext by using the according data
	 * source
	 *
	 * @param dataSource
	 * @param dbSchemaValidation
	 * @param omitTermLoading
	 */
	protected CdmApplicationController(Resource applicationContextResource, ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation,
			boolean omitTermLoading, IProgressMonitor progressMonitor, List<ApplicationListener> listeners){
		logger.info("Start CdmApplicationController with datasource: " + dataSource.getName());

		if (dbSchemaValidation == null) {
			dbSchemaValidation = defaultDbSchemaValidation;
		}
		this.applicationContextResource = applicationContextResource != null ? applicationContextResource : getClasspathResource();
		this.progressMonitor = progressMonitor != null ? progressMonitor : new NullProgressMonitor();

		setNewDataSource(dataSource, dbSchemaValidation, omitTermLoading, listeners);
	}


	/**
	 * Sets the application context to a new spring ApplicationContext by using the
	 * according data source and initializes the Controller.
	 *
	 * @param dataSource
	 */
	private boolean setNewDataSource(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading,
			List<ApplicationListener> listeners){

		if (dbSchemaValidation == null) {
			dbSchemaValidation = defaultDbSchemaValidation;
		}
		logger.info("Connecting to '" + dataSource.getName() + "'");

		MonitoredGenericApplicationContext applicationContext = new MonitoredGenericApplicationContext();
		int refreshTasks = 45;
		int nTasks = 5 + refreshTasks;
		//		nTasks += applicationContext.countTasks();
		progressMonitor.beginTask("Connecting to '" + dataSource.getName() + "'", nTasks);

		//		progressMonitor.worked(1);

		BeanDefinition datasourceBean = dataSource.getDatasourceBean();
		datasourceBean.setAttribute("isLazy", false);
		progressMonitor.subTask("Registering datasource.");
		applicationContext.registerBeanDefinition("dataSource", datasourceBean);
		progressMonitor.worked(1);

		BeanDefinition hibernatePropBean = dataSource.getHibernatePropertiesBean(dbSchemaValidation);
		applicationContext.registerBeanDefinition("hibernateProperties", hibernatePropBean);

		XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(applicationContext);
		progressMonitor.subTask("Registering resources.");
		xmlReader.loadBeanDefinitions(applicationContextResource);
		progressMonitor.worked(1);

		//omitTerms
		if (omitTermLoading == true) {
			String initializerName = "persistentTermInitializer";
			BeanDefinition beanDef = applicationContext.getBeanDefinition(initializerName);
			MutablePropertyValues values = beanDef.getPropertyValues();
			values.addPropertyValue("omit", omitTermLoading);
		}

		if (listeners != null) {
			for (ApplicationListener listener : listeners) {
				applicationContext.addApplicationListener(listener);
			}
		}

		//		String message = "Start application context. This might take a while ...";
		////		progressMonitor.subTask(message);
		//		SubProgressMonitor subMonitor= new SubProgressMonitor(progressMonitor, 10);
		//		subMonitor.beginTask(message, 2);
		//		applicationContext.setProgressMonitor(subMonitor);

		applicationContext.refresh(new SubProgressMonitor(progressMonitor, refreshTasks));
		applicationContext.start();
		//		progressMonitor.worked(1);

		progressMonitor.subTask("Cleaning up.");
		setApplicationContext(applicationContext);
		progressMonitor.worked(1);

		progressMonitor.done();
		return true;
	}


	/**
	 * Tests if some DefinedTermsAreMissing.
	 *
	 * @return true, if at least one is missing, else false
	 */
	public boolean testDefinedTermsAreMissing(){
		UUID englishUuid = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
		DefinedTermBase<?> english = this.getTermService().load(englishUuid);
		if (english == null || !english.getUuid().equals(englishUuid)) {
			return true;
		}
		else {
			return false;
		}
	}


	/**
	 * Changes the ApplicationContext to the new dataSource
	 *
	 * @param dataSource
	 */
	public boolean changeDataSource(ICdmDataSource dataSource){
		//logger.info("Change datasource to : " + dataSource);
		return setNewDataSource(dataSource, DbSchemaValidation.VALIDATE, false, null);
	}


	/**
	 * Changes the ApplicationContext to the new dataSource
	 *
	 * @param dataSource
	 * @param dbSchemaValidation
	 */
	public boolean changeDataSource(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation){
		//logger.info("Change datasource to : " + dataSource);
		return setNewDataSource(dataSource, dbSchemaValidation, false, null);
	}


	/**
	 * Changes the ApplicationContext to the new dataSource
	 *
	 * @param dataSource
	 * @param dbSchemaValidation
	 * @param omitTermLoading
	 */
	public boolean changeDataSource(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading){
		logger.info("Change datasource to : " + dataSource);
		return setNewDataSource(dataSource, dbSchemaValidation, omitTermLoading, null);
	}


	/**
	 * Changes the ApplicationContext to the new dataSource
	 *
	 * @param dataSource
	 * @param dbSchemaValidation
	 * @param omitTermLoading
	 */
	public boolean changeDataSource(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading,
			List<ApplicationListener> listeners){
		logger.info("Change datasource to : " + dataSource);
		return setNewDataSource(dataSource, dbSchemaValidation, omitTermLoading, listeners);
	}


	/**
	 * Sets a new application Context.
	 *
	 * @param ac
	 */
	public void setApplicationContext(AbstractApplicationContext ac){
		closeApplicationContext(); //closes old application context if necessary
		applicationContext = ac;
		applicationContext.registerShutdownHook();
		init();
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#finalize()
	 */
	@Override
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
	protected void closeApplicationContext(){
		if (applicationContext != null) {
			logger.info("Close ApplicationContext");
			applicationContext.close();
		}
	}


	protected void init(){
		logger.debug("Init " + this.getClass().getName() + " ... ");
		if (logger.isDebugEnabled()) {
			for (String beanName : applicationContext.getBeanDefinitionNames()) {
				logger.debug(beanName);
			}
		}
		//TODO delete next row (was just for testing)
		if (logger.isInfoEnabled()) {
			logger.info("Registered Beans: ");
			String[] beanNames = applicationContext.getBeanDefinitionNames();
			for (String beanName : beanNames) {
				logger.info(beanName);
			}
		}
		configuration = (ICdmApplicationConfiguration) applicationContext.getBean("cdmApplicationDefaultConfiguration");
		try {
			//FIXME:Remoting catching exection to allow for remoting
			getDatabaseService().setApplicationController(this);
		}
		catch (UnsupportedOperationException uoe) {
			logger.warn("getDatabaseService() is not implmented for current application context");
		}
	}


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
	public final IDatabaseService getDatabaseService(){
		return configuration.getDatabaseService();
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
	public final IFeatureTreeService getFeatureTreeService(){
		return configuration.getFeatureTreeService();
	}


	@Override
	public final IFeatureNodeService getFeatureNodeService(){
		return configuration.getFeatureNodeService();
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


    /**
     * {@inheritDoc}
     */
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
	public final IService<CdmBase> getMainService(){
		return configuration.getMainService();
	}


	@Override
	public final IWorkingSetService getWorkingSetService(){
		return configuration.getWorkingSetService();
	}


	@Override
	public final ConversationHolder NewConversation(){
		//return (ConversationHolder)applicationContext.getBean("conversationHolder");
		return configuration.NewConversation();
	}


	/* **** Security ***** */

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


	@Override
	public ICdmPermissionEvaluator getPermissionEvaluator(){
		return configuration.getPermissionEvaluator();
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


	@Override
	public final PlatformTransactionManager getTransactionManager(){
		return configuration.getTransactionManager();
	}


	@Override
	public final Object getBean(String name){
		return this.applicationContext.getBean(name);
	}


	/*
	 * OLD TRANSACTION STUFF
	 */

	/* **** flush ********** */
	public void flush(){
		SessionFactory sf = (SessionFactory) applicationContext.getBean("sessionFactory");
		sf.getCurrentSession().flush();
	}


	public SessionFactory getSessionFactory(){
		return (SessionFactory) applicationContext.getBean("sessionFactory");
	}


	@Override
	public TransactionStatus startTransaction(){
		return startTransaction(false);
	}


	@Override
	public TransactionStatus startTransaction(Boolean readOnly){
		return configuration.startTransaction(readOnly);
	}

	@Override
	public void commitTransaction(TransactionStatus txStatus){
		PlatformTransactionManager txManager = configuration.getTransactionManager();
		txManager.commit(txStatus);
		return;
	}

}

