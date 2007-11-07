package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import eu.etaxonomy.cdm.model.name.*;
import eu.etaxonomy.cdm.persistence.dao.ITaxonNameDao;
import eu.etaxonomy.cdm.strategy.BotanicNameCacheStrategy;

import java.util.List;



public class NameServiceImpl extends ServiceBase implements INameService {
	static Logger logger = Logger.getLogger(NameServiceImpl.class);
	
	private ITaxonNameDao taxonNameDao;
	
	/**
	 * @return the taxonNameDao
	 */
	public ITaxonNameDao getTaxonNameDao() {
		return taxonNameDao;
	}

	/**
	 * @param taxonNameDao the taxonNameDao to set
	 */
	public void setTaxonNameDao(ITaxonNameDao taxonNameDao) {
		this.taxonNameDao = taxonNameDao;
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.INameService#getNewTaxonName()
	 */
	public TaxonName createTaxonName(Rank rank) {
		//TODO implement factory methods 
		TaxonName tn = (TaxonName) createCdmObject(TaxonName.class);
		tn.setRank(rank);
		return tn;
		//return new TaxonName(new BotanicNameCacheStrategy());
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.INameService#getTaxonNameById(java.lang.Integer)
	 */
	public TaxonName getTaxonNameById(Integer id){
		TaxonName tn = taxonNameDao.findById(id);
		if (tn != null) {
			logger.info("getTaxonNameById: UUID: " + tn.getUuid());
		}
		return tn;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.INameService#saveTaxonName(eu.etaxonomy.cdm.model.name.TaxonName)
	 */
	public int saveTaxonName(TaxonName taxonName){
		taxonNameDao.saveOrUpdate(taxonName);
		return taxonName.getId();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.INameService#getAllNames()
	 */
	public List getAllNames(){
		return taxonNameDao.getAllNames();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.INameService#getNamesByName(java.lang.String)
	 */
	public List getNamesByNameString(String name){
		return taxonNameDao.getNamesByName(name);
	}
	
}
