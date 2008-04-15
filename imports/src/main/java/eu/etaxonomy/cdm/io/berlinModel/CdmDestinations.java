package eu.etaxonomy.cdm.io.berlinModel;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;

public class CdmDestinations {
	private static Logger logger = Logger.getLogger(CdmDestinations.class);
	
	public static ICdmDataSource cdm_1_1(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_1_1"; // values: "cdm_1_1"  "cdm_build"
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	
	public static ICdmDataSource cdm_build(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_1_1"; // values: "cdm_1_1"  "cdm_build"
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}

	
	public static ICdmDataSource cdm_test(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_1_1"; // values: "cdm_1_1"  "cdm_build"
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	
//	public static ICdmDataSource LAPTOP_HP(){
//		DatabaseTypeEnum dbType = DatabaseTypeEnum.SqlServer2005;
//		String cdmServer = "LAPTOPHP";
//		String cdmDB = "cdmTest"; 
//		String cdmUserName = "edit";
//		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
//	}
	
	
	/**
	 * initializes source
	 * @return true, if connection establisehd
	 */
	private static ICdmDataSource makeDestination(String cdmServer, String cdmDB, int port, String cdmUserName, String pwd ){
		//establish connection
		try {
			if (pwd == null){
				pwd = CdmUtils.readInputLine("Please insert password for " + CdmUtils.Nz(cdmUserName) + ": ");
			}
			//TODO not MySQL
			ICdmDataSource destination = CdmDataSource.NewMySqlInstance(cdmServer, cdmDB, port, cdmUserName, pwd);
			return destination;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

}
