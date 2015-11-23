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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.monitor.IRestServiceProgressMonitor;

/**
 * Manages monitors for long running jobs.
 *
 * @author cmathew
 * @date 14 Oct 2015
 *
 */
@Component
public class ProgressMonitorManager<T extends IRestServiceProgressMonitor> {

    private final Map<UUID, T> monitors = new ConcurrentHashMap<UUID, T>();

    private final Map<UUID, Long> timeoutMap = new HashMap<UUID, Long>();

    private Thread cleanUpThread = null;

    /**
     * Time out in minutes for monitors which are done.
     * A monitor which is set done will be removed after this interval.
     */
    private final int cleanUpTimeout = 1;

    /**
     *
     */
    private final int cleanUpInterval = 1000 * 10; // 10 seconds

    public ProgressMonitorManager() {

        this.cleanUpThread = new Thread(){

            @Override
            public void run() {
                while(true){
                    scheduledCleanUp();
                    try {
                        sleep(cleanUpInterval);
                    } catch (InterruptedException e) {
                        /* IGNORE */
                    }
                }
            }

        };
        cleanUpThread.start();
    }

    /**
     * run every n minutes clean up monitors which have been marked done x minutes ago
     */
    private void scheduledCleanUp() {

        List<UUID> timedOutMonitors = new ArrayList<UUID>();
        IRestServiceProgressMonitor monitor;

        long now = System.currentTimeMillis();
        long nextTimeout = now + cleanUpTimeout * 1000 * 60;


        // add monitors which are stopped or done to the timeoutMap
        for(UUID uuid : monitors.keySet()){
            monitor = monitors.get(uuid);
            if((monitor.isFailed() || monitor.isDone())){
                if(!timeoutMap.containsKey(uuid)){
                    timeoutMap.put(uuid, nextTimeout);
                }
            }
            if(monitor.hasFeedbackWaitTimedOut()) {
                monitor.interrupt();
            }
        }

        // check with monitor has timed out
        for(UUID uuid : timeoutMap.keySet()){
            if(timeoutMap.get(uuid) <= now){
                timedOutMonitors.add(uuid);
            }
        }

        //finally remove the monitors
        for(UUID uuid : timedOutMonitors){
            timeoutMap.remove(uuid);
            monitors.remove(uuid);
        }

    }

    public UUID registerMonitor(T monitor){
        UUID uuid = UUID.randomUUID();
        monitors.put(uuid, monitor);
        return uuid;
    }

    public IRestServiceProgressMonitor getMonitor(UUID uuid) {
        if(uuid == null) {
            return null;
        }
        return monitors.get(uuid);
    }

    /**
     * returns true if the {@link IRestServiceProgressMonitor} identified by the <code>uuid</code>
     * exists and if it is still indicating a running thread
     * @param uuid
     * @return
     */
    public boolean isMonitorRunning(UUID uuid) {
        IRestServiceProgressMonitor monitor = getMonitor(uuid);
        return monitor != null && !monitor.isCanceled() && !monitor.isDone() && !monitor.isFailed();
    }

    public Map<UUID, T> getMonitors() {
        return monitors;
    }

}
