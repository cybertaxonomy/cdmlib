/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.cache.spi.RegionFactory;
import org.springframework.beans.factory.config.BeanDefinition;

import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

public interface ICdmDataSource {

	/**
	 * Returns a BeanDefinition object of type  DataSource that contains
	 * datsource properties (url, username, password, ...)
	 * @return BeanDefinition
	 */
	public BeanDefinition getDatasourceBean();
	
	/**
	 * @param hbm2dll
	 * @return BeanDefinition
	 */
	public BeanDefinition getHibernatePropertiesBean(DbSchemaValidation hbm2dll);
	
	/**
	 * @param hbm2dll
	 * @param showSql
	 * @param formatSql
	 * @param cacheProviderClass
	 * @return BeanDefinition
	 */
	public BeanDefinition getHibernatePropertiesBean(DbSchemaValidation hbm2dll, Boolean showSql, Boolean formatSql, Boolean registerSearchListener, Class<? extends RegionFactory> cacheProviderClass);

	
	/**
	 * The name representation of thie Datasource.
	 * @return
	 */
	public String getName();
	

	/**
	 * @return
	 */
	public String getServer();

	/**
	 * @return
	 */
	public String getDatabase();
	
	public DatabaseTypeEnum getDatabaseType();
	
	/**
	 * @return
	 */
	public int getPort();

	/**
	 * @return
	 */
	public String getFilePath();

	/**
	 * @return
	 */
	public H2Mode getMode();
	
	/**
	 * Tests, if a database connection can be established.
	 * @return true if test was successful, false otherwise
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws Exception 
	 */
	public boolean testConnection() throws ClassNotFoundException, SQLException;
	
	public String getUsername();
	
	public String getPassword();
	
	 /**
     * Executes a query and returns the ResultSet.
     * @return ResultSet for the query.
	 * @throws SQLException 
     */
	public ResultSet executeQuery (String query) throws SQLException;
	
    /**
     * Executes an update
     * @return return code
     */
	public int executeUpdate (String sqlUpdate) throws SQLException;
	
	/**
	 * Starts a transaction for the given datasource.
	 */
	public void startTransaction();
	
	/**
	 * Commits the transaction for the given datasource.
	 * @return
	 * @throws SQLException 
	 */
	public void commitTransaction() throws SQLException;
	
	/**
	 * Rolls the connection back.
	 * @throws SQLException 
	 */
	public void rollback() throws SQLException;


	public NomenclaturalCode getNomenclaturalCode();

	/**
	 * Returns a single the first value of a row of a resultset.
	 * 
	 * <strong>Caution</strong> This method opens a connection on first use. Subsequent calls will use the same connection.
	 * Please close the connection when not needed anymore with {@link ICdmDataSource#closeOpenConnections()}
	 * 
	 * @param query
	 * @return
	 * @throws SQLException
	 */
	public Object getSingleValue(String query) throws SQLException;	
	
	/**
	 * Returns {@link DatabaseMetaData} for <code>this</code> datasource.
	 * 
	 * <br>
	 * <br>
	 * <strong>Caution</strong> This method opens a connection that should be closed
	 * with {@link #closeOpenConnections()}
	 * 
	 * @return
	 */
	public DatabaseMetaData getMetaData();
	
	/**
	 * 
	 */
	public void closeOpenConnections();

	
//
//	public void setFilePath(String filePath);
//	
//	public void setMode(H2Mode mode);

}