/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.search.ICdmMassIndexer;
import eu.etaxonomy.cdm.database.DataSourceInfo;
import eu.etaxonomy.cdm.database.DataSourceReloader;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.remote.controller.util.ProgressMonitorUtil;
import eu.etaxonomy.cdm.remote.editor.CdmTypePropertyEditor;
import io.swagger.annotations.Api;

@Controller
@Api("manage")
@CrossOrigin(origins="*")
@RequestMapping(value = { "/manage" })
public class ManagementController {
    public static final Logger logger = Logger
            .getLogger(ManagementController.class);

    // @Autowired
    private DataSourceReloader datasoucrceLoader;

    @Autowired
    public ICdmMassIndexer indexer;

    @Autowired
    public ProgressMonitorController progressMonitorController;

    /**
     * There should only be one processes operating on the lucene index
     * therefore the according progress monitor uuid is stored in this static
     * field.
     */
    private static UUID indexMonitorUuid = null;

    @InitBinder
    public void initIndexClassBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Class.class, new CdmTypePropertyEditor());
    }

    @InitBinder
    public void initIndexArrayBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Class[].class, new CdmTypePropertyEditor());
    }

    /*
     * return page not found http error (404) for unknown or incorrect UUIDs
     * (non-Javadoc)
     *
     * @see
     * org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal
     * (javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    // @RequestMapping(value = { "/manager/datasources/list" }, method =
    // RequestMethod.GET)
    protected ModelAndView doList(HttpServletRequest request,
            HttpServletResponse respone) throws Exception {

        ModelAndView mv = new ModelAndView();
        Map<String, DataSourceInfo> dataSourceInfos = datasoucrceLoader.test();
        mv.addObject(dataSourceInfos);

        return mv;
    }

    // @RequestMapping(value = { "/manager/datasources/reload" }, method =
    // RequestMethod.GET)
    public ModelAndView doReload(HttpServletRequest request,
            HttpServletResponse respone) throws Exception {

        ModelAndView mv = new ModelAndView();
        Map<String, DataSourceInfo> dataSourceInfos = datasoucrceLoader
                .reload();
        mv.addObject(dataSourceInfos);

        return mv;
    }

    /**
     *
     * Reindex all cdm entities listed in
     * {@link ICdmMassIndexer#indexedClasses()}. Re-indexing will not purge the
     * index.
     *
     * @param frontendBaseUrl
     *            if the CDM server is running behind a reverse proxy you need
     *            to supply the base URL of web service front-end which is
     *            provided by the proxy server.
     * @param request
     * @param respone
     * @return
     * @throws Exception
     */
    @RequestMapping(value = { "reindex" }, method = {RequestMethod.GET, RequestMethod.OPTIONS})
    public synchronized ModelAndView doReindex(
             @RequestParam(value = "frontendBaseUrl", required = false) String frontendBaseUrl,
             @RequestParam(value = "type", required = false) Class<? extends CdmBase>[] types,
             @RequestParam(value = "priority", required = false) Integer priority,
             @RequestParam(value = "dataRedirect", required = false) boolean dataRedirect,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        final List<Class<? extends CdmBase>> typeSet = asList(types);

        String processLabel = "Re-indexing";

        ProgressMonitorUtil progressUtil = new ProgressMonitorUtil(
                progressMonitorController);

        if (!progressMonitorController.isMonitorRunning(indexMonitorUuid)) {
            indexMonitorUuid = progressUtil.registerNewMonitor();
            Thread subThread = new Thread() {
                @Override
                public void run() {
                    indexer.reindex(typeSet, progressMonitorController.getMonitor(indexMonitorUuid));
                }
            };
            if (priority == null) {
                priority = AbstractController.DEFAULT_BATCH_THREAD_PRIORITY;
            }
            subThread.setPriority(priority);
            subThread.start();
        }
        // send redirect "see other"
        return progressUtil.respondWithMonitor(frontendBaseUrl, processLabel,
                indexMonitorUuid, dataRedirect, request, response);
    }

    /**
     * @param types
     */
    private List<Class<? extends CdmBase>> asList(Class<? extends CdmBase>[] types) {

    	List<Class<? extends CdmBase>> typeList = null;
        if(types != null) {
            typeList = new ArrayList<Class<? extends CdmBase>>();
            for (Class<? extends CdmBase> type : types) {
                if(type != null && ! typeList.contains(type)) {
                    typeList.add(type);
                }
            }
        }
        return typeList;
    }

    /**
     *
     * Create dictionaries for all cdm entities listed in
     * {@link ICdmMassIndexer#dictionaryClasses()}. Re-dicting will not purge
     * the dictionaries.
     *
     * @param frontendBaseUrl
     *            if the CDM server is running behind a reverse proxy you need
    *            to supply the base URL of web service front-end which is
    *            provided by the proxy server.
    * @param request
    * @param respone
    * @return
    * @throws Exception
    */
   @RequestMapping(value = { "redict" }, method = {RequestMethod.GET, RequestMethod.OPTIONS})
    public synchronized ModelAndView doRedict(
            @RequestParam(value = "frontendBaseUrl", required = false) String frontendBaseUrl,
            @RequestParam(value = "priority", required = false) Integer priority,
            @RequestParam(value = "dataRedirect", required = false) boolean dataRedirect,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

       String processLabel = "Re-Dicting";
        ProgressMonitorUtil progressUtil = new ProgressMonitorUtil(
                progressMonitorController);

        if (!progressMonitorController.isMonitorRunning(indexMonitorUuid)) {
           indexMonitorUuid = progressUtil.registerNewMonitor();
            Thread subThread = new Thread() {
               @Override
                public void run() {
                    indexer.createDictionary(progressMonitorController
                            .getMonitor(indexMonitorUuid));
               }
           };
            if (priority == null) {
               priority = AbstractController.DEFAULT_BATCH_THREAD_PRIORITY;
           }
           subThread.setPriority(priority);
           subThread.start();
       }
       // send redirect "see other"
        return progressUtil.respondWithMonitor(frontendBaseUrl, processLabel,
                indexMonitorUuid, dataRedirect, request, response);
   }

    /**
     * This will wipe out the index.
     *
     * @param request
     * @param respone
     * @return
     * @throws Exception
     */
    @RequestMapping(value = { "purge" }, method = {RequestMethod.GET, RequestMethod.OPTIONS})
    public synchronized ModelAndView doPurge(
            @RequestParam(value = "frontendBaseUrl", required = false) String frontendBaseUrl,
            @RequestParam(value = "priority", required = false) Integer priority,
            @RequestParam(value = "dataRedirect", required = false) boolean dataRedirect,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String processLabel = "Purging";

        ProgressMonitorUtil progressUtil = new ProgressMonitorUtil(
                progressMonitorController);

        if (!progressMonitorController.isMonitorRunning(indexMonitorUuid)) {
            indexMonitorUuid = progressUtil.registerNewMonitor();
            Thread subThread = new Thread() {
                @Override
                public void run() {
                    indexer.purge(progressMonitorController
                            .getMonitor(indexMonitorUuid));
                }
            };
            if (priority == null) {
                priority = AbstractController.DEFAULT_BATCH_THREAD_PRIORITY;
            }
            subThread.setPriority(priority);
            subThread.start();
        }

        // send redirect "see other"
        return progressUtil.respondWithMonitor(frontendBaseUrl, processLabel,
                indexMonitorUuid, dataRedirect, request, response);
    }



}
