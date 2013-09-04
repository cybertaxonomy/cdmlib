/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database.types;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.Oracle10gDialect;

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
    private Dialect hibernateDialect = new Oracle10gDialect();

    private String dbSeparator = ":";
    
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.types.IDatabaseType#getServerNameByConnectionString(java.lang.String)
	 */
	public String getServerNameByConnectionString(String connectionString){
    	return getServerNameByConnectionString(connectionString, urlString, dbSeparator);
    }
    
    //connection String
	public String getConnectionString(ICdmDataSource ds, int port){
        return urlString + ds.getServer() + ":" + port + dbSeparator + ds.getDatabase();
    }  
    

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.types.DatabaseTypeBase#getPortByConnectionString(java.lang.String)
	 */
	@Override
	public int getPortByConnectionString(String connectionString) {
		return getPortByConnectionString(connectionString, urlString, dbSeparator);
	}
	
    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.database.types.DatabaseTypeBase#getServerNameByConnectionString(java.lang.String)
     */
    public String getDatabaseNameByConnectionString(String connectionString){
    	String result;
    	result = getDatabasePartOfConnectionString(connectionString, dbSeparator);
    	//returns port also because port separator == db separator
    	if (result != null){
	    	int pos = result.indexOf(dbSeparator);
    		if (pos != -1 ){
	    		result = result.substring(pos+ dbSeparator.length());
	    	}
    	}
	    	
    	//TODO
//    	int posParams = result.indexOf("?");
//    	if (posParams != -1){
//    		result = result.substring(0, posParams);
//    	}
     	return result;
    }

	
    /**
     * Constructor
     */
	public OracleDatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}


}
