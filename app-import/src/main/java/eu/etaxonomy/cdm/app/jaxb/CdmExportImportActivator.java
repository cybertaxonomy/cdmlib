/**
 * Copyright (C) 2008 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 */

package eu.etaxonomy.cdm.app.jaxb;

import java.io.File;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.util.TestDatabase;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultExport;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.jaxb.CdmExporter;
import eu.etaxonomy.cdm.io.jaxb.CdmImporter;
import eu.etaxonomy.cdm.io.jaxb.JaxbExportConfigurator;
import eu.etaxonomy.cdm.io.jaxb.JaxbImportConfigurator;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.babadshanjan
 * @created 25.09.2008
 */
public class CdmExportImportActivator {

	/* SerializeFrom DB **/
	private static final String sourceDbName = "cdm_test_jaxb";
	private static final String destinationDbName = "cdm_test_jaxb2";

	/** NUMBER_ROWS_TO_RETRIEVE = 0 is the default case to retrieve all rows.
	 * For testing purposes: If NUMBER_ROWS_TO_RETRIEVE >0 then retrieve 
	 *  as many rows as specified for agents, references, etc. 
	 *  Only root taxa and no synonyms and relationships are retrieved. */
	private static final int NUMBER_ROWS_TO_RETRIEVE = 0;

	private static final String server = "192.168.2.10";
	private static final String username = "edit";

	public static ICdmDataSource CDM_DB(String dbname) {

		logger.info("Setting DB " + dbname);
		String password = AccountStore.readOrStorePassword(dbname, server, username, null);
		ICdmDataSource datasource = CdmDataSource.NewMySqlInstance(server, dbname, username, password);
		return datasource;
	}

	private static final Logger logger = Logger.getLogger(CdmExportImportActivator.class);

	private static final ICdmDataSource sourceDb = CdmExportImportActivator.CDM_DB(sourceDbName);
	private static final ICdmDataSource destinationDb = CdmExportImportActivator.CDM_DB(destinationDbName);

	private static boolean doAgents = true;
	private static boolean doAgentData = true;
	private static boolean doLanguageData = true;
	private static boolean doFeatureData = true;
	private static boolean doDescriptions = true;
	private static boolean doMedia = true;
	private static boolean doOccurrences = true;
	//private static boolean doReferences = true;
	private static final DO_REFERENCES doReferences =  DO_REFERENCES.ALL;
    private static boolean doReferencedEntities = true;
	private static boolean doRelationships = true;
	private static boolean doSynonyms = true;
	private static boolean doTaxonNames = true;
	private static boolean doTaxa = true;
	private static boolean doTerms = true;
	private static boolean doTermVocabularies = true;
	private static boolean doHomotypicalGroups = true;

	private String fileName = new String(System.getProperty("user.home") + File.separator + "cdm_test_jaxb_marshalled.xml");


	private void invokeExport() {
		
		JaxbExportConfigurator jaxbExportConfigurator = 
			JaxbExportConfigurator.NewInstance(destinationDb, fileName);
		
		CdmDefaultExport<JaxbExportConfigurator> jaxbExport = 
			new CdmDefaultExport<JaxbExportConfigurator>();
		
//		jaxbExportConfigurator.setSource(sourceDb);
//		jaxbExportConfigurator.setDestination(fileName);
		jaxbExportConfigurator.setDbSchemaValidation(DbSchemaValidation.VALIDATE);

		jaxbExportConfigurator.setMaxRows(NUMBER_ROWS_TO_RETRIEVE);

		jaxbExportConfigurator.setDoAuthors(doAgents);
		jaxbExportConfigurator.setDoAgentData(doAgentData);
		jaxbExportConfigurator.setDoLanguageData(doLanguageData);
		jaxbExportConfigurator.setDoFeatureData(doFeatureData);
		jaxbExportConfigurator.setDoDescriptions(doDescriptions);
		jaxbExportConfigurator.setDoMedia(doMedia);
		jaxbExportConfigurator.setDoOccurrence(doOccurrences);
		jaxbExportConfigurator.setDoReferences(doReferences);
		jaxbExportConfigurator.setDoReferencedEntities(doReferencedEntities);
		jaxbExportConfigurator.setDoRelTaxa(doRelationships);
		jaxbExportConfigurator.setDoSynonyms(doSynonyms);
		jaxbExportConfigurator.setDoTaxonNames(doTaxonNames);
		jaxbExportConfigurator.setDoTaxa(doTaxa);
		jaxbExportConfigurator.setDoTerms(doTerms);
		jaxbExportConfigurator.setDoTermVocabularies(doTermVocabularies);
		jaxbExportConfigurator.setDoHomotypicalGroups(doHomotypicalGroups);

		// invoke export
		logger.debug("Invoking Jaxb export");
		jaxbExport.invoke(jaxbExportConfigurator);

	}


	private void invokeImport() {

		JaxbImportConfigurator jaxbImportConfigurator = 
			JaxbImportConfigurator.NewInstance(fileName, destinationDb);

		CdmDefaultImport<JaxbImportConfigurator> jaxbImport = 
			new CdmDefaultImport<JaxbImportConfigurator>();
		
//		jaxbImportConfigurator.setSource(fileName);
//		jaxbImportConfigurator.setDestination(destinationDb);
		jaxbImportConfigurator.setDbSchemaValidation(DbSchemaValidation.CREATE);

		jaxbImportConfigurator.setMaxRows(NUMBER_ROWS_TO_RETRIEVE);

		jaxbImportConfigurator.setDoAuthors(doAgents);
		jaxbImportConfigurator.setDoAgentData(doAgentData);
		jaxbImportConfigurator.setDoLanguageData(doLanguageData);
		jaxbImportConfigurator.setDoFeatureData(doFeatureData);
		jaxbImportConfigurator.setDoDescriptions(doDescriptions);
		jaxbImportConfigurator.setDoMedia(doMedia);
		jaxbImportConfigurator.setDoOccurrence(doOccurrences);
		jaxbImportConfigurator.setDoReferences(doReferences);
		jaxbImportConfigurator.setDoReferencedEntities(doReferencedEntities);
		jaxbImportConfigurator.setDoRelTaxa(doRelationships);
		jaxbImportConfigurator.setDoSynonyms(doSynonyms);
		jaxbImportConfigurator.setDoTaxonNames(doTaxonNames);
		jaxbImportConfigurator.setDoTaxa(doTaxa);
		jaxbImportConfigurator.setDoTerms(doTerms);
		jaxbImportConfigurator.setDoTermVocabularies(doTermVocabularies);
		jaxbImportConfigurator.setDoHomotypicalGroups(doHomotypicalGroups);

		// invoke import
		logger.debug("Invoking Jaxb import");
//		CdmImporter cdmImporter = new CdmImporter();
//		cdmImporter.invoke(jaxbImportConfigurator, null);
		jaxbImport.invoke(jaxbImportConfigurator);

	}

	
	private CdmApplicationController initDb(ICdmDataSource db) {

		// Init source DB
		CdmApplicationController appCtrInit = null;

		// initDb(ICdmDataSource db, DbSchemaValidation dbSchemaValidation, boolean omitTermLoading)
		appCtrInit = TestDatabase.initDb(db, DbSchemaValidation.CREATE, false);

		return appCtrInit;
	}

	
	// Load test data to DB
	private void loadTestData(CdmApplicationController appCtrInit) {

		TestDatabase.loadTestData(sourceDbName, appCtrInit);
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		CdmExportImportActivator sc = new CdmExportImportActivator();

//		CdmApplicationController appCtr = null;
//		appCtr = sc.initDb(sourceDb);
//		sc.loadTestData(appCtr);
		
		sc.invokeExport();
		sc.invokeImport();
	}

}
