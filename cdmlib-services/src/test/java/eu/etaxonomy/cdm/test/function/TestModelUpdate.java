/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.function;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.config.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CdmUpdater;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.model.description.TemporalData;

/**
 * This class is meant for functional testing of model changes. It is not meant
 * for running in maven.
 *
 * For testing
 *
 * 1. First run with CREATE first against H2, than MySQL, PostGreSQL, (SQLServer)
 * 2. Save old schema databases
 * 3. Run with VALIDATE
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
		String database = (schema == DbSchemaValidation.VALIDATE  ? "cdm523" : "cdm527");
//		database = "cdm_test1";

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
    		    TransactionStatus tx = appCtr.startTransaction(false);
    		    TemporalData td = (TemporalData)appCtr.getDescriptionElementService().find(
    		            UUID.fromString("9a1c91c0-fc58-4310-94cb-8c26115985d3"));
    		    td.getFeature().setSupportsCategoricalData(true);
    		    appCtr.getTermService().saveOrUpdate(td.getFeature());
    		    System.out.println(td.getPeriod());
                appCtr.commitTransaction(tx);
    		}

    		appCtr.close();
 		}catch (Exception e) {
 		    e.printStackTrace();
 		}
 		System.out.println("Ready");
	}

    private CdmDataSource getDatasource(DatabaseTypeEnum dbType, String database) {
        String server = "localhost";
        String username = "edit";
        String serverSql = "130.133.70.26";
//        server = "160.45.63.175";

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
	private void updateRemoteWebappTestH2(){
	    String pathToProject = "C:\\Users\\a.mueller\\eclipse\\git\\cdmlib\\cdmlib-remote-webapp\\";
	    updateH2(pathToProject);
	}

    /**
     * Updates the H2 test database in TaxEditor.
     * Requires that the local path to the database is adapted
     */
    private void updateTaxEditorH2(){
        String pathToProject = "C:\\Users\\a.mueller\\eclipse\\git\\taxeditor2\\eu.etaxonomy.taxeditor.test\\";
        updateH2(pathToProject);
    }

    /**
     * Updates the H2 test database in CDM vaadin.
     * Requires that the local path to the database is adapted
     */
    private void updateVaadinH2(){
        String pathToProject = "C:\\Users\\a.mueller\\eclipse\\git\\cdm-vaadin\\";
        updateH2(pathToProject);
    }

    private void updateH2(String pathToProject) {
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
    }

    @SuppressWarnings("unused")  //enable only if needed
    private void updateEdaphobasePostgres(){
       String serverSql = "130.133.70.26";
       String database = "cdm_edaphobase";
       int port = 5433;
       String username = "edaphobase";
       String password = AccountStore.readOrStorePassword(database, serverSql, username, null);

       ICdmDataSource dataSource = CdmDataSource.NewPostgreSQLInstance(serverSql,
                database, port, username, password);
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
    }

	private void test(){
		System.out.println("Start TestModelUpdate");
		testSelectedDb();

//		updateRemoteWebappTestH2();
//		updateAllTestH2();
//		updateEdaphobasePostgres();

		System.out.println("\nEnd Datasource");
	}

	/**
     * Updates all H2 test DBs
     */
    private void updateAllTestH2() {
        updateRemoteWebappTestH2();
        updateTaxEditorH2();
        updateVaadinH2();
    }

	public static void  main(String[] args) {
	    TestModelUpdate cc = new TestModelUpdate();
		cc.test();
    	System.exit(0);
	}
}
