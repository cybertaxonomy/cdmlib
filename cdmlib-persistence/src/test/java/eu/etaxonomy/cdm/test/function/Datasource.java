package eu.etaxonomy.cdm.test.function;

import java.sql.Connection;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;

public class Datasource {
	private static final Logger logger = Logger.getLogger(Datasource.class);

	private void test(){
		System.out.println("Start Datasource");

		CdmPersistentDataSource defaultDataSource = CdmPersistentDataSource.save(
				"mysql_cichorieae", DatabaseTypeEnum.MySQL, "87.106.88.177", "cdm_edit_cichorieae", 80, "edit", "R3m0teAt80");

//		logger.warn(defaultDataSource.getDatabase());
//		logger.warn(defaultDataSource.getPort());
//		logger.warn(defaultDataSource.getServer());
		
		defaultDataSource = CdmPersistentDataSource.save(
				"mysql_cichorieae", DatabaseTypeEnum.SqlServer2005, "87.106.88.177", "cdm_edit_cichorieae", 80, "edit", "R3m0teAt80");

		logger.warn(defaultDataSource.getDatabase());
		logger.warn(defaultDataSource.getPort());
		logger.warn(defaultDataSource.getServer());
		
		defaultDataSource = CdmPersistentDataSource.save(
				"mysql_cichorieae", DatabaseTypeEnum.MySQL, "192.168.2.10", "cdm_edit_cichorieae", 3306, "edit", "wp5");
		
		defaultDataSource = CdmPersistentDataSource.save(
				"mysql_cichorieae", DatabaseTypeEnum.MySQL, "87.106.88.177", "cdm_edit_cichorieae", 80, "edit", "R3m0teAt80");

		logger.warn("Connect: " + defaultDataSource.testConnection());
		
		
		
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
