// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.util;

import java.util.concurrent.ConcurrentHashMap;

import eu.etaxonomy.cdm.common.monitor.IRemotingProgressMonitor;
import eu.etaxonomy.cdm.model.common.User;

/**
 * @author cmathew
 * @date 22 Oct 2015
 *
 */
public abstract class RemotingProgressMonitorThread extends Thread {

    private static ConcurrentHashMap<IRemotingProgressMonitor, RemotingProgressMonitorThread> monitorsInProgress =
            new ConcurrentHashMap<IRemotingProgressMonitor, RemotingProgressMonitorThread>();

    private IRemotingProgressMonitor monitor;


    public RemotingProgressMonitorThread(IRemotingProgressMonitor monitor) {
        if(monitor == null) {
            throw new IllegalStateException("Monitor is null");
        }
        User user = User.getCurrentAuthenticatedUser();
        if(user == null) {
            throw new IllegalStateException("Current authenticated user is null");
        }
        this.monitor = monitor;
        this.monitor.setOwner(user.getUsername());
    }

    @Override
    public void run() {
        try {
            monitorsInProgress.put(monitor, this);
            monitor.setResult(doRun(monitor));
        } catch(Exception ex) {
            monitor.setResult(ex);
            monitor.setIsFailed(true);
        }
        monitor.done();
        monitorsInProgress.remove(monitor);
    }

    public abstract Object doRun(IRemotingProgressMonitor monitor);

    public static RemotingProgressMonitorThread getMonitorThread(IRemotingProgressMonitor monitor) {
        return monitorsInProgress.get(monitor);
    }

    @Override
    public void interrupt() {
        super.interrupt();
        monitor.setCanceled(true);
        monitor.done();
        monitorsInProgress.remove(monitor);
    }

}
