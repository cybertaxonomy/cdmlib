/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller.checklist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.UUIDEditor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.config.MatchingTaxonConfigurator;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultExport;
import eu.etaxonomy.cdm.io.csv.redlist.out.CsvTaxExportConfiguratorRedlist;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.remote.controller.AbstractController;
import eu.etaxonomy.cdm.remote.controller.ProgressMonitorController;
import eu.etaxonomy.cdm.remote.editor.UUIDListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;

/**
 * @author a.oppermann
 * @since 20.09.2012
 *
 */
@Controller
@RequestMapping(value = { "/csv" })
public class CsvExportController extends AbstractController{

	@Autowired
	private ApplicationContext appContext;

	@Autowired
	private ITermService termService;

	@Autowired
	private IClassificationService classificationService;

	@Autowired
	private ITaxonService taxonService;

	@Autowired
	public ProgressMonitorController progressMonitorController;

	private static final Logger logger = Logger.getLogger(CsvExportController.class);


    private static final List<String> TAXON_WITH_NODES_INIT_STRATEGY = Arrays.asList(new String []{
            "taxonNodes.$",
            "taxonNodes.classification.$",
            "taxonNodes.childNodes.$"
            });

    private static final List<String> CLASSIFICATION_INIT_STRATEGY =
            Arrays.asList(new String[]{"rootNode"});

	/**
	 * Helper method, which allows to convert strings directly into uuids.
	 *
	 * @param binder Special DataBinder for data binding from web request parameters to JavaBean objects.
	 */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UuidList.class, new UUIDListPropertyEditor());
//        binder.registerCustomEditor(NamedArea.class, new NamedAreaPropertyEditor());
        binder.registerCustomEditor(UUID.class, new UUIDEditor());
    }

    /**
     * Fetches data from the application context and forwards the stream to the HttpServletResponse,
     * which offers a file download.
     *
     * @param featureUuids List of uuids to download/select {@link Feature feature}features
     * @param taxonName the selected taxon name
     * @param classificationUuid the uuid of the selected classification
     * @param response HttpServletResponse which returns the ByteArrayOutputStream
     */
	@RequestMapping(value = { "exportRedlist" }, method = { RequestMethod.POST })
	public void doExportRedlist(
			@RequestParam(value = "features", required = false) UuidList featureUuids,
			@RequestParam(value = "classificationUuid", required = false) String classificationUuid,
			@RequestParam(value = "taxonName", required = false) String taxonName,
            @RequestParam(value = "area", required = false) UuidList areas,
			@RequestParam(value = "downloadTokenValueId", required = false) String downloadTokenValueId,
			HttpServletResponse response,
			HttpServletRequest request) {

	    boolean includeUnpublished = NO_UNPUBLISHED;
		Classification classification = classificationService.load(UUID.fromString(classificationUuid), CLASSIFICATION_INIT_STRATEGY);
		UUID taxonNodeUuid = classification.getRootNode().getUuid();

		if(CdmUtils.isNotBlank(taxonName)){
			MatchingTaxonConfigurator config = new MatchingTaxonConfigurator();
			config.setIncludeUnpublished(includeUnpublished);
			config.setClassificationUuid(UUID.fromString(classificationUuid));
			config.setTaxonNameTitle(taxonName);

			List<TaxonBase> taxaByName = taxonService.findTaxaByName(config);
			for (TaxonBase<?> taxonBase : taxaByName) {
			    if(taxonBase.isInstanceOf(Taxon.class)){
			        TaxonNode taxonNode = classification.getNode(HibernateProxyHelper.deproxy(
			                taxonService.load(taxonBase.getUuid(),TAXON_WITH_NODES_INIT_STRATEGY), Taxon.class));
			        if(taxonNode!=null){
			            taxonNodeUuid = taxonNode.getUuid();
			            break;
			        }
			    }
			}
		}

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		CsvTaxExportConfiguratorRedlist config = setTaxExportConfigurator(taxonNodeUuid, featureUuids, areas, byteArrayOutputStream);
		CdmApplicationAwareDefaultExport<CsvTaxExportConfiguratorRedlist> defaultExport =
		        (CdmApplicationAwareDefaultExport<CsvTaxExportConfiguratorRedlist>) appContext.getBean("defaultExport");
		logger.info("Start export...");
		logger.info("doExportRedlist()" + requestPathAndQuery(request));
		defaultExport.invoke(config);
		try {
			/*
			 *  Fetch data from the appContext and forward stream to HttpServleResponse
			 *
			 *  FIXME: Large Data could be out of memory
			 *
			 *  HTPP Error Break
			 */
			ByteArrayInputStream bais = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());//byteArrayOutputStream.toByteArray()
			InputStreamReader isr = new InputStreamReader(bais);
			Cookie progressCookie = new Cookie("fileDownloadToken", downloadTokenValueId);
			progressCookie.setPath("/");
			progressCookie.setMaxAge(60);
			response.addCookie(progressCookie);
			response.setContentType("text/csv; charset=utf-8");
			response.setHeader("Content-Disposition", "attachment; filename=\""+config.getClassificationTitleCache()+".txt\"");
			PrintWriter printWriter = response.getWriter();

			int i;
			while((i = isr.read())!= -1){
				printWriter.write(i);
			}
			byteArrayOutputStream.flush();
			isr.close();
			byteArrayOutputStream.close();
			printWriter.flush();
			printWriter.close();
		} catch (Exception e) {
			logger.error("error generating feed", e);
		}
	}

	/**
	 * Cofiguration method to set the configuration details for the defaultExport in the application context.
	 *
	 * @param taxonNodeUuid pass-through the selected {@link Classification classification}
	 * @param featureUuids pass-through the selected {@link Feature feature} of a {@link Taxon}, in order to fetch it.
	 * @param areas
	 * @param byteArrayOutputStream pass-through the stream to write out the data later.
	 * @return the CsvTaxExportConfiguratorRedlist config
	 */
	private CsvTaxExportConfiguratorRedlist setTaxExportConfigurator(UUID taxonNodeUuid, UuidList featureUuids, UuidList areas, ByteArrayOutputStream byteArrayOutputStream) {

		Set<UUID> taxonNodeUuids = Collections.singleton(taxonNodeUuid);
		String destination = System.getProperty("java.io.tmpdir");
		List<Feature> features = new ArrayList<>();
		if(featureUuids != null){
			for(UUID uuid : featureUuids) {
				features.add((Feature) termService.find(uuid));
			}
		}
		List<NamedArea> selectedAreas = new ArrayList<>();
		if(areas != null){
			for(UUID area:areas){
				logger.info(area);
				selectedAreas.add((NamedArea)termService.find(area));
			}
		}

		CsvTaxExportConfiguratorRedlist config = CsvTaxExportConfiguratorRedlist.NewInstance(null, new File(destination));
		config.setHasHeaderLines(true);
		config.setFieldsTerminatedBy("\t");
		config.setTaxonNodeUuids(taxonNodeUuids);
		config.setByteArrayOutputStream(byteArrayOutputStream);
		config.setFeatures(features);
        config.setNamedAreas(selectedAreas);
		return config;
	}

	@Override
	public void setService(IService service) {}

}
