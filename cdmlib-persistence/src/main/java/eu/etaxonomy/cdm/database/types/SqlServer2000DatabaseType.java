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
public class SqlServer2000DatabaseType extends SqlServer2005DatabaseType {
    
 	//name
	//protected String typeName = "SQL Server";
	
	//driver class
	//protected String classString = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
    
	//url
//	protected String urlString = "jdbc:microsoft:sqlserver://";
    
    //hibernate dialect
   // private String hibernateDialect = "SQLServerDialect";
    
    public String getConnectionString(ICdmDataSource ds, int port, String instanceName){
    	//TODO check if instances exist for SQL Server 2000
    	return super.getConnectionString(ds, port, instanceName);
    }
    
	//Constructor
    public SqlServer2000DatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}
    


}
