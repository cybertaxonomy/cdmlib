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
     * @return
     */
    public UUID registerNewRemotingMonitor();

    /**
     * @param uuid
     * @return
     */
    public IRemotingProgressMonitor getRemotingMonitor(UUID uuid);

    /**
     * @param uuid
     */
    public void interrupt(UUID uuid);

    /**
     * @param uuid
     * @return
     */
    public boolean isMonitorThreadRunning(UUID uuid);

}
