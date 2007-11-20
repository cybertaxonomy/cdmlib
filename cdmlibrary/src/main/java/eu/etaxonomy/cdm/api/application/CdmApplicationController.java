package eu.etaxonomy.cdm.api.application;


import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hsqldb.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.INameService;

/**
 * @author a.mueller
 *
 */
public class CdmApplicationController {
	private static final Logger logger = Logger.getLogger(CdmApplicationController.class);
	
	private ClassPathXmlApplicationContext applicationContext;
	@Autowired
	private INameService nameService;
	@Autowired
	private IAgentService agentService;
	@Autowired
	private IDatabaseService databaseService;
	
	private Server hsqldbServer;
	
	
	/* Constructor */
	public CdmApplicationController() {
		//logger.info("Start HSQLDB Server");
		//startHsqldbServer();
		logger.info("Start CdmApplicationController");
		String fileName = "applicationContext.xml";
		setApplicationContext(new ClassPathXmlApplicationContext(fileName));
		
		//TODO find out if DataSource is localHsqldb,
		//if yes then find out if Server is running
		//if not running, start server
	}
	
	public void finalize(){
		close();
	}
	
	public void close(){
		if (applicationContext != null)
			logger.info("Close ApplicationContext");
			applicationContext.close();
	}
	
	
	public void setApplicationContext(ClassPathXmlApplicationContext appCtx){
		applicationContext = appCtx;
		applicationContext.registerShutdownHook();
		setServices();
	}
	
	private void setServices(){
		//TODO ? also possible via SPRING?
		nameService = (INameService)applicationContext.getBean("nameServiceImpl");
		agentService = (IAgentService)applicationContext.getBean("agentServiceImpl");
		databaseService = (IDatabaseService)applicationContext.getBean("databaseServiceHibernateImpl");
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
	
	public void flush() {
		SessionFactory sf = (SessionFactory)applicationContext.getBean("sessionFactory");
		sf.getCurrentSession().flush();
	}
		
		
	
}
