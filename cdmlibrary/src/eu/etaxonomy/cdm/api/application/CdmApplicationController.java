package eu.etaxonomy.cdm.api.application;

import java.sql.Connection;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.impl.SessionFactoryImpl;
import org.hsqldb.Server;
import org.springframework.beans.BeansException;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate3.AbstractSessionFactoryBean;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.util.PathMatcher;

import eu.etaxonomy.cdm.api.service.AgentServiceImpl;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.NameServiceImpl;

/**
 * @author a.mueller
 *
 */
public class CdmApplicationController {
	private static final Logger logger = Logger.getLogger(CdmApplicationController.class);
	
	private ClassPathXmlApplicationContext applicationContext;
	private INameService nameService;
	private IAgentService agentService;
	private Server hsqldbServer;
	
	
	/* Constructor */
	public CdmApplicationController() {
		//logger.info("Start HSQLDB Server");
		//startHsqldbServer();
		logger.warn("Start CdmApplicationController");
		String fileName = "editCdm.spring.cfg.xml";
		setApplicationContext(new ClassPathXmlApplicationContext(fileName));
		Object sf = (Object)applicationContext.containsBean("sessionFactory");
		SessionFactory sf1 = (SessionFactory)applicationContext.getBean("sessionFactory");
		
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
		nameService = (INameService)applicationContext.getBean("nameService");
		agentService = (IAgentService)applicationContext.getBean("agentService");
	}
	
	/* Services */
	public final INameService getNameService(){
		return nameService;
	}
	
	public final IAgentService getAgentService(){
		return agentService;
	}

	
}
