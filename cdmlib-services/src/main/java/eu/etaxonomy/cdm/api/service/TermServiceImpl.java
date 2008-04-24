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
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;

@Service
@Transactional(readOnly = true)
public class TermServiceImpl extends ServiceBase<DefinedTermBase> implements ITermService{
	static Logger logger = Logger.getLogger(TermServiceImpl.class);
	
	@Autowired
	protected void setDao(IDefinedTermDao dao) {
		this.dao = dao;
	}
	
	public DefinedTermBase getTermByUri(String uri) {
		//FIXME transformation from URI to UUID
		return DefinedTermBase.findByUuid(UUID.fromString(uri));  
	}
	public DefinedTermBase getTermByUuid(UUID uuid) {
		return DefinedTermBase.findByUuid(uuid);  
	}

	public List<DefinedTermBase> listTerms() {
//		if (DefinedTermBase.isInitialized()){
//			logger.debug("listTerms by Map");
//			List<DefinedTermBase> result = new ArrayList<DefinedTermBase>();
//			result.addAll(DefinedTermBase.getDefinedTerms().values());
//			return result;
//		}else{
			//needed for initialization by DefinedTermBase
			logger.debug("listTerms by Init");
			return dao.list(100000, 0);
//		}
	}

	public List<DefinedTermBase> listTerms(UUID vocabularyUuid) {
		// TODO Auto-generated method stub
		logger.error("Method not implemented yet");
		return null;
	}

	public List<TermVocabulary> listVocabularies(Class termClass) {
		// TODO Auto-generated method stub
		logger.error("Method not implemented yet");
		return null;
	}

}
