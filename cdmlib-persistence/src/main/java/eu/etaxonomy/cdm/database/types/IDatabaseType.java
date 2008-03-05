package eu.etaxonomy.cdm.database.types;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

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
	 * Returns the connection String for the given parameters
	 * @param server the server, e.g. IP-Address
	 * @param database the database name on the server (e.g. "testDB")
	 * @param port the port number
	 * @return the connection String
	 */
	public String getConnectionString(String server, String database, int port);
	
	/**
	 * Returns the connection String for the given parameters, using default port
	 * @param server the server, e.g. IP-Address
	 * @param database the database name on the server (e.g. "testDB")
	 * @return the connection String
	 */
	public String getConnectionString(String server, String database);

	/**
	 * Returns the DriverManagerDataSource class that that the datasource needs to create a spring bean
	 * @return the DriverManagerDataSource class
	 */
	public Class<? extends DriverManagerDataSource> getDriverManagerDataSourceClass();
	
}