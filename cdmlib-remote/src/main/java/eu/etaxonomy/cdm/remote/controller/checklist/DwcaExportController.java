/*
* Copyright  EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller.checklist;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.DocUtils;
import eu.etaxonomy.cdm.common.monitor.IRestServiceProgressMonitor;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultExport;
import eu.etaxonomy.cdm.io.dwca.out.DwcaEmlRecord;
import eu.etaxonomy.cdm.io.dwca.out.DwcaTaxExportConfigurator;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.remote.controller.AbstractController;
import eu.etaxonomy.cdm.remote.controller.ProgressMonitorController;
import eu.etaxonomy.cdm.remote.controller.util.ProgressMonitorUtil;
import eu.etaxonomy.cdm.remote.editor.UUIDListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;
import eu.etaxonomy.cdm.remote.view.FileDownloadView;
import eu.etaxonomy.cdm.remote.view.HtmlView;

/**
 * @author a.mueller
 * @created 28.06.2017
 * <p>
 *  This controller exports taxonomies via Darwin Core Archive
 *  (https://en.wikipedia.org/wiki/Darwin_Core_Archive).
 */
@Controller
@RequestMapping(value = { "/dwca" })
public class DwcaExportController extends AbstractController implements ResourceLoaderAware{


    private static final String DWCA_TAX_EXPORT_DOC_RESSOURCE = "classpath:eu/etaxonomy/cdm/doc/remote/apt/dwca-tax-export-default.apt";

    private static final List<String> TAXON_NODE_INIT_STRATEGY = Arrays.asList(new String []{
            "taxon.name",
            });

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private IClassificationService classificationService;

    @Autowired
    private ITaxonNodeService taxonNodeService;

    @Autowired
    public ProgressMonitorController progressMonitorController;

    private ResourceLoader resourceLoader;


    /**
     * There should only be one processes operating on the export
     * therefore the according progress monitor uuid is stored in this static
     * field.
     */
    private static UUID indexMonitorUuid = null;

    private final static long DAY_IN_MILLIS = 86400000;



    private static final Logger logger = Logger.getLogger(DwcaExportController.class);

    /**
     * Helper method, which allows to convert strings directly into uuids.
     *
     * @param binder Special DataBinder for data binding from web request parameters to JavaBean objects.
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UuidList.class, new UUIDListPropertyEditor());
//        binder.registerCustomEditor(NamedArea.class, new TermBaseListPropertyEditor<>(termService));
//        binder.registerCustomEditor(UUID.class, new UUIDEditor());
    }




    /**
     * Documentation webservice for this controller.
     *
     * @param response unused
     * @param request unused
     * @return
     * @throws IOException
     */
    @RequestMapping(value = {""}, method = { RequestMethod.GET})
    public ModelAndView exportGetExplanation(HttpServletResponse response,
            HttpServletRequest request) throws IOException{
        ModelAndView mv = new ModelAndView();
        // Read apt documentation file.
        Resource resource = resourceLoader.getResource(DWCA_TAX_EXPORT_DOC_RESSOURCE);
        // using input stream as this works for both files in the classes directory
        // as well as files inside jars
        InputStream aptInputStream = resource.getInputStream();
        // Build Html View
        Map<String, String> modelMap = new HashMap<>();
        // Convert Apt to Html
        modelMap.put("html", DocUtils.convertAptToHtml(aptInputStream));
        mv.addAllObjects(modelMap);

        HtmlView hv = new HtmlView();
        mv.setView(hv);
        return mv;
    }

    /**
     * This service endpoint is for generating the documentation site.
     * If any request of the other endpoint below is incomplete or false
     * then this method will be triggered.
     *
     * @param response
     * @param request
     * @return
     * @throws IOException
     */
    public ModelAndView exportGetExplanation(HttpServletResponse response,
            HttpServletRequest request, Resource res) throws IOException{
        ModelAndView mv = new ModelAndView();
        // Read apt documentation file.
        Resource resource = (res!= null) ? res : resourceLoader.getResource(DWCA_TAX_EXPORT_DOC_RESSOURCE);
        // using input stream as this works for both files in the classes directory
        // as well as files inside jars
        InputStream aptInputStream = resource.getInputStream();
        // Build Html View
        Map<String, String> modelMap = new HashMap<>();
        // Convert Apt to Html
        modelMap.put("html", DocUtils.convertAptToHtml(aptInputStream));
        mv.addAllObjects(modelMap);

        HtmlView hv = new HtmlView();
        mv.setView(hv);
        return mv;
    }



    /**
     *
     * This Service endpoint will offer a csv file. It caches the csv-file in the system temp directory
     * and will only generate a new one after 24 hours. Or if explicitly triggerd by noCache parameter.
     *
     * @param featureUuids List of uuids to download/select {@link Feature feature}features
     * @param clearCache will trigger export and avoids cached file
     * @param classificationUUID Selected {@link Classification classification} to iterate the {@link Taxon}
     * @param response HttpServletResponse which returns the ByteArrayOutputStream
     * @throws Exception
     */
    @RequestMapping(value = { "dwcaTaxExport" }, method = { RequestMethod.GET })
    public synchronized ModelAndView doDwcaTaxExport(
            @RequestParam(value = "subtrees", required = false) final UuidList subtreeUuids,
            @RequestParam(value = "clearCache", required = false) final boolean clearCache,
//            @RequestParam(value = "demoExport", required = false) final boolean demoExport,
//            @RequestParam(value = "conceptExport", required = false) final boolean conceptExport,
//            @RequestParam(value = "classification", required = false) final String classificationUUID,
//            @RequestParam(value = "area", required = false) final UuidList areas,
            @RequestParam(value = "downloadTokenValueId", required = false) final String downloadTokenValueId,
            @RequestParam(value = "priority", required = false) Integer priority,
            final HttpServletResponse response,
            final HttpServletRequest request) throws Exception {
        /**
         * ========================================
         * progress monitor & new thread for export
         * ========================================
         */
        try{
            ModelAndView mv = new ModelAndView();

            String fileName = makeFileName(response, subtreeUuids);

            final File cacheFile = new File(new File(System.getProperty("java.io.tmpdir")), fileName);
            final String origin = request.getRequestURL().append('?')
                    .append(request.getQueryString()).toString();

            Long result = null;
            if(cacheFile.exists()){
                result = System.currentTimeMillis() - cacheFile.lastModified();
            }
            //if file exists return file instantly
            //timestamp older than one day?
            if(clearCache == false && result != null){ //&& result < 7*(DAY_IN_MILLIS)
                logger.info("result of calculation: " + result);
                Map<String, File> modelMap = new HashMap<>();
                modelMap.put("file", cacheFile);
                mv.addAllObjects(modelMap);
                //application/zip
                FileDownloadView fdv = new FileDownloadView(fileName, "zip");
                mv.setView(fdv);
                return mv;
            }else{//trigger progress monitor and performExport()
                String processLabel = "Exporting ...";
                final String frontbaseUrl = null;
                ProgressMonitorUtil progressUtil = new ProgressMonitorUtil(progressMonitorController);
                if (!progressMonitorController.isMonitorRunning(indexMonitorUuid)) {
                    indexMonitorUuid = progressUtil.registerNewMonitor();
                    Thread subThread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                boolean created = cacheFile.createNewFile();
//                                boolean created = cacheFile.mkdir();
                                if (!created){logger.info("Could not create file");}
                            } catch (Exception e) {
                                logger.info("Could not create file " + e);
                            }
                            performExport(cacheFile, progressMonitorController.getMonitor(indexMonitorUuid),
                                    subtreeUuids, downloadTokenValueId, origin, response);
                        }
                    };
                    if (priority == null) {
                        priority = AbstractController.DEFAULT_BATCH_THREAD_PRIORITY;
                    }
                    subThread.setPriority(priority);
                    subThread.start();
                }
                mv = progressUtil.respondWithMonitorOrDownload(frontbaseUrl, origin, processLabel, indexMonitorUuid, false, request, response);
            }
            return mv;
        }catch(Exception e){
            //TODO: Write an specific documentation for this service endpoint
           Resource resource = resourceLoader.getResource(DWCA_TAX_EXPORT_DOC_RESSOURCE);
           return exportGetExplanation(response, request, resource);
        }
    }



    //=========== Helper Methods ===============//

    /**
     *
     * This private methods finally triggers the export back in the io-package and will create a cache file
     * in system temp directory.
     *
     * @param downloadTokenValueId
     * @param response
     * @param byteArrayOutputStream
     * @param config
     * @param defaultExport
     */
    private void performExport(File cacheFile, IRestServiceProgressMonitor progressMonitor,
            UuidList featureUuids, String downloadTokenValueId, String origin,
            HttpServletResponse response
            ) {

        progressMonitor.subTask("configure export");
        DwcaTaxExportConfigurator config = setDwcaTaxExportConfigurator(cacheFile, progressMonitor, featureUuids);
        @SuppressWarnings("unchecked")
        CdmApplicationAwareDefaultExport<DwcaTaxExportConfigurator> defaultExport =
                (CdmApplicationAwareDefaultExport<DwcaTaxExportConfigurator>)appContext.getBean("defaultExport");
        progressMonitor.subTask("invoke export");
        defaultExport.invoke(config);  //triggers export
        progressMonitor.subTask("wrote results to cache");
        progressMonitor.done();
        progressMonitor.setOrigin(origin);
    }

    /**
     * Cofiguration method to set the configuration details for the defaultExport in the application context.
     * @param cacheFile
     *
     * @param classificationUUID pass-through the selected {@link Classification classification}
     * @param featureUuids pass-through the selected {@link Feature feature} of a {@link Taxon}, in order to fetch it.
     * @param areas
     * @param byteArrayOutputStream pass-through the stream to write out the data later.
     * @param progressMonitor
     * @param conceptExport
     * @param demoExport
     * @return the CsvTaxExportConfiguratorRedlist config
     */
    private DwcaTaxExportConfigurator setDwcaTaxExportConfigurator(File cacheFile, IRestServiceProgressMonitor progressMonitor,
            UuidList subtreeUuids) {

        if(cacheFile == null){
            String destination = System.getProperty("java.io.tmpdir");
            cacheFile = new File(destination);
        }

        DwcaEmlRecord emlRecord = null;
        DwcaTaxExportConfigurator config = DwcaTaxExportConfigurator.NewInstance(
                null, cacheFile, emlRecord);

        Set<UUID> subtreeSet = new HashSet<>(subtreeUuids);
        config.setProgressMonitor(progressMonitor);
        config.setSubtreeUuids(subtreeSet);

//        config.setHasHeaderLines(true);
//        config.setFieldsTerminatedBy("\t");
//        config.setClassificationUuids(classificationUUIDS);

//        if(demoExport == false && conceptExport == false){
//        	config.createPreSelectedExport(false, true);
//        }else{
//        	config.createPreSelectedExport(demoExport, conceptExport);
//        }

        return config;
    }


    /**
     * @param response
     * @param subtreeUuids
     * @throws IOException
     */
    private String makeFileName(HttpServletResponse response, UuidList subtreeUuids) throws IOException {
        String fileName;
        if (subtreeUuids != null && ! subtreeUuids.isEmpty()){
            UUID firstUuid = subtreeUuids.get(0);
            TaxonNode node = taxonNodeService.load(firstUuid, TAXON_NODE_INIT_STRATEGY);
            if (node != null && node.getTaxon() != null){
                if (node.getTaxon().getName() != null){
                    fileName = node.getTaxon().getName().getTitleCache();
                }else{
                    fileName = node.getTaxon().getTitleCache();
                }
            }else if (node != null){
                fileName = node.getClassification().getTitleCache();
            }else{
                Classification classification = classificationService.find(firstUuid);
                if (classification != null){
                    fileName = classification.getTitleCache();
                }else{
                    //handle via repso
                    response.sendError(404, "Subtree uuid does not exist: " + firstUuid);
                    fileName = "Error";
                }
            }
        }else{
            List<Classification> classificationList = classificationService.list(null, 1, null
                    , null, null);
            if (!classificationList.isEmpty()){
                fileName = classificationList.get(0).getTitleCache();
            }else{
               //handle via repso
                response.sendError(404, "No classification found");
                fileName = "Error";
            }
        }


        fileName = fileName + "_" + uuidListToString(subtreeUuids, 40);

        return fileName;

    }

    private String uuidListToString(UuidList uuidList, Integer truncate) {
        String result = null;
        for (UUID uuid : uuidList){
            result = CdmUtils.concat("_", uuid.toString());
        }
        if (result != null && result.length() > truncate){
            result = result.substring(0, truncate);
        }
        return result;
    }

    @Override
    public void setService(IService service) {}

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}
