package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.statistics.Statistics;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsConfigurator;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsTypeEnum;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.statistics.IStatisticsDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;

/**
 *
 * @author s.buers Service to provide statistic data of the database elements
 */

@Service
@Transactional
public class StatisticsServiceImpl implements IStatisticsService {

	private static final Logger logger = Logger
			.getLogger(StatisticsServiceImpl.class);



	// this does not make sense, we just count nothing if no type is given.
	// the one who calls this service should check that there are types given!
//	private static final StatisticsTypeEnum DEFAULT_TYPE=StatisticsTypeEnum.ALL_TAXA;
	//TODO create a list with all types.

	// this constant can also be used by the ones that use the service

	private static final IdentifiableEntity<?> ALL_DB = null;

	@Override
	@Transactional
	public IdentifiableEntity<?> getFilterALL_DB(){
		return ALL_DB;
	}

	private ArrayList<Statistics> statisticsList;

	@Autowired
	private ITaxonDao taxonDao;

	@Autowired
	private ITaxonNameDao taxonNameDao;

	@Autowired
	private IClassificationDao classificationDao;

	@Autowired
	private IReferenceDao referenceDao;

	@Autowired
	private IStatisticsDao statisticsDao;

	@Autowired
	private IDescriptionDao descriptionDao;

	/**
	 * counts all the elements referenced in the configurator from the part of
	 * the database referenced in the configurator
	 *
	 * @param configurators
	 * @return be aware that a Statistics.countMap might contain "null"
	 *         {@link Number} values, if the count failed (, if the value is "0"
	 *         the count succeeded in counting zero elements.)
	 */
	@Override
	@Transactional
	public List<Statistics> getCountStatistics(
			List<StatisticsConfigurator> configurators) {

		statisticsList = new ArrayList<>();

		for (StatisticsConfigurator statisticsConfigurator : configurators) {
			// create a Statistics element for each configurator
			countStatisticsPart(statisticsConfigurator);
		}
		return this.statisticsList;
	}

	@Transactional
	private void countStatisticsPart(StatisticsConfigurator configurator) {
		// get last element of configurator.filter (the node that is the root
		// for the count):
		IdentifiableEntity filter = configurator.getFilter().get(
				(configurator.getFilter().size()) - 1);
		if (filter == getFilterALL_DB()) {
			countAll(configurator);
		} else { // check for classtype classification
			countPart(configurator, filter);
		}

	}

	/**
	 * @param configurator
	 */
	private void countAll(StatisticsConfigurator configurator) {
		Statistics statistics = new Statistics(configurator);

		for (StatisticsTypeEnum type : configurator.getType()) {
			Long counter = null;
			switch (type) {

			case ALL_TAXA:
				counter = Long.valueOf(taxonDao.count(TaxonBase.class));
				break;
			case SYNONYMS:
				counter = Long.valueOf(taxonDao.count(Synonym.class));
				break;
			case ACCEPTED_TAXA:
				counter = Long.valueOf(taxonDao.count(Taxon.class));
				break;
			case ALL_REFERENCES:
				counter = Long
						.valueOf(referenceDao
								.count(eu.etaxonomy.cdm.model.reference.Reference.class));
				counter -=statisticsDao.countNomenclaturalReferences();
				break;

			case NOMENCLATURAL_REFERENCES:

				counter = statisticsDao.countNomenclaturalReferences();
				break;

			case CLASSIFICATION:
				counter = Long.valueOf(classificationDao
						.count(Classification.class));

				break;

			case TAXON_NAMES:
				counter = Long.valueOf(taxonNameDao.count(TaxonNameBase.class));
				break;

			case DESCRIPTIVE_SOURCE_REFERENCES:

				counter = statisticsDao.countDescriptiveSourceReferences();

				break;
			case DESCRIPTIONS:

				counter = Long.valueOf(descriptionDao.count(DescriptionBase.class));

				break;
			}

			statistics.addCount(type, counter);
		}
		statisticsList.add(statistics);
	}

	@Transactional
	private void countPart(StatisticsConfigurator configurator,
			IdentifiableEntity filter) {
		// TODO maybe remove redundant parameter filter
		Statistics statistics = new Statistics(configurator);

		Long counter = null;

		if (filter instanceof Classification) {

			for (StatisticsTypeEnum type : configurator.getType()) {

				switch (type) {
				case CLASSIFICATION:
					logger.info("there should not be any classification "
							+ "nested in an other classification");
					// so we set counter to 1, as a classification itself is one classification
					counter = new Long(1);
					break;
				case ACCEPTED_TAXA:
					counter = statisticsDao.countTaxaInClassification(
							Taxon.class, (Classification) filter);
					break;

				case ALL_TAXA:
					counter = statisticsDao.countTaxaInClassification(
							TaxonBase.class, (Classification) filter);
					break;
				case SYNONYMS:
					counter = statisticsDao.countTaxaInClassification(
							Synonym.class, (Classification) filter);
					break;
				case TAXON_NAMES:
					counter = statisticsDao
							.countTaxonNames((Classification) filter);
					break;
				case ALL_REFERENCES:
//					counter = statisticsDao.countReferencesInClassification((Classification) filter);
					counter = statisticsDao.countReferencesInClassificationWithUuids((Classification) filter);
					counter+=statisticsDao
							.countDescriptive(true, (Classification) filter);
					break;
				case DESCRIPTIVE_SOURCE_REFERENCES:
					counter = statisticsDao
							.countDescriptive(true, (Classification) filter);
					break;
				case DESCRIPTIONS:
					counter = statisticsDao
							.countDescriptive(false, (Classification) filter);
					break;
				case NOMENCLATURAL_REFERENCES:
					counter = statisticsDao
							.countNomenclaturalReferences((Classification) filter);
					break;

				}

				statistics.addCount(type, counter);
			}
		} else if(filter instanceof Taxon) {
			//TODO get all taxa of the tree:
			do{
				filter.getUuid();
				statisticsDao.getTaxonTree(filter);
			}while(true);
		}else {
			// we just return null as count for the statistics
			// element, if the filter is neither classification nor null.
		}

		statisticsList.add(statistics);
	}
}
