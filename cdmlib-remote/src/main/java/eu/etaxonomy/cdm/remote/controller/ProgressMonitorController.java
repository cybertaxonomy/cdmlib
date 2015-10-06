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

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.wordnik.swagger.annotations.Api;

import eu.etaxonomy.cdm.common.monitor.IRestServiceProgressMonitor;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;

/**
 * @author Andreas Kohlbecker
 * @date Jul 16, 2012
 *
 */
@Controller
@Api(value="progress", description="provides access to information on long running processes. "
        + "URIs to the resources exposed by this controller are provided in the responses to the"
        + "HTTP requests that trigger long term processes.")
@RequestMapping(value="/progress/")
public class ProgressMonitorController {

    private final Map<UUID, IRestServiceProgressMonitor> monitors = new HashMap<UUID, IRestServiceProgressMonitor>();

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

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UUID.class, new UUIDPropertyEditor());
    }

    public ProgressMonitorController(){

        this.cleanUpThread = new Thread(){

            @Override
            public void run() {
                while(true){
                    scheduledCeanUp();
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

    public IRestServiceProgressMonitor getMonitor(UUID uuid) {
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

    /**
     * provides the relative path to the ProgressMonitor specified by its UUID.
     * File extensions like .xml, .json used during the initial request will be
     * preserved in order to not to break the content type negotiation.
     *
     * @param request
     *            the request for which to create he path for. The file
     *            extension will be read from the servlet path and is appended
     *            to the resulting path.
     * @param uuid
     *            the uuid key of the monitor
     * @return the path of the ProgressMonitor
     */
    public String pathFor(HttpServletRequest request, UUID uuid){
        String fileExtension = FilenameUtils.getExtension(request.getServletPath());
        return "/progress/" + uuid.toString() + (fileExtension.length() > 0 ? '.': "") + fileExtension;
    }

    @RequestMapping(value = "{uuid}", method = RequestMethod.GET)
    public ModelAndView doProgressMonitor(@PathVariable("uuid") UUID uuid, HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        ModelAndView mv = new ModelAndView();

        if (monitors.containsKey(uuid)) {
            mv.addObject(monitors.get(uuid));
        } else {
            response.sendError(404, "No such progress monitor found. The process being monitored may "
                    + "have been completed and the according monitor may have been removed due to "
                    + "the clean up timepout.");
        }

        return mv;
    }
}
