/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;

/**
 * @author a.mueller
 *
 */
public interface IDatabaseService {

//TODO removed 04.02.2009 as spring 2.5.6 does not support DriverManagerDataSource.getDriverClassName anymore
//Let's see if this is not needed by any other application1
//	/**
//	 * Returns the databaseTypeEnum
//	 * @return
//	 */
//	public DatabaseTypeEnum getDatabaseEnum();
//	
//	
//	/**
//	 * Returns the database driver class name
//	 * @return
//	 */
//	public String getDriverClassName();
	
	/**
	 * Returns the database URL
	 * @return
	 */
	public String getUrl();
	
	/**
	 * Returns the username.
	 * @return
	 */
	public String getUsername();
	
	/**
	 * Set the database connection to the local Hsqldb-database using
	 * the default parameters (defined in Spring-configuration).
	 * Make sure to close the application when exiting the programm, 
	 * otherwise the hsql-server might still be running, if startet by cdmLibrary.
	 * @return true if a connection could be established
	 */
	public boolean useLocalDefaultHsqldb()  throws TermNotFoundException;
	
	/**
	 * Set the database connection to the local Hsqldb-database using
	 * the given parameters. If a parameter is missing, the default parameter
	 * (defined in Spring-configuration) is taken.
	 * Make sure to close the application when exiting the programm, 
	 * otherwise the hsql-server might still be running, if startet by cdmLibrary.
	 * All paramters are optional.
	 * @param path the path to the database files
	 * @param databaseName name of the used database
	 * @param silent if true, messages from the db server are send to stdout 
	 * @param startServer if true, the dbserver is started if the url can't be reached by the dbDriver
	 * @return true if a connection could be established
	 * TODO exceptions
	 */
	public boolean useLocalHsqldb(String databasePath, String databaseName, String username, String password, boolean silent, boolean startServer) throws TermNotFoundException;

	/**
	 * Connect to the database with the given parameters
	 * @param databaseTypeEnum
	 * @param url
	 * @param username
	 * @param password
	 * @param port
	 * @return returns true if successful
	 */
	public boolean connectToDatabase(DatabaseTypeEnum databaseTypeEnum, String server, String database, String username, String password, int port)  throws TermNotFoundException ;

	/**
	 * Connect to the database with the given parameters. Uses default port.
	 * @param databaseTypeEnum
	 * @param url
	 * @param username
	 * @param password
	 * @return returns true if successful
	 */
	public boolean connectToDatabase(DatabaseTypeEnum databaseTypeEnum, String server, String database, String username, String password)  throws TermNotFoundException;
	

	/**
	 * Connect to the database with the given parameters. Uses default port.
	 * @param dataSource
	 * @return returns true if successful
	 */
	public boolean connectToDatasource(CdmPersistentDataSource dataSource) throws TermNotFoundException;

	/**
	 * Saves a new CdmDatasource into the datasource config file.
	 * @param strDataSourceName
	 * @param databaseTypeEnum
	 * @param server
	 * @param database
	 * @param username
	 * @param password
	 * @return the CdmDataSource, null if not successful.
	 */
	public CdmPersistentDataSource saveDataSource(String strDataSourceName, DatabaseTypeEnum databaseTypeEnum, String server, String database, String username, String password) throws TermNotFoundException;
	
	/**
	 * Saves a new hsqldb datasource into the datasource config file.
	 * @param strDataSourceName
	 * @param path
	 * @param database
	 * @param username
	 * @param password
	 * @return the CdmDataSource, null if not successful.
	 */
	public CdmPersistentDataSource saveLocalHsqldb(String strDataSourceName, String path, String database, String username, String password, boolean silent, boolean startServer);
	
	public void setApplicationController(CdmApplicationController cdmApplicationController);
}