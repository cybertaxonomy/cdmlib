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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;

@Service
@Transactional(readOnly = true)
public class ServiceImpl extends ServiceBase<CdmBase, ICdmEntityDao<CdmBase>> implements IService<CdmBase> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ServiceImpl.class);

	//@Autowired
	//@Qualifier("mainDao")
	protected ICdmEntityDao<CdmBase> mainDao;

	//@Autowired
	@Override
    protected void setDao(ICdmEntityDao<CdmBase> dao) {
		this.dao = dao;
	}

}
