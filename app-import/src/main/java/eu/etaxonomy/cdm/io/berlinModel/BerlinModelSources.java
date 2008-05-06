package eu.etaxonomy.cdm.io.berlinModel;

import java.io.BufferedReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.source.Source;

public class BerlinModelSources {
	private static Logger logger = Logger.getLogger(BerlinModelSources.class);
	
	public static Source euroMed(){
		//	BerlinModel - Euro+Med
		String dbms = "SQLServer";
		String strServer = "BGBM111";
		String strDB = "EuroPlusMed_00_Edit";
		int port = 1247;
		String userName = "webUser";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}
	
	public static Source editWP6(){
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
	 * initializes source
	 * @return true, if connection establisehd
	 */
	private static Source makeSource(String dbms, String strServer, String strDB, int port, String userName, String pwd ){
		//establish connection
		try {
			Source source = new Source(dbms, strServer, strDB);
			source.setPort(port);
			if (pwd == null){
				pwd = CdmUtils.readInputLine("Please insert password for " + CdmUtils.Nz(userName) + ": ");
			}
			source.setUserAndPwd(userName, pwd);
			return source;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

}
