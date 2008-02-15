package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;

import java.util.List;
import java.util.UUID;


@Service
@Transactional(readOnly = true)
public class ReferenceServiceImpl extends ServiceBase<ReferenceBase> implements IReferenceService {
	static Logger logger = Logger.getLogger(ReferenceServiceImpl.class);
	
	private IReferenceDao referenceDao;
	
	@Autowired
	protected void setDao(IReferenceDao dao) {
		this.dao = dao;
		this.referenceDao = dao;
	}
	

	public ReferenceBase getReferenceByUuid(UUID uuid) {
		return super.getCdmObjectByUuid(uuid); 
	}

	@Transactional(readOnly = false)
	public UUID saveReference(ReferenceBase reference) {
		return super.saveCdmObject(reference);
	}
	
	public List<ReferenceBase> getAllReferences(int limit, int start){
			return referenceDao.list(limit, start);
	}


}
