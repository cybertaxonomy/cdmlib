package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.statistics.Statistics;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsConfigurator;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsPartEnum;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsTypeEnum;
import eu.etaxonomy.cdm.common.TreeNode;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
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
	
	private static final StatisticsTypeEnum DEFAULT_TYPE=StatisticsTypeEnum.ALL_TAXA; //TODO create a list with all types.
	private static final StatisticsPartEnum DEFAULT_FILTER=StatisticsPartEnum.ALL;

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

		statisticsList = new ArrayList<Statistics>();

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
		// TODO constant for null filter
		if (filter == null) {
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
				break;

			case NOMECLATURAL_REFERENCES:

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
					// so do nothing
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
					counter = statisticsDao.countReferencesInClassification((Classification) filter);
					break;
				case DESCRIPTIVE_SOURCE_REFERENCES:
					counter = statisticsDao
							.countDescriptiveSourceReferences((Classification) filter);
					break;
				case NOMECLATURAL_REFERENCES:
					counter = statisticsDao
							.countNomenclaturalReferences((Classification) filter);
					break;

				}

				statistics.addCount(type, counter);
			}
		} else if(filter instanceof Taxon) {
			//get all taxa of the tree:
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
