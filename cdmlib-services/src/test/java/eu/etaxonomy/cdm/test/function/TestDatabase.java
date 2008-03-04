/* just for testing */


package eu.etaxonomy.cdm.test.function;


import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.transaction.annotation.Transactional;

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
