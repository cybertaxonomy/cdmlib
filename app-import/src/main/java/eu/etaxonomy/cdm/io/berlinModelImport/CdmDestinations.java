/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

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
		String cdmDB = "cdm_build"; // values: "cdm_1_1"  "cdm_build"
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}

	
	public static ICdmDataSource cdm_test(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_test"; // values: "cdm_1_1"  "cdm_build"
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	
	public static ICdmDataSource cdm_test_andreasM(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_test_andreasM"; 
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	
	public static ICdmDataSource cdm_editor2(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_editor_test2"; 
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
		public static ICdmDataSource cdm_portal(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_portal";
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_portal_test(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_portal_test";
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_portal_test_localhost(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "cdm_portal_test";
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_portal_test_pollux(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.11";
		String cdmDB = "cdm_portal_test";
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_edit_cichorieae(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_edit_cichorieae"; 
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_edit_diptera(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_edit_diptera"; 
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
		
	public static ICdmDataSource cdm_edit_palmae(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_edit_palmae"; 
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
		AccountStore accounts = new AccountStore();
		String strServer = "cdm-server";
		boolean doStore = false;
		try {
			if (pwd == null){
				pwd = accounts.getPassword(strServer, cdmServer, cdmUserName);
				if(pwd == null){
					doStore = true;
					pwd = CdmUtils.readInputLine("Please insert password for " + CdmUtils.Nz(cdmUserName) + ": ");
				} else {
					logger.info("using stored password for "+CdmUtils.Nz(cdmUserName));
				}
			}
			//TODO not MySQL
			ICdmDataSource destination = CdmDataSource.NewMySqlInstance(cdmServer, cdmDB, port, cdmUserName, pwd);
			// on success store userName, pwd in property file
			if(doStore){
				accounts.setPassword(strServer, cdmServer, cdmUserName, pwd);
				logger.info("password stored in "+accounts.accountsFile);
			}
			return destination;
		} catch (Exception e) {
			if(doStore){
				accounts.removePassword(strServer, cdmServer, cdmUserName);
				logger.info("password removed from "+accounts.accountsFile);
			}
			logger.error(e);
			return null;
		}
	}

}
