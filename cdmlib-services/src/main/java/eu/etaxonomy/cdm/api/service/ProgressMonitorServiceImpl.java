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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import eu.etaxonomy.cdm.api.service.util.RemotingProgressMonitorThread;
import eu.etaxonomy.cdm.common.monitor.IRemotingProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.IRestServiceProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.RemotingProgressMonitor;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermissionEvaluator;
import eu.etaxonomy.cdm.persistence.hibernate.permission.Role;

/**
 * @author cmathew
 * @date 14 Oct 2015
 *
 */
@Service
public class ProgressMonitorServiceImpl implements IProgressMonitorService {

    @Autowired
    public ProgressMonitorManager<IRestServiceProgressMonitor> progressMonitorManager;

    @Autowired
    public CdmPermissionEvaluator permissionEvaluator;

    @Override
    public UUID registerNewRemotingMonitor() {
        RemotingProgressMonitor monitor = new RemotingProgressMonitor();
        UUID uuid = progressMonitorManager.registerMonitor(monitor);
        return uuid;
    }

    @Override
    public IRemotingProgressMonitor getRemotingMonitor(UUID uuid) {
        IRestServiceProgressMonitor monitor = progressMonitorManager.getMonitor(uuid);
        if(monitor != null && monitor instanceof IRemotingProgressMonitor ) {
            IRemotingProgressMonitor remotingMonitor = (IRemotingProgressMonitor)monitor;
            String monitorOwner = remotingMonitor.getOwner();
            User currentUser = User.getCurrentAuthenticatedUser();
            if(currentUser != null &&
                    (currentUser.getUsername().equals(monitorOwner) ||
                            permissionEvaluator.hasOneOfRoles(SecurityContextHolder.getContext().getAuthentication(), Role.ROLE_ADMIN))) {
                return remotingMonitor;
            }
        }
        return null;
    }

    @Override
    public void interrupt(UUID uuid) {
        RemotingProgressMonitorThread monitorThread = RemotingProgressMonitorThread.getMonitorThread(getRemotingMonitor(uuid));
        if(monitorThread != null) {
            monitorThread.interrupt();
        }

    }

    @Override
    public boolean isMonitorThreadRunning(UUID uuid) {
        return RemotingProgressMonitorThread.getMonitorThread(getRemotingMonitor(uuid)) != null;
    }

}
