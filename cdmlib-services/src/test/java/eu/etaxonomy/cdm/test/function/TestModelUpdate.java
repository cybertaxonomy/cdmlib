/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.test.function;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CdmUpdater;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.IntextReference;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class TestModelUpdate {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TestModelUpdate.class);

	
	private void testMySQL(){
		DbSchemaValidation schema = DbSchemaValidation.VALIDATE;

//		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		
		String server = "localhost";
		String database = (schema == DbSchemaValidation.VALIDATE  ? "cdm34" : "cdm35");
		database = "campanula_test";
		String username = "edit";
		CdmDataSource dataSource = CdmDataSource.NewMySqlInstance(server, database, username, AccountStore.readOrStorePassword(server, database, username, null));

    	
 		try {
			CdmUpdater updater = new CdmUpdater();
			if (schema == DbSchemaValidation.VALIDATE){
				updater.updateToCurrentVersion(dataSource, DefaultProgressMonitor.NewInstance());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//CdmPersistentDataSource.save(dataSource.getName(), dataSource);
		CdmApplicationController appCtr;
		appCtr = CdmApplicationController.NewInstance(dataSource,schema);
		
//		Person person = Person.NewInstance();
//		TextData textData = TextData.NewInstance();
//		Taxon taxon = Taxon.NewInstance(null, null);
//		TaxonDescription description = TaxonDescription.NewInstance(taxon);
//		description.addElement(textData);
//		LanguageString text = textData.putText(Language.ENGLISH(), "Ich bin ein toller text");
//		IntextReference.NewAgentInstance(person, text, 3, 5);
//
//		appCtr.getAgentService().save(person);
//		appCtr.getTaxonService().save(taxon);
//		appCtr.getCommonService().createFullSampleData();

		//		insertSomeData(appCtr);
//		deleteHighLevelNode(appCtr);   //->problem with Duplicate Key in Classification_TaxonNode 		
		
		appCtr.close();
	}
	
	

//	String server = "localhost";
//	String database = "testCDM";
//	String username = "postgres";
//	dataSource = CdmDataSource.NewInstance(DatabaseTypeEnum.PostgreSQL, server, database, DatabaseTypeEnum.PostgreSQL.getDefaultPort(), username, AccountStore.readOrStorePassword(server, database, username, null)); 
	
	
//	//SQLServer
//	database = "CDMTest";
//	int port = 1433;
//	username = "pesiexport";
////	dataSource = CdmDataSource.NewSqlServer2005Instance(server, database, port, username, AccountStore.readOrStorePassword(server, database, username, null));
//	
	//H2
//	String path = "C:\\Users\\a.mueller\\eclipse\\svn\\cdmlib-trunk\\cdmlib-remote-webapp\\src\\test\\resources\\h2";
//	String path = "C:\\Users\\a.mueller\\.cdmLibrary\\writableResources\\h2\\LocalH2_test34";
//	username = "sa";
//	dataSource = CdmDataSource.NewH2EmbeddedInstance("cdmTest", username, "", path,   NomenclaturalCode.ICNAFP);
//	dataSource = CdmDataSource.NewH2EmbeddedInstance(database, username, "sa", NomenclaturalCode.ICNAFP);

	/**
	 * Updates the H2 test database in remote web-app.
	 * Requires that the local path to the database is adapted
	 */
	@SuppressWarnings("unused")  //enable only if needed
	private void updateRemoteWebappTestH2(){
		String path = "C:\\Users\\a.mueller\\eclipse\\svn\\cdmlib-trunk\\cdmlib\\cdmlib-remote-webapp\\src\\test\\resources\\h2";
		ICdmDataSource dataSource = CdmDataSource.NewH2EmbeddedInstance("cdmTest", "sa", "", path, NomenclaturalCode.ICNAFP);
		
    	
 		try {
			CdmUpdater updater = new CdmUpdater();
			updater.updateToCurrentVersion(dataSource, DefaultProgressMonitor.NewInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//CdmPersistentDataSource.save(dataSource.getName(), dataSource);
		CdmApplicationController appCtr;
		appCtr = CdmApplicationController.NewInstance(dataSource,DbSchemaValidation.VALIDATE);
		appCtr.close();
	}
	

	private void test(){
		System.out.println("Start Datasource");
		testMySQL();
		
//		updateRemoteWebappTestH2();
		
    	//testDatabaseChange();
		
		//testSqlServer();
		
		//CdmUtils.findLibrary(au.com.bytecode.opencsv.CSVReader.class);
		//testPostgreServer();
		//testLocalHsql();
		//testLocalH2();
		//testWritableResourceDirectory();
//		testH2();
		System.out.println("\nEnd Datasource");
	}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestModelUpdate cc = new TestModelUpdate();
    	cc.test();
	}

}
