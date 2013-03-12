/**
 * Copyright (C) 2008 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 */

package eu.etaxonomy.cdm.app.pilotOutputHtml;

import java.io.File;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.sdd.ViolaExportActivator;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultExport;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.pilotOutputHtml.PilotOutputExportConfigurator;

/**
 * @author h.fradin (from a.babadshanjan eu.etaxonomy.cdm.app.jaxb.CdmExportImportActivator)
 * @created 09.12.2008
 */
public class SimpleDescriptionOutput {

	/* SerializeFrom DB **/
	private static final String sourceDbName = "cdm";
	private static final String destinationFileName = "ViolaFromCDMhtml.xml";
	private static final String destinationFolder = "C:/Documents and Settings/lis/Mes documents/EDIT/CDM/exports SDD";
	//private static final String destinationFolder = "C:/tmp/viola/exports_SDD";

	/** NUMBER_ROWS_TO_RETRIEVE = 0 is the default case to retrieve all rows.
	 * For testing purposes: If NUMBER_ROWS_TO_RETRIEVE >0 then retrieve 
	 *  as many rows as specified for agents, references, etc. 
	 *  Only root taxa and no synonyms and relationships are retrieved. */
	private static final int NUMBER_ROWS_TO_RETRIEVE = 0;

	private static final String server = "134.157.190.207";
	private static final String username = "sa";

	public static ICdmDataSource CDM_DB(String dbname) {

		logger.info("Setting DB " + dbname);
		ICdmDataSource datasource = CdmDataSource.NewH2EmbeddedInstance(dbname, username, "");
		return datasource;
	}

	private static final Logger logger = Logger.getLogger(ViolaExportActivator.class);

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

	// 3 arguments: name of the CDM database, name of the destination file, path for the destination file
	private void invokeExport(String[] args) {
		
//		PilotOutputExportConfigurator htmlExportConfigurator = 
//			PilotOutputExportConfigurator.NewInstance(sourceDb, destinationFileName, destinationFolder);
		ICdmDataSource sourceDb = ViolaExportActivator.CDM_DB(args[0]);
		PilotOutputExportConfigurator htmlExportConfigurator = 
			PilotOutputExportConfigurator.NewInstance(sourceDb, args[1], args[2]);
		
		CdmDefaultExport<PilotOutputExportConfigurator> htmlExport = 
			new CdmDefaultExport<PilotOutputExportConfigurator>();
		
		htmlExportConfigurator.setSource(sourceDb);
		File destinationFile = new File(args[2] + File.separator + args[1]);
		htmlExportConfigurator.setDestination(destinationFile);
		htmlExportConfigurator.setDbSchemaValidation(DbSchemaValidation.VALIDATE);

		htmlExportConfigurator.setMaxRows(NUMBER_ROWS_TO_RETRIEVE);

		htmlExportConfigurator.setDoAuthors(doAgents);
		htmlExportConfigurator.setDoAgentData(doAgentData);
		htmlExportConfigurator.setDoLanguageData(doLanguageData);
		htmlExportConfigurator.setDoFeatureData(doFeatureData);
		htmlExportConfigurator.setDoDescriptions(doDescriptions);
		htmlExportConfigurator.setDoMedia(doMedia);
		htmlExportConfigurator.setDoOccurrence(doOccurrences);
		htmlExportConfigurator.setDoReferences(doReferences);
		htmlExportConfigurator.setDoReferencedEntities(doReferencedEntities);
		htmlExportConfigurator.setDoRelTaxa(doRelationships);
		htmlExportConfigurator.setDoSynonyms(doSynonyms);
		htmlExportConfigurator.setDoTaxonNames(doTaxonNames);
		htmlExportConfigurator.setDoTaxa(doTaxa);
		htmlExportConfigurator.setDoTerms(doTerms);
		htmlExportConfigurator.setDoTermVocabularies(doTermVocabularies);
		htmlExportConfigurator.setDoHomotypicalGroups(doHomotypicalGroups);

		// invoke export
		logger.debug("Invoking SimpleDescriptionOutput export");
		htmlExport.invoke(htmlExportConfigurator);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		SimpleDescriptionOutput sdo = new SimpleDescriptionOutput();

//		CdmApplicationController appCtr = null;
//		appCtr = sc.initDb(sourceDb);
//		sc.loadTestData(appCtr);
		
		sdo.invokeExport(args);

	}

}
