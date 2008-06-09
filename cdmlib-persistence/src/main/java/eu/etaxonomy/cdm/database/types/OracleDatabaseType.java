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
public class OracleDatabaseType extends DatabaseTypeBase {

	//typeName
	protected String typeName = "Oracle";
	//class
	protected String classString = "oracle.jdbc.driver.OracleDriver";
	//url
    protected String urlString = "jdbc:oracle:thin:@";
    //port
    private int defaultPort = 1521;
    //hibernate dialect
    private String hibernateDialect = "OracleDialect";

    
    //connection String
	public String getConnectionString(ICdmDataSource ds, int port){
        return urlString + ds.getServer() + ":" + port + ":" + ds.getDatabase();
    }  
    
    public OracleDatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}




}
