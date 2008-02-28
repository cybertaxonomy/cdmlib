package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;


public abstract class ServiceBase<T extends CdmBase> implements IService<T>, ApplicationContextAware {
	static Logger logger = Logger.getLogger(ServiceBase.class);
	
	protected ApplicationContext appContext;
	protected ICdmEntityDao<T> dao;
	
	protected void setEntityDao(ICdmEntityDao<T> dao){
		this.dao=dao;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.Iyyy#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext appContext){
		this.appContext = appContext;
	}

	protected T getCdmObjectByUuid(UUID uuid){
		return dao.findByUuid(uuid);
	}

	@Transactional(readOnly = false)
	protected UUID saveCdmObject(T cdmObj){
		if (logger.isDebugEnabled()){logger.debug("Save cdmObj: " + (cdmObj == null? null: cdmObj.toString()));}
		return dao.saveOrUpdate(cdmObj);
	}

	@Transactional(readOnly = false)
	protected Map<UUID, T> saveCdmObjectAll(Collection<T> cdmObjCollection){
		Map<UUID, T> resultMap = new HashMap<UUID, T>();
		Iterator<T> iterator = cdmObjCollection.iterator();
		while(iterator.hasNext()){
			T cdmObj = iterator.next();
			UUID uuid = saveCdmObject(cdmObj);
			if (logger.isDebugEnabled()){logger.debug("Save cdmObj: " + (cdmObj == null? null: cdmObj.toString()));}
			resultMap.put(uuid, cdmObj);
		}
		return resultMap;
	}
	
	@Transactional(readOnly = false)
	protected UUID removeCdmObject(T cdmObj){
		if (logger.isDebugEnabled()){logger.debug("Save cdmObj: " + (cdmObj == null? null: cdmObj.toString()));}
		return dao.delete(cdmObj);
	}
	
	protected List<T> list(int limit, int start) {
		return dao.list(limit, start);
	}

}
