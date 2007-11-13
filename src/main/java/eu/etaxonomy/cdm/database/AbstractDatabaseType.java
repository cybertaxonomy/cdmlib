/**
 * 
 */
package eu.etaxonomy.cdm.database;

/**
 * @author a.mueller
 *
 */
abstract class AbstractDatabaseType implements IDatabaseType {
	//typeName
	protected String typeName;
	//String for DriverClass
	protected String classString;
	protected String urlString;
	protected int defaultPort;
	
	public String getName(){
		return typeName;
	}
	public String getClassString(){
		return classString;
	}
	
	public String getUrlString(){
		return urlString;
	}
	
	public int getDefaultPort(){
		return defaultPort;
	}
	
	//connection String with default port
	public String getConnectionString(String server, String database){
		return getConnectionString(server, database, defaultPort);
    } 
	
	
}
