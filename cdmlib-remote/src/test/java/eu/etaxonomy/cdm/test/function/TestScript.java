/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.function;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.config.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.remote.controller.TaxonPortalController;
import eu.etaxonomy.cdm.remote.editor.UuidList;
import eu.etaxonomy.cdm.remote.io.application.CdmRemoteApplicationController;

/**
 * @author a.mueller
 * @since 08.11.2021
 */
public class TestScript {

	private static final Logger logger = Logger.getLogger(TestScript.class);


	private void testNewConfigControler(){

		DbSchemaValidation schema = DbSchemaValidation.VALIDATE;

		String server;
		String database;
		String username;
		ICdmDataSource dataSource;

//      List<CdmPersistentDataSource> lsDataSources = CdmPersistentDataSource.getAllDataSources();
//     System.out.println(lsDataSources);
//     dataSource = lsDataSources.get(1);

//		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;

//		server = "localhost";
//		database = "cdm_bupleurum";
////		database = "cdm_production_edaphobase";
//		username = "edit";
//		dataSource = CdmDataSource.NewMySqlInstance(server, database, username, AccountStore.readOrStorePassword(server, database, username, null));

//		server = "160.45.63.171";
//		database = "cdm_production_campanulaceae";
//		username = "edit";
//		dataSource = CdmDataSource.NewMySqlInstance(server, database, username, AccountStore.readOrStorePassword(server, database, username, null));


//		server = "test.e-taxonomy.eu";
//		database = "cdm_rem_conf_am";
//		username = "edit";
//		dataSource = CdmDataSource.NewMySqlInstance(server, database, username, AccountStore.readOrStorePassword(server, database, username, null));

//		String server = "localhost";
//		String database = "testCDM";
//		String username = "postgres";
//		dataSource = CdmDataSource.NewInstance(DatabaseTypeEnum.PostgreSQL, server, database, DatabaseTypeEnum.PostgreSQL.getDefaultPort(), username, AccountStore.readOrStorePassword(server, database, username, null));


//		//SQLServer
//		server = "BGBM-PESISQL";
//		database = "cdm36";
//		int port = 1433;
//		username = "cdmupdater";
//		dataSource = CdmDataSource.NewSqlServer2012Instance(server, database, port, username, AccountStore.readOrStorePassword(server, database, username, null));
//
//		//H2
//        String path = "C:\\Users\\a.mueller\\.cdmLibrary\\writableResources\\h2\\LocalH2";
////		String path = "C:\\Users\\pesiimport\\.cdmLibrary\\writableResources\\h2\\LocalH2";
////      String path = "C:\\Users\\a.mueller\\eclipse\\svn\\cdmlib-trunk\\cdmlib-remote-webapp\\src\\test\\resources\\h2";
//		username = "sa";
//    	dataSource = CdmDataSource.NewH2EmbeddedInstance("cdm", username, "", path);

//    	dataSource = CdmDataSource.NewH2EmbeddedInstance(database, username, "sa");


       server = "160.45.63.201";
       database = "cdm_integration_cichorieae";
       username = "edit";
       dataSource = CdmDataSource.NewMySqlInstance(server, database, username, AccountStore.readOrStorePassword(server, database, username, null));

		CdmApplicationController appCtr = CdmRemoteApplicationController.NewRemoteInstance(dataSource, schema);

		try {
            doTemporary(appCtr);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

//		List<UuidAndTitleCache<TaxonBase>> list = appCtr.getTaxonService().getUuidAndTitleCache(null, 10, "Abies alba%");
//		System.out.println(list);
//		appCtr.getOccurrenceService().findRootUnitDTOs(UUID.fromString("2debf5ee-cb57-40bc-af89-173d1d17cefe"));
//		aggregateDDS(appCtr);
//		aggregateDistribution(appCtr);

		appCtr.close();
		System.exit(0);
	}

    private void doTemporary(CdmApplicationController appCtr) throws IOException {

        UUID taxonUUID = UUID.fromString("85176c77-e4b6-4899-a08b-e257ab09350a");
        boolean includeTaxonomicChildren = true;
        UuidList relationshipUuids = new UuidList();
        UuidList relationshipInversUuids = new UuidList();
        boolean includeTaxonDescriptions = true;
        boolean includeOccurrences = true;
        boolean includeTaxonNameDescriptions = true;
        HttpServletResponse response = null;
        TaxonPortalController taxonPortalController = (TaxonPortalController) appCtr.getBean("taxonPortalController");

        TaxonBase<?> taxon = taxonPortalController.doGet(taxonUUID, null, response);
        TeamOrPersonBase<?> secAuthor = taxon.getSec().getAuthorship();

    }

	private void test(){
		System.out.println("Start TestScript");
		testNewConfigControler();
    	//testDatabaseChange();

		System.out.println("\nEnd TestScript");
	}

	public static void  main(String[] args) {
	    TestScript cc = new TestScript();
    	cc.test();
	}

}
