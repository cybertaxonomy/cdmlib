package eu.etaxonomy.cdm.test.function;

import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

public class Datasource {
	private static final Logger logger = Logger.getLogger(Datasource.class);

	
	private void testNewConfigControler(){
		List<CdmDataSource> lsDataSources = CdmDataSource.getAllDataSources();
		System.out.println(lsDataSources);
		CdmDataSource dataSource = lsDataSources.get(0);
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		CdmDataSource.save(dataSource.getName(), dbType, "192.168.2.10", "cdm_test_andreas", "edit", "wp5");
		CdmApplicationController appCtr;
		try {
			appCtr = new CdmApplicationController(dataSource);
			appCtr.close();
		} catch (DataSourceNotFoundException e) {
			logger.error("Unknown datasource");
		}
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
		try {
			CdmApplicationController appCtr = new CdmApplicationController(ds);
			Person agent = new Person();
			appCtr.getAgentService().saveAgent(agent);
			TaxonNameBase tn = new BotanicalName(null);
			appCtr.getNameService().saveTaxonName(tn);
			appCtr.close();
		} catch (DataSourceNotFoundException e) {
			logger.error("Unknown datasource");
		}
	}
	
	private void testPostgreServer(){
		DatabaseTypeEnum databaseTypeEnum = DatabaseTypeEnum.PostgreSQL;
		String server = "192.168.1.17";
		String database = "cdm_test";
		String username = "edit";
		String password = "wp5";
		CdmDataSource ds = CdmDataSource.save("PostgreTest", databaseTypeEnum, server, database, username, password);
		try {
			CdmApplicationController appCtr = new CdmApplicationController(ds);
			Person agent = new Person();
			appCtr.getAgentService().saveAgent(agent);
			TaxonNameBase tn = new BotanicalName(null);
			appCtr.getNameService().saveTaxonName(tn);
			appCtr.close();
		} catch (DataSourceNotFoundException e) {
			logger.error("Unknown datasource");
		}
	}
	
	private void testLocalHsql(){
		try {
			CdmDataSource ds = CdmDataSource.NewLocalHsqlInstance();
			CdmApplicationController appCtr = new CdmApplicationController(ds);
			try {
				List l = appCtr.getNameService().getAllNames(5, 1);
				System.out.println(l);
				//Agent agent = new Agent();
				//appCtr.getAgentService().saveAgent(agent);
				appCtr.close();
			} catch (RuntimeException e) {
				logger.error("Runtime Exception");
				e.printStackTrace();
				appCtr.close();
				
			}
		} catch (DataSourceNotFoundException e) {
			logger.error("LOCAL HSQL");
		}
	}
		
	
	private void test(){
		System.out.println("Start Datasource");
		//testNewConfigControler();
    	//testDatabaseChange();
		//testSqlServer();
		//CdmUtils.findLibrary(au.com.bytecode.opencsv.CSVReader.class);
		//testPostgreServer();
		testLocalHsql();
		System.out.println("\nEnd Datasource");
	}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		Datasource cc = new Datasource();
    	cc.test();
	}

}
