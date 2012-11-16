package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.statistics.Statistics;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsConfigurator;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;

@Service
@Transactional
public class StatisticsServiceImpl
		extends
		ServiceBase<IdentifiableEntity<?>, CdmEntityDaoBase<IdentifiableEntity<?>>>
		implements IStatisticsService {

	private static final Logger logger = Logger
			.getLogger(StatisticsServiceImpl.class);
	
	private StatisticsConfigurator configurator;
	
	private Statistics statistics;

	@Autowired
	private ITaxonDao taxonDao;

	@Autowired
	private IClassificationDao classificationDao;

	@Override
	@Transactional
	public Statistics getStatistics(StatisticsConfigurator configurator) {
		this.configurator=configurator;
		this.statistics=new Statistics();
		
		
//		return (Integer) count(Taxon.class);
		return this.statistics;
	}


	@Transactional
	public Integer countAll(StatisticsConfigurator configurator) {
		this.configurator=configurator;
		this.statistics=new Statistics();
		Integer i = taxonDao.count(Taxon.class);
		
		return i;
	
	}
	
	// @Override
	// protected void setDao(CdmEntityDaoBase<Taxon> dao) {
	// // we would not use this funktion yet, because we autowire
	// // several daos of different entities
	//
	// }

	@Override
	protected void setDao(CdmEntityDaoBase<IdentifiableEntity<?>> dao) {
		this.dao = dao;

	}

}
