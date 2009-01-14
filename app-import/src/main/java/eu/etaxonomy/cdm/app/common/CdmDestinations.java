/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.common;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.AccountStore;
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
	
	
	public static ICdmDataSource cdm_test_anahit(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_test_anahit"; 
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_test_anahit2(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_test_anahit2"; 
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_test_jaxb(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_test_jaxb"; 
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_test_jaxb2(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_test_jaxb2"; 
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
	
	public static ICdmDataSource cdm_test_andreasM2(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_test_andreasM2"; 
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_test_andreasM3(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_test_andreasM3"; 
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
	
	public static ICdmDataSource cdm_test_patricia(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_test_patricia";
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}

	public static ICdmDataSource cdm_test_niels1(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_test_niels1";
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_test_niels2(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_test_niels2";
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	//
	public static ICdmDataSource cdm_test_andreasK1(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_test_andreasK1";
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_pesi_erms(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_pesi_erms";
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
	
	public static ICdmDataSource cdm_portal_test_localhost2(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "cdm_portal_test2";
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource NULL(){
		return null;
	}
	
	public static ICdmDataSource localH2(){
		return CdmDataSource.NewH2EmbeddedInstance("cdm", "sa", "");
	}
	
	public static ICdmDataSource localH2_viola(){
		return CdmDataSource.NewH2EmbeddedInstance("testViola", "sa", "");
	}
	
	public static ICdmDataSource localH2_LIAS(){
		return CdmDataSource.NewH2EmbeddedInstance("testLIAS", "sa", "");
	}
	
	public static ICdmDataSource localH2_Erythroneura(){
		return CdmDataSource.NewH2EmbeddedInstance("testErythroneura", "sa", "");
	}
	
	public static ICdmDataSource localH2_Cicad(){
		return CdmDataSource.NewH2EmbeddedInstance("testCicad", "sa", "");
	}
	
	public static ICdmDataSource localH2_ValRosandraFRIDAKey(){
		return CdmDataSource.NewH2EmbeddedInstance("testValRosandraFRIDAKey", "sa", "");
	}
	
	public static ICdmDataSource localH2_FreshwaterAquaticInsects(){
		return CdmDataSource.NewH2EmbeddedInstance("testFreshwaterAquaticInsects", "sa", "");
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
	
	public static ICdmDataSource cdm_v1_cichorieae(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_v1_cichorieae"; 
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
		
	public static ICdmDataSource cdm_v1_diptera(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_v1_diptera"; 
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
	
	public static ICdmDataSource cdm_v1_palmae(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_v1_palmae"; 
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_edit_salvador(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_edit_salvador"; 
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
		pwd = AccountStore.readOrStorePassword(cdmServer, cdmDB, cdmUserName, pwd);
		//TODO not MySQL
		ICdmDataSource destination = CdmDataSource.NewMySqlInstance(cdmServer, cdmDB, port, cdmUserName, pwd);
		return destination;

	}

}

