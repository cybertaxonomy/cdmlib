package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import eu.etaxonomy.cdm.model.name.*;
import eu.etaxonomy.cdm.persistence.dao.INonViralNameDao;
import eu.etaxonomy.cdm.strategy.BotanicNameCacheStrategy;

import java.util.List;



public class NameServiceImpl extends ServiceBase implements INameService {
	static Logger logger = Logger.getLogger(NameServiceImpl.class);
	
	private INonViralNameDao nonViralNameDao;
	
	/**
	 * @return the taxonNameDao
	 */
	public INonViralNameDao getTaxonNameDao() {
		return nonViralNameDao;
	}

	/**
	 * @param nonViralNameDao the taxonNameDao to set
	 */
	public void setTaxonNameDao(INonViralNameDao nonViralNameDao) {
		this.nonViralNameDao = nonViralNameDao;
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.INameService#getNewTaxonName()
	 */
	public NonViralName createNonViralName(Rank rank) {
		//TODO implement factory methods 
		NonViralName tn = (NonViralName) createCdmObject(NonViralName.class);
		tn.setRank(rank);
		return tn;
		//return new TaxonName(new BotanicNameCacheStrategy());
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.INameService#getTaxonNameById(java.lang.Integer)
	 */
	public NonViralName getNonViralNameById(Integer id) {
		NonViralName tn = nonViralNameDao.findById(id);
		if (tn != null) {
			logger.info("getTaxonNameById: UUID: " + tn.getUuid());
		}
		return tn;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.INameService#saveTaxonName(eu.etaxonomy.cdm.model.name.TaxonName)
	 */
	public int saveNonViralName(NonViralName taxonName) {
		nonViralNameDao.saveOrUpdate(taxonName);
		return taxonName.getId();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.INameService#getAllNames()
	 */
	public List getAllNames(){
		return nonViralNameDao.getAllNames();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.INameService#getNamesByName(java.lang.String)
	 */
	public List getNamesByNameString(String name){
		return nonViralNameDao.getNamesByName(name);
	}

}
