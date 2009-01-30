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
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.etaxonomy.cdm.api.application.eclipse.EclipseRcpSaveGenericApplicationContext;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IMediaService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;


/**
 * @author a.mueller
 *
 */
public class CdmApplicationController {
	private static final Logger logger = Logger.getLogger(CdmApplicationController.class);
	
	public AbstractApplicationContext applicationContext;
	private ICdmApplicationConfiguration configuration;
	
	final static DbSchemaValidation defaultDbSchemaValidation = DbSchemaValidation.VALIDATE;
	
	
	/**
	 * Constructor, opens an spring 2.5 ApplicationContext by using the default data source
	 */
	public static CdmApplicationController NewInstance()  throws DataSourceNotFoundException, TermNotFoundException {
		logger.info("Start CdmApplicationController with default data source");
		CdmPersistentDataSource dataSource = CdmPersistentDataSource.NewDefaultInstance();
		DbSchemaValidation dbSchemaValidation = defaultDbSchemaValidation;
		return new CdmApplicationController(dataSource, dbSchemaValidation);
	}

	
	
	/**
	 * Constructor, opens an spring 2.5 ApplicationContext by using the default data source
	 * @param dbSchemaValidation validation type for database schema
	 */
	public static CdmApplicationController NewInstance(DbSchemaValidation dbSchemaValidation)  throws DataSourceNotFoundException, TermNotFoundException {
		logger.info("Start CdmApplicationController with default data source");
		CdmPersistentDataSource dataSource = CdmPersistentDataSource.NewDefaultInstance();
		if (dbSchemaValidation == null){
			dbSchemaValidation = defaultDbSchemaValidation;
		}
		return new CdmApplicationController(dataSource, dbSchemaValidation);
	}

	
	/**
	 * Constructor, opens an spring 2.5 ApplicationContext by using the according data source and the
	 * default database schema validation type
	 * @param dataSource
	 */
	public static CdmApplicationController NewInstance(ICdmDataSource dataSource) 
	throws DataSourceNotFoundException, TermNotFoundException{
		return new CdmApplicationController(dataSource, defaultDbSchemaValidation);
	}
	
	public static CdmApplicationController NewInstance(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation) 
	throws DataSourceNotFoundException, TermNotFoundException{
		return new CdmApplicationController(dataSource, dbSchemaValidation);
	}

	public static CdmApplicationController NewInstance(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading) 
	throws DataSourceNotFoundException, TermNotFoundException{
		return new CdmApplicationController(dataSource, dbSchemaValidation, omitTermLoading);
	}
	
	/**
	 * Constructor, opens an spring 2.5 ApplicationContext by using the according data source
	 * @param dataSource
	 * @param dbSchemaValidation
	 */
	private CdmApplicationController(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation) throws DataSourceNotFoundException, TermNotFoundException{
        this(dataSource, dbSchemaValidation, false);
	}

	/**
	 * Constructor, opens an spring 2.5 ApplicationContext by using the according data source
	 * @param dataSource
	 * @param dbSchemaValidation
	 * @param omitTermLoading
	 */
	private CdmApplicationController(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading) 
	throws DataSourceNotFoundException, TermNotFoundException{
		logger.info("Start CdmApplicationController with datasource: " + dataSource.getName());
		if (setNewDataSource(dataSource, dbSchemaValidation, omitTermLoading) == false){
			throw new DataSourceNotFoundException("Wrong datasource: " + dataSource );
		}
	}
	
	/**
	 * Sets the application context to a new spring ApplicationContext by using the according data source and initializes the Controller.
	 * @param dataSource
	 */
	private boolean setNewDataSource(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading) throws TermNotFoundException {
		if (dbSchemaValidation == null){
			dbSchemaValidation = defaultDbSchemaValidation;
		}
		logger.info("Connecting to '" + dataSource.getName() + "'");


		GenericApplicationContext appContext;
		try {
			appContext = new EclipseRcpSaveGenericApplicationContext();
			
			BeanDefinition datasourceBean = dataSource.getDatasourceBean();
			datasourceBean.setAttribute("isLazy", false);
			appContext.registerBeanDefinition("dataSource", datasourceBean);
			
			BeanDefinition hibernatePropBean= dataSource.getHibernatePropertiesBean(dbSchemaValidation);
			appContext.registerBeanDefinition("hibernateProperties", hibernatePropBean);
			
			XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appContext);
			xmlReader.loadBeanDefinitions(new ClassPathResource("/eu/etaxonomy/cdm/services.xml"));		 
			
            //TODO: This is a workaround to omit term loading for JAXB serializing/deserializing.
			/* should be able to omit defined terms loading using a */
//			CdmTermInitializer.omit = omitTermLoading;
			
			appContext.refresh();
			appContext.start();
			
//			CdmTermInitializer.omit = false;

		} catch (BeanCreationException e) {
			// create new schema
			if (dbSchemaValidation == DbSchemaValidation.VALIDATE) {
				logger.error("ApplicationContext could not be created. " +
					" Maybe your database schema is not up-to-date, " +
					" but there might be other BeanCreation problems too." +
					" Try to run CdmApplicationController with dbSchemaValidation.CREATE or dbSchemaValidation.UPDATE option. ");
			} else {
				logger.error("BeanCreationException (CdmApplicationController startet with " + dbSchemaValidation.toString() + " option.");
			}
			e.printStackTrace();
			return false;
		}
		setApplicationContext(appContext);
		// load defined terms if necessary 
		//TODO not necessary any more
//		if (CdmTermInitializer.omit == false) {
//			if (testDefinedTermsAreMissing()){
//				throw new TermNotFoundException("Some needed Terms are Missing.");
//			}
//		}
//		
//		CdmTermInitializer.omit = false;
		return true;
	}
	
	
	/**
	 * Tests if some DefinedTermsAreMissing.
	 * @return true, if at least one is missing, else false
	 */
	public boolean testDefinedTermsAreMissing(){
		UUID englishUuid = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
		DefinedTermBase<?> english = this.getTermService().getTermByUri(englishUuid.toString());
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
	public boolean changeDataSource(ICdmDataSource dataSource) throws TermNotFoundException {
		//logger.info("Change datasource to : " + dataSource);
		return setNewDataSource(dataSource, DbSchemaValidation.VALIDATE, false);
	}
	
	/**
	 * Changes the ApplicationContext to the new dataSource
	 * @param dataSource
	 * @param dbSchemaValidation
	 */
	public boolean changeDataSource(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation)  throws TermNotFoundException {
		//logger.info("Change datasource to : " + dataSource);
		return setNewDataSource(dataSource, dbSchemaValidation, false);
	}
	
	/**
	 * Changes the ApplicationContext to the new dataSource
	 * @param dataSource
	 * @param dbSchemaValidation
	 * @param omitTermLoading
	 */
	public boolean changeDataSource(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading)  
	throws TermNotFoundException {
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
			String[] beans = applicationContext.getBeanDefinitionNames();
			for (String bean:beans){
				logger.info(bean);
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
	
	public final IService<CdmBase> getMainService(){
		return configuration.getMainService();
	}
	
	/* **** flush ***********/
	public void flush() {
		SessionFactory sf = (SessionFactory)applicationContext.getBean("sessionFactory");
		sf.getCurrentSession().flush();
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
		logger.debug("Transaction name = " + txDef.getName());
		logger.debug("Transaction facets:");
		logger.debug("Propagation behavior = " + txDef.getPropagationBehavior());
		logger.debug("Isolation level = " + txDef.getIsolationLevel());
		logger.debug("Timeout = " + txDef.getTimeout());
		logger.debug("Read Only = " + txDef.isReadOnly());
		// org.springframework.orm.hibernate3.HibernateTransactionManager
		// provides more transaction/session-related debug information.
		
		TransactionStatus txStatus = txManager.getTransaction(txDef);
		return txStatus;
	}

	public void commitTransaction(TransactionStatus txStatus){
		PlatformTransactionManager txManager = configuration.getTransactionManager();
		txManager.commit(txStatus);
		return;
	}

}
