/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License VeresultSetion 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.types.IDatabaseType;

/**
 * @author a.mueller
 * @created 18.12.2008
 * @veresultSetion 1.0
 */
abstract class CdmDataSourceBase implements ICdmDataSource {
	private static final Logger logger = Logger.getLogger(CdmDataSourceBase.class);

	private static final int TIMEOUT = 10;
	private Connection connection;
	

	public Connection getConnection() throws SQLException {
		return getConnection(getUsername(), getPassword());
	}
	

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
			logger.error("Database driver class could not be loaded\n" + "Exception: " + e.toString());
		} catch(SQLException e) {
			logger.error("Problems with database connection\n" + "Exception: " + e.toString());
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.ICdmDataSource#testConnection()
	 */
	public boolean testConnection() throws ClassNotFoundException, SQLException {

		IDatabaseType dbType = getDatabaseType().getDatabaseType();
		String classString = dbType.getClassString();
		Class.forName(classString);
		String mUrl = dbType.getConnectionString(this);
		Connection connection = DriverManager.getConnection(mUrl, getUsername(), getPassword());
		if (connection != null){
			return true;
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.ICdmDataSource#getSingleValue(java.lang.String)
	 */
	@Override
	public Object getSingleValue(String query) throws SQLException{
		String queryString = query == null? "(null)": query;  
		ResultSet resultSet = executeQuery(query);
		if (resultSet == null || resultSet.next() == false){
			logger.warn("No record returned for query " +  queryString);
			return null;
		}
		if (resultSet.getMetaData().getColumnCount() != 1){
			logger.warn("More than one column selected in query" +  queryString);
			return null;
		}
		Object object = resultSet.getObject(1);
		if (resultSet.next()){
			logger.warn("Multiple results for query " +  queryString);
			return null;
		}
		return object;
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
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.ICdmDataSource#executeUpdate(java.lang.String)
	 */
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
				if (! connection.getAutoCommit()){
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
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.ICdmDataSource#startTransaction()
	 */
	@Override
	public void startTransaction() {
		try {
			Connection connection = getConnection();
			connection.setAutoCommit(false);
			this.connection = connection;
	    	return;
		} catch(SQLException e) {
			logger.error("Problems when starting transaction \n" + "Exception: " + e);
			return;
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.ICdmDataSource#commitTransaction()
	 */
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
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.ICdmDataSource#getMetaData()
	 */
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
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.ICdmDataSource#closeOpenConnections()
	 */
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
}
