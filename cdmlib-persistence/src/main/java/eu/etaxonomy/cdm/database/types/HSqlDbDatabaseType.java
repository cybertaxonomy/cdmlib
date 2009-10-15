/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database.types;

import javax.sql.DataSource;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.LocalHsqldb;


/**
 * @author a.mueller
 *
 */
public class HSqlDbDatabaseType extends DatabaseTypeBase {

	//typeName
	private String typeName = "Hypersonic SQL DB (HSqlDb)";
   
	//class
	private String classString = "org.hsqldb.jdbcDriver";
    
	//url
	private String urlString = "jdbc:hsqldb:hsql://";
    
    //port
    private int defaultPort = 9001;
    
    //hibernate dialect
    private String hibernateDialect = "HSQLCorrectedDialect";
    
    //init method
    private String initMethod = "init";
    
    //destroy method
    private String destroyMethod = "destroy";
    
    //connection String
	public String getConnectionString(ICdmDataSource ds, int port){
        return urlString + ds.getServer() + ":" + port + dbSeparator + ds.getDatabase();
    }
	
	private static String dbSeparator = "/";
	
    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.database.types.DatabaseTypeBase#getServerNameByConnectionString(java.lang.String)
     */
    public String getDatabaseNameByConnectionString(String connectionString){
    	String result;
    	result = getDatabasePartOfConnectionString(connectionString, dbSeparator);
    	//TODO
//    	int posParams = result.indexOf("?");
//    	if (posParams != -1){
//    		result = result.substring(0, posParams);
//    	}
     	return result;
    }
	
    
    public HSqlDbDatabaseType() {
		init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}

	@Override
	public Class<? extends DataSource> getDataSourceClass() {
		return LocalHsqldb.class;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.types.IDatabaseType#getInitMethod()
	 */
	@Override
	public String getInitMethod() {
		return initMethod;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.types.IDatabaseType#getDestroyMethod()
	 */
	@Override
	public String getDestroyMethod() {
		return destroyMethod;
	}


}
