package eu.etaxonomy.cdm.api.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;


public abstract class ServiceBase<T extends CdmBase> implements IService<T>, ApplicationContextAware {
	static Logger logger = Logger.getLogger(ServiceBase.class);
	
	protected ApplicationContext appContext;
	protected ICdmEntityDao<T> dao;
	
	protected void setEntityDao(ICdmEntityDao<T> da){
		this.dao=da;
	}
	
	public void setApplicationContext(ApplicationContext appContext){
		this.appContext = appContext;
	}

	protected T getCdmObjectByUuid(String uuid){
		return dao.findByUuid(uuid);
	}

	@Transactional(readOnly = false)
	protected String saveCdmObject(T cdmObj){
		return dao.saveOrUpdate(cdmObj);
	}

	protected List<T> list(int limit, int start) {
		return dao.list(limit, start);
	}

}
