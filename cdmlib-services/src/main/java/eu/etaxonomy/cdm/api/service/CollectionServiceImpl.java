/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.persistence.dao.occurrence.ICollectionDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Service
@Transactional(readOnly = true)
public class CollectionServiceImpl extends	IdentifiableServiceBase<Collection, ICollectionDao> implements	ICollectionService {
	
	static private final Logger logger = Logger.getLogger(CollectionServiceImpl.class);

    @Autowired
	@Override
	protected void setDao(ICollectionDao dao) {
		this.dao = dao;
	}

	public void generateTitleCache() {
		logger.warn("Not yet implemented");
	}
	
	public List<Collection> searchByCode(String code) {
		return this.dao.getCollectionByCode(code);
	}
}
