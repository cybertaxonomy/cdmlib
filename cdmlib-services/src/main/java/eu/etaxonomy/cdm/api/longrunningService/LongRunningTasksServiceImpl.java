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
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.IProgressMonitorService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.UpdateResult;
import eu.etaxonomy.cdm.api.service.config.SecundumForSubtreeConfigurator;
import eu.etaxonomy.cdm.common.monitor.IRemotingProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.RemotingProgressMonitorThread;

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
    IProgressMonitorService progressMonitorService;


    @Override
    public UUID monitLongRunningTask(SecundumForSubtreeConfigurator config) {

        RemotingProgressMonitorThread monitorThread = new RemotingProgressMonitorThread() {
            @Override
            public Serializable doRun(IRemotingProgressMonitor monitor) {
                UpdateResult result;
                config.setMonitor(monitor);
                result = updateData(config);
                for(Exception e : result.getExceptions()) {
                    monitor.addReport(e.getMessage());
                }
                return result;
            }
        };
        UUID uuid = progressMonitorService.registerNewRemotingMonitor(monitorThread);
        monitorThread.setPriority(2);
        monitorThread.start();
        return uuid;
    }

    private UpdateResult updateData(SecundumForSubtreeConfigurator config){
        return taxonNodeService.setSecundumForSubtree(config);
    }


}
