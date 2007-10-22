package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import eu.etaxonomy.cdm.model.common.VersionableEntity;


public abstract class ServiceBase implements IService, ApplicationContextAware {
	static Logger logger = Logger.getLogger(ServiceBase.class);
	
	protected ApplicationContext appContext;
	
	public void setApplicationContext(ApplicationContext appContext){
		this.appContext = appContext;
	}
	
	protected VersionableEntity createCdmObject (Class clazz){
		String beanId = "proto"+clazz.getSimpleName();
		VersionableEntity ve = (VersionableEntity)this.appContext.getBean(beanId);
		return ve;		
	}
	
//	static final XmlBeanFactory factory() {
//		String fileName = "cdmSpringConfig.xml";
//		ClassPathResource cpr = new ClassPathResource(fileName);
//		XmlBeanFactory  bf = new XmlBeanFactory(cpr);
//		
//		return bf;
//	};
//	
//	static final ApplicationContext appContext() {
//		String fileName = "cdmSpringConfig.xml";
//		ClassPathResource cpr = new ClassPathResource(fileName);
//		ApplicationContext ac =
//		      new ClassPathXmlApplicationContext( fileName );
//		return ac;
//	};
	
}
