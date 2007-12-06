package eu.etaxonomy.cdm.api.application;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.impl.SessionFactoryImpl;
import org.hsqldb.Server;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;


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
		dataSource.updateSessionFactory(); 
		FileSystemXmlApplicationContext ac = new FileSystemXmlApplicationContext(CdmUtils.getApplicationContextString());
		setApplicationContext(ac);
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
	
	
	/* **** flush ***********/
	public void flush() {
		SessionFactory sf = (SessionFactory)applicationContext.getBean("sessionFactory");
		sf.getCurrentSession().flush();
	}
		
		
	
}
