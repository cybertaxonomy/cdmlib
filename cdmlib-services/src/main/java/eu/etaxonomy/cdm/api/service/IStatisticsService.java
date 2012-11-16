package eu.etaxonomy.cdm.api.service;

import eu.etaxonomy.cdm.api.service.statistics.Statistics;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsConfigurator;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public interface IStatisticsService extends IService<IdentifiableEntity<?>>{

	Statistics getStatistics(StatisticsConfigurator configurator);

	Integer countAll(StatisticsConfigurator configurator);
}
