/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;

/**
 * @author a.mueller
 *
 */
public interface IDatabaseService extends IService {

	/**
	 * Returns the databaseTypeEnum
	 * @return
	 */
	public DatabaseTypeEnum getDatabaseEnum();
	
	
	/**
	 * Returns the database driver class name
	 * @return
	 */
	public String getDriverClassName();
	
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
	public boolean useLocalDefaultHsqldb();
	
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
	public boolean useLocalHsqldb(String databasePath, String databaseName, String username, String password, boolean silent, boolean startServer);

	/**
	 * Connect to the database with the given parameters
	 * @param databaseTypeEnum
	 * @param url
	 * @param username
	 * @param password
	 * @param port
	 * @return returns true if successful
	 */
	public boolean connectToDatabase(DatabaseTypeEnum databaseTypeEnum, String server, String database, String username, String password, int port);

	/**
	 * Connect to the database with the given parameters. Uses default port.
	 * @param databaseTypeEnum
	 * @param url
	 * @param username
	 * @param password
	 * @return returns true if successful
	 */
	public boolean connectToDatabase(DatabaseTypeEnum databaseTypeEnum, String server, String database, String username, String password);
	

	/**
	 * Connect to the database with the given parameters. Uses default port.
	 * @param dataSource
	 * @return returns true if successful
	 */
	public boolean connectToDatasource(CdmDataSource dataSource);

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
	public CdmDataSource saveDataSource(String strDataSourceName, DatabaseTypeEnum databaseTypeEnum, String server, String database, String username, String password);
	
	/**
	 * Saves a new hsqldb datasource into the datasource config file.
	 * @param strDataSourceName
	 * @param path
	 * @param database
	 * @param username
	 * @param password
	 * @return the CdmDataSource, null if not successful.
	 */
	public CdmDataSource saveLocalHsqldb(String strDataSourceName, String path, String database, String username, String password, boolean silent, boolean startServer);
	
	public void setApplicationController(CdmApplicationController cdmApplicationController);
}