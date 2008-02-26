/**
 * 
 */
package eu.etaxonomy.cdm.database.types;


/**
 * @author a.mueller
 *
 */
public class SqlServer2005DatabaseType extends AbstractDatabaseType {

	//name
	protected String typeName = "SQL Server";
	
	//driver class
	protected String classString = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    
	//url
	protected String urlString = "jdbc:sqlserver://";
    
	//[serverName[\instanceName][:portNumber]][;property=value[;property=value]]
	
    //default port
    private int defaultPort = 1433;
    
    //hibernate dialect
    private String hibernateDialect = "SQLServerDialect";
 
    public String getConnectionString(String server, String database, int port){
    	return getConnectionString(server, database, port, null);
    }
    
    public String getConnectionString(String server, String database, int port, String instanceName){
    	String instance = "";
    	if (instanceName != null && ! instanceName.equals("")){
			instance = "\\" + instanceName;
		}
    	return urlString + server + instance + ":" + port + ";databaseName=" + database+";SelectMethod=cursor";
    }
	
	//Constructor
    public SqlServer2005DatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}

}