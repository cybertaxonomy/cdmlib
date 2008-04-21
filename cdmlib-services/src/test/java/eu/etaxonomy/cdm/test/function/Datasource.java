package eu.etaxonomy.cdm.test.function;

import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

public class Datasource {
	private static final Logger logger = Logger.getLogger(Datasource.class);

	
	private void testNewConfigControler(){
		List<CdmPersistentDataSource> lsDataSources = CdmPersistentDataSource.getAllDataSources();
		System.out.println(lsDataSources);
		CdmPersistentDataSource dataSource = lsDataSources.get(0);
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		CdmPersistentDataSource.save(dataSource.getName(), dbType, "192.168.2.10", "cdm_test_andreas", "edit", "wp5");
		CdmApplicationController appCtr;
		try {
			appCtr = CdmApplicationController.NewInstance(dataSource);
			appCtr.close();
		} catch (DataSourceNotFoundException e) {
			logger.error("Unknown datasource");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}

	}
	
	private void testDatabaseChange(){
		CdmApplicationController appCtr;
		try {
			appCtr = CdmApplicationController.NewInstance();
		
	//		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
	//		String server = "192.168.2.10";
	//		String database = "cdm_test_andreas";
	//		String user = "edit";
	//		String pwd = "wp5";
	//		
			DatabaseTypeEnum dbType = DatabaseTypeEnum.SqlServer2005;
			String server = "LAPTOPHP";
			String database = "cdmTest";
			String username = "edit";
			String password = "wp5";
			
			appCtr.getDatabaseService().saveDataSource("testSqlServer", dbType, server, database, username, password);
			appCtr.getDatabaseService().connectToDatabase(dbType, server, database, username, password);
			
			appCtr.close();
		} catch (DataSourceNotFoundException e) {
			logger.error("datasource error");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
	}

	private void testSqlServer(){
		DatabaseTypeEnum databaseTypeEnum = DatabaseTypeEnum.SqlServer2000;
		String server = "LAPTOPHP";
		String database = "cdmTest";
		String username = "edit";
		String password = "wp5";
		CdmPersistentDataSource ds = CdmPersistentDataSource.save("testSqlServer", databaseTypeEnum, server, database, username, password);
		try {
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(ds);
			Person agent = Person.NewInstance();
			appCtr.getAgentService().saveAgent(agent);
			TaxonNameBase tn = BotanicalName.NewInstance(null);
			appCtr.getNameService().saveTaxonName(tn);
			appCtr.close();
		} catch (DataSourceNotFoundException e) {
			logger.error("Unknown datasource");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
	}

	private void testSqlServer2005(){
		DatabaseTypeEnum databaseTypeEnum = DatabaseTypeEnum.SqlServer2005;
		String server = "LAPTOPHP";
		String database = "cdmTest";
		String username = "edit";
		String password = "wp5";
		CdmPersistentDataSource ds = CdmPersistentDataSource.save("testSqlServer", databaseTypeEnum, server, database, username, password);
		try {
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(ds);
			Person agent = Person.NewInstance();
			appCtr.getAgentService().saveAgent(agent);
			TaxonNameBase tn = BotanicalName.NewInstance(null);
			appCtr.getNameService().saveTaxonName(tn);
			appCtr.close();
		} catch (DataSourceNotFoundException e) {
			logger.error("Unknown datasource");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
	}
	
	private void testPostgreServer(){
		DatabaseTypeEnum databaseTypeEnum = DatabaseTypeEnum.PostgreSQL;
		String server = "192.168.1.17";
		String database = "cdm_test";
		String username = "edit";
		String password = "wp5";
		CdmPersistentDataSource ds = CdmPersistentDataSource.save("PostgreTest", databaseTypeEnum, server, database, username, password);
		try {
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(ds);
			Person agent = Person.NewInstance();
			appCtr.getAgentService().saveAgent(agent);
			TaxonNameBase tn = BotanicalName.NewInstance(null);
			appCtr.getNameService().saveTaxonName(tn);
			appCtr.close();
		} catch (DataSourceNotFoundException e) {
			logger.error("Unknown datasource");
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
		}
	}
	
	private void testLocalHsql(){
		try {
			CdmPersistentDataSource ds = CdmPersistentDataSource.NewLocalHsqlInstance();
			CdmApplicationController appCtr = CdmApplicationController.NewInstance(ds);
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
		} catch (TermNotFoundException e) {
			logger.error("defined terms not found");
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
