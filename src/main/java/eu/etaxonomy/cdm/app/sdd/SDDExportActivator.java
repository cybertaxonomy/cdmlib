// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.app.sdd;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.SQLException;

import junit.framework.Assert;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultExport;
import eu.etaxonomy.cdm.io.common.IExportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.sdd.out.SDDExportConfigurator;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author l.morris
 * @date 29 Nov 2012
 *
 */
public class SDDExportActivator {

	/* SerializeFrom DB **/
	private static final String sourceDbName = "cdm";
	//private static final String destinationFileName = "ViolaFromCDM.xml";
	//private static final String destinationFolder = "/Developer/exports SDD";
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
	
    private static ICdmDataSource customDataSource() {

        CdmPersistentDataSource loadedDataSource = null;
       //ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance("192.168.2.10", "cdm_test_niels2", 3306, "edit", password, code);

       String dataSourceName = CdmUtils.readInputLine("Database name: ");
       String username = CdmUtils.readInputLine("Username: ");
       String password = CdmUtils.readInputLine("Password: ");
       
       dataSourceName = (dataSourceName.equals("")) ? "cdm_test3" : dataSourceName;
       username = (username.equals("")) ? "ljm" : username;
       
       //ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance("127.0.0.1", "cdm_test3", 3306, "ljm", password, NomenclaturalCode.ICBN);
       ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance("127.0.0.1", dataSourceName, 3306, username, password, NomenclaturalCode.ICBN);
       //ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance("127.0.0.1", "cdm_edit_cichorieae", 3306, "ljm", password, NomenclaturalCode.ICBN);
       //ICdmDataSource dataSource = 
       CdmDataSource.NewMySqlInstance("160.45.63.201", "cdm_edit_cichorieae", 3306, "edit", password, NomenclaturalCode.ICBN);
       boolean connectionAvailable;
       try {
           connectionAvailable = dataSource.testConnection();
           System.out.println("LORNA connection available " + connectionAvailable);
           Assert.assertTrue("Testdatabase is not available", connectionAvailable);

       } catch (ClassNotFoundException e1) {
           // TODO Auto-generated catch block
           e1.printStackTrace();
       } catch (SQLException e1) {
           // TODO Auto-generated catch block
           e1.printStackTrace();
       }

       CdmPersistentDataSource.save(dataSourceName, dataSource);
       try {
           loadedDataSource = CdmPersistentDataSource.NewInstance(dataSourceName);
//			CdmApplicationController.NewInstance(loadedDataSource, DbSchemaValidation.CREATE);
           NomenclaturalCode loadedCode = loadedDataSource.getNomenclaturalCode();

           Assert.assertEquals(NomenclaturalCode.ICBN, loadedCode);
       } catch (DataSourceNotFoundException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
       //return loadedDataSource;
       return dataSource;
   }
	
	

	private static final Logger logger = Logger.getLogger(ViolaExportActivator.class);

	//private static final ICdmDataSource sourceDb = ViolaExportActivator.CDM_DB(sourceDbName);
	private static final ICdmDataSource sourceDb = customDataSource();
	//private static final File destinationFile = new File(destinationFolder + File.separator + destinationFileName);

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
	private static boolean doRelationships = false;  //causes org.hibernate.ObjectNotFoundException: No row with the given identifier exists: [eu.etaxonomy.cdm.model.taxon.Taxon#24563] in cichoriae
	private static boolean doSynonyms = true;
	private static boolean doTaxonNames = true;
	private static boolean doTaxa = true;
	private static boolean doTerms = true;
	private static boolean doTermVocabularies = true;
	private static boolean doHomotypicalGroups = true;//try export again

	private void invokeExport() {

        // export data
        //String exporturlStr = "/sdd/SDDImportExportTest.sdd.xml";
        String exporturlStr = "SDDImportExportTest.sdd.xml";
        File f = new File(exporturlStr);
        try {
			exporturlStr = f.toURI().toURL().toString();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//lorna//SDDExportConfigurator sddExportConfigurator = 
			//SDDExportConfigurator.NewInstance(sourceDb, destinationFileName, destinationFolder);
		
		SDDExportConfigurator sddExportConfigurator = 
				SDDExportConfigurator.NewInstance(sourceDb, exporturlStr);
		
		
		CdmDefaultExport<SDDExportConfigurator> sddExport = 
			new CdmDefaultExport<SDDExportConfigurator>();
		
		sddExportConfigurator.setSource(sourceDb);
		//lorna//sddExportConfigurator.setDestination(destinationFile);
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

		SDDExportActivator sddex = new SDDExportActivator();
		
		sddex.invokeExport();

	}

}
