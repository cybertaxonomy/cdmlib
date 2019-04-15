/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.longrunningService;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.IDescriptiveDataSetService;
import eu.etaxonomy.cdm.api.service.IProgressMonitorService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.UpdateResult;
import eu.etaxonomy.cdm.api.service.config.CacheUpdaterConfigurator;
import eu.etaxonomy.cdm.api.service.config.ForSubtreeConfiguratorBase;
import eu.etaxonomy.cdm.api.service.config.PublishForSubtreeConfigurator;
import eu.etaxonomy.cdm.api.service.config.SecundumForSubtreeConfigurator;
import eu.etaxonomy.cdm.api.service.config.SortIndexUpdaterConfigurator;
import eu.etaxonomy.cdm.api.service.util.CacheUpdater;
import eu.etaxonomy.cdm.api.service.util.SortIndexUpdaterWrapper;
import eu.etaxonomy.cdm.common.monitor.IRemotingProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.RemotingProgressMonitorThread;
import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;

/**
 * @author k.luther
 * @since 04 May 2018
 *
 */
@Service
@Transactional(readOnly = false)
public class LongRunningTasksServiceImpl implements ILongRunningTasksService{
    @Autowired
    ITaxonNodeService taxonNodeService;

    @Autowired
    IDescriptiveDataSetService descriptiveDataSetService;

    @Autowired
    IProgressMonitorService progressMonitorService;

    @Autowired
    CacheUpdater updater;

    @Autowired
    SortIndexUpdaterWrapper sortIndexUpdater;

    @Override
    public UUID monitGetRowWrapper(DescriptiveDataSet descriptiveDataSet) {
        RemotingProgressMonitorThread monitorThread = new RemotingProgressMonitorThread() {
            @Override
            public Serializable doRun(IRemotingProgressMonitor monitor) {
                return descriptiveDataSetService.getRowWrapper(descriptiveDataSet, monitor);
            }
        };
        UUID uuid = progressMonitorService.registerNewRemotingMonitor(monitorThread);
        monitorThread.setPriority(3);
        monitorThread.start();
        return uuid;
    }

    @Override
    public UUID aggregateComputedTaxonDescriptions(UUID taxonNodeUuid, UUID descriptiveDataSetUuid){
        RemotingProgressMonitorThread monitorThread = new RemotingProgressMonitorThread() {
            @Override
            public Serializable doRun(IRemotingProgressMonitor monitor) {
                UpdateResult updateResult = descriptiveDataSetService.aggregateTaxonDescription(taxonNodeUuid, descriptiveDataSetUuid, monitor);
                for(Exception e : updateResult.getExceptions()) {
                    monitor.addReport(e.getMessage());
                }
                monitor.setResult(updateResult);
                return updateResult;

            }
        };
        UUID uuid = progressMonitorService.registerNewRemotingMonitor(monitorThread);
        monitorThread.setPriority(2);
        monitorThread.start();
        return uuid;
    }

    @Override
    public UUID monitLongRunningTask(ForSubtreeConfiguratorBase config) {

        RemotingProgressMonitorThread monitorThread = new RemotingProgressMonitorThread() {
            @Override
            public Serializable doRun(IRemotingProgressMonitor monitor) {
                UpdateResult result;
                config.setMonitor(monitor);

                result = updateData(config);
                for(Exception e : result.getExceptions()) {
                    monitor.addReport(e.getMessage());
                }
                monitor.setResult(result);
                return result;
            }
        };
        UUID uuid = progressMonitorService.registerNewRemotingMonitor(monitorThread);
        monitorThread.setPriority(2);
        monitorThread.start();
        return uuid;
    }

    private UpdateResult updateData(ForSubtreeConfiguratorBase config){
        if (config instanceof SecundumForSubtreeConfigurator){
            return taxonNodeService.setSecundumForSubtree((SecundumForSubtreeConfigurator)config);
        }else{
            return taxonNodeService.setPublishForSubtree(config.getSubtreeUuid(), ((PublishForSubtreeConfigurator)config).isPublish(), ((PublishForSubtreeConfigurator)config).isIncludeAcceptedTaxa(), ((PublishForSubtreeConfigurator)config).isIncludeSynonyms(), ((PublishForSubtreeConfigurator)config).isIncludeSharedTaxa(), config.getMonitor());
        }
    }

    @Override
    public UUID monitLongRunningTask(Set<UUID> movingUuids, UUID targetTreeNodeUuid, int movingType) {

        RemotingProgressMonitorThread monitorThread = new RemotingProgressMonitorThread() {
            @Override
            public Serializable doRun(IRemotingProgressMonitor remotingMonitor) {
                UpdateResult result;

                result = taxonNodeService.moveTaxonNodes(movingUuids,targetTreeNodeUuid, movingType,  remotingMonitor);
                for(Exception e : result.getExceptions()) {
                    remotingMonitor.addReport(e.getMessage());
                }
                remotingMonitor.setResult(result);
                return result;
            }
        };
        UUID uuid = progressMonitorService.registerNewRemotingMonitor(monitorThread);
        monitorThread.setPriority(2);
        monitorThread.start();
        return uuid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID monitLongRunningTask(CacheUpdaterConfigurator configurator) {
        RemotingProgressMonitorThread monitorThread = new RemotingProgressMonitorThread() {
            @Override
            public Serializable doRun(IRemotingProgressMonitor monitor) {
                UpdateResult result;

                configurator.setMonitor(monitor);

                result = updater.doInvoke(configurator);

                for(Exception e : result.getExceptions()) {
                    monitor.addReport(e.getMessage());
                }
                monitor.setResult(result);
                return result;
            }
        };
        UUID uuid = progressMonitorService.registerNewRemotingMonitor(monitorThread);
        monitorThread.setPriority(2);
        monitorThread.start();
        return uuid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID monitLongRunningTask(SortIndexUpdaterConfigurator configurator) {
        RemotingProgressMonitorThread monitorThread = new RemotingProgressMonitorThread() {
            @Override
            public Serializable doRun(IRemotingProgressMonitor monitor) {
                UpdateResult result;
                configurator.setMonitor(monitor);

                result = sortIndexUpdater.doInvoke(configurator);

                for(Exception e : result.getExceptions()) {
                    monitor.addReport(e.getMessage());
                }
                monitor.setResult(result);
                return result;
            }
        };
        UUID uuid = progressMonitorService.registerNewRemotingMonitor(monitorThread);
        monitorThread.setPriority(2);
        monitorThread.start();
        return uuid;
    }


}
