package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.*;
import eu.etaxonomy.cdm.persistence.dao.common.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.strategy.BotanicNameCacheStrategy;

import java.util.List;


@Service
public class NameServiceImpl extends IdentifiableServiceBase<TaxonNameBase> implements INameService {
	static Logger logger = Logger.getLogger(NameServiceImpl.class);
	
	@Autowired
	protected void setDao(ITaxonNameDao dao) {
		this.dao = dao;
	}


	public List getNamesByName(String name){
		return super.findCdmObjectsByTitle(name);
	}

	public TaxonNameBase getTaxonNameByUuid(String uuid) {
		return super.getCdmObjectByUuid(uuid);
	}

	public String saveTaxonName(TaxonNameBase taxonName) {
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
