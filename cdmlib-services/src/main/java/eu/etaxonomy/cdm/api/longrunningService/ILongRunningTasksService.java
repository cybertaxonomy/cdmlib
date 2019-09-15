/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.longrunningService;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.IDescriptiveDataSetService;
import eu.etaxonomy.cdm.api.service.config.CacheUpdaterConfigurator;
import eu.etaxonomy.cdm.api.service.config.DescriptionAggregationConfiguration;
import eu.etaxonomy.cdm.api.service.config.ForSubtreeConfiguratorBase;
import eu.etaxonomy.cdm.api.service.config.SortIndexUpdaterConfigurator;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.persistence.dto.SpecimenNodeWrapper;

/**
 * @author cmathew
 * @since 31 Jul 2015
 *
 */
public interface ILongRunningTasksService {


    /**
     * @param configurator
     * @return
     */
    public UUID monitLongRunningTask(ForSubtreeConfiguratorBase configurator);

    /**
     * @param configurator
     * @return
     */
    public UUID monitLongRunningTask(CacheUpdaterConfigurator configurator);


    /**
     * @param movingUuids
     * @param targetTreeNodeUuid
     * @param movingType
     * @param monitor
     * @return
     */
    UUID monitLongRunningTask(Set<UUID> movingUuids, UUID targetTreeNodeUuid, int movingType);

    /**
     * Monitored invocation of {@link IDescriptiveDataSetService#aggregate(UUID, IProgressMonitor)}
     * @param descriptiveDataSetUuid the data set which should be aggregated
     * @param config the aggregation configuration
     * @return the uuid of the monitor
     */
    public UUID aggregateDescriptiveDataSet(UUID descriptiveDataSetUuid,  DescriptionAggregationConfiguration config);

    /**
     * Monitored invocation of {@link IDescriptiveDataSetService#addRowWrapperToDataset(Collection, UUID)}
     * @param wrapper the specimen wrapper to use for creating the row wrapper
     * @param datasetUuid the target dataset
     * @return the uuid of the monitor
     */
    public UUID addRowWrapperToDataset(Collection<SpecimenNodeWrapper> wrapper, UUID datasetUuid);

    /**
     * Monitored invocation of {@link IDescriptiveDataSetService#getRowWrapper(DescriptiveDataSet, IProgressMonitor)}
     * @param descriptiveDataSetUuid the working set for which getRowWrapper() is invoked
     * @return the uuid of the monitor
     */
    public UUID monitGetRowWrapper(UUID descriptiveDataSetUuid);

    /**
     * @param configurator
     * @return
     */
    UUID monitLongRunningTask(SortIndexUpdaterConfigurator configurator);
}
