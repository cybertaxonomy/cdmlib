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

import org.hibernate.dialect.Dialect;

import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @since 17.12.2008
 * @version 1.0
 */
public interface IDatabaseType {
	/**
	 * @return
	 */
	public String getName();
	/**
	 * @return
	 */
	public String getClassString();
	/**
	 * @return
	 */
	public String getUrlString();
	/**
	 * @return
	 */
	public int getDefaultPort();
	
	/**
	 * Returns the {@link Dialect hibernate dialect}
	 * @return
	 */
	public Dialect getHibernateDialect();
	
	/**
	 * Returns the canoncial (full path) name of the {@link Dialect hibernate dialect} class
	 * @return
	 */
	public String getHibernateDialectCanonicalName();
	
	/**
	 * Returns the connection String for the given parameters, using default port
	 * @param cdmDataSource represents a datasource
	 * @return the connection String
	 */
	public String getConnectionString(ICdmDataSource cdmDataSource);


	/**
	 * Returns the DataSource class that that the datasource needs to create a spring bean
	 * @return the DataSource class
	 */
	public Class<? extends DataSource> getDataSourceClass();
	
	
	/**
	 * Returns the Name of the initialization method to be used when a hibernate datasource is created for this database
	 * @return String name of the init method
	 */
	public String getInitMethod();
	
	/**
	 * Returns the Name of the destroying method to be used when a hibernate datasource representing this database is destroyed
	 * @return String name of the destroy method
	 */
	public String getDestroyMethod();

	/**
	 * Returns the server name for a given connection string. Null or empty string is returned if the 
	 * connection string is invalid
	 * @param connectionString the connection string
	 * @return string representing the server
	 */
	public String getServerNameByConnectionString(String connectionString);
	
	
	/**
	 * Returns the port for a given connection string. If no port is defined the default port is returned.
	 * If connection string is <code>null</code> or unvalid -1 is returned 
	 * @param connectionString the connection string
	 * @return int representing the port number
	 */
	public int getPortByConnectionString(String connectionString);
	
	
	/**
	 * Returns the database name for a given connection string. Null or empty string is returned if the 
	 * connection string is invalid
	 * @param connectionString the connection string
	 * @return string representing the database
	 */
	public abstract String getDatabaseNameByConnectionString(String connectionString);
}
