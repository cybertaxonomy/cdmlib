/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.longrunningService;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.IDescriptiveDataSetService;
import eu.etaxonomy.cdm.api.service.config.CacheUpdaterConfigurator;
import eu.etaxonomy.cdm.api.service.config.ForSubtreeConfiguratorBase;
import eu.etaxonomy.cdm.api.service.config.SortIndexUpdaterConfigurator;
import eu.etaxonomy.cdm.api.service.description.DescriptionAggregationBase;
import eu.etaxonomy.cdm.api.service.description.DescriptionAggregationConfigurationBase;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.persistence.dto.SpecimenNodeWrapper;

/**
 * @author cmathew
 * @since 31 Jul 2015
 */
public interface ILongRunningTasksService {

    public UUID monitLongRunningTask(ForSubtreeConfiguratorBase configurator);

    public UUID monitLongRunningTask(CacheUpdaterConfigurator configurator);

    public UUID monitLongRunningTask(Set<UUID> movingUuids, UUID targetTreeNodeUuid, int movingType);

    /**
     * Monitored invocation of {@link DescriptionAggregationBase#invoke(DescriptionAggregationConfigurationBase, eu.etaxonomy.cdm.api.application.ICdmRepository)}
     * In future not only Aggregation Tasks but all Standardtasks should be called via this method.
     * @param config configuration
     * @return the uuid of the monitor
     */
    public <T extends DescriptionAggregationBase<T,C>, C extends DescriptionAggregationConfigurationBase<T>>
            UUID invoke(C config);

    /**
     * Monitored invocation of {@link IDescriptiveDataSetService#addRowWrapperToDataset(Collection, UUID)}
     * @param wrapper the specimen wrapper to use for creating the row wrapper
     * @param datasetUuid the target dataset
     * @return the uuid of the monitor
     */
    public UUID addRowWrapperToDataset(Collection<SpecimenNodeWrapper> wrapper, UUID datasetUuid);

    /**
     * Monitored invocation of {@link IDescriptiveDataSetService#generatePolytomousKey(UUID, UUID))}
     * @param datasetUuid the data set
     * @param taxonUuid the taxonomic scope of the key
     * @return the uuid of the monitor
     */
    public UUID generatePolytomousKey(UUID datasetUuid, UUID taxonUuid);

    /**
     * Monitored invocation of {@link IDescriptiveDataSetService#getRowWrapper(DescriptiveDataSet, IProgressMonitor)}
     * @param descriptiveDataSetUuid the working set for which getRowWrapper() is invoked
     * @return the uuid of the monitor
     */
    public UUID monitGetRowWrapper(UUID descriptiveDataSetUuid);

    public UUID monitLongRunningTask(SortIndexUpdaterConfigurator configurator);

}
