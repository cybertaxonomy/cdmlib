/**
 * Copyright (C) 2012 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.controller.dwca;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultExport;
import eu.etaxonomy.cdm.io.dwca.out.DwcaEmlRecord;

import eu.etaxonomy.cdm.io.dwca.redlist.out.DwcaTaxExportConfiguratorRedlist;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;

//import eu.etaxonomy.cdm.app.common.CdmDestinations;

/**
 * @author a.oppermann
 * @created 20.09.2012
 * 
 */
@Controller
@RequestMapping(value = { "/dwca" })
public class DwcaExportController {

	@Autowired
	private ApplicationContext appContext;

	private static final Logger logger = Logger
			.getLogger(DwcaExportController.class);

	@RequestMapping(value = { "getDB" }, method = { RequestMethod.POST })
	public void doExport(
			@RequestParam(value = "dlOptions", required = false) String options[],
			@RequestParam(value = "combobox", required = false) String classificationUUID,
			HttpServletResponse response) {
		
		DwcaTaxExportConfiguratorRedlist config = setTaxExportConfigurator(classificationUUID, options);
		logger.info("Selected classification: " +classificationUUID);
		CdmApplicationAwareDefaultExport<?> defaultExport = (CdmApplicationAwareDefaultExport<?>) appContext.getBean("defaultExport");
		logger.info("Start export...");
		defaultExport.invoke(config);
		try {
			response.sendRedirect("http://localhost:8080/dwca/success.jsp");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.warn("Redirect failed " + e);
		}

	}

	
	
	
	
	
	

	private DwcaTaxExportConfiguratorRedlist setTaxExportConfigurator(String classificationUUID, String options[]) {

		boolean doTaxa = false;
		boolean doResourceRelation = false;
		boolean doTypesAndSpecimen = false;
		boolean doVernacularNames = false;
		boolean doReferences = false;
		boolean doDescription = false;
		boolean doDistributions = false;
		boolean doImages = false;
		boolean doMetaData = false;
		boolean doEml = false;
		
		@SuppressWarnings("unchecked")
		Set<UUID> classificationUUIDS = new HashSet
				(Arrays.asList(new UUID[] {UUID.fromString(classificationUUID)}));
		
		List<UUID> featureExclusions = Arrays.asList(new UUID[] {
				UUID.fromString("5deff505-1a32-4817-9a74-50e6936fd630"), // occurrences
				UUID.fromString("8075074c-ace8-496b-ac82-47c14553f7fd"), // Editor_Parenthesis
				UUID.fromString("c0cc5ebe-1f0c-4c31-af53-d486858ea415"), // Image
				// Sources
				UUID.fromString("9f6c551d-0f19-45ea-a855-4946f6fc1093"), // Credits
				UUID.fromString("cbf12c6c-94e6-4724-9c48-0f6f10d83e1c"), // Editor
				// Brackets
				UUID.fromString("0508114d-4158-48b5-9100-369fa75120d3") // inedited
				});

		String destination = System.getProperty("java.io.tmpdir");
		DwcaEmlRecord emlRecord = getEmlRecord();

		if (options != null && options.length != 0) {
			logger.info("set individual configurations for export...");
			// out.println("You have selected: <br>");
			for (String option : options) {
				logger.info("... "+option);
				if(option.equals("setDoTaxa")) doTaxa = true;
				if(option.equals("setDoResourceRelation")) doResourceRelation= true;
				if(option.equals("setDoTypesAndSpecimen")) doTypesAndSpecimen = true;
				if(option.equals("setDoVernacularNames")) doVernacularNames=true;
				if(option.equals("setDoReferences")) doReferences=true;
				if(option.equals("setDdoDescription")) doDescription=true;
				if(option.equals("setDoDistributions"))doDistributions=true;
				if(option.equals("setDoImages"))doImages=true;
				if(option.equals("setDoMetaData"))doMetaData=true;
				if(option.equals("setDoEml"))doEml=true;
			}
			
		}else {
			logger.info("set standard configurations for export...");
		}
		DwcaTaxExportConfiguratorRedlist config = DwcaTaxExportConfiguratorRedlist
				.NewInstance(null, new File(destination), emlRecord);
		config.setHasHeaderLines(true);
		config.setFieldsTerminatedBy("\t");
		config.setFieldsEnclosedBy(",");
		config.setDoTaxa(doTaxa);
//		config.setDoResourceRelation(doResourceRelation);
//		config.setDoTypesAndSpecimen(doTypesAndSpecimen);
//		config.setDoVernacularNames(doVernacularNames);
//		config.setDoReferences(doReferences);
//		config.setDoDescription(doDescription);
		config.setDoDistributions(doDistributions);
//		config.setDoImages(doImages);
//		config.setDoMetaData(doMetaData);
		config.setDoEml(doEml);
		// config.setCheck(true);
		// config.setProgressMonitor(true);
		// config.setDefaultBibliographicCitation(defaultBibliographicCitation);
		config.setDefaultTaxonSource("http://wp6-cichorieae.e-taxonomy.eu/portal/?q=cdm_dataportal/taxon/{id}");
		// monitor.beginTask("DwcA-Export", 10);
		config.setFeatureExclusions(featureExclusions);
		config.setClassificationUuids(classificationUUIDS);

		return config;

	}

	

	private DwcaEmlRecord getEmlRecord() {
		DwcaEmlRecord emlRecord = new DwcaEmlRecord();
		emlRecord.setIdentifier("My Identifier");
		emlRecord.setTitle("Flora of cyprus");
		emlRecord.setPublicationDate(new DateTime());
		emlRecord.setExpectedCitation("Expected Citation");
		emlRecord.setAbstractInfo("The abstract");
		emlRecord.setAdditionalInformation("Add info");
		emlRecord.setResourceLanguage(null);
		emlRecord.setResourceUrl(URI
				.create("http://www.flora-of-cyprus.eu//portal/"));
		emlRecord.setMetaDataLanguage(null);
		emlRecord.setResourceLogoUri(null);
		emlRecord.setCreativeCommonsLicensing(null);
		emlRecord.setProjectTitle("Projekt tit");
		emlRecord.setProjectLead("Proj Lead");
		emlRecord.setProjectDescription("Proj Desc");

		Person person = Person.NewInstance();
		Institution institution = Institution.NewInstance();

		InstitutionalMembership m = person.addInstitutionalMembership(
				institution, null, null, null);
		emlRecord.setResourceCreator(m);

		return emlRecord;
	}
}