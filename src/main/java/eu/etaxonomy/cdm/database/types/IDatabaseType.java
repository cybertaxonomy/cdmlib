package eu.etaxonomy.cdm.database.types;

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
	
}