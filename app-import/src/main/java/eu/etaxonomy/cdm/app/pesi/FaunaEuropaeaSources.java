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
import eu.etaxonomy.cdm.io.common.Source;

/**
 * @author a.babadshanjan
 * @created 12.05.2009
 */
public class FaunaEuropaeaSources {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaSources.class);
	
	public static Source faunEu(){
		//	Fauna Europaea
		//String dbms = Source.SQL_SERVER_2000;
		String dbms = Source.SQL_SERVER_2005;
//		String strServer = "160.45.63.37";
		String strServer = "BGBM14";               // "192.168.1.36";
		String strDB = "FaunEu";
		int port = 1433;
		String userName = "WebUser";
		return  ImportUtils.makeSource(dbms, strServer, strDB, port, userName, null);
	}
	
}
