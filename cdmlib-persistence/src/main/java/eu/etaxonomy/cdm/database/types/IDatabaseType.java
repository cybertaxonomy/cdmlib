/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database.types;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import eu.etaxonomy.cdm.database.ICdmDataSource;

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
	 * @return
	 */
	public String getHibernateDialect();
	
	/**
	 * Returns the connection String for the given parameters, using default port
	 * @param server the server, e.g. IP-Address
	 * @param database the database name on the server (e.g. "testDB")
	 * @return the connection String
	 */
	public String getConnectionString(ICdmDataSource cdmDataSource);

	/**
	 * Returns the DriverManagerDataSource class that that the datasource needs to create a spring bean
	 * @return the DriverManagerDataSource class
	 */
	public Class<? extends DriverManagerDataSource> getDriverManagerDataSourceClass();
	
	
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

	
}