/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database.types;


/**
 * @author a.mueller
 *
 */
public class MySQLDatabaseType extends DatabaseTypeBase {

	//typeName
	protected String typeName = "MySQL";
   
	//class
	protected String classString = "com.mysql.jdbc.Driver";
    
	//url
    protected String urlString = "jdbc:mysql://";
    
    //port
    private int defaultPort = 3306;
    
    
    //hibernate dialect
    private String hibernateDialect = "MySQLDialect";

    
    //connection String
	public String getConnectionString(String server, String database, int port){
        return urlString + server + ":" + port + "/" + database;
    }  
    
    public MySQLDatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}




}
