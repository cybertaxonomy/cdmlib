/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database.types;

import eu.etaxonomy.cdm.database.ICdmDataSource;


/**
 * !! UNTESTED !!
 * Db2 in use with Universal Driver (db2jcc.jar)
 * 
 * @author a.mueller
 *
 */
public class Db2DatabaseType extends DatabaseTypeBase {

	//typeName
	protected String typeName = "DB2 Universal";
	//class
	protected String classString = "com.ibm.db2.jcc.DB2Driver"; 
	//protected String classString = "COM.ibm.db2.jdbc.app.DB2Driver";
	//protected String classString = "COM.ibm.db2.jdbc.net.DB2Driver";
	
	//url
    protected String urlString = "jdbc:db2://";
    //port
    private int defaultPort = 50000;
    //hibernate dialect
    private String hibernateDialect = "DB2Dialect";

    
    //connection String
	public String getConnectionString(ICdmDataSource ds, int port){
		
		return urlString + ds.getServer() + ":" + port + ds.getDatabase();
    }  
    
    public Db2DatabaseType() {
    	init (typeName, classString, urlString, defaultPort, hibernateDialect );
	}


}
