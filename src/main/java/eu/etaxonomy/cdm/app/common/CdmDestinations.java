/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;

public class CdmDestinations {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CdmDestinations.class);
	
	/**
	 * Intended to be used for imports
	 */
	public static ICdmDataSource import_a(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "localhost";
		String cdmDB = "import_a";
		String cdmUserName = "root";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}

	public static ICdmDataSource cdm_test_alex(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_production_palmae"; 
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_redlist_localhost(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "localhost";
		String cdmDB = "vaadinDB"; 
		String cdmUserName = "root";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	

	public static ICdmDataSource cdm_test_useSummary(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "localhost";
		String cdmDB = "palmae_2011_07_17"; 
		String cdmUserName = "root";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_test_local_mysql(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "cdm_test"; 
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	
	public static ICdmDataSource cdm_test_local_mysql_moose(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "moose"; 
		String cdmUserName = "root";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_test_local_mysql_standardliste(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "standardliste"; 
		String cdmUserName = "root";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	

	public static ICdmDataSource cdm_test_local_mysql_dwca(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "dwca"; 
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}

	
	public static ICdmDataSource cdm_test_local_mysql_fdac(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "fdac"; 
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_test_local_mysql_test(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "test"; 
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_corvidae_dev(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.201";
		String cdmDB = "cdm_corvidae"; 
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}

	public static ICdmDataSource cdm_ildis_dev(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.201";
		String cdmDB = "cdm_edit_ildis";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
//
//	public static ICdmDataSource cdm_ildis_production(){
//		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
//		String cdmServer = "160.45.63.171";
//		String cdmDB = "cdm_edit_ildis";
//		String cdmUserName = "edit";
//		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
//	}
	

	public static ICdmDataSource cdm_redlist_moose_dev(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.201";
		String cdmDB = "cdm_mt_moose";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_redlist_standardlist_dev(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.201";
		String cdmDB = "cdm_mt_standardliste";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_cyprus_dev(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.201";
		String cdmDB = "cdm_cyprus";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_cyprus_production(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.171";
		String cdmDB = "cdm_production_cyprus";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}

	public static ICdmDataSource cdm_cyprus_production_tunnel(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		int port = 13306;
		String cdmDB = "cdm_production_cyprus";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, port, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_cyprus_dev_tunnel(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		int port = 13306;
		String cdmDB = "cdm_cyprus";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, port, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_campanulaceae_production(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.171";
		String cdmDB = "cdm_production_campanulaceae";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_flora_malesiana_preview(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.201";
		String cdmDB = "cdm_edit_flora_malesiana";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}

	public static ICdmDataSource cdm_flora_malesiana_production(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.171";
		String cdmDB = "cdm_production_flora_malesiana";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_flora_central_africa_preview(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.201";
		String cdmDB = "cdm_edit_flora_central_africa";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_flora_central_africa_production(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.171";
		String cdmDB = "cdm_production_flora_central_africa";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_pesi_euroMed(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_pesi_euroMed";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_pesi_all(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_pesi_all";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_portal_test_localhost(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "cdm_portal_test";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_portal_test_localhost2(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "cdm_portal_test2";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_local_cichorieae(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "cdm_edit_cichorieae";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_local_dipera(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "cdm_edit_diptera";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_local_palmae(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "cdm_edit_palmae";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_globis_dev(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.201";
		String cdmDB = "cdm_edit_globis";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_local_globis(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "cdm_globis";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}

	public static ICdmDataSource cdm_local_postgres_CdmTest(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.PostgreSQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "CdmTest";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}

	public static ICdmDataSource cdm_local_tdwg2010(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "cdm_tdwg2010";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	
	public static ICdmDataSource NULL(){
		return null;
	}
	
	
	public static ICdmDataSource localH2(){
		return CdmDataSource.NewH2EmbeddedInstance("cdm", "sa", "");
	}
	
	public static ICdmDataSource localH2(String database, String username, String filePath){
		return CdmDataSource.NewH2EmbeddedInstance(database, "sa", "", filePath, null);
	}
	
	public static ICdmDataSource localH2Salvador(){
		return CdmDataSource.NewH2EmbeddedInstance("salvador", "sa", "");
	}
	
	public static ICdmDataSource localH2Diptera(){
		return CdmDataSource.NewH2EmbeddedInstance("diptera", "sa", "");
	}
	
	
	public static ICdmDataSource localH2Cichorieae(){
		return CdmDataSource.NewH2EmbeddedInstance("cichorieae", "sa", "");
	}
	
	public static ICdmDataSource localH2Palmae(){
		return CdmDataSource.NewH2EmbeddedInstance("palmae", "sa", "");
	}
	
	public static ICdmDataSource localH2EuroMed(){
		return CdmDataSource.NewH2EmbeddedInstance("euroMed", "sa", "");
	}
	
	public static ICdmDataSource localH2Erms(){
		return CdmDataSource.NewH2EmbeddedInstance("erms", "sa", "");
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
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}

	public static ICdmDataSource cdm_algaterra_preview(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.201";
		String cdmDB = "cdm_edit_algaterra"; 
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}

	public static ICdmDataSource cdm_algaterra_production(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.171";
		String cdmDB = "cdm_production_algaterra"; 
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_edit_cichorieae_local_PG(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.PostgreSQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "cdm_edit_cichorieae_a"; 
		String cdmUserName = "edit";
		int port = 15432;
		return makeDestination(dbType, cdmServer, cdmDB, port, cdmUserName, null);
	}

	public static ICdmDataSource cdm_cichorieae_preview(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.201";
		String cdmDB = "cdm_edit_cichorieae"; 
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_production_cichorieae(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.171";
//		String cdmServer = "127.0.0.1";
		String cdmDB = "cdm_production_cichorieae"; 
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_production_palmae(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.171";
		String cdmDB = "cdm_production_palmae"; 
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	
	public static ICdmDataSource cdm_production_diptera(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.171";
		String cdmDB = "cdm_production_diptera"; 
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource local_cdm_edit_cichorieae_a(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "cdm_edit_cichorieae_a"; 
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}

	public static ICdmDataSource cdm_edit_palmae_a(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_edit_palmae_a";
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_edit_cichorieae_preview(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "cdm_edit_cichorieae"; 
		String cdmUserName = "edit";
		int port = 13306;
		return makeDestination(dbType, cdmServer, cdmDB, port, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_edit_cichorieae_preview_direct(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "160.45.63.201";
		String cdmDB = "cdm_edit_cichorieae"; 
		String cdmUserName = "edit";
		int port = 3306;
		return makeDestination(dbType, cdmServer, cdmDB, port, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_edit_cichorieae_integration(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "cdm_integration_cichorieae"; 
		String cdmUserName = "edit";
		int port = 13306;
		return makeDestination(dbType, cdmServer, cdmDB, port, cdmUserName, null);
	}

	public static ICdmDataSource cdm_edit_palmae_preview(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "127.0.0.1";
		String cdmDB = "cdm_edit_palmae"; 
		String cdmUserName = "edit";
		int port = 13306;
		return makeDestination(dbType, cdmServer, cdmDB, port, cdmUserName, null);
	}		
	
	public static ICdmDataSource cdm_edit_salvador(){
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_edit_salvador"; 
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_import_salvador() {
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_import_salvador"; 
		String cdmUserName = "edit";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	public static ICdmDataSource cdm_salvador_production() {
		DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "salvador_cdm"; 
		String cdmUserName = "salvador";
		return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
	}
	
	/**
     * patricia
     */
    public static ICdmDataSource mon_cdm() {
        DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
        String cdmServer = "localhost";
        String cdmDB = "cdm_local";
        String cdmUserName = "root";
        return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
    }
    
   public static ICdmDataSource proibiosphere_local() {
        DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
        String cdmServer = "localhost";
        String cdmDB = "cdm_production_proibiosphere_chenopodium_pilot";
        String cdmUserName = "root";
        return makeDestination(dbType, cdmServer, cdmDB, -1, cdmUserName, null);
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
	 * TODO only supports MySQL and PostgreSQL
	 * 
	 * @param dbType
	 * @param cdmServer
	 * @param cdmDB
	 * @param port
	 * @param cdmUserName
	 * @param pwd
	 * @return
	 */
	public static ICdmDataSource makeDestination(DatabaseTypeEnum dbType, String cdmServer, String cdmDB, int port, String cdmUserName, String pwd ){
		//establish connection
		pwd = AccountStore.readOrStorePassword(cdmServer, cdmDB, cdmUserName, pwd);
		ICdmDataSource destination;
		if(dbType.equals(DatabaseTypeEnum.MySQL)){
			destination = CdmDataSource.NewMySqlInstance(cdmServer, cdmDB, port, cdmUserName, pwd, null);			
		} else if(dbType.equals(DatabaseTypeEnum.PostgreSQL)){
			destination = CdmDataSource.NewPostgreSQLInstance(cdmServer, cdmDB, port, cdmUserName, pwd, null);			
		} else {
			//TODO others
			throw new RuntimeException("Unsupported DatabaseType");
		}
		return destination;

	}


	/**
	 * Accepts a string array and tries to find a method returning an ICdmDataSource with 
	 * the name of the given first string in the array
	 * 
	 * @param args
	 * @return
	 */
	public static ICdmDataSource chooseDestination(String[] args) {
		if(args == null)
			return null;
		
		if(args.length != 1)
			return null;
		
		String possibleDestination = args[0];
		
		Method[] methods = CdmDestinations.class.getMethods();
		
		for (Method method : methods){
			if(method.getName().equals(possibleDestination)){
				try {
					return (ICdmDataSource) method.invoke(null, null);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}

