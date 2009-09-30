/**
 * Copyright (C) 2008 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.app.jaxb;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.app.util.TestDatabase;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultExport;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.jaxb.JaxbExportConfigurator;
import eu.etaxonomy.cdm.io.jaxb.JaxbImportConfigurator;

/**
 * @author a.babadshanjan
 * @created 25.09.2008
 * @version 1.0
 */
public class CdmExportImportActivator {

	/* SerializeFrom DB **/
	private static final ICdmDataSource source = CdmDestinations.cdm_test_jaxb2();
	private static final ICdmDataSource destination = CdmDestinations.cdm_test_jaxb();

	// Import:
	private static String importFileName = 
		"file:/C:/Dokumente%20und%20Einstellungen/k.luther/Eigene%20Dateien/Neuer%20Ordner/cdmlib/cdmlib-io/target/classes/schema/cdm/export_test_app_import.xml";
    // Export:
	private static String exportFileName = 
		"C:\\Dokumente und Einstellungen\\k.luther\\Eigene Dateien\\archive\\export_test_app_import.xml";

	/** NUMBER_ROWS_TO_RETRIEVE = 0 is the default case to retrieve all rows.
	 * For testing purposes: If NUMBER_ROWS_TO_RETRIEVE >0 then retrieve 
	 *  as many rows as specified for agents, references, etc. 
	 *  Only root taxa and no synonyms and relationships are retrieved. */
	private static final int NUMBER_ROWS_TO_RETRIEVE = 0;

	private static final Logger logger = Logger.getLogger(CdmExportImportActivator.class);

	private void invokeExport() {
		
		JaxbExportConfigurator jaxbExportConfigurator = 
			JaxbExportConfigurator.NewInstance(source, exportFileName);
		
		CdmDefaultExport<JaxbExportConfigurator> jaxbExport = 
			new CdmDefaultExport<JaxbExportConfigurator>();
		
//		jaxbExportConfigurator.setSource(sourceDb);
//		jaxbExportConfigurator.setDestination(exportFileName);
//		jaxbExportConfigurator.setDbSchemaValidation(DbSchemaValidation.UPDATE);

//		jaxbExportConfigurator.setMaxRows(NUMBER_ROWS_TO_RETRIEVE);
//
//		jaxbExportConfigurator.setDoAuthors(doAgents);
//		jaxbExportConfigurator.setDoAgentData(doAgentData);
//		jaxbExportConfigurator.setDoLanguageData(doLanguageData);
//		jaxbExportConfigurator.setDoFeatureData(doFeatureData);
//		jaxbExportConfigurator.setDoDescriptions(doDescriptions);
//		jaxbExportConfigurator.setDoMedia(doMedia);
//		jaxbExportConfigurator.setDoOccurrence(doOccurrences);
//		jaxbExportConfigurator.setDoReferences(doReferences);
//		jaxbExportConfigurator.setDoReferencedEntities(doReferencedEntities);
//		jaxbExportConfigurator.setDoRelTaxa(doRelationships);
//		jaxbExportConfigurator.setDoSynonyms(doSynonyms);
//		jaxbExportConfigurator.setDoTaxonNames(doTaxonNames);
//		jaxbExportConfigurator.setDoTaxa(doTaxa);
//		jaxbExportConfigurator.setDoTerms(doTerms);
//		jaxbExportConfigurator.setDoTermVocabularies(doTermVocabularies);
//		jaxbExportConfigurator.setDoHomotypicalGroups(doHomotypicalGroups);

		// invoke export
		logger.debug("Invoking Jaxb export");
		jaxbExport.invoke(jaxbExportConfigurator);

	}


	private void invokeImport() {

		JaxbImportConfigurator jaxbImportConfigurator = 
			JaxbImportConfigurator.NewInstance(importFileName, destination);

		CdmDefaultImport<JaxbImportConfigurator> jaxbImport = 
			new CdmDefaultImport<JaxbImportConfigurator>();
		
//		jaxbImportConfigurator.setSource(importFileName);
//		jaxbImportConfigurator.setDestination(destinationDb);
//		jaxbImportConfigurator.setDbSchemaValidation(DbSchemaValidation.CREATE);

//		jaxbImportConfigurator.setMaxRows(NUMBER_ROWS_TO_RETRIEVE);
//
//		jaxbImportConfigurator.setDoAuthors(doAgents);
//		jaxbImportConfigurator.setDoAgentData(doAgentData);
//		jaxbImportConfigurator.setDoLanguageData(doLanguageData);
//		jaxbImportConfigurator.setDoFeatureData(doFeatureData);
//		jaxbImportConfigurator.setDoDescriptions(doDescriptions);
//		jaxbImportConfigurator.setDoMedia(doMedia);
//		jaxbImportConfigurator.setDoOccurrence(doOccurrences);
//		jaxbImportConfigurator.setDoReferences(doReferences);
//		jaxbImportConfigurator.setDoReferencedEntities(doReferencedEntities);
//		jaxbImportConfigurator.setDoRelTaxa(doRelationships);
//		jaxbImportConfigurator.setDoSynonyms(doSynonyms);
//		jaxbImportConfigurator.setDoTaxonNames(doTaxonNames);
//		jaxbImportConfigurator.setDoTaxa(doTaxa);
//		jaxbImportConfigurator.setDoTerms(false);
//		jaxbImportConfigurator.setDoTermVocabularies(doTermVocabularies);
//		jaxbImportConfigurator.setDoHomotypicalGroups(doHomotypicalGroups);

		// invoke import
		logger.debug("Invoking Jaxb import");
//		CdmImporter cdmImporter = new CdmImporter();
//		cdmImporter.invoke(jaxbImportConfigurator, null);
		jaxbImport.invoke(jaxbImportConfigurator, destination, true);

	}

	
	private CdmApplicationController initDb(ICdmDataSource db) {

		// Init source DB
		CdmApplicationController appCtrInit = null;

		appCtrInit = TestDatabase.initDb(db, DbSchemaValidation.CREATE, true);

		return appCtrInit;
	}

	
	// Load test data to DB
	private void loadTestData(CdmApplicationController appCtrInit) {

		TestDatabase.loadTestData("", appCtrInit);
	}


	/**c
	 * @param args
	 */
	public static void main(String[] args) {

		CdmExportImportActivator sc = new CdmExportImportActivator();

		CdmApplicationController appCtr = null;
		//appCtr = sc.initDb(source);
		appCtr = sc.initDb(destination);
		
		//sc.loadTestData(appCtr);
		
		//sc.invokeExport();
		sc.invokeImport();
	}

}
