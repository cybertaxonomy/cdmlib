/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common.monitor;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

/**
 * Thread class to be used to run monitored jobs
 *
 * @author cmathew
 * @since 22 Oct 2015
 *
 */
public abstract class RemotingProgressMonitorThread extends Thread {

    private static ConcurrentHashMap<IRemotingProgressMonitor, RemotingProgressMonitorThread> monitorsInProgress =
            new ConcurrentHashMap<IRemotingProgressMonitor, RemotingProgressMonitorThread>();

    private IRemotingProgressMonitor monitor;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(RemotingProgressMonitorThread.class);

    public void setMonitor(IRemotingProgressMonitor monitor) {
        if(monitor == null) {
            throw new IllegalStateException("Monitor is null");
        }
        this.monitor = monitor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            monitorsInProgress.put(monitor, this);
            monitor.setResult(doRun(monitor));
        } catch(Exception ex) {
            logger.info("Exception in RemotingProgressMonitorThread ", ex);
            monitor.setResult(ex);
            monitor.setIsFailed(true);
        }
        monitor.done();
        monitorsInProgress.remove(monitor);
    }

    /**
     * Executes the monitored job.
     *
     * @param monitor to be updated by the monitored job
     * @return result object
     */
    public abstract Serializable doRun(IRemotingProgressMonitor monitor);

    /**
     * Returns a currently running monitor thread corresponding to the
     * given monitor.
     *
     * @param monitor for which the thread
     * @return
     */
    protected static RemotingProgressMonitorThread getMonitorThread(IRemotingProgressMonitor monitor) {
        return monitorsInProgress.get(monitor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void interrupt() {
        super.interrupt();
        monitor.setCanceled(true);
        monitor.done();
        monitorsInProgress.remove(monitor);
    }

}
