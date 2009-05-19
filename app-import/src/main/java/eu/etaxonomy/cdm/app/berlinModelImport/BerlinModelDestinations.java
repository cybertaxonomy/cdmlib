
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


public class BerlinModelDestinations {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BerlinModelDestinations.class);
	
	public static Source El_Salvador_Andreas(){
		//	BerlinModel - El_Salvador
		String dbms = Source.SQL_SERVER_2005;
		String strServer = "LENOVO-T61";
		String strDB = "Salvador_export";
		int port = 1433;
		String userName = "salvadorExport";
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
