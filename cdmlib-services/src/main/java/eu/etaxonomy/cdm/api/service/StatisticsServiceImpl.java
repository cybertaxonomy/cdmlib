package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.statistics.Statistics;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsConfigurator;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsPartEnum;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsTypeEnum;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;

@Service
@Transactional
public class StatisticsServiceImpl implements IStatisticsService {

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
		this.configurator = configurator;
		this.statistics = new Statistics(configurator);
		calculateParts();

		// return (Integer) count(Taxon.class);
		return this.statistics;
	}

	private void calculateParts() {
		for (StatisticsPartEnum part : configurator.getPartList()) {
			switch (part) {
			case ALL:
				countAll();
				break;

			case CLASSIFICATION:
				// TODO
				break;
			}
		}

	}

	@Transactional
	private void countAll() {

		for (StatisticsTypeEnum type : configurator.getTypeList()) {
			Integer number = 0;
			switch (type) {
			case ACCEPTED_TAXA:
				break;
			case ALL_TAXA:
				number = taxonDao.count(Synonym.class);
			case SYNONYMS:
				break;

			case ALL_REFERENCES:

				break;
			case CLASSIFICATION:
				number = classificationDao.count(Classification.class);

				break;

			case TAXON_NAMES:
				break;

			case DESCRIPTIVE_SOURCE_REFERENCES:
				break;
			case NOMECLATURAL_REFERENCES:
				break;
			}
			statistics.addCount(type, number);

		}

	}

	// @Override
	// protected void setDao(IIdentifiableDao<Taxon> dao) {
	// this.dao = dao;
	//
	// }

}
