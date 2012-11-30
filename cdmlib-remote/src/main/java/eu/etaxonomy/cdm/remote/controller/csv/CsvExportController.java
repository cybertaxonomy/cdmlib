/**
 * Copyright (C) 2012 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.controller.csv;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletOutputStream;
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

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultExport;
import eu.etaxonomy.cdm.io.csv.redlist.out.CsvTaxExportConfiguratorRedlist;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.remote.editor.UUIDListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;

/**
 * @author a.oppermann
 * @created 20.09.2012
 *
 */
@Controller
@RequestMapping(value = { "/csv" })
public class CsvExportController{

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private ITermService termService;

    private static final Logger logger = Logger.getLogger(CsvExportController.class);

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UuidList.class, new UUIDListPropertyEditor());
        binder.registerCustomEditor(UUID.class, new UUIDEditor());
    }

    @RequestMapping(value = { "exportRedlist" }, method = { RequestMethod.POST })
    public void doExportRedlist(
            @RequestParam(value = "features", required = false) UuidList featureUuids,
            @RequestParam(value = "combobox", required = false) String classificationUUID,
            HttpServletResponse response) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        CsvTaxExportConfiguratorRedlist config = setTaxExportConfigurator(classificationUUID, featureUuids, byteArrayOutputStream);
        CdmApplicationAwareDefaultExport<?> defaultExport = (CdmApplicationAwareDefaultExport<?>) appContext.getBean("defaultExport");
        logger.info("Start export...");
        defaultExport.invoke(config);
        try {
            /*
             *  Fetch data from the appContext and forward stream to HttpServleResponse
             *
             *  FIXME: Large Data could be out of memory
             *
             *  HTPP Error Break
             */
            ByteArrayInputStream bais = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            InputStreamReader isr = new InputStreamReader(bais, "UTF-8");
            ServletOutputStream sos = response.getOutputStream();
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=\""+config.getClassificationTitleCache()+".txt\"");

            int i;
            while((i = isr.read())!= -1){
                sos.write(i);
            }
            byteArrayOutputStream.flush();
            isr.close();
            byteArrayOutputStream.close();
            sos.flush();
            sos.close();

        } catch (Exception e) {
            logger.error("error generating feed", e);
        }
    }

    private CsvTaxExportConfiguratorRedlist setTaxExportConfigurator(String classificationUUID, UuidList featureUuids, ByteArrayOutputStream byteArrayOutputStream) {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        Set<UUID> classificationUUIDS = new HashSet
        (Arrays.asList(new UUID[] {UUID.fromString(classificationUUID)}));
        String destination = System.getProperty("java.io.tmpdir");
        List<Feature> features = new ArrayList<Feature>();
        if(featureUuids != null){
            for(UUID uuid : featureUuids) {
                features.add((Feature) termService.find(uuid));
            }
        }

        CsvTaxExportConfiguratorRedlist config = CsvTaxExportConfiguratorRedlist.NewInstance(null, new File(destination));
        config.setHasHeaderLines(true);
        config.setFieldsTerminatedBy("\t");
        config.setClassificationUuids(classificationUUIDS);
        config.setByteArrayOutputStream(byteArrayOutputStream);
        if(features != null)config.setFeatures(features);
        return config;
    }
}