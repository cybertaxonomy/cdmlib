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

import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.persistence.dao.collection.ICollectionDao;
import java.util.List;
import java.util.UUID;


@Service
@Transactional(readOnly = true)
public class CollectionServiceImpl extends ServiceBase<Collection> implements ICollectionService {
	static Logger logger = Logger.getLogger(CollectionServiceImpl.class);
	
	private ICollectionDao collectionDao;
	
	@Autowired
	protected void setDao(ICollectionDao dao) {
		this.dao = dao;
		this.collectionDao = dao;
	}

	public Collection getCollectionByUuid(UUID uuid) {
		return super.getCdmObjectByUuid(uuid); 
	}

	public List<Collection> searchCollectionByCode(String code) {
		return collectionDao.getCollectionByCode(code);
	}



	

	public void generateTitleCache() {
		generateTitleCache(true);
	}
	//TODO
	public void generateTitleCache(boolean forceProtected) {
		logger.warn("generateTitleCache not yet fully implemented!");
//		for (Collection tb : taxonDao.getAllTaxa(null,null)){
//			logger.warn("Old taxon title: " + tb.getTitleCache());
//			if (forceProtected || !tb.isProtectedTitleCache() ){
//				tb.setTitleCache(tb.generateTitle(), false);
//				taxonDao.update(tb);
//				logger.warn("New title: " + tb.getTitleCache());
//			}
//		}
		
	}
}
