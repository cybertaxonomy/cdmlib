/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;

public abstract class IdentifiableServiceBase<T extends IdentifiableEntity> extends ServiceBase<T> 
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
