// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.app.pesi;


import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.io.common.Source;

/**
 * @author e.-m.lee
 * @date 16.02.2010
 *
 */
public class PesiDestinations {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PesiDestinations.class);
	
	public static Source pesi_test_local_PESI_V10(){
		//	CDM - PESI
		String dbms = Source.SQL_SERVER_2008;
		String strServer = "C206\\MSSQLSERVER2";
		String strDB = "PESI_V10";
		int port = 1433;
		String userName = "sa";
		String pwd = "bewell";
		return makeSource(dbms, strServer, strDB, port, userName, pwd);
	}
	
	public static Source pesi_test_local_CDM_DWH_FaEu(){
		//	CDM - PESI
		String dbms = Source.SQL_SERVER_2008;
		String strServer = "C206\\MSSQLSERVER2";
		String strDB = "CDM_DWH_FaEu";
		int port = 1433;
		String userName = "sa";
		String pwd = "bewell";
		return makeSource(dbms, strServer, strDB, port, userName, pwd);
	}
	
	public static Source pesi_test_bgbm42_CDM_DWH_FaEu(){
		//	CDM - PESI
		String dbms = Source.SQL_SERVER_2008;
		String strServer = "BGBM42";
		String strDB = "CDM_DWH_FaEu";
		int port = 1433;
		String userName = "WebUser";
		String pwd = "";
		return makeSource(dbms, strServer, strDB, port, userName, pwd);
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
