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

import javax.sql.DataSource;

import org.hibernate.cache.spi.RegionFactory;
import org.springframework.beans.factory.config.BeanDefinition;

import eu.etaxonomy.cdm.config.ICdmSource;
import eu.etaxonomy.cdm.persistence.hibernate.HibernateConfiguration;

public interface ICdmDataSource  extends DataSource,ICdmSource {

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
	 * @param registerSearchListener
	 * @param cacheProviderClass
	 * @return
	 * @deprecated use {@link #getHibernatePropertiesBean(DbSchemaValidation, Boolean, Boolean, Boolean, Boolean, Class)} instead
	 */
	@Deprecated
    public BeanDefinition getHibernatePropertiesBean(DbSchemaValidation hbm2dll, Boolean showSql,
            Boolean formatSql, Boolean registerSearchListener, Class<? extends RegionFactory> cacheProviderClass);

	/**
	 * @param hbm2dll schema validation
	 * @param hibernateConfig the hibernate configuration
	 * @return the computed {@link BeanDefinition bean definition}
	 */
	public BeanDefinition getHibernatePropertiesBean(DbSchemaValidation hbm2dll,
	        HibernateConfiguration hibernateConfig);

	/**
	 * @return
	 */
	public String getFilePath();

	/**
	 * @return
	 */
	public H2Mode getMode();

	public String getUsername();

	public String getPassword();

	public String getDatabase();

	public void setMode(H2Mode h2Mode);

	public void setUsername(String username);

	public void setPassword(String password);

	public void setDatabase(String database);


	/**
	 * Returns the database type of the data source.
	 * @return the database type of the data source.
	 * <code>null</code> if the bean or the driver class property does not exist or the driver class is unknown.
	 */
	public DatabaseTypeEnum getDatabaseType();
	/**
	 * Tests, if a database connection can be established.
	 * @return true if test was successful, false otherwise
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws Exception
	 */
	public boolean testConnection() throws ClassNotFoundException, SQLException;


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


	/**
	 * Returns the first value of the first row of a result set.<BR>
	 * If no row exists in the result set
	 * <code>null</code> is returned.
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

}
