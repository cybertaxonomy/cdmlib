
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.berlinModelImport;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.io.common.Source;


public class BerlinModelSources {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BerlinModelSources.class);
	
	public static Source euroMed(){
		//	BerlinModel - Euro+Med
		String dbms = Source.SQL_SERVER_2005;
		String strServer = "BGBM111";
		String strDB = "EuroPlusMed_00_Edit";
		int port = 1247;
		String userName = "webUser";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}
	
	public static Source Campanulaceae(){
		//	BerlinModel - Campanulaceae
		String dbms = Source.SQL_SERVER_2005;
		String strServer = "BGBM111";
		String strDB = "Campanulaceae";
		int port = 1247;
		String userName = "webUser";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}
	
	public static Source ILDIS(){
		//	BerlinModel - EditWP6
		String dbms = Source.SQL_SERVER_2008;
		String strServer = "BGBM42";
		String strDB = "ILDIS_EM_BM";
		int port = 1433;
		String userName = "webUser";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}
	
	public static Source EDIT_CICHORIEAE(){
		//	BerlinModel - EditWP6
		String dbms = Source.SQL_SERVER_2005;
		String strServer = "BGBM111";
		String strDB = "EditWP6";
		int port = 1247;
		String userName = "webUser";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}
	
	public static Source EDIT_Taraxacum(){
		//	BerlinModel - EditWP6
		String dbms = Source.SQL_SERVER_2005;
		String strServer = "BGBM111";
		String strDB = "Edit_Taraxacum";
		int port = 1247;
		String userName = "webUser";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}

	public static Source EDIT_Diptera(){
		//	BerlinModel - EDIT_Diptera
		String dbms = Source.SQL_SERVER_2005;
		String strServer = "BGBM111";
		String strDB = "EDIT_Diptera";
		int port = 1247;
		String userName = "webUser";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}
	
	public static Source EDIT_Palmae(){
		//	BerlinModel - EDIT_Palmae
		String dbms = Source.SQL_SERVER_2005;
		String strServer = "BGBM111";
		String strDB = "EDIT_Palmae";
		int port = 1247;
		String userName = "webUser";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}

	public static Source El_Salvador(){
		//	BerlinModel - El_Salvador
		String dbms = Source.SQL_SERVER_2005;
		String strServer = "SQL2000Intern";
		String strDB = "Salvador";
		int port = 1433;
		String userName = "WebUser";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}
	
	public static Source El_Salvador_Local(){
		//	BerlinModel - El_Salvador
		String dbms = Source.SQL_SERVER_2005;
		String strServer = "LENOVO-T61";
		String strDB = "Salvador";
		int port = 1433;
		String userName = "salvadorExport";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}

	
	public static Source AlgaTerra(){
		//	BerlinModel - AlgaTerra
		String dbms = Source.SQL_SERVER_2005;
		String strServer = "BGBM17";
		String strDB = "Algaterra";
		int port = 1433;
		String userName = "WebUser";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}
	
	public static Source PESI_ERMS(){
		//	BerlinModel - Pesi-ERMS
		String dbms = Source.SQL_SERVER_2005;
		String strServer = "SQL2000Intern\\SQL2005";
		String strDB = "BM_ERMS";
		int port = 1433;
		String userName = "WebUser";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}
	
	/**
	 * Initializes the source.
	 * @param dbms
	 * @param strServer
	 * @param strDB
	 * @param port
	 * @param userName
	 * @param pwd
	 * @return the source
	 */
	private static Source makeSource(String dbms, String strServer, String strDB, int port, String userName, String pwd ){
		//establish connection
		Source source = null;
		source = new Source(dbms, strServer, strDB);
		source.setPort(port);
			
		pwd = AccountStore.readOrStorePassword(dbms, strServer, userName, pwd);
		source.setUserAndPwd(userName, pwd);
		// write pwd to account store
		return source;
	}

}
