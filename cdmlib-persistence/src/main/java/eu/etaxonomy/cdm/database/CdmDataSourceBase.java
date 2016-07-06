/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License VeresultSetion 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.config.CdmSource;
import eu.etaxonomy.cdm.config.CdmSourceException;
import eu.etaxonomy.cdm.database.types.IDatabaseType;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.MetaDataPropertyName;

/**
 * @author a.mueller
 * @created 18.12.2008
 * @veresultSetion 1.0
 */
abstract class CdmDataSourceBase extends CdmSource implements ICdmDataSource  {

    private static final Logger logger = Logger.getLogger(CdmDataSourceBase.class);


    //	private static final int TIMEOUT = 10;
    private Connection connection;


    @Override
    public Connection getConnection() throws SQLException {
        return getConnection(getUsername(), getPassword());
    }


    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        try {
            if(connection != null){
                boolean isValid = true;
//				try{
//					isValid = connection.isValid(TIMEOUT);
//				} catch (java.lang.AbstractMethodError e){
//					logger.error("Problems with Connection.isValid method\n" + "Exception: " + e.toString());
//				}
                if (isValid){
                    return connection;
                }
            }else{
                IDatabaseType dbType = getDatabaseType().getDatabaseType();
                String classString = dbType.getClassString();
                Class.forName(classString);
                String mUrl = dbType.getConnectionString(this);
                Connection connection = DriverManager.getConnection(mUrl, username, password);
                return 	connection;
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Database driver class could not be loaded\n" + "Exception: " + e.toString(),e);
        } catch(SQLException e) {
            throw new RuntimeException("Problems with database connection\n" + "Exception: " + e.toString(), e);
        }
        return null;
    }

    @Override
    public boolean testConnection() throws ClassNotFoundException, SQLException {

        IDatabaseType dbType = getDatabaseType().getDatabaseType();
        String classString = dbType.getClassString();
        Class.forName(classString);
        String mUrl = dbType.getConnectionString(this);
        if(logger.isDebugEnabled()){
            logger.debug("testConnection() : " + mUrl);
        }

        if(logger.isDebugEnabled()){
            logger.debug("testConnection() : " + mUrl + " : service is available");
        }
        // try to connect to the database server
        Connection connection = DriverManager.getConnection(mUrl, getUsername(), getPassword());
        if (connection != null){
            if(logger.isDebugEnabled()){
                logger.debug("testConnection() : " + mUrl + " : jdbc connect successful");
            }
            return true;
        }

        if(logger.isDebugEnabled()){
            logger.debug("testConnection() : " + mUrl + " : FAIL");
        }
        return false;
    }

	@Override
	public boolean checkConnection() throws CdmSourceException {
		try {
			return testConnection();
		} catch (ClassNotFoundException e) {
			throw new CdmSourceException(e.getMessage());
		} catch (SQLException e) {
			throw new CdmSourceException(e.getMessage());
		}
	}

	@Override
	public String getConnectionMessage() {
		String message = "";
		if (getDatabaseType().equals(DatabaseTypeEnum.H2)) {
			message = " local CDM Store ";
		} else {
			message = " CDM Community Store ";
		}
		message += "'" + getName() + "'";

		message = "Connecting to" + message + ".";

		return message;
	}

    @Override
    public Object getSingleValue(String query) throws SQLException{
        String queryString = query == null? "(null)": query;
        ResultSet resultSet = executeQuery(query);
        if (resultSet == null || resultSet.next() == false){
            logger.info("No record returned for query " +  queryString);
            return null;
        }
        if (resultSet.getMetaData().getColumnCount() != 1){
            logger.info("More than one column selected in query" +  queryString);
            //first value will be taken
        }
        Object object = resultSet.getObject(1);
        if (resultSet.next()){
            logger.info("Multiple results for query " +  queryString);
            //first row will be taken
        }
        return object;
    }

	@Override
	public  String getDbSchemaVersion() throws CdmSourceException  {
		try {
			return (String)getSingleValue(MetaDataPropertyName.DB_SCHEMA_VERSION.getSqlQuery());
		} catch (SQLException e) {
			throw new CdmSourceException(e.getMessage());
		}
	}

	@Override
	public boolean isDbEmpty() throws CdmSourceException {
		// Any CDM DB should have a schema version
		String dbSchemaVersion = getDbSchemaVersion();

		return (dbSchemaVersion == null || dbSchemaVersion.equals(""));
	}
    /**
     * Executes a query and returns the ResultSet.
     * @return ResultSet for the query.
     * @throws SQLException
     */
    @Override
    public ResultSet executeQuery (String query) throws SQLException {

        ResultSet resultSet;

        if (query == null){
            return null;
        }
        Connection connection = getConnection();
        if (connection != null){
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
        }else{
            throw new RuntimeException("Could not establish connection to database");
        }
        return resultSet;

    }

    @Override
    public int executeUpdate (String sqlUpdate) throws SQLException{

        int result;
        Connection connection = null;
        try {
            if (sqlUpdate == null){
                return 0;
            }
            connection = getConnection();
            Statement statement = connection.createStatement();
            result = statement.executeUpdate(sqlUpdate);
            return result;
        } catch(SQLException e) {
            try{
                if (connection != null && ! connection.getAutoCommit()){
                    connection.rollback();
                }
            }catch (SQLException ex){
                //do nothing -  maybe throw RuntimeException in future
                throw new RuntimeException(ex);
            }
            logger.error("Problems when executing update\n  " + sqlUpdate + " \n" + "Exception: " + e);
            throw e;
        }
    }

    @Override
    public void startTransaction() {
        try {
            Connection connection = getConnection();
            this.connection = connection;
            connection.setAutoCommit(false);
            return;
        } catch(SQLException e) {
            logger.error("Problems when starting transaction \n" + "Exception: " + e);
            return;
        }
    }

    @Override
    public void commitTransaction() throws SQLException {
        try {
            Connection connection = getConnection();
            connection.commit();
        } catch(SQLException e) {
            logger.error("Problems when commiting transaction \n" + "Exception: " + e);
            throw e;
        }
    }

    @Override
    public void rollback() throws SQLException {
        try {
            Connection connection = getConnection();
            connection.rollback();
        } catch(SQLException e) {
            logger.error("Problems when rolling back transaction \n" + "Exception: " + e);
            throw e;
        }
    }


    @Override
    public DatabaseMetaData getMetaData() {
        Connection connection = null;
        try {
            connection = getConnection();
            return connection.getMetaData();
        } catch (SQLException e) {
            logger.error("Could not get metadata for datasource", e);
            return null;
        }
    }

    @Override
    public void closeOpenConnections() {
        try {
            if(connection != null && !connection.isClosed()){
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            logger.error("Error closing the connection");
        }
    }


    @Override
    public Map<MetaDataPropertyName, String> getMetaDataMap() throws CdmSourceException {
		Map<MetaDataPropertyName, String> cdmMetaDataMap = new HashMap<MetaDataPropertyName, String>();

		for(MetaDataPropertyName mdpn : MetaDataPropertyName.values()) {
			String value = null;
			try {
				value = (String)getSingleValue(mdpn.getSqlQuery());
			} catch (SQLException e) {
				throw new CdmSourceException(this.toString(), e.getMessage());
			}
			if(value != null) {
				cdmMetaDataMap.put(mdpn, value);
			}
		}
		return cdmMetaDataMap;
    }

    // ************ javax.sql.DataSource base interfaces ********************/


    @Override
    public PrintWriter getLogWriter() throws SQLException {
        //implementations copied from org.springframework.jdbc.datasource.AbstractDataSource;
        throw new UnsupportedOperationException("getLogWriter");
    }


    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        //implementations copied from org.springframework.jdbc.datasource.AbstractDataSource;
        throw new UnsupportedOperationException("setLogWriter");
    }


    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        //implementations copied from org.springframework.jdbc.datasource.AbstractDataSource;
        throw new UnsupportedOperationException("setLoginTimeout");
    }


    @Override
    public int getLoginTimeout() throws SQLException {
        //implementations copied from org.springframework.jdbc.datasource.AbstractDataSource;
        return 0;
    }


    /*
     * This is a preliminary implementation to be compliant with
     * java.sql.Datasource (1.6). It may not be fully working.
     * Please let the developers know if this doesn't work.
     */

    //---------------------------------------------------------------------
    // Implementation of JDBC 4.0's Wrapper interface
    //---------------------------------------------------------------------

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        throw new SQLException("DataSource of type [" + getClass().getName() +
                "] cannot be unwrapped as [" + iface.getName() + "]");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this);
    }


    //---------------------------------------------------------------------
    // Implementation of JDBC 4.1's getParentLogger method
    // Required in Java >=7.x
    // must not have the @Override annotation for compatibility with
    // java 1.6
    //---------------------------------------------------------------------

    @Override
    public java.util.logging.Logger getParentLogger() {
        //copied from org.springframework.jdbc.datasource.AbstractDataSource, not checked if this is correct
        return java.util.logging.Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
    }


}
