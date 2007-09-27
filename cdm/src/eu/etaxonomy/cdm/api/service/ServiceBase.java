package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import eu.etaxonomy.cdm.control.SpringControl;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.persistence.dao.ITaxonNameDao;


public abstract class ServiceBase implements IService, ApplicationContextAware {
	static Logger logger = Logger.getLogger(ServiceBase.class);
	
	protected ApplicationContext appContext;
	
	public void setApplicationContext(ApplicationContext appContext){
		this.appContext = appContext;
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
