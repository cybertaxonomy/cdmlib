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
 * @author a.mueller
 *
 */
public class SqlServer2005DatabaseType extends DatabaseTypeBase {

	//name
	protected String typeName = "SQL Server";
	
	//driver class
	protected String classString = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    
	//url
	protected String urlString = "jdbc:sqlserver://";
    
	//[serverName[\instanceName][:portNumber]][;property=value[;property=value]]
	
    //default port
    private int defaultPort = 1433;
    
    //hibernate dialect
    private String hibernateDialect = "SQLServerDialect";
 
    public String getConnectionString(ICdmDataSource ds, int port){
    	return getConnectionString(ds, port, null);
    }
    
    public String getConnectionString(ICdmDataSource ds, int port, String instanceName){
    	String instance = "";
    	if (instanceName != null && ! instanceName.equals("")){
			instance = "\\" + instanceName;
		}
    	return urlString + ds.getServer() + instance + ":" + port + ";databaseName=" + ds.getDatabase() +";SelectMethod=cursor";
    }
	
	//Constructor
    public SqlServer2005DatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}

}