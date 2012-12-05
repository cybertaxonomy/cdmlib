package eu.etaxonomy.cdm.api.service;

import eu.etaxonomy.cdm.api.service.statistics.Statistics;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsConfigurator;

/**
 * @author sybille
 *
 */
public interface IStatisticsService {


	Statistics getCountStatistics(StatisticsConfigurator configurator);

	
}
