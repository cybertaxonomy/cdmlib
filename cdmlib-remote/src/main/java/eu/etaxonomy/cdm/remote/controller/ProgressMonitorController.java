// $Id$
/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.common.monitor.IRestServiceProgressMonitor;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;

/**
 * @author Andreas Kohlbecker
 * @date Jul 16, 2012
 *
 */
@Controller
@RequestMapping(value="/progress/")
public class ProgressMonitorController {

    private Map<UUID, IRestServiceProgressMonitor> monitors = new HashMap<UUID, IRestServiceProgressMonitor>();

    private Map<UUID, Long> timeoutMap = new HashMap<UUID, Long>();

    private Thread cleanUpThread = null;

    /**
     * Time out in minutes for monitors which are done.
     * A monitor which is set done will be removed after this interval.
     */
    private int cleanUpTimeout = 1;

    /**
     *
     */
    private int cleanUpInterval = 1000 * 10; // 10 seconds

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UUID.class, new UUIDPropertyEditor());
    }

    public ProgressMonitorController(){

        this.cleanUpThread = new Thread(){

            public void run() {
                scheduledCeanUp();
                try {
                    sleep(cleanUpInterval);
                } catch (InterruptedException e) {
                    /* IGNORE */
                }
                run();
            }

        };
        cleanUpThread.start();
    }


    /**
     * run every n minutes clean up monitors which have been marked done x minutes ago
     */
    private void scheduledCeanUp() {

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

    public UUID registerMonitor(IRestServiceProgressMonitor monitor){
        UUID uuid = UUID.randomUUID();
        monitors.put(uuid, monitor);
        return uuid;
    }

    public String pathFor(HttpServletRequest request, UUID uuid){
        String servletPath = request.getServletPath();
        String fileExtension = FilenameUtils.getExtension(servletPath);
        return request.getContextPath() + "/progress/" + uuid.toString() + (fileExtension.length() > 0 ? '.': "") + fileExtension;
    }

    @RequestMapping(value = "{uuid}", method = RequestMethod.GET)
    public ModelAndView doProgressMonitor(@PathVariable("uuid") UUID uuid, HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        ModelAndView mv = new ModelAndView();

        if (monitors.containsKey(uuid)) {
            mv.addObject(monitors.get(uuid));
        } else {
            response.sendError(404, "No such progress monitor found. The process being monitored may have been completed and the according monitor may have been removed due to the clean up timepout.");
        }

        return mv;
    }
}
