package eu.etaxonomy.cdm.api.application;

import java.io.FileNotFoundException;
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
import eu.etaxonomy.cdm.database.CdmPersistentDataSource.HBM2DDL;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.NoDefinedTermClassException;
import eu.etaxonomy.cdm.model.common.init.TermLoader;
import eu.etaxonomy.cdm.database.ICdmDataSource;



/**
 * @author a.mueller
 *
 */
public class CdmApplicationController {
	private static final Logger logger = Logger.getLogger(CdmApplicationController.class);
	
	public AbstractApplicationContext applicationContext;
	private INameService nameService;
	private ITaxonService taxonService;
	private IReferenceService referenceService;
	private IAgentService agentService;
	private IDatabaseService databaseService;
	private ITermService termService;
	private Server hsqldbServer;
	
	final static HBM2DDL defaultHbm2dll = HBM2DDL.VALIDATE;
	
	
	
	/**
	 * Constructor, opens an spring 2.5 ApplicationContext by using the default data source
	 */
	public static CdmApplicationController NewInstance()  throws DataSourceNotFoundException {
		logger.info("Start CdmApplicationController with default data source");
		CdmPersistentDataSource dataSource = CdmPersistentDataSource.NewDefaultInstance();
		HBM2DDL hbm2dll = defaultHbm2dll;
		return new CdmApplicationController(dataSource, hbm2dll);
	}

	
	
	/**
	 * Constructor, opens an spring 2.5 ApplicationContext by using the default data source
	 * @param hbm2dll validation type for database schema
	 */
	public static CdmApplicationController NewInstance(HBM2DDL hbm2dll)  throws DataSourceNotFoundException {
		logger.info("Start CdmApplicationController with default data source");
		CdmPersistentDataSource dataSource = CdmPersistentDataSource.NewDefaultInstance();
		if (hbm2dll == null){
			hbm2dll = defaultHbm2dll;
		}
		return new CdmApplicationController(dataSource, hbm2dll);
	}

	
	/**
	 * Constructor, opens an spring 2.5 ApplicationContext by using the according data source and the
	 * default database schema validation type
	 * @param dataSource
	 */
	public static CdmApplicationController NewInstance(ICdmDataSource dataSource) throws DataSourceNotFoundException{
		return new CdmApplicationController(dataSource, defaultHbm2dll);
	}
	
	public static CdmApplicationController NewInstance(ICdmDataSource dataSource, HBM2DDL hbm2dll) throws DataSourceNotFoundException{
		return new CdmApplicationController(dataSource, hbm2dll);
	}


	/**
	 * Constructor, opens an spring 2.5 ApplicationContext by using the according data source
	 * @param dataSource
	 */
	private CdmApplicationController(ICdmDataSource dataSource, HBM2DDL hbm2dll) throws DataSourceNotFoundException{
		logger.info("Start CdmApplicationController with datasource: " + dataSource.getName());
		if (setNewDataSource(dataSource, hbm2dll) == false){
			throw new DataSourceNotFoundException("Wrong datasource: " + dataSource );
		}
	
	}

	
	/**
	 * Sets the application context to a new spring ApplicationContext by using the according data source and initializes the Controller.
	 * @param dataSource
	 */
	private boolean setNewDataSource(ICdmDataSource dataSource, HBM2DDL hbm2dll) {
		if (hbm2dll == null){
			hbm2dll = defaultHbm2dll;
		}
		logger.info("Connecting to '" + dataSource.getName() + "'");


		GenericApplicationContext appContext;
		try {
			appContext = new EclipseRcpSaveGenericApplicationContext();
			
			BeanDefinition datasourceBean = dataSource.getDatasourceBean();
			appContext.registerBeanDefinition("dataSource", datasourceBean);
			
			BeanDefinition hibernatePropBean= dataSource.getHibernatePropertiesBean(hbm2dll);
			appContext.registerBeanDefinition("hibernateProperties", hibernatePropBean);
			
			XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appContext);
			xmlReader.loadBeanDefinitions(new ClassPathResource("/eu/etaxonomy/cdm/persistence.xml"));		 
			
			appContext.refresh();
			appContext.start();
		} catch (BeanCreationException e) {
			// create new schema
			if (hbm2dll == HBM2DDL.VALIDATE) {
				logger.error("ApplicationContext could not be created. " +
					" Maybe your database schema is not up-to-date, " +
					" but there might be other BeanCreation problems too." +
					" Try to run CdmApplicationController with hbm2dll.CREATE or hbm2dll.UPDATE option. ");
			} else {
				logger.error("BeanCreationException (CdmApplicationController startet with " + hbm2dll.toString() + " option.");
			}
			e.printStackTrace();
			return false;
		}
		setApplicationContext(appContext);
		// load defined terms if necessary 
		//TODO not necessary any more
		if (testDefinedTermsAreMissing()){
			TermLoader termLoader = (TermLoader) appContext.getBean("termLoader");
			try {
				termLoader.loadAllDefaultTerms();
			} catch (FileNotFoundException fileNotFoundException) {
				logger.error("One or more DefinedTerm initialisation files could not be found");
				fileNotFoundException.printStackTrace();
				return false;
			} catch (NoDefinedTermClassException noDefinedTermClassException) {
				logger.error("NoDefinedTermClassException");
				noDefinedTermClassException.printStackTrace();
				return false;
			}
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
	public boolean changeDataSource(CdmPersistentDataSource dataSource) {
		logger.info("Change datasource to : " + dataSource);
		return setNewDataSource(dataSource, HBM2DDL.VALIDATE);
	}
	
	/**
	 * Changes the ApplicationContext to the new dataSource
	 * @param dataSource
	 */
	public boolean changeDataSource(CdmPersistentDataSource dataSource, HBM2DDL hbm2dll) {
		logger.info("Change datasource to : " + dataSource);
		return setNewDataSource(dataSource, hbm2dll);
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
		taxonService = (ITaxonService)applicationContext.getBean("taxonServiceImpl");
		nameService = (INameService)applicationContext.getBean("nameServiceImpl");
		referenceService = (IReferenceService)applicationContext.getBean("referenceServiceImpl");
		agentService = (IAgentService)applicationContext.getBean("agentServiceImpl");
		termService = (ITermService)applicationContext.getBean("termServiceImpl");
		databaseService = (IDatabaseService)applicationContext.getBean("databaseServiceHibernateImpl");
		databaseService.setApplicationController(this);
	}
	

	
	/* ******  Services *********/
	public final INameService getNameService(){
		return this.nameService;
	}

	public final ITaxonService getTaxonService(){
		return this.taxonService;
	}

	public final IReferenceService getReferenceService(){
		return this.referenceService;
	}
	
	public final IAgentService getAgentService(){
		return this.agentService;
	}
	
	public final IDatabaseService getDatabaseService(){
		return this.databaseService;
	}
	
	public final ITermService getTermService(){
		return this.termService;
	}
	
	/* **** flush ***********/
	public void flush() {
		SessionFactory sf = (SessionFactory)applicationContext.getBean("sessionFactory");
		sf.getCurrentSession().flush();
	}
		
		
	
}
