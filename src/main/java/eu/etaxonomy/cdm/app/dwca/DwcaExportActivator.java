/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.dwca;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultExport;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.dwca.out.DwcaTaxExportConfigurator;


/**
 *
 * @author a.mueller
 *
 */
public class DwcaExportActivator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaExportActivator.class);

	//database validation status (create, update, validate ...)
	private static final String fileDestination = "/tmp/Bloup/";
//	private static final ICdmDataSource cdmSource = CdmDestinations.cdm_local_cichorieae();
//	private static final ICdmDataSource cdmSource = CdmDestinations.cdm_test_local_mysql();
//	private static final ICdmDataSource cdmSource = CdmDestinations.cdm_production_cichorieae();
//	private static final ICdmDataSource cdmSource = CdmDestinations.cdm_flora_central_africa_production();
//	private static final ICdmDataSource cdmSource = CdmDestinations.cdm_cyprus_production();

	private final IProgressMonitor monitor = DefaultProgressMonitor.NewInstance();

	private static DateTime dateTime = new DateTime();
	private static String date = dateTime.getYear() + "-" + dateTime.getMonthOfYear() + "-" + dateTime.getDayOfMonth();

//	private static final String defaultBibliographicCitation = "ICN (Hand, R., Kilian, N. & Raab-Straube, E. von; general editors) 2009+ (continuously updated): International Cichorieae Network: Cichorieae Portal. Published on the Internet at http://wp6-cichorieae.e-taxonomy.eu/portal/; " +
//		"accessed ["+date+"].";

	private static final String taxonSourceDefault = "http://localhost/drupal7/?q=cdm_dataportal/taxon/{id}";

	//check - import
	private static final CHECK check = CHECK.EXPORT_WITHOUT_CHECK;

	private static List<UUID> featureExclusions = Arrays.asList(new UUID[]{
			UUID.fromString("5deff505-1a32-4817-9a74-50e6936fd630"),   //occurrences
			UUID.fromString("8075074c-ace8-496b-ac82-47c14553f7fd"),    //Editor_Parenthesis
			UUID.fromString("c0cc5ebe-1f0c-4c31-af53-d486858ea415"),   //Image Sources
			UUID.fromString("9f6c551d-0f19-45ea-a855-4946f6fc1093"),		//Credits
			UUID.fromString("cbf12c6c-94e6-4724-9c48-0f6f10d83e1c"),   //Editor Brackets
			UUID.fromString("0508114d-4158-48b5-9100-369fa75120d3")     //inedited
	});


// ****************** ALL *****************************************

//	private boolean doTaxa = true;
//	private boolean doResourceRelation = true;
//	private boolean doTypesAndSpecimen = true;
//	private boolean doVernacularNames = true;
//	private boolean doReferences = true;
//	private boolean doDescription = true;
//	private boolean doDistributions = true;
//	private boolean doImages = false;
//	private boolean doMetaData = true;
//	private boolean doEml = true;

// ************************ NONE **************************************** //

	private final boolean doTaxa = true;
	private final boolean doResourceRelation = false;
	private final boolean doTypesAndSpecimen = true;
	private final boolean doVernacularNames = false;
	private final boolean doReferences = false;
	private final boolean doDescription = false;
	private final boolean doDistributions = true;
	private final boolean doImages = false;
	private final boolean doMetaData = true;
	private final boolean doEml = false;

	public boolean 	doExport(ICdmDataSource source){
		System.out.println("Start export to DWC-A ("+ fileDestination + ") ...");

//		CdmUpdater su = CdmUpdater.NewInstance();
//		IProgressMonitor monitor = DefaultProgressMonitor.NewInstance();
//
//		try {
//			su.updateToCurrentVersion(source, monitor);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		if (true){
//			return true;
//		}

		//make file destination
		String destination = fileDestination;
//		DwcaEmlRecord emlRecord = getEmlRecord();

//		DwcaTaxExportConfigurator config = DwcaTaxExportConfigurator.NewInstance(source, new File(destination), emlRecord);
		DwcaTaxExportConfigurator config = DwcaTaxExportConfigurator.NewInstance(source, new File(destination), null);

		config.setDoTaxa(doTaxa);
		config.setDoResourceRelation(doResourceRelation);
		config.setDoTypesAndSpecimen(doTypesAndSpecimen);
		config.setDoVernacularNames(doVernacularNames);
		config.setDoReferences(doReferences);
		config.setDoDescription(doDescription);
		config.setDoDistributions(doDistributions);
		config.setDoImages(doImages);
		config.setDoMetaData(doMetaData);
		config.setDoEml(doEml);
		config.setCheck(check);
		config.setProgressMonitor(monitor);
//		config.setDefaultBibliographicCitation(defaultBibliographicCitation);
		config.setDefaultTaxonSource(taxonSourceDefault);
		monitor.beginTask("DwcA-Export", 10);

//		config.setFeatureExclusions(featureExclusions);
		// invoke import
		CdmDefaultExport<DwcaTaxExportConfigurator> bmExport = new CdmDefaultExport<DwcaTaxExportConfigurator>();
		boolean result = bmExport.invoke(config);

		System.out.println("End export to DWC-A ("+ fileDestination + ")..." + (result? "(successful)":"(with errors)"));
		return result;
	}


//	private DwcaEmlRecord getEmlRecord() {
//		DwcaEmlRecord emlRecord = new DwcaEmlRecord();
//		emlRecord.setIdentifier("My Identifier");
//		emlRecord.setTitle("Flora of cyprus");
//		emlRecord.setPublicationDate(new DateTime());
//		emlRecord.setExpectedCitation("Expected Citation");
//		emlRecord.setAbstractInfo("The abstract");
//		emlRecord.setAdditionalInformation("Add info");
//		emlRecord.setResourceLanguage(null);
//		emlRecord.setResourceUrl(URI.create("http://www.flora-of-cyprus.eu//portal/"));
//		emlRecord.setMetaDataLanguage(null);
//		emlRecord.setResourceLogoUri(null);
//		emlRecord.setCreativeCommonsLicensing(null);
//		emlRecord.setProjectTitle("Projekt tit");
//		emlRecord.setProjectLead("Proj Lead");
//		emlRecord.setProjectDescription("Proj Desc");
//
//		Person person = Person.NewInstance();
//		Institution institution = Institution.NewInstance();
//
//		InstitutionalMembership m = person.addInstitutionalMembership(institution, null, null, null);
//		emlRecord.setResourceCreator(m);
//
//		return emlRecord;
//	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DwcaExportActivator ex = new DwcaExportActivator();
		ICdmDataSource source = CdmDestinations.proibiosphere_production();

		ex.doExport(source);
	}




}
