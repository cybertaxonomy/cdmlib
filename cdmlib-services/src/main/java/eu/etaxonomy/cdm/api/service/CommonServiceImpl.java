/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IOriginalSource;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.persistence.dao.common.IOriginalSourceDao;


@Service
@Transactional(readOnly = true)
public class CommonServiceImpl extends ServiceBase<CdmBase> implements ICommonService {
	private static final Logger logger = Logger.getLogger(CommonServiceImpl.class);
	
	@Autowired
	IOriginalSourceDao originalSourceDao;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ICommonService#getSourcedObjectById(java.lang.String, java.lang.String)
	 */
	public IOriginalSource getSourcedObjectByIdInSource(String idInSource, String idNamespace) {
		IOriginalSource result = null;
		OriginalSource originalSource = originalSourceDao.findOriginalSourceByIdInSource(idInSource, idNamespace);
		if (originalSource!= null){
			result = originalSource.getSourcedObj();
		}
		return result;
	}

	
}
