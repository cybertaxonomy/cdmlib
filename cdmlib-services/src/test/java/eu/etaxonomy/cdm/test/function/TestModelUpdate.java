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
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CdmUpdater;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;

/**
 * This class is meant for functional testing of model changes. It is not meant
 * for running in maven.
 *
 * @author a.mueller
 * @since 22.05.2015
 * @see CdmUpdater
 */
public class TestModelUpdate {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TestModelUpdate.class);


	private void testSelectedDb(){
		DbSchemaValidation schema = DbSchemaValidation.VALIDATE;

		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;


		String database = (schema == DbSchemaValidation.VALIDATE  ? "cdm41" : "cdm47");
//		database = "cdm36";
		CdmDataSource dataSource = getDatasource(dbType, database);


 		try {
// 		    int n = dataSource.executeUpdate("UPDATE CdmMetaData SET value = '3.1.0.0.201607300000' WHERE propertyname = 0 ");
			CdmUpdater updater = new CdmUpdater();
			if (schema == DbSchemaValidation.VALIDATE){
				SchemaUpdateResult result = updater.updateToCurrentVersion(dataSource,
				        DefaultProgressMonitor.NewInstance());
				String report = result.createReport().toString();
				System.out.println(report);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
 		try{
    		CdmApplicationController appCtr = CdmApplicationController.NewInstance(dataSource, schema);

    //		Classification classification = Classification.NewInstance("Me");
    //		Taxon taxon = Taxon.NewInstance(null, null);
    //		Person person = Person.NewInstance();
    //		TaxonNode node = classification.addChildTaxon(taxon, null, null);
    //		DefinedTerm lastScrutiny = (DefinedTerm)appCtr.getTermService().find(DefinedTerm.uuidLastScrutiny);
    //		TaxonNodeAgentRelation rel = node.addAgentRelation(lastScrutiny, person);
    //      appCtr.getClassificationService().save(classification);

    		if (schema == DbSchemaValidation.CREATE){
    		    System.out.println("fillData");
    		    appCtr.getCommonService().createFullSampleData();
    		    appCtr.getNameService().list(null, null, null, null, null);
    		}


    		appCtr.close();
 		}catch (Exception e) {
 		    e.printStackTrace();
 		}
 		System.out.println("Ready");
		System.exit(0);
	}



/**
     * @param dbType
     * @param database
     * @return
     */
    private CdmDataSource getDatasource(DatabaseTypeEnum dbType, String database) {
        String server = "localhost";
        String username = "edit";
        String serverSql = "130.133.70.26";

        if (dbType == DatabaseTypeEnum.MySQL){
            return CdmDataSource.NewMySqlInstance(server, database, username, AccountStore.readOrStorePassword(server, database, username, null));
        }else if (dbType == DatabaseTypeEnum.H2){
            //H2
            String path = "C:\\Users\\a.mueller\\.cdmLibrary\\writableResources\\h2\\LocalH2_" + database;
//            String path = "C:\\Users\\a.mueller\\.cdmLibrary\\writableResources\\h2\\LocalH2_xyz";
            username = "sa";
            CdmDataSource dataSource = CdmDataSource.NewH2EmbeddedInstance("cdmTest", username, "", path);
            return dataSource;
        }else if (dbType == DatabaseTypeEnum.SqlServer2005){
            server = serverSql;
            username = "cdmupdater";
            CdmDataSource dataSource = CdmDataSource.NewSqlServer2005Instance(server, database, 1433, username, AccountStore.readOrStorePassword(server, database, username, null));
            return dataSource;
        }else if (dbType == DatabaseTypeEnum.PostgreSQL){
            server = serverSql;
            username = "postgres";
            CdmDataSource dataSource = CdmDataSource.NewPostgreSQLInstance(server, database, 5432, username,  AccountStore.readOrStorePassword(server, database, username, null));
            return dataSource;
        }else{
            throw new IllegalArgumentException("dbType not supported:" + dbType);
        }
    }



	/**
	 * Updates the H2 test database in remote web-app.
	 * Requires that the local path to the database is adapted
	 */
	@SuppressWarnings("unused")  //enable only if needed
	private void updateRemoteWebappTestH2(){
	    String pathToProject = "C:\\Users\\a.mueller\\eclipse\\git\\cdmlib\\cdmlib-remote-webapp\\";
//	    String pathToProject = "C:\\Users\\a.mueller\\eclipse\\git\\cdm-vaadin\\";
//	    String pathToProject = "C:\\Users\\a.mueller\\eclipse\\git\\taxeditor\\eu.etaxonomy.taxeditor.test\\";

	    String pathInProject = "src\\test\\resources\\h2";

	    String path = pathToProject + pathInProject;
		ICdmDataSource dataSource = CdmDataSource.NewH2EmbeddedInstance("cdmTest", "sa", "", path);


 		try {
			CdmUpdater updater = new CdmUpdater();
			SchemaUpdateResult result = updater.updateToCurrentVersion(dataSource, DefaultProgressMonitor.NewInstance());
			System.out.println(result.createReport());
		} catch (Exception e) {
			e.printStackTrace();
		}

		//CdmPersistentDataSource.save(dataSource.getName(), dataSource);
		CdmApplicationController appCtr;
		appCtr = CdmApplicationController.NewInstance(dataSource,DbSchemaValidation.VALIDATE);
		appCtr.close();
		System.out.println("\nEnd Datasource");
		System.exit(0);
	}


	private void test(){
		System.out.println("Start Datasource");
		testSelectedDb();

//		updateRemoteWebappTestH2();  //also updates vaadin and taxedtior model

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
