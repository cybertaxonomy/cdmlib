/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.service.longrunningService;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.service.IDescriptiveDataSetService;
import eu.etaxonomy.cdm.api.service.IProgressMonitorService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.UpdateResult;
import eu.etaxonomy.cdm.api.service.config.CacheUpdaterConfigurator;
import eu.etaxonomy.cdm.api.service.config.ForSubtreeConfiguratorBase;
import eu.etaxonomy.cdm.api.service.config.PublishForSubtreeConfigurator;
import eu.etaxonomy.cdm.api.service.config.SecundumForSubtreeConfigurator;
import eu.etaxonomy.cdm.api.service.config.SortIndexUpdaterConfigurator;
import eu.etaxonomy.cdm.api.service.description.DescriptionAggregationBase;
import eu.etaxonomy.cdm.api.service.description.DescriptionAggregationConfigurationBase;
import eu.etaxonomy.cdm.api.service.util.CacheUpdater;
import eu.etaxonomy.cdm.api.service.util.SortIndexUpdaterWrapper;
import eu.etaxonomy.cdm.common.JvmLimitsException;
import eu.etaxonomy.cdm.common.monitor.IRemotingProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.RemotingProgressMonitorThread;
import eu.etaxonomy.cdm.persistence.dto.SpecimenNodeWrapper;

/**
 * @author k.luther
 * @since 04 May 2018
 */
@Service("longRunningTasksService")
@Transactional(readOnly = false)
public class LongRunningTasksService implements ILongRunningTasksService{

    @Autowired
    private ITaxonNodeService taxonNodeService;

    @Autowired
    private IDescriptiveDataSetService descriptiveDataSetService;

    @Autowired
    private IProgressMonitorService progressMonitorService;

    @Autowired
    private CacheUpdater updater;

    @Autowired
    private SortIndexUpdaterWrapper sortIndexUpdater;

    @Autowired
    @Qualifier("cdmRepository")
    private ICdmRepository repository;

    @Override
    public UUID monitGetRowWrapper(UUID descriptiveDataSetUuid) {
        RemotingProgressMonitorThread monitorThread = new RemotingProgressMonitorThread() {
            @Override
            public Serializable doRun(IRemotingProgressMonitor monitor) {
                return descriptiveDataSetService.getRowWrapper(descriptiveDataSetUuid, monitor);
            }
        };
        UUID uuid = progressMonitorService.registerNewRemotingMonitor(monitorThread);
        monitorThread.setPriority(3);
        monitorThread.start();
        return uuid;
    }


    @Override
    public <T extends DescriptionAggregationBase<T,C>, C extends DescriptionAggregationConfigurationBase<T>>
                UUID invoke(C config){

        RemotingProgressMonitorThread monitorThread = new RemotingProgressMonitorThread() {
            @Override
            public Serializable doRun(IRemotingProgressMonitor monitor) {
                T task = config.getTaskInstance();
                UpdateResult updateResult = null;
                try{
                    updateResult = task.invoke(config, repository);
                    for(Exception e : updateResult.getExceptions()) {
                        monitor.addReport(e.getMessage());
                    }
                } catch (JvmLimitsException e) {
                    monitor.warning("Memory problem. Java Virtual Machine limits exceeded. Task was interrupted", e);
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
    public UUID addRowWrapperToDataset(Collection<SpecimenNodeWrapper> wrapper, UUID datasetUuid){
        RemotingProgressMonitorThread monitorThread = new RemotingProgressMonitorThread() {
            @Override
            public Serializable doRun(IRemotingProgressMonitor monitor) {
                UpdateResult updateResult = descriptiveDataSetService.addRowWrapperToDataset(wrapper, datasetUuid);
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
    public UUID generatePolytomousKey(UUID datasetUuid, UUID taxonUuid){
        RemotingProgressMonitorThread monitorThread = new RemotingProgressMonitorThread() {
            @Override
            public Serializable doRun(IRemotingProgressMonitor monitor) {
                UpdateResult updateResult = descriptiveDataSetService.generatePolytomousKey(datasetUuid, taxonUuid);
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
                config.setMonitor(monitor);

                UpdateResult result = updateData(config);
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
            return taxonNodeService.setPublishForSubtree((PublishForSubtreeConfigurator) config);
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

    @Override
    public UUID monitLongRunningTask(SortIndexUpdaterConfigurator configurator) {
        RemotingProgressMonitorThread monitorThread = new RemotingProgressMonitorThread() {
            @Override
            public Serializable doRun(IRemotingProgressMonitor monitor) {

                configurator.setMonitor(monitor);
                UpdateResult result = sortIndexUpdater.doInvoke(configurator);

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
