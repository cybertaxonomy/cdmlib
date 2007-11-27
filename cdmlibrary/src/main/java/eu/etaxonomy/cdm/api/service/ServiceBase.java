package eu.etaxonomy.cdm.api.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.persistence.dao.IDao;


public abstract class ServiceBase<T extends IdentifiableEntity> implements IIdentifiableEntityService<T>, ApplicationContextAware {
	static Logger logger = Logger.getLogger(ServiceBase.class);
	
	protected ApplicationContext appContext;
	protected IDao<T> dao;
	
	protected void setEntityDao(IDao<T> da){
		this.dao=da;
	}
	
	public void setApplicationContext(ApplicationContext appContext){
		this.appContext = appContext;
	}

	protected T getCdmObjectByUuid(String uuid){
		return dao.findByUuid(uuid);
	}

	protected String saveCdmObject(T cdmObj){
		return dao.saveOrUpdate(cdmObj);
	}

	protected List<T> findCdmObjectsByTitle(String title){
		return dao.find(title);
	}

	protected List<T> list(int limit, int start) {
		return dao.list(limit, start);
	}

}
