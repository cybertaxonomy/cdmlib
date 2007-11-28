package eu.etaxonomy.cdm.api.service;

import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

public abstract class IdentifiableServiceBase<T extends IdentifiableEntity> 
						extends ServiceBase<T> 
						implements IIdentifiableEntityService<T>{
	static Logger logger = Logger.getLogger(IdentifiableServiceBase.class);

	protected List<T> findCdmObjectsByTitle(String title){
		return dao.find(title);
	}
}
