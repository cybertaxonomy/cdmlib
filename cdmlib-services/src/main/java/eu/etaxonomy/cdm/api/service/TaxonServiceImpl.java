package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
@Transactional(readOnly = true)
public class TaxonServiceImpl extends ServiceBase<TaxonBase> implements ITaxonService {
	static Logger logger = Logger.getLogger(TaxonServiceImpl.class);
	
	private ITaxonDao taxonDao;
	
	@Autowired
	protected void setDao(ITaxonDao dao) {
		this.dao = dao;
		this.taxonDao = dao;
	}
	

	public TaxonBase getTaxonByUuid(UUID uuid) {
		return super.getCdmObjectByUuid(uuid); 
	}

	@Transactional(readOnly = false)
	public UUID saveTaxon(TaxonBase taxon) {
		return super.saveCdmObject(taxon);
	}
	
	@Transactional(readOnly = false)
	public Map<UUID, TaxonBase> saveTaxonAll(Collection<TaxonBase> taxonCollection){
		return saveCdmObjectAll(taxonCollection);
	}

	

	@Transactional(readOnly = false)
	public UUID removeTaxon(TaxonBase taxon) {
		return super.removeCdmObject(taxon);
	}

	public List<TaxonBase> searchTaxaByName(String name, ReferenceBase sec) {
		return taxonDao.getTaxaByName(name, sec);
	}

	public List<Taxon> getRootTaxa(ReferenceBase sec) {
		return taxonDao.getRootTaxa(sec);
	}


	public void generateTitleCache() {
		generateTitleCache(true);
	}
	public void generateTitleCache(boolean forceProtected) {
		for (TaxonBase tb : taxonDao.getAllTaxa(null,null)){
			logger.warn("Old taxon title: " + tb.getTitleCache());
			if (forceProtected || !tb.isProtectedTitleCache() ){
				tb.setTitleCache(tb.generateTitle(), false);
				taxonDao.update(tb);
				logger.warn("New title: " + tb.getTitleCache());
			}
		}
		
	}
}
