package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.api.service.statistics.Statistics;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsConfigurator;

/**
 * @author sybille
 *
 */
public interface IStatisticsService {


//	List<Statistics> getCountStatistics(StatisticsConfigurator configurator);

	List<Statistics> getCountStatistics(
			List<StatisticsConfigurator> configurator);

	
}
