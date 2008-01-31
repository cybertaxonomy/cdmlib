package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.*;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;


import java.util.List;
import java.util.UUID;


@Service
@Transactional(readOnly = true)
public class NameServiceImpl extends IdentifiableServiceBase<TaxonNameBase> implements INameService {
	static Logger logger = Logger.getLogger(NameServiceImpl.class);
	
	@Autowired
	protected void setDao(ITaxonNameDao dao) {
		this.dao = dao;
	}

	public NameServiceImpl(){
		logger.info("Load NameService Bean");
	}

	public List getNamesByName(String name){
		return super.findCdmObjectsByTitle(name);
	}

	public TaxonNameBase getTaxonNameByUuid(UUID uuid) {
		return super.getCdmObjectByUuid(uuid);
	}

	@Transactional(readOnly = false)
	public UUID saveTaxonName(TaxonNameBase taxonName) {
		return super.saveCdmObject(taxonName);
	}

	public List getAllNames(int limit, int start){
		return dao.list(limit, start);
	}

	public TermVocabulary getRankEnumeration() {
		// TODO Auto-generated method stub
		return null;
	}

}
