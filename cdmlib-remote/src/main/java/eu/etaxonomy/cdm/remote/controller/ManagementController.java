// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.wsdl.extensions.http.HTTPConstants;

import eu.etaxonomy.cdm.api.service.search.CdmMassIndexer;
import eu.etaxonomy.cdm.api.service.search.ICdmMassIndexer;
import eu.etaxonomy.cdm.common.monitor.RestServiceProgressMonitor;
import eu.etaxonomy.cdm.database.DataSourceInfo;
import eu.etaxonomy.cdm.database.DataSourceReloader;
import eu.etaxonomy.cdm.remote.json.JsonpRedirect;

@Controller
@RequestMapping(value = {"/manage"})
public class ManagementController
{
    public static final Logger logger = Logger.getLogger(ManagementController.class);

//    @Autowired
      private DataSourceReloader datasoucrceLoader;

    @Autowired
    public ICdmMassIndexer indexer;

    @Autowired
    public ProgressMonitorController progressMonitorController;


    private static final int DEFAULT_PAGE_SIZE = 25;

    /*
     * return page not found http error (404) for unknown or incorrect UUIDs
     * (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    //@RequestMapping(value = { "/manager/datasources/list" }, method = RequestMethod.GET)
    protected ModelAndView doList(HttpServletRequest request, HttpServletResponse respone) throws Exception {

        ModelAndView mv = new ModelAndView();
        Map<String, DataSourceInfo> dataSourceInfos = datasoucrceLoader.test();
        mv.addObject(dataSourceInfos);

        return mv;
    }

    //@RequestMapping(value = { "/manager/datasources/reload" }, method = RequestMethod.GET)
    public ModelAndView doReload(HttpServletRequest request, HttpServletResponse respone) throws Exception {

        ModelAndView mv = new ModelAndView();
        Map<String, DataSourceInfo> dataSourceInfos = datasoucrceLoader.reload();
        mv.addObject(dataSourceInfos);

        return mv;
    }

    /**
     *
     * Reindex all cdm entities litest in {@link ICdmMassIndexer#indexedClasses()}.
     * Re-indexing will not purge the index.
     *
     * @param request
     * @param respone
     * @return
     * @throws Exception
     */
    @RequestMapping(value = { "reindex" }, method = RequestMethod.GET)
    public ModelAndView doReindex(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ModelAndView mv = new ModelAndView();

        final RestServiceProgressMonitor monitor = new RestServiceProgressMonitor();
        UUID monitorUuid = progressMonitorController.registerMonitor(monitor);
        String monitorPath = progressMonitorController.pathFor(request, monitorUuid);

        Thread subThread = new Thread(){
            public void run(){
                indexer.reindex(monitor);
            }
        };
        subThread.start();

        // send redirect "see other"
        response.setHeader("Location", monitorPath);
//        response.sendError(303, null);
        boolean isJSONP = request.getParameter("callback") != null;
        if(isJSONP){
            JsonpRedirect jsonpRedirect = new JsonpRedirect(request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() +  monitorPath);
            mv.addObject(jsonpRedirect);
//            response.sendError(303, "{\"redirect\":  \""+ monitorPath + "\"}");
        } else {
            response.sendError(303, "Reindexing started, for progress information please see <a href=\"" + monitorPath + "\">" + monitorPath + "</a>");
        }
//        mv.addObject(monitorPath);
//        mv.setViewName("text");
        return mv;
    }

    /**
     * This will wipe out the index.
     *
     * @param request
     * @param respone
     * @return
     * @throws Exception
     */
    @RequestMapping(value = { "purge" }, method = RequestMethod.GET)
    public ModelAndView doPurge(HttpServletRequest request, HttpServletResponse respone) throws Exception {

        ModelAndView mv = new ModelAndView();

        indexer.purge(null);

        mv.addObject("done!");
        mv.setViewName("text");

        return mv;
    }




}

