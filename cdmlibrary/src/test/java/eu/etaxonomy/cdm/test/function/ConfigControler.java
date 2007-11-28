package eu.etaxonomy.cdm.test.function;

import java.util.List;

import org.hibernate.cfg.Environment;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

public class ConfigControler {

	
	private void testNewConfigControler(){
		List<CdmDataSource> lsDataSources = CdmDataSource.getAllDataSources();
		System.out.println(lsDataSources);
		CdmDataSource dataSource = lsDataSources.get(0);
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		CdmDataSource.save(dataSource.getName(), dbType, "192.168.2.10", "cdm_test_andreas", "edit", "wp5");
		CdmApplicationController appCtr = new CdmApplicationController(dataSource);
		appCtr.close();
	}
	
	private void testDatabaseChange(){
		CdmApplicationController appCtr = new CdmApplicationController();
		
//		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
//		String server = "192.168.2.10";
//		String database = "cdm_test_andreas";
//		String user = "edit";
//		String pwd = "wp5";
//		
		DatabaseTypeEnum dbType = DatabaseTypeEnum.SqlServer;
		String server = "LAPTOPHP";
		String database = "cdmTest";
		String username = "edit";
		String password = "wp5";
		
		appCtr.getDatabaseService().saveDataSource("testSqlServer", dbType, server, database, username, password);
		appCtr.getDatabaseService().connectToDatabase(dbType, server, database, username, password);
		
		appCtr.close();
	}

	private void testSqlServer(){
		DatabaseTypeEnum databaseTypeEnum = DatabaseTypeEnum.SqlServer;
		String server = "LAPTOPHP";
		String database = "cdmTest";
		String username = "edit";
		String password = "wp5";
		CdmDataSource ds = CdmDataSource.save("testSqlServer", databaseTypeEnum, server, database, username, password);
		CdmApplicationController appCtr = new CdmApplicationController(ds);
		
		//appCtr.getDatabaseService().connectToDatabase(dbType, server, database, username, password);
		
		appCtr.close();
	}
	
	private void test(){
		System.out.println("Start ConfigControler");
		//testNewConfigControler();
    	//testDatabaseChange();
		testSqlServer();
		System.out.println("\nEnd ConfigControler");
	}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		ConfigControler cc = new ConfigControler();
    	cc.test();
	}

}
