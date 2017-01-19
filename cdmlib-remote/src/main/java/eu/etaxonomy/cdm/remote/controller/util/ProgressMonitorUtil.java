/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller.util;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import eu.etaxonomy.cdm.common.monitor.IRestServiceProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.RestServiceProgressMonitor;
import eu.etaxonomy.cdm.remote.controller.ProgressMonitorController;
import eu.etaxonomy.cdm.remote.json.JsonpRedirect;

/**
 * @author Andreas Kohlbecker
 * @since Sep 21, 2012
 *
 */
public class ProgressMonitorUtil {

    private final ProgressMonitorController progressMonitorController;

    public ProgressMonitorUtil(ProgressMonitorController progressMonitorController) {

        this.progressMonitorController = progressMonitorController;
    }

    /**
     * send redirect "see other"
     *
     * @param frontendBaseUrl
     * @param request
     * @param response
     * @param processLabel
     * @param monitorUuid
     * @return
     * @throws IOException
     */
    public ModelAndView respondWithMonitor(String frontendBaseUrl, HttpServletRequest request, HttpServletResponse response, String processLabel,
            final UUID monitorUuid) throws IOException {
        return respondWithMonitorOrDownload(frontendBaseUrl, null, request, response, processLabel, monitorUuid);
    }


    /**
     * send redirect "see other"
     *
     * @param frontendBaseUrl
     * @param downloadUrl can be null
     * @param request
     * @param response
     * @param processLabel
     * @param monitorUuid
     * @return
     * @throws IOException
     */
    public ModelAndView respondWithMonitorOrDownload(String frontendBaseUrl, String downloadUrl, HttpServletRequest request, HttpServletResponse response, String processLabel,
            final UUID monitorUuid) throws IOException {

        //TODO: add reference to exportCSV...
        ModelAndView mv = new ModelAndView();
        String monitorPath = progressMonitorController.pathFor(request, monitorUuid);
        IRestServiceProgressMonitor monitor = progressMonitorController.getMonitor(monitorUuid);

        if(downloadUrl != null && monitor.isDone() && !monitor.isCanceled() && !monitor.isFailed()) {
            response.setHeader("Location", downloadUrl);
            RedirectView redirectView = new RedirectView(downloadUrl);
            mv.setView(redirectView);
        } else {
            JsonpRedirect jsonpRedirect;
            if(frontendBaseUrl != null){
                jsonpRedirect = new JsonpRedirect(frontendBaseUrl, monitorPath);
            } else {
                jsonpRedirect = new JsonpRedirect(request, monitorPath);
            }

            boolean isJSONP = request.getParameter("callback") != null;
            if(isJSONP){
                response.setHeader("Location", jsonpRedirect.getRedirectURL());
                mv.addObject(jsonpRedirect);
            } else {
                RedirectView redirectView = new RedirectView(jsonpRedirect.getRedirectURL());
                mv.setView(redirectView);
            }
        }
        return mv;
    }


    /**
     * @return
     */
    public UUID registerNewMonitor() {
        final RestServiceProgressMonitor monitor = new RestServiceProgressMonitor();
        final UUID monitorUuid = progressMonitorController.registerMonitor(monitor);
        return monitorUuid;
    }

}
