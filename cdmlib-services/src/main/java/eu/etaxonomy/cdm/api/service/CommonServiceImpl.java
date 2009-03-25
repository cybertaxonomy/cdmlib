// $Id$
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.dao.common.IOriginalSourceDao;


@Service
@Transactional(readOnly = true)
public class CommonServiceImpl extends ServiceBase<OriginalSource,IOriginalSourceDao> implements ICommonService {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CommonServiceImpl.class);
	
	@Autowired
	IOriginalSourceDao originalSourceDao;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ICommonService#getSourcedObjectById(java.lang.String, java.lang.String)
	 */
	public ISourceable getSourcedObjectByIdInSource(Class clazz, String idInSource, String idNamespace) {
		ISourceable result = null;
//		List<OriginalSource> originalSource = originalSourceDao.findOriginalSourceByIdInSource(idInSource, idNamespace);
//		if (! originalSource.isEmpty()){
//			result = originalSource.get(0).getSourcedObj();
//		}
		List<IdentifiableEntity> list = originalSourceDao.findOriginalSourceByIdInSource(clazz, idInSource, idNamespace);
		if (! list.isEmpty()){
			result = list.get(0);
		}return result;
	}

	@Autowired
	protected void setDao(IOriginalSourceDao dao) {
		this.dao = dao;
	}
	
}
