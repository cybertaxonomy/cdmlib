/* just for testing */


package eu.etaxonomy.cdm.test.function;


import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IDatabaseService;
import eu.etaxonomy.cdm.api.service.INameService;



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
