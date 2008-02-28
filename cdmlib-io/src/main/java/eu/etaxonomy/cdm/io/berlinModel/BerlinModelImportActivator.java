/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.application.CdmApplicationController.HBM2DDL;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.io.source.Source;

/**
 * @author a.mueller
 *
 */
public class BerlinModelImportActivator {
	private static Logger logger = Logger.getLogger(BerlinModelImportActivator.class);


	//	BerlinModelDatabase
	static String dbms = "SQLServer";
	static String strServer = "BGBM111";
	static String strDB = "EuroPlusMed_00_Edit";
	static int port = 1247;
	static String userName = "webUser";
	static String pwd = "";

	
	
//	static DatabaseTypeEnum dbType = DatabaseTypeEnum.SqlServer2005;
//	static String cdmServer = "LAPTOPHP";
//	static String cdmDB = "cdmTest";
//	//static int cdmPort = 1433;
//	static String cdmUserName = "edit";
//	static String cdmPwd = "wp5";
	
	static DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
	static String cdmServer = "192.168.2.10";
	static String cdmDB = "cdm_test_lib";
	//static int cdmPort = 1247;
	static String cdmUserName = "edit";
	static String cdmPwd = "wp5";

	
	static HBM2DDL hbm2dll = HBM2DDL.CREATE;
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start import from BerlinModel ...");
		Source source;
		CdmApplicationController cdmApp;
		
		//make BerlinModel Source
		source = makeSource(dbms, strServer, strDB, port, userName, pwd);
		if (source == null){
			logger.error("Connection to BerlinModel could not be established");
			System.out.println("End import from BerlinModel ...");
			return;
		}
		//make CdmApplication
		String dataSourceName;
		dataSourceName = "cdmImportLibrary";
//		dataSourceName = "testSqlServer";
		
		CdmDataSource dataSource;
		try {
			dataSource = CdmDataSource.NewInstance(dataSourceName);
		} catch (DataSourceNotFoundException e1) {
			dataSource = CdmDataSource.save(dataSourceName, dbType, cdmServer, cdmDB, cdmUserName, cdmPwd);
		}
		try {
			cdmApp = new CdmApplicationController(dataSource, hbm2dll);
		} catch (DataSourceNotFoundException e) {
			logger.error(e.getMessage());
			return;
		}
		
		// invoke import
		if (source != null){
			BerlinModelImport bmImport = new BerlinModelImport();
			bmImport.doImport(source, cdmApp);
		}
		System.out.println("End import from BerlinModel ...");
	}
	
	
	/**
	 * initializes source
	 * @return true, if connection establisehd
	 */
	private static Source makeSource(String dbms, String strServer, String strDB, int port, String userName, String pwd){
		//establish connection
		try {
			Source source = new Source(dbms, strServer, strDB);
			source.setPort(port);
			source.setUserAndPwd(userName, pwd);
			return source;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	

}
