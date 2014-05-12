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
import java.util.ArrayList;
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
import org.springframework.beans.propertyeditors.UUIDEditor;
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
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.DocUtils;
import eu.etaxonomy.cdm.common.monitor.IRestServiceProgressMonitor;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultExport;
import eu.etaxonomy.cdm.io.csv.redlist.demo.CsvDemoExportConfigurator;
import eu.etaxonomy.cdm.io.csv.redlist.demo.CsvDemoRecord;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.remote.controller.AbstractController;
import eu.etaxonomy.cdm.remote.controller.ProgressMonitorController;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import eu.etaxonomy.cdm.remote.controller.util.ProgressMonitorUtil;
import eu.etaxonomy.cdm.remote.editor.TermBaseListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;
import eu.etaxonomy.cdm.remote.view.FileDownloadView;
import eu.etaxonomy.cdm.remote.view.HtmlView;

/**
 * @author a.oppermann
 * @created 20.09.2012
 * <p>
 *  This controller enables an export of the cdm platform via a
 *  REST request. It is debatable if this a wanted behavior.
 *  For the time being it serves its purpose.
 */
@Controller
@RequestMapping(value = { "/checklist" })
public class ChecklistDemoController extends AbstractController implements ResourceLoaderAware{

    @Autowired
	private ApplicationContext appContext;

	@Autowired
	private ITermService termService;

	@Autowired
	private IClassificationService classificationService;

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



	private static final Logger logger = Logger.getLogger(ChecklistDemoController.class);

	/**
	 * Helper method, which allows to convert strings directly into uuids.
	 *
	 * @param binder Special DataBinder for data binding from web request parameters to JavaBean objects.
	 */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UuidList.class, new UUIDListPropertyEditor());
        binder.registerCustomEditor(NamedArea.class, new TermBaseListPropertyEditor<NamedArea>(termService));
        binder.registerCustomEditor(UUID.class, new UUIDEditor());
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
    @RequestMapping(value = {""}, method = { RequestMethod.GET })
    public ModelAndView exportGetExplanation(HttpServletResponse response,
            HttpServletRequest request) throws IOException{
        ModelAndView mv = new ModelAndView();
        // Read apt documentation file.
        Resource resource = resourceLoader.getResource("classpath:eu/etaxonomy/cdm/doc/remote/apt/checklist-catalogue-default.apt");
        // using input stream as this works for both files in the classes directory
        // as well as files inside jars
        InputStream aptInputStream = resource.getInputStream();
        // Build Html View
        Map<String, String> modelMap = new HashMap<String, String>();
        // Convert Apt to Html
        modelMap.put("html", DocUtils.convertAptToHtml(aptInputStream));
        mv.addAllObjects(modelMap);

        HtmlView hv = new HtmlView();
        mv.setView(hv);
        return mv;
    }

    /**
     *
     * This service endpoint generates a json and xml view of the exported list.
     * It takes advantage of pagination.
     *
     * @param classificationUUID
     * @param pageNumber
     * @param pageSize
     * @param response
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping(value = { "export" }, method = { RequestMethod.GET })
    public ModelAndView doGeneralExport(
            @RequestParam(value = "classification", required = false) String classificationUUID,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletResponse response,
            HttpServletRequest request) throws IOException {

        try{
            if(pageSize == null) {
                pageSize = 20;
            }
            if(pageNumber == null) {
                pageNumber = 0;
            }

            PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
            pagerParams.normalizeAndValidate(response);

            List<CsvDemoRecord> recordList = new ArrayList<CsvDemoRecord>();

            CsvDemoExportConfigurator config = setTaxExportConfigurator(null ,classificationUUID, null, null, null);
            config.setPageSize(pagerParams.getPageSize());
            config.setPageNumber(pagerParams.getPageIndex());
            config.setRecordList(recordList);

            CdmApplicationAwareDefaultExport<?> defaultExport = (CdmApplicationAwareDefaultExport<?>) appContext.getBean("defaultExport");
            defaultExport.invoke(config);

            DefaultPagerImpl<CsvDemoRecord> dpi = new DefaultPagerImpl<CsvDemoRecord>(pagerParams.getPageIndex(), config.getTaxonNodeListSize(), pagerParams.getPageSize(), recordList);
            ModelAndView mv = new ModelAndView();
//            mv.addObject(recordList);f
            mv.addObject(dpi);
            return mv;
        }catch(Exception e){
            return exportGetExplanation(response, request);
        }

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
	@RequestMapping(value = { "exportCSV" }, method = { RequestMethod.GET })
	public synchronized ModelAndView doExportRedlist(
			@RequestParam(value = "features", required = false) final UuidList featureUuids,
			@RequestParam(value = "clearCache", required = false) final boolean clearCache,
			@RequestParam(value = "demoExport", required = false) boolean demoExport,
			@RequestParam(value = "conceptExport", required = false) boolean conceptExport,
			@RequestParam(value = "classification", required = true) final String classificationUUID,
            @RequestParam(value = "area", required = false) final UuidList areas,
			@RequestParam(value = "downloadTokenValueId", required = false) final String downloadTokenValueId,
			@RequestParam(value = "priority", required = false) Integer priority,
			final HttpServletResponse response,
			final HttpServletRequest request) throws Exception {
	    /**
	     * ========================================
	     * progress monitor & new thread for export
	     * ========================================
	     */
        ModelAndView mv = new ModelAndView();
        String fileName = classificationService.find(UUID.fromString(classificationUUID)).getTitleCache();
        final File cacheFile = new File(new File(System.getProperty("java.io.tmpdir")), classificationUUID);
        final String origin = request.getRequestURL().append('?').append(request.getQueryString()).toString();

        Long result = null;
        if(cacheFile.exists()){
            result = System.currentTimeMillis() - cacheFile.lastModified();
        }
        //if file exists return file instantly
        //timestamp older than one day?
        if(clearCache == false && result != null && result < 7*(DAY_IN_MILLIS)){
            logger.info("result of calculation: " + result);
            Map<String, File> modelMap = new HashMap<String, File>();
            modelMap.put("file", cacheFile);
            mv.addAllObjects(modelMap);
            FileDownloadView fdv = new FileDownloadView("text/csv", fileName, "txt", "UTF-8");
            mv.setView(fdv);
            return mv;
        }else{//trigger progress monitor and performExport()
            String processLabel = "Exporting...";
            final String frontbaseUrl = null;
            ProgressMonitorUtil progressUtil = new ProgressMonitorUtil(progressMonitorController);
            if (!progressMonitorController.isMonitorRunning(indexMonitorUuid)) {
                indexMonitorUuid = progressUtil.registerNewMonitor();
                Thread subThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            cacheFile.createNewFile();
                        } catch (IOException e) {
                            logger.info("Could not create file "+ e);
                        }
                        performExport(cacheFile, featureUuids, classificationUUID, areas, downloadTokenValueId, origin, response, progressMonitorController.getMonitor(indexMonitorUuid));
                    }
                };
                if (priority == null) {
                    priority = AbstractController.DEFAULT_BATCH_THREAD_PRIORITY;
                }
                subThread.setPriority(priority);
                subThread.start();
            }
            mv = progressUtil.respondWithMonitorOrDownload(frontbaseUrl, origin, request, response, processLabel, indexMonitorUuid);
        }
        return mv;
	}



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
    private void performExport(File cacheFile, UuidList featureUuids,String classificationUUID, UuidList areas,
            String downloadTokenValueId, String origin, HttpServletResponse response, IRestServiceProgressMonitor progressMonitor) {

        progressMonitor.subTask("configure export");
        CsvDemoExportConfigurator config = setTaxExportConfigurator(cacheFile, classificationUUID, featureUuids, areas, progressMonitor);
        CdmApplicationAwareDefaultExport<?> defaultExport = (CdmApplicationAwareDefaultExport<?>) appContext.getBean("defaultExport");
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
	 * @return the CsvTaxExportConfiguratorRedlist config
	 */
	private CsvDemoExportConfigurator setTaxExportConfigurator(File cacheFile, String classificationUUID, UuidList featureUuids, UuidList areas, IRestServiceProgressMonitor progressMonitor) {

		@SuppressWarnings({ "unchecked", "rawtypes" })
		Set<UUID> classificationUUIDS = new HashSet
		(Arrays.asList(new UUID[] {UUID.fromString(classificationUUID)}));
		if(cacheFile == null){
		    String destination = System.getProperty("java.io.tmpdir");
		    cacheFile = new File(destination);
		}
		List<Feature> features = new ArrayList<Feature>();
		if(featureUuids != null){
			for(UUID uuid : featureUuids) {
				features.add((Feature) termService.find(uuid));
			}
		}
		List<NamedArea> selectedAreas = new ArrayList<NamedArea>();
		if(areas != null){
			for(UUID area:areas){
				logger.info(area);
				selectedAreas.add((NamedArea)termService.find(area));
			}
		}

		CsvDemoExportConfigurator config = CsvDemoExportConfigurator.NewInstance(null, cacheFile);
		config.setDestination(cacheFile);
		config.setProgressMonitor(progressMonitor);
		config.setHasHeaderLines(true);
		config.setFieldsTerminatedBy("\t");
		config.setClassificationUuids(classificationUUIDS);
		config.createPreSelectedExport(false, true);
		if(features != null) {
            config.setFeatures(features);
        }
        config.setNamedAreas(selectedAreas);
		return config;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.AbstractController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Override
	public void setService(IService service) {
		// TODO Auto-generated method stub

	}

    /* (non-Javadoc)
     * @see org.springframework.context.ResourceLoaderAware#setResourceLoader(org.springframework.core.io.ResourceLoader)
     */
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}