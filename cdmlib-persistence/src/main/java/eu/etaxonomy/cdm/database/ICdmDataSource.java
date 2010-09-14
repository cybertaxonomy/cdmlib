/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.cache.CacheProvider;
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
	public BeanDefinition getHibernatePropertiesBean(DbSchemaValidation hbm2dll, Boolean showSql, Boolean formatSql, Boolean registerSearchListener, Class<? extends CacheProvider> cacheProviderClass);

	
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
	 * @throws Exception 
	 */
	public boolean testConnection() throws DataSourceNotFoundException;
	
	public String getUsername();
	
	public String getPassword();
	
	 /**
     * Executes a query and returns the ResultSet.
     * @return ResultSet for the query.
     */
	public ResultSet executeQuery (String query);
	
    /**
     * Executes an update
     * @return return code
     */
	public int executeUpdate (String sqlUpdate);

	public NomenclaturalCode getNomenclaturalCode();

	public Object getSingleValue(String query) throws SQLException;	
	
//
//	public void setFilePath(String filePath);
//	
//	public void setMode(H2Mode mode);

}