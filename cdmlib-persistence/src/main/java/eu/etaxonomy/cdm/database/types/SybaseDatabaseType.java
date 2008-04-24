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
 * !! UNTESTED !!
 * @author a.mueller
 *
 */
public class SybaseDatabaseType extends DatabaseTypeBase {

	//typeName
	protected String typeName = "Sybase";
	//class
	protected String classString = "com.sybase.jdbc2.jdbc.SybDriver";
	//url
    protected String urlString = "jdbc:sybase:Tds:";
    //port
    private int defaultPort = 4100;
    //hibernate dialect
    private String hibernateDialect = "SybaseDialect";

    
    //connection String
	public String getConnectionString(String server, String database, int port){
        return urlString + server + ":" + port + "/" + database;
    }  
    
    public SybaseDatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}




}
