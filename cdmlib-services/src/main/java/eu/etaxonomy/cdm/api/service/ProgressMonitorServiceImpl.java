// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.etaxonomy.cdm.common.monitor.IRemotingProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.IRestServiceProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.RemotingProgressMonitor;

/**
 * @author cmathew
 * @date 14 Oct 2015
 *
 */
@Service
public class ProgressMonitorServiceImpl implements IProgressMonitorService {

    @Autowired
    public ProgressMonitorManager<IRestServiceProgressMonitor> progressMonitorManager;

    @Override
    public UUID registerNewRemotingMonitor() {
        RemotingProgressMonitor monitor = new RemotingProgressMonitor();
        UUID uuid = progressMonitorManager.registerMonitor(monitor);
        return uuid;
    }

    @Override
    public IRemotingProgressMonitor getRemotingMonitor(UUID uuid) {
        return (IRemotingProgressMonitor) progressMonitorManager.getMonitor(uuid);
    }

}
