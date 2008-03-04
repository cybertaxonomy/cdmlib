/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.CdmDataSource.HBM2DDL;
import eu.etaxonomy.cdm.io.berlinModel.test.BerlinModelSources;
import eu.etaxonomy.cdm.io.source.Source;

/**
 * @author a.mueller
 *
 */
public class BerlinModelImportActivator {
	private static Logger logger = Logger.getLogger(BerlinModelImportActivator.class);

	//database validation status (create, update, validate ...)
	static HBM2DDL hbm2dll = HBM2DDL.CREATE;

	//Berlin MOdel Source
	//static final Source berlinModelSource = BerlinModelSources.euroMed();
	static final Source berlinModelSource = BerlinModelSources.editWP6();
//	
////	static DatabaseTypeEnum dbType = DatabaseTypeEnum.SqlServer2000;
////	static String cdmServer = "BGBM10/ENTWICKLUNG";
////	static String cdmDB = "cdmlib_test_1";
////	static int cdmPort = 1433;
////	static String cdmUserName = "edit";
////	static String cdmPwd = "wp5";
////	
	
	//	static DatabaseTypeEnum dbType = DatabaseTypeEnum.SqlServer2005;
//	static String cdmServer = "LAPTOPHP";
//	static String cdmDB = "cdmTest";
//	//static int cdmPort = 1433;
//	static String cdmUserName = "edit";
//	static String cdmPwd = "wp5";
	
	static DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
	static String cdmServer = "192.168.2.10";
	static String cdmDB = "cdm_1_1";
	//static int cdmPort = 1247;
	static String cdmUserName = "edit";
	static String cdmPwd = "wp5";


	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start import from BerlinModel ...");
		Source source = berlinModelSource;
		CdmApplicationController cdmApp;
		
		//make CdmApplication
		String dataSourceName;
		dataSourceName = "cdmImportLibrary";
//		dataSourceName = "testSqlServer";	
		
		//make BerlinModel Source
		source = BerlinModelSources.euroMed();
		if (source == null){
			logger.error("Connection to BerlinModel could not be established");
			System.out.println("End import from BerlinModel ...");
			return;
		}
	
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
	
	

	
	

}
