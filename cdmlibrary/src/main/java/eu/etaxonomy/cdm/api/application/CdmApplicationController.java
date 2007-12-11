package eu.etaxonomy.cdm.api.application;

import java.io.FileNotFoundException;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.engine.loading.LoadContexts;
import org.hibernate.impl.SessionFactoryImpl;
import org.hsqldb.Server;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.init.TermLoader;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.NoDefinedTermClassException;


/**
 * @author a.mueller
 *
 */
public class CdmApplicationController {
	private static final Logger logger = Logger.getLogger(CdmApplicationController.class);
	
	public AbstractApplicationContext applicationContext;
	private INameService nameService;
	private IAgentService agentService;
	private IDatabaseService databaseService;
	private ITermService termService;
	
	
	private Server hsqldbServer;
	
	
	/**
	 * Constructor, opens an spring 2.5 ApplicationContext by using the default data source
	 * @param dataSource
	 */
	public CdmApplicationController() {
		//logger.info("Start HSQLDB Server");
		//startHsqldbServer();
		
		//TODO find out if DataSource is localHsqldb,
		//if yes then find out if Server is running
		//if not running, start server

		logger.info("Start CdmApplicationController with default data source");
		CdmDataSource dataSource = CdmDataSource.NewDefaultInstance();
		setNewDataSource(dataSource);
	}
	
	/**
	 * Constructor, opens an spring 2.5 ApplicationContext by using the according data source
	 * @param dataSource
	 */
	public CdmApplicationController(CdmDataSource dataSource) 
			throws DataSourceNotFoundException{
		logger.info("Start CdmApplicationController with datasource: " + dataSource);
		if (setNewDataSource(dataSource) == false){
			throw new DataSourceNotFoundException("Wrong datasource: " + dataSource );
		}
	}

	
	/**
	 * Sets the application context to a new spring ApplicationContext by using the according data source and initializes the Controller.
	 * @param dataSource
	 */
	private boolean setNewDataSource(CdmDataSource dataSource) {
		dataSource.updateSessionFactory(null);
		FileSystemXmlApplicationContext appContext;
		try {
			logger.debug("Start spring-2.5 ApplicationContex with hibernate.hbm2ddl.auto default property");
			appContext = new FileSystemXmlApplicationContext(CdmUtils.getApplicationContextString());
		} catch (BeanCreationException e) {
			logger.warn("Database schema not up-to-date. Schema must be updated. All DefindeTerms are deleted and created new!");
			logger.debug("Start spring-2.5 ApplicationContex with hibernate.hbm2ddl.auto 'CREATE' property");
			dataSource.updateSessionFactory("create"); 
			appContext = new FileSystemXmlApplicationContext(CdmUtils.getApplicationContextString());
			TermLoader termLoader = (TermLoader) appContext.getBean("termLoader");
			try {
				termLoader.loadAllDefaultTerms();
			} catch (FileNotFoundException fileNotFoundException) {
				logger.error("One or more DefinedTerm initialisation files could not be found");
				fileNotFoundException.printStackTrace();
			} catch (NoDefinedTermClassException noDefinedTermClassException) {
				logger.error("NoDefinedTermClassException");
				noDefinedTermClassException.printStackTrace();
			}
		}
		setApplicationContext(appContext);
		return true;
	}

	/**
	 * Changes the ApplicationContext to the new dataSource
	 * @param dataSource
	 */
	public boolean changeDataSource(CdmDataSource dataSource) {
		logger.info("Change datasource to : " + dataSource);
		return setNewDataSource(dataSource);
	}
	
	/**
	 * Sets a new application Context.
	 * @param ac
	 */
	public void setApplicationContext(AbstractXmlApplicationContext ac){
		closeApplicationContext(); //closes old application context if necessary
		applicationContext = ac;
		applicationContext.registerShutdownHook();
		setServices();
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
	
	private void setServices(){
		//TODO ? also possible via SPRING?
		nameService = (INameService)applicationContext.getBean("nameServiceImpl");
		agentService = (IAgentService)applicationContext.getBean("agentServiceImpl");
		termService = (ITermService)applicationContext.getBean("termServiceImpl");
		databaseService = (IDatabaseService)applicationContext.getBean("databaseServiceHibernateImpl");
		databaseService.setApplicationController(this);
	}
	

	
	/* ******  Services *********/
	public final INameService getNameService(){
		return this.nameService;
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
