package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.*;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.IDao;
import eu.etaxonomy.cdm.persistence.dao.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.ITaxonNameDao;
import eu.etaxonomy.cdm.strategy.BotanicNameCacheStrategy;

import java.util.List;


@Service
public class TaxonServiceImpl extends ServiceBase<TaxonBase> implements ITaxonService {
	static Logger logger = Logger.getLogger(TaxonServiceImpl.class);
	
	@Autowired
	private ITaxonDao dao;

	public TaxonBase getTaxonByUuid(String uuid) {
		return super.getCdmObjectByUuid(uuid); 
	}

	public String saveTaxon(TaxonBase taxon) {
		return super.saveCdmObject(taxon);
	}

	public List<TaxonBase> searchTaxaByName(String name, ReferenceBase sec) {
		return dao.getTaxaByName(name, sec);
	}

	public List<Taxon> getRootTaxa(ReferenceBase sec) {
		return dao.getRootTaxa(sec);
	}
}
