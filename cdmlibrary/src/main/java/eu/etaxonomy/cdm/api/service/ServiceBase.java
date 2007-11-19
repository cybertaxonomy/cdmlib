package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import eu.etaxonomy.cdm.model.common.VersionableEntity;


public abstract class ServiceBase implements IService, ApplicationContextAware {
	static Logger logger = Logger.getLogger(ServiceBase.class);
	
	protected ApplicationContext appContext;
	
	public void setApplicationContext(ApplicationContext appContext){
		this.appContext = appContext;
	}
	
}
