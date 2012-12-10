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
	 * @return be aware that a Statistics.countMap might contain "null" for {@link Number} value,
	 * if the count failed (, if the value is "0" the count succeeded and sum is 0)
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
		// TODO use "about" parameter of Statistics element

		return this.statisticsList;
	}
	


	@Transactional
	private void countStatisticsPart(StatisticsConfigurator configurator) {
		//TODO use "filter" in count functionality 
		//get last element of configurator.filter:
		IdentifiableEntity filter= configurator.getFilter().get((configurator.getFilter().size())-1);
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
				// number +=
				// (referenceDao.getAllNomenclaturalReferences()).size(); // to
				// slow!!!
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
	
	
	
	//-------------------------------------------------

	
//	private void countStatistic(StatisticsConfigurator configurator){
//	
//		Statistics statistics = new Statistics(configurator);
//		for (StatisticsPartEnum part : configurator.getPart()) {
//			switch (part) {
//			case ALL:
//				countAll(configurator);
//				break;
//
//			case CLASSIFICATION:
//				// get classifications
//				// foreach classification:
//				countPart();
//				break;
//			}
//		}
//	}

//	private void calculatePart() {
//		//TODO
//		for (StatisticsPartEnum part : configurator.getPart()) {
//			switch (part) {
//			case ALL:
//				countAll();
//				break;
//
//			case CLASSIFICATION:
//				// get classifications
//				// foreach classification:
//				countPart();
//				break;
//			}
//		}
//
//	}
	
	
	@Transactional
	private void countPart() {

	}

	private Integer getDescriptiveSourceReferences() {
		// int counter = 0;

		// count references from each description:
		// TODO test this function or write dao and delete it

		// // we need the set to get off the doubles:

		/*
		 * TODO >>> better performance and more reliabale deduplication with
		 * Set<UUID> referenceUuids = new HashSet<UUID>();
		 */
		Set<UUID> referenceUuids = new HashSet<UUID>();
		Set<eu.etaxonomy.cdm.model.reference.Reference<?>> references = new HashSet<eu.etaxonomy.cdm.model.reference.Reference<?>>();
		// TODO second param 0?:

		/*
		 * TODO >>>> it should not be necessary to use init stratgies >>>>
		 * listDescriptions(null, null, null, null, null, null, null, null);
		 * would list all descriptions
		 */
		List<DescriptionBase> descriptions = descriptionDao.listDescriptions(
				TaxonDescription.class, null, null, null, null, null, null,
				DESCRIPTION_SOURCE_REF_STRATEGIE);
		descriptions.addAll(descriptionDao.listDescriptions(
				TaxonNameDescription.class, null, null, null, null, null, null,
				DESCRIPTION_SOURCE_REF_STRATEGIE));
		descriptions.addAll(descriptionDao.listDescriptions(
				SpecimenDescription.class, null, null, null, null, null, null,
				DESCRIPTION_SOURCE_REF_STRATEGIE));
		// list(null, 0);
		for (DescriptionBase<?> description : descriptions) {

			// get all sources of the description
			Set<IdentifiableSource> sources = description.getSources();
			for (IdentifiableSource source : sources) {
				if (source.getCitation() != null)

					references.add(source.getCitation());
			}

			/*
			 * TODO >>>> get all description elements from the description
			 * 
			 * e.g: for (DescriptionElementBase element :
			 * description.getElements()) { for (DescriptionElementSource source
			 * : element.getSources()) {
			 * 
			 * } }
			 */
		}

		// this part still provokes an error:
		// count references from each description element:
		List<DescriptionElementBase> descrElements = descrElementDao.list(null,
				0, null, DESCR_ELEMENT_REF_STRATEGIE);
		for (DescriptionElementBase descriptionElement : descrElements) {
			Set<DescriptionElementSource> elementSources = descriptionElement
					.getSources();
			for (DescriptionElementSource source : elementSources) {
				if (source.getCitation() != null)
					references.add(source.getCitation());
			}
		}

		return references.size();
	}

}
