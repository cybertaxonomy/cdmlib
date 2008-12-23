/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import org.hibernate.cache.CacheProvider;
import org.springframework.beans.factory.config.BeanDefinition;

import eu.etaxonomy.cdm.database.DbSchemaValidation;

public interface ICdmDataSource {

	/**
	 * Returns a BeanDefinition object of type  DriverManagerDataSource that contains
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
	public BeanDefinition getHibernatePropertiesBean(DbSchemaValidation hbm2dll, Boolean showSql, Boolean formatSql, Class<? extends CacheProvider> cacheProviderClass);

	
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
	 */
	public boolean testConnection();
	
	public String getUsername();
	
	public String getPassword();
	
	
//
//	public void setFilePath(String filePath);
//	
//	public void setMode(H2Mode mode);

}