package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.api.service.statistics.Statistics;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsConfigurator;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * @author sybille
 *
 */
public interface IStatisticsService {


//	List<Statistics> getCountStatistics(StatisticsConfigurator configurator);

	public List<Statistics> getCountStatistics(
			List<StatisticsConfigurator> configurator);

	public IdentifiableEntity<?> getFilterALL_DB();

	
}
