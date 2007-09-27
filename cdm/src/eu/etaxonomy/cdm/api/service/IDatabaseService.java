/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import eu.etaxonomy.cdm.database.DbType;

/**
 * @author a.mueller
 *
 */
public interface IDatabaseService extends IService {
	
	/**
	 * @return
	 */
	public String getDriverClassName();
	
	/**
	 * @return
	 */
	public String getUrl();
	
	/**
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
	public boolean useLocalHsqldb();
	
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
	public boolean useLocalHsqldb(String path, String databaseName, boolean silent, boolean startServer);

	/**
	 * Connect to the database with the given parameters
	 * @param dbType
	 * @param url
	 * @param username
	 * @param password
	 * @param port
	 * @return
	 */
	public boolean connectToDatabase(DbType dbType, String url, String username, String password, int port);
	
}
