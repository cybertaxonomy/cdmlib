/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.test.function;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;

public class Datasource {
	private static final Logger logger = Logger.getLogger(Datasource.class);

	private void test(){
		System.out.println("Start Datasource");

		int port = 3306;
		String username = "root";
		String pwd = "mysqlr00t";
		String server = "localhost";
		
		CdmPersistentDataSource defaultDataSource = CdmPersistentDataSource.save(
				"bla", CdmDataSource.NewMySqlInstance(server , "mon_cdm",port, username, pwd, null));

//		logger.warn(defaultDataSource.getDatabase());
//		logger.warn(defaultDataSource.getPort());
//		logger.warn(defaultDataSource.getServer());
		
//		defaultDataSource = CdmPersistentDataSource.save(
//				"bla", CdmDataSource.NewSqlServer2005Instance(server, "mon_cdm",
//port, username, pwd));

		logger.warn(defaultDataSource.getDatabase());
		logger.warn(defaultDataSource.getPort());
		logger.warn(defaultDataSource.getServer());
		
//		defaultDataSource = CdmPersistentDataSource.save(
//				"mon_cdm", CdmDataSource.NewMySqlInstance("localhost", "mon_cdm", 3306, username, "XXX", null));
//		
//		defaultDataSource = CdmPersistentDataSource.save(
//				"mon_cdm", CdmDataSource.NewMySqlInstance(server, "mon_cdm", port, username, pwd, null));

		
		try {
			logger.warn("Connect: " + defaultDataSource.testConnection());
		} catch (Exception e) {
			logger.warn("Could not connect", e);
		}
		
		
		
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
