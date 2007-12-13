package eu.etaxonomy.cdm.api.service;

import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;

public abstract class IdentifiableServiceBase<T extends IdentifiableEntity> 
						extends ServiceBase<T> 
						implements IIdentifiableEntityService<T>{
	static Logger logger = Logger.getLogger(IdentifiableServiceBase.class);
//	protected IIdentifiableDao<T> dao;
//
//	protected void setEntityDao(IIdentifiableDao<T> dao){
//		this.dao=dao;
//	}

	protected List<T> findCdmObjectsByTitle(String title){
		return ((IIdentifiableDao)dao).findByTitle(title);
	}
}
