package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.*;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.strategy.BotanicNameCacheStrategy;

import java.util.List;


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
	

	public ReferenceBase getReferenceByUuid(String uuid) {
		return super.getCdmObjectByUuid(uuid); 
	}

	@Transactional(readOnly = false)
	public String saveReference(ReferenceBase reference) {
		return super.saveCdmObject(reference);
	}

}
