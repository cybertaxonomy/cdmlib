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
 * TODO not tested yet !!
 * 
 * @author a.mueller
 *
 */
public class OdbcDatabaseType extends DatabaseTypeBase {

	//typeName
	protected String typeName = "ODBC";
   
	//class
	protected String classString = "sun.jdbc.odbc.JdbcOdbcDriver";
    
	//url
    protected String urlString = "jdbc:odbc:";
    
    //port
    private int defaultPort = 0;
    
    //hibernate dialect
    //TODO
    private String hibernateDialect = "xxx";
    
    //connection String
	public String getConnectionString(ICdmDataSource ds, int port){
        return urlString + ds.getServer() ;
    }  
	
	//constructor
    public OdbcDatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
    }




}
