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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;


@Service
@Transactional(readOnly = true)
public class ReferenceServiceImpl extends IdentifiableServiceBase<ReferenceBase,IReferenceDao> implements IReferenceService {
	
	static Logger logger = Logger.getLogger(ReferenceServiceImpl.class);

	public ReferenceBase getReferenceByUuid(UUID uuid) {
		return super.getCdmObjectByUuid(uuid); 
	}

	public List<ReferenceBase> getReferencesByTitle(String title) {
		return super.findCdmObjectsByTitle(title);
	}
	
	public List<ReferenceBase> getReferencesByTitle(String title, Class<ReferenceBase> clazz) {
		return super.findCdmObjectsByTitle(title, clazz);
	}
	
	@Transactional(readOnly = false)
	public UUID saveReference(ReferenceBase reference) {
		return super.saveCdmObject(reference);
	}

	@Transactional(readOnly = false)
	public Map<UUID, ReferenceBase> saveReferenceAll(Collection<ReferenceBase> referenceCollection){
		return saveCdmObjectAll(referenceCollection);
	}

	public List<ReferenceBase> getAllReferences(int limit, int start){
			return dao.list(limit, start);
	}


	public void generateTitleCache() {
		logger.warn("Not yet implemented");
		// TODO Auto-generated method stub
		
	}

	@Autowired
	protected void setDao(IReferenceDao dao) {
		this.dao = dao;
	}


}
