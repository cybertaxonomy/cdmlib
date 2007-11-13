package eu.etaxonomy.cdm.database;

interface IDatabaseType {
	public String getName();
	public String getClassString();
	public String getUrlString();
	public int getDefaultPort();
	
	public String getConnectionString(String server, String database, int port);
	public String getConnectionString(String server, String database);
	
}