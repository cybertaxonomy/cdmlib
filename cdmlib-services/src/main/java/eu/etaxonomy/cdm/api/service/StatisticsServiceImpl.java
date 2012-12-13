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

	private static final List<String> DESCRIPTION_SOURCE_REF_STRATEGIE = Arrays
			.asList(new String[] { "sources.citation" });
	// "descriptionSources", "citation"

	private static final List<String> DESCR_ELEMENT_REF_STRATEGIE = Arrays
			.asList(new String[] { "sources.citation", });;

	private List<StatisticsConfigurator> configurators;

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
	private IDescriptionDao descriptionDao;

	@Autowired
	private IDescriptionElementDao descrElementDao;

	@Autowired
	private IStatisticsDao statisticsDao;

	/**
	 * counts all the elements referenced in the configurator from the part of
	 * the database referenced in the configurator
	 * 
	 * @param configurators
	 * @return be aware that a Statistics.countMap might contain "null" for
	 *         {@link Number} value, if the count failed (, if the value is "0"
	 *         the count succeeded and sum is 0)
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
		// get last element of configurator.filter (the node that is the root for the count):
		IdentifiableEntity filter = configurator.getFilter().get(
				(configurator.getFilter().size()) - 1);

		if (filter == null) {
			countAll(configurator);
		} else {
			countPart(configurator, filter);
		}
	}

	/**
	 * @param configurator
	 */
	private void countAll(StatisticsConfigurator configurator) {
		Statistics statistics = new Statistics(configurator);

		for (StatisticsTypeEnum type : configurator.getType()) {
			Long number = null;
			switch (type) {

			case ALL_TAXA:
				number = Long.valueOf(taxonDao.count(TaxonBase.class));
				break;
			case SYNONYMS:
				number = Long.valueOf(taxonDao.count(Synonym.class));
				break;
			case ACCEPTED_TAXA:
				number = Long.valueOf(taxonDao.count(Taxon.class));
				break;
			case ALL_REFERENCES:
				number = Long
						.valueOf(referenceDao
								.count(eu.etaxonomy.cdm.model.reference.Reference.class));
				break;

			case NOMECLATURAL_REFERENCES:

				number = statisticsDao.countNomenclaturalReferences();
				break;

			case CLASSIFICATION:
				number = Long.valueOf(classificationDao
						.count(Classification.class));

				break;

			case TAXON_NAMES:
				number = Long.valueOf(taxonNameDao.count(TaxonNameBase.class));
				break;

			case DESCRIPTIVE_SOURCE_REFERENCES:

				number = statisticsDao.countDescriptiveSourceReferences();

				break;
			}

			statistics.addCount(type, number);
		}
		statisticsList.add(statistics);
	}

	@Transactional
	private void countPart(StatisticsConfigurator configurator,
			IdentifiableEntity filter) {
		Statistics statistics = new Statistics(configurator);
		// TODO count the items in the classification - there have to be dao method(s) for that first.
		System.out.println("count in classification: " + filter.toString());
		
		statisticsList.add(statistics);
	}
	
}
