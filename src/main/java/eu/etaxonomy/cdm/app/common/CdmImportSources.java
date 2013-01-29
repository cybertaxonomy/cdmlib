// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.app.common;

import java.net.URI;
import java.net.URL;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.berlinModelImport.SourceBase;
import eu.etaxonomy.cdm.app.tcs.TcsSources;
import eu.etaxonomy.cdm.io.common.Source;

/**
 * @author a.mueller
 * @date 21.04.2010
 *
 */
public class CdmImportSources extends SourceBase{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmImportSources.class);
	
	public static Source ROTE_LISTE_DB(){
		String dbms = Source.ORACLE;
		String strServer = "xxx";
		String strDB = "dbName";
		int port = 1433;
		String userName = "adam";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}

	
	public static Source GLOBIS(){
		String dbms = Source.SQL_SERVER_2005;
		String strServer = "LENOVO-T61";
		String strDB = "globis";
		int port = 0001;
		String userName = "user";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}

	public static Source GLOBIS_ODBC(){
		String dbms = Source.ODDBC;
		String strServer = "LENOVO-T61";
		String strDB = "globis";
		int port = 1433;
		String userName = "sa";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}
	
	public static Source GLOBIS_MDB(){
		String dbms = Source.ACCESS;
		String strServer = null;
		String strDB = "C:\\localCopy\\Data\\globis\\globis.mdb";
		int port = -1;
		String userName = "";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}

	public static Source GLOBIS_MDB_20120928(){
		String dbms = Source.ACCESS;
		String strServer = null;
		String strDB = "C:\\localCopy\\Data\\globis\\globis.20120928.mdb";
		int port = -1;
		String userName = "";
		return  makeSource(dbms, strServer, strDB, port, userName, null);
	}
	
//	public static Source GLOBIS_MDB_20120928(){
//		String dbms = Source.ACCESS;
//		String strServer = null;
//		String strDB = "\\\\PESIIMPORT3\\globis\\globis.20120928.mdb";
//		int port = -1;
//		String userName = "";
//		return  makeSource(dbms, strServer, strDB, port, userName, null);
//	}
	
	public static URI SYNTHESYS_SPECIMEN(){
		//		tcsXmlTest.xml
		URL url = new TcsSources().getClass().getResource("/specimen/SynthesysSpecimenExample.xls");
		String sourceUrl = url.toString();
		URI uri = URI.create(sourceUrl);
		return uri;	
	}
	
	
	

}
