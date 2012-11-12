/**
 * Copyright (C) 2012 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.controller.dwca;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultExport;
import eu.etaxonomy.cdm.io.dwca.redlist.out.DwcaTaxExportConfiguratorRedlist;

/**
 * @author a.oppermann
 * @created 20.09.2012
 * 
 */
@Controller
@RequestMapping(value = { "/dwca" })
public class DwcaExportController{

	@Autowired
	private ApplicationContext appContext;
	private static final Logger logger = Logger.getLogger(DwcaExportController.class);

	@RequestMapping(value = { "getDB" }, method = { RequestMethod.POST })
	public void doExport(
			@RequestParam(value = "dlOptions", required = false) String[] options,
			@RequestParam(value = "combobox", required = false) String classificationUUID,
			HttpServletResponse response) {

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DwcaTaxExportConfiguratorRedlist config = setTaxExportConfigurator(classificationUUID, options, byteArrayOutputStream);
		CdmApplicationAwareDefaultExport<?> defaultExport = (CdmApplicationAwareDefaultExport<?>) appContext.getBean("defaultExport");
		logger.info("Start export...");
		defaultExport.invoke(config);
		try {
			/*
			 *  Fetch data from the appContext and forward stream to HttpServleResponse
			 *  
			 *  FIXME: Large Data could be out of memory
			 */
			ByteArrayInputStream bais = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
			InputStreamReader isr = new InputStreamReader(bais, "UTF-8");
			ServletOutputStream sos = response.getOutputStream();
			response.setContentType("text/csv");
			response.setHeader("Content-Disposition", "attachment; filename=\"RedlistCoreTax.txt\"");

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

	private DwcaTaxExportConfiguratorRedlist setTaxExportConfigurator(String classificationUUID, String[] options, ByteArrayOutputStream byteArrayOutputStream) {

		boolean doRlStatus2013 = false;
		boolean doRlStatus1996 = false;		

		@SuppressWarnings({ "unchecked", "rawtypes" })
		Set<UUID> classificationUUIDS = new HashSet
		(Arrays.asList(new UUID[] {UUID.fromString(classificationUUID)}));
		String destination = System.getProperty("java.io.tmpdir");

		if (options != null && options.length != 0) {
			logger.info("set individual configurations for export...");
			for (String option : options) {
				logger.info("... "+option);
				if(option.equals("setRl1996")) doRlStatus1996 = true;
				if(option.equals("setRl2013")) doRlStatus2013 = true;
				//if(option.equals("setDoDistributions"))doDistributions=true;
			}
		}else {
			logger.info("set standard configurations for export...");
		}
		DwcaTaxExportConfiguratorRedlist config = DwcaTaxExportConfiguratorRedlist.NewInstance(null, new File(destination));
		config.setHasHeaderLines(true);
		config.setFieldsTerminatedBy("\t");
		config.setClassificationUuids(classificationUUIDS);
		config.setByteArrayOutputStream(byteArrayOutputStream);
		config.setIncludeRl1996(doRlStatus1996);
		config.setIncludeRl2013(doRlStatus2013);
		return config;
	}




}