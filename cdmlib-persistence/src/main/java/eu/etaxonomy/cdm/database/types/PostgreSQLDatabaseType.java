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
import org.hibernate.dialect.PostgreSQL82Dialect;

import eu.etaxonomy.cdm.database.ICdmDataSource;


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
    private final int defaultPort = 5432;
    //hibernate dialect
    private final Dialect hibernateDialect = new PostgreSQL82Dialect();

    private static String dbSeparator = "/";

    //connection String
    @Override
	public String getConnectionString(ICdmDataSource ds, int port){
        return urlString + ds.getServer() + ":" + port + dbSeparator + ds.getDatabase();
    }

	@Override
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


    /**
     * Constructor
     */
    public PostgreSQLDatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}



}
