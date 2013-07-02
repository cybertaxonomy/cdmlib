/**
 * Copyright (C) 2008 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 */

package eu.etaxonomy.cdm.app.sdd;

import java.io.File;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultExport;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.sdd.out.SDDExportConfigurator;

/**
 * @author h.fradin (from a.babadshanjan eu.etaxonomy.cdm.app.jaxb.CdmExportImportActivator)
 * @created 09.12.2008
 */
public class ViolaExportActivator {

	/* SerializeFrom DB **/
	private static final String sourceDbName = "cdm";
	private static final String destinationFileName = "ViolaFromCDM.xml";
	private static final String destinationFolder = "/Developer/exports SDD";
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

	private static final ICdmDataSource sourceDb = ViolaExportActivator.CDM_DB(sourceDbName);
	private static final File destinationFile = new File(destinationFolder + File.separator + destinationFileName);

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

	private void invokeExport() {
		
		SDDExportConfigurator sddExportConfigurator = 
			SDDExportConfigurator.NewInstance(sourceDb, destinationFileName, destinationFolder);
		
		CdmDefaultExport<SDDExportConfigurator> sddExport = 
			new CdmDefaultExport<SDDExportConfigurator>();
		
		sddExportConfigurator.setSource(sourceDb);
		sddExportConfigurator.setDestination(destinationFile);
		sddExportConfigurator.setDbSchemaValidation(DbSchemaValidation.VALIDATE);

		sddExportConfigurator.setMaxRows(NUMBER_ROWS_TO_RETRIEVE);

		sddExportConfigurator.setDoAuthors(doAgents);
		sddExportConfigurator.setDoAgentData(doAgentData);
		sddExportConfigurator.setDoLanguageData(doLanguageData);
		sddExportConfigurator.setDoFeatureData(doFeatureData);
		sddExportConfigurator.setDoDescriptions(doDescriptions);
		sddExportConfigurator.setDoMedia(doMedia);
		sddExportConfigurator.setDoOccurrence(doOccurrences);
		sddExportConfigurator.setDoReferences(doReferences);
		sddExportConfigurator.setDoReferencedEntities(doReferencedEntities);
		sddExportConfigurator.setDoRelTaxa(doRelationships);
		sddExportConfigurator.setDoSynonyms(doSynonyms);
		sddExportConfigurator.setDoTaxonNames(doTaxonNames);
		sddExportConfigurator.setDoTaxa(doTaxa);
		sddExportConfigurator.setDoTerms(doTerms);
		sddExportConfigurator.setDoTermVocabularies(doTermVocabularies);
		sddExportConfigurator.setDoHomotypicalGroups(doHomotypicalGroups);

		// invoke export
		logger.debug("Invoking SDD export");
		sddExport.invoke(sddExportConfigurator);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ViolaExportActivator vea = new ViolaExportActivator();

//		CdmApplicationController appCtr = null;
//		appCtr = sc.initDb(sourceDb);
//		sc.loadTestData(appCtr);
		
		
		vea.invokeExport();

	}

}
