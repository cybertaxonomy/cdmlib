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
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;


@Service
@Transactional(readOnly = true)
public class ReferenceServiceImpl<T extends ReferenceBase> extends IdentifiableServiceBase<T> implements IReferenceService<T> {
//public class ReferenceServiceImpl extends ServiceBase<ReferenceBase> implements IReferenceService {
	static Logger logger = Logger.getLogger(ReferenceServiceImpl.class);
	
	private IReferenceDao referenceDao;
	
	@Autowired
	protected void setDao(IReferenceDao dao) {
		this.dao = (ICdmEntityDao)dao;
		this.referenceDao = dao;
	}

	public ReferenceBase getReferenceByUuid(UUID uuid) {
		return super.getCdmObjectByUuid(uuid); 
	}

	public List<T> getReferencesByTitle(String title) {
		return super.findCdmObjectsByTitle(title);
	}
	
	public List<T> getReferencesByTitle(String title, Class<T> clazz) {
		return super.findCdmObjectsByTitle(title, clazz);
	}
	
	@Transactional(readOnly = false)
	public UUID saveReference(T reference) {
		return super.saveCdmObject(reference);
	}

	@Transactional(readOnly = false)
	public Map<UUID, T> saveReferenceAll(Collection<T> referenceCollection){
		return saveCdmObjectAll(referenceCollection);
	}

	public List<ReferenceBase> getAllReferences(int limit, int start){
			return referenceDao.list(limit, start);
	}


	public void generateTitleCache() {
		// TODO Auto-generated method stub
		
	}


}
