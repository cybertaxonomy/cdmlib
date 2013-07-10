/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.pesi;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.ImportUtils;
import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.io.common.Source;

/**
 * @author a.babadshanjan
 * @created 12.05.2009
 */
public class PesiSources {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PesiSources.class);
	
	public static Source faunEu_pesi3(){
		//	Fauna Europaea auf pesiimport3
		String dbms = Source.SQL_SERVER_2008;
        String strServer = "pesiimport3";
        String strDB = "FaunEu";
		int port = 1433;
		String userName = "pesiExportFaunaEu";
		return  ImportUtils.makeSource(dbms, strServer, strDB, port, userName, null);
	}
	
	public static Source faunEu(){
		//	Fauna Europaea
		String dbms = Source.SQL_SERVER_2008;
       	String strServer = "BGBM42";               // "192.168.1.36";
        String strDB = "FaunEu";
		int port = 1433;
		String userName = "WebUser";
		return  ImportUtils.makeSource(dbms, strServer, strDB, port, userName, null);
	}
	
	public static Source faunaEu_previous(){
		//	Fauna Europaea
		String dbms = Source.SQL_SERVER_2008;
 		String strServer = "BGBM42";               // "192.168.1.36";
 		String strDB = "FaunEu_2_2";
		int port = 1433;
		String userName = "WebUser";
		return  ImportUtils.makeSource(dbms, strServer, strDB, port, userName, null);
	}
	public static Source faunaEu_old(){
		//	Fauna Europaea
		String dbms = Source.SQL_SERVER_2008;
		String strServer = "BGBM42";               // "192.168.1.36";
		String strDB = "FaunaEu_1_3";
		int port = 1433;
		String userName = "WebUser";
		return  ImportUtils.makeSource(dbms, strServer, strDB, port, userName, null);
	}
	
	public static Source PESI_ERMS(){
		//	BerlinModel - Pesi-ERMS
		String dbms = Source.SQL_SERVER_2008;
		String strServer = "BGBM42";
		String strDB = "ERMS";
		int port = 1433;
		String userName = "WebUser";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}
	
	
	public static Source PESI3_ERMS(){
		//	BerlinModel - Pesi-ERMS
		String dbms = Source.SQL_SERVER_2008;
		String strServer = "Pesiimport3";
		String strDB = "ERMS";
		int port = 1433;
		String userName = "pesiexport";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}
	
	public static Source PESI_IF(){
		//	BerlinModel - Pesi-IF
		String dbms = Source.SQL_SERVER_2008;
		String strServer = "BGBM42";
		String strDB = "IF";
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
