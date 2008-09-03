
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
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.Source;


public class BerlinModelSources {
	private static final Logger logger = Logger.getLogger(BerlinModelSources.class);
	
	public static Source euroMed(){
		//	BerlinModel - Euro+Med
		String dbms = "SQLServer";
		String strServer = "BGBM111";
		String strDB = "EuroPlusMed_00_Edit";
		int port = 1247;
		String userName = "webUser";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}
	
	public static Source EDIT_CICHORIEAE(){
		//	BerlinModel - EditWp6
		String dbms = "SQLServer";
		String strServer = "BGBM111";
		String strDB = "EditWP6";
		int port = 1247;
		String userName = "webUser";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}

	public static Source EDIT_Diptera(){
		//	BerlinModel - EditWp6
		String dbms = "SQLServer";
		String strServer = "BGBM111";
		String strDB = "EDIT_Diptera";
		int port = 1247;
		String userName = "webUser";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}
	
	public static Source EDIT_Palmae(){
		//	BerlinModel - EditWp6
		String dbms = "SQLServer";
		String strServer = "BGBM111";
		String strDB = "EDIT_Palmae";
		int port = 1247;
		String userName = "webUser";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}

	public static Source El_Salvador(){
		//	BerlinModel - EditWp6
		String dbms = "SQLServer";
		String strServer = "SQL2000Intern";
		String strDB = "Salvador";
		int port = 1433;
		String userName = "webUser";
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
