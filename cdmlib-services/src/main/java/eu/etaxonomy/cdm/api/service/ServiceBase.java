/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;


public abstract class ServiceBase<T extends CdmBase> implements IService<T>, ApplicationContextAware {
	static Logger logger = Logger.getLogger(ServiceBase.class);
	
	//flush after saving this number of objects
	int flushAfterNo = 2000;
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
		int types = cdmObjCollection.getClass().getTypeParameters().length;
		if (types > 0){
			if (logger.isDebugEnabled()){logger.debug("ClassType: + " + cdmObjCollection.getClass().getTypeParameters()[0]);}
		}
		
		Map<UUID, T> resultMap = new HashMap<UUID, T>();
		Iterator<T> iterator = cdmObjCollection.iterator();
		int i = 0;
			while(iterator.hasNext()){
				if ( ( (i % 5000) == 0) && (i > 0)   ){logger.debug("Saved " + i + " objects" );}
				T cdmObj = iterator.next();
				UUID uuid = saveCdmObject(cdmObj);
				if (logger.isDebugEnabled()){logger.debug("Save cdmObj: " + (cdmObj == null? null: cdmObj.toString()));}
				resultMap.put(uuid, cdmObj);
				i++;
				if ( (i % flushAfterNo) == 0){
					try{
									logger.debug("flush");
					dao.flush();
					}catch(Exception e){
						logger.error("UUUIIIII");
						e.printStackTrace();
					}
				}
			}

		if ( logger.isInfoEnabled() ){logger.info("Saved " + i + " objects" );}
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
