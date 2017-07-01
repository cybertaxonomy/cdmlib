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
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.ProgressMonitorManager;
import eu.etaxonomy.cdm.common.monitor.IRestServiceProgressMonitor;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;
import io.swagger.annotations.Api;

/**
 * @author Andreas Kohlbecker
 * @date Jul 16, 2012
 *
 */
@Controller
@CrossOrigin(origins="*")
@Api(value="progress", description="Provides access to information on long running processes. "
        + "URIs to the resources exposed by this controller are provided in the responses to the"
        + "HTTP requests that trigger long term processes.")
@RequestMapping(value="/progress/")
public class ProgressMonitorController {


    @Autowired
    private ProgressMonitorManager<IRestServiceProgressMonitor> progressMonitorManager;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UUID.class, new UUIDPropertyEditor());
    }

    public UUID registerMonitor(IRestServiceProgressMonitor monitor){
        return progressMonitorManager.registerMonitor(monitor);
    }

    public IRestServiceProgressMonitor getMonitor(UUID uuid) {
        return progressMonitorManager.getMonitor(uuid);
    }

    /**
     * returns true if the {@link IRestServiceProgressMonitor} identified by the <code>uuid</code>
     * exists and if it is still indicating a running thread
     * @param uuid
     * @return
     */
    public boolean isMonitorRunning(UUID uuid) {
        return progressMonitorManager.isMonitorRunning(uuid);
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
        Map<UUID, IRestServiceProgressMonitor> monitors = progressMonitorManager.getMonitors();
        if (monitors.containsKey(uuid)) {
            mv.addObject(monitors.get(uuid));
        } else {
            response.sendError(404, "No such progress monitor found. The process being monitored may "
                    + "have been completed and the according monitor may have been removed by "
                    + "the clean up timeout.");
        }

        return mv;
    }
}
