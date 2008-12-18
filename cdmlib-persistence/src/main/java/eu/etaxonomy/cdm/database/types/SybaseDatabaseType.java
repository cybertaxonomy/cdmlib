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
	public String getConnectionString(ICdmDataSource ds, int port){
        return urlString + ds.getServer()+ ":" + port + "/" + ds.getDatabase();
    }
	
    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.database.types.DatabaseTypeBase#getServerNameByConnectionString(java.lang.String)
     */
	@Override
    public String getDatabaseNameByConnectionString(String connectionString){
    	String result;
    	result = getDatabasePartOfConnectionString(connectionString);
    	//TODO
//    	int posParams = result.indexOf("?");
//    	if (posParams != -1){
//    		result = result.substring(0, posParams);
//    	}
     	return result;
    }   
    
    public SybaseDatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}

}
