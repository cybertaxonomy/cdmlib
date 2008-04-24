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
public class PostgreSQLDatabaseType extends DatabaseTypeBase {

	//typeName
	protected String typeName = "PostgreSQL";
	//class
	protected String classString = "org.postgresql.Driver";
	//url
    protected String urlString = "jdbc:postgresql://";
    //port
    private int defaultPort = 5432;
    //hibernate dialect
    private String hibernateDialect = "PostgreSQLDialect";

    
    //connection String
	public String getConnectionString(String server, String database, int port){
        return urlString + server + ":" + port + "/" + database;
    }  
    
    public PostgreSQLDatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}




}
