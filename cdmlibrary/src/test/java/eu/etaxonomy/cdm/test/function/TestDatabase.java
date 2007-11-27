/* just for testing */


package eu.etaxonomy.cdm.test.function;


import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.types.MySQLDatabaseType;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;



public class TestDatabase {
	static Logger logger = Logger.getLogger(TestDatabase.class);
	
	
	public void testNewDatabaseConnection(){
		CdmApplicationController appCtr = new CdmApplicationController();
		IDatabaseService dbService = appCtr.getDatabaseService();
		INameService nameService = appCtr.getNameService();
		
		//existing connection
		logger.info(dbService.getDatabaseEnum().getName());
		logger.info(dbService.getUrl());
		
		List<TaxonNameBase> list = appCtr.getNameService().getAllNames(1000, 0);
		logger.info("Count: " + list.size());
		
		BotanicalName bn = new BotanicalName(null);
		logger.info("ID:" + bn.getId());
		nameService.saveTaxonName(bn);
		logger.info("ID:" + bn.getId());
		
		bn.setInfraGenericEpithet("test");
		nameService.saveTaxonName(bn);
		//change connection
		dbService.connectToDatabase(DatabaseTypeEnum.SqlServer, "LAPTOPHP", "cdmTest", "sa", "sa");
		//dbService.connectToDatabase(DatabaseTypeEnum.MySQL, "192.168.2.10", "cdm_test", "edit", "wp5");
		logger.info(dbService.getDatabaseEnum().getName());
		logger.info(dbService.getUrl());
		list = nameService.getAllNames(1000, 0);
		logger.info("Count: " + list.size());
		
		BotanicalName bn2 = new BotanicalName(null);
		logger.info("bn2 created");
		//tn2.setId(51);
		logger.info("ID (before):" + bn2.getId());
		
		nameService.saveTaxonName(bn2);
		logger.info("ID:" + bn2.getId());
		
		
		nameService.saveTaxonName(bn);
		logger.info("ID:" + bn.getId());
		
		appCtr.close();
	}
	
	private void test(){
		System.out.println("Start TestDatabase");
		testNewDatabaseConnection();
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
