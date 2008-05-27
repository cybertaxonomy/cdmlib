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
import org.hsqldb.Server;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

import eu.etaxonomy.cdm.api.application.eclipse.EclipseRcpSaveGenericApplicationContext;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.database.ICdmDataSource;


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
	public static CdmApplicationController NewInstance(ICdmDataSource dataSource) throws DataSourceNotFoundException, TermNotFoundException{
		return new CdmApplicationController(dataSource, defaultDbSchemaValidation);
	}
	
	public static CdmApplicationController NewInstance(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation) throws DataSourceNotFoundException, TermNotFoundException{
		return new CdmApplicationController(dataSource, dbSchemaValidation);
	}


	/**
	 * Constructor, opens an spring 2.5 ApplicationContext by using the according data source
	 * @param dataSource
	 */
	private CdmApplicationController(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation) throws DataSourceNotFoundException, TermNotFoundException{
		logger.info("Start CdmApplicationController with datasource: " + dataSource.getName());
		if (setNewDataSource(dataSource, dbSchemaValidation) == false){
			throw new DataSourceNotFoundException("Wrong datasource: " + dataSource );
		}
	
	}

	
	/**
	 * Sets the application context to a new spring ApplicationContext by using the according data source and initializes the Controller.
	 * @param dataSource
	 */
	private boolean setNewDataSource(ICdmDataSource dataSource, DbSchemaValidation dbSchemaValidation) throws TermNotFoundException {
		if (dbSchemaValidation == null){
			dbSchemaValidation = defaultDbSchemaValidation;
		}
		logger.info("Connecting to '" + dataSource.getName() + "'");


		GenericApplicationContext appContext;
		try {
			appContext = new EclipseRcpSaveGenericApplicationContext();
			
			BeanDefinition datasourceBean = dataSource.getDatasourceBean();
			appContext.registerBeanDefinition("dataSource", datasourceBean);
			
			BeanDefinition hibernatePropBean= dataSource.getHibernatePropertiesBean(dbSchemaValidation);
			appContext.registerBeanDefinition("hibernateProperties", hibernatePropBean);
			
			XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appContext);
			xmlReader.loadBeanDefinitions(new ClassPathResource("/eu/etaxonomy/cdm/persistence.xml"));		 
			
			appContext.refresh();
			appContext.start();
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
		if (testDefinedTermsAreMissing()){
			throw new TermNotFoundException("Some needed Terms are Missing.");
		}
		return true;
	}
	
	
	/**
	 * Tests if some DefinedTermsAreMissing.
	 * @return true, if at least one is missing, else false
	 */
	public boolean testDefinedTermsAreMissing(){
		UUID englishUuid = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
		DefinedTermBase english = this.getTermService().getTermByUri(englishUuid.toString());
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
	public boolean changeDataSource(CdmPersistentDataSource dataSource) throws TermNotFoundException {
		logger.info("Change datasource to : " + dataSource);
		return setNewDataSource(dataSource, DbSchemaValidation.VALIDATE);
	}
	
	/**
	 * Changes the ApplicationContext to the new dataSource
	 * @param dataSource
	 */
	public boolean changeDataSource(CdmPersistentDataSource dataSource, DbSchemaValidation dbSchemaValidation)  throws TermNotFoundException {
		logger.info("Change datasource to : " + dataSource);
		return setNewDataSource(dataSource, dbSchemaValidation);
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
	
	/* **** flush ***********/
	public void flush() {
		SessionFactory sf = (SessionFactory)applicationContext.getBean("sessionFactory");
		sf.getCurrentSession().flush();
	}

}
