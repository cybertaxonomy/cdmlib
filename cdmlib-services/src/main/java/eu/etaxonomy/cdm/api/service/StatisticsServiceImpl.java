package eu.etaxonomy.cdm.api.service;

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
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
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

    private StatisticsConfigurator configurator;

    private Statistics statistics;

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

    /**
     * counts all the elements referenced in the configurator from the part of
     * the database referenced in the configurator
     */
    @Override
    @Transactional
    public Statistics getCountStatistics(StatisticsConfigurator configurator) {
        this.configurator = configurator;
        this.statistics = new Statistics(configurator);
        //TODO use "about" parameter of Statistics element
        calculateParts();
        return this.statistics;
        // return new Statistics(null);
    }

    private void calculateParts() {
        for (StatisticsPartEnum part : configurator.getPart()) {
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

        for (StatisticsTypeEnum type : configurator.getType()) {
            Integer number = 0;
            switch (type) {

            case ALL_TAXA:
                number += taxonDao.count(Taxon.class);
            case SYNONYMS:
                number += taxonDao.count(Synonym.class);
                break;
            case ACCEPTED_TAXA:
                number += taxonDao.count(Taxon.class);
                break;
            case ALL_REFERENCES:
                number += referenceDao
                        .count(eu.etaxonomy.cdm.model.reference.Reference.class);
                break;
            case CLASSIFICATION:
                number += classificationDao.count(Classification.class);

                break;

            case TAXON_NAMES:
                number += taxonNameDao.count(TaxonNameBase.class);
                break;

            case NOMECLATURAL_REFERENCES:
                number += referenceDao.getAllNomenclaturalReferences().size();
                break;

            case DESCRIPTIVE_SOURCE_REFERENCES:
            	//TODO create statistics DAO
//                number += getDescriptiveSourceReferences();
                break;
            }
            statistics.addCount(type, number);
        }

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


            /* TODO
             *  >>>> it should not be necessary to use init stratgies
             *  >>>> listDescriptions(null, null, null, null, null, null, null, null); would list all descriptions
             *
             */
            List<DescriptionBase> descriptions = descriptionDao
                    .listDescriptions(TaxonDescription.class, null, null, null,
                            null, null, null, DESCRIPTION_SOURCE_REF_STRATEGIE);
            descriptions.addAll(descriptionDao.listDescriptions(
                    TaxonNameDescription.class, null, null, null, null, null,
                    null, DESCRIPTION_SOURCE_REF_STRATEGIE));
            descriptions.addAll(descriptionDao.listDescriptions(
                    SpecimenDescription.class, null, null, null, null, null,
                    null, DESCRIPTION_SOURCE_REF_STRATEGIE));
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
                 * e.g:
                for (DescriptionElementBase element : description.getElements()) {
                    for (DescriptionElementSource source : element.getSources()) {

                    }
                }
                */
            }

            //this part still provokes an error:
            // count references from each description element:
			List<DescriptionElementBase> descrElements = descrElementDao.list(
					null, 0, null, DESCR_ELEMENT_REF_STRATEGIE);
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
