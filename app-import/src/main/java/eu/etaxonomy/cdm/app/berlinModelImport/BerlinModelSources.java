
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

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.source.Source;


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

	
	/**
	 * Initialises source
	 * @return true, if connection established
	 */
	private static Source makeSource(String dbms, String strServer, String strDB, int port, String userName, String pwd ){
		//establish connection
		Source source = null;
		AccountStore accounts = new AccountStore();
		boolean doStore = false;
		try {
			source = new Source(dbms, strServer, strDB);
			source.setPort(port);
			
			if (pwd == null){
				pwd = accounts.getPassword(dbms, strServer, userName);
				if(pwd == null){
					doStore = true;
					pwd = CdmUtils.readInputLine("Please insert password for " + CdmUtils.Nz(userName) + ": ");
				} else {
					logger.info("using stored password for  "+CdmUtils.Nz(userName));
				}
			}
			source.setUserAndPwd(userName, pwd);
			// on success store userName, pwd in property file
			if(doStore){
				accounts.setPassword(dbms, strServer, userName, pwd);
				logger.info("password stored in "+accounts.accountsFile);
			}
		} catch (Exception e) {
			if(doStore){
				accounts.removePassword(dbms, strServer, userName);
				logger.info("password removed from "+accounts.accountsFile);
			}
			logger.error(e);
		}
		// write pwd to account store
		return source;
	}

}
