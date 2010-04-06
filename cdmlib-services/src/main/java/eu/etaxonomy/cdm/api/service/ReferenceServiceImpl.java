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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;


@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class ReferenceServiceImpl extends IdentifiableServiceBase<ReferenceBase,IReferenceDao> implements IReferenceService {
	
	static Logger logger = Logger.getLogger(ReferenceServiceImpl.class);

	/**
	 * Constructor
	 */
	public ReferenceServiceImpl(){
		if (logger.isDebugEnabled()) { logger.debug("Load ReferenceService Bean"); }
	}

	public void generateTitleCache() {
		logger.warn("Not yet implemented");
		// TODO Auto-generated method stub
		
	}

	@Autowired
	protected void setDao(IReferenceDao dao) {
		this.dao = dao;
	}

	public List<UuidAndTitleCache<ReferenceBase>> getUuidAndTitle() {
		
		return dao.getUuidAndTitle();
	}
	
	public List<ReferenceBase> getAllReferencesForPublishing(){
		return dao.getAllNotNomenclaturalReferencesForPublishing();
	}

	public List<ReferenceBase> getAllNomenclaturalReferences() {
		
		return dao.getAllNomenclaturalReferences();
	}

}
