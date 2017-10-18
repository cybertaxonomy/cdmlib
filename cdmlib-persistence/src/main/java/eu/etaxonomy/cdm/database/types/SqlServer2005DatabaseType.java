/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database.types;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.SQLServer2005Dialect;

import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.ICdmDataSource;


/**
 * @author a.mueller
 *
 */
public class SqlServer2005DatabaseType extends DatabaseTypeBase {

	//name
	protected String typeName = "SQL Server";

	//driver class
	protected String classString = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

	//url
	protected String urlString = "jdbc:sqlserver://";

	//[serverName[\instanceName][:portNumber]][;property=value[;property=value]]

    //default port
    protected int defaultPort = 1433;

    //hibernate dialect
    protected Dialect hibernateDialect = new SQLServer2005Dialect();

    @Override
    public String getConnectionString(ICdmDataSource ds, int port){
    	return getConnectionString(ds, port, null);
    }

    public String getConnectionString(ICdmDataSource ds, int port, String instanceName){
    	String instance = "";
    	if (instanceName != null && ! instanceName.equals("")){
			instance = "\\" + instanceName;
		}
    	return urlString + ds.getServer() + instance + ":" + port + ";databaseName=" + ds.getDatabase() +";SelectMethod=cursor";
    }

	@Override
	public String getServerNameByConnectionString(String connectionString) {
		String dbSeparator = ";";
		return super.getServerNameByConnectionString(connectionString, urlString, dbSeparator);
	}

	@Override
	public int getPortByConnectionString(String connectionString) {
		String dbSeparator = ";";
		return getPortByConnectionString(connectionString, urlString, dbSeparator);
	}

    @Override
    public String getDatabaseNameByConnectionString(String connectionString){
    	String result;
    	String dbStart = ";databaseName=";
    	int posDbStart = connectionString.indexOf(dbStart);
    	result = connectionString.substring(posDbStart + dbStart.length());
    	int posNextAttr = result.indexOf(";");
    	if (posNextAttr != 0){
    		result = result.substring(0, posNextAttr);
    	}
     	return result;
    }

	//Constructor
    public SqlServer2005DatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}


    /**
     * Deletes all foreign keys between tables in a sql server database.
     * This makes deleting tables easier.
     * @param sqlServerDataSource
     * @return
     * @throws SQLException
     */
    public boolean deleteForeignKeys(CdmDataSource sqlServerDataSource) throws SQLException{
    	String sql = "SELECT name, id FROM sys.sysobjects WHERE (xtype = 'U')"; //all tables
		ResultSet rs = sqlServerDataSource.executeQuery(sql);
		while (rs.next()){
			String tableName = rs.getString("name");
			long tableId = rs.getLong("id");
			sql = "SELECT name FROM sys.sysobjects WHERE xtype='F' and parent_obj = " +  tableId;//get foreignkeys
			ResultSet rsFk = sqlServerDataSource.executeQuery(sql);
			while (rsFk.next()){
				String fk = rsFk.getString("name");
				sql = " ALTER TABLE "+tableName+" DROP CONSTRAINT "+fk + "";
				sqlServerDataSource.executeUpdate(sql);
			}

		}
		return true;
    }

}
