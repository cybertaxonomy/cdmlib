package eu.etaxonomy.cdm.test.function;

import java.util.List;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;

public class ConfigControler {

	
	private void testNewConfigControler(){
		List<CdmDataSource> lsDataSources = CdmDataSource.getAllDataSources();
		System.out.println(lsDataSources);
		CdmDataSource dataSource = lsDataSources.get(0);
		DatabaseTypeEnum dbType = DatabaseTypeEnum.SqlServer;
		CdmDataSource.save(dataSource.getName(), dbType, "192.168.2.10", "cdm_test_andreas", "edit", "wp5");
		CdmApplicationController appCtr = new CdmApplicationController(dataSource);
		appCtr.close();
	}
	
	
	private void test(){
		System.out.println("Start ConfigControler");
		testNewConfigControler();
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
