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

import eu.etaxonomy.cdm.common.monitor.IRemotingProgressMonitor;

/**
 *
 * Service interface to manage progress monitors
 *
 * @author cmathew
 * @date 14 Oct 2015
 *
 */
public interface IProgressMonitorService {

    /**
     * Registers new remoting progress monitor
     *
     * @return uuid of remoting monitor
     */
    public UUID registerNewRemotingMonitor();

    /**
     * Return remoting monitor corresponding to give uuid
     *
     * @param uuid of remoting monitor
     * @return remoting monitor
     */
    public IRemotingProgressMonitor getRemotingMonitor(UUID uuid);

    /**
     * Sets the cancel flag to true for the monitor corresponding to the
     * given uuid
     *
     * @param uuid of remoting monitor
     */
    public void cancel(UUID uuid);

    /**
     * Interrupt thread corresponding to remoting monitor with
     * given uuid
     *
     * @param uuid of remoting monitor
     */
    public void interrupt(UUID uuid);

    /**
     * Checks whether thread corresponding to remoting monitor with
     * given uuid is currently in progress
     *
     * @param uuid of remoting monitor
     * @return true if corresponding thread is in progres, o/w false
     */
    public boolean isMonitorThreadRunning(UUID uuid);

}
