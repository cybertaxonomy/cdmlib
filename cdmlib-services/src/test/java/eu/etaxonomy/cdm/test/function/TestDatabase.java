/* just for testing */


package eu.etaxonomy.cdm.test.function;


import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource.HBM2DDL;



public class TestDatabase {
	static Logger logger = Logger.getLogger(TestDatabase.class);
	
	
	public void testNewDatabaseConnection(){
		try {
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(HBM2DDL.CREATE);
			IDatabaseService dbService = appCtr.getDatabaseService();
			INameService nameService = appCtr.getNameService();
			appCtr.close();
		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		}
	}
	
	public void testNewDatasourceClass(){
		try {
			String server = "192.168.2.10";
			String database = "cdm_1_1";
			String username = "edit";
			String password = "xxx";
			CdmDataSource.NewMySqlInstance("", database, username, password);
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(HBM2DDL.CREATE);
			IDatabaseService dbService = appCtr.getDatabaseService();
			INameService nameService = appCtr.getNameService();
			appCtr.close();
		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		}
	}
	
	
	private void test(){
		System.out.println("Start TestDatabase");
		//testNewDatabaseConnection();
		testNewDatasourceClass();
		System.out.println("\nEnd TestDatabase");
	}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestDatabase sc = new TestDatabase();
    	sc.test();
	}

}
